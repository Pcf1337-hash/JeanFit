package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [Index("dateEpochDay", unique = true)]
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Float,
    val dateEpochDay: Long,
    val note: String? = null,
    val trendWeightKg: Float? = null
)
