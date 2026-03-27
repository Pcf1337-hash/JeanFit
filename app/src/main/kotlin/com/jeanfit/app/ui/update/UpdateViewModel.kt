package com.jeanfit.app.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeanfit.app.domain.update.AppUpdateManager
import com.jeanfit.app.domain.update.ReleaseInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateUiState(
    val release: ReleaseInfo? = null,
    val isVisible: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val isChecking: Boolean = false,
    val lastCheckText: String = "Noch nie"
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: AppUpdateManager
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateUiState())
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        _state.update { it.copy(lastCheckText = updateManager.formatLastCheck()) }
        // Auto-Check beim Start — 3s Delay damit die App erst lädt
        viewModelScope.launch {
            delay(3000)
            checkForUpdateIfDue()
        }
    }

    /** Automatischer Check — nur wenn Cooldown abgelaufen */
    private suspend fun checkForUpdateIfDue() {
        if (!updateManager.shouldCheckForUpdate()) return
        performCheck()
    }

    /** Manueller Check — immer ausführen */
    fun checkForUpdateManually() {
        viewModelScope.launch { performCheck() }
    }

    private suspend fun performCheck() {
        _state.update { it.copy(isChecking = true, error = null) }
        val (hasUpdate, release) = updateManager.checkForUpdate()
        _state.update { it.copy(
            isChecking = false,
            lastCheckText = updateManager.formatLastCheck()
        )}

        if (!hasUpdate || release == null) return
        if (updateManager.isVersionSkipped(release.versionName)) return

        _state.update { it.copy(release = release, isVisible = true) }
    }

    fun startDownloadAndInstall() {
        val release = _state.value.release ?: return
        if (release.downloadUrl.isBlank()) {
            _state.update { it.copy(error = "Keine Download-URL gefunden") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isDownloading = true, downloadProgress = 0, error = null) }
            val apkFile = try {
                updateManager.downloadApkWithProgress(release.downloadUrl) { progress ->
                    _state.update { it.copy(downloadProgress = progress) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isDownloading = false,
                    error = "Download fehlgeschlagen: ${e.message}"
                )}
                return@launch
            }
            _state.update { it.copy(isDownloading = false) }
            try {
                updateManager.installApk(apkFile)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Installation fehlgeschlagen: ${e.message}") }
            }
        }
    }

    fun dismiss() {
        if (!_state.value.isDownloading) {
            _state.update { it.copy(isVisible = false) }
        }
    }

    fun skipVersion() {
        val version = _state.value.release?.versionName ?: return
        updateManager.skipVersion(version)
        _state.update { it.copy(isVisible = false, release = null) }
    }

    fun getCurrentVersion(): String = updateManager.getCurrentVersionName()
}
