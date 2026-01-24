package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.Preset
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun getAllPresets(): Flow<List<Preset>>
    fun getPresetById(id: Long): Flow<Preset?>
    fun getFavoritePresets(): Flow<List<Preset>>
    suspend fun savePreset(preset: Preset): Long
    suspend fun updatePreset(preset: Preset)
    suspend fun deletePreset(id: Long)
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    suspend fun updateLastUsed(id: Long)
}
