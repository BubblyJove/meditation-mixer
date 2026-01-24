package com.mediationmixer.app.ui.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.usecase.SavePresetUseCase
import com.meditationmixer.core.domain.usecase.UpdateLayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MixerViewModel @Inject constructor(
    private val updateLayer: UpdateLayerUseCase,
    private val savePresetUseCase: SavePresetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    fun setToneFrequency(frequency: Float) {
        _uiState.update { it.copy(toneFrequency = frequency.coerceIn(1f, 40f)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, frequency = frequency)
        }
    }

    fun setToneVolume(volume: Float) {
        _uiState.update { it.copy(toneVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, volume = volume)
        }
    }

    fun setUserAudioVolume(volume: Float) {
        _uiState.update { it.copy(userAudioVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, volume = volume)
        }
    }

    fun toggleUserAudioLoop() {
        val newLoop = !_uiState.value.userAudioLoop
        _uiState.update { it.copy(userAudioLoop = newLoop) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, loop = newLoop)
        }
    }

    fun setAmbienceVolume(volume: Float) {
        _uiState.update { it.copy(ambienceVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.AMBIENCE, volume = volume)
        }
    }

    fun toggleAmbienceLoop() {
        val newLoop = !_uiState.value.ambienceLoop
        _uiState.update { it.copy(ambienceLoop = newLoop) }
        viewModelScope.launch {
            updateLayer(LayerType.AMBIENCE, loop = newLoop)
        }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun importUserAudio() {
        // This would trigger file picker via Activity result
        // For now, just update state to show intent
        _uiState.update { it.copy(showFilePicker = true) }
    }

    fun onUserAudioSelected(uri: String, name: String) {
        _uiState.update {
            it.copy(
                userAudioUri = uri,
                userAudioName = name,
                showFilePicker = false
            )
        }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, sourceUri = uri)
        }
    }

    fun selectAmbience() {
        _uiState.update { it.copy(showAmbiencePicker = true) }
    }

    fun onAmbienceSelected(assetId: String, name: String) {
        _uiState.update {
            it.copy(
                ambienceAssetId = assetId,
                ambienceName = name,
                showAmbiencePicker = false
            )
        }
        viewModelScope.launch {
            updateLayer(LayerType.AMBIENCE, assetId = assetId)
        }
    }

    fun savePreset() {
        viewModelScope.launch {
            val preset = Preset(
                id = 0,
                name = _uiState.value.presetName,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis(),
                layers = listOf(
                    LayerConfig(
                        type = LayerType.TONE,
                        volume = _uiState.value.toneVolume,
                        loop = true,
                        frequency = _uiState.value.toneFrequency
                    ),
                    LayerConfig(
                        type = LayerType.USER_AUDIO,
                        sourceUri = _uiState.value.userAudioUri,
                        volume = _uiState.value.userAudioVolume,
                        loop = _uiState.value.userAudioLoop
                    ),
                    LayerConfig(
                        type = LayerType.AMBIENCE,
                        assetId = _uiState.value.ambienceAssetId,
                        volume = _uiState.value.ambienceVolume,
                        loop = _uiState.value.ambienceLoop
                    )
                ),
                timerDurationMs = 30 * 60 * 1000L,
                fadeOutDurationMs = 30 * 1000L
            )
            savePresetUseCase(preset)
            _uiState.update { it.copy(presetSaved = true) }
        }
    }
}

data class MixerUiState(
    val presetName: String = "New Mix",
    val isFavorite: Boolean = false,
    
    // Tone layer
    val toneFrequency: Float = 6f,
    val toneVolume: Float = 0.5f,
    
    // User audio layer
    val userAudioUri: String? = null,
    val userAudioName: String? = null,
    val userAudioVolume: Float = 0.7f,
    val userAudioLoop: Boolean = true,
    
    // Ambience layer
    val ambienceAssetId: String? = "rain_light",
    val ambienceName: String? = "Light Rain",
    val ambienceVolume: Float = 0.4f,
    val ambienceLoop: Boolean = true,
    
    // UI state
    val showFilePicker: Boolean = false,
    val showAmbiencePicker: Boolean = false,
    val presetSaved: Boolean = false
)
