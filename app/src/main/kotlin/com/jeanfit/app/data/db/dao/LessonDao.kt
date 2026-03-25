package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jeanfit.app.data.db.entities.Course
import com.jeanfit.app.data.db.entities.Lesson
import com.jeanfit.app.data.db.entities.LessonProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("SELECT * FROM courses ORDER BY weekNumber ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE lessonId = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: String): Lesson?

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId LIMIT 1")
    fun getProgressForLesson(lessonId: String): Flow<LessonProgress?>

    @Query("SELECT * FROM lesson_progress WHERE isCompleted = 1")
    fun getAllCompletedProgress(): Flow<List<LessonProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LessonProgress)

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE isCompleted = 1")
    fun getCompletedLessonCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE isCompleted = 1 AND completedAtMs >= :sinceMs")
    suspend fun getCompletedCountSince(sinceMs: Long): Int
}
