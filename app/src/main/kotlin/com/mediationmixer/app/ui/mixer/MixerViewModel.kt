package com.mediationmixer.app.ui.mixer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.usecase.PreviewToneUseCase
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
    private val savePresetUseCase: SavePresetUseCase,
    private val previewTone: PreviewToneUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    fun setToneFrequency(frequency: Float) {
        _uiState.update { it.copy(toneFrequency = frequency.coerceIn(1f, 40f)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, frequency = frequency)

            if (_uiState.value.isTonePreviewing) {
                previewTone(frequencyHz = _uiState.value.toneFrequency, volume = _uiState.value.toneVolume)
            }
        }
    }

    fun toggleToneEnabled() {
        val newEnabled = !_uiState.value.toneEnabled
        _uiState.update { it.copy(toneEnabled = newEnabled) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, enabled = newEnabled)
        }
    }

    fun setToneVolume(volume: Float) {
        _uiState.update { it.copy(toneVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, volume = volume)

            if (_uiState.value.isTonePreviewing) {
                previewTone(frequencyHz = _uiState.value.toneFrequency, volume = _uiState.value.toneVolume)
            }
        }
    }

    fun toggleTonePreview() {
        val shouldPreview = !_uiState.value.isTonePreviewing
        _uiState.update { it.copy(isTonePreviewing = shouldPreview) }

        viewModelScope.launch {
            if (shouldPreview) {
                runCatching {
                    previewTone(frequencyHz = _uiState.value.toneFrequency, volume = _uiState.value.toneVolume)
                }.onFailure {
                    _uiState.update { it.copy(isTonePreviewing = false, tonePreviewError = "Failed to preview tone") }
                }
            } else {
                previewTone.stop()
            }
        }
    }

    fun stopTonePreview() {
        _uiState.update { it.copy(isTonePreviewing = false) }
        viewModelScope.launch {
            previewTone.stop()
        }
    }

    fun consumeTonePreviewError() {
        _uiState.update { it.copy(tonePreviewError = null) }
    }

    fun setUserAudioVolume(volume: Float) {
        _uiState.update { it.copy(userAudioVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, volume = volume)
        }
    }

    fun toggleUserAudioEnabled() {
        val newEnabled = !_uiState.value.userAudioEnabled
        _uiState.update { it.copy(userAudioEnabled = newEnabled) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, enabled = newEnabled)
        }
    }

    fun toggleUserAudioLoop() {
        val newLoop = !_uiState.value.userAudioLoop
        _uiState.update { it.copy(userAudioLoop = newLoop) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, loop = newLoop)
        }
    }

    fun setUserAudioRepeatDelayMs(delayMs: Long) {
        val safe = delayMs.coerceAtLeast(0L)
        _uiState.update { it.copy(userAudioRepeatDelayMs = safe) }
        viewModelScope.launch {
            updateLayer(LayerType.USER_AUDIO, startOffsetMs = safe)
        }
    }

    fun setAmbienceVolume(volume: Float) {
        _uiState.update { it.copy(ambienceVolume = volume.coerceIn(0f, 1f)) }
        viewModelScope.launch {
            updateLayer(LayerType.AMBIENCE, volume = volume)
        }
    }

    fun toggleAmbienceEnabled() {
        val newEnabled = !_uiState.value.ambienceEnabled
        _uiState.update { it.copy(ambienceEnabled = newEnabled) }
        viewModelScope.launch {
            updateLayer(LayerType.AMBIENCE, enabled = newEnabled)
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

    fun dismissFilePicker() {
        _uiState.update { it.copy(showFilePicker = false) }
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

    fun dismissAmbiencePicker() {
        _uiState.update { it.copy(showAmbiencePicker = false) }
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
                        enabled = _uiState.value.toneEnabled,
                        volume = _uiState.value.toneVolume,
                        loop = true,
                        frequency = _uiState.value.toneFrequency
                    ),
                    LayerConfig(
                        type = LayerType.USER_AUDIO,
                        enabled = _uiState.value.userAudioEnabled,
                        sourceUri = _uiState.value.userAudioUri,
                        volume = _uiState.value.userAudioVolume,
                        loop = _uiState.value.userAudioLoop,
                        startOffsetMs = _uiState.value.userAudioRepeatDelayMs
                    ),
                    LayerConfig(
                        type = LayerType.AMBIENCE,
                        enabled = _uiState.value.ambienceEnabled,
                        assetId = _uiState.value.ambienceAssetId,
                        volume = _uiState.value.ambienceVolume,
                        loop = _uiState.value.ambienceLoop
                    )
                ),
                timerDurationMs = 30 * 60 * 1000L,
                fadeOutDurationMs = 30 * 1000L
            )

            runCatching {
                savePresetUseCase(preset)
            }.onSuccess {
                _uiState.update { it.copy(presetSaved = true, presetSaveError = null) }
            }.onFailure {
                _uiState.update { it.copy(presetSaved = false, presetSaveError = "Failed to save preset") }
            }
        }
    }

    fun consumePresetSaveFeedback() {
        _uiState.update { it.copy(presetSaved = false, presetSaveError = null) }
    }
}

data class MixerUiState(
    val presetName: String = "New Mix",
    val isFavorite: Boolean = false,
    
    // Tone layer
    val toneFrequency: Float = 6f,
    val toneVolume: Float = 0.5f,
    val toneEnabled: Boolean = true,
    val isTonePreviewing: Boolean = false,
    val tonePreviewError: String? = null,
    
    // User audio layer
    val userAudioUri: String? = null,
    val userAudioName: String? = null,
    val userAudioVolume: Float = 0.7f,
    val userAudioLoop: Boolean = true,
    val userAudioEnabled: Boolean = true,
    val userAudioRepeatDelayMs: Long = 0,
    
    // Ambience layer
    val ambienceAssetId: String? = "rain_light",
    val ambienceName: String? = "Light Rain",
    val ambienceVolume: Float = 0.4f,
    val ambienceLoop: Boolean = true,
    val ambienceEnabled: Boolean = true,
    
    // UI state
    val showFilePicker: Boolean = false,
    val showAmbiencePicker: Boolean = false,
    val presetSaved: Boolean = false,
    val presetSaveError: String? = null
)
