# tasks/todo.md — NutriMind: Noom-Klon Implementierungsplan

> **Workflow**: Plan → Verify → Implement → Test → Mark Done
> Kein Task als `[x]` markieren ohne bewiesene Funktionsfähigkeit.

---

## Phase UI-Overhaul (v2.0.0 — Ocean Blue Update)

**Ziel:** Komplettes UI/UX Redesign + Emulator-Test + GitHub Release

### Schritt 1: Fundament (Design-System)
- [ ] **U1.1** `ui/theme/Gradients.kt` erstellen (heroGradient, cardGradient, progressRing, foodGreen, coin)
- [ ] **U1.2** `ui/components/JeanFitCard.kt` — Gradient/Surface Card mit border + fadeIn Animation
- [ ] **U1.3** `ui/components/GradientButton.kt` — OceanBlue→TealAccent, scale-on-press
- [ ] **U1.4** `ui/components/ColorCategoryBadge.kt` — Ampel-Badge grün/gelb/orange
- [ ] **U1.5** `ui/components/CalorieRingProgress.kt` — Canvas Gradient-Ring mit Animation
- [ ] **U1.6** `ui/components/ShimmerCard.kt` — Skeleton Shimmer für Loading-States
- [ ] **U1.7** `JeanFitBottomBar` rebuild — Floating Pill, Glassmorphism, Pill-Indicator

### Schritt 2: HomeScreen
- [ ] **U2.1** heroGradient Header (200dp) mit Greeting + Coins
- [ ] **U2.2** CalorieRingCard mit neuem CalorieRingProgress Composable
- [ ] **U2.3** MealSlots mit LinearProgressIndicator Farbbalken
- [ ] **U2.4** DailyTasksCard mit animateColorAsState Checkboxen
- [ ] **U2.5** StreakBanner (FoodOrange Gradient, wenn Streak ≥ 3)

### Schritt 3: Onboarding Screens
- [ ] **U3.1** Gradient-Header + Sheet-from-bottom Transition auf allen Screens
- [ ] **U3.2** Fortschrittsleiste oben animiert
- [ ] **U3.3** GradientButton für "Weiter"
- [ ] **U3.4** WelcomeScreen: Pulse-Animation + Tagline staggered fadeIn
- [ ] **U3.5** CalorieCalculationScreen: animierter Counter

### Schritt 4: Progress Screen
- [ ] **U4.1** Hero-Card mit großen Zahlen (Roboto Mono)
- [ ] **U4.2** Vico Chart: DarkSurface Hintergrund, TealAccent Trendlinie
- [ ] **U4.3** Zeitraum-Chips (30T/90T/Alle) in SkyBlue

### Schritt 5: Learn Screens
- [ ] **U5.1** CourseMap: Journey-Visualisierung, Nodes mit Glow/Häkchen/Schloss
- [ ] **U5.2** LessonReader: Gradient Header, TipBlock OceanBlue-Balken, Lese-Fortschrittsbalken

### Schritt 6: Food Screens
- [ ] **U6.1** FoodSearch: OceanBlue Suchfeld-Focus, Skeleton Loading, Barcode FAB Pulse
- [ ] **U6.2** BarcodeScanner: Nur-Ecken Rahmen, TealAccent Scan-Linie Animation
- [ ] **U6.3** RecipeList: LazyVerticalGrid 2-Spalten, Kategorie-Chips, Zufalls-Rezept-Card

### Schritt 7: Coach Screen
- [ ] **U7.1** Gradient Header mit Jean Avatar + Online-Dot
- [ ] **U7.2** Typing-Indicator (3 Punkte, gestaffelter Bounce)
- [ ] **U7.3** Send-FAB Scale-Animation

### Schritt 8: Micro-Interactions
- [ ] **U8.1** Haptic Feedback bei Coin-Gewinn und Slider
- [ ] **U8.2** Navigation Transitions: fadeIn + slideInHorizontally
- [ ] **U8.3** Achievement-Dialog: spring() Einblenden

### Phase 2: Emulator-Test
- [ ] **E1** `./gradlew assembleDebug` — 0 Errors
- [ ] **E2** APK auf Emulator installieren, alle Screens prüfen
- [ ] **E3** Dark Mode Test via ADB
- [ ] **E4** Bugs sofort fixen

