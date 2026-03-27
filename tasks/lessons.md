# tasks/lessons.md — NutriMind: Lektionssystem + Agent-Lessons

Diese Datei hat **zwei Funktionen**:
1. **Teil A**: Inhalt aller App-Lektionen (7 Kurse × 14 Lektionen)
2. **Teil B**: Agent-Lessons — Lernregeln für den Claude-Agenten

---

# TEIL A: APP-LEKTIONS-INHALTE

> **Format für jeden Kurs**: Kursmeta + vollständiges JSON für jede Lektion
> Alle Texte auf **Deutsch**, Ton: freundlich, locker, motivierend, wissenschaftlich fundiert

---

## Kurs 1: Grundlagen — Dein Werkzeugkoffer
**courseId:** `course_01`
**Wochen:** 1–2 | **Lektionen:** 14 | **Icon:** 🧠

### Lektion 1.1 — Willkommen: Dein Big Picture
**lessonId:** `lesson_01_01` | **Typ:** article | **Minuten:** 7 | **Coins:** 0

```json
[
  {"type":"text","content":"# Willkommen! Lass uns über dein Warum reden 🎯\n\nDu hast diesen ersten Schritt gemacht. Das ist kein Zufall – irgendwas hat dich hierher gebracht.\n\nBei NutriMind geht es nicht darum, eine Diät zu machen. Es geht darum, **langfristig zu verstehen, warum wir essen wie wir essen** – und das zu verändern.\n\nForschung zeigt: Menschen die wissen *warum* sie abnehmen wollen, halten ihre neuen Gewohnheiten viermal häufiger bei."},
  {"type":"tip","icon":"💡","content":"**Wissenschaft dahinter**: Das nennt sich *intrinsische Motivation*. Je konkreter dein persönlicher Grund, desto stärker zieht er dich voran."},
  {"type":"reflection","prompt":"Was ist dein **tiefster Grund**, warum du dich jetzt verändern möchtest? Nicht 'ich will Gewicht verlieren' – sondern: Was wird sich in deinem Leben besser anfühlen?","placeholder":"Zum Beispiel: 'Ich will mit meinen Kindern spielen können ohne außer Atem zu sein...'"},
  {"type":"text","content":"## Dein Big Picture (YBP)\n\nDein 'Big Picture' ist dein übergeordnetes Lebensziel. Es ist der Leuchtturm, zu dem du navigierst – auch wenn der Weg manchmal holprig ist.\n\nSchreib dir deinen YBP auf. Wir werden immer wieder darauf zurückkommen."},
  {"type":"activity","title":"Jetzt handeln","instruction":"Tippe auf 'Profil' und füge deinen persönlichen Motivationstext hinzu. Er wird dir an schwierigen Tagen angezeigt.","action":"navigate_profile"}
]
```

### Lektion 1.2 — Dein Gehirn: Elefant und Reiter
**lessonId:** `lesson_01_02` | **Typ:** article | **Minuten:** 8

```json
[
  {"type":"text","content":"# Warum du manchmal 'schwach' wirst – und warum das normal ist 🐘\n\nStell dir vor: Du bist der Reiter auf einem riesigen Elefanten. Der Reiter = dein rationales Gehirn. Der Elefant = dein emotionales, impulsives Gehirn.\n\nWenn du um Mitternacht zum Kühlschrank gehst, obwohl du es nicht wolltest – wer hat gewonnen? Richtig: der Elefant.\n\nDas ist keine Schwäche. Das ist Biologie."},
  {"type":"image","resName":"ic_elephant_rider","caption":"Elefant (emotional) vs. Reiter (rational) — beide brauchen einander"},
  {"type":"text","content":"## Wie du den Reiter stärkst\n\n**Strategie 1: Wenn-Dann-Pläne**\n'*Wenn* ich Stress habe und Lust auf Chips bekomme, *dann* mache ich erst einen 5-minütigen Spaziergang.*'\n\n**Strategie 2: Umgebung gestalten**\nDer Elefant geht immer den Weg des geringsten Widerstands. Räume Süßigkeiten aus dem Sichtfeld.\n\n**Strategie 3: Pause einlegen**\n'Surf the urge': Wenn ein Impuls kommt, warte 10 Minuten. 70% aller Impulse vergehen von selbst."},
  {"type":"quiz","question":"Was ist der 'Elefant' in der Metapher?","options":["Das rationale, planende Gehirn","Das emotionale, impulsive Gehirn","Der Hunger","Der Wille"],"correctIndex":1,"explanation":"Richtig! Der Elefant steht für das limbische System – das emotionale Gehirn, das auf Sofortbelohnungen ausgerichtet ist."},
  {"type":"tip","icon":"🧠","content":"**CBT-Technik**: Diese Methode heißt 'Wenn-Dann-Planung' und wurde in hunderten Studien als wirksam belegt. Kleine Vorsätze erzeugen große Veränderungen."}
]
```

