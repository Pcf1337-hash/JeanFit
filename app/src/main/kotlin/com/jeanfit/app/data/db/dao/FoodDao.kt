package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.data.db.entities.FoodLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // FoodItem queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(items: List<FoodItem>)

    @Query("SELECT * FROM food_items WHERE foodId = :id LIMIT 1")
    suspend fun getFoodItemById(id: String): FoodItem?

    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getFoodItemByBarcode(barcode: String): FoodItem?

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 50")
    fun searchFoodItems(query: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 50")
    suspend fun searchFoodItemsOnce(query: String): List<FoodItem>

    @Query("SELECT * FROM food_items ORDER BY createdAt DESC LIMIT 20")
    fun getRecentFoodItems(): Flow<List<FoodItem>>

    // FoodLogEntry queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(entry: FoodLogEntry): Long

    @Query("DELETE FROM food_log_entries WHERE id = :id")
    suspend fun deleteLogEntry(id: Long)

    @Query("UPDATE food_log_entries SET servingMultiplier = :multiplier, servingSizeG = :sizeG, calories = :calories, protein = :protein, carbs = :carbs, fat = :fat WHERE id = :id")
    suspend fun updateLogEntry(id: Long, multiplier: Float, sizeG: Float, calories: Float, protein: Float, carbs: Float, fat: Float)

    @Query("SELECT * FROM food_log_entries WHERE logDateEpochDay = :dayEpoch ORDER BY loggedAtMs ASC")
    fun getEntriesForDay(dayEpoch: Long): Flow<List<FoodLogEntry>>

    @Query("SELECT * FROM food_log_entries WHERE logDateEpochDay = :dayEpoch ORDER BY loggedAtMs ASC")
    suspend fun getEntriesForDayOnce(dayEpoch: Long): List<FoodLogEntry>

    @Query("SELECT * FROM food_log_entries WHERE logDateEpochDay BETWEEN :startDay AND :endDay ORDER BY logDateEpochDay ASC")
    fun getEntriesInRange(startDay: Long, endDay: Long): Flow<List<FoodLogEntry>>

    @Query("SELECT SUM(calories) FROM food_log_entries WHERE logDateEpochDay = :dayEpoch")
    fun getCaloriesForDay(dayEpoch: Long): Flow<Float?>

    @Query("SELECT COUNT(*) FROM food_log_entries WHERE logDateEpochDay = :dayEpoch")
    suspend fun getEntryCountForDay(dayEpoch: Long): Int
}
