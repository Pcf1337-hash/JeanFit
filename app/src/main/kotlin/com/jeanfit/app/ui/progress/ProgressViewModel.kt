package com.jeanfit.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.WeightEntry
import com.jeanfit.app.data.repository.UserRepository
import com.jeanfit.app.data.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProgressUiState(
    val latestWeight: WeightEntry? = null,
    val goalWeightKg: Float = 70f,
    val startWeightKg: Float = 80f,
    val entries: List<WeightEntry> = emptyList(),
    val showAddDialog: Boolean = false,
    val inputWeight: String = "",
    val inputNote: String = "",
    val isSaving: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                weightRepository.getAllEntries(),
                weightRepository.getLatestEntry(),
                userRepository.getProfile()
            ) { entries, latest, profile ->
                _state.update {
                    it.copy(
                        entries = entries,
                        latestWeight = latest,
                        goalWeightKg = profile?.goalWeightKg ?: 70f,
                        startWeightKg = profile?.startWeightKg ?: 80f,
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true, inputWeight = it.latestWeight?.weightKg?.toString() ?: "") }
    fun hideAddDialog() = _state.update { it.copy(showAddDialog = false, inputWeight = "", inputNote = "") }
    fun setInputWeight(w: String) = _state.update { it.copy(inputWeight = w) }
    fun setInputNote(n: String) = _state.update { it.copy(inputNote = n) }

    fun saveWeight() {
        val kg = _state.value.inputWeight.toFloatOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            weightRepository.addEntry(kg, note = _state.value.inputNote.ifBlank { null })
            _state.update { it.copy(isSaving = false, showAddDialog = false, inputWeight = "", inputNote = "") }
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { weightRepository.deleteEntry(id) }
    }
}
