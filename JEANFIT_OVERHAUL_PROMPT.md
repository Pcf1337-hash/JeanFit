# JeanFit — Vollständige UI/UX Überarbeitung, Emulator-Test & GitHub Release

> **Lies zuerst:** `CLAUDE.md`, `tasks/todo.md`, `tasks/lessons.md`, `tasks/erweiterung.md`
> Dann lies `/mnt/skills/public/frontend-design/SKILL.md` für Design-Prinzipien.
> Erst danach beginnst du mit Schritt 1.

---

## Deine Aufgabe (3 Phasen)

1. **UI/UX Komplett-Overhaul** — Ocean Blue Theme + modernes Compose-Design
2. **Emulator-Test** — Alle Screens und Funktionen live testen via ADB
3. **Release Build + GitHub Release** — Signiertes APK + automatisches In-App-Update

---

## PHASE 1: UI/UX OVERHAUL

### Schritt 1.0 — Plan aufschreiben (PFLICHT)
Schreibe einen detaillierten Plan in `tasks/todo.md` unter einer neuen Sektion
`## Phase UI-Overhaul` mit Checkboxen für jeden Sub-Task.
Kein Code bevor der Plan steht.

---

### Schritt 1.1 — Design-System: Ocean Blue Theme

Ersetze das komplette Farbsystem in `ui/theme/Color.kt` und `ui/theme/Theme.kt`:

**Neue Palette:**
```kotlin
object JeanFitColors {
    // === PRIMÄR ===
    val OceanBlue       = Color(0xFF1565C0)  // Primär: Buttons, CTAs, aktive Nav
    val SkyBlue         = Color(0xFF42A5F5)  // Akzent: Chips, Links, Highlights
    val DeepNavy        = Color(0xFF0D2B4E)  // Headlines, dunkler Text
    val IceBlue         = Color(0xFFE3F2FD)  // Background Light
    val MidnightBlue    = Color(0xFF0A1929)  // Background Dark
    val TealAccent      = Color(0xFF00BCD4)  // Sekundärer Akzent, Charts, Badges
    val PearlWhite      = Color(0xFFF8FAFF)  // Card Light
    val DarkSurface     = Color(0xFF122136)  // Card Dark
    val DarkSurface2    = Color(0xFF1A2E45)  // Elevated Card Dark

    // === AMPEL ===
    val FoodGreen       = Color(0xFF2ECC71)
    val FoodYellow      = Color(0xFFF39C12)
    val FoodOrange      = Color(0xFFE74C3C)

    // === GAMIFICATION ===
    val CoinGold        = Color(0xFFFFD700)
    val StreakFire      = Color(0xFFFF6B35)
    val SuccessGreen    = Color(0xFF00E676)
}
```

**Gradients (in `ui/theme/Gradients.kt` neue Datei):**
```kotlin
object JeanFitGradients {
    val heroGradient = Brush.verticalGradient(
        listOf(Color(0xFF1565C0), Color(0xFF0D2B4E))
    )
    val cardGradient = Brush.linearGradient(
        listOf(Color(0xFF1A2E45), Color(0xFF0D2B4E))
    )
    val progressRing = Brush.sweepGradient(
        listOf(Color(0xFF42A5F5), Color(0xFF00BCD4), Color(0xFF1565C0))
    )
    val foodGreenGradient = Brush.linearGradient(
        listOf(Color(0xFF2ECC71), Color(0xFF27AE60))
    )
    val coinGradient = Brush.linearGradient(
        listOf(Color(0xFFFFD700), Color(0xFFFFA000))
    )
}
```

**Typografie (Google Fonts):**
- Headlines/Display: **Nunito** (rund, freundlich, feminin)
- Body/Labels: **DM Sans** (modern, lesbar)
- Zahlen/Counter: **Roboto Mono** (für Kalorien, Gewicht, Coins)

```kotlin
// In libs.versions.toml:
google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts", version = "1.8.1" }
```

