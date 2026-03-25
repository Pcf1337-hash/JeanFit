package com.jeanfit.app

import androidx.lifecycle.ViewModel
import com.jeanfit.app.data.datastore.UserPreferences
import com.jeanfit.app.data.db.dao.UserProfileDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    val isDarkMode = userPreferences.isDarkMode

    // Room emittiert sofort null wenn kein Profil → false = Onboarding zeigen
    val onboardingCompleted = userProfileDao.getProfile()
        .map { it?.onboardingCompleted ?: false }
}
