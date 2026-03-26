package com.jeanfit.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.Course
import com.jeanfit.app.data.db.entities.Lesson
import com.jeanfit.app.data.db.entities.LessonProgress
import com.jeanfit.app.data.repository.LessonRepository
import com.jeanfit.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearnUiState(
    val courses: List<Course> = emptyList(),
    val completedLessonIds: Set<String> = emptySet(),
    val coins: Int = 0,
    val isLoading: Boolean = true
)

data class LessonListUiState(
    val course: Course? = null,
    val lessons: List<Lesson> = emptyList(),
    val completedIds: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

data class LessonReaderUiState(
    val lesson: Lesson? = null,
    val progress: LessonProgress? = null,
    val currentQuizIndex: Int = -1,
    val quizAnswers: Map<Int, Int> = emptyMap(),
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val learnState: StateFlow<LearnUiState> = combine(
        lessonRepository.getAllCourses(),
        lessonRepository.getAllCompletedProgress(),
        userRepository.getCoins()
    ) { courses, progress, coins ->
        LearnUiState(
            courses = courses,
            completedLessonIds = progress.map { it.lessonId }.toSet(),
            coins = coins ?: 0,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnUiState())

    private val _selectedCourseId = MutableStateFlow<String?>(null)
    val lessonListState: StateFlow<LessonListUiState> = combine(
        _selectedCourseId.filterNotNull().flatMapLatest { courseId ->
            lessonRepository.getLessonsForCourse(courseId)
        },
        lessonRepository.getAllCompletedProgress(),
        learnState.map { it.courses }
    ) { lessons, progress, courses ->
        LessonListUiState(
            course = courses.find { it.courseId == _selectedCourseId.value },
            lessons = lessons,
            completedIds = progress.map { it.lessonId }.toSet(),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LessonListUiState())

    private val _selectedLessonId = MutableStateFlow<String?>(null)
    val lessonReaderState: StateFlow<LessonReaderUiState> = _selectedLessonId
        .filterNotNull()
        .flatMapLatest { lessonId ->
            lessonRepository.getProgressForLesson(lessonId).map { progress ->
                val lesson = lessonRepository.getLessonById(lessonId)
                LessonReaderUiState(
                    lesson = lesson,
                    progress = progress,
                    isCompleted = progress?.isCompleted == true,
                    isLoading = false
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LessonReaderUiState())

    fun selectCourse(courseId: String) { _selectedCourseId.value = courseId }
    fun selectLesson(lessonId: String) { _selectedLessonId.value = lessonId }

    fun completeLesson(lessonId: String, coinsReward: Int = 0) {
        viewModelScope.launch {
            lessonRepository.markLessonCompleted(lessonId)
            if (coinsReward > 0) userRepository.addCoins(coinsReward)
        }
    }

    init {
        viewModelScope.launch {
            // Seed initial courses if empty
            if (lessonRepository.getAllCourses().first().isEmpty()) {
                seedInitialContent()
            }
        }
    }

    private suspend fun seedInitialContent() {
        val courses = listOf(
            Course("course_1", "Grundlagen der Ernährung", "Kalorien, Nährstoffe und das Ampelsystem — die Basis für deinen Erfolg", 1, "🍎", 5, 0),
            Course("course_2", "Dein Gehirn und das Essen", "Warum wir essen, was wir essen — Psychologie und Emotionen", 2, "🧠", 5, 0),
            Course("course_3", "Gewohnheiten aufbauen", "Wie nachhaltige Routinen entstehen und wie du sie nutzt", 3, "🔄", 5, 3),
            Course("course_4", "Bewegung und Stoffwechsel", "Wie Bewegung deinen Körper und deine Kalorien beeinflusst", 4, "🏃", 5, 5),
            Course("course_5", "Nachhaltig abnehmen", "Warum Diäten scheitern — und was wirklich funktioniert", 5, "📉", 5, 7),
            Course("course_6", "Einkaufen & Meal-Prep", "Gesund essen mit wenig Zeit und ohne Stress", 6, "🛒", 5, 9),
            Course("course_7", "Selbstfürsorge & Mindset", "Körperbild, Selbstwert und das richtige Mindset zum Erfolg", 7, "💜", 5, 12)
        )

        val lessons = listOf(
            // ── KURS 1: Grundlagen der Ernährung ─────────────────────────────────────
            Lesson("l1_1", "course_1", "Was ist Kaloriendichte?", 0, 5,
                """[{"type":"text","content":"## Das Ampelsystem\n\nKaloriendichte = Kalorien pro 100g Lebensmittel.\n\n🟢 **Grün** (≤ 1,0 kcal/g): Gemüse, Salat, Beeren, Brühen\n🟡 **Gelb** (1,0–2,4 kcal/g): Vollkorn, mageres Fleisch, Hülsenfrüchte\n🟠 **Orange** (> 2,4 kcal/g): Nüsse, Öle, Käse, Süßes\n\nDas Ziel: Mehr Grün, mäßig Gelb, wenig Orange — nicht alles verbieten!"},{"type":"tip","icon":"💡","content":"Fülle deinen Teller zu **mindestens 50%** mit grünen Lebensmitteln. Damit kannst du satt werden ohne das Kalorienziel zu sprengen."},{"type":"quiz","question":"Welcher Wert bestimmt die Farb-Kategorie eines Lebensmittels?","options":["Kalorien pro Portion","Kalorien pro 100g","Proteingehalt"],"correctIndex":1,"explanation":"Die Kaloriendichte (kcal pro 100g) bestimmt die Farbe. Ein Lebensmittel mit 150 kcal/100g ist Gelb, egal wie groß die Portion ist."},{"type":"activity","title":"Schau in deine Küche","instruction":"Nimm 3 Lebensmittel aus deinem Kühlschrank und schau auf die Nährwerttabelle. Wie viele kcal/100g haben sie? Welche Farbe gehören sie?","action":"navigate_food_log"}]""",
                "article", 0),
            Lesson("l1_2", "course_1", "Makronährstoffe verstehen", 1, 7,
                """[{"type":"text","content":"## Protein, Kohlenhydrate, Fett\n\n**Protein** (4 kcal/g)\n- Sättigt am stärksten\n- Erhält Muskelmasse beim Abnehmen\n- Quellen: Fleisch, Fisch, Eier, Hülsenfrüchte, Quark\n\n**Kohlenhydrate** (4 kcal/g)\n- Bevorzugter Treibstoff fürs Gehirn\n- Vollkorn > Weißmehl (mehr Ballaststoffe, länger satt)\n- Quellen: Vollkornbrot, Haferflocken, Reis, Kartoffeln\n\n**Fett** (9 kcal/g)\n- Mehr als doppelt so kalorienreich!\n- Wichtig für Hormone und fettlösliche Vitamine\n- Gesunde Quellen: Olivenöl, Avocado, Nüsse, Lachs"},{"type":"tip","icon":"💡","content":"Ziel: **30% Protein, 40% Kohlenhydrate, 30% Fett** — aber perfekte Zahlen sind weniger wichtig als das große Bild."},{"type":"quiz","question":"Warum sättigt Protein besonders gut?","options":["Es hat weniger Kalorien als Fett","Es stimuliert Sättigungshormone am stärksten","Es verdaut sich am langsamsten"],"correctIndex":1,"explanation":"Protein stimuliert Hormone wie GLP-1 und PYY, die dem Gehirn 'satt' signalisieren — stärker als Kohlenhydrate oder Fett."}]""",
                "article", 0),
            Lesson("l1_3", "course_1", "Portionsgrößen ohne Waage", 2, 5,
                """[{"type":"text","content":"## Die Hand-Methode\n\nOhne Waage und Küchen-Stress:\n\n✋ **Handfläche** = ca. 100g Protein (Fleisch, Fisch)\n✊ **Faust** = ca. 150g Kohlenhydrate (Nudeln, Reis gekocht)\n👍 **Daumen** = ca. 15g Fett (Öl, Nussbutter)\n🤲 **Zwei Hände** = Gemüse (unbegrenzt!)\n\nFür eine ausgewogene Mahlzeit: 1× Protein, 1× Kohlenhydrate, 1× Fett, 2× Gemüse"},{"type":"tip","icon":"💡","content":"Restaurant-Portionen sind oft 2–3× zu groß. Iss die Hälfte und pack den Rest ein — so sparst du 400–600 kcal ohne Hunger."},{"type":"activity","title":"Schätzen und Prüfen","instruction":"Beim nächsten Essen: Schätze erst die Gramm-Menge mit der Hand-Methode, dann wiege wirklich nach. Wie genau warst du?","action":"navigate_food_log"}]""",
                "article", 0),
            Lesson("l1_4", "course_1", "Sattheit und Hunger verstehen", 3, 6,
                """[{"type":"text","content":"## Zwei Arten von Hunger\n\n**Physischer Hunger** entsteht 3–5 Stunden nach der letzten Mahlzeit. Kommt langsam, akzeptiert viele verschiedene Speisen.\n\n**Heißhunger / Appetit** kommt plötzlich, will oft genau ein Ding (Schokolade, Chips). Entsteht meist aus Emotion, Stress oder Langeweile.\n\n## Sättigung braucht Zeit\n\nDas Sättigungssignal braucht **15–20 Minuten** um vom Magen zum Gehirn zu gelangen. Wer schnell isst, überfrisst sich regelmäßig."},{"type":"tip","icon":"💡","content":"Lege beim Essen das Handy weg, kaue jeden Bissen 15–20× und mach nach 10 Minuten eine kurze Pause. Du wirst merken, dass du weniger isst."},{"type":"quiz","question":"Wie lange braucht das Sättigungssignal vom Magen zum Gehirn?","options":["2–3 Minuten","15–20 Minuten","45–60 Minuten"],"correctIndex":1,"explanation":"Deshalb überessen wir uns beim schnellen Essen — das Gehirn hat noch kein 'Stopp'-Signal bekommen."},{"type":"reflection","prompt":"Isst du eher langsam und bewusst oder schnell und nebenbei? Was könntest du beim nächsten Essen anders machen?","placeholder":"Meine Gedanken..."}]""",
                "article", 0),
            Lesson("l1_5", "course_1", "Die Ampel im Alltag nutzen", 4, 5,
                """[{"type":"text","content":"## Praktische Umsetzung\n\nDu musst nicht alles perfekt machen. Das **80/20-Prinzip** reicht:\n\n- 80% der Zeit: bewusst grüne und gelbe Lebensmittel wählen\n- 20% der Zeit: Genuss ohne schlechtes Gewissen\n\n## Typische Fallen\n\n🚫 **Dressings und Soßen**: Sind oft orange, obwohl der Salat grün wäre\n🚫 **Snacks**: Nüsse sind orange, nicht grün (viele unterschätzen das)\n🚫 **Getränke**: Smoothies, Fruchtsäfte und Kaffee-Drinks haben viele Kalorien\n\n✅ **Grüne Helfer**: Wasser, Kräutertee, Gemüsebrühe satt und kalorienfrei"},{"type":"quiz","question":"Ein Salat mit 50g Olivenöl-Dressing hat welche Farbe?","options":["Grün (wegen des Salats)","Orange (wegen des Öls)","Gelb (Mischung)"],"correctIndex":1,"explanation":"Olivenöl hat ~900 kcal/100g — eindeutig orange. Dressings können einen grünen Salat zur Kalorienbombe machen."}]""",
                "article", 1),

            // ── KURS 2: Psychologie des Essens ────────────────────────────────────────
            Lesson("l2_1", "course_2", "Der Elefant und der Reiter", 0, 6,
                """[{"type":"text","content":"## Zwei Systeme in deinem Kopf\n\n**Der Elefant** = dein emotionales, impulsives System. Reagiert sofort auf Belohnungen, Stress, Gewohnheiten. Er ist stark und schnell.\n\n**Der Reiter** = dein rationales System. Plant, denkt langfristig, kennt deine Ziele. Er ist klug, aber schwach gegen den Elefanten.\n\nBeide haben ihren Platz. Aber beim Essen gewinnt oft der Elefant — und das ist okay, wenn du es weißt."},{"type":"tip","icon":"💡","content":"Du kämpfst nicht gegen dich selbst — du lenkst nur den Elefanten. Statt Willenskraft brauchst du **clevere Umgebungsgestaltung**: Gesundes sichtbar, Ungesundes versteckt."},{"type":"reflection","prompt":"In welchen Situationen gewinnt dein 'Elefant' beim Essen? (Stress? Langeweile? Abends vor dem TV?)","placeholder":"Schreib ehrlich, was dich auslöst..."}]""",
                "article", 0),
            Lesson("l2_2", "course_2", "Emotionales Essen erkennen", 1, 7,
                """[{"type":"text","content":"## Warum wir aus Emotion essen\n\nEssen ist eine der zuverlässigsten Methoden, um kurzfristig besser zu fühlen. Das Gehirn lernt schnell: **Stress → Essen → besser fühlen**.\n\nHäufige Auslöser:\n- 😰 **Stress**: Cortisol steigert Heißhunger auf Zucker und Fett\n- 😔 **Traurigkeit**: Schokolade erhöht kurz den Serotonin-Spiegel\n- 😤 **Langeweile**: Essen füllt Leere und gibt Stimulation\n- 🎉 **Freude**: Feiern mit Essen ist kulturell tief verankert"},{"type":"tip","icon":"💡","content":"Führe 3 Tage lang ein 'Hunger-Tagebuch': Bevor du isst, notiere kurz: Bin ich wirklich hungrig (1-10)? Was fühle ich gerade?"},{"type":"quiz","question":"Was ist der häufigste Auslöser für emotionales Essen?","options":["Echten Hunger","Cortisol-Anstieg durch Stress","Zu wenig Schlaf"],"correctIndex":1,"explanation":"Stress erhöht das Hormon Cortisol, das direkt Heißhunger auf kalorienreiche Nahrung auslöst — ein Überbleibsel der Evolution."},{"type":"reflection","prompt":"Was ist dein persönlicher Haupt-Trigger für Heißhunger? Was hilft dir stattdessen?","placeholder":"Ehrliche Antwort..."}]""",
                "article", 0),
            Lesson("l2_3", "course_2", "Sättigungssignale hören", 2, 5,
                """[{"type":"text","content":"## Achtsamkeit beim Essen\n\nUnser Körper sendet ständig Sättigungssignale — wir überhören sie oft, weil wir:\n- Zu schnell essen\n- Abgelenkt sind (TV, Handy)\n- Uns 'gezwungen' fühlen, den Teller leer zu essen\n\n## Die Hunger-Skala (1–10)\n\n1–3: Wirklich hungrig, Energie niedrig → Essen\n4–5: Leichter Hunger → bald essen\n6–7: Neutral, zufrieden → ideal aufhören!\n8–10: Übersatt, unwohl → zu viel gegessen\n\nZiel: Zwischen 4 und 7 bleiben."},{"type":"activity","title":"Die Hunger-Skala testen","instruction":"Beim nächsten Essen: Rate deinen Hunger vor, während (nach der Hälfte), und nach der Mahlzeit. Notiere die Zahlen.","action":"navigate_food_log"}]""",
                "article", 0),
            Lesson("l2_4", "course_2", "Trigger und Auslöser kennen", 3, 6,
                """[{"type":"text","content":"## Deine persönlichen Trigger\n\nJeder hat andere Auslöser für ungesundes Essen. Die häufigsten:\n\n🕗 **Zeit-Trigger**: Abends nach 20 Uhr, nachmittags um 15 Uhr\n📍 **Orts-Trigger**: Auf dem Sofa, im Auto, vor dem PC\n😔 **Gefühls-Trigger**: Stress, Einsamkeit, Frust\n🫂 **Soziale Trigger**: Geburtstage, Feiern, Essen mit anderen\n\n## Strategie: Trigger → Alternative\n\nStatt Trigger bekämpfen: Alternative Reaktion einüben.\nBeispiel: **Stress → Chips** wird zu **Stress → Tee + 5 Min. spazieren**"},{"type":"tip","icon":"💡","content":"Notiere deine Top-3-Trigger und schreibe für jeden eine realistische Alternative auf. Nicht 'Sport machen' — was kannst du wirklich tun?"},{"type":"reflection","prompt":"Was sind deine 3 größten Trigger? Welche Alternative könntest du ausprobieren?","placeholder":"Trigger 1: ... Alternative: ..."}]""",
                "article", 0),
            Lesson("l2_5", "course_2", "Achtsamkeit beim Essen", 4, 8,
                """[{"type":"text","content":"## Mindful Eating — Was ist das?\n\nAchtsamkeit beim Essen bedeutet: **vollständig präsent sein** während du isst. Keine Ablenkung, kein Stress, nur Essen.\n\nStudien zeigen: Wer achtsam isst, nimmt automatisch **15–30% weniger Kalorien** zu sich — ohne Diät.\n\n## 5 Regeln fürs achtsame Essen\n\n1. Kein Handy, kein TV — nur Essen\n2. Vor jedem Bissen: kurz wahrnehmen, wie das Essen aussieht und riecht\n3. Jeden Bissen 20× kauen\n4. Besteck zwischendurch ablegen\n5. Vor dem zweiten Nachschlag: 10 Minuten warten"},{"type":"tip","icon":"💡","content":"Du musst nicht jede Mahlzeit achtsamkeitsvoll essen. Probiere es erst mit **einem Meal pro Tag** — zum Beispiel dem Mittagessen."},{"type":"quiz","question":"Wie viel weniger essen achtsame Esser laut Studien im Durchschnitt?","options":["5–10%","15–30%","50%"],"correctIndex":1,"explanation":"15–30% weniger Kalorien, ohne Hunger, ohne Diät — nur durch Achtsamkeit. Das entspricht bei 2000 kcal/Tag 300–600 kcal täglich!"}]""",
                "article", 1),

            // ── KURS 3: Gewohnheiten aufbauen ────────────────────────────────────────
            Lesson("l3_1", "course_3", "Die Gewohnheitsschleife", 0, 7,
                """[{"type":"text","content":"## Cue → Routine → Reward\n\nJede Gewohnheit folgt diesem Muster:\n\n1. **Cue (Auslöser)**: Ein Reiz, der die Gewohnheit startet\n2. **Routine**: Das automatische Verhalten\n3. **Reward (Belohnung)**: Das befriedigende Gefühl danach\n\nBeispiel: **Stress** (Cue) → **Snacken** (Routine) → **kurze Erleichterung** (Reward)\n\nUm die Gewohnheit zu ändern, musst du nur die **Routine** ersetzen. Cue und Reward bleiben!"},{"type":"tip","icon":"💡","content":"Du kämpfst nicht gegen eine Gewohnheit — du ersetzt sie. **Stress → Snacken** wird zu **Stress → kurzer Spaziergang** wenn der Walk auch kurze Erleichterung bringt."},{"type":"quiz","question":"Welchen Teil der Gewohnheitsschleife musst du ändern, um eine Gewohnheit umzuprogrammieren?","options":["Den Cue","Die Routine","Den Reward"],"correctIndex":1,"explanation":"Cue und Reward bleiben — du ersetzt nur die Routine durch eine gesündere Alternative, die denselben Reward bringt."}]""",
                "article", 0),
            Lesson("l3_2", "course_3", "Kleine Gewohnheiten, große Wirkung", 1, 6,
                """[{"type":"text","content":"## Atomic Habits — Das 1%-Prinzip\n\nWenn du dich jeden Tag nur 1% verbesserst, bist du nach einem Jahr **37× besser** als heute.\n\nDas Problem mit großen Vorsätzen:\n- 'Ich höre ab morgen auf zu snacken' → scheitert nach 3 Tagen\n- 'Ich esse von 10 Snacks einer weniger pro Woche' → nachhaltig!\n\n## Die 2-Minuten-Regel\n\nJede neue Gewohnheit soll am Anfang maximal 2 Minuten dauern:\n- 'Sport machen' → '2 Minuten Dehnübungen'\n- 'Gesund essen' → '1 grünes Lebensmittel pro Mahlzeit'\n\nErst wenn die 2-Minuten-Version zur Routine geworden ist, erweiterst du."},{"type":"reflection","prompt":"Was ist eine gesunde Gewohnheit, die du aufbauen möchtest? Wie kannst du sie auf 2 Minuten pro Tag reduzieren?","placeholder":"Meine 2-Minuten-Gewohnheit..."}]""",
                "article", 0),
            Lesson("l3_3", "course_3", "Umgebungsdesign", 2, 5,
                """[{"type":"text","content":"## Deine Umgebung entscheidet für dich\n\nMehr als 40% unserer täglichen Handlungen sind Gewohnheiten — und die werden stark von der Umgebung gesteuert.\n\n**Gesundes sichtbar machen:**\n- Obst auf dem Tisch statt im Kühlschrank\n- Wasser-Glas neben dem Arbeitsplatz\n- Fertig gewaschenes Gemüse im Kühlschrank in Griffweite\n\n**Ungesundes unsichtbar machen:**\n- Chips nicht kaufen oder hinten ins Regal\n- Schokolade nicht in der Sichtweite\n- Handy beim Essen in eine andere Ecke legen"},{"type":"tip","icon":"💡","content":"Veränderung durch Willenskraft ist anstrengend und kurzlebig. Veränderung durch Umgebungsdesign ist dauerhaft und erfordert keine Energie."},{"type":"activity","title":"Deine Küche umgestalten","instruction":"Mach 1 Änderung in deiner Küche: Stelle gesundes Essen sichtbarer auf und verstecke eine ungesunde Sache. Berichte nach 3 Tagen wie es läuft.","action":""}]""",
                "article", 0),
            Lesson("l3_4", "course_3", "Streaks und Belohnungen", 3, 5,
                """[{"type":"text","content":"## Warum Streaks funktionieren\n\nDer **Endowment-Effekt** sagt: Menschen arbeiten hart um zu verlieren, was sie haben. Wenn du einen 10-Tage-Streak hast, willst du ihn nicht brechen — dieser Druck ist dein Freund.\n\n**Wichtig:** Belohnungen müssen unmittelbar sein.\n- Schlechte Belohnung: 'In 3 Monaten bin ich schlanker'\n- Gute Belohnung: 'Ich mache heute einen Haken und fühle mich stolz'\n\nJeanFit-Coins sind genau für diesen Zweck: sofortige kleine Belohnung für heutigen Fortschritt."},{"type":"quiz","question":"Warum sind sofortige Belohnungen effektiver als langfristige?","options":["Das Gehirn kann langfristige Belohnungen nicht verarbeiten","Das Gehirn bewertet sofortige Belohnungen viel höher (Zeitrabatt)","Langfristige Belohnungen machen nicht glücklich"],"correctIndex":1,"explanation":"Das Gehirn 'diskontiert' zukünftige Belohnungen stark — eine Belohnung in 3 Monaten fühlt sich heute fast wertlos an. Sofortige Belohnungen wirken 10× stärker."}]""",
                "article", 0),
            Lesson("l3_5", "course_3", "Rückfälle als Lernchance", 4, 6,
                """[{"type":"text","content":"## Der 'Alles-oder-Nichts'-Fehler\n\nDer häufigste Fehler beim Abnehmen: Nach einem Rückfall alles aufgeben.\n\n'Ich habe heute die Torte gegessen, jetzt ist eh alles egal' → Nein!\n\n## Das Glas-Boden-Prinzip\n\nStell dir vor, du lässt aus Versehen ein Glas fallen und ein Stück springt ab.\nReagierst du mit: 'Dann schmeiß ich das ganze Glas auf den Boden!'? Nein!\n\nGenauso beim Essen: Ein Ausrutscher ist ein kleines abgesprungenes Stück — nicht das Ende.\n\n**Die wichtigste Fähigkeit:** Immer und immer wieder anfangen. Nicht perfekt sein, sondern weitermachen."},{"type":"tip","icon":"💡","content":"Nach einem Rückfall: Lass **niemals** zwei schlechte Tage hintereinander passieren. Einmal ist ein Ausrutscher. Zweimal beginnt eine neue Gewohnheit."},{"type":"reflection","prompt":"Wie reagierst du bisher auf Rückfälle? Was ist dein neuer Plan für das nächste Mal?","placeholder":"Mein Plan für Rückfälle..."}]""",
                "article", 1),

            // ── KURS 4: Bewegung und Stoffwechsel ────────────────────────────────────
            Lesson("l4_1", "course_4", "NEAT — die unterschätzte Superpower", 0, 6,
                """[{"type":"text","content":"## Was ist NEAT?\n\n**NEAT = Non-Exercise Activity Thermogenesis**\n\nKalorien, die du verbrennst, ohne 'Sport zu machen':\n- Gehen, Stehen, Treppensteigen\n- Hausarbeit, Gartenarbeit, Einkaufen\n- Zappeln, Aufstehen beim Telefonieren\n\n## NEAT macht den Unterschied\n\nAktive Menschen (gleiche Größe/Gewicht) verbrennen durch NEAT **200–700 kcal/Tag mehr** als inaktive.\n\nDas ist mehr als die meisten Trainingseinheiten!"},{"type":"tip","icon":"💡","content":"Stehe beim Telefonieren auf. Nimm immer die Treppe. Gehe beim Einkaufen eine Extra-Runde. Diese kleinen Dinge summieren sich zu Hunderten von Kalorien täglich."},{"type":"quiz","question":"Was ist NEAT?","options":["Eine Diät-Methode","Kalorienverbrennung durch Alltagsbewegung","Schlafqualität-Messung"],"correctIndex":1,"explanation":"NEAT umfasst alle Kalorien die du verbrennst wenn du nicht Sport machst, isst oder schläfst — Gehen, Stehen, Gestikulieren etc."}]""",
                "article", 0),
            Lesson("l4_2", "course_4", "Spazierengehen als Abnehm-Tool", 1, 5,
                """[{"type":"text","content":"## Die einfachste Übung der Welt\n\nSpazierengehen verbrennt etwa **4–5 kcal/Minute** — weniger als Joggen, aber:\n\n✅ Jeder kann es sofort tun\n✅ Kein Equipment nötig\n✅ Verletzungsrisiko fast null\n✅ Stressreduzierend (Cortisol sinkt)\n✅ Kann nach dem Essen direkt beginnen\n\n## Nach dem Essen besonders wertvoll\n\nEin 10-minütiger Spaziergang nach der Mahlzeit:\n- Senkt den Blutzuckeranstieg\n- Fördert die Verdauung\n- Hilft beim Signalisieren der Sättigung"},{"type":"activity","title":"Heute nach dem Essen gehen","instruction":"Mache nach deinem nächsten Essen — egal ob Frühstück, Mittag oder Abend — einen mindestens 10-minütigen Spaziergang.","action":""}]""",
                "article", 0),
            Lesson("l4_3", "course_4", "Krafttraining und Abnehmen", 2, 7,
                """[{"type":"text","content":"## Warum Muskeln beim Abnehmen helfen\n\nJedes kg Muskelmasse verbrennt täglich **13 kcal mehr** — auch im Schlaf.\n\nBei 5 kg Muskeln = **65 kcal/Tag extra** = fast 7 kg weniger pro Jahr.\n\n## Der Jojo-Effekt und Muskeln\n\nWer nur durch Kalorienreduktion abnimmt ohne Krafttraining, verliert **Muskeln und Fett**. Wer dann wieder normal isst, baut erst Fett wieder auf — der klassische Jojo-Effekt.\n\nKrafttraining beim Abnehmen schützt die Muskeln und erhöht den Grundumsatz."},{"type":"tip","icon":"💡","content":"Du musst nicht ins Fitnessstudio. Liegestütze, Kniebeugen und Planks zuhause 3× pro Woche reichen um Muskeln zu erhalten."},{"type":"quiz","question":"Wie viele extra Kalorien verbrennt 1 kg Muskelmasse täglich?","options":["3 kcal","13 kcal","50 kcal"],"correctIndex":1,"explanation":"1 kg Muskel verbrennt ca. 13 kcal/Tag im Ruhezustand. Das klingt wenig, summiert sich aber über Monate erheblich."}]""",
                "article", 0),
            Lesson("l4_4", "course_4", "Schlaf und Gewicht", 3, 6,
                """[{"type":"text","content":"## Schlafmangel macht dick\n\nNur 1 Woche mit 5 statt 8 Stunden Schlaf:\n\n- Ghrelin (Hunger-Hormon) steigt um **15%**\n- Leptin (Sättigungs-Hormon) sinkt um **15%**\n- Cortisol steigt → Heißhunger auf Zucker\n- Entscheidungsfähigkeit sinkt → schlechtere Essensauswahl\n\nSchlafmangel ist einer der am meisten unterschätzten Faktoren beim Abnehmen."},{"type":"tip","icon":"💡","content":"**Schlaf-Hygiene**: Gleiche Schlafzeit täglich, dunkel und kühl (18–20°C), kein Bildschirm 1 Stunde vor dem Schlafen. Dies kann das Einschlafen um 30–60 Minuten verkürzen."},{"type":"quiz","question":"Was passiert mit dem Hunger-Hormon Ghrelin bei Schlafmangel?","options":["Es sinkt um 15%","Es steigt um 15%","Es bleibt gleich"],"correctIndex":1,"explanation":"Schlafmangel erhöht Ghrelin (macht hungrig) und senkt Leptin (macht satt) — ein Doppeleffekt der zu mehr Kalorien führt."}]""",
                "article", 0),
            Lesson("l4_5", "course_4", "Stresshormone und Bauchfett", 4, 6,
                """[{"type":"text","content":"## Cortisol und Fettspeicherung\n\nCortisol ist das wichtigste Stresshormon. Bei chronischem Stress:\n\n- Heißhunger auf Zucker und Fett steigt\n- Fett wird bevorzugt am **Bauch** gespeichert\n- Schlaf wird schlechter\n- Motivation sinkt\n\n## Stressmanagement ist Ernährungsmanagement\n\nMethoden zur Cortisol-Reduktion:\n- Spazierengehen (15+ Min.)\n- Tiefes Atmen (4-7-8 Methode)\n- Meditation (auch 5 Min. wirken)\n- Soziale Verbindungen\n- Schlaf"},{"type":"tip","icon":"💡","content":"Die 4-7-8 Methode: 4 Sekunden einatmen, 7 Sekunden halten, 8 Sekunden ausatmen. 3-4× wiederholen. Senkt nachweislich Cortisol innerhalb von Minuten."},{"type":"reflection","prompt":"Was sind deine größten Stressquellen? Was könntest du heute tun um sie zu reduzieren?","placeholder":"Meine Stressquellen und Strategien..."}]""",
                "article", 1),

            // ── KURS 5: Nachhaltig abnehmen ──────────────────────────────────────────
            Lesson("l5_1", "course_5", "Warum Diäten scheitern", 0, 7,
                """[{"type":"text","content":"## Das Diät-Paradox\n\nÜber 95% aller Diäten scheitern langfristig. Warum?\n\n**1. Zu starkes Kaloriendefizit**\nUnter 1200 kcal/Tag → Körper schaltet in 'Hungersnot-Modus' → Stoffwechsel verlangsamt sich, Muskeln werden abgebaut.\n\n**2. Verbote funktionieren nicht**\nPsychologisches Reaktanz-Phänomen: Was verboten ist, will man mehr.\n\n**3. Diäten sind temporär**\n'Ich esse 6 Wochen Salat' → Was passiert danach? Man kehrt zur alten Gewohnheit zurück.\n\n**Die Alternative:** Keine Diät. Kleine, nachhaltige Veränderungen, die du für immer machen kannst."},{"type":"tip","icon":"💡","content":"Frage dich bei jeder Ernährungsänderung: 'Kann ich das in 5 Jahren noch tun?' Wenn nicht, ist es keine Lösung."},{"type":"quiz","question":"Was ist die Hauptursache für den Jojo-Effekt nach einer Diät?","options":["Zu wenig Sport","Rückkehr zu alten Gewohnheiten + veränderter Stoffwechsel","Psychologische Schwäche"],"correctIndex":1,"explanation":"Der Körper hat sich angepasst (niedrigerer Grundumsatz) und die Verhaltensursachen wurden nicht geändert — klassische Jojo-Kombination."}]""",
                "article", 0),
            Lesson("l5_2", "course_5", "Das richtige Kaloriendefizit", 1, 6,
                """[{"type":"text","content":"## Wie viel Defizit ist optimal?\n\n**500 kcal/Tag unter TDEE** ist der Goldstandard:\n- Entspricht ~0,5 kg Gewichtsverlust pro Woche\n- Erhält Muskelmasse\n- Ist langfristig durchhaltbar\n- Kein Hunger-Modus des Körpers\n\n**Zu viel Defizit (> 1000 kcal/Tag):**\n- Muskelabbau\n- Nährstoffmangel\n- Stoffwechsel-Anpassung\n- Psychologische Erschöpfung\n\n**JeanFit rechnet für dich:** Dein Kalorienziel ist bereits auf diesen Wert eingestellt."},{"type":"tip","icon":"💡","content":"An Tagen mit viel Sport: esse 100–200 kcal mehr. Du musst nicht jeden Tag gleich essen — das wöchentliche Gesamtdefizit zählt."}]""",
                "article", 0),
            Lesson("l5_3", "course_5", "Das Gewichtsplateau überwinden", 2, 7,
                """[{"type":"text","content":"## Das Plateau — normal, nicht schlimm\n\nNach 4–8 Wochen Abnehmen stagniert das Gewicht oft. Das passiert weil:\n\n1. Der Körper effizienter geworden ist (braucht weniger Kalorien)\n2. Der Grundumsatz gesunken ist (weniger Körpermasse zu versorgen)\n3. Kleine Mengen-Schleicher (unbewusst mehr essen)\n\n## Strategien gegen Plateaus\n\n- **Refeeding-Tag**: 1–2 Tage bei Erhaltungskalorien essen\n- **Kalorienziel anpassen**: Es muss nach 10+ kg Verlust neu berechnet werden\n- **NEAT erhöhen**: 10 Minuten mehr gehen täglich\n- **Menge prüfen**: Schleichen sich kleine Extras ein?"},{"type":"quiz","question":"Was ist ein 'Refeeding-Day'?","options":["Ein Cheat-Day ohne Grenzen","1-2 Tage bei Erhaltungskalorien um den Stoffwechsel zu resetten","Fasten für 24 Stunden"],"correctIndex":1,"explanation":"Refeeding bedeutet kurze Zeit bei Erhaltungskalorien essen — das verhindert Stoffwechsel-Anpassung und ist psychologisch regenerierend."}]""",
                "article", 0),
            Lesson("l5_4", "course_5", "Treat Days strategisch nutzen", 3, 5,
                """[{"type":"text","content":"## Treat Days — nicht Cheat Days\n\n**Cheat Day**: 'Heute ist alles erlaubt' → Kann leicht 3000–5000 extra kcal bedeuten, die eine ganze Woche zunichte machen.\n\n**Treat Day**: Geplantes, bewusstes Genießen innerhalb eines erhöhten Rahmens (+20% Kalorien).\n\nIn JeanFit kannst du mit 5 Coins einen Treat Day freischalten:\n- Kalorienziel +20%\n- Keine Warnungen\n- Das Icon ändert sich\n\n## Warum das funktioniert\n\nDer 'verbotene Früchte'-Effekt wird aufgehoben. Du weißt, dass du dich gelegentlich genießen kannst — und das macht die anderen Tage leichter."},{"type":"tip","icon":"💡","content":"Plane Treat Days im Voraus (Freitag-Abend, Geburtstag, Restaurant). Ungeplante Treats führen eher zu Überkonsum als geplante."}]""",
                "article", 0),
            Lesson("l5_5", "course_5", "Langfristige Ziele setzen", 4, 6,
                """[{"type":"text","content":"## SMART-Ziele für dein Gewicht\n\n**Schlecht:** 'Ich will abnehmen'\n**Gut:** 'Ich möchte bis zum 1. September 5 kg abnehmen, indem ich täglich meine Kalorien logge und 3× pro Woche spazieren gehe'\n\n**S** – Spezifisch: Was genau?\n**M** – Messbar: Wie viele kg?\n**A** – Angemessen: Realistisch?\n**R** – Relevant: Warum wichtig?\n**T** – Terminiert: Bis wann?\n\n## Prozess- vs. Ergebnisziele\n\nErgebnisziel: '5 kg abnehmen' (du hast wenig Kontrolle)\nProzessziel: 'Täglich loggen' (du hast volle Kontrolle)\n\nFokussiere dich auf Prozessziele — die Ergebnisse folgen automatisch."},{"type":"reflection","prompt":"Was ist dein SMART-Ziel? Schreibe es vollständig auf.","placeholder":"Bis [Datum] möchte ich [Gewicht] erreichen, indem ich täglich [Aktion] tue..."}]""",
                "article", 1),

            // ── KURS 6: Einkaufen & Meal-Prep ────────────────────────────────────────
            Lesson("l6_1", "course_6", "Der gesunde Einkaufszettel", 0, 5,
                """[{"type":"text","content":"## Die goldene Einkaufs-Regel\n\n**Kaufe nichts, was du nicht im Haus haben willst.**\n\nWas nicht da ist, kann nicht gegessen werden. Der Supermarkt ist die wichtigste Entscheidung — nicht das Abendessen.\n\n## Der Einkaufszettel-Plan\n\n🟢 **Immer dabei**: Gemüse (Tiefkühl ok!), Obst, Hülsenfrüchte, Eier\n🟡 **Regelmäßig**: Vollkornprodukte, Fisch, Hühnchen, Quark, Joghurt\n🟠 **Bewusst begrenzt**: Nüsse, Käse, Öle (hochwertig aber kalorienreich)\n🚫 **Selten**: Chips, Süßes, Fertigprodukte mit langer Zutatenliste"},{"type":"tip","icon":"💡","content":"Nie hungrig einkaufen! Studien zeigen, dass hungrige Einkäufer 30–40% mehr Kalorien einkaufen und bevorzugt ungesunde Snacks wählen."},{"type":"activity","title":"Den Vorrat prüfen","instruction":"Schau in deinen Kühlschrank und Schrank. Was ist Grün, was Orange? Notiere was du beim nächsten Einkauf anders kaufen möchtest.","action":""}]""",
                "article", 0),
            Lesson("l6_2", "course_6", "Meal-Prep in 2 Stunden", 1, 7,
                """[{"type":"text","content":"## Warum Meal-Prep funktioniert\n\nDie größte Falle beim gesunden Essen: **Hunger + kein gesundes Essen griffbereit = Junkfood**.\n\nMeal-Prep löst das Problem durch Vorbereitung.\n\n## Der 2-Stunden-Sonntag\n\n**Stunde 1 — Kochen:**\n- Getreide (Reis, Quinoa) für 4–5 Mahlzeiten kochen\n- Hülsenfrüchte kochen oder aus Dose\n- Protein vorbereiten (Hühnchen backen, Eier kochen)\n\n**Stunde 2 — Vorbereiten:**\n- Gemüse waschen, schneiden, portionieren\n- Salate vorbereiten (Dressing separat!)\n- In Behälter portionieren"},{"type":"tip","icon":"💡","content":"Gefrorenes Gemüse ist genauso nährstoffreich wie frisches — und bleibt viel länger haltbar. Kaufe Tiefkühl-Gemüse in großen Mengen."}]""",
                "article", 0),
            Lesson("l6_3", "course_6", "Gesunde Snack-Strategien", 2, 5,
                """[{"type":"text","content":"## Snacken — ja oder nein?\n\nEs gibt keine universelle Antwort. Beides ist okay:\n\n**2–3 Mahlzeiten ohne Snacks**: Hormonal günstiger (Insulinspiegel kann sinken), einfacher zu tracken.\n\n**3 Mahlzeiten + 1–2 kleine Snacks**: Verhindert Überessen bei den Hauptmahlzeiten.\n\n## Snacks die satt machen\n\n✅ Protein-reiche Snacks: Quark, Hüttenkäse, Eier, Edamame\n✅ Volumen-Snacks: Rohes Gemüse mit Hummus, Obstsalat\n✅ Kombis: Apfel + Mandelbutter (Volumen + Fett + Protein)\n\n❌ Vermeiden: Chips, Kekse, Riegel (hohe Kaloriendichte, sättigen kaum)"},{"type":"tip","icon":"💡","content":"Bereite Snacks vor, bevor du Hunger bekommst. Ein Schälchen Karotten im Kühlschrank, bereit zum Essen, wird tatsächlich gegessen."}]""",
                "article", 0),
            Lesson("l6_4", "course_6", "Auswärts Essen ohne Reue", 3, 6,
                """[{"type":"text","content":"## Restaurant-Strategien\n\n**Vor dem Restaurant:**\n- Menü vorher online ansehen und Wahl treffen\n- Nicht ausgehungert hingehen (kleiner Snack vorher)\n\n**Im Restaurant:**\n- Brot/Chips vom Tisch wegstellen lassen\n- Dressings und Soßen separat bestellen\n- Wasser statt Softdrinks\n- Große Portionen: Hälfte einpacken lassen\n\n**Fast Food (wenn es sein muss):**\n- Gegrilltes statt frittiertes\n- Salat als Beilage statt Pommes\n- Kein Supersizing"},{"type":"tip","icon":"💡","content":"Ein Restaurant-Abend einmal pro Woche, bei dem du einfach genießt ohne zu tracken, ist okay. 1 von 21 Mahlzeiten macht keinen Unterschied."}]""",
                "article", 0),
            Lesson("l6_5", "course_6", "Kochen lernen zahlt sich aus", 4, 7,
                """[{"type":"text","content":"## Der Wert des Selbstkochens\n\nWer selbst kocht, isst im Durchschnitt:\n- **200–500 kcal weniger** pro Mahlzeit\n- Mehr Gemüse und Ballaststoffe\n- Weniger verstecktes Fett, Zucker und Salz\n\n**Plus:** Du weißt genau, was drin ist.\n\n## Einfache Basis-Rezepte lohnen sich\n\nLerne 5–7 Grundrezepte sehr gut:\n1. Einfaches Rührei / Omelette\n2. Gebratenes Hähnchen mit Gemüse\n3. Linsensuppe\n4. Vollkornnudeln mit Gemüsesoße\n5. Salat mit Protein\n\nMit 5 Grundrezepten kannst du Monate lang abwechslungsreich essen."},{"type":"activity","title":"Neues Rezept ausprobieren","instruction":"Wähle heute eines der JeanFit-Rezepte das du noch nie gemacht hast und probiere es diese Woche.","action":"navigate_recipes"}]""",
                "article", 1),

            // ── KURS 7: Selbstfürsorge & Mindset ─────────────────────────────────────
            Lesson("l7_1", "course_7", "Körperbild und Selbstwert", 0, 7,
                """[{"type":"text","content":"## Der Unterschied macht es\n\n**Körper-Scham** als Motivations-Treiber: Funktioniert kurzfristig, ist langfristig destruktiv und führt zu Essstörungen, Unzufriedenheit und Aufgabe.\n\n**Selbst-Fürsorge** als Motivations-Treiber: 'Ich möchte gesünder sein, weil ich mich und meinen Körper wertschätze' — nachhaltig, positiv, langfristig.\n\n## Abnehmen aus Liebe, nicht aus Hass\n\nFrage dich: Woher kommt deine Motivation? Aus dem Wunsch, deinen Körper zu strafen? Oder aus dem Wunsch, gut für ihn zu sorgen?\n\nBeide können kurzfristig funktionieren — aber langfristig macht der zweite Weg glücklicher."},{"type":"reflection","prompt":"Was ist deine ehrliche Motivation abzunehmen? Klingt sie nach Fürsorge oder nach Strafe?","placeholder":"Meine ehrliche Antwort..."}]""",
                "article", 0),
            Lesson("l7_2", "course_7", "Innerer Kritiker vs. Mitgefühl", 1, 6,
                """[{"type":"text","content":"## Der innere Kritiker\n\n'Du Versagerin, schon wieder die Diät gebrochen'\n'Andere schaffen das doch auch'\n'Du hast einfach keine Willenskraft'\n\nKennt jemand diese Stimme? Diese Stimme ist eine Lüge.\n\n## Selbst-Mitgefühl (Self-Compassion)\n\nStudien zeigen: Menschen mit **mehr Selbst-Mitgefühl** halten Diäten und gesunde Routinen **besser durch** als Menschen die sich selbst kritisieren.\n\nWhy? Weil Self-Compassion Resilienz aufbaut. 'Ich bin heute gefallen, aber das passiert. Ich stehe morgen wieder auf.'"},{"type":"tip","icon":"💡","content":"Wenn du deiner besten Freundin nicht das sagen würdest, was du dir selbst sagst — dann sag es dir auch nicht."},{"type":"reflection","prompt":"Was würdest du einer guten Freundin sagen, die gerade einen Rückfall hatte? Sage das als nächstes dir selbst.","placeholder":"Was ich mir sagen würde..."}]""",
                "article", 0),
            Lesson("l7_3", "course_7", "Fortschritte richtig messen", 2, 5,
                """[{"type":"text","content":"## Gewicht ist nicht alles\n\nDas Körpergewicht schwankt täglich 1–2 kg durch:\n- Wassereinlagerungen\n- Darminhalt\n- Monatszyklus (Frauen)\n- Salz-Konsum\n\nDer **Trendwert** (7-Tage-Durchschnitt) ist viel aussagekräftiger als der Tageswert.\n\n## Weitere Erfolgsmesser\n\n✅ Kleidung sitzt besser\n✅ Mehr Energie im Alltag\n✅ Besser schlafen\n✅ Weniger Gelenk-Schmerzen\n✅ Bessere Laune\n✅ Blutdruck / Blutzucker verbessert\n\nDiese Veränderungen passieren oft Wochen bevor die Waage sich bewegt!"},{"type":"tip","icon":"💡","content":"Wiege dich immer zur gleichen Zeit (morgens nach dem Toilettengang, nüchtern) und schaue auf den Wochentrend, nicht den Tageswert."}]""",
                "article", 0),
            Lesson("l7_4", "course_7", "Erfolge feiern", 3, 4,
                """[{"type":"text","content":"## Warum Erfolge feiern wichtig ist\n\nDas Gehirn braucht positive Bestärkung um Verhaltensweisen zu wiederholen. Ohne Feiern lernt es nicht, was gut war.\n\n**Kleine Erfolge feiern:**\n- Erster Tag vollständig geloggt → Teile es mit jemandem\n- 1 Woche Streak → Gönne dir ein nicht-Essen-Belohnung\n- 2 kg weniger → Anerkenne wie viel Arbeit das war\n\n**Wichtig: Belohnungen nicht mit Essen**\nEsse-Belohnungen verstärken die Verknüpfung 'Erfolg = Essen'. Besser:\n- Bad/Beauty-Produkt\n- Film schauen\n- Schöner Spaziergang\n- Neues Kleidungsstück"},{"type":"activity","title":"Deine Belohnungsliste","instruction":"Schreibe 5 Belohnungen auf (keine Essensbezogen), die du dir bei bestimmten Meilensteinen gönnen möchtest.","action":""}]""",
                "article", 0),
            Lesson("l7_5", "course_7", "Das neue Ich — wer bist du jetzt?", 4, 8,
                """[{"type":"text","content":"## Identitäts-basierte Veränderung\n\nDer stärkste Motivator für Verhaltensänderung ist **Identität**.\n\nNicht: 'Ich versuche weniger zu snacken'\nSondern: **'Ich bin jemand, der achtsam isst'**\n\nJede Handlung ist entweder ein Beweis für oder gegen deine Identität. Wenn du dich als 'gesunde Person' siehst, wirst du automatisch gesündere Entscheidungen treffen.\n\n## Dein Weg hierher\n\nDu hast diesen Kurs abgeschlossen. Das bedeutet:\n- Du nimmst deine Gesundheit ernst\n- Du bist bereit zu lernen und zu wachsen\n- Du kannst auch an schlechten Tagen weitermachen\n\nDas ist die Identität einer Person, die ihr Ziel erreicht."},{"type":"tip","icon":"💡","content":"Sage nicht 'Ich versuche...' — sage 'Ich bin...'. Diese kleine Sprachveränderung verändert dein Selbstbild und dein Verhalten."},{"type":"reflection","prompt":"Wer bist du nach diesen 7 Kursen? Beschreibe deine neue Identität als gesunde Person.","placeholder":"Ich bin jemand, der/die..."}]""",
                "article", 1)
        )
        lessonRepository.seedInitialContent(courses, lessons)
    }
}