**Material 3 ColorScheme:**
```kotlin
private val DarkColorScheme = darkColorScheme(
    primary          = JeanFitColors.OceanBlue,
    onPrimary        = Color.White,
    primaryContainer = JeanFitColors.DeepNavy,
    secondary        = JeanFitColors.TealAccent,
    background       = JeanFitColors.MidnightBlue,
    surface          = JeanFitColors.DarkSurface,
    surfaceVariant   = JeanFitColors.DarkSurface2,
    onBackground     = Color(0xFFE8F4FD),
    onSurface        = Color(0xFFCFE8FF),
    error            = Color(0xFFFF5252)
)
private val LightColorScheme = lightColorScheme(
    primary          = JeanFitColors.OceanBlue,
    onPrimary        = Color.White,
    primaryContainer = JeanFitColors.IceBlue,
    secondary        = JeanFitColors.TealAccent,
    background       = JeanFitColors.IceBlue,
    surface          = JeanFitColors.PearlWhite,
    onBackground     = JeanFitColors.DeepNavy,
    onSurface        = JeanFitColors.DeepNavy,
)
```

---

### Schritt 1.2 — Shared Components überarbeiten (`ui/components/`)

**JeanFitBottomBar** — kompletter Rebuild:
- Floating Bottom Bar (kein klassisches NavigationBar unten)
- `shape = RoundedCornerShape(28.dp)`, Elevation mit `shadowElevation = 24.dp`
- Hintergrund: `DarkSurface` mit 95% Opazität + BlurEffect (Glassmorphism)
- Aktiver Tab: Pill-Indicator in `OceanBlue`, weiches `animateFloatAsState` beim Wechsel
- Icons: filled bei aktiv, outlined bei inaktiv
- Tab-Bezeichnungen: Nunito, 11sp

**JeanFitCard** — neues wiederverwendbares Composable:
```kotlin
@Composable
fun JeanFitCard(
    modifier: Modifier = Modifier,
    gradient: Brush? = null,           // optional Gradient-Hintergrund
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
)
// Hintergrund: gradient ODER DarkSurface
// Border: 1dp, Color(0xFF1E3A5F) für subtile Tiefe
// Smooth entry animation: fadeIn + slideInVertically
```

**GradientButton** — primärer CTA:
```kotlin
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
// Brush: linearGradient(OceanBlue → TealAccent)
// Shape: RoundedCornerShape(14.dp)
// Scale-Animation bei Press: animateFloatAsState(if pressed 0.95f else 1.0f)
// Ripple: Custom RippleTheme in SkyBlue
```

**ColorCategoryBadge** — Ampel-Badge:
```kotlin
@Composable
fun ColorCategoryBadge(category: String)
// "green" → FoodGreen Hintergrund, 🟢 Emoji + "Grün"
// "yellow" → FoodYellow, 🟡 "Gelb"
// "orange" → FoodOrange, 🟠 "Orange"
// Shape: RoundedCornerShape(6.dp), Nunito Bold 11sp
```

**CalorieRingProgress** — animierter Kalorien-Ring:
```kotlin
@Composable
fun CalorieRingProgress(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier
)
// Canvas drawArc mit progressRing Brush
// Animiertes Einblenden: animateFloatAsState(easing = FastOutSlowIn)
// Center: große Zahl in Roboto Mono, "kcal" darunter in DM Sans
// Außenring: subtiler Track in DarkSurface2
```

---

### Schritt 1.3 — Onboarding überarbeiten (`ui/onboarding/`)

Jeder Screen bekommt:
- **Blauer Gradient-Header** (`heroGradient`), Höhe ~220dp
- **Weißes "Sheet"** das von unten hochkommt (`AnimatedVisibility` + `slideInVertically`)
- **Fortschrittsleiste** oben: schmale Line, OceanBlue gefüllt, animiert
- **GradientButton** für "Weiter"
- **Smooth Transitions**: `fadeIn() + slideInHorizontally()` zwischen Screens

**WelcomeScreen** spezifisch:
- Großes JeanFit-Logo in der Mitte des Gradient-Headers
- Subtitle: "Dein persönlicher Coach" in Nunito Light, IceBlue
- Pulsierende Wellen-Animation unter dem Logo (Canvas, repeat)
- Tagline mit staggered fadeIn (Buchstabe für Buchstabe oder Wort für Wort)

