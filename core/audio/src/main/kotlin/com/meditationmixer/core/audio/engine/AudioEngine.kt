package com.meditationmixer.core.audio.engine

import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.Preset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AudioEngine {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    
    suspend fun loadPreset(preset: Preset)
    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    suspend fun seekTo(positionMs: Long)
    
    suspend fun setMasterVolume(volume: Float)
    suspend fun setLayerVolume(type: LayerType, volume: Float)
    suspend fun setLayerLoop(type: LayerType, loop: Boolean)
    suspend fun setLayerEnabled(type: LayerType, enabled: Boolean)
    suspend fun setLayerStartOffset(type: LayerType, startOffsetMs: Long)
    suspend fun updateLayerSource(type: LayerType, sourceUri: String?, assetId: String?)
    suspend fun updateToneFrequency(frequencyHz: Float)
    suspend fun setToneBinaural(enabled: Boolean)

    suspend fun fadeOut(durationMs: Long)
    
    fun release()
}
