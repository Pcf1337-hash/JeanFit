package com.jeanfit.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "jeanfit_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.DARK_MODE] ?: false }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