**CalorieCalculationScreen** spezifisch:
- Animierter Counter der von 0 auf den Kalorienwertt zählt (2 Sekunden)
- Darunter: 3 Karten (Protein / Carbs / Fett) mit Gradient-Badges
- "Das ist dein persönliches Ziel" in Nunito Italic

---

### Schritt 1.4 — HomeScreen komplett neu (`ui/home/HomeScreen.kt`)

**Layout:**
```
┌─────────────────────────────────────┐
│  [GRADIENT HEADER 200dp]            │
│  "Guten Morgen, [Name] 👋"  Nunito  │
│  [Datum]           [Coins: 🟡 12]   │
└─────────────────────────────────────┘
│  [CALORIE RING CARD]                │
│   Großer Ring, Consumed/Goal        │
│   Wische → Makros (animiert)        │
└─────────────────────────────────────┘
│  [DAILY TASKS CARD]                 │
│   ✓ Gewicht  ✓ Mahlzeiten  ○ Lektion│
│   [Coin-Badge bei Abschluss]        │
└─────────────────────────────────────┘
│  [4 MAHLZEIT-SLOTS]                 │
│   Frühstück | Mittagessen           │
│   Abendessen | Snack                │
│   Je: Kalorien + Farb-Balken        │
└─────────────────────────────────────┘
│  [STREAK-BANNER] wenn ≥3 Tage       │
│  🔥 12 Tage Streak — weiter so!     │
└─────────────────────────────────────┘
```

**Details:**
- Header: `heroGradient` Background, weißer Text, großzügiges Padding
- Coin-Counter in AppBar: animierter Zähler bei Coin-Gewinn (CountUp-Animation)
- CalorieRingCard: Vico-Ring oder Canvas, Gradient-Ring, Wisch-Geste für Makros
- DailyTaskCard: Checkboxen mit `animateColorAsState` (grau → OceanBlue bei Erledigung)
- Mahlzeit-Slots: je `JeanFitCard`, Icons (🌅🌞🌙🍎), Farbbalken als `LinearProgressIndicator`
- StreakBanner: `FoodOrange` Gradient, Flammen-Lottie-Animation

---

### Schritt 1.5 — ProgressScreen (`ui/progress/ProgressScreen.kt`)

**Layout:**
- Hero-Card oben: Aktuell / Start / Ziel mit großen Zahlen (Roboto Mono Bold)
- Fortschrittsbalken: Animated `LinearProgressIndicator`, `OceanBlue → TealAccent` Gradient
- **Vico Weight Chart:**
  - Hintergrund: `DarkSurface`, kein weißer Hintergrund
  - Trendlinie: `TealAccent`, 3dp dick, abgerundet
  - Datenpunkte: `OceanBlue` gefüllte Kreise
  - Grid-Linien: `DarkSurface2`, sehr subtil
  - Zeitraum-Tabs: "30T / 90T / Alle" als Chips in `SkyBlue`
- Metric-Cards darunter (Wasser, Schritte, Stimmung): 2-Spalten-Grid

---

### Schritt 1.6 — LearnScreen (`ui/learn/`)

**CourseMapScreen:**
- Vertikale "Journey"-Visualisierung statt Liste
- Verbindungslinie zwischen Kursen (gestrichelt = gesperrt, voll = verfügbar)
- Kurs-Node: Kreis mit Emoji, `OceanBlue` Glow bei aktivem Kurs
- Abgeschlossen: `SuccessGreen` Kreis + Häkchen
- Gesperrt: `DarkSurface2` + Schloss-Icon

**LessonReaderScreen:**
- Header: Gradient-Banner mit Kurs-Emoji (groß) und Lektionstitel
- Content-Blöcke mit sauberem Spacing:
  - Text: DM Sans 16sp, lineHeight 26sp, `onSurface` Farbe
  - TipBlock: Linker `OceanBlue`-Balken (4dp), `DarkSurface2` Hintergrund
  - QuizBlock: Antworten als Cards, `OceanBlue`-Border bei Auswahl, grün/rot nach Antwort
  - ReflectionBlock: `TextField` mit `OceanBlue` Fokus-Indicator
