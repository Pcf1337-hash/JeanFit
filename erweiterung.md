# JeanFit — Erweiterungsplan
> **Ziel:** 100% fertige, wiederverwendbare, spaßige App mit blauem Design, Rezeptdatenbank, KI-Coach und Fitness-Tracking.

---

## 🎨 Neues Design-System: Ocean Blue

Deine Freundin liebt Blau → komplettes Rebranding auf ein tiefes, modernes Ozean-Blau.

```kotlin
object JeanFitColors {
    // === PRIMÄR (Blau-Familie) ===
    val OceanBlue      = Color(0xFF1565C0)  // Primary CTAs, Buttons
    val SkyBlue        = Color(0xFF42A5F5)  // Akzente, Chips, Links
    val DeepNavy       = Color(0xFF0D2B4E)  // Headlines, dunkler Text
    val IceBlue        = Color(0xFFE3F2FD)  // Background (Light Mode)
    val MidnightBlue   = Color(0xFF0A1929)  // Background (Dark Mode)

    // === SEKUNDÄR ===
    val TealAccent     = Color(0xFF00BCD4)  // Sekundärer Akzent, Charts
    val PearlWhite     = Color(0xFFF8FAFF)  // Card-Hintergrund Light
    val DarkSurface    = Color(0xFF122136)  // Card-Hintergrund Dark

    // === AMPELSYSTEM (bleibt farblich neutral) ===
    val FoodGreen      = Color(0xFF2ECC71)  // Grüne Lebensmittel
    val FoodYellow     = Color(0xFFF39C12)  // Gelbe Lebensmittel
    val FoodOrange     = Color(0xFFE74C3C)  // Orange Lebensmittel

    // === GAMIFICATION ===
    val CoinGold       = Color(0xFFFFD700)  // NoomCoins
    val StreakFire     = Color(0xFFFF6B35)  // Streak-Anzeige
}
```

### Gradient-Styles (für Hero-Bereiche)
```kotlin
// Header-Gradient (Home, Onboarding)
Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF0D2B4E)))

// Card-Shimmer (Loading)
Brush.horizontalGradient(listOf(Color(0xFF1E3A5F), Color(0xFF2A5298), Color(0xFF1E3A5F)))

// Progress-Ring
Brush.sweepGradient(listOf(Color(0xFF42A5F5), Color(0xFF00BCD4), Color(0xFF1565C0)))
```

### Typografie
```kotlin
// Google Fonts empfohlen:
// Headlines: "Nunito" (rund, freundlich, weiblich)
// Body:      "Inter"  (lesbar, modern)
// Zahlen:    "Roboto Mono" (für Kalorien-Counter, Gewicht)
```

---

## 📋 Erweiterungsübersicht

| # | Feature | Priorität | Aufwand | Status |
|---|---------|-----------|---------|--------|
| 1 | Blaues Design-System | 🔴 Hoch | 1 Tag | [ ] |
| 2 | Rezeptdatenbank (100+) | 🔴 Hoch | 3 Tage | [ ] |
| 3 | KI-Coach / Chat | 🔴 Hoch | 2 Tage | [ ] |
| 4 | Schrittzähler & Health Connect | 🔴 Hoch | 2 Tage | [ ] |
| 5 | Wassertracking | 🟡 Mittel | 0.5 Tage | [ ] |
| 6 | Körpermaße-Tracker | 🟡 Mittel | 1 Tag | [ ] |
| 7 | Fortschrittsfotos | 🟡 Mittel | 1 Tag | [ ] |
| 8 | Barcode-Verbesserungen | 🟡 Mittel | 0.5 Tage | [ ] |
| 9 | Widgets (Homescreen) | 🟢 Nice | 1.5 Tage | [ ] |
| 10 | Onboarding-Ausbau | 🟢 Nice | 1 Tag | [ ] |

---

## 1. 🍽️ Rezeptdatenbank — TheMealDB API

> **Änderung:** Statt lokaler JSON-Datei wird [TheMealDB](https://www.themealdb.com/api.php) genutzt.
> Kostenlos, kein API-Key nötig, ~300 Mahlzeiten, Bilder inklusive.
> Rezepte werden beim ersten Abruf in Room gecacht → danach 100% offline.

### TheMealDB API-Endpunkte
```
# Alle Kategorien abrufen
GET https://www.themealdb.com/api/json/v1/1/categories.php

# Mahlzeiten nach Kategorie
GET https://www.themealdb.com/api/json/v1/1/filter.php?c=Chicken

# Vollständiges Rezept per ID
GET https://www.themealdb.com/api/json/v1/1/lookup.php?i=52772

# Zufälliges Rezept (für "Überrasch mich"-Feature)
GET https://www.themealdb.com/api/json/v1/1/random.php

# Suche nach Name
GET https://www.themealdb.com/api/json/v1/1/search.php?s=Chicken
```

### Retrofit Interface
```kotlin
// data/api/TheMealDbApi.kt
interface TheMealDbApi {
    @GET("api/json/v1/1/categories.php")
    suspend fun getCategories(): Response<MealCategoriesResponse>

    @GET("api/json/v1/1/filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): Response<MealListResponse>

    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealById(@Query("i") id: String): Response<MealDetailResponse>

    @GET("api/json/v1/1/search.php")
    suspend fun searchMeals(@Query("s") query: String): Response<MealListResponse>

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): Response<MealDetailResponse>
}
// BaseUrl: "https://www.themealdb.com/"
```

### Response-Models (Moshi)
```kotlin
data class MealCategoriesResponse(
    @Json(name = "categories") val categories: List<MealCategory>
)
data class MealCategory(
    @Json(name = "idCategory") val id: String,
    @Json(name = "strCategory") val name: String,
    @Json(name = "strCategoryThumb") val imageUrl: String,
    @Json(name = "strCategoryDescription") val description: String
)

data class MealListResponse(
    @Json(name = "meals") val meals: List<MealSummary>?
)
data class MealSummary(
    @Json(name = "idMeal") val id: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strMealThumb") val imageUrl: String
)

// Vollständiges Rezept — TheMealDB nutzt strIngredient1..20 + strMeasure1..20
data class MealDetail(
    @Json(name = "idMeal") val id: String,
    @Json(name = "strMeal") val name: String,
    @Json(name = "strCategory") val category: String,
    @Json(name = "strArea") val cuisine: String,
    @Json(name = "strInstructions") val instructions: String,
    @Json(name = "strMealThumb") val imageUrl: String,
    @Json(name = "strYoutube") val youtubeUrl: String?,
    @Json(name = "strIngredient1") val ingredient1: String?,
    // ... bis strIngredient20
    @Json(name = "strMeasure1") val measure1: String?,
    // ... bis strMeasure20
) {
    // Helper: extrahiert Zutaten-Liste aus den 20 Feldern
    fun getIngredients(): List<Pair<String, String>> =
        (1..20).mapNotNull { i ->
            val ing = this::class.java.getDeclaredField("ingredient$i")
                .apply { isAccessible = true }.get(this) as? String
            val mea = this::class.java.getDeclaredField("measure$i")
                .apply { isAccessible = true }.get(this) as? String
            if (!ing.isNullOrBlank()) Pair(ing.trim(), mea?.trim() ?: "") else null
        }
}
```

### Room-Entity (erweitert für TheMealDB)
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val recipeId: String,       // TheMealDB idMeal
    val title: String,
    val category: String,                   // z.B. "Chicken", "Vegetarian"
    val cuisine: String,                    // z.B. "Italian", "German"
    val imageUrl: String,                   // TheMealDB CDN URL
    val instructions: String,              // Schritt-für-Schritt Text
    val ingredientsJson: String,           // JSON: [{name, measure}]
    val youtubeUrl: String?,               // optionales Video
    val tags: String,                      // CSV aus Kategorie + area
    // Lokal berechnet / manuell ergänzt:
    val estimatedCalories: Int?,           // aus Zutaten grob schätzen
    val colorCategory: String,             // "green","yellow","orange"
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,         // true = vom Nutzer erstellt
    val cachedAt: Long = System.currentTimeMillis()
)
```

### RecipeRepository — NetworkBoundResource Pattern
```kotlin
@Singleton
class RecipeRepository @Inject constructor(
    private val api: TheMealDbApi,
    private val dao: RecipeDao,
    private val colorCalc: CalcColorCategoryUseCase
) {
    // Kategorien laden (gecacht)
    fun getCategories(): Flow<List<MealCategory>> = flow {
        val cached = dao.getAllCategories()
        if (cached.isNotEmpty()) emit(cached)
        else {
            val response = api.getCategories()
            if (response.isSuccessful) {
                val cats = response.body()?.categories ?: emptyList()
                dao.insertCategories(cats)
                emit(cats)
            }
        }
    }

    // Rezept nach ID (erst Cache, dann API)
    fun getRecipeById(id: String): Flow<Recipe?> = flow {
        val cached = dao.getRecipeById(id)
        if (cached != null) { emit(cached); return@flow }
        val response = api.getMealById(id)
        if (response.isSuccessful) {
            val meal = response.body()?.meals?.firstOrNull()
            meal?.let {
                val recipe = it.toRecipeEntity(colorCalc)
                dao.insertRecipe(recipe)
                emit(recipe)
            }
        }
    }

    // Suche mit Offline-Fallback
    fun searchRecipes(query: String): Flow<List<Recipe>> = flow {
        // 1. Lokale Room-Suche sofort
        val local = dao.searchRecipes("%$query%")
        if (local.isNotEmpty()) emit(local)
        // 2. Remote-Suche und Cache updaten
        val response = api.searchMeals(query)
        if (response.isSuccessful) {
            val meals = response.body()?.meals ?: emptyList()
            // Für jedes Suchergebnis Volldetails laden und cachen
            meals.take(10).forEach { summary ->
                if (dao.getRecipeById(summary.id) == null) {
                    val detail = api.getMealById(summary.id)
                    detail.body()?.meals?.firstOrNull()?.let { meal ->
                        dao.insertRecipe(meal.toRecipeEntity(colorCalc))
                    }
                }
            }
            emit(dao.searchRecipes("%$query%"))
        }
    }

    // Zufälliges Rezept
    suspend fun getRandomRecipe(): Recipe? {
        val response = api.getRandomMeal()
        return response.body()?.meals?.firstOrNull()?.toRecipeEntity(colorCalc)
    }
}
```

### Kalorienabschätzung (lokal, ohne externe Nährwert-API)
```kotlin
// Grobe Abschätzung aus Zutaten-Liste per Kategorie
fun estimateCalories(ingredients: List<Pair<String, String>>, category: String): Int {
    val baseCals = mapOf(
        "Chicken" to 400, "Beef" to 520, "Seafood" to 350,
        "Vegetarian" to 300, "Vegan" to 280, "Dessert" to 450,
        "Pasta" to 420, "Breakfast" to 320, "Side" to 200
    )
    return baseCals[category] ?: 380
}
```

### UI: Rezept-Screens
```kotlin
// RecipeListScreen: LazyVerticalGrid (2 Spalten)
// - Coil AsyncImage mit TheMealDB Thumbnails
// - Kategorie-Filter-Chips (Chicken, Vegetarian, Seafood...)
// - "🎲 Überrasch mich"-Button → getRandomRecipe()
// - Suchfeld mit Debounce 400ms

