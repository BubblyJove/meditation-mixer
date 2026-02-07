package com.meditationmixer.core.audio.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.meditationmixer.core.audio.engine.AudioEngine
import com.meditationmixer.core.audio.engine.NoiseGenerator
import com.meditationmixer.core.audio.engine.ToneGenerator
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioEngine: AudioEngine
) : AudioRepository {
    
    private val _masterVolume = MutableStateFlow(1.0f)
    private var previewPlayer: ExoPlayer? = null
    private val previewToneGenerator = ToneGenerator()
    private val previewNoiseGenerator = NoiseGenerator()
    
    private val layerConfigs = mutableMapOf<LayerType, MutableStateFlow<LayerConfig>>()
    
    init {
        LayerType.entries.forEach { type ->
            layerConfigs[type] = MutableStateFlow(
                LayerConfig(type = type)
            )
        }
    }
    
    override fun getMasterVolume(): Flow<Float> = _masterVolume.asStateFlow()
    
    override fun getLayerConfig(type: LayerType): Flow<LayerConfig> {
        return layerConfigs[type]?.asStateFlow() 
            ?: MutableStateFlow(LayerConfig(type = type)).asStateFlow()
    }
    
    override suspend fun setMasterVolume(volume: Float) {
        _masterVolume.value = volume.coerceIn(0f, 1f)
        audioEngine.setMasterVolume(_masterVolume.value)
    }
    
    override suspend fun updateLayer(
        type: LayerType,
        enabled: Boolean?,
        volume: Float?,
        loop: Boolean?,
        sourceUri: String?,
        assetId: String?,
        frequency: Float?,
        startOffsetMs: Long?,
        binaural: Boolean?
    ) {
        val currentConfig = layerConfigs[type]?.value ?: LayerConfig(type = type)
        val updatedConfig = currentConfig.copy(
            enabled = enabled ?: currentConfig.enabled,
            volume = volume ?: currentConfig.volume,
            loop = loop ?: currentConfig.loop,
            sourceUri = sourceUri ?: currentConfig.sourceUri,
            assetId = assetId ?: currentConfig.assetId,
            frequency = frequency ?: currentConfig.frequency,
            startOffsetMs = startOffsetMs ?: currentConfig.startOffsetMs,
            binaural = binaural ?: currentConfig.binaural
        )
        layerConfigs[type]?.value = updatedConfig

        // Apply changes to audio engine
        enabled?.let { audioEngine.setLayerEnabled(type, it) }
        volume?.let { audioEngine.setLayerVolume(type, it) }
        loop?.let { audioEngine.setLayerLoop(type, it) }
        if (sourceUri != null || assetId != null) {
            audioEngine.updateLayerSource(type = type, sourceUri = sourceUri, assetId = assetId)
        }
        startOffsetMs?.let { audioEngine.setLayerStartOffset(type, it) }
        frequency?.let {
            if (type == LayerType.TONE) {
                audioEngine.updateToneFrequency(it)
            }
        }
        binaural?.let {
            if (type == LayerType.TONE) {
                audioEngine.setToneBinaural(it)
            }
        }
    }
    
    override suspend fun previewAmbience(assetPath: String) {
        stopPreview()

        // Extract asset ID from path (e.g., "ambience/rain_light.ogg" -> "rain_light")
        val assetId = assetPath
            .substringAfterLast("/")
            .substringBeforeLast(".")
            .ifEmpty { assetPath }

        val hasAsset = runCatching { context.assets.open(assetPath).close() }.isSuccess
        if (hasAsset) {
            previewPlayer = ExoPlayer.Builder(context).build().apply {
                val uri = "asset:///$assetPath"
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                play()
            }
        } else {
            previewNoiseGenerator.setProfileFromAssetId(assetId)
            previewNoiseGenerator.setVolume(0.25f)
            previewNoiseGenerator.start()
        }
    }
    
    override suspend fun stopPreview() {
        previewPlayer?.stop()
        previewPlayer?.release()
        previewPlayer = null
        previewNoiseGenerator.stop()
    }

    override suspend fun previewTone(frequencyHz: Float, volume: Float) {
        previewToneGenerator.setFrequency(frequencyHz)
        previewToneGenerator.setVolume(volume)
        previewToneGenerator.start()
    }

    override suspend fun stopTonePreview() {
        previewToneGenerator.stop()
    }
}