- Lese-Fortschrittsbalken oben (dünn, `OceanBlue`)
- "Lektion abschließen" Button: `GradientButton` am Ende

---

### Schritt 1.7 — ToolsScreen + Food-Screens (`ui/tools/`, `ui/foodlog/`)

**FoodSearchScreen:**
- Großes Suchfeld oben: `OceanBlue`-Border bei Fokus, Lupe-Icon
- Suchergebnis-Cards: horizontal mit `ColorCategoryBadge` rechts
- Skeleton-Loading beim API-Call (shimmer in `DarkSurface2 → DarkSurface`)
- Barcode-Scanner FAB: `OceanBlue` FAB mit Scanner-Icon, Pulse-Animation

**BarcodeScannerScreen:**
- Kamera-Overlay: dunkel abgedunkelter Hintergrund
- Scan-Rahmen: `OceanBlue` Ecken (nur Ecken sichtbar, modernes Design)
- Animierte Scan-Linie die auf- und ab fährt (`TealAccent`, 2dp)
- "Oder manuell eingeben" TextButton unten in `SkyBlue`

**RecipeListScreen (TheMealDB):**
- `LazyVerticalGrid` (2 Spalten)
- Rezept-Card: Bild oben (Coil, crossfade 300ms), Titel + Kategorie-Chip unten
- Kategorie-Filter-Chips oben (horizontal scrollbar), aktiv = `OceanBlue` Chip
- "🎲 Zufälliges Rezept" Floating Card oben: `cardGradient` Background
- Favoriten-Herz: `OceanBlue` gefüllt / outline

---

### Schritt 1.8 — CoachChatScreen (`ui/coach/CoachChatScreen.kt`)

**Layout (Claude API):**
```
┌─────────────────────────────────────┐
│ [GRADIENT HEADER]                   │
│  🤖 Jean  "Dein persönlicher Coach" │
│  [Online-Dot ● grün]                │
└─────────────────────────────────────┘
│ [CHAT MESSAGES - LazyColumn reverse]│
│                                     │
│  [Coach-Bubble] links:              │
│  ├ Avatar: OceanBlue Kreis + 🤖     │
│  └ Bubble: DarkSurface2, 20dp radius│
│                                     │
│  [User-Bubble] rechts:              │
│  └ Bubble: OceanBlue Gradient       │
│                                     │
│  [Typing Indicator: ● ● ●]          │
└─────────────────────────────────────┘
│ [QUICK REPLY CHIPS - horizontal]    │
│ [💧 Hunger] [😔 Demotiviert] ...    │
└─────────────────────────────────────┘
│ [TEXT INPUT ROW]                    │
│ [TextField] [Send-FAB: OceanBlue]   │
└─────────────────────────────────────┘
```

**Animationen:**
- Neue Nachrichten: `slideInVertically + fadeIn`
- Typing-Indicator: 3 Punkte mit `animateFloatAsState` (Bounce, gestaffelt)
- Send-Button: Scale-Animation beim Tippen (leer = 0.85f, Text vorhanden = 1.0f)

---

### Schritt 1.9 — Micro-Interactions & Polish

Implementiere in der gesamten App:

1. **Haptic Feedback** bei wichtigen Aktionen:
```kotlin
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress) // bei Coin-Gewinn
haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // bei Slider
```

2. **Smooth Navigation Transitions:**
```kotlin
// In NavGraph: alle Screens mit crossfade-Transition
enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it / 4 } }
exitTransition  = { fadeOut(tween(200)) + slideOutHorizontally { -it / 4 } }
```

3. **Achievement-Dialog Überarbeitung:**
- Lottie Konfetti (blau/gold Farben)
- Achievement-Badge springt mit `spring(dampingRatio = 0.5f)` ein
- Background-Blur (Scrim `DarkSurface` mit 80% Opazität)

4. **Loading States** — Skeleton Shimmer für alle Listen:
```kotlin
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    // animierter Shimmer: DarkSurface → DarkSurface2 → DarkSurface
    // mittels infiniteTransition + animateFloat
}
```

5. **Empty States** — illustriert:
- Noch keine Mahlzeiten: 🍽️ großes Emoji + "Logge deine erste Mahlzeit"
- Noch kein Gewicht: ⚖️ + "Starte dein Tracking"
- Keine Rezepte geladen: 👨‍🍳 + "Lade Rezepte..."