// RecipeDetailScreen:
// - Großes Header-Bild (Coil, crossfade)
// - Kategorie + Herkunftsland Badge
// - Zutaten-Liste (aus ingredientsJson geparst)
// - Schritt-für-Schritt Anleitung (instructions aufgeteilt an "\n")
// - YouTube-Button wenn youtubeUrl vorhanden
// - "Ins Mahlzeiten-Log" FAB
```

### Verfügbare TheMealDB Kategorien (direkt nutzbar)
```
Beef · Chicken · Lamb · Pasta · Seafood · Pork · Vegetarian
Vegan · Breakfast · Dessert · Side · Starter · Goat · Miscellaneous
```
> TheMealDB liefert ~300 vollständige Rezepte mit Bild, Zutaten und Anleitung.
> Kein lokales JSON nötig — alles wird on-demand geladen und in Room gecacht.

### Erste Kategorien beim App-Start vorladen (PreloadWorker)
```kotlin
class RecipePreloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recipeRepo: RecipeRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Beim ersten Start: 3 Starter-Kategorien cachen
        listOf("Chicken", "Vegetarian", "Seafood").forEach { cat ->
            recipeRepo.preloadCategory(cat)
        }
        return Result.success()
    }
}
```

> **Hinweis**: Die alten 100 lokalen JSON-Rezepte entfallen komplett.
> TheMealDB liefert echte Rezepte mit professionellen Fotos.
> Offline-Support: einmal geladen → immer verfügbar via Room-Cache.

---

## 2. 🤖 KI-Coach / Chat-System — Claude API
### Claude API Integration (CoachRepository.kt)

> **API-Key**: In `local.properties` speichern, nie im Code hardcoden!
> ```
> CLAUDE_API_KEY=sk-ant-api03-...  # in local.properties
> ```
> In `BuildConfig` einbinden via `build.gradle.kts`:
> ```kotlin
> buildConfigField("String", "CLAUDE_API_KEY", "\"${properties["CLAUDE_API_KEY"]}\"")
> ```

```kotlin
// data/api/ClaudeApi.kt
interface ClaudeApi {
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String = BuildConfig.CLAUDE_API_KEY,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body request: ClaudeRequest
    ): Response<ClaudeResponse>
}
// BaseUrl: "https://api.anthropic.com/"

