package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val email: String? = null,
    val gender: String = "prefer_not",
    val birthDateEpochDay: Long = 0L,
    val heightCm: Float = 170f,
    val startWeightKg: Float = 80f,
    val goalWeightKg: Float = 70f,
    val activityLevel: String = "lightly_active",
    val dailyCalorieGoal: Int = 1600,
    val programStartDate: Long = 0L,
    val noomCoins: Int = 0,
    val onboardingCompleted: Boolean = false,
    val motivationText: String = "",
    val bigPicture: String = ""
)
