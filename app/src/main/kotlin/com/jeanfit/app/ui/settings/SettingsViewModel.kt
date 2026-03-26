package com.jeanfit.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.UserProfile
import com.jeanfit.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class SettingsUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    // Profil
    val name: String = "",
    val goalWeightKg: String = "",
    val heightCm: String = "",
    val activityLevel: String = "lightly_active",
    val dailyCalorieGoal: String = "",
    val goalKgPerWeek: Float = 0.5f,
    // Ziele
    val waterGoalLiter: String = "2.0",
    val stepsGoal: String = "8000",
    // App
    val showCaloriesOnBadge: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getProfile().filterNotNull().first()
            _state.update {
                it.copy(
                    profile = profile,
                    isLoading = false,
                    name = profile.name,
                    goalWeightKg = formatFloat(profile.goalWeightKg),
                    heightCm = profile.heightCm.toInt().toString(),
                    activityLevel = profile.activityLevel,
                    dailyCalorieGoal = profile.dailyCalorieGoal.toString()
                )
            }
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v, isSaved = false) }
    fun setGoalWeight(v: String) = _state.update { it.copy(goalWeightKg = v, isSaved = false) }
    fun setHeight(v: String) = _state.update { it.copy(heightCm = v, isSaved = false) }
    fun setActivityLevel(v: String) = _state.update { it.copy(activityLevel = v, isSaved = false) }
    fun setCalorieGoal(v: String) = _state.update { it.copy(dailyCalorieGoal = v, isSaved = false) }
    fun setGoalKgPerWeek(v: Float) = _state.update { it.copy(goalKgPerWeek = v, isSaved = false) }
    fun setWaterGoal(v: String) = _state.update { it.copy(waterGoalLiter = v, isSaved = false) }
    fun setStepsGoal(v: String) = _state.update { it.copy(stepsGoal = v, isSaved = false) }

    fun recalculateCalories() {
        val profile = _state.value.profile ?: return
        val height = _state.value.heightCm.toFloatOrNull() ?: profile.heightCm
        val age = run {
            val birth = LocalDate.ofEpochDay(profile.birthDateEpochDay)
            java.time.Period.between(birth, LocalDate.now()).years
        }
        val cal = calculateDailyCalories(
            gender = profile.gender,
            weightKg = profile.startWeightKg,
            heightCm = height,
            ageYears = age,
            activityLevel = _state.value.activityLevel,
            weeklyGoalKgLoss = _state.value.goalKgPerWeek
        )
        _state.update { it.copy(dailyCalorieGoal = cal.toString(), isSaved = false) }
    }

    fun save() {
        val current = _state.value.profile ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = _state.value.name.trim().ifBlank { current.name },
                goalWeightKg = _state.value.goalWeightKg.toFloatOrNull() ?: current.goalWeightKg,
                heightCm = _state.value.heightCm.toFloatOrNull() ?: current.heightCm,
                activityLevel = _state.value.activityLevel,
                dailyCalorieGoal = _state.value.dailyCalorieGoal.toIntOrNull() ?: current.dailyCalorieGoal
            )
            userRepository.saveProfile(updated)
            _state.update { it.copy(profile = updated, isSaved = true) }
        }
    }

    private fun formatFloat(f: Float): String {
        val s = f.toString()
        return if (s.endsWith(".0")) s.dropLast(2) else s
    }

    private fun calculateDailyCalories(
        gender: String, weightKg: Float, heightCm: Float,
        ageYears: Int, activityLevel: String, weeklyGoalKgLoss: Float
    ): Int {
        val bmr = when (gender) {
            "male" -> 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * ageYears)
            "female" -> 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * ageYears)
            else -> 500.0 + (11.0 * weightKg) + (4.0 * heightCm) - (5.0 * ageYears)
        }
        val activityFactor = when (activityLevel) {
            "sedentary" -> 1.2
            "lightly_active" -> 1.375
            "active" -> 1.55
            "very_active" -> 1.725
            else -> 1.2
        }
        val tdee = bmr * activityFactor
        val deficit = weeklyGoalKgLoss * 7700 / 7
        return maxOf(1200, (tdee - deficit).roundToInt())
    }
}
