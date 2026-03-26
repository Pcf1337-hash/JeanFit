package com.jeanfit.app.data.repository

import com.jeanfit.app.BuildConfig
import com.jeanfit.app.data.api.ClaudeApi
import com.jeanfit.app.data.api.ClaudeMessage
import com.jeanfit.app.data.api.ClaudeRequest
import com.jeanfit.app.data.db.dao.CoachDao
import com.jeanfit.app.data.db.dao.FoodDao
import com.jeanfit.app.data.db.dao.WeightDao
import com.jeanfit.app.data.db.entities.CoachMemory
import com.jeanfit.app.data.db.entities.CoachMessage
import com.jeanfit.app.data.db.entities.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachRepository @Inject constructor(
    private val api: ClaudeApi,
    private val dao: CoachDao,
    private val userRepository: UserRepository,
    private val weightDao: WeightDao,
    private val foodDao: FoodDao
) {
    companion object {
        private const val CONTEXT_WINDOW = 20   // Letzte N Nachrichten als API-Kontext
        private const val MAX_MEMORY_ENTRIES = 30

        private val OFFLINE_FALLBACKS = listOf(
            "Du machst das super! Auch kleine Schritte führen ans Ziel 💪",
            "Denk dran: Fortschritt, nicht Perfektion ist das Ziel 🌟",
            "Trink ein Glas Wasser – oft hilft das schon! 💧",
            "Jeder Tag, an dem du trackst, ist ein Gewinn 📊",
            "Du bist bereits ein Schritt weiter als gestern 🚀"
        )
    }

    fun getChatHistory(): Flow<List<CoachMessage>> = dao.getAllMessages()
    fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()

    suspend fun sendMessage(userInput: String): Flow<String> = flow {
        // 1. User-Nachricht sofort lokal speichern
        dao.insertMessage(CoachMessage(content = userInput, isFromCoach = false))

        // 2. Kontext aufbauen
        val profile = userRepository.getProfileOnce()
        val systemPrompt = buildSystemPrompt(profile)
        val history = buildConversationHistory()

        // 3. Claude API aufrufen
        val response = api.sendMessage(
            apiKey = BuildConfig.CLAUDE_API_KEY,
            request = ClaudeRequest(
                system = systemPrompt,
                messages = history
            )
        )

        val reply = if (response.isSuccessful) {
            response.body()?.content?.firstOrNull()?.text
                ?: OFFLINE_FALLBACKS.random()
        } else {
            OFFLINE_FALLBACKS.random()
        }

        // 4. Antwort speichern
        dao.insertMessage(CoachMessage(content = reply, isFromCoach = true))

        // 5. Gedächtnis aus Antwort extrahieren (Background-Task)
        extractAndSaveMemory(userInput, reply)

        emit(reply)
    }.catch { _ ->
        val fallback = OFFLINE_FALLBACKS.random()
        dao.insertMessage(CoachMessage(content = fallback, isFromCoach = true))
        emit(fallback)
    }

    suspend fun sendGreeting(): String {
        val profile = userRepository.getProfileOnce()
        val greeting = buildGreeting(profile)
        dao.insertMessage(CoachMessage(content = greeting, isFromCoach = true, messageType = "greeting"))
        return greeting
    }

    suspend fun markAllRead() = dao.markAllCoachMessagesRead()

    suspend fun clearChat() = dao.clearAllMessages()

    // ================================================================
    // System-Prompt mit vollem Nutzer-Kontext + Langzeit-Gedächtnis
    // ================================================================

    private suspend fun buildSystemPrompt(profile: UserProfile?): String {
        val memories = dao.getAllMemories()
        val memorySection = if (memories.isNotEmpty()) {
            val grouped = memories.groupBy { it.category }
            buildString {
                appendLine("\nWas du über ${profile?.name ?: "den Nutzer"} weißt:")
                grouped["milestone"]?.let { m ->
                    appendLine("Meilensteine: ${m.joinToString(", ") { it.value }}")
                }
                grouped["challenge"]?.let { m ->
                    appendLine("Herausforderungen: ${m.joinToString(", ") { "${it.key}: ${it.value}" }}")
                }
                grouped["preference"]?.let { m ->
                    appendLine("Vorlieben: ${m.joinToString(", ") { "${it.key}: ${it.value}" }}")
                }
                grouped["fact"]?.let { m ->
                    appendLine("Sonstige Fakten: ${m.joinToString(", ") { "${it.key}: ${it.value}" }}")
                }
            }
        } else ""

        val weightLost = if (profile != null) {
            val lastWeight = weightDao.getLatestEntryOnce()
            if (lastWeight != null) profile.startWeightKg - lastWeight.weightKg else 0f
        } else 0f

        val todayEntries = foodDao.getEntriesForDayOnce(LocalDate.now().toEpochDay())
        val todayKcal = todayEntries.sumOf { it.calories.toDouble() }.toFloat()

        return buildString {
            appendLine("Du bist Jean, ein einfühlsamer, motivierender Fitness- und Ernährungs-Coach in der JeanFit-App.")
            appendLine("Du antwortest IMMER auf Deutsch, kurz und klar (2-4 Sätze), warmherzig und wissenschaftlich fundiert.")
            appendLine("Nutze gelegentlich passende Emojis – aber nicht übertreiben.")
            appendLine("")
            appendLine("NUTZER-PROFIL:")
            if (profile != null) {
                appendLine("- Name: ${profile.name.ifBlank { "du" }}")
                appendLine("- Ziel: ${profile.startWeightKg}kg → ${profile.goalWeightKg}kg")
                appendLine("- Bereits abgenommen: ${"%.1f".format(weightLost.coerceAtLeast(0f))} kg")
                appendLine("- Tageskalorien-Ziel: ${profile.dailyCalorieGoal} kcal")
                appendLine("- Heute geloggt: ${todayKcal.toInt()} kcal")
                appendLine("- Aktivitätslevel: ${profile.activityLevel}")
                if (profile.motivationText.isNotBlank()) {
                    appendLine("- Motivation: ${profile.motivationText}")
                }
                if (profile.bigPicture.isNotBlank()) {
                    appendLine("- Großes Ziel: ${profile.bigPicture}")
                }
            }
            append(memorySection)
            appendLine("")
            appendLine("REGELN:")
            appendLine("- Keine medizinischen Diagnosen oder Therapieempfehlungen")
            appendLine("- Bei Zeichen von Essstörungen: sanft professionelle Hilfe empfehlen")
            appendLine("- Immer konkrete, umsetzbare Tipps geben")
            appendLine("- Den Nutzer beim Namen ansprechen wenn bekannt")
            appendLine("- Auf den heutigen Fortschritt eingehen wenn relevant")
            appendLine("- Du erinnerst dich an frühere Gespräche und beziehst dich darauf")
        }
    }

    private suspend fun buildConversationHistory(): List<ClaudeMessage> {
        val messages = dao.getLastMessages(CONTEXT_WINDOW).reversed()
        return messages.map { msg ->
            ClaudeMessage(
                role = if (msg.isFromCoach) "assistant" else "user",
                content = msg.content
            )
        }
    }

    private suspend fun buildGreeting(profile: UserProfile?): String {
        val name = profile?.name?.ifBlank { null }?.let { ", $it" } ?: ""
        val hour = java.time.LocalTime.now().hour
        val greeting = when {
            hour < 11 -> "Guten Morgen$name! ☀️"
            hour < 17 -> "Hallo$name! 👋"
            else       -> "Guten Abend$name! 🌙"
        }
        return "$greeting Ich bin Jean, dein persönlicher Coach. Wie kann ich dir heute helfen?"
    }

    /**
     * Extrahiert wichtige Infos aus dem Gespräch und speichert sie als Gedächtnis.
     * Jean merkt sich z.B. Lieblingsessen, Schwachstellen, Erfolge.
     */
    private suspend fun extractAndSaveMemory(userInput: String, coachReply: String) {
        val lower = userInput.lowercase()

        // Meilensteine
        if (lower.contains("geschafft") || lower.contains("erreicht") || lower.contains("abgenommen")) {
            dao.saveMemory(CoachMemory(
                key = "last_milestone_${System.currentTimeMillis()}",
                value = userInput.take(100),
                category = "milestone"
            ))
        }

        // Herausforderungen
        val challenges = mapOf(
            "plateau" to "Gewichtsplateau",
            "stress" to "Stress-Essen",
            "hunger" to "Hungergefühl abends",
            "süßigkeiten" to "Heißhunger auf Süßes",
            "schokolade" to "Heißhunger auf Schokolade",
            "motivat" to "Motivationsprobleme"
        )
        challenges.entries.firstOrNull { lower.contains(it.key) }?.let { (key, value) ->
            dao.saveMemory(CoachMemory(key = "challenge_$key", value = value, category = "challenge"))
        }

        // Präferenzen (Lieblingsessen etc.)
        if (lower.contains("liebe") || lower.contains("mag gern") || lower.contains("esse gerne")) {
            dao.saveMemory(CoachMemory(
                key = "food_preference_${System.currentTimeMillis()}",
                value = userInput.take(80),
                category = "preference"
            ))
        }
    }
}
