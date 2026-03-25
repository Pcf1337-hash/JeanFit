package com.jeanfit.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.dao.GamificationDao
import com.jeanfit.app.data.db.entities.DailyTask
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.data.db.entities.UserProfile
import com.jeanfit.app.data.db.entities.WeightEntry
import com.jeanfit.app.data.repository.FoodRepository
import com.jeanfit.app.data.repository.UserRepository
import com.jeanfit.app.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfile? = null,
    val todayCalories: Float = 0f,
    val todayEntries: List<FoodLogEntry> = emptyList(),
    val latestWeight: WeightEntry? = null,
    val dailyTask: DailyTask? = null,
    val coins: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val foodRepository: FoodRepository,
    private val weightRepository: WeightRepository,
    private val gamificationDao: GamificationDao
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    // Combine 5 flows max, then zip coins separately
    private val coreState = combine(
        userRepository.getProfile(),
        foodRepository.getCaloriesForDay(today),
        foodRepository.getEntriesForDay(today),
        weightRepository.getLatestEntry(),
        gamificationDao.getDailyTask(today)
    ) { profile, calories, entries, weight, task ->
        HomeUiState(
            profile = profile as UserProfile?,
            todayCalories = (calories as Float?) ?: 0f,
            todayEntries = entries as List<FoodLogEntry>,
            latestWeight = weight as WeightEntry?,
            dailyTask = task as DailyTask?,
            isLoading = false
        )
    }

    val uiState: StateFlow<HomeUiState> = coreState
        .combine(userRepository.getCoins()) { state, coins ->
            state.copy(coins = coins ?: 0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch {
            gamificationDao.insertDailyTaskIfAbsent(DailyTask(dateEpochDay = today))
        }
    }

    fun finishDay() {
        viewModelScope.launch {
            gamificationDao.setAllMealsLogged(today, true)
            checkAndAwardCoin()
        }
    }

    private suspend fun checkAndAwardCoin() {
        val task = gamificationDao.getDailyTaskOnce(today) ?: return
        if (task.weightLogged && task.allMealsLogged && task.lessonCompleted) {
            val awarded = gamificationDao.markCoinAwarded(today)
            if (awarded > 0) {
                userRepository.addCoins(1)
            }
        }
    }
}