### Lektion 1.3 — Kaloriendichte: Warum Volumen zählt
**lessonId:** `lesson_01_03` | **Typ:** article | **Minuten:** 6

```json
[
  {"type":"text","content":"# Das Geheimnis: Mehr essen, weniger Kalorien 🥦\n\nDas klingt wie Magie – ist aber Physik.\n\n**Kaloriendichte** = Kalorien pro Gramm. Broccoli hat 0,35 kcal/g. Chips haben 5,3 kcal/g. Das heißt: Du musst **15x mehr** Chips essen um dasselbe Gewicht Broccoli zu erreichen.\n\nDein Magen registriert *Volumen* als Sättigung – nicht Kalorien. Deshalb kannst du mit kalorienarmen Lebensmitteln satt werden."},
  {"type":"image","resName":"ic_calorie_density_comparison","caption":"Gleiche Kalorienmenge: 500g Obst vs. 90g Chips"},
  {"type":"quiz","question":"Welches Lebensmittel hat die niedrigste Kaloriendichte?","options":["Weißbrot","Hähnchenbrust","Gurke","Nüsse"],"correctIndex":2,"explanation":"Gurke hat ca. 0,15 kcal/g – fast nur Wasser und Ballaststoffe. Nüsse dagegen kommen auf 6 kcal/g!"},
  {"type":"text","content":"## Das Ampelsystem\n\nGenau darauf basiert unser Farbsystem:\n\n🟢 **Grün** (≤1 kcal/g): Gemüse, Obst, fettarme Milchprodukte – so viel du willst\n🟡 **Gelb** (1–2,4 kcal/g): Hülsenfrüchte, mageres Fleisch, Vollkorn – in Maßen\n🟠 **Orange** (>2,4 kcal/g): Öle, Nüsse, Süßigkeiten – kleine Portionen\n\nDas ist kein Verbot. Nur Bewusstsein."},
  {"type":"activity","title":"Teste es jetzt","instruction":"Logge dein nächstes Essen und schau dir die Farbe an. Überrascht dich etwas?","action":"navigate_food_log"}
]
```

### Lektion 1.4 — Das Ampelsystem im Alltag
**lessonId:** `lesson_01_04` | **Typ:** quiz | **Minuten:** 5

```json
[
  {"type":"text","content":"# Quiz: Welche Farbe hat dein Lieblingsessen? 🎨\n\nZeit das Wissen zu testen! Beantworte 5 Fragen zum Ampelsystem."},
  {"type":"quiz","question":"Welche Farbe hat Vollkornreis?","options":["🟢 Grün","🟡 Gelb","🟠 Orange"],"correctIndex":0,"explanation":"Vollkornreis ist ein gutes Beispiel: Normaler Reis wäre Gelb (1,3 kcal/g), aber Vollkorn wird eine Stufe heruntergestuft wegen höherem Ballaststoffgehalt!"},
  {"type":"quiz","question":"Erdnussbutter: Welche Farbe?","options":["🟢 Grün","🟡 Gelb","🟠 Orange"],"correctIndex":2,"explanation":"Orange! Mit ~6 kcal/g ist Erdnussbutter sehr kalorienreich. Ein Esslöffel = ca. 90 kcal. Das ist okay – aber in kleinen Mengen."},
  {"type":"quiz","question":"Hähnchenbrust (ohne Haut): Welche Farbe?","options":["🟢 Grün","🟡 Gelb","🟠 Orange"],"correctIndex":0,"explanation":"Seit 2025: Hähnchenbrust ist Grün! Mit ca. 1,1 kcal/g und sehr hohem Proteingehalt wurde sie heruntergestuft."},
  {"type":"quiz","question":"Wie viel Prozent der täglichen Kalorien sollten idealerweise Grün sein?","options":["10–20%","30–40%","50–60%","80%+"],"correctIndex":2,"explanation":"Das Ziel ist eine Mahlzeit mit 50–60% grünen Kalorien als Basis. Das hält dich satt bei niedrigem Kalorienverbrauch."},
  {"type":"tip","icon":"💡","content":"**Merke**: Das System verbietet nichts. Orange-Lebensmittel sind köstlich – du lernst nur, sie mit Bewusstsein zu genießen."}
]
```

