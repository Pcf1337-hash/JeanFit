package com.jeanfit.app.ui.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FoodSearchUiState(
    val query: String = "",
    val localResults: List<FoodItem> = emptyList(),
    val remoteResults: List<FoodItem> = emptyList(),
    val recentItems: List<FoodItem> = emptyList(),
    val selectedItem: FoodItem? = null,
    val servingSize: Float = 100f,
    val isSearching: Boolean = false,
    val isLogging: Boolean = false,
    val logSuccess: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FoodSearchUiState())
    val state = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            foodRepository.getRecentItems()
                .collect { items -> _state.update { it.copy(recentItems = items) } }
        }
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .collectLatest { q ->
                    _state.update { it.copy(isSearching = true) }
                    val remote = foodRepository.searchRemote(q)
                    foodRepository.searchLocal(q)
                        .collect { local ->
                            _state.update { it.copy(localResults = local, remoteResults = remote, isSearching = false) }
                        }
                }
        }
    }

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
        queryFlow.value = q
        if (q.isBlank()) _state.update { it.copy(localResults = emptyList(), remoteResults = emptyList()) }
    }

    fun selectItem(item: FoodItem) {
        _state.update { it.copy(selectedItem = item, servingSize = item.defaultServingSizeG) }
    }

    fun setServingSize(g: Float) = _state.update { it.copy(servingSize = g) }

    fun clearSelection() = _state.update { it.copy(selectedItem = null) }

    fun logFood(mealType: String) {
        val item = _state.value.selectedItem ?: return
        val serving = _state.value.servingSize
        viewModelScope.launch {
            _state.update { it.copy(isLogging = true) }
            val factor = serving / 100f
            val entry = FoodLogEntry(
                foodId = item.foodId,
                foodName = item.name,
                mealType = mealType,
                servingMultiplier = serving / item.defaultServingSizeG,
                servingSizeG = serving,
                calories = item.caloriesPer100g * factor,
                protein = item.proteinPer100g * factor,
                carbs = item.carbsPer100g * factor,
                fat = item.fatPer100g * factor,
                colorCategory = item.colorCategory,
                logDateEpochDay = LocalDate.now().toEpochDay()
            )
            foodRepository.logEntry(entry)
            _state.update { it.copy(isLogging = false, logSuccess = true, selectedItem = null) }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }
            val item = foodRepository.getByBarcode(barcode)
            if (item != null) {
                _state.update { it.copy(selectedItem = item, servingSize = item.defaultServingSizeG, isSearching = false) }
            } else {
                _state.update { it.copy(isSearching = false, error = "Produkt nicht gefunden. Bitte manuell suchen.") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun addCustomFood(name: String, caloriesPer100g: Float, protein: Float, carbs: Float, fat: Float) {
        viewModelScope.launch {
            val item = FoodItem(
                foodId = java.util.UUID.randomUUID().toString(),
                name = name,
                caloriesPer100g = caloriesPer100g,
                proteinPer100g = protein,
                carbsPer100g = carbs,
                fatPer100g = fat,
                defaultServingSizeG = 100f,
                colorCategory = com.jeanfit.app.domain.usecase.CalcColorCategoryUseCase().calculate(caloriesPer100g),
                calorieDensity = caloriesPer100g / 100f,
                source = "custom"
            )
            foodRepository.insertFoodItem(item)
            selectItem(item)
        }
    }
}