data class ClaudeRequest(
    @Json(name = "model") val model: String = "claude-haiku-4-5-20251001",
    @Json(name = "max_tokens") val maxTokens: Int = 300,
    @Json(name = "system") val system: String,
    @Json(name = "messages") val messages: List<ClaudeMessage>
)
data class ClaudeMessage(
    @Json(name = "role") val role: String,     // "user" oder "assistant"
    @Json(name = "content") val content: String
)
data class ClaudeResponse(
    @Json(name = "content") val content: List<ClaudeContentBlock>
)
data class ClaudeContentBlock(
    @Json(name = "type") val type: String,
    @Json(name = "text") val text: String
)
```

### System-Prompt (Coach-Persönlichkeit)
```kotlin
// domain/coach/CoachSystemPrompt.kt
fun buildSystemPrompt(profile: UserProfile, stats: DailyStats): String = """
    Du bist Jean, ein freundlicher, motivierender Ernährungs- und Abnehm-Coach
    in der JeanFit-App. Du antwortest auf Deutsch, kurz (max. 3 Sätze) und
    immer positiv-aufbauend. Nutze gelegentlich passende Emojis.
    
    Nutzer-Kontext:
    - Name: ${profile.name}
    - Ziel: ${profile.startWeightKg}kg → ${profile.goalWeightKg}kg
    - Bereits abgenommen: ${"%.1f".format(stats.weightLostKg)} kg
    - Aktueller Streak: ${stats.streakDays} Tage
    - Heutige Kalorien: ${stats.caloriesToday} / ${profile.dailyCalorieGoal} kcal
    - Programm-Tag: ${stats.programDay}
    
    Regeln:
    - Keine medizinischen Diagnosen oder Therapieempfehlungen
    - Bei Essstörungssignalen sanft professionelle Hilfe empfehlen
    - Immer konkrete, umsetzbare Tipps geben
    - Rezeptvorschläge aus TheMealDB nennen wenn sinnvoll
""".trimIndent()
```

### CoachRepository
```kotlin
@Singleton
class CoachRepository @Inject constructor(
    private val api: ClaudeApi,
    private val dao: CoachMessageDao,
    private val userRepo: UserRepository
) {
    // Konversations-History (letzte 10 Nachrichten als Kontext)
    private val conversationHistory = mutableListOf<ClaudeMessage>()

    suspend fun sendMessage(userInput: String): Flow<String> = flow {
        // 1. User-Nachricht lokal speichern
        dao.insert(CoachMessage(content = userInput, isFromCoach = false))

        // 2. Kontext aufbauen
        val profile = userRepo.getProfile()
        val stats = userRepo.getTodayStats()
        conversationHistory.add(ClaudeMessage("user", userInput))

        // 3. Claude API aufrufen
        val response = api.sendMessage(
            request = ClaudeRequest(
                system = buildSystemPrompt(profile, stats),
                messages = conversationHistory.takeLast(10)  // max. 10 Nachrichten History
            )
        )

        val reply = response.body()?.content?.firstOrNull()?.text
            ?: "Ich bin gerade nicht erreichbar. Bitte versuche es später nochmal 😊"

        // 4. Antwort in History + Room speichern
        conversationHistory.add(ClaudeMessage("assistant", reply))
        dao.insert(CoachMessage(content = reply, isFromCoach = true))
        emit(reply)
    }.catch { e ->
        // Offline-Fallback
        val fallback = offlineFallbacks.random()
        dao.insert(CoachMessage(content = fallback, isFromCoach = true))
        emit(fallback)
    }

    // Offline-Fallbacks wenn API nicht erreichbar
    private val offlineFallbacks = listOf(
        "Du machst das super! Auch kleine Schritte führen ans Ziel 💪",
        "Denk dran: Fortschritt, nicht Perfektion ist das Ziel 🌟",
        "Trink ein Glas Wasser – oft hilft das schon weiter! 💧"
    )

    fun getChatHistory(): Flow<List<CoachMessage>> = dao.getAllMessages()
}
```

### Room Entity
```kotlin
@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isFromCoach: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
```

### Chat-Screen UI
```kotlin
@Composable
fun CoachChatScreen(viewModel: CoachViewModel = hiltViewModel()) {
    // Ocean Blue Gradient Header mit "Jean" Avatar-Icon
    // LazyColumn (reverseLayout=true) mit Nachrichten-Bubbles
    //   Nutzer: rechts, OceanBlue (#1565C0) Bubble, weiße Schrift
    //   Jean:   links, DarkSurface Bubble, SkyBlue Avatar-Kreis
    // Loading-Indicator: 3 animierte Punkte (TypingIndicator Composable)
    // Quick-Reply-Chips (horizontal scrollbar, unter Eingabe)
    // TextField + Send-Button (OceanBlue Icon)
}
```

### Quick-Reply Chips (8 voreingestellt)
```
💧 "Ich habe Hunger"
😔 "Ich bin demotiviert"
📊 "Wie läuft es?"
🍽️ "Rezeptvorschlag"
⚖️ "Ich stecke auf einem Plateau"
🏃 "Sport-Tipp"
😰 "Ich hatte Stress"
🎉 "Ich habe mein Ziel erreicht!"
```

### Abhängigkeit in libs.versions.toml (kein neues Package nötig)
```toml
# Claude API läuft über vorhandenes Retrofit + Moshi
# Nur BaseUrl-Konstante im NetworkModule ergänzen:
# "https://api.anthropic.com/"
```

---

---

## 3. 👟 Schrittzähler & Fitness-Tracking (NEU - ERSETZT DOPPELTEN BLOCK)
<!-- altes JSON + alter Coach-Block komplett entfernt -->
    "emoji": "🍓",
    "prepTime": 5, "cookTime": 0, "servings": 1,
    "calories": 280, "protein": 20, "carbs": 32, "fat": 6, "fiber": 3,
    "color": "green",
    "difficulty": "leicht",
    "tags": "high-protein,schnell,vegetarisch,frühstück",
    "ingredients": [
      {"name": "Griechischer Joghurt (0%)", "amount": 200, "unit": "g"},
      {"name": "Erdbeeren", "amount": 100, "unit": "g"},
      {"name": "Granola", "amount": 30, "unit": "g"},
      {"name": "Mandeln (gehackt)", "amount": 15, "unit": "g"},
      {"name": "Honig", "amount": 10, "unit": "g"}
    ],
    "steps": [
      "Joghurt in eine Schüssel geben.",
      "Erdbeeren waschen, halbieren und darauflegen.",
      "Granola und Mandeln darüberstreuen.",
      "Mit Honig beträufeln und sofort servieren."
    ]
  },
  {
    "recipeId": "r_003",
    "title": "Protein-Pancakes (Banane & Ei)",
    "emoji": "🥞",
    "prepTime": 5, "cookTime": 10, "servings": 2,
    "calories": 220, "protein": 18, "carbs": 28, "fat": 5, "fiber": 2,
    "color": "yellow",
    "difficulty": "leicht",
    "tags": "high-protein,vegetarisch,frühstück",
    "ingredients": [
      {"name": "Reife Banane", "amount": 1, "unit": "Stück"},
      {"name": "Eier", "amount": 2, "unit": "Stück"},
      {"name": "Haferflocken", "amount": 40, "unit": "g"},
      {"name": "Zimt", "amount": 1, "unit": "TL"},
      {"name": "Backpulver", "amount": 0.5, "unit": "TL"}
    ],
    "steps": [
      "Banane zerdrücken, mit Eiern verquirlen.",
      "Haferflocken, Zimt und Backpulver untermischen.",
      "Pfanne mit etwas Kokosöl bei mittlerer Hitze erhitzen.",
      "Kleine Pancakes (5-7cm) ausbacken, je 2 Min. pro Seite.",
      "Mit Beeren oder Joghurt servieren."
    ]
  },
  {
    "recipeId": "r_004",
    "title": "Avocado-Toast mit Ei",
    "emoji": "🥑",
    "prepTime": 5, "cookTime": 8, "servings": 1,
    "calories": 380, "protein": 16, "carbs": 30, "fat": 22, "fiber": 8,
    "color": "yellow",
    "difficulty": "leicht",
    "tags": "vegetarisch,frühstück,sättigend",
    "ingredients": [
      {"name": "Vollkornbrot", "amount": 2, "unit": "Scheiben"},
      {"name": "Avocado (reif)", "amount": 0.5, "unit": "Stück"},
      {"name": "Ei", "amount": 1, "unit": "Stück"},
      {"name": "Zitronensaft", "amount": 5, "unit": "ml"},
      {"name": "Chiliflocken", "amount": 1, "unit": "Prise"},
      {"name": "Salz, Pfeffer", "amount": 1, "unit": "Prise"}
    ],
    "steps": [
      "Brot toasten.",
      "Avocado zerdrücken, mit Zitronensaft, Salz und Pfeffer würzen.",
      "Ei nach Wahl zubereiten (pochiert empfohlen: 4 Min. im siedenden Wasser).",
      "Avocado auf Toast streichen, Ei darauflegen.",
      "Mit Chiliflocken und optionalem Sesam toppen."
    ]
  },
  {
    "recipeId": "r_005",
    "title": "Grüner Detox-Smoothie",
    "emoji": "💚",
    "prepTime": 5, "cookTime": 0, "servings": 1,
    "calories": 180, "protein": 5, "carbs": 38, "fat": 3, "fiber": 6,
    "color": "green",
    "difficulty": "leicht",
    "tags": "vegan,schnell,detox,frühstück",
    "ingredients": [
      {"name": "Babyspinat", "amount": 60, "unit": "g"},
      {"name": "Banane (gefroren)", "amount": 1, "unit": "Stück"},
      {"name": "Apfel", "amount": 0.5, "unit": "Stück"},
      {"name": "Ingwer", "amount": 5, "unit": "g"},
      {"name": "Kokoswasser", "amount": 200, "unit": "ml"},
      {"name": "Zitronensaft", "amount": 15, "unit": "ml"}
    ],
    "steps": [
      "Alle Zutaten in den Mixer geben.",
      "60 Sekunden auf höchster Stufe mixen.",
      "Konsistenz prüfen, ggf. mehr Flüssigkeit hinzufügen.",
      "Sofort trinken."
    ]
  },
  {
    "recipeId": "r_006", "title": "Rührei mit Gemüse", "emoji": "🍳",
    "prepTime": 5, "cookTime": 8, "servings": 1,
    "calories": 260, "protein": 20, "carbs": 8, "fat": 16, "fiber": 3,
    "color": "green", "difficulty": "leicht",
    "tags": "high-protein,schnell,vegetarisch,low-carb,frühstück"
  },
  {
    "recipeId": "r_007", "title": "Chia Pudding mit Mango", "emoji": "🥭",
    "prepTime": 5, "cookTime": 0, "servings": 1,
    "calories": 290, "protein": 8, "carbs": 40, "fat": 12, "fiber": 12,
    "color": "green", "difficulty": "leicht",
    "tags": "vegan,meal-prep,frühstück"
  },
  {
    "recipeId": "r_008", "title": "Müsli Bowl mit Nüssen", "emoji": "🌰",
    "prepTime": 3, "cookTime": 0, "servings": 1,
    "calories": 420, "protein": 14, "carbs": 52, "fat": 18, "fiber": 9,
    "color": "yellow", "difficulty": "leicht",
    "tags": "vegetarisch,schnell,frühstück"
  },
  {
    "recipeId": "r_009", "title": "Vollkorn-Waffeln mit Himbeeren", "emoji": "🧇",
    "prepTime": 10, "cookTime": 15, "servings": 2,
    "calories": 310, "protein": 12, "carbs": 48, "fat": 8, "fiber": 5,
    "color": "yellow", "difficulty": "mittel",
    "tags": "vegetarisch,wochenende,frühstück"
  },
  {
    "recipeId": "r_010", "title": "Quark mit Leinsamen & Beeren", "emoji": "🫐",
    "prepTime": 3, "cookTime": 0, "servings": 1,
    "calories": 220, "protein": 22, "carbs": 18, "fat": 4, "fiber": 4,
    "color": "green", "difficulty": "leicht",
    "tags": "high-protein,schnell,vegetarisch,frühstück"
  },
  {"recipeId":"r_011","title":"Smoothie Bowl Açaí","emoji":"🫐","calories":320,"protein":8,"carbs":52,"fat":10,"color":"green","tags":"vegan,frühstück"},
  {"recipeId":"r_012","title":"Haferbrei mit Apfel-Zimt","emoji":"🍎","calories":310,"protein":9,"carbs":55,"fat":6,"color":"green","tags":"vegan,schnell,frühstück"},
  {"recipeId":"r_013","title":"Frittata mit Spinat","emoji":"🥬","calories":240,"protein":18,"carbs":6,"fat":15,"color":"green","tags":"low-carb,vegetarisch,frühstück"},
  {"recipeId":"r_014","title":"Bananenbrot (gesund)","emoji":"🍌","calories":280,"protein":6,"carbs":48,"fat":8,"color":"yellow","tags":"vegan,backen,frühstück"},
  {"recipeId":"r_015","title":"Ei-Muffins mit Paprika","emoji":"🫑","calories":180,"protein":14,"carbs":4,"fat":11,"color":"green","tags":"meal-prep,low-carb,frühstück"},
  {"recipeId":"r_016","title":"Toast mit Hüttenkäse & Tomaten","emoji":"🍅","calories":240,"protein":18,"carbs":24,"fat":6,"color":"yellow","tags":"high-protein,schnell,frühstück"},
  {"recipeId":"r_017","title":"Kokos-Haferflocken","emoji":"🥥","calories":350,"protein":8,"carbs":50,"fat":12,"color":"yellow","tags":"vegan,frühstück"},
  {"recipeId":"r_018","title":"Matcha Latte Smoothie","emoji":"🍵","calories":160,"protein":6,"carbs":28,"fat":3,"color":"green","tags":"vegan,schnell,frühstück"},
  {"recipeId":"r_019","title":"Rote-Bete Smoothie","emoji":"🫀","calories":170,"protein":4,"carbs":36,"fat":2,"color":"green","tags":"vegan,detox,frühstück"},
  {"recipeId":"r_020","title":"Kichererbsen-Omelette","emoji":"🥙","calories":290,"protein":16,"carbs":32,"fat":9,"color":"yellow","tags":"vegan,high-protein,frühstück"}
]
```

