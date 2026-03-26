# JeanFit

A full-featured Android nutrition and weight-loss coaching app — inspired by Noom. Built entirely offline-first with Jetpack Compose, Room DB, and Hilt.

[![Download APK](https://img.shields.io/github/v/release/Pcf1337-hash/JeanFit?label=Download%20APK&color=1565C0)](https://github.com/Pcf1337-hash/JeanFit/releases/latest)

---

## What's New in v1.2.0

- **Settings Screen** — manage everything in one place
  - Edit name, goal weight, height, activity level, calorie target
  - Recalculate daily calories (Harris-Benedict) with a single tap
  - Set water goal & step goal
  - View profile info: gender, age, coins, programme days
- **125 German Recipes** — full recipe book with breakfast, lunch, dinner, snacks, desserts
  - All recipes in German with ingredients, steps, calorie data and colour categories
  - Replaced all English MealDB entries from previous versions
- **Meal Planner reworked** — day-by-day planning with recipe picker and daily calorie summary
- **Food log editing** — swipe to delete or tap to edit serving size of any logged item
- **Coach improvements**
  - Does not greet on every app start — only on first use
  - Sees your actual meals for today and gives contextual advice
  - Asks follow-up questions before giving recipe recommendations
  - Chat history persisted across sessions
- **Learn section** — 7 full courses × 5 lessons = 35 lessons with quizzes, activities & reflections

## What's New in v1.1.0

- **Jean — KI-Coach** powered by Claude Haiku (real Claude API, not offline fallback)
  - Personalized fitness & nutrition coaching
  - Long-term memory: remembers your goals, milestones, and challenges
  - Quick-reply chips for common questions
  - Unread message badge on the Coach tab
- **Ocean Blue Design System** — complete UI refresh (deep navy + sky blue)
- **In-App Auto-Update** — checks GitHub Releases on startup + manual update button
  - Download progress modal with changelog
  - Version skip option
  - Forced-update support for breaking changes

---

## Screenshots

| Welcome | Home | KI-Coach |
|---------|------|----------|
| Onboarding flow | Calorie dashboard | Chat with Jean |

| Progress | Learn | Update |
|----------|-------|--------|
| Weight tracking | Course map + lessons | In-app download modal |

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
- Manual update-check button (SystemUpdate icon)

### KI-Coach Jean
- Full chat interface with Claude Haiku AI
- Remembers your: name, goals, current stats, past topics (stored in Room DB)
- Dynamic system prompt built from live user data (calories today, activity level, etc.)
- Time-aware greeting on first open
- 8 quick-reply chips for fast questions
- Clear chat option
- Offline fallback messages if no internet

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

### Auto-Update
- Checks GitHub Releases API on every app start (24h cooldown)
- Manual check via header button
- Download progress modal with animated progress bar
- Scrollable changelog with markdown rendering
- Version skip & "remind me later" options
- Forced-update mode (set `FORCED_UPDATE: true` in release notes)

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
| AI Coach | Claude Haiku API (`claude-haiku-4-5-20251001`) |
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
│   │   ├── JeanFitDatabase.kt        # Room DB, 14 entities (v2 with Coach tables)
│   │   ├── dao/                      # One DAO per entity group + CoachDao
│   │   └── entities/                 # UserProfile, Food, Weight, Lessons, Gamification, Recipes, Coach
│   ├── repository/                   # UserRepository, FoodRepository, WeightRepository, CoachRepository
│   ├── api/                          # OpenFoodFactsApi, UsdaFoodApi, ClaudeApi, GithubApi
│   ├── datastore/                    # UserPreferences (dark mode, onboarding flag)
│   └── model/
├── domain/
│   ├── usecase/
│   │   ├── CalcCaloriesUseCase.kt    # Harris-Benedict formula
│   │   ├── CalcTrendUseCase.kt       # Exponential moving average
│   │   └── CalcColorCategoryUseCase.kt
│   └── update/
│       └── AppUpdateManager.kt       # GitHub Releases check + OkHttp download + FileProvider install
├── ui/
│   ├── theme/                        # Ocean Blue design system
│   ├── onboarding/                   # 8-step onboarding flow
│   ├── home/                         # Dashboard + update trigger
│   ├── coach/                        # CoachChatScreen + CoachViewModel
│   ├── update/                       # UpdateBottomSheet + UpdateViewModel
│   ├── foodlog/                      # Search + barcode scanner
│   ├── progress/                     # Weight chart + history
│   ├── learn/                        # Course map, lesson list, lesson reader
│   ├── tools/                        # Tools hub, recipes, meal planner
│   └── components/                   # JeanFitBottomBar (5 tabs), shared composables
├── navigation/                       # NavGraph, Screen routes, BottomNavItem (5 tabs)
├── di/                               # DatabaseModule, NetworkModule (Claude + GitHub Retrofit)
└── worker/                           # WorkManager jobs (reminders, streak check)
```

---

## Room Database Schema

14 entities across 7 DAOs (v2 migration adds Coach tables):

- **UserProfile** — singleton, stores all onboarding data + coins + onboarding flag
- **FoodItem** — cached food items from API/barcode, with color category
- **FoodLogEntry** — daily meal logs with serving multiplier
- **WeightEntry** — daily weigh-ins + trend weight (EMA)
- **Course / Lesson / LessonProgress** — 3-week structured curriculum
- **DailyTask / Streak / Achievement** — full gamification system
- **Recipe / MealPlan** — recipe book + weekly planner
- **CoachMessage** — full chat history with Jean
- **CoachMemory** — key-value long-term memory (goals, milestones, preferences)

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
| Claude Haiku | Yes (Anthropic) | KI-Coach Jean conversations |
| GitHub Releases | No | Auto-update version check |

OpenFoodFacts and USDA are only used to populate the local Room cache. The app works **100% offline** once data is cached (Coach requires internet for AI responses).

---

## Build & Run

### Requirements
- Android Studio Meerkat or newer
- Android SDK 26+
- JDK 21
- Claude API key (get one at console.anthropic.com)

### Setup
```bash
git clone https://github.com/Pcf1337-hash/JeanFit.git
cd JeanFit

# Create local.properties with your SDK path and Claude API key:
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
echo "CLAUDE_API_KEY=sk-ant-..." >> local.properties

./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

### Download pre-built APK
Get the latest signed APK from the [Releases page](https://github.com/Pcf1337-hash/JeanFit/releases/latest).

Enable "Install from unknown sources" in Android settings before installing.

---

## Design System

Ocean Blue theme (v1.1.0+):

| Token | Color | Usage |
|-------|-------|-------|
| OceanBlue | `#1565C0` | Primary CTAs, accents |
| SkyBlue | `#42A5F5` | Coach UI, highlights |
| DeepNavy | `#0D2B4E` | Headlines, dark containers |
| MidnightBlue | `#0A1929` | Dark backgrounds |
| CoachCardDark | `#122136` | Coach message bubbles |
| FoodGreen | `#4CAF50` | Green food category |
| FoodYellow | `#FFC107` | Yellow food category |
| FoodOrange | `#FF6B35` | Orange food category |

Dark mode is fully supported on every screen.

---

## Auto-Update (for maintainers)

To publish a new version:

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`
2. Build: `./gradlew assembleRelease`
3. Create a GitHub release tagged `vX.Y.Z` with the APK as an asset
4. Include release notes — the app will show them in the update modal

To force an update (users cannot skip):
```
Include this line anywhere in the release notes:
FORCED_UPDATE: true
```

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
