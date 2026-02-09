package com.meditationmixer.core.data.database

import androidx.room.TypeConverter
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.ToneMode

class Converters {

    @TypeConverter
    fun fromLayerConfigList(layers: List<LayerConfig>): String {
        return layers.joinToString(LAYER_SEPARATOR) { layer ->
            listOf(
                layer.type.name,
                layer.enabled.toString(),
                layer.sourceUri ?: "",
                layer.assetId ?: "",
                layer.volume.toString(),
                layer.loop.toString(),
                layer.frequency?.toString() ?: "",
                layer.startOffsetMs.toString(),
                layer.binaural.toString(),
                layer.toneMode.name,
                layer.carrierFrequency.toString(),
                layer.modulationDepth.toString()
            ).joinToString(FIELD_SEPARATOR)
        }
    }

    @TypeConverter
    fun toLayerConfigList(value: String): List<LayerConfig> {
        if (value.isEmpty()) return emptyList()

        return value.split(LAYER_SEPARATOR).map { layerString ->
            val fields = layerString.split(FIELD_SEPARATOR)

            val type = LayerType.valueOf(fields[0])

            val enabledField = fields.getOrNull(1)
            val enabledParsed = enabledField
                ?.takeIf { it.equals("true", ignoreCase = true) || it.equals("false", ignoreCase = true) }
                ?.toBoolean()
            val hasEnabled = enabledParsed != null

            val enabled = enabledParsed ?: true

            val baseOffset = if (hasEnabled) 0 else -1
            val sourceUri = fields.getOrNull(2 + baseOffset)?.takeIf { it.isNotEmpty() }
            val assetId = fields.getOrNull(3 + baseOffset)?.takeIf { it.isNotEmpty() }
            val volume = fields.getOrNull(4 + baseOffset)?.toFloatOrNull() ?: 0.5f
            val loop = fields.getOrNull(5 + baseOffset)?.toBoolean() ?: true
            val frequency = fields.getOrNull(6 + baseOffset)?.takeIf { it.isNotEmpty() }?.toFloatOrNull()
            val startOffsetMs = fields.getOrNull(7 + baseOffset)?.toLongOrNull() ?: 0L
            val binaural = fields.getOrNull(8 + baseOffset)?.toBoolean() ?: false

            // New fields (9-11): toneMode, carrierFrequency, modulationDepth
            val toneModeStr = fields.getOrNull(9 + baseOffset)
            val toneMode = toneModeStr?.let {
                runCatching { ToneMode.valueOf(it) }.getOrNull()
            } ?: if (binaural) ToneMode.BINAURAL else ToneMode.AM

            val carrierFrequency = fields.getOrNull(10 + baseOffset)?.toFloatOrNull() ?: 200f
            val modulationDepth = fields.getOrNull(11 + baseOffset)?.toFloatOrNull() ?: 0.4f

            LayerConfig(
                type = type,
                enabled = enabled,
                sourceUri = sourceUri,
                assetId = assetId,
                volume = volume,
                loop = loop,
                frequency = frequency,
                startOffsetMs = startOffsetMs,
                binaural = binaural,
                toneMode = toneMode,
                carrierFrequency = carrierFrequency,
                modulationDepth = modulationDepth
            )
        }
    }

    companion object {
        private const val LAYER_SEPARATOR = "|||"
        private const val FIELD_SEPARATOR = ":::"
    }
}