---

## PHASE 2: EMULATOR-TEST

### Schritt 2.1 — App auf Emulator deployen
```bash
# Debug-Build bauen
./gradlew assembleDebug

# Emulator prüfen
adb devices

# APK installieren
adb install -r app/build/outputs/apk/debug/app-arm64-v8a-debug.apk

# App starten
adb shell am start -n com.jeanfit.app/.MainActivity
```

### Schritt 2.2 — Alle Screens systematisch testen

Führe für jeden Screen aus:
1. Screenshot via `adb_toolkit__take-screenshot` oder `mobile-mcp__mobile_take_screenshot`
2. UI-Hierarchy via `android-toolkit__dump-ui-hierarchy`
3. Logcat auf Errors prüfen: `adb logcat -s JeanFit:E AndroidRuntime:E`

**Test-Checkliste (jeden Punkt abhaken):**

```
ONBOARDING:
[ ] WelcomeScreen — Gradient korrekt, Animation läuft
[ ] GoalWeightScreen — Slider funktioniert, Wert wird gespeichert
[ ] CurrentWeightScreen — Positives Feedback erscheint
[ ] GenderAgeHeightScreen — Alle 3 Felder speicherbar
[ ] ActivityLevelScreen — 4 Karten selektierbar
[ ] CalorieCalculationScreen — Counter-Animation, Wert korrekt
[ ] ProfileCreatedScreen — Konfetti läuft

HOME:
[ ] Greeting korrekt (Tageszeit-basiert)
[ ] CalorieRing zeigt korrekte Werte
[ ] 4 Mahlzeit-Slots vorhanden
[ ] DailyTask-Checkboxen reagieren
[ ] Coin-Counter sichtbar
[ ] StreakBanner (wenn Streak > 0)

FOOD LOGGING:
[ ] Suchfeld öffnet FoodSearchScreen
[ ] Textsuche liefert Ergebnisse
[ ] Barcode-Scanner öffnet Kamera (Permission-Dialog)
[ ] Lebensmittel auswählen → FoodDetailSheet
[ ] Serving-Multiplier änderbar
[ ] "Hinzufügen" speichert in Room
[ ] Kalorien-Tally aktualisiert sich
[ ] Farb-Badge korrekt (grün/gelb/orange)

PROGRESS:
[ ] Gewicht-Dialog öffnet (Ruler-Slider)
[ ] Gewicht speichern → Chart aktualisiert
[ ] Vico-Chart zeigt beide Linien
[ ] Zeitraum-Toggle (30T/90T/Alle) funktioniert
[ ] Fortschrittsbalken korrekt

LEARN:
[ ] CourseMap zeigt alle Kurse
[ ] Gesperrte Kurse grau
[ ] Lektion öffnet LessonReader
[ ] Alle Block-Typen rendern korrekt
[ ] Quiz-Antwort gibt Feedback
[ ] Lektion abschließen → Coin vergeben

TOOLS:
[ ] TheMealDB Rezepte laden (Netzwerk nötig)
[ ] Rezept-Detail öffnet
[ ] Favorit-Herz togglet
[ ] MealPlanner-Kalender sichtbar

COACH:
[ ] CoachChatScreen öffnet
[ ] Quick-Reply-Chips tippbar
[ ] Nachricht abschicken → Claude API antwortet
[ ] Typing-Indicator erscheint während Laden
[ ] Chat-History scrollbar

GAMIFICATION:
[ ] Alle 3 Daily Tasks → Coin vergeben (testen!)
[ ] Achievement "Erster Schritt" triggert bei erster Mahlzeit
[ ] Streak-Counter zählt korrekt
```

### Schritt 2.3 — Gefundene Bugs sofort fixen
- Bug → Root Cause identifizieren → Fix → Sofort neu bauen → Neu testen
- Jeden Fix in `tasks/lessons.md` dokumentieren
- KEIN Bug darf offen bleiben

