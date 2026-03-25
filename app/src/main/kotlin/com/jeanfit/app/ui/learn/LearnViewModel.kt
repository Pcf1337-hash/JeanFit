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
            com.jeanfit.app.data.db.entities.Course(
                courseId = "course_1",
                title = "Grundlagen der Ernährung",
                description = "Verstehe, wie Kalorien und Nährstoffe wirklich funktionieren",
                weekNumber = 1,
                iconEmoji = "🍎",
                totalLessons = 5,
                requiredCoinsToUnlock = 0
            ),
            com.jeanfit.app.data.db.entities.Course(
                courseId = "course_2",
                title = "Dein Gehirn und das Essen",
                description = "Psychologie des Essens — warum wir essen, was wir essen",
                weekNumber = 2,
                iconEmoji = "🧠",
                totalLessons = 5,
                requiredCoinsToUnlock = 3
            ),
            com.jeanfit.app.data.db.entities.Course(
                courseId = "course_3",
                title = "Gewohnheiten aufbauen",
                description = "Wie du nachhaltige Gewohnheiten entwickelst",
                weekNumber = 3,
                iconEmoji = "🔄",
                totalLessons = 4,
                requiredCoinsToUnlock = 7
            )
        )
        val lessons = listOf(
            com.jeanfit.app.data.db.entities.Lesson(
                lessonId = "l1_1",
                courseId = "course_1",
                title = "Was ist Kaloriendichte?",
                orderIndex = 0,
                estimatedMinutes = 5,
                contentJson = """[{"type":"text","content":"## Kaloriendichte\n\nKaloriendichte beschreibt, wie viele Kalorien in 100g eines Lebensmittels stecken.\n\n**Grüne Lebensmittel** (≤ 1,0 kcal/g): Gemüse, Obst, Hülsenfrüchte\n\n**Gelbe Lebensmittel** (1,0–2,4 kcal/g): Vollkorn, mageres Fleisch, Fisch\n\n**Orange Lebensmittel** (> 2,4 kcal/g): Nüsse, Öle, Süßes"},{"type":"tip","icon":"💡","content":"**Tipp**: Fülle deinen Teller zu 50% mit grünen Lebensmitteln!"},{"type":"quiz","question":"Was ist Kaloriendichte?","options":["Kalorien pro 100g","Kalorien pro Tag","Kalorien pro Stunde"],"correctIndex":0,"explanation":"Richtig! Kaloriendichte = Kalorien ÷ 100g des Lebensmittels."}]""",
                lessonType = "article",
                coinsReward = 0
            ),
            com.jeanfit.app.data.db.entities.Lesson(
                lessonId = "l1_2",
                courseId = "course_1",
                title = "Makronährstoffe verstehen",
                orderIndex = 1,
                estimatedMinutes = 7,
                contentJson = """[{"type":"text","content":"## Die drei Makronährstoffe\n\n**Protein** (4 kcal/g): Baut Muskeln auf, hält lange satt\n\n**Kohlenhydrate** (4 kcal/g): Schnelle Energie, bevorzugt aus Vollkorn\n\n**Fett** (9 kcal/g): Essentiell für Hormone und Vitamine\n\nEin gutes Verhältnis: 30% Protein, 40% Kohlenhydrate, 30% Fett"},{"type":"quiz","question":"Wie viele Kalorien hat 1g Fett?","options":["4 kcal","7 kcal","9 kcal"],"correctIndex":2,"explanation":"Fett hat 9 kcal/g — mehr als doppelt so viel wie Protein oder Kohlenhydrate."}]""",
                lessonType = "article",
                coinsReward = 0
            ),
            com.jeanfit.app.data.db.entities.Lesson(
                lessonId = "l1_3",
                courseId = "course_1",
                title = "Portionsgrößen im Griff",
                orderIndex = 2,
                estimatedMinutes = 5,
                contentJson = """[{"type":"text","content":"## Portionen richtig einschätzen\n\nOhne Waage:\n- **Protein**: Handfläche = ca. 100g\n- **Kohlenhydrate**: Faust = ca. 100g\n- **Fett**: Daumen = ca. 15g Öl/Nüsse\n- **Gemüse**: Beide Hände = unbegrenzt!"},{"type":"activity","title":"Jetzt ausprobieren","instruction":"Schätze die nächste Mahlzeit ohne Waage ab — dann wiege nach!","action":"navigate_food_log"}]""",
                lessonType = "article",
                coinsReward = 0
            ),
            com.jeanfit.app.data.db.entities.Lesson(
                lessonId = "l2_1",
                courseId = "course_2",
                title = "Der Elefant und der Reiter",
                orderIndex = 0,
                estimatedMinutes = 6,
                contentJson = """[{"type":"text","content":"## Zwei Systeme in deinem Kopf\n\nDein Gehirn hat zwei Teile:\n\n**Der Elefant** = dein emotionales, automatisches System. Entscheidet schnell, reagiert auf Belohnungen.\n\n**Der Reiter** = dein rationales System. Plant, denkt langfristig.\n\nMeistens gewinnt der Elefant — deshalb essen wir aus Emotion, nicht aus Hunger."},{"type":"tip","icon":"💡","content":"**Tipp**: Bevor du isst — frage dich: Bin ich wirklich hungrig oder suche ich Trost?"},{"type":"reflection","prompt":"Wann hast du zuletzt aus Emotion gegessen? Was war der Auslöser?","placeholder":"Schreib deine Gedanken..."}]""",
                lessonType = "article",
                coinsReward = 0
            ),
            com.jeanfit.app.data.db.entities.Lesson(
                lessonId = "l3_1",
                courseId = "course_3",
                title = "Die Gewohnheitsschleife",
                orderIndex = 0,
                estimatedMinutes = 8,
                contentJson = """[{"type":"text","content":"## Cue – Routine – Reward\n\nJede Gewohnheit folgt diesem Muster:\n\n1. **Cue (Auslöser)**: z.B. Stress, Langeweile, eine bestimmte Zeit\n2. **Routine**: das automatische Verhalten (z.B. Snacken)\n3. **Reward (Belohnung)**: das Gefühl danach\n\nUm eine Gewohnheit zu ändern, behalte Cue und Reward — aber ändere die Routine!"},{"type":"quiz","question":"Was ist der erste Schritt der Gewohnheitsschleife?","options":["Reward","Routine","Cue"],"correctIndex":2,"explanation":"Der Cue (Auslöser) startet die Gewohnheit — erkenne ihn, um sie zu verändern."}]""",
                lessonType = "article",
                coinsReward = 0
            )
        )
        lessonRepository.seedInitialContent(courses, lessons)
    }
}