#### 🥗 MITTAGESSEN (25 Rezepte)

```json
[
  {
    "recipeId": "r_021",
    "title": "Buddha Bowl mit Hähnchen",
    "emoji": "🍗",
    "prepTime": 10, "cookTime": 20, "servings": 1,
    "calories": 480, "protein": 38, "carbs": 45, "fat": 14, "fiber": 9,
    "color": "yellow",
    "difficulty": "mittel",
    "tags": "high-protein,meal-prep,mittag",
    "ingredients": [
      {"name": "Hähnchenbrust", "amount": 150, "unit": "g"},
      {"name": "Quinoa (gekocht)", "amount": 100, "unit": "g"},
      {"name": "Babyspinat", "amount": 60, "unit": "g"},
      {"name": "Süßkartoffel", "amount": 100, "unit": "g"},
      {"name": "Kichererbsen (Dose)", "amount": 80, "unit": "g"},
      {"name": "Tahini-Dressing", "amount": 30, "unit": "ml"},
      {"name": "Paprika (rot)", "amount": 0.5, "unit": "Stück"}
    ],
    "steps": [
      "Süßkartoffel würfeln, mit Olivenöl bei 200°C 20 Min. rösten.",
      "Hähnchen in Streifen schneiden, in Pfanne 6-8 Min. braten.",
      "Quinoa nach Packungsanleitung kochen.",
      "Alle Zutaten in einer Bowl anrichten.",
      "Tahini-Dressing darübergießen."
    ]
  },
  {
    "recipeId": "r_022",
    "title": "Linsensuppe mit Kurkuma",
    "emoji": "🍲",
    "prepTime": 10, "cookTime": 25, "servings": 2,
    "calories": 320, "protein": 16, "carbs": 52, "fat": 5, "fiber": 14,
    "color": "green",
    "difficulty": "leicht",
    "tags": "vegan,meal-prep,suppe,mittag,high-fiber",
    "ingredients": [
      {"name": "Rote Linsen", "amount": 150, "unit": "g"},
      {"name": "Karotten", "amount": 2, "unit": "Stück"},
      {"name": "Zwiebel", "amount": 1, "unit": "Stück"},
      {"name": "Gemüsebrühe", "amount": 800, "unit": "ml"},
      {"name": "Kurkuma", "amount": 1, "unit": "TL"},
      {"name": "Kreuzkümmel", "amount": 1, "unit": "TL"},
      {"name": "Zitronensaft", "amount": 20, "unit": "ml"}
    ],
    "steps": [
      "Zwiebel und Karotten würfeln, in Topf mit Öl anschwitzen.",
      "Gewürze hinzufügen, 1 Min. rösten.",
      "Linsen und Brühe dazugeben, aufkochen.",
      "25 Min. köcheln lassen bis Linsen weich sind.",
      "Hälfte pürieren für cremige Konsistenz, mit Zitrone abschmecken."
    ]
  },
  {
    "recipeId": "r_023",
    "title": "Lachs-Salat mit Avocado",
    "emoji": "🐟",
    "prepTime": 10, "cookTime": 12, "servings": 1,
    "calories": 420, "protein": 32, "carbs": 12, "fat": 28, "fiber": 7,
    "color": "yellow",
    "difficulty": "leicht",
    "tags": "high-protein,low-carb,omega3,mittag",
    "ingredients": [
      {"name": "Lachsfilet", "amount": 150, "unit": "g"},
      {"name": "Avocado", "amount": 0.5, "unit": "Stück"},
      {"name": "Rucola", "amount": 60, "unit": "g"},
      {"name": "Kirschtomaten", "amount": 100, "unit": "g"},
      {"name": "Gurke", "amount": 0.5, "unit": "Stück"},
      {"name": "Olivenöl", "amount": 15, "unit": "ml"},
      {"name": "Zitronensaft", "amount": 15, "unit": "ml"}
    ],
    "steps": [
      "Lachs mit Salz und Pfeffer würzen, in Pfanne 5-6 Min. pro Seite braten.",
      "Rucola, Tomaten und Gurke in Schüssel mischen.",
      "Avocado würfeln, dazugeben.",
      "Olivenöl und Zitronensaft als Dressing.",
      "Lachs darauflegen und servieren."
    ]
  },
  {"recipeId":"r_024","title":"Vollkorn-Wrap mit Truthahn","emoji":"🌯","calories":380,"protein":28,"carbs":38,"fat":10,"color":"yellow","tags":"high-protein,schnell,mittag"},
  {"recipeId":"r_025","title":"Gazpacho (kalte Suppe)","emoji":"🍅","calories":120,"protein":4,"carbs":22,"fat":3,"color":"green","tags":"vegan,sommergericht,leicht,mittag"},
  {"recipeId":"r_026","title":"Hühnchen-Gemüse-Wok","emoji":"🥢","calories":390,"protein":36,"carbs":32,"fat":11,"color":"yellow","tags":"high-protein,schnell,mittag"},
  {"recipeId":"r_027","title":"Vegane Bowls mit Tempeh","emoji":"🌱","calories":440,"protein":24,"carbs":48,"fat":16,"color":"yellow","tags":"vegan,meal-prep,mittag"},
  {"recipeId":"r_028","title":"Tomatensuppe mit Basilikum","emoji":"🍅","calories":160,"protein":5,"carbs":28,"fat":4,"color":"green","tags":"vegan,suppe,mittag"},
  {"recipeId":"r_029","title":"Quinoa-Salat mediterran","emoji":"🫙","calories":360,"protein":14,"carbs":50,"fat":12,"color":"yellow","tags":"vegan,meal-prep,mittag"},
  {"recipeId":"r_030","title":"Zucchini-Nudeln mit Pesto","emoji":"🌿","calories":280,"protein":8,"carbs":18,"fat":20,"color":"yellow","tags":"low-carb,vegetarisch,schnell,mittag"},
  {"recipeId":"r_031","title":"Griechischer Salat mit Feta","emoji":"🫒","calories":260,"protein":10,"carbs":14,"fat":18,"color":"green","tags":"vegetarisch,leicht,mittag"},
  {"recipeId":"r_032","title":"Hähnchen-Caesar-Salat","emoji":"🥗","calories":370,"protein":34,"carbs":14,"fat":20,"color":"yellow","tags":"high-protein,mittag"},
  {"recipeId":"r_033","title":"Minestrone Gemüsesuppe","emoji":"🫕","calories":220,"protein":9,"carbs":38,"fat":4,"color":"green","tags":"vegan,meal-prep,suppe,mittag"},
  {"recipeId":"r_034","title":"Tofu-Erdnuss-Bowl","emoji":"🥜","calories":460,"protein":22,"carbs":44,"fat":22,"color":"yellow","tags":"vegan,mittag"},
  {"recipeId":"r_035","title":"Türkisches Linsen-Tabbouleh","emoji":"🌾","calories":290,"protein":12,"carbs":46,"fat":7,"color":"green","tags":"vegan,meal-prep,mittag"},
  {"recipeId":"r_036","title":"Gurken-Dill-Suppe","emoji":"🥒","calories":110,"protein":5,"carbs":12,"fat":4,"color":"green","tags":"vegetarisch,leicht,suppe,mittag"},
  {"recipeId":"r_037","title":"Soba-Nudeln mit Edamame","emoji":"🫛","calories":360,"protein":18,"carbs":54,"fat":8,"color":"yellow","tags":"vegan,mittag"},
  {"recipeId":"r_038","title":"Chicken-Shawarma-Wrap","emoji":"🌮","calories":420,"protein":32,"carbs":40,"fat":12,"color":"yellow","tags":"high-protein,mittag"},
  {"recipeId":"r_039","title":"Kohl-Sellerie-Salat","emoji":"🥬","calories":140,"protein":4,"carbs":22,"fat":4,"color":"green","tags":"vegan,leicht,mittag"},
  {"recipeId":"r_040","title":"Thunfisch-Avocado-Bowl","emoji":"🐟","calories":390,"protein":34,"carbs":16,"fat":22,"color":"yellow","tags":"high-protein,low-carb,mittag"},
  {"recipeId":"r_041","title":"Blumenkohl-Curry","emoji":"🌸","calories":280,"protein":10,"carbs":34,"fat":12,"color":"yellow","tags":"vegan,mittag"},
  {"recipeId":"r_042","title":"Süßkartoffel-Kichererbsen-Salat","emoji":"🍠","calories":340,"protein":12,"carbs":56,"fat":8,"color":"green","tags":"vegan,meal-prep,mittag"},
  {"recipeId":"r_043","title":"Lachs-Quinoa-Salat","emoji":"🐠","calories":460,"protein":36,"carbs":38,"fat":16,"color":"yellow","tags":"high-protein,omega3,mittag"},
  {"recipeId":"r_044","title":"Brokkoli-Cheddar-Suppe","emoji":"🥦","calories":260,"protein":12,"carbs":22,"fat":14,"color":"yellow","tags":"vegetarisch,suppe,mittag"},
  {"recipeId":"r_045","title":"Mexikanische Black-Bean-Bowl","emoji":"🫘","calories":400,"protein":18,"carbs":60,"fat":9,"color":"yellow","tags":"vegan,high-fiber,mittag"}
]
```

