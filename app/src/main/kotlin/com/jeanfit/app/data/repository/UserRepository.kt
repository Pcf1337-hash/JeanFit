package com.jeanfit.app.data.repository

import com.jeanfit.app.data.db.dao.UserProfileDao
import com.jeanfit.app.data.db.entities.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserProfileDao
) {
    fun getProfile(): Flow<UserProfile?> = dao.getProfile()
    suspend fun getProfileOnce(): UserProfile? = dao.getProfileOnce()
    suspend fun saveProfile(profile: UserProfile) = dao.insertOrReplace(profile)
    suspend fun addCoins(amount: Int) = dao.addCoins(amount)
    fun getCoins(): Flow<Int?> = dao.getCoins()
}