### Schritt 2.4 — Dark Mode Test
```bash
# Dark Mode einschalten via ADB
adb shell cmd uimode night yes
# Screenshot machen
# Light Mode wieder an
adb shell cmd uimode night no
```
Alle Screens müssen in beiden Modi korrekt aussehen — kein hartkodierter Weiß-Text auf dunklem Hintergrund.

---

## PHASE 3: RELEASE BUILD & GITHUB RELEASE

### Schritt 3.1 — Version-Code erhöhen
In `app/build.gradle.kts`:
```kotlin
versionCode = <aktueller Code + 1>
versionName = "<neue Versionsnummer>"  // z.B. "2.0.0"
```

### Schritt 3.2 — Release-Build bauen
```bash
# Release APK (arm64 only für kleinere Größe)
./gradlew assembleRelease

# Prüfen ob APK existiert
ls -la app/build/outputs/apk/release/
```

### Schritt 3.3 — APK auf Emulator testen (Release)
```bash
# Release APK installieren (ersetzt Debug)
adb install -r app/build/outputs/apk/release/app-arm64-v8a-release.apk

# Kurz-Smoke-Test: App startet, kein Crash
adb shell am start -n com.jeanfit.app/.MainActivity
adb logcat -s AndroidRuntime:E -d | head -20
```

### Schritt 3.4 — GitHub Release erstellen

**APK kopieren mit Versionsname:**
```bash
cp app/build/outputs/apk/release/app-arm64-v8a-release.apk \
   jeanfit-v<versionName>.apk
```

**Release Notes schreiben** — erstelle `release_notes.md`:
```markdown
## JeanFit v<Version> — Ocean Blue Update

### 🎨 Neues Design
- Komplett neues Ocean Blue Theme
- Modernes Glassmorphism Bottom Navigation
- Gradient-Animationen in allen Screens
- Nunito + DM Sans Typografie

### ✨ Neue Features
- 🤖 KI-Coach "Jean" (Claude API) — echter AI-Chat
- 🍽️ TheMealDB Rezeptdatenbank — 300+ echte Rezepte
- 👟 Schrittzähler mit Health Connect

### 🐛 Bug Fixes
- [Liste der gefixten Bugs aus Phase 2]

### 📱 Technisch
- Minimum Android 8.0 (API 26)
- arm64-v8a optimiert
```

**GitHub Release via gh CLI:**
```bash
gh release create v<versionName> \
  jeanfit-v<versionName>.apk \
  --title "JeanFit v<versionName> — Ocean Blue Update" \
  --notes-file release_notes.md \
  --latest
```

**Verifikation — In-App-Update prüfen:**
```bash
# Prüfen ob Release auf GitHub sichtbar ist
gh release view v<versionName>

# Update-Check in App triggern (ADB Intent)
adb shell am broadcast -a com.jeanfit.app.CHECK_UPDATE
```

---

## Kritische Regeln (NIEMALS brechen)

1. **Keine Hardcoded API-Keys** — `BuildConfig.CLAUDE_API_KEY` aus `local.properties`
2. **Kein Code ohne Test** — jede Änderung sofort auf Emulator prüfen
3. **Keine weißen Screens** — jeder Screen hat Loading + Error + Empty State
4. **Dark Mode immer** — `dynamicColor = false` in Theme
5. **Vor Release Build**: `./gradlew lint` — keine kritischen Warnings
6. **Room Migration** — wenn Entities geändert: `version++` + Migration
7. **Compilierung nach jedem Schritt**: `./gradlew assembleDebug` → 0 Errors
8. **Lessons updaten** — nach jedem gefixten Bug in `tasks/lessons.md` eintragen

---

## Erwartetes Endergebnis

Nach Abschluss aller 3 Phasen:
- ✅ App startet ohne Crash
- ✅ Alle Screens im neuen Ocean Blue Design
- ✅ Dark Mode korrekt auf allen Screens
- ✅ KI-Coach antwortet via Claude API
- ✅ TheMealDB Rezepte laden und werden gecacht
- ✅ Alle Daily Tasks/Coins/Streaks funktionieren
- ✅ GitHub Release mit APK vorhanden
- ✅ In-App-Update erkennt neue Version
- ✅ `tasks/lessons.md` mit allen Bugs/Fixes aktualisiert
