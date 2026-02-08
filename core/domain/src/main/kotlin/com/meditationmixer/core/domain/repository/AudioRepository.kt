package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    fun getMasterVolume(): Flow<Float>
    fun getLayerConfig(type: LayerType): Flow<LayerConfig>
    suspend fun setMasterVolume(volume: Float)
    suspend fun updateLayer(
        type: LayerType,
        enabled: Boolean? = null,
        volume: Float? = null,
        loop: Boolean? = null,
        sourceUri: String? = null,
        assetId: String? = null,
        frequency: Float? = null,
        startOffsetMs: Long? = null,
        binaural: Boolean? = null
    )
    suspend fun previewAmbience(assetPath: String)
    suspend fun stopPreview()

    suspend fun previewTone(frequencyHz: Float, volume: Float)
    suspend fun stopTonePreview()

    suspend fun generateToneWav(
        outputStream: OutputStream,
        durationSeconds: Int,
        frequencyHz: Float,
        volume: Float,
        binaural: Boolean
    )
}
