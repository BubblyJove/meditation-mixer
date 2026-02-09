package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.ToneMode
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
        binaural: Boolean? = null,
        toneMode: ToneMode? = null,
        carrierFrequency: Float? = null,
        modulationDepth: Float? = null
    )
    suspend fun previewAmbience(assetPath: String)
    suspend fun stopPreview()

    suspend fun previewTone(
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode = ToneMode.AM,
        carrierFrequency: Float = 200f,
        modulationDepth: Float = 0.4f
    )
    suspend fun stopTonePreview()

    suspend fun generateToneWav(
        outputStream: OutputStream,
        durationSeconds: Int,
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode = ToneMode.AM,
        carrierFrequency: Float = 200f,
        modulationDepth: Float = 0.4f
    )
}
