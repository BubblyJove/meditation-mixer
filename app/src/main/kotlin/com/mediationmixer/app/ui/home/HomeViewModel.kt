package com.mediationmixer.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.model.SessionState
import com.meditationmixer.core.domain.usecase.GetCurrentSessionUseCase
import com.meditationmixer.core.domain.usecase.GetPresetsUseCase
import com.meditationmixer.core.domain.usecase.PlayPauseSessionUseCase
import com.meditationmixer.core.domain.usecase.SeekSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentSession: GetCurrentSessionUseCase,
    private val getPresets: GetPresetsUseCase,
    private val playPauseSession: PlayPauseSessionUseCase,
    private val seekSession: SeekSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var presets: List<Preset> = emptyList()
    private var currentPresetIndex = 0

    init {
        loadPresets()
        observeSession()
    }

    private fun loadPresets() {
        viewModelScope.launch {
            getPresets().collect { loadedPresets ->
                presets = loadedPresets
                if (loadedPresets.isNotEmpty() && _uiState.value.currentPreset == null) {
                    _uiState.update { it.copy(currentPreset = loadedPresets.first()) }
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            getCurrentSession().collect { session ->
                _uiState.update { state ->
                    state.copy(
                        isPlaying = session.state == SessionState.PLAYING,
                        progress = session.progress,
                        elapsedFormatted = formatTime(session.elapsedMs),
                        remainingFormatted = formatTime(session.remainingMs)
                    )
                }
            }
        }
    }

    fun onPlayPause() {
        viewModelScope.launch {
            _uiState.value.currentPreset?.let { preset ->
                playPauseSession(preset)
            }
        }
    }

    fun onSeek(progress: Float) {
        viewModelScope.launch {
            seekSession(progress)
        }
    }

    fun onPreviousPreset() {
        if (presets.isEmpty()) return
        currentPresetIndex = if (currentPresetIndex > 0) {
            currentPresetIndex - 1
        } else {
            presets.lastIndex
        }
        _uiState.update { it.copy(currentPreset = presets[currentPresetIndex]) }
    }

    fun onNextPreset() {
        if (presets.isEmpty()) return
        currentPresetIndex = if (currentPresetIndex < presets.lastIndex) {
            currentPresetIndex + 1
        } else {
            0
        }
        _uiState.update { it.copy(currentPreset = presets[currentPresetIndex]) }
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

data class HomeUiState(
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val elapsedFormatted: String = "00:00",
    val remainingFormatted: String = "00:00",
    val currentPreset: Preset? = null,
    val streakCount: Int = 0
)
