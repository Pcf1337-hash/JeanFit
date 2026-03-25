package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val contentJson: String,
    val lessonType: String,
    val coinsReward: Int = 0,
    val isAudioAvailable: Boolean = false
)

@Entity(tableName = "lesson_progress", primaryKeys = ["lessonId"])
data class LessonProgress(
    val lessonId: String,
    val isCompleted: Boolean = false,
    val completedAtMs: Long? = null,
    val quizScore: Int? = null,
    val timeSpentSeconds: Int = 0
)
