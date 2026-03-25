# JeanFit

A full-featured Android nutrition and weight-loss coaching app — inspired by Noom. Built entirely offline-first with Jetpack Compose, Room DB, and Hilt.

---

## Screenshots

| Welcome | Home | Progress |
|---------|------|----------|
| Onboarding flow | Calorie dashboard | Weight tracking |

| Learn | Tools |
|-------|-------|
| Course map + lesson reader | Food search, recipes, meal planner |

---

## Features

### Onboarding (8 steps)
- Name, goal weight, current weight
- Gender, birth year, height
- Activity level selection
- Health conditions
- Motivation selection
- **Harris-Benedict calorie calculation** — computed locally, no server needed
- Profile created screen

### Home Dashboard
- Circular calorie progress ring
- 4 meal slots (Breakfast, Lunch, Dinner, Snack) with add buttons
- Daily task checklist (weigh in, log meals, read lesson)
- NoomCoin counter (gamification)
- Personalized greeting

### Food Logging
- Search via **OpenFoodFacts API** and **USDA FoodData Central**
- Barcode scanner with **CameraX + ML Kit**
- Manual fallback entry if scan fails
- Food color system (green / yellow / orange by calorie density)
- Serving size multiplier

### Progress
- Current vs. goal weight card
- Overall progress bar (start → goal)
- Weight history list
- Exponential moving average trend line (λ = 0.1)

### Learn (Course Map)
- 3-week structured course
- Lesson reader with rich content blocks:
  - Article (Markdown)
  - Tip cards
  - Quiz with instant feedback
  - Activity prompts
  - Reflection prompts
- Lesson completion tracking with coin rewards

### Tools
- Food search screen
- Recipe book (4 seeded healthy recipes)
- Meal planner (weekly calendar)
- Color system explainer card

### Gamification
- +1 NoomCoin for completing all 3 daily tasks
- Streak tracking (logging, weigh-in, lessons, all-tasks)
- Achievements system (first log, 30 meals, scanner, streaks, goal reached)
- Treat Day unlock at 5 coins (+20% calorie goal, no warnings)
- Library unlock at 15 coins

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1.20 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture + UDF |
| DI | Hilt (KSP) |
| Database | Room 2.8.4 (offline-first) |
| Navigation | Navigation Compose 2.9.6 |
| Network | Retrofit 2.11.0 + OkHttp 5.3.2 + Moshi |
| Camera | CameraX 1.4.1 |
| Barcode | ML Kit Barcode Scanning 17.3.0 |
| Images | Coil 3.1.0 |
| Charts | Vico 3.0.1 |
| Async | Kotlin Coroutines + Flow |
| Persistence | DataStore Preferences 1.2.0 |
| Background | WorkManager 2.11.0 |
| Animation | Lottie 6.6.2 |
| Calendar | Kizitonwose Calendar 2.6.1 |
| Build | Gradle 8.12, AGP 8.9.0, KSP 2.1.20-1.0.32 |

---

## Architecture

```
app/src/main/
├── data/
│   ├── db/
│   │   ├── JeanFitDatabase.kt        # Room DB, 12 entities
│   │   ├── dao/                      # One DAO per entity group
│   │   └── entities/                 # UserProfile, Food, Weight, Lessons, Gamification, Recipes
│   ├── repository/                   # UserRepository, FoodRepository, WeightRepository, ...
│   ├── api/                          # OpenFoodFactsApi, UsdaFoodApi (Retrofit)
│   ├── datastore/                    # UserPreferences (dark mode, onboarding flag)
│   └── model/
├── domain/
│   └── usecase/
│       ├── CalcCaloriesUseCase.kt    # Harris-Benedict formula
│       ├── CalcTrendUseCase.kt       # Exponential moving average
│       └── CalcColorCategoryUseCase.kt  # Green / yellow / orange
├── ui/
│   ├── theme/                        # JeanFit brand colors, typography
│   ├── onboarding/                   # 8-step onboarding flow
│   ├── home/                         # Dashboard
│   ├── foodlog/                      # Search + barcode scanner
│   ├── progress/                     # Weight chart + history
│   ├── learn/                        # Course map, lesson list, lesson reader
│   ├── tools/                        # Tools hub, recipes, meal planner
│   └── components/                   # JeanFitBottomBar, shared composables
├── navigation/                       # NavGraph, Screen routes, BottomNavItem
├── di/                               # DatabaseModule, NetworkModule (Hilt)
└── worker/                           # WorkManager jobs (reminders, streak check)
```

---

## Room Database Schema

12 entities across 6 DAOs:

- **UserProfile** — singleton, stores all onboarding data + coins + onboarding flag
- **FoodItem** — cached food items from API/barcode, with color category
- **FoodLogEntry** — daily meal logs with serving multiplier
- **WeightEntry** — daily weigh-ins + trend weight (EMA)
- **Course / Lesson / LessonProgress** — 3-week structured curriculum
- **DailyTask / Streak / Achievement** — full gamification system
- **Recipe / MealPlan** — recipe book + weekly planner

---

## Algorithms (all local, no backend)

### Harris-Benedict Calorie Calculation
```kotlin
fun calculateDailyCalories(gender, weightKg, heightCm, ageYears, activityLevel): Int
// BMR × activity factor − weekly deficit → minimum 1200 kcal
```

### Weight Trend (Exponential Moving Average)
```kotlin
fun calculateTrend(newWeightKg, previousTrend, lambda = 0.1f): Float
// Smooths daily fluctuations to show true progress direction
```

### Food Color Category
```kotlin
fun calculateColorCategory(caloriesPer100g, isWholeGrain): String
// ≤ 100 kcal/100g → green | 101-240 → yellow | > 240 → orange
// Whole grain foods get bumped one category greener
```

---

## APIs

| API | Key Required | Usage |
|-----|-------------|-------|
| OpenFoodFacts | No | Barcode lookup, product search |
| USDA FoodData Central | DEMO_KEY (free) | Food nutrition search |

Both APIs are only used to populate the local Room cache. The app works **100% offline** once data is cached.

---

## Build & Run

### Requirements
- Android Studio Meerkat or newer
- Android SDK 26+
- JDK 21

### Setup
```bash
git clone https://github.com/Pcf1337-hash/JeanFit.git
cd JeanFit
# Add local.properties with your SDK path:
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew assembleDebug
```

### APK
The app builds arm64-v8a only (targets modern Galaxy S/A series) — roughly half the size of a universal APK.

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

---

## Design System

Custom Material 3 theme with JeanFit brand colors:

| Token | Color | Usage |
|-------|-------|-------|
| SunsetOrange | `#FB513B` | CTAs, progress indicators, accents |
| SpringWood | `#F6F4EE` | Warm cream background |
| BlueDianne | `#1D3A44` | Headlines, dark text |
| FoodGreen | `#4CAF50` | Green food category |
| FoodYellow | `#FFC107` | Yellow food category |
| FoodOrange | `#FF6B35` | Orange food category |

Dark mode is fully supported on every screen.

---

## WorkManager Jobs

| Job | Schedule | Trigger |
|-----|----------|---------|
| Weight reminder | 08:00 daily | If no weigh-in today |
| Meal log reminder | 19:00 daily | If meals not finished |
| Lesson reminder | 10:00 daily | If lesson not read |
| Offline food sync | Daily, Wi-Fi only | Cache refresh |
| Streak check | Midnight | Resets expired streaks |

---

## License

MIT