### Lektionen 1.5 bis 1.14 — Kurzübersicht
```
1.5  Gewohnheiten verstehen: Trigger → Routine → Belohnung
1.6  SMART-Ziele für Ernährung und Bewegung
1.7  Hunger vs. Appetit unterscheiden (4 Hunger-Typen)
1.8  Portionsgrößen: Was ist wirklich eine Portion?
1.9  Wasser und Sättigung (Hunger oft = Durst)
1.10 Essen verlangsamen: Die 20-Minuten-Regel
1.11 Schlaf und Gewicht: Ghrelin und Leptin
1.12 Stressessen erkennen: Dein persönliches Muster
1.13 Mini-Quiz: Erstes Wochen-Review
1.14 Kurs 1 Abschluss: Dein Werkzeugkoffer ist gefüllt
```

---

## Kurs 2: Emotionales Essen — Deine Trigger kennen
**courseId:** `course_02`
**Wochen:** 3–4 | **Lektionen:** 14 | **Icon:** ❤️

### Lektion 2.1 — Was ist emotionales Essen?
**lessonId:** `lesson_02_01` | **Typ:** article | **Minuten:** 8

```json
[
  {"type":"text","content":"# Wenn das Herz Hunger hat 💔\n\nEmotionales Essen ist, wenn wir essen um uns zu fühlen – nicht weil wir körperlich hungrig sind.\n\nStudien zeigen: **75% aller Überessen-Episoden** haben eine emotionale Ursache. Das sind echte Zahlen.\n\nDie häufigsten Emotionen hinter dem Essen:\n- 😰 **Stress** – Cortisol treibt Heißhunger auf Zucker und Fett\n- 😔 **Langeweile** – Essen als Stimulation\n- 😢 **Traurigkeit** – Komfortessen als Selbstmedikation\n- 😤 **Ärger** – Essen als Ventil\n- 🎉 **Feier** – Belohnungsessen (auch das ist emotional!)"},
  {"type":"reflection","prompt":"Wann hast du zuletzt gegessen ohne körperlichen Hunger? Was hast du dabei gefühlt?","placeholder":"Denke ehrlich nach – kein Urteil..."},
  {"type":"text","content":"## Die gute Nachricht\n\nEmotionales Essen ist **lernbar und veränderbar**. Es ist keine Charakterschwäche. Es ist eine erlernte Strategie – die du durch bessere Strategien ersetzen kannst."},
  {"type":"tip","icon":"🔬","content":"**CBT-Basis**: Gedanken → Gefühle → Verhalten. Wenn wir den Gedanken ändern, ändert sich das Verhalten. Das ist der Kern unserer Methode."}
]
```

### Lektionen 2.2 bis 2.14 — Kurzübersicht
```
2.2  Deine persönliche Trigger-Liste erstellen
2.3  Hunger-Skala: 1–10 bewerten bevor du isst
2.4  Die HALT-Methode (Hungry/Angry/Lonely/Tired)
2.5  Coping-Strategien ohne Essen (Liste erstellen)
2.6  Gedanken beobachten ohne zu urteilen (Mindfulness)
2.7  Achtsamkeitsessen: Eine Mahlzeit ohne Ablenkung
2.8  Körperlicher vs. emotionaler Hunger: 7 Unterschiede
2.9  Selbstmitgefühl statt Selbstkritik
2.10 Notfall-Plan für schwierige Momente
2.11 Dein Stress-Essen-Protokoll (Woche)
2.12 DBT-Technik: TIPP (Temperatur, intensiv, Paced Breathing, Paired relaxation)
2.13 Rückfall als Teil des Prozesses
2.14 Kurs 2 Abschluss: Du kennst deine Trigger
```

---