#### 🍽️ ABENDESSEN (25 Rezepte)

```json
[
  {
    "recipeId": "r_046",
    "title": "Ofenhähnchen mit Gemüse",
    "emoji": "🍗",
    "prepTime": 10, "cookTime": 35, "servings": 2,
    "calories": 380, "protein": 42, "carbs": 22, "fat": 14, "fiber": 6,
    "color": "yellow", "difficulty": "leicht",
    "tags": "high-protein,meal-prep,abend",
    "ingredients": [
      {"name": "Hähnchenschenkel", "amount": 300, "unit": "g"},
      {"name": "Karotten", "amount": 2, "unit": "Stück"},
      {"name": "Zucchini", "amount": 1, "unit": "Stück"},
      {"name": "Paprika", "amount": 1, "unit": "Stück"},
      {"name": "Olivenöl", "amount": 20, "unit": "ml"},
      {"name": "Kräuter der Provence", "amount": 2, "unit": "TL"},
      {"name": "Knoblauch", "amount": 2, "unit": "Zehen"}
    ],
    "steps": [
      "Ofen auf 200°C vorheizen.",
      "Gemüse grob würfeln, auf Backblech legen.",
      "Hähnchen mit Olivenöl, Knoblauch und Kräutern marinieren.",
      "Hähnchen auf Gemüse legen, 35 Min. im Ofen garen.",
      "Kerntemperatur 75°C prüfen bevor servieren."
    ]
  },
  {
    "recipeId": "r_047",
    "title": "Gebratener Lachs mit Brokkoli",
    "emoji": "🥦",
    "prepTime": 5, "cookTime": 15, "servings": 1,
    "calories": 440, "protein": 38, "carbs": 14, "fat": 26, "fiber": 5,
    "color": "yellow", "difficulty": "leicht",
    "tags": "high-protein,low-carb,omega3,abend"
  },
  {"recipeId":"r_048","title":"Vegane Bolognese (Linsen)","emoji":"🍝","calories":380,"protein":18,"carbs":56,"fat":8,"color":"yellow","tags":"vegan,pasta,abend"},
  {"recipeId":"r_049","title":"Hackfleisch-Paprika-Pfanne","emoji":"🫑","calories":420,"protein":32,"carbs":24,"fat":20,"color":"yellow","tags":"high-protein,schnell,abend"},
  {"recipeId":"r_050","title":"Zitronenhähnchen mit Quinoa","emoji":"🍋","calories":460,"protein":40,"carbs":42,"fat":12,"color":"yellow","tags":"high-protein,abend"},
  {"recipeId":"r_051","title":"Gebratener Tofu mit Brokkoli","emoji":"🥦","calories":310,"protein":20,"carbs":22,"fat":16,"color":"green","tags":"vegan,low-carb,abend"},
  {"recipeId":"r_052","title":"Lachs-Curry mit Kokosmilch","emoji":"🥥","calories":480,"protein":34,"carbs":24,"fat":28,"color":"orange","tags":"high-protein,abend"},
  {"recipeId":"r_053","title":"Miso-Suppe mit Tofu","emoji":"🍜","calories":160,"protein":12,"carbs":14,"fat":6,"color":"green","tags":"vegan,leicht,abend"},
  {"recipeId":"r_054","title":"Gefüllte Paprika (Hackfleisch)","emoji":"🫑","calories":360,"protein":28,"carbs":26,"fat":16,"color":"yellow","tags":"abend"},
  {"recipeId":"r_055","title":"Gemüsecurry mit Kichererbsen","emoji":"🍛","calories":340,"protein":14,"carbs":52,"fat":9,"color":"green","tags":"vegan,abend"},
  {"recipeId":"r_056","title":"Hähnchenstreifen mit Süßkartoffel","emoji":"🍠","calories":440,"protein":36,"carbs":46,"fat":10,"color":"yellow","tags":"high-protein,meal-prep,abend"},
  {"recipeId":"r_057","title":"Forelle aus dem Ofen","emoji":"🐟","calories":320,"protein":36,"carbs":8,"fat":16,"color":"yellow","tags":"high-protein,low-carb,abend"},
  {"recipeId":"r_058","title":"Vegane Tacos (Black Beans)","emoji":"🌮","calories":380,"protein":16,"carbs":58,"fat":9,"color":"yellow","tags":"vegan,abend"},
  {"recipeId":"r_059","title":"Hähnchen-Brokkoli-Auflauf","emoji":"🧀","calories":400,"protein":38,"carbs":18,"fat":20,"color":"yellow","tags":"high-protein,abend"},
  {"recipeId":"r_060","title":"Roasted Veggies Grain Bowl","emoji":"🌾","calories":380,"protein":14,"carbs":58,"fat":10,"color":"yellow","tags":"vegan,abend"},
  {"recipeId":"r_061","title":"Spaghetti Carbonara (leicht)","emoji":"🍝","calories":460,"protein":24,"carbs":58,"fat":14,"color":"yellow","tags":"vegetarisch,abend"},
  {"recipeId":"r_062","title":"Kabeljau-Filet mit Salsa","emoji":"🐡","calories":280,"protein":32,"carbs":12,"fat":10,"color":"green","tags":"high-protein,leicht,abend"},
  {"recipeId":"r_063","title":"Glasnudeln mit Gemüse","emoji":"🥢","calories":310,"protein":10,"carbs":52,"fat":7,"color":"yellow","tags":"vegan,abend"},
  {"recipeId":"r_064","title":"Pute mit Pilzsoße","emoji":"🍄","calories":370,"protein":40,"carbs":14,"fat":16,"color":"yellow","tags":"high-protein,abend"},
  {"recipeId":"r_065","title":"Schwarze Bohnen Enchiladas","emoji":"🌯","calories":420,"protein":18,"carbs":60,"fat":12,"color":"yellow","tags":"vegan,abend"},
  {"recipeId":"r_066","title":"Spinat-Ricotta-Cannelloni","emoji":"🥬","calories":390,"protein":20,"carbs":44,"fat":14,"color":"yellow","tags":"vegetarisch,abend"},
  {"recipeId":"r_067","title":"Hähnchen-Shawarma Teller","emoji":"🥙","calories":450,"protein":38,"carbs":38,"fat":14,"color":"yellow","tags":"high-protein,abend"},
  {"recipeId":"r_068","title":"Kürbiscremesuppe","emoji":"🎃","calories":200,"protein":6,"carbs":32,"fat":6,"color":"green","tags":"vegan,suppe,abend"},
  {"recipeId":"r_069","title":"Zucchini-Hackfleisch-Pfanne","emoji":"🥒","calories":380,"protein":30,"carbs":14,"fat":22,"color":"yellow","tags":"low-carb,abend"},
  {"recipeId":"r_070","title":"Jakobsmuscheln mit Erbsenpüree","emoji":"🫛","calories":290,"protein":24,"carbs":24,"fat":10,"color":"yellow","tags":"seafood,abend"}
]
```

