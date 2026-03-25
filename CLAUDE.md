# CLAUDE.md — Agent-Instruktionen: NoomClone Android App

> **Lies diese Datei zu Beginn JEDER Session. Kein Schritt ohne Verifikation.**

---

## Projektübersicht

**App-Name:** NutriMind (Noom-Klon)
**Plattform:** Android (minSdk 26, targetSdk 36)
**Sprache:** Kotlin 2.1+
**UI:** Jetpack Compose + Material 3 (Custom Theme)
**Datenbank:** Room DB (offline-first, Single Source of Truth)
**Architektur:** MVVM + Clean Architecture + UDF (Unidirectional Data Flow)
**DI:** Hilt (KSP)
**Ziel:** 100% fertige App – keine Platzhalter, keine ausgebauten Funktionen

---

## Pflicht-Workflow für JEDEN Task

### 1. Plan First
- Schreibe **immer zuerst** einen Plan in `tasks/todo.md`
- Kein Code ohne vorherigen Plan bei Tasks mit 3+ Schritten
- Nutze Subagenten für parallele Recherche oder isolierte Teilaufgaben
- Bei Problemen: **STOPP** → neu planen, nicht weiterpushen

### 2. Subagent-Strategie
- Research-Tasks → eigener Subagent
- Komplexe Architekturentscheidungen → Subagent für Alternativen-Analyse
- Parallele Feature-Implementierung → separate Subagenten
- Hauptkontext sauber halten

### 3. Verifikation vor "Done"
- Niemals Task als erledigt markieren ohne Beweis dass es funktioniert
- Build muss kompilieren: `./gradlew assembleDebug`
- UI-Tests für kritische Flows (Onboarding, Food-Logging, Weight-Entry)
- Frage dich: „Würde ein Senior-Engineer das abnehmen?"

### 4. Self-Improvement Loop
- Nach jeder Korrektur vom User → `tasks/lessons.md` updaten
- Muster dokumentieren die den gleichen Fehler verhindern
- Lessons zu Session-Start reviewen

### 5. Eleganz vor Hack
- Vor nicht-trivialem Code: „Gibt es eine elegantere Lösung?"
- Hacky-Fixes sind verboten – Rootcause finden
- Minimaler Impact: nur berühren was nötig ist

---

## Tech-Stack (exakte Versionen)

```toml
# libs.versions.toml
[versions]
kotlin = "2.1.20"
compose-bom = "2026.03.00"
room = "2.8.4"
hilt = "2.57.2"
navigation = "2.9.6"
retrofit = "2.11.0"
okhttp = "5.3.2"
coil = "3.1.0"
vico = "3.0.1"
mlkit-barcode = "17.3.0"
camerax = "1.4.1"
datastore = "1.2.0"
workmanager = "2.11.0"
lottie = "6.6.2"
calendar = "2.6.1"
markdown = "0.33.0"
coroutines = "1.10.2"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-tooling = { group = "androidx.compose.ui", name = "ui-tooling-preview" }

# Room
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" } # KSP

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Netzwerk
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Charts
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# Barcode
mlkit-barcode = { group = "com.google.mlkit", name = "barcode-scanning", version.ref = "mlkit-barcode" }
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

# Bilder
coil = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-network = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }

# DataStore
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# WorkManager
workmanager = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }

# Animationen
lottie = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }

# Kalender
calendar = { group = "com.kizitonwose.calendar", name = "compose", version.ref = "calendar" }

# Markdown
markdown = { group = "com.mikepenz", name = "multiplatform-markdown-renderer-m3", version.ref = "markdown" }
```

---

## Design-System (Custom Material 3 Theme)

```kotlin
// PFLICHT: Alle UI-Komponenten NUR mit diesen Farben/Styles

object NutriMindTheme {
    // Primär
    val SunsetOrange = Color(0xFFFB513B)   // CTAs, Akzente, Progress
    val SpringWood   = Color(0xFFF6F4EE)   // Background (warmes Creme)
    val BlueDianne   = Color(0xFF1D3A44)   // Dunkler Text, Headlines

    // Ampelsystem
    val FoodGreen    = Color(0xFF4CAF50)   // Grüne Lebensmittel
    val FoodYellow   = Color(0xFFFFC107)   // Gelbe Lebensmittel
    val FoodOrange   = Color(0xFFFF6B35)   // Orange Lebensmittel

    // Dark Mode Äquivalente
    val DarkBackground    = Color(0xFF121212)
    val DarkSurface       = Color(0xFF1E1E1E)
    val DarkSurfaceVariant = Color(0xFF2C2C2C)
    val DarkOnSurface     = Color(0xFFE8E0D8)   // warmes Off-White

    // Shapes
    val cardShape = RoundedCornerShape(16.dp)
    val buttonShape = RoundedCornerShape(12.dp)
    val chipShape = RoundedCornerShape(8.dp)
}
```

