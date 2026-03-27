package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_items",
    indices = [Index("barcode"), Index("name")]
)
data class FoodItem(
    @PrimaryKey val foodId: String,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float? = null,
    val defaultServingSizeG: Float = 100f,
    val colorCategory: String,
    val calorieDensity: Float,
    val source: String = "custom",
    val unit: String = "g",         // "g" für Feststoffe, "ml" für Getränke
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "food_log_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["foodId"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("foodId"), Index("logDateEpochDay")]
)
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val foodId: String,
    val foodName: String = "",      // denormalisiert für schnelle Anzeige
    val mealType: String,
    val servingMultiplier: Float = 1f,
    val servingSizeG: Float,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val colorCategory: String,
    val logDateEpochDay: Long,
    val loggedAtMs: Long = System.currentTimeMillis()
)
