package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jeanfit.app.data.db.entities.Achievement
import com.jeanfit.app.data.db.entities.DailyTask
import com.jeanfit.app.data.db.entities.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {
    // DailyTask
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyTaskIfAbsent(task: DailyTask)

    @Query("SELECT * FROM daily_tasks WHERE dateEpochDay = :dayEpoch LIMIT 1")
    fun getDailyTask(dayEpoch: Long): Flow<DailyTask?>

    @Query("SELECT * FROM daily_tasks WHERE dateEpochDay = :dayEpoch LIMIT 1")
    suspend fun getDailyTaskOnce(dayEpoch: Long): DailyTask?

    @Query("UPDATE daily_tasks SET weightLogged = :value WHERE dateEpochDay = :dayEpoch")
    suspend fun setWeightLogged(dayEpoch: Long, value: Boolean)

    @Query("UPDATE daily_tasks SET allMealsLogged = :value WHERE dateEpochDay = :dayEpoch")
    suspend fun setAllMealsLogged(dayEpoch: Long, value: Boolean)

    @Query("UPDATE daily_tasks SET lessonCompleted = :value WHERE dateEpochDay = :dayEpoch")
    suspend fun setLessonCompleted(dayEpoch: Long, value: Boolean)

    @Query("UPDATE daily_tasks SET coinAwarded = 1 WHERE dateEpochDay = :dayEpoch AND coinAwarded = 0")
    suspend fun markCoinAwarded(dayEpoch: Long): Int

    // Streaks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: Streak)

    @Query("SELECT * FROM streaks WHERE type = :type LIMIT 1")
    fun getStreak(type: String): Flow<Streak?>

    @Query("SELECT * FROM streaks WHERE type = :type LIMIT 1")
    suspend fun getStreakOnce(type: String): Streak?

    @Query("SELECT * FROM streaks")
    fun getAllStreaks(): Flow<List<Streak>>

    // Achievements
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievementsIfAbsent(achievements: List<Achievement>)

    @Query("SELECT * FROM achievements ORDER BY category ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE achievementId = :id LIMIT 1")
    suspend fun getAchievementById(id: String): Achievement?

    @Transaction
    suspend fun unlockAchievement(id: String, now: Long) {
        val a = getAchievementById(id)
        if (a?.isUnlocked == false) {
            updateAchievementUnlocked(id, now)
        }
    }

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAtMs = :now WHERE achievementId = :id")
    suspend fun updateAchievementUnlocked(id: String, now: Long)

    @Query("UPDATE achievements SET currentProgress = :progress WHERE achievementId = :id")
    suspend fun updateProgress(id: String, progress: Int)

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedAtMs DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>
}
