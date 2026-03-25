package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_tasks",
    indices = [Index("dateEpochDay", unique = true)]
)
data class DailyTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val weightLogged: Boolean = false,
    val allMealsLogged: Boolean = false,
    val lessonCompleted: Boolean = false,
    val coinAwarded: Boolean = false
)

@Entity(tableName = "streaks")
data class Streak(
    @PrimaryKey val type: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityEpochDay: Long = 0L
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val targetValue: Int,
    val isUnlocked: Boolean = false,
    val unlockedAtMs: Long? = null,
    val currentProgress: Int = 0
)
