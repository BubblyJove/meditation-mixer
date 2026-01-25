package com.meditationmixer.core.data.database

import androidx.room.TypeConverter
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType

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
                layer.startOffsetMs.toString()
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

            val sourceUri = fields.getOrNull(if (hasEnabled) 2 else 1)?.takeIf { it.isNotEmpty() }
            val assetId = fields.getOrNull(if (hasEnabled) 3 else 2)?.takeIf { it.isNotEmpty() }
            val volume = fields.getOrNull(if (hasEnabled) 4 else 3)?.toFloatOrNull() ?: 0.5f
            val loop = fields.getOrNull(if (hasEnabled) 5 else 4)?.toBoolean() ?: true
            val frequency = fields.getOrNull(if (hasEnabled) 6 else 5)?.takeIf { it.isNotEmpty() }?.toFloatOrNull()
            val startOffsetMs = fields.getOrNull(if (hasEnabled) 7 else 6)?.toLongOrNull() ?: 0L

            LayerConfig(
                type = type,
                enabled = enabled,
                sourceUri = sourceUri,
                assetId = assetId,
                volume = volume,
                loop = loop,
                frequency = frequency,
                startOffsetMs = startOffsetMs
            )
        }
    }
    
    companion object {
        private const val LAYER_SEPARATOR = "|||"
        private const val FIELD_SEPARATOR = ":::"
    }
}