**Dark Mode ist PFLICHT** – alle Screens müssen Dark + Light unterstützen.

---

## Datenbankschema (Room DB)

### UserProfile
```kotlin
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton
    val name: String,
    val email: String?,
    val gender: String,           // "male","female","non_binary","prefer_not"
    val birthDateEpochDay: Long,
    val heightCm: Float,
    val startWeightKg: Float,
    val goalWeightKg: Float,
    val activityLevel: String,    // "sedentary","lightly_active","active","very_active"
    val dailyCalorieGoal: Int,    // Harris-Benedict berechnet
    val programStartDate: Long,   // epoch day
    val noomCoins: Int = 0,
    val onboardingCompleted: Boolean = false
)
```

### FoodItem + FoodLogEntry
```kotlin
@Entity(tableName = "food_items", indices = [Index("barcode"), Index("name")])
data class FoodItem(
    @PrimaryKey val foodId: String,   // UUID oder OpenFoodFacts-ID
    val name: String,
    val brand: String?,
    val barcode: String?,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float?,
    val defaultServingSizeG: Float,
    val colorCategory: String,        // "green","yellow","orange"
    val calorieDensity: Float,        // cal/g = caloriesPer100g / 100
    val source: String,               // "preloaded","usda","openfoodfacts","custom"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_log_entries",
    foreignKeys = [ForeignKey(FoodItem::class, ["foodId"], ["foodId"])])
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,
    val mealType: String,             // "breakfast","lunch","dinner","snack"
    val servingMultiplier: Float,     // 1.5 = 1.5 Portionen
    val servingSizeG: Float,          // tatsächliche Gramm
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val colorCategory: String,
    val logDateEpochDay: Long,
    val loggedAtMs: Long = System.currentTimeMillis()
)
```

### WeightEntry
```kotlin
@Entity(tableName = "weight_entries", indices = [Index("dateEpochDay", unique = true)])
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateEpochDay: Long,
    val note: String? = null,
    val trendWeightKg: Float? = null  // exponentieller Gleitender Ø (λ=0.1)
)
```

### LessonSystem
```kotlin
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val courseId: String,
    val title: String,
    val description: String,
    val weekNumber: Int,
    val iconEmoji: String,
    val totalLessons: Int,
    val requiredCoinsToUnlock: Int = 0
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val lessonId: String,
    val courseId: String,
    val title: String,
    val orderIndex: Int,
    val estimatedMinutes: Int,
    val contentJson: String,          // JSON-Array von Block-Objekten
    val lessonType: String,           // "article","quiz","activity","reflection"
    val coinsReward: Int = 0,
    val isAudioAvailable: Boolean = false
)

@Entity(tableName = "lesson_progress",
    primaryKeys = ["lessonId"])
data class LessonProgress(
    val lessonId: String,
    val isCompleted: Boolean = false,
    val completedAtMs: Long? = null,
    val quizScore: Int? = null,       // 0-100
    val timeSpentSeconds: Int = 0
)
```

### Gamification
```kotlin
@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val weightLogged: Boolean = false,
    val allMealsLogged: Boolean = false,  // + "Tag abschließen" getippt
    val lessonCompleted: Boolean = false,
    val coinAwarded: Boolean = false       // verhindert Doppelvergabe
)

@Entity(tableName = "streaks")
data class Streak(
    @PrimaryKey val type: String,     // "logging","lesson","weigh_in","all_tasks"
    val currentStreak: Int,
    val longestStreak: Int,
    val lastActivityEpochDay: Long
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,             // "logging","weight","lesson","streak"
    val targetValue: Int,
    val isUnlocked: Boolean = false,
    val unlockedAtMs: Long? = null,
    val currentProgress: Int = 0
)
```

### Rezepte & MealPlanner
```kotlin
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val recipeId: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val totalCaloriesPerServing: Float,
    val proteinPerServing: Float,
    val carbsPerServing: Float,
    val fatPerServing: Float,
    val greenPercent: Float,           // Anteil grüne Kalorien
    val yellowPercent: Float,
    val orangePercent: Float,
    val ingredientsJson: String,       // Liste: [{name, amount, unit, foodId?}]
    val stepsJson: String,             // Liste: [{step, instruction}]
    val tags: String,                  // CSV: "vegan,schnell,frühstück"
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(tableName = "meal_plan",
    primaryKeys = ["dateEpochDay","mealType"])
data class MealPlan(
    val dateEpochDay: Long,
    val mealType: String,
    val recipeId: String?,
    val customMealName: String?
)
```

