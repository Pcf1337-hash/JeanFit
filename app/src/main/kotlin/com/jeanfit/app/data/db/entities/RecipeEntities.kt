package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val recipeId: String,
    val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servings: Int = 1,
    val totalCaloriesPerServing: Float,
    val proteinPerServing: Float,
    val carbsPerServing: Float,
    val fatPerServing: Float,
    val greenPercent: Float = 0f,
    val yellowPercent: Float = 0f,
    val orangePercent: Float = 0f,
    val ingredientsJson: String = "[]",
    val stepsJson: String = "[]",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

@Entity(
    tableName = "meal_plan",
    primaryKeys = ["dateEpochDay", "mealType"]
)
data class MealPlan(
    val dateEpochDay: Long,
    val mealType: String,
    val recipeId: String? = null,
    val customMealName: String? = null
)
