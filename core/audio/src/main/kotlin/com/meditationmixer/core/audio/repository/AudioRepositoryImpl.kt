package com.meditationmixer.core.audio.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.meditationmixer.core.audio.engine.AudioEngine
import com.meditationmixer.core.audio.engine.NoiseGenerator
import com.meditationmixer.core.audio.engine.ToneGenerator
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.ToneMode
import com.meditationmixer.core.domain.repository.AudioRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.OutputStream
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
        binaural: Boolean?,
        toneMode: ToneMode?,
        carrierFrequency: Float?,
        modulationDepth: Float?
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
            binaural = binaural ?: currentConfig.binaural,
            toneMode = toneMode ?: currentConfig.toneMode,
            carrierFrequency = carrierFrequency ?: currentConfig.carrierFrequency,
            modulationDepth = modulationDepth ?: currentConfig.modulationDepth
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
        toneMode?.let {
            if (type == LayerType.TONE) {
                audioEngine.setToneMode(it)
            }
        }
        carrierFrequency?.let {
            if (type == LayerType.TONE) {
                audioEngine.setCarrierFrequency(it)
            }
        }
        modulationDepth?.let {
            if (type == LayerType.TONE) {
                audioEngine.setModulationDepth(it)
            }
        }
    }

    override suspend fun previewAmbience(assetPath: String) {
        stopPreview()

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

    override suspend fun previewTone(
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode,
        carrierFrequency: Float,
        modulationDepth: Float
    ) {
        previewToneGenerator.setFrequency(frequencyHz)
        previewToneGenerator.setVolume(volume)
        previewToneGenerator.setToneMode(toneMode)
        previewToneGenerator.setCarrierFrequency(carrierFrequency)
        previewToneGenerator.setModulationDepth(modulationDepth)
        previewToneGenerator.start()
    }

    override suspend fun stopTonePreview() {
        previewToneGenerator.stop()
    }

    override suspend fun generateToneWav(
        outputStream: OutputStream,
        durationSeconds: Int,
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode,
        carrierFrequency: Float,
        modulationDepth: Float
    ) {
        withContext(Dispatchers.IO) {
            val generator = ToneGenerator()
            generator.generateWavToStream(
                outputStream = outputStream,
                durationSeconds = durationSeconds,
                frequencyHz = frequencyHz,
                volume = volume,
                toneMode = toneMode,
                carrierFreqHz = carrierFrequency,
                modulationDepth = modulationDepth
            )
        }
    }
}