#### 🥨 SNACKS (15 Rezepte)

```json
[
  {"recipeId":"r_071","title":"Energy Balls (Dattel & Kakao)","emoji":"🍫","calories":120,"protein":4,"carbs":16,"fat":5,"color":"yellow","tags":"vegan,snack,meal-prep"},
  {"recipeId":"r_072","title":"Hummus mit Gemüsesticks","emoji":"🥕","calories":150,"protein":6,"carbs":18,"fat":6,"color":"green","tags":"vegan,snack"},
  {"recipeId":"r_073","title":"Protein-Riegel (Haferflocken)","emoji":"💪","calories":180,"protein":10,"carbs":22,"fat":6,"color":"yellow","tags":"high-protein,snack,meal-prep"},
  {"recipeId":"r_074","title":"Apfel mit Mandelbutter","emoji":"🍎","calories":160,"protein":4,"carbs":24,"fat":7,"color":"green","tags":"snack,schnell"},
  {"recipeId":"r_075","title":"Edamame mit Meersalz","emoji":"🫛","calories":100,"protein":8,"carbs":8,"fat":4,"color":"green","tags":"vegan,snack"},
  {"recipeId":"r_076","title":"Frozen Yogurt Beeren","emoji":"🍦","calories":130,"protein":6,"carbs":22,"fat":2,"color":"green","tags":"vegetarisch,snack,dessert"},
  {"recipeId":"r_077","title":"Kokos-Protein-Bällchen","emoji":"🥥","calories":140,"protein":8,"carbs":14,"fat":6,"color":"yellow","tags":"snack,meal-prep"},
  {"recipeId":"r_078","title":"Reiswaffeln mit Avocado","emoji":"🍚","calories":120,"protein":2,"carbs":18,"fat":5,"color":"green","tags":"vegan,schnell,snack"},
  {"recipeId":"r_079","title":"Nussmix (Mandeln/Cashews)","emoji":"🌰","calories":170,"protein":5,"carbs":8,"fat":14,"color":"orange","tags":"vegan,snack"},
  {"recipeId":"r_080","title":"Smoothie (Beeren & Protein)","emoji":"🍓","calories":200,"protein":16,"carbs":28,"fat":3,"color":"green","tags":"high-protein,snack"},
  {"recipeId":"r_081","title":"Guacamole mit Nachos","emoji":"🥑","calories":220,"protein":3,"carbs":24,"fat":14,"color":"orange","tags":"vegan,treat,snack"},
  {"recipeId":"r_082","title":"Joghurt-Parfait","emoji":"🍨","calories":180,"protein":12,"carbs":24,"fat":4,"color":"green","tags":"vegetarisch,schnell,snack"},
  {"recipeId":"r_083","title":"Gebratene Kichererbsen","emoji":"🫘","calories":160,"protein":8,"carbs":24,"fat":4,"color":"green","tags":"vegan,snack,meal-prep"},
  {"recipeId":"r_084","title":"Ei-Weißwürfelchen","emoji":"🥚","calories":80,"protein":10,"carbs":0,"fat":4,"color":"green","tags":"high-protein,low-carb,snack"},
  {"recipeId":"r_085","title":"Ingwer-Kurkuma-Shot","emoji":"🧡","calories":30,"protein":0,"carbs":7,"fat":0,"color":"green","tags":"vegan,detox,snack"}
]
```

#### 🍮 DESSERTS (15 Rezepte)