## Kurs 3: Ernährung verstehen
**courseId:** `course_03`
**Wochen:** 5–6 | **Lektionen:** 14 | **Icon:** 🥗

```
3.1  Makronährstoffe: Was Protein, Carbs und Fett wirklich machen
3.2  Protein und Sättigung: Warum mehr Protein hilft
3.3  Gesunde Fette vs. schlechte Fette: Olivenöl ja, Transfette nein
3.4  Kohlenhydrate und Blutzucker: der Spike-Crash-Kreislauf
3.5  Ballaststoffe: Der unterschätzte Sättigungsmacher
3.6  Zucker lesen: Labels richtig interpretieren
3.7  Gesund im Restaurant bestellen (10 Strategien)
3.8  Meal Prep: 1 Stunde für die ganze Woche
3.9  Einkaufen ohne Impulsbuying
3.10 Alkohol und Kalorien: der blinde Fleck
3.11 Intermittierendes Fasten: Pro/Contra
3.12 Supplements: Was bringt was?
3.13 Mediterrane Ernährung als Blaupause
3.14 Kurs 3 Abschluss: Ernährungsexperte Level 1
```

---

## Kurs 4: Bewegung & Schlaf
**courseId:** `course_04`
**Wochen:** 7–8 | **Lektionen:** 14 | **Icon:** 🏃‍♀️

```
4.1  Bewegung und Gewicht: Die echten Zahlen
4.2  NEAT: Die unterschätzte Aktivität (Non-Exercise Activity Thermogenesis)
4.3  10.000 Schritte – Mythos oder Ziel?
4.4  Krafttraining und Grundumsatz
4.5  Wie du eine Bewegungsroutine aufbaust (Habit Stacking)
4.6  Schlaf und Gewicht: Ghrelin, Leptin, Cortisol
4.7  Schlafhygiene: 10 Regeln für besseren Schlaf
4.8  Chronotypen: Bist du Lerche oder Eule?
4.9  Sitzen ist das neue Rauchen: Gegenmittel
4.10 Stressreduktion durch Bewegung (Endorphine, BDNF)
4.11 Sport und Kaloriendefizit: Nicht übertrainieren
4.12 Yoga und Meditation als Ergänzung
4.13 Deinen Activity Level realistisch einschätzen
4.14 Kurs 4 Abschluss: Die vier Säulen der Gesundheit
```

---

## Kurs 5: Soziale Dynamiken
**courseId:** `course_05`
**Wochen:** 9–10 | **Lektionen:** 14 | **Icon:** 👥

```
5.1  Essen in Gesellschaft: Die soziale Kalorienblindheit
5.2  Familiäre Essgewohnheiten verstehen
5.3  Mit Freunden essen ohne Stress
5.4  "Nur einmal" – mit Druck umgehen
5.5  Feste und Feiern ohne schlechtes Gewissen
5.6  Mit dem Partner über Ernährung sprechen
5.7  Kinder und gesundes Essen
5.8  Arbeitsumfeld: Büroküche, Meetings, Kantinenessen
5.9  Urlaub und Reisen: Flexibel bleiben
5.10 Social Media und Körperbild
5.11 Vergleiche stoppen: Deine Reise ist einzigartig
5.12 Support-System aufbauen
5.13 Anderen helfen ohne sich selbst zu vergessen
5.14 Kurs 5 Abschluss: Soziale Meisterin/Meister
```

---

## Kurs 6: Plateaus & Rückschläge
**courseId:** `course_06`
**Wochen:** 11–12 | **Lektionen:** 14 | **Icon:** 📈

```
6.1  Warum Plateaus normal sind (Physiologie)
6.2  Adaptive Thermogenese verstehen
6.3  Plateau-Strategien: 5 bewährte Methoden
6.4  Rückfall ≠ Versagen: Reframing
6.5  Self-Compassion nach dem Rückfall (Kristin Neff)
6.6  ACT-Technik: Werte statt Regeln
6.7  Das Yo-Yo-Syndrom verhindern
6.8  Emotionen nach dem Rückfall verarbeiten
6.9  Wenn Motivation schwindet: was dann?
6.10 Kleine Wins feiern (Nicht-Gewichts-Erfolge)
6.11 Körperbild und Waage entkoppeln
6.12 Körpermaße statt Gewicht messen
6.13 Langfristiges Denken trainieren
6.14 Kurs 6 Abschluss: Resilienz aufgebaut
```

