package com.mediationmixer.app.ui.mixer

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.model.ToneMode
import com.meditationmixer.core.domain.usecase.GetSettingsUseCase
import com.meditationmixer.core.domain.usecase.PreviewToneUseCase
import com.meditationmixer.core.domain.usecase.SavePresetUseCase
import com.meditationmixer.core.domain.usecase.SaveToneUseCase
import com.meditationmixer.core.domain.usecase.UpdateLayerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MixerViewModel @Inject constructor(
    private val updateLayer: UpdateLayerUseCase,
    private val savePresetUseCase: SavePresetUseCase,
    private val getSettings: GetSettingsUseCase,
    private val previewTone: PreviewToneUseCase,
    private val saveToneUseCase: SaveToneUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MixerUiState())
    val uiState: StateFlow<MixerUiState> = _uiState.asStateFlow()

    fun setToneFrequency(frequency: Float) {
        _uiState.update { it.copy(toneFrequency = frequency.coerceIn(Constants.MIN_FREQUENCY, Constants.MAX_BEAT_FREQUENCY)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, frequency = frequency)
            if (_uiState.value.isTonePreviewing) {
                previewCurrentTone()
            }
        }
    }

    fun setToneMode(mode: ToneMode) {
        _uiState.update { it.copy(toneMode = mode) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, toneMode = mode)
            if (_uiState.value.isTonePreviewing) {
                previewCurrentTone()
            }
        }
    }

    fun setCarrierFrequency(hz: Float) {
        _uiState.update { it.copy(carrierFrequency = hz.coerceIn(Constants.MIN_CARRIER_FREQUENCY, Constants.MAX_CARRIER_FREQUENCY)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, carrierFrequency = hz)
            if (_uiState.value.isTonePreviewing) {
                previewCurrentTone()
            }
        }
    }

    fun setModulationDepth(depth: Float) {
        _uiState.update { it.copy(modulationDepth = depth.coerceIn(Constants.MIN_MODULATION_DEPTH, Constants.MAX_MODULATION_DEPTH)) }
        viewModelScope.launch {
            updateLayer(LayerType.TONE, modulationDepth = depth)
            if (_uiState.value.isTonePreviewing) {
                previewCurrentTone()
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
                previewCurrentTone()
            }
        }
    }

    private suspend fun previewCurrentTone() {
        val state = _uiState.value
        previewTone(
            frequencyHz = state.toneFrequency,
            volume = state.toneVolume,
            toneMode = state.toneMode,
            carrierFrequency = state.carrierFrequency,
            modulationDepth = state.modulationDepth
        )
    }

    fun toggleTonePreview() {
        val shouldPreview = !_uiState.value.isTonePreviewing
        _uiState.update { it.copy(isTonePreviewing = shouldPreview) }

        viewModelScope.launch {
            if (shouldPreview) {
                runCatching {
                    previewCurrentTone()
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

    fun getToneSaveFilename(): String {
        val state = _uiState.value
        val freqLabel = state.toneFrequency.toInt()
        val mode = state.toneMode.name.lowercase()
        return "tone_${freqLabel}hz_$mode.wav"
    }

    fun saveToneToUri(context: Context, uri: Uri) {
        _uiState.update { it.copy(isSavingTone = true, toneSaveSuccess = false, toneSaveError = null) }
        viewModelScope.launch {
            val state = _uiState.value
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    saveToneUseCase(
                        outputStream = stream,
                        durationSeconds = 30,
                        frequencyHz = state.toneFrequency,
                        volume = state.toneVolume,
                        toneMode = state.toneMode,
                        carrierFrequency = state.carrierFrequency,
                        modulationDepth = state.modulationDepth
                    )
                } ?: throw IllegalStateException("Could not open output stream")
            }.onSuccess {
                _uiState.update { it.copy(isSavingTone = false, toneSaveSuccess = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSavingTone = false, toneSaveError = "Failed to save: ${e.message}") }
            }
        }
    }

    fun consumeToneSaveFeedback() {
        _uiState.update { it.copy(toneSaveSuccess = false, toneSaveError = null) }
    }

    fun savePreset() {
        viewModelScope.launch {
            val settings = getSettings().first()
            val timerMs = settings.defaultTimerMinutes.toLong() * 60 * 1000L
            val fadeOutMs = settings.fadeDurationSeconds.toLong() * 1000L
            val state = _uiState.value

            val preset = Preset(
                id = 0,
                name = state.presetName,
                createdAt = System.currentTimeMillis(),
                lastUsedAt = System.currentTimeMillis(),
                layers = listOf(
                    LayerConfig(
                        type = LayerType.TONE,
                        enabled = state.toneEnabled,
                        volume = state.toneVolume,
                        loop = true,
                        frequency = state.toneFrequency,
                        toneMode = state.toneMode,
                        carrierFrequency = state.carrierFrequency,
                        modulationDepth = state.modulationDepth
                    ),
                    LayerConfig(
                        type = LayerType.USER_AUDIO,
                        enabled = state.userAudioEnabled,
                        sourceUri = state.userAudioUri,
                        volume = state.userAudioVolume,
                        loop = state.userAudioLoop,
                        startOffsetMs = state.userAudioRepeatDelayMs
                    ),
                    LayerConfig(
                        type = LayerType.AMBIENCE,
                        enabled = state.ambienceEnabled,
                        assetId = state.ambienceAssetId,
                        volume = state.ambienceVolume,
                        loop = state.ambienceLoop
                    )
                ),
                timerDurationMs = timerMs,
                fadeOutDurationMs = fadeOutMs
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
    val toneMode: ToneMode = ToneMode.AM,
    val carrierFrequency: Float = Constants.DEFAULT_CARRIER_FREQUENCY,
    val modulationDepth: Float = Constants.DEFAULT_MODULATION_DEPTH,
    val isTonePreviewing: Boolean = false,
    val tonePreviewError: String? = null,
    val isSavingTone: Boolean = false,
    val toneSaveSuccess: Boolean = false,
    val toneSaveError: String? = null,

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
