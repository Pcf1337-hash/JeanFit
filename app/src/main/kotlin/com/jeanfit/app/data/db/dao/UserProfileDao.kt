package com.jeanfit.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jeanfit.app.data.db.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("UPDATE user_profile SET onboardingCompleted = 1 WHERE id = 1")
    suspend fun markOnboardingCompleted()

    @Query("UPDATE user_profile SET noomCoins = noomCoins + :amount WHERE id = 1")
    suspend fun addCoins(amount: Int)

    @Query("SELECT noomCoins FROM user_profile WHERE id = 1")
    fun getCoins(): Flow<Int?>
}
