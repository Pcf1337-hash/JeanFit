package com.jeanfit.app.domain.usecase

import javax.inject.Inject

class CalcColorCategoryUseCase @Inject constructor() {
    fun calculate(caloriesPer100g: Float, isWholeGrain: Boolean = false): String {
        val cd = caloriesPer100g / 100f
        val category = when {
            cd <= 1.0f -> "green"
            cd <= 2.4f -> "yellow"
            else -> "orange"
        }
        return if (isWholeGrain && category == "yellow") "green"
        else if (isWholeGrain && category == "orange") "yellow"
        else category
    }
}