---

## Kurs 7: Langfristige Erhaltung
**courseId:** `course_07`
**Wochen:** 13–16 | **Lektionen:** 14 | **Icon:** 🏆

```
7.1  Von Diät-Denken zu Lifestyle-Denken
7.2  Identitätsbasierte Gewohnheiten (James Clear - Atomic Habits)
7.3  Gewicht halten ist anders als abnehmen
7.4  Maintenance-Kalorien berechnen
7.5  Flexible Kontrolle vs. rigide Kontrolle
7.6  80/20 Regel für die Erhaltungsphase
7.7  Saisonales Essen und Feiertage
7.8  Langfritiges Monitoring: Was messen, was loslassen
7.9  Rückfallprävention: Dein persönlicher Notfallplan
7.10 Die neue Version von dir
7.11 Anderen mit deinem Wissen helfen
7.12 Dankbarkeit und Körperakzeptanz
7.13 Nächste Gesundheitsziele setzen
7.14 🏆 Kurs 7 Abschluss: Du hast es geschafft!
```

---

# TEIL B: AGENT LESSONS — LERNREGELN FÜR CLAUDE

> Diese Regeln werden nach jeder Nutzer-Korrektur erweitert. Immer reviewen!

---

## Lessons aus diesem Projekt

### LESSON-001: Kein Placeholder-Code
**Problem:** Compose-Screens mit `// TODO: Implement` hinterlassen
**Regel:** Jede implementierte Funktion muss vollständig funktionsfähig sein. Kein `TODO()`, kein `throw NotImplementedError()` in fertigem Code.
**Check:** Vor Code-Abgabe: Grep nach "TODO", "FIXME", "NotImplementedError".

### LESSON-002: Room Flow immer via StateFlow
**Problem:** Repository gibt direkt `List<>` zurück statt `Flow<List<>>`
**Regel:** Alle Room-Queries die sich ändern können müssen `Flow<T>` zurückgeben. ViewModel sammelt zu `StateFlow` via `stateIn()`.
**Beispiel:**
```kotlin
// ❌ Falsch
suspend fun getTodayLogs(): List<FoodLogEntry>
// ✅ Richtig
fun getTodayLogs(): Flow<List<FoodLogEntry>>
```

### LESSON-003: Hilt Module Scoping
**Problem:** Singleton-Scope für Retrofit/Room vergessen → mehrfache Instanzen
**Regel:** Alle DB-/Network-Instanzen immer `@Singleton` annotieren. Repository `@Singleton`. ViewModel `@HiltViewModel`.

### LESSON-004: CameraX in Compose
**Problem:** `PreviewView` aus CameraX crasht als direktes Composable
**Regel:** CameraX `PreviewView` immer über `AndroidView` wrappen:
```kotlin
AndroidView(factory = { PreviewView(it).apply { ... } })
```
CameraX-Lifecycle immer an `LocalLifecycleOwner` binden.

### LESSON-005: Vico Chart Dark Mode
**Problem:** Vico Chart ignoriert das Material-Theme im Dark Mode
**Regel:** Vico `CartesianChartHost` benötigt explizites `rememberCartesianChartHostColors()` mit den Custom-Farben. Nie auf Auto-Theming verlassen.

### LESSON-006: Room FTS Queries
**Problem:** FTS4-Suche gibt keine Ergebnisse wegen Syntax
**Regel:** FTS-Queries mit Wildcard immer so schreiben:
```kotlin
@Query("SELECT * FROM food_items_fts WHERE food_items_fts MATCH :query || '*'")
```
Sternchen am Ende für Prefix-Suche. Leerzeichen in Query zu `*` umwandeln.

### LESSON-007: Compose State in Onboarding
**Problem:** Onboarding-State geht beim Screen-Wechsel verloren
**Regel:** Onboarding-State NUR im ViewModel (Hilt) halten, nicht in lokalen `remember`-Variablen. DataStore als Backup für vorzeitigen App-Kill.

### LESSON-008: WorkManager Constraints
**Problem:** WorkManager-Job läuft nicht zur richtigen Zeit
**Regel:** Für tägliche Reminders `PeriodicWorkRequest` mit `setInitialDelay` und `ExistingPeriodicWorkPolicy.UPDATE` nutzen. Bei Cancel-and-Reschedule: alten Job erst canceln.

