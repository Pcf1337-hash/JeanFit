package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jeanfit.app.data.db.entities.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: WeightEntry)

    @Update
    suspend fun update(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM weight_entries ORDER BY dateEpochDay DESC")
    fun getAllEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY dateEpochDay DESC LIMIT 1")
    fun getLatestEntry(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries ORDER BY dateEpochDay DESC LIMIT 1")
    suspend fun getLatestEntryOnce(): WeightEntry?

    @Query("SELECT * FROM weight_entries WHERE dateEpochDay = :dayEpoch LIMIT 1")
    suspend fun getEntryForDay(dayEpoch: Long): WeightEntry?

    @Query("SELECT * FROM weight_entries WHERE dateEpochDay BETWEEN :startDay AND :endDay ORDER BY dateEpochDay ASC")
    fun getEntriesInRange(startDay: Long, endDay: Long): Flow<List<WeightEntry>>

    @Query("SELECT COUNT(*) FROM weight_entries")
    fun getTotalEntryCount(): Flow<Int>
}
