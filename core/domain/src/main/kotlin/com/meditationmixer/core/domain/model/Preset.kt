package com.meditationmixer.core.domain.model

data class Preset(
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val lastUsedAt: Long,
    val layers: List<LayerConfig>,
    val timerDurationMs: Long = 30 * 60 * 1000L,
    val fadeOutDurationMs: Long = 30 * 1000L,
    val isFavorite: Boolean = false
)

data class LayerConfig(
    val type: LayerType,
    val enabled: Boolean = true,
    val sourceUri: String? = null,
    val assetId: String? = null,
    val volume: Float = 0.5f,
    val loop: Boolean = true,
    val frequency: Float? = null,
    val startOffsetMs: Long = 0,
    val binaural: Boolean = false,
    val toneMode: ToneMode = ToneMode.AM,
    val carrierFrequency: Float = 200f,
    val modulationDepth: Float = 0.4f
)

enum class LayerType {
    TONE,
    USER_AUDIO,
    AMBIENCE
}

enum class ToneMode {
    AM,
    ISOCHRONIC,
    BINAURAL,
    MONAURAL
}