### LESSON-009: ML Kit Barcode Analyzer Thread
**Problem:** ML Kit Analyzer blockiert Main-Thread
**Regel:** `ImageAnalysis.setAnalyzer(Dispatchers.IO.asExecutor(), analyzer)` – immer im IO-Dispatcher laufen lassen. Ergebnis via `Channel` oder `callback` zurückgeben.

### LESSON-010: OpenFoodFacts API Ratelimit
**Problem:** API liefert 429 bei schnellen Scans
**Regel:** Nach jedem Scan 500ms Delay einbauen. Room-Cache immer zuerst prüfen (Barcode als Index). `isSynced`-Pattern verwenden: lokal erst speichern, dann API.

### LESSON-011: Compose Recomposition Optimierung
**Problem:** Ganze Listen recomponieren bei kleinen State-Änderungen
**Regel:** LazyColumn-Items mit `key = { item.id }` versehen. Lambdas als `remember { }` wrappen. `@Stable`-Annotation für unveränderliche Datenklassen nutzen.

### LESSON-012: Dark Mode für Custom Components
**Problem:** Custom Composables ignorieren Dark/Light-Wechsel
**Regel:** Niemals hardcodierte Farben in Composables. Immer `MaterialTheme.colorScheme.xyz` nutzen oder eigene Token über `LocalContentColor`.

### LESSON-013: Harris-Benedict Grenzwert
**Problem:** Kalorienberechnung ergibt unrealistisch niedrige Werte
**Regel:** `maxOf(1200, berechneterWert)` – niemals unter 1200 kcal ausgeben. Für Männer: `maxOf(1500, wert)`. Immer im UseCase validieren, nicht in der UI.

### LESSON-014: Achievement Race Condition
**Problem:** Achievement mehrfach vergeben bei schnellen aufeinanderfolgenden Aktionen
**Regel:** Achievement-Vergabe immer mit `isUnlocked`-Check in Room-Transaction:
```kotlin
@Transaction
suspend fun unlockAchievement(id: String) {
    if (getAchievement(id)?.isUnlocked == false) {
        updateAchievement(id, isUnlocked = true, unlockedAtMs = now())
    }
}
```

### LESSON-015: Lottie in Compose Lifecycle
**Problem:** Lottie-Animation läuft unendlich ohne Stop-Bedingung
**Regel:** Immer `iterations = 1` für Celebrations-Animationen. `LottieAnimation(iterations = LottieConstants.IterateForever)` nur für Loop-Animationen (Loader etc.).

---

## Allgemeine Agent-Regeln (projektübergreifend)

### RULE-A: Compile-First
Bevor du einen neuen Feature-Branch anfängst: `./gradlew assembleDebug`. Wenn es nicht kompiliert, ist der vorherige Task nicht fertig.

### RULE-B: Migrations bei Room-Änderungen
Wenn ein Room-Entity geändert wird (neues Feld, neuer Typ): `version++` und `addMigrations()`. Niemals `fallbackToDestructiveMigration()` in Production-Builds.

### RULE-C: Keine Magic Numbers
Alle Konstanten in einem `object Constants { ... }` oder als `companion object`-Felder. Keine `1200`, `0.1f`, `"breakfast"` direkt im Code.

### RULE-D: Dependency Injection bis ins Detail
Kein `FoodRepository()` direkt instantiieren. Kein `Retrofit.Builder()` außerhalb von Hilt-Modules. Jede Abhängigkeit muss injizierbar sein.

### RULE-E: Error State immer
Kein ViewModel-State ohne Error-Handling. Sealed class für UI-State:
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### RULE-F: Keine direkten API-Calls aus Composables
Composables sind UI. Alle Daten kommen aus ViewModel-StateFlow. Kein `LaunchedEffect { repository.fetch() }` in Composables.

---

## Template für neue Lessons

Wenn der User eine Korrektur macht, sofort eintragen:

```
### LESSON-XXX: [Kurztitel]
**Problem:** [Was ist schiefgelaufen?]
**Regel:** [Was ist die richtige Vorgehensweise?]
**Beispiel:**
// ❌ Falsch: [schlechtes Beispiel]
// ✅ Richtig: [gutes Beispiel]
**Check:** [Wie verifizieren?]
```
