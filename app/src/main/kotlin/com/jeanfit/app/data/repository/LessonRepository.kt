package com.jeanfit.app.data.repository

import com.jeanfit.app.data.db.dao.LessonDao
import com.jeanfit.app.data.db.entities.Course
import com.jeanfit.app.data.db.entities.Lesson
import com.jeanfit.app.data.db.entities.LessonProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val dao: LessonDao
) {
    fun getAllCourses(): Flow<List<Course>> = dao.getAllCourses()
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>> = dao.getLessonsForCourse(courseId)
    suspend fun getLessonById(lessonId: String): Lesson? = dao.getLessonById(lessonId)
    fun getProgressForLesson(lessonId: String): Flow<LessonProgress?> = dao.getProgressForLesson(lessonId)
    fun getAllCompletedProgress(): Flow<List<LessonProgress>> = dao.getAllCompletedProgress()
    fun getCompletedCount(): Flow<Int> = dao.getCompletedLessonCount()

    suspend fun markLessonCompleted(lessonId: String, quizScore: Int? = null, timeSpent: Int = 0) {
        dao.upsertProgress(
            LessonProgress(
                lessonId = lessonId,
                isCompleted = true,
                completedAtMs = System.currentTimeMillis(),
                quizScore = quizScore,
                timeSpentSeconds = timeSpent
            )
        )
    }

    suspend fun seedInitialContent(courses: List<Course>, lessons: List<Lesson>) {
        dao.insertCourses(courses)
        dao.insertLessons(lessons)
    }
}
