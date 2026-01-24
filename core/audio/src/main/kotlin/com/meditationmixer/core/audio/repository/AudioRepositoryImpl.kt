package com.meditationmixer.core.audio.repository

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.meditationmixer.core.audio.engine.AudioEngine
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
        volume: Float?,
        loop: Boolean?,
        sourceUri: String?,
        assetId: String?,
        frequency: Float?
    ) {
        val currentConfig = layerConfigs[type]?.value ?: LayerConfig(type = type)
        val updatedConfig = currentConfig.copy(
            volume = volume ?: currentConfig.volume,
            loop = loop ?: currentConfig.loop,
            sourceUri = sourceUri ?: currentConfig.sourceUri,
            assetId = assetId ?: currentConfig.assetId,
            frequency = frequency ?: currentConfig.frequency
        )
        layerConfigs[type]?.value = updatedConfig
        
        // Apply changes to audio engine
        volume?.let { audioEngine.setLayerVolume(type, it) }
        loop?.let { audioEngine.setLayerLoop(type, it) }
        frequency?.let { 
            if (type == LayerType.TONE) {
                audioEngine.updateToneFrequency(it)
            }
        }
    }
    
    override suspend fun previewAmbience(assetPath: String) {
        stopPreview()
        
        previewPlayer = ExoPlayer.Builder(context).build().apply {
            val uri = "asset:///$assetPath"
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
        }
    }
    
    override suspend fun stopPreview() {
        previewPlayer?.stop()
        previewPlayer?.release()
        previewPlayer = null
    }
}
