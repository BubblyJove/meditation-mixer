package com.meditationmixer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.Preset

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val lastUsedAt: Long,
    val layers: List<LayerConfig>,
    val timerDurationMs: Long,
    val fadeOutDurationMs: Long,
    val isFavorite: Boolean = false
) {
    fun toDomain(): Preset = Preset(
        id = id,
        name = name,
        createdAt = createdAt,
        lastUsedAt = lastUsedAt,
        layers = layers,
        timerDurationMs = timerDurationMs,
        fadeOutDurationMs = fadeOutDurationMs,
        isFavorite = isFavorite
    )
    
    companion object {
        fun fromDomain(preset: Preset): PresetEntity = PresetEntity(
            id = preset.id,
            name = preset.name,
            createdAt = preset.createdAt,
            lastUsedAt = preset.lastUsedAt,
            layers = preset.layers,
            timerDurationMs = preset.timerDurationMs,
            fadeOutDurationMs = preset.fadeOutDurationMs,
            isFavorite = preset.isFavorite
        )
    }
}
