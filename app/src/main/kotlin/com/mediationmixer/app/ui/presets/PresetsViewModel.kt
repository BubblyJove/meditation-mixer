package com.mediationmixer.app.ui.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.usecase.GetCurrentSessionUseCase
import com.meditationmixer.core.domain.usecase.GetPresetsUseCase
import com.meditationmixer.core.domain.usecase.SetFavoritePresetUseCase
import com.meditationmixer.core.domain.usecase.StartSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresetsViewModel @Inject constructor(
    private val getPresets: GetPresetsUseCase,
    private val getCurrentSession: GetCurrentSessionUseCase,
    private val startSession: StartSessionUseCase,
    private val setFavoritePreset: SetFavoritePresetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PresetsUiState())
    val uiState: StateFlow<PresetsUiState> = _uiState.asStateFlow()

    init {
        observePresets()
        observeSession()
    }

    private fun observePresets() {
        viewModelScope.launch {
            getPresets().collect { presets ->
                _uiState.update { it.copy(presets = presets) }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            getCurrentSession().collect { session ->
                _uiState.update { it.copy(playingPresetId = session.presetId) }
            }
        }
    }

    fun playPreset(preset: Preset) {
        viewModelScope.launch {
            startSession(preset)
        }
    }

    fun toggleFavorite(preset: Preset) {
        viewModelScope.launch {
            setFavoritePreset(presetId = preset.id, isFavorite = !preset.isFavorite)
        }
    }
}

data class PresetsUiState(
    val presets: List<Preset> = emptyList(),
    val playingPresetId: Long? = null
)
