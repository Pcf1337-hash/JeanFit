package com.jeanfit.app.domain.usecase

import javax.inject.Inject
import kotlin.math.roundToInt

class CalcCaloriesUseCase @Inject constructor() {

    fun calculate(
        gender: String,
        weightKg: Float,
        heightCm: Float,
        ageYears: Int,
        activityLevel: String,
        weeklyGoalKgLoss: Float = 0.5f
    ): Int {
        val bmr = when (gender) {
            "male" -> 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * ageYears)
            "female" -> 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * ageYears)
            else -> 500.0 + (11.0 * weightKg) + (4.0 * heightCm) - (5.0 * ageYears)
        }
        val activityFactor = when (activityLevel) {
            "sedentary" -> 1.2
            "lightly_active" -> 1.375
            "active" -> 1.55
            "very_active" -> 1.725
            else -> 1.2
        }
        val tdee = bmr * activityFactor
        val deficit = weeklyGoalKgLoss * 7700.0 / 7.0
        val minCalories = if (gender == "male") 1500 else 1200
        return maxOf(minCalories, (tdee - deficit).roundToInt())
    }
}
