package com.jeanfit.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.data.db.entities.CoachMessage
import com.jeanfit.app.data.repository.CoachRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoachUiState(
    val messages: List<CoachMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val coachRepository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    val unreadCount: StateFlow<Int> = coachRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        observeMessages()
        sendGreetingIfEmpty()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            coachRepository.getChatHistory().collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun sendGreetingIfEmpty() {
        viewModelScope.launch {
            // Nur beim ersten Mal (keine Nachrichten vorhanden)
            val current = _uiState.value.messages
            if (current.isEmpty()) {
                coachRepository.sendGreeting()
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val input = _uiState.value.inputText.trim()
        if (input.isBlank() || _uiState.value.isLoading) return

        _uiState.update { it.copy(inputText = "", isLoading = true, error = null) }

        viewModelScope.launch {
            coachRepository.sendMessage(input).collect { reply ->
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendQuickReply(text: String) {
        _uiState.update { it.copy(inputText = text) }
        sendMessage()
    }

    fun markAllRead() {
        viewModelScope.launch { coachRepository.markAllRead() }
    }

    fun clearChat() {
        viewModelScope.launch {
            coachRepository.clearChat()
            // Danach neuen Gruß senden
            coachRepository.sendGreeting()
        }
    }
}