---

## App-Navigation (Compose NavHost)

```
NavGraph:
├── onboarding/                    (nur wenn onboardingCompleted == false)
│   ├── welcome
│   ├── goal_weight
│   ├── current_weight
│   ├── gender_age_height
│   ├── activity_level
│   ├── health_conditions
│   ├── motivation
│   ├── calorie_calculation         (Harris-Benedict, Ergebnis zeigen)
│   └── profile_created             → Main
│
└── main/                           (Bottom Nav)
    ├── home/                       Tab 1: Dashboard
    │   └── home_screen
    ├── progress/                   Tab 2: Fortschritt
    │   ├── progress_screen
    │   ├── weight_entry_dialog
    │   └── weight_history
    ├── tools/                      Tab 3: Tools/Rezepte
    │   ├── tools_screen
    │   ├── recipe_list
    │   ├── recipe_detail/{recipeId}
    │   ├── meal_planner
    │   ├── food_search
    │   └── barcode_scanner
    └── learn/                      Tab 4: Lektionen
        ├── course_map
        ├── lesson_list/{courseId}
        └── lesson_reader/{lessonId}

Modals (über Navigation):
    ├── food_log_entry/{mealType}
    ├── quick_weight_entry
    └── achievement_unlocked/{achievementId}
```

---

## Offene API-Endpunkte (Offline-First)

```kotlin
// OpenFoodFacts (kostenlos, kein API-Key)
interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<OFFProductResponse>

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "product_name,brands,nutriments,serving_size,image_url"
    ): Response<OFFSearchResponse>
}
// BaseUrl: "https://world.openfoodfacts.org/"

// USDA FoodData Central (kostenlos, API-Key: Demo-Key für Dev)
interface UsdaFoodApi {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = BuildConfig.USDA_API_KEY,
        @Query("pageSize") pageSize: Int = 20,
        @Query("dataType") dataType: String = "Foundation,SR Legacy"
    ): Response<UsdaSearchResponse>
}
// BaseUrl: "https://api.nal.usda.gov/"
```

---

## Algorithmen (lokal, kein Backend nötig)

### Harris-Benedict Kalorienberechnung
```kotlin
fun calculateDailyCalories(
    gender: String, weightKg: Float, heightCm: Float,
    ageYears: Int, activityLevel: String, weeklyGoalKgLoss: Float = 0.5f
): Int {
    val bmr = when (gender) {
        "male"   -> 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * ageYears)
        "female" -> 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * ageYears)
        else     -> 500.0 + (11.0 * weightKg) + (4.0 * heightCm) - (5.0 * ageYears)
    }
    val activityFactor = when (activityLevel) {
        "sedentary"       -> 1.2
        "lightly_active"  -> 1.375
        "active"          -> 1.55
        "very_active"     -> 1.725
        else              -> 1.2
    }
    val tdee = bmr * activityFactor
    val deficit = weeklyGoalKgLoss * 7700 / 7  // ~550 kcal/Tag bei 0.5kg/Woche
    return maxOf(1200, (tdee - deficit).roundToInt())
}
```

### Gewichts-Trendlinie (Exponentieller Gleitender Durchschnitt)
```kotlin
fun calculateTrend(newWeightKg: Float, previousTrend: Float?, lambda: Float = 0.1f): Float {
    return if (previousTrend == null) newWeightKg
    else lambda * newWeightKg + (1 - lambda) * previousTrend
}
```

### Kaloriendichte & Farbkategorie
```kotlin
fun calculateColorCategory(caloriesPer100g: Float, isWholeGrain: Boolean = false): String {
    val cd = caloriesPer100g / 100f
    val category = when {
        cd <= 1.0f -> "green"
        cd <= 2.4f -> "yellow"
        else       -> "orange"
    }
    // Vollkorn: eine Stufe herunterstufen
    return if (isWholeGrain && category == "yellow") "green"
    else if (isWholeGrain && category == "orange") "yellow"
    else category
}
```

---

## Content-Format für Lektionen (JSON)

