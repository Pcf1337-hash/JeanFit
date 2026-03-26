package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jeanfit.app.data.db.entities.CoachMemory
import com.jeanfit.app.data.db.entities.CoachMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {

    // --- Nachrichten ---

    @Insert
    suspend fun insertMessage(message: CoachMessage): Long

    @Query("SELECT * FROM coach_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CoachMessage>>

    /** Letzte N Nachrichten für Kontext-Window (API-Calls) */
    @Query("SELECT * FROM coach_messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastMessages(limit: Int = 20): List<CoachMessage>

    @Query("UPDATE coach_messages SET isRead = 1 WHERE isFromCoach = 1 AND isRead = 0")
    suspend fun markAllCoachMessagesRead()

    @Query("SELECT COUNT(*) FROM coach_messages WHERE isFromCoach = 1 AND isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("DELETE FROM coach_messages")
    suspend fun clearAllMessages()

    // --- Gedächtnis ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: CoachMemory)

    @Query("SELECT * FROM coach_memory ORDER BY updatedAt DESC")
    suspend fun getAllMemories(): List<CoachMemory>

    @Query("SELECT * FROM coach_memory WHERE category = :category")
    suspend fun getMemoriesByCategory(category: String): List<CoachMemory>

    @Query("DELETE FROM coach_memory WHERE key = :key")
    suspend fun deleteMemory(key: String)
}
