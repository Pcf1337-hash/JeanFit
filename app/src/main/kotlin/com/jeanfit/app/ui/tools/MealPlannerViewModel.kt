package com.jeanfit.app.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.MealPlan
import com.jeanfit.app.data.db.entities.Recipe
import com.jeanfit.app.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MealPlannerUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val mealPlans: List<MealPlan> = emptyList(),
    val allRecipes: List<Recipe> = emptyList(),
    val isPickerOpen: Boolean = false,
    val pickerMealType: String = ""
)

@HiltViewModel
class MealPlannerViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isPickerOpen = MutableStateFlow(false)
    private val _pickerMealType = MutableStateFlow("")

    val uiState: StateFlow<MealPlannerUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date ->
            recipeRepository.getMealPlanForDay(date.toEpochDay())
        },
        recipeRepository.getAllRecipes(),
        _isPickerOpen,
        _pickerMealType
    ) { date, plans, recipes, pickerOpen, pickerMealType ->
        MealPlannerUiState(
            selectedDate = date,
            mealPlans = plans,
            allRecipes = recipes,
            isPickerOpen = pickerOpen,
            pickerMealType = pickerMealType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MealPlannerUiState())

    fun nextDay() { _selectedDate.value = _selectedDate.value.plusDays(1) }
    fun prevDay() { _selectedDate.value = _selectedDate.value.minusDays(1) }

    fun openPicker(mealType: String) {
        _pickerMealType.value = mealType
        _isPickerOpen.value = true
    }

    fun closePicker() { _isPickerOpen.value = false }

    fun assignRecipe(recipeId: String) {
        val date = _selectedDate.value
        val mealType = _pickerMealType.value
        viewModelScope.launch {
            recipeRepository.upsertMealPlan(MealPlan(
                dateEpochDay = date.toEpochDay(),
                mealType = mealType,
                recipeId = recipeId,
                customMealName = null
            ))
        }
        closePicker()
    }

    fun removeMealPlan(mealType: String) {
        viewModelScope.launch {
            recipeRepository.deleteMealPlan(_selectedDate.value.toEpochDay(), mealType)
        }
    }
}