```json
[
  {
    "type": "text",
    "content": "## Dein Gehirn und das Essen\n\nKennst du das? Du hast gerade gegessen..."
  },
  {
    "type": "tip",
    "icon": "💡",
    "content": "**Tipp**: Vor dem Essen 30 Sekunden innehalten..."
  },
  {
    "type": "quiz",
    "question": "Was ist Kaloriendichte?",
    "options": ["Kalorien pro 100g", "Kalorien pro Tag", "Kalorien pro Stunde"],
    "correctIndex": 0,
    "explanation": "Richtig! Kaloriendichte = Kalorien ÷ Gramm."
  },
  {
    "type": "activity",
    "title": "Jetzt ausprobieren",
    "instruction": "Logge deine nächste Mahlzeit und achte auf die Farbe.",
    "action": "navigate_food_log"
  },
  {
    "type": "reflection",
    "prompt": "Was war heute dein größter Trigger für ungesundes Essen?",
    "placeholder": "Schreib hier deine Gedanken..."
  },
  {
    "type": "image",
    "resName": "ic_elephant_rider",
    "caption": "Elefant = emotionales Gehirn / Reiter = rationales Gehirn"
  }
]
```

---

## Gamification-Regeln (exakt umsetzen)

| Aktion | Belohnung |
|--------|-----------|
| Alle 3 Tagesaufgaben abgeschlossen | +1 Noomcoin |
| 7-Tage-Streak Mahlzeiten | Achievement "Eiserne Gewohnheit" |
| 7-Tage-Streak Gewicht | Achievement "Waagenprofi" |
| 7-Tage-Streak Lektionen | Achievement "Wissenssammler" |
| 5 Noomcoins | Treat Days freischalten |
| 15 Noomcoins | Bibliothek freischalten |
| Zielgewicht erreicht | Achievement "Ziel erreicht" + konfetti |
| Erste Mahlzeit geloggt | Achievement "Erster Schritt" |
| 30 Mahlzeiten geloggt | Achievement "Logging-Profi" |
| Ersten Barcode gescannt | Achievement "Scanner" |

**Treat Day:** Kalorienziel +20%, keine roten Auge-Warnungen, Icon ändert sich.

---

## WorkManager Jobs

```kotlin
// 1. Tägliche Erinnerung: Gewicht wiegen (08:00)
// 2. Abend-Reminder: Mahlzeiten abschließen (19:00, wenn nicht alle geloggt)
// 3. Lektion-Reminder (10:00, wenn Lektion nicht gelesen)
// 4. Offline-Sync: Food-Cache von OpenFoodFacts (WLAN, täglich)
// 5. Streak-Prüfung: Mitternacht – Streak-Reset wenn nötig
```

---

## Kritische Regeln (NIE brechen)

1. **Keine Platzhalter** – `TODO()` oder `// Implement later` sind verboten
2. **Kein Netzwerk-Pflicht** – App muss 100% offline funktionieren
3. **Room ist die einzige Wahrheit** – UI liest NUR aus Room (via Flow), nie direkt aus Netzwerk
4. **Dark Mode immer** – Kein Screen ohne `dynamicColor = false` + custom ColorScheme
5. **Harris-Benedict lokal** – Kein Server für Kalorienberechnung
6. **Barcode-Fallback** – Wenn ML Kit fehlschlägt: manuelle Eingabe immer möglich
7. **Keine leere States** – Jeder Screen hat Loading-, Error- und Empty-State
8. **Accessibility** – `contentDescription` für alle Icons und Bilder
9. **Test jede ViewModel-Methode** – Unit Tests mit JUnit5 + Turbine für Flows
10. **Compose Preview** – Jedes Composable hat `@Preview` Dark + Light

---

## Verzeichnisstruktur

```
app/src/main/
├── data/
│   ├── db/
│   │   ├── NutriMindDatabase.kt
│   │   ├── dao/         (ein DAO pro Entity-Gruppe)
│   │   └── entities/
│   ├── repository/      (ein Repo pro Feature-Domäne)
│   ├── api/             (OpenFoodFacts + USDA)
│   ├── datastore/       (UserPreferences, OnboardingState)
│   └── model/           (Domain Models ≠ DB Entities)
├── domain/
│   ├── usecase/         (CalcCaloriesUseCase, CalcTrendUseCase etc.)
│   └── model/
├── ui/
│   ├── theme/           (NutriMindTheme.kt, Color.kt, Typography.kt)
│   ├── onboarding/
│   ├── home/
│   ├── foodlog/
│   ├── progress/
│   ├── learn/
│   ├── recipes/
│   └── components/      (wiederverwendbare Composables)
├── worker/              (WorkManager Jobs)
└── di/                  (Hilt Module)
```

---

## Session-Start Checkliste

- [ ] `tasks/lessons.md` lesen (letzte Korrekturen reviewen)
- [ ] `tasks/todo.md` öffnen – wo waren wir?
- [ ] Aktuellen Build-Status prüfen: kompiliert alles?
- [ ] Nächsten offenen Task aus Phase identifizieren
- [ ] Plan schreiben bevor Code schreiben
