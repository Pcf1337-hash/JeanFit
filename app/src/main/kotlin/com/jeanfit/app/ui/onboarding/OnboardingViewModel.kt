package com.jeanfit.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.dao.UserProfileDao
import com.jeanfit.app.data.db.entities.UserProfile
import com.jeanfit.app.domain.usecase.CalcCaloriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingState(
    val name: String = "",
    val email: String = "",
    val goalWeightKg: Float = 70f,
    val currentWeightKg: Float = 80f,
    val gender: String = "prefer_not",
    val birthDate: LocalDate = LocalDate.now().minusYears(30),
    val heightCm: Float = 170f,
    val activityLevel: String = "lightly_active",
    val healthConditions: List<String> = emptyList(),
    val motivation: String = "",
    val bigPicture: String = "",
    val calculatedCalories: Int = 0,
    val isSaving: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val calcCaloriesUseCase: CalcCaloriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state = _state.asStateFlow()

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setEmail(email: String) = _state.update { it.copy(email = email) }
    fun setGoalWeight(kg: Float) = _state.update { it.copy(goalWeightKg = kg) }
    fun setCurrentWeight(kg: Float) = _state.update { it.copy(currentWeightKg = kg) }
    fun setGender(gender: String) = _state.update { it.copy(gender = gender) }
    fun setBirthDate(date: LocalDate) = _state.update { it.copy(birthDate = date) }
    fun setHeight(cm: Float) = _state.update { it.copy(heightCm = cm) }
    fun setActivityLevel(level: String) = _state.update { it.copy(activityLevel = level) }
    fun toggleHealthCondition(condition: String) = _state.update { s ->
        val list = s.healthConditions.toMutableList()
        if (condition in list) list.remove(condition) else list.add(condition)
        s.copy(healthConditions = list)
    }
    fun setMotivation(motivation: String) = _state.update { it.copy(motivation = motivation) }
    fun setBigPicture(text: String) = _state.update { it.copy(bigPicture = text) }

    fun calculateCalories() {
        val s = _state.value
        val age = LocalDate.now().year - s.birthDate.year
        val calories = calcCaloriesUseCase.calculate(
            gender = s.gender,
            weightKg = s.currentWeightKg,
            heightCm = s.heightCm,
            ageYears = age,
            activityLevel = s.activityLevel
        )
        _state.update { it.copy(calculatedCalories = calories) }
    }

    fun saveProfile(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val profile = UserProfile(
                id = 1,
                name = s.name.ifBlank { "JeanFit User" },
                email = s.email.ifBlank { null },
                gender = s.gender,
                birthDateEpochDay = s.birthDate.toEpochDay(),
                heightCm = s.heightCm,
                startWeightKg = s.currentWeightKg,
                goalWeightKg = s.goalWeightKg,
                activityLevel = s.activityLevel,
                dailyCalorieGoal = s.calculatedCalories,
                programStartDate = LocalDate.now().toEpochDay(),
                noomCoins = 0,
                onboardingCompleted = true,
                motivationText = s.motivation,
                bigPicture = s.bigPicture
            )
            userProfileDao.insertOrReplace(profile)
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}
