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
            LayerConfig(
                type = LayerType.valueOf(fields[0]),
                sourceUri = fields[1].takeIf { it.isNotEmpty() },
                assetId = fields[2].takeIf { it.isNotEmpty() },
                volume = fields[3].toFloat(),
                loop = fields[4].toBoolean(),
                frequency = fields[5].takeIf { it.isNotEmpty() }?.toFloat(),
                startOffsetMs = fields[6].toLong()
            )
        }
    }
    
    companion object {
        private const val LAYER_SEPARATOR = "|||"
        private const val FIELD_SEPARATOR = ":::"
    }
}