```json
[
  {"recipeId":"r_086","title":"Protein-Muffins Blaubeere","emoji":"🫐","calories":160,"protein":10,"carbs":20,"fat":4,"color":"yellow","tags":"high-protein,backen,dessert"},
  {"recipeId":"r_087","title":"Nice Cream (Bananen-Eis)","emoji":"🍦","calories":140,"protein":2,"carbs":34,"fat":1,"color":"green","tags":"vegan,dessert"},
  {"recipeId":"r_088","title":"Dunkle-Schokolade-Mousse","emoji":"🍫","calories":210,"protein":5,"carbs":20,"fat":12,"color":"yellow","tags":"vegetarisch,dessert"},
  {"recipeId":"r_089","title":"Erdbeeren mit Balsamico","emoji":"🍓","calories":90,"protein":1,"carbs":20,"fat":0,"color":"green","tags":"vegan,leicht,dessert"},
  {"recipeId":"r_090","title":"Kürbis-Protein-Kuchen","emoji":"🎃","calories":200,"protein":12,"carbs":24,"fat":6,"color":"yellow","tags":"backen,high-protein,dessert"},
  {"recipeId":"r_091","title":"Chia-Pudding-Torte (no bake)","emoji":"🎂","calories":240,"protein":8,"carbs":30,"fat":10,"color":"yellow","tags":"vegan,dessert"},
  {"recipeId":"r_092","title":"Frozen Yogurt Bark","emoji":"🧊","calories":120,"protein":5,"carbs":18,"fat":3,"color":"green","tags":"vegetarisch,dessert"},
  {"recipeId":"r_093","title":"Haferflocken-Cookies","emoji":"🍪","calories":180,"protein":5,"carbs":26,"fat":6,"color":"yellow","tags":"vegan,backen,dessert"},
  {"recipeId":"r_094","title":"Kokos-Panna-Cotta","emoji":"🥥","calories":190,"protein":3,"carbs":22,"fat":10,"color":"yellow","tags":"vegan,dessert"},
  {"recipeId":"r_095","title":"Protein-Brownies","emoji":"🍫","calories":200,"protein":14,"carbs":22,"fat":6,"color":"yellow","tags":"high-protein,backen,dessert"},
  {"recipeId":"r_096","title":"Watermelon Slushie","emoji":"🍉","calories":60,"protein":1,"carbs":14,"fat":0,"color":"green","tags":"vegan,leicht,dessert"},
  {"recipeId":"r_097","title":"Gesunder Käsekuchen","emoji":"🍰","calories":220,"protein":14,"carbs":22,"fat":8,"color":"yellow","tags":"vegetarisch,backen,dessert"},
  {"recipeId":"r_098","title":"Dattel-Kakaoballen","emoji":"🟫","calories":150,"protein":3,"carbs":24,"fat":5,"color":"yellow","tags":"vegan,dessert,snack"},
  {"recipeId":"r_099","title":"Mango-Kokosnuss-Parfait","emoji":"🥭","calories":200,"protein":4,"carbs":32,"fat":7,"color":"yellow","tags":"vegan,dessert"},
  {"recipeId":"r_100","title":"Erdbeer-Rhabarber-Kompott","emoji":"🍓","calories":80,"protein":1,"carbs":18,"fat":0,"color":"green","tags":"vegan,leicht,dessert"}
]
```

### Implementation in Room
```kotlin
// assets/recipes.json → beim ersten Start seeden
class RecipeSeedWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recipeDao: RecipeDao
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val json = context.assets.open("recipes.json").bufferedReader().readText()
        val recipes = Moshi.Builder().build().adapter(List::class.java).fromJson(json)
        recipeDao.insertAll(recipes as List<Recipe>)
        return Result.success()
    }
}
```

---

## 2. 🤖 KI-Coach / Chat-System

### Konzept: Regelbasierter Coach (kein echtes KI-Backend nötig)
Funktioniert 100% offline mit einem Entscheidungsbaum + vordefinierten Antwort-Templates.

### Room Entity
```kotlin
@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isFromCoach: Boolean,           // true = Coach, false = Nutzer
    val messageType: String,            // "greeting","tip","question","motivation","feedback"
    val contextType: String?,           // "weight_loss","food_log","streak","plateau"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
```

### Coach-Logik (CoachEngine.kt)
```kotlin
object CoachEngine {

    // Täglich personalisierte Nachrichten
    fun getDailyMessage(profile: UserProfile, stats: DailyStats): String {
        return when {
            stats.streakDays >= 7   -> motivationMessages.random().format(profile.name, stats.streakDays)
            stats.weightLostKg > 0  -> progressMessages.random().format(stats.weightLostKg)
            stats.caloriesUnder     -> calorieSucessMessages.random()
            stats.missedYesterday   -> comebackMessages.random()
            else                    -> defaultMorningMessages.random().format(profile.name)
        }
    }

    // Reaktion auf Nutzereingaben (Keyword-Matching)
    fun respondToUser(input: String, context: AppContext): String {
        val lower = input.lowercase()
        return when {
            lower.contains("hunger")    -> hungerTips.random()
            lower.contains("plateau")   -> plateauAdvice.random()
            lower.contains("motivat")   -> motivationBoost.random()
            lower.contains("rezept")    -> recipesuggestion(context)
            lower.contains("sport")     -> fitnessTips.random()
            lower.contains("stress")    -> stressTips.random()
            lower.contains("schlechte") -> reframeMessages.random()
            else                        -> generalResponses.random()
        }
    }

    // 50+ vordefinierte Antworten pro Kategorie
    private val hungerTips = listOf(
        "Trink erstmal ein großes Glas Wasser – oft ist Hunger eigentlich Durst! 💧",
        "Warte 10 Minuten. Wenn der Hunger noch da ist, dann iss was Grünes 🥦",
        "Ein Apfel mit etwas Mandelbutter ist ideal – hält lange satt! 🍎"
    )
    private val plateauAdvice = listOf(
        "Plateaus sind völlig normal – dein Körper passt sich an. Bleib dran! 💪",
        "Tipp: Versuche mal 2-3 Tage etwas mehr Kalorien zu essen (Refeeding). Das kann den Stoffwechsel ankurbeln.",
        "Hast du deine Portionsgrößen kürzlich überprüft? Oft schleichen sich kleine Mengen ein..."
    )
    // ... usw.
}
```

### Chat-Screen UI
```kotlin
@Composable
fun CoachChatScreen(viewModel: CoachViewModel = hiltViewModel()) {
    // Gradient Header mit Coach-Avatar
    // LazyColumn mit Nachrichten-Bubbles
    // Nutzer: rechts, Ozean-Blau Bubble
    // Coach: links, weißes/graues Bubble mit Avatar-Icon
    // Quick-Reply-Chips unten: "Ich habe Hunger", "Zeig mir ein Rezept", "Ich bin demotiviert"
    // TextField für freie Eingabe
}
```

### Quick-Reply Chips (8 voreingestellt)
```
💧 "Ich habe Hunger"
😔 "Ich bin demotiviert"
📊 "Wie läuft es?"
🍽️ "Rezeptvorschlag"
⚖️ "Ich stecke auf einem Plateau"
🏃 "Sport-Tipp"
😰 "Ich hatte Stress"
🎉 "Ich habe mein Ziel erreicht!"
```

---

## 3. 👟 Schrittzähler & Fitness-Tracking

### Health Connect Integration
```kotlin
// build.gradle.kts
implementation("androidx.health.connect:connect-client:1.1.0-rc01")
```

### Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.health.READ_STEPS"/>
<uses-permission android:name="android.permission.health.WRITE_STEPS"/>
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"/>
```

### StepRepository
```kotlin
class StepRepository @Inject constructor(
    private val healthConnectClient: HealthConnectClient,
    private val stepSensorManager: StepSensorManager   // Fallback
) {
    suspend fun getTodaySteps(): Int {
        return try {
            // Health Connect (primär)
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    startOfToday(), Instant.now()
                )
            )
            healthConnectClient.readRecords(request)
                .records.sumOf { it.count }.toInt()
        } catch (e: Exception) {
            // Fallback: Android Step Counter Sensor
            stepSensorManager.currentSteps
        }
    }

    fun calculateCaloriesBurned(steps: Int, weightKg: Float): Float {
        // MET-Formel: 1 Schritt ≈ 0.04 kcal für 70kg Person
        val metFactor = weightKg / 70f
        return steps * 0.04f * metFactor
    }
}
```

### Room Entity
```kotlin
@Entity(tableName = "step_entries",
    indices = [Index("dateEpochDay", unique = true)])