### Phase 3: Release
- [ ] **R1** versionCode = 4, versionName = "2.0.0"
- [ ] **R2** `./gradlew assembleRelease`
- [ ] **R3** GitHub Release `v2.0.0` mit APK erstellen

---

## Status-Legende
- `[ ]` — Offen
- `[~]` — In Bearbeitung
- `[x]` — Fertig & verifiziert
- `[!]` — Blockiert / Problem

---

## Phase 0: Projekt-Setup & Fundament
**Ziel:** Kompilierbares Grundgerüst mit Theme, Navigation und Room

- [ ] **0.1** Android-Projekt erstellen (Compose Activity, minSdk 26, targetSdk 36, Kotlin 2.1)
- [ ] **0.2** `libs.versions.toml` mit allen Libraries aus CLAUDE.md befüllen
- [ ] **0.3** KSP-Plugin konfigurieren (Room + Hilt)
- [ ] **0.4** `build.gradle.kts` (app): alle Abhängigkeiten eintragen
- [ ] **0.5** `NutriMindTheme.kt` erstellen (Color.kt, Typography.kt, Shape.kt)
  - Custom ColorScheme Dark + Light mit Noom-Farben (#FB513B, #F6F4EE, #1D3A44)
  - `dynamicColor = false` erzwingen
- [ ] **0.6** `NutriMindDatabase.kt` mit Room erstellen
  - Alle Entities registrieren (Version 1, keine Migration nötig initial)
  - `createFromAsset("nutrimind.db")` für Pre-populated Food DB vorbereiten
- [ ] **0.7** Hilt `@HiltAndroidApp` in Application-Klasse
- [ ] **0.8** Hilt Module erstellen: `DatabaseModule`, `NetworkModule`, `RepositoryModule`
- [ ] **0.9** `MainActivity` mit `NutriMindTheme`, `NavHost` und `onboardingCompleted`-Check
- [ ] **0.10** Bottom Navigation Bar (4 Tabs: Home, Fortschritt, Tools, Lernen)
- [ ] **0.11** Build-Verifikation: `./gradlew assembleDebug` → 0 Errors

**✅ Phase 0 Done wenn:** App startet, Bottom Nav ist sichtbar, Theme wird angewendet.

---

## Phase 1: Onboarding-Flow
**Ziel:** Vollständiger 10-Screen Onboarding-Wizard mit State-Machine

- [ ] **1.1** `OnboardingState` Data Class (alle Felder aus CLAUDE.md Schema)
- [ ] **1.2** `OnboardingViewModel` (Hilt) mit `StateFlow<OnboardingState>`
- [ ] **1.3** NavGraph für Onboarding: 10 Screens sequenziell

**Screens:**
- [ ] **1.4** `WelcomeScreen` — Logo, "Starte deine Reise", weiter-Button (animiert)
- [ ] **1.5** `GoalWeightScreen` — Zielgewicht-Eingabe (Ruler-Slider, kg/lbs)
- [ ] **1.6** `CurrentWeightScreen` — Aktuelles Gewicht + positives Feedback-Overlay
- [ ] **1.7** `GenderAgeHeightScreen` — 3 Felder auf einem Screen
- [ ] **1.8** `ActivityLevelScreen` — 4 Karten mit Icons (sitzend → sehr aktiv)
- [ ] **1.9** `HealthConditionsScreen` — MultiSelect-Chips (Diabetes, Bluthochdruck etc.)
- [ ] **1.10** `MotivationScreen` — Was motiviert dich? (Hochzeit, Urlaub, Gesundheit...)
- [ ] **1.11** `CalorieCalculationScreen` — Harris-Benedict Ergebnis, animierter Counter
  - UseCase: `CalculateDailyCaloriesUseCase` mit Harris-Benedict-Formel
  - "Dein Tagesziel: **1.847 kcal**" mit Untertext-Erklärung
- [ ] **1.12** `SocialProofScreen` — Statistic-Cards, Testimonials, "3,6M Nutzer"
- [ ] **1.13** `ProfileCreatedScreen` — Konfetti-Animation (Lottie), "Dein Profil ist fertig!"
- [ ] **1.14** DataStore schreiben: `onboardingCompleted = true`, UserProfile in Room speichern
- [ ] **1.15** Progress-Indikator (Schritte oben, z.B. "Schritt 3 von 10")
- [ ] **1.16** Back-Navigation im Onboarding (vorherige Antworten beibehalten)

**✅ Phase 1 Done wenn:** Onboarding komplett durchläuft, Profil in Room gespeichert, App startet danach direkt bei Home.

---

## Phase 2: Kalorientracker (Food Logging)
**Ziel:** Vollständiger Mahlzeiten-Log mit Suche, Barcode und Ampelsystem

### 2A: Datenbank & Repository
- [ ] **2.1** `FoodItemDao` mit FTS4 für Textsuche, CRUD, Barcode-Lookup
- [ ] **2.2** `FoodLogEntryDao` mit Queries für: heute geloggte Kalorien, nach Datum, nach Mahlzeit
- [ ] **2.3** `FoodRepository` — NetworkBoundResource-Pattern:
  - Erst Room-Cache → dann OpenFoodFacts API → Cache updaten
- [ ] **2.4** OpenFoodFacts Retrofit-Interface + Response-Models
- [ ] **2.5** USDA API Retrofit-Interface + Response-Models
- [ ] **2.6** Pre-populated DB mit ~500 häufigen deutschen Lebensmitteln (JSON → Room)
- [ ] **2.7** `ColorCategoryCalculator` UseCase (Kaloriendichte-Formel aus CLAUDE.md)

### 2B: Food-Log Screen
- [ ] **2.8** `FoodLogScreen` — Tagesübersicht mit 4 Mahlzeiten-Sektionen
  - Kalorienzähler oben: "847 / 1847 kcal" mit Farbbalken (Grün/Gelb/Orange Anteile)
  - Makro-Zeile (optional: Protein/Carbs/Fat als kleine Chips)
- [ ] **2.9** Mahlzeiten-Sektion Composable — Expandierbar, Einträge mit Swipe-to-Delete
- [ ] **2.10** FAB "Mahlzeit hinzufügen" → öffnet FoodSearchScreen als BottomSheet

### 2C: Lebensmittel-Suche
- [ ] **2.11** `FoodSearchScreen` — Suchfeld mit Debounce (300ms), Live-Ergebnisse
  - Room FTS4 für Offline-Suche
  - Remote-Fallback bei <3 lokalen Treffern
- [ ] **2.12** Suchergebnisse-Liste: Name, Marke, Kalorien/100g, Farb-Chip (🟢🟡🟠)
- [ ] **2.13** Zuletzt verwendet Section (Room Query: letzte 5 einzigartigen FoodIds)
- [ ] **2.14** `FoodDetailBottomSheet` — Menge/Portionen-Eingabe
  - Slider oder NumericInput für Gramm
  - Live-Kalkulation: "237 kcal für 150g"
  - Makro-Aufschlüsselung
  - "Hinzufügen"-Button → FoodLogEntry in Room speichern

### 2D: Barcode-Scanner
- [ ] **2.15** `BarcodeScannerScreen` — CameraX PreviewView in Compose
- [ ] **2.16** ML Kit Barcode Analyzer: `ImageAnalysis.Analyzer`
- [ ] **2.17** Scan-Overlay: animierte Suchleiste, Rahmen, Anleitung-Text
- [ ] **2.18** Nach Scan: → Room prüfen → OpenFoodFacts API → FoodDetailBottomSheet öffnen
- [ ] **2.19** Fehlerfall: "Produkt nicht gefunden" + Option für manuelle Eingabe
- [ ] **2.20** Camera Permission Handling mit `rememberLauncherForActivityResult`

### 2E: Custom Lebensmittel
- [ ] **2.21** `CreateCustomFoodScreen` — Formular: Name, Kalorien/100g, Protein, Carbs, Fett
  - Live-Vorschau der Farbkategorie beim Eingeben
  - Speichert mit `source = "custom"` in Room

### 2F: "Tag abschließen"
- [ ] **2.22** Button am Ende des FoodLogScreens
- [ ] **2.23** Tagesabschluss-Dialog: Zusammenfassung (Kalorien, Aufschlüsselung), Glückwunsch
- [ ] **2.24** `DailyTask.allMealsLogged = true` setzen → Coin-Check-Logic triggern

**✅ Phase 2 Done wenn:** Mahlzeiten loggen, suchen, scannen, bearbeiten, löschen funktioniert. Kalorien-Tally korrekt.

---

## Phase 3: Gewichts-Tracker & Charts
**Ziel:** Tägliche Gewichtseingabe mit interaktivem Vico-Diagramm und Trendlinie

- [ ] **3.1** `WeightEntryDao` — CRUD, letzte 90 Tage, letzter Eintrag, Trend-Berechnung
- [ ] **3.2** `WeightRepository` mit Trendlinien-Berechnung (EMA, λ=0.1)
- [ ] **3.3** `WeightEntryViewModel` — StateFlow für Chart-Daten
- [ ] **3.4** `QuickWeightDialog` — Ruler-Slider Composable
  - Horizontaler Scroll-Slider (kg/lbs Toggle)
  - Live-Feedback: "Du bist 0,8 kg von deinem Ziel entfernt"
  - Speichert WeightEntry + aktualisiert Trendlinie
- [ ] **3.5** `ProgressScreen` — oberer Bereich
  - Aktuellesgewicht | Startgewicht | Differenz (mit Trend-Pfeil ↓)
  - BMI-Anzeige (berechnet aus Größe + aktuellem Gewicht)
  - Zielgewicht mit Fortschrittsbalken

- [ ] **3.6** Vico Line Chart für Gewichtsverlauf:
  - Graue Linie: tatsächliche tägliche Werte
  - Dicke grüne Trendlinie: EMA
  - X-Achse: Datumsangaben (dd.MM)
  - Y-Achse: Gewicht in kg
  - Touch-Interaktion: Tap zeigt Tooltip mit Datum + Wert
  - Long-Press ermöglicht Bearbeiten/Löschen
  - Zeitraum-Toggle: 30 Tage / 90 Tage / Gesamt

- [ ] **3.7** Weitere Metriken-Cards (scrollbar):
  - Wasser getrunken (ml, + / - Buttons)
  - Schritte (Google Health Connect Integration)
  - Stimmung (5-Emoji-Skala)
- [ ] **3.8** `WeightHistoryScreen` — vollständige Liste aller Einträge (LazyColumn)
- [ ] **3.9** Gewichts-Ziel Visualisierung: "Noch 8 kg bis zum Ziel"
- [ ] **3.10** Health Connect Permission Handling + Schrittdaten lesen

**✅ Phase 3 Done wenn:** Gewicht eingeben, Chart zeigt beide Linien, Trendlinie korrekt, Touch-Interaktion funktioniert.

---

## Phase 4: Lektionssystem (Coaching)
**Ziel:** Vollständiges CBT-basiertes Kurs-System mit interaktiven Lektionen

### 4A: Datenbank & Inhalte
- [ ] **4.1** `CourseDao`, `LessonDao`, `LessonProgressDao`
- [ ] **4.2** `LessonRepository` — Fortschrittslogik, sequenzielle Freischaltung
- [ ] **4.3** Content-JSON für alle Lektionen erstellen (siehe lessons.md)
  - 7 Kurse × ~14 Lektionen = ~98 Lektionen
  - Jede Lektion als JSON-Datei in `assets/lessons/`
- [ ] **4.4** `LessonContentParser` — JSON → `List<ContentBlock>` (sealed class)
- [ ] **4.5** DB-Seeding: Alle Kurse + Lektionen beim ersten App-Start einfügen

### 4B: UI-Screens
- [ ] **4.6** `CourseMapScreen` — horizontale oder vertikale Progress Map
  - Kurs-Karten mit Titel, Emoji, Fortschritt (z.B. "4/14")
  - Gesperrte Kurse: grau + Schloss-Icon
  - Fortschritt-Balken pro Kurs
- [ ] **4.7** `LessonListScreen` — Lektionen eines Kurses als LazyColumn
  - Abgeschlossene: Häkchen + grün
  - Aktuelle: hervorgehoben
  - Gesperrte: ausgegraut
- [ ] **4.8** `LessonReaderScreen` — der Kern-Screen
  - LazyColumn mit Content-Blöcken
  - Fortschritts-Indicator oben (% gelesen)
  - "Weiter"-Button unten → nächste Sektion oder Abschluss

### 4C: Content-Block Composables
- [ ] **4.9** `TextBlock` — Markdown-Rendering mit mikepenz-Library
- [ ] **4.10** `TipBlock` — Karte mit Glühbirnen-Icon, Akzentfarbe
- [ ] **4.11** `QuizBlock` — Multiple Choice mit:
  - Antwort-Buttons (RadioButton-Style)
  - Nach Auswahl: grün/rot Feedback + Erklärung
  - Kein Weiter ohne Antwort
- [ ] **4.12** `ActivityBlock` — CTA-Karte mit Deep-Link-Button (z.B. "Jetzt Mahlzeit loggen")
- [ ] **4.13** `ReflectionBlock` — TextField für Notizen (lokal gespeichert)
- [ ] **4.14** `ImageBlock` — Bild mit Caption (drawable-Ressourcen)

### 4D: Abschluss-Logic
- [ ] **4.15** Lektion abschließen: `LessonProgress.isCompleted = true`
- [ ] **4.16** Confetti-Animation (Lottie) beim Lektions-Abschluss
- [ ] **4.17** "Nächste Lektion"-Button direkt nach Abschluss
- [ ] **4.18** Kurs-Abschluss-Screen bei letzter Lektion im Kurs

**✅ Phase 4 Done wenn:** Alle 7 Kurse mit Lektionen lesbar, Quiz funktioniert, Fortschritt wird korrekt gespeichert.

---

## Phase 5: Gamification
**Ziel:** Noomcoins, Streaks, Achievements mit Animationen

- [ ] **5.1** `GamificationRepository` — Coin-Logik, Streak-Updates, Achievement-Checks
- [ ] **5.2** `DailyTaskChecker` — prüft täglich ob alle 3 Tasks abgeschlossen → Coin vergeben
  - Trigger: nach Mahlzeiten-Abschluss, nach Lektions-Abschluss, nach Gewichtseintrag
- [ ] **5.3** Streak-Update-Logic:
  - `lastActivityEpochDay == today - 1` → Streak++
  - `lastActivityEpochDay < today - 1` → Streak = 1
  - Bei Mitternacht via WorkManager prüfen
- [ ] **5.4** `CoinDisplayWidget` Composable — Münz-Icon + Zahl (in AppBar oder Home)
- [ ] **5.5** `CoinEarned` Animation — kleine Münze fliegt von Position zur AppBar (Compose Animation)
- [ ] **5.6** **Noomcoin-System im HomeScreen:**
  - Tageszustand: "🔴 2/3 Aufgaben erledigt" oder "🟢 Coin verdient!"
  - Aufgaben-Checklist: Gewicht ✓ | Mahlzeiten ✓ | Lektion ○
- [ ] **5.7** **Treat Day Feature:**
  - Freigeschaltet bei 5 Coins
  - Toggle in Einstellungen: Treat Day für heute aktivieren
  - Kalorienziel +20%, keine Warnungen
- [ ] **5.8** **Achievements-System (15 Achievements):**
  - [ ] "Erster Schritt" — erste Mahlzeit geloggt
  - [ ] "Scanner" — ersten Barcode gescannt
  - [ ] "Logging-Profi" — 30 Mahlzeiten geloggt
  - [ ] "Eiserne Gewohnheit" — 7 Tage Logging-Streak
  - [ ] "Unbezwingbar" — 30 Tage Logging-Streak
  - [ ] "Waagenprofi" — 7 Tage Gewicht-Streak
  - [ ] "Wissenssammler" — 7 Tage Lektions-Streak
  - [ ] "Kursabsolvent" — ersten Kurs abgeschlossen
  - [ ] "Wissenshungrig" — alle Kurse abgeschlossen
  - [ ] "Erster Meilenstein" — 5 Coins gesammelt
  - [ ] "Coin-Sammler" — 30 Coins gesammelt
  - [ ] "Zielgewicht fast da" — 90% Fortschritt
  - [ ] "Ziel erreicht!" — Zielgewicht erreicht
  - [ ] "Rezeptliebhaber" — 10 Rezepte als Favorit
  - [ ] "Planungsgenie" — Wochenplaner 1 Woche gefüllt
- [ ] **5.9** `AchievementUnlockedDialog` — Lottie Konfetti + Badge-Reveal Animation
- [ ] **5.10** `AchievementsScreen` — Übersicht aller Achievements (locked/unlocked)
- [ ] **5.11** `StreakScreen` / Section im ProgressTab — Streak-Kalender Heatmap

**✅ Phase 5 Done wenn:** Coins werden korrekt vergeben, Streaks brechen und erholen sich, Achievements triggern korrekt.

---

## Phase 6: Rezepte & Meal-Planner
**Ziel:** 50+ Rezepte, Favoriten, Rezept-ins-Log, Wochenplaner

- [ ] **6.1** `RecipeDao` — CRUD, Suche, Filter by Tags, Favoriten
- [ ] **6.2** `MealPlanDao` — CRUD by Date+MealType
- [ ] **6.3** JSON-Asset mit 50+ vorinstallierten Rezepten (Deutsch, gesund)
  - Jedes Rezept mit korrekter Farbaufschlüsselung (% Grün/Gelb/Orange)
- [ ] **6.4** `RecipeListScreen` — durchsuchbare, filterbare Rezeptliste
  - Suchfeld + Tag-Filter-Chips (Vegan, Schnell, Frühstück, Abendessen...)
  - Rezept-Karte: Bild, Titel, Zeit, Farb-Balken, Kalorien
- [ ] **6.5** `RecipeDetailScreen`
  - Header: Bild, Titel, Metadaten (Zeit, Portionen, kcal)
  - Farb-Aufschlüsselung: visueller Balken mit %-Angaben
  - Zutaten-Liste mit Mengen (skalierbar)
  - Schritt-für-Schritt Anleitung
  - "Ins Mahlzeiten-Log" Button → FoodLogEntry erstellen
  - Favoriten-Heart-Button
- [ ] **6.6** `CreateCustomRecipeScreen` — Zutaten aus FoodDB hinzufügen
  - Zutat suchen → Menge eingeben → Live-Kalorien-Update
  - Schritte als sortierbare Liste
- [ ] **6.7** `MealPlannerScreen` — Wochenansicht (Kizitonwose Calendar)
  - 7-Tage-Streak oben, je Tage: Frühstück/Mittag/Abend/Snack als Slots
  - Slot antippen → Rezept suchen und zuweisen
  - Gesamtkalorien pro Tag am unteren Rand
- [ ] **6.8** "Rezept loggen" von MealPlanner aus
- [ ] **6.9** `FavoritesSection` im ToolsTab (schneller Zugriff)

**✅ Phase 6 Done wenn:** Rezepte anzeigen, favorisieren, ins Log übernehmen, Wochenplaner ausfüllen.

---

## Phase 7: Home-Dashboard
**Ziel:** Intelligenter Homescreen mit Task-Cards und Tageszusammenfassung

- [ ] **7.1** `HomeViewModel` — aggregiert alle Tages-Daten (Kalorien, Gewicht, Lektionen, Tasks)
- [ ] **7.2** Greeting-Header — "Guten Morgen, [Name] 👋", Tagesdatum
- [ ] **7.3** **Daily Progress Card** — Kalorienring / Balken:
  - Gegessen: X kcal von Y kcal
  - Farbverteilung (Grün/Gelb/Orange Segmente)
  - Wischgeste rechts → Makros
- [ ] **7.4** **Task-Cards** (priorisiert, abhängig vom Tagesstand):
  - "Gewicht für heute eintragen" → Quick-Dialog
  - "Mahlzeiten loggen" → FoodLogScreen
  - "Heutige Lektion: [Titel]" → LessonReaderScreen
  - "Tag abschließen" → Tagesabschluss-Dialog
- [ ] **7.5** **Streak-Banner** — "🔥 12 Tage Streak!" (bei Streak ≥ 3)
- [ ] **7.6** **Coin-Counter** in der AppBar
- [ ] **7.7** **Wochenziel-Progress** — "Diese Woche: -0,4 kg (Ziel: -0,5 kg)"
- [ ] **7.8** **Motivations-Quote** — täglich wechselnd (7×7 Matrix, aus assets/quotes.json)
- [ ] **7.9** Konfetti-Animation bei Tagesziel erreicht (alle Tasks erledigt)
- [ ] **7.10** Lottie "All Done" Animation (Häkchen, das sich füllt)

**✅ Phase 7 Done wenn:** Homescreen zeigt korrekten Tageszustand, alle Task-Cards navigieren korrekt.

---

## Phase 8: Notifications & Hintergrund
**Ziel:** Tägliche Erinnerungen, Streak-Schutz, Offline-Sync

- [ ] **8.1** WorkManager Job: `WeighInReminderWorker` (08:00, wenn nicht bereits gewogen)
- [ ] **8.2** WorkManager Job: `MealReminderWorker` (19:00, wenn <2 Mahlzeiten geloggt)
- [ ] **8.3** WorkManager Job: `LessonReminderWorker` (10:00, wenn Lektion noch offen)
- [ ] **8.4** WorkManager Job: `MidnightStreakWorker` (00:05, Streak-Prüfung + Reset)
- [ ] **8.5** WorkManager Job: `FoodCacheSyncWorker` (täglich, WLAN only, OpenFoodFacts Pre-Cache)
- [ ] **8.6** Notification-Channels erstellen (Android 8+)
- [ ] **8.7** POST_NOTIFICATIONS Permission Handling (Android 13+)
- [ ] **8.8** Notification-Einstellungen in Profil-Screen (ein/ausschalten pro Typ)
- [ ] **8.9** AlarmManager als Backup für kritische tägliche Reminders

**✅ Phase 8 Done wenn:** Reminders kommen zur konfigurierten Zeit, Streak-Job läuft zuverlässig.

---

## Phase 9: Profil & Einstellungen
**Ziel:** Profil bearbeiten, Ziele anpassen, App-Einstellungen

- [ ] **9.1** `ProfileScreen` — Avatar-Initialen-Circle, Name, E-Mail
- [ ] **9.2** Gewichtsziel anpassen (neues Ziel → Harris-Benedict neu berechnen)
- [ ] **9.3** Aktivitätslevel ändern
- [ ] **9.4** Einheit-Toggle (kg / lbs / Stones)
- [ ] **9.5** Notification-Einstellungen
- [ ] **9.6** Dark Mode Toggle (System / Immer Dark / Immer Light)
- [ ] **9.7** Daten-Export (CSV: Gewicht, Kalorien-Log)
- [ ] **9.8** App zurücksetzen (für Testing)
- [ ] **9.9** About-Screen (Version, Libraries, Danksagungen)

---

## Phase 10: Polish, Tests & Finale Verifikation

- [ ] **10.1** Loading-States: Skeleton-Shimmer für alle Netzwerk-Requests
- [ ] **10.2** Error-States: Snackbar + Retry für API-Fehler
- [ ] **10.3** Empty-States: Illustrationen für leere Listen
- [ ] **10.4** Alle `@Preview`s (Dark + Light) für jeden Screen
- [ ] **10.5** Unit-Tests: `CalculateDailyCaloriesUseCase`, `ColorCategoryCalculator`, Streak-Logic
- [ ] **10.6** Unit-Tests: `WeightTrendCalculator` (EMA), `CoinAwardChecker`
- [ ] **10.7** UI-Tests: Onboarding-Flow End-to-End
- [ ] **10.8** UI-Tests: Food-Log → Mahlzeit hinzufügen → Kalorien korrekt
- [ ] **10.9** Accessibility: TalkBack-Test, Content Descriptions
- [ ] **10.10** Performance: Compose Profiler, kein Re-Compose ohne State-Änderung
- [ ] **10.11** ProGuard/R8 Rules für Moshi, Room, Hilt
- [ ] **10.12** Release-Build: `./gradlew assembleRelease` → 0 Warnings

---

## Review-Sektion (wird nach Fertigstellung ausgefüllt)

### Phase 0 Review
- Ergebnis: 
- Probleme: 
- Lessons: 

### Phase 1 Review
- Ergebnis: 
- Probleme: 
- Lessons: 

_(Für jede Phase wiederholen)_

---

## Offene Entscheidungen

| ID | Entscheidung | Optionen | Status |
|----|-------------|---------|--------|
| D1 | Lottie-Dateien: selbst erstellen oder Open-Source? | LottieFiles.com (kostenlos) | Offen |
| D2 | Rezept-Bilder: eigene Assets oder Unsplash-API? | Unsplash (kein Key nötig für dev) | Offen |
| D3 | Health Connect: Min API Level? | API 26 (WorkAround nötig) | Offen |
| D4 | Onboarding: wie viele Screens für MVP? | 10 (abgespeckt) vs 15 (vollständig) | → 10 |