data class StepEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val steps: Int,
    val stepGoal: Int = 10000,
    val caloriesBurned: Float,
    val distanceKm: Float,          // steps * 0.00075km (Durchschnitt)
    val source: String              // "health_connect","sensor","manual"
)
```

### UI: Schrittzähler-Widget (Home Card)
```kotlin
@Composable
fun StepCounterCard(steps: Int, goal: Int = 10000) {
    // Kreisförmiger Fortschrittsring in Ocean Blue
    // Große Schrittzahl in der Mitte
    // "X von 10.000 Schritten" darunter
    // Kalorien-Badge rechts unten
    // Animierter Ring-Fortschritt beim Screen-Eintritt
}
```

### Schritt-Ziele
```kotlin
val stepGoalLevels = listOf(
    5_000  to "Einstieg 🚶",
    7_500  to "Aktiv 🚶‍♀️",
    10_000 to "Standard 💪",
    12_500 to "Ambitioniert 🏃",
    15_000 to "Profi 🏃‍♀️"
)
```

---

## 4. 💧 Wassertracking (Erweiterung)

### Room Entity
```kotlin
@Entity(tableName = "water_entries",
    indices = [Index("dateEpochDay", unique = true)])
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val amountMl: Int,
    val goalMl: Int = 2000,
    val glassesCount: Int          // amountMl / 250
)
```

### UI: Water Tracker Card
```kotlin
// Animiertes Wasser-Fill-Animation (Lottie oder Custom Canvas)
// + / - Buttons für 250ml
// Gläser-Icons (blau gefüllt = getrunken, outline = noch nicht)
// Tagesstatistik: "1.500 von 2.000ml"
```

---

## 5. 📸 Fortschrittsfotos

```kotlin
@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val filePath: String,          // lokal gespeichert (kein Upload!)
    val weightAtTime: Float?,
    val note: String?
)
```

### UI: Before/After Slider
```kotlin
// Horizontaler Drag-Slider zwischen zwei Fotos
// "Davor" links, "Danach" rechts
// Gewichts-Differenz als Badge
// Privatmodus: Biometric-Auth vor Anzeige
```

---

## 6. 🏠 Homescreen-Widget (Android Widget)

```kotlin
// Zeigt auf dem Android-Homescreen:
// - Kalorien heute (Ring)
// - Schritte heute
// - Streak-Zahl
// - "Heutige Lektion lesen" Button

class JeanFitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Glance Compose UI
            Column {
                Text("🔥 ${streak} Tage Streak")
                CircularProgressIndicator(progress = calorieProgress)
                Text("${steps} Schritte")
                Button("Lektion lesen", onClick = actionStartActivity<MainActivity>())
            }
        }
    }
}
// Library: implementation("androidx.glance:glance-appwidget:1.1.1")
```

---

## 7. 🔔 Notification-System (vollständig ausbauen)

```kotlin
// Aktuell: WorkManager-Jobs vorhanden
// Erweitern um:

// 1. Smart Reminders (kontextbasiert)
class SmartReminderWorker : CoroutineWorker() {
    // Sendet Erinnerung NUR wenn:
    // - Nutzer hat App heute noch nicht geöffnet
    // - UND es ist nach 18:00
    // - UND Mahlzeiten fehlen noch
}

// 2. Milestone Notifications
// "🎉 Du hast 5kg abgenommen!" → Push mit Konfetti
// "🔥 7-Tage-Streak erreicht!" → Push mit Streak-Badge

// 3. Wöchentliche Zusammenfassung (Sonntag 18:00)
// "Diese Woche: -0,8kg | 3 Lektionen | 45.000 Schritte"
```

---

## 8. 🎨 Onboarding-Verbesserungen

```kotlin
// Noch ausstehend:
// - Animierter Splash Screen (Lottie, blaue Welle)
// - Motivations-Video/GIF auf Welcome Screen
// - "Wie hast du von uns erfahren?" Screen
// - Body-Silhouette-Visualisierung (Zielgewicht)
// - Ziel-Datum-Prognose animiert einblenden
// - Körpermaße erfassen (optional)
```

---

## 🗂️ Dateien die erstellt/geändert werden müssen

```
DESIGN-ÄNDERUNGEN:
├── ui/theme/Color.kt                    → komplette Neudefinition (Ocean Blue)
├── ui/theme/Theme.kt                    → neues ColorScheme Dark/Light
├── ui/theme/Typography.kt              → Nunito + Inter Fonts einbinden
└── res/font/                           → Nunito_*.ttf, Inter_*.ttf

NEUE FEATURE-DATEIEN:
├── data/db/entities/StepEntry.kt
├── data/db/entities/WaterEntry.kt
├── data/db/entities/ProgressPhoto.kt
├── data/db/entities/CoachMessage.kt
├── data/db/dao/StepDao.kt
├── data/db/dao/WaterDao.kt
├── data/db/dao/CoachDao.kt
├── data/repository/StepRepository.kt
├── data/repository/WaterRepository.kt
├── data/repository/CoachRepository.kt
├── domain/usecase/GetDailyStepsUseCase.kt
├── domain/coach/CoachEngine.kt
├── ui/coach/CoachChatScreen.kt
├── ui/coach/CoachViewModel.kt
├── ui/fitness/StepCounterCard.kt
├── ui/fitness/FitnessScreen.kt
├── ui/progress/ProgressPhotoScreen.kt
├── ui/water/WaterTrackerCard.kt
├── worker/SmartReminderWorker.kt
├── worker/WeeklySummaryWorker.kt
├── widget/JeanFitWidget.kt
└── assets/recipes.json                 → alle 100 Rezepte als JSON

ERWEITERTE DATEIEN:
├── data/db/JeanFitDatabase.kt          → +4 neue Entities
├── navigation/NavGraph.kt              → Coach-Screen, Fitness-Screen hinzufügen
├── ui/home/HomeScreen.kt               → StepCard + WaterCard hinzufügen
├── ui/tools/ToolsScreen.kt             → mehr Rezepte laden
└── AndroidManifest.xml                 → Health Connect Permissions
```

---

## 📦 Neue Dependencies

```toml
# libs.versions.toml — ERGÄNZUNGEN

[versions]
health-connect = "1.1.0-rc01"
glance = "1.1.1"
accompanist-permissions = "0.37.0"

[libraries]
health-connect = { group = "androidx.health.connect", name = "connect-client", version.ref = "health-connect" }
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanist-permissions" }
# Fonts
google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts", version = "1.8.1" }
```

---

## ✅ Implementierungsreihenfolge (empfohlen)

```
Woche 1:
  [ ] Schritt 1: Design-System auf Ocean Blue umstellen (alle Files)
  [ ] Schritt 2: recipes.json Asset mit 100 Rezepten erstellen
  [ ] Schritt 3: RecipeSeedWorker implementieren + DB-Migration

Woche 2:
  [ ] Schritt 4: CoachEngine + CoachChatScreen
  [ ] Schritt 5: Quick-Reply Chips + 50 Coach-Antworten schreiben
  [ ] Schritt 6: Coach in Bottom Navigation einbinden

Woche 3:
  [ ] Schritt 7: StepEntry Entity + StepRepository
  [ ] Schritt 8: Health Connect Integration + Sensor-Fallback
  [ ] Schritt 9: StepCounterCard auf HomeScreen

Woche 4:
  [ ] Schritt 10: WaterTrackerCard
  [ ] Schritt 11: SmartReminderWorker
  [ ] Schritt 12: HomeScreen-Widget (Glance)
  [ ] Schritt 13: Fortschrittsfotos
  [ ] Schritt 14: Finale Tests + Polishing
```

---

## 💎 Design-Inspo: Ocean Blue Fitness Apps

| App | Blauton | Hex |
|-----|---------|-----|
| Calm | Dunkelblau-Grün | `#1A4A6E` |
| Headspace | Kobaltblau | `#FF6B35` (Orange) + `#1F4D8C` |
| Clue | Tiefes Marineblau | `#1D1B44` |
| **JeanFit (neu)** | **Ozean-Blau** | **`#1565C0`** |

**Tipp für deine Freundin:** Die Kombination `#1565C0` (Primär) + `#42A5F5` (Akzent) + `#00BCD4` (Teal) wirkt frisch, weiblich und modern — genau wie das Cover eines Lifestyle-Magazins. 💙
