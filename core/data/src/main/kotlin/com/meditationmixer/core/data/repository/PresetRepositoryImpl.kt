package com.meditationmixer.core.data.repository

import com.meditationmixer.core.data.database.dao.PresetDao
import com.meditationmixer.core.data.database.entity.PresetEntity
import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepositoryImpl @Inject constructor(
    private val presetDao: PresetDao
) : PresetRepository {
    
    override fun getAllPresets(): Flow<List<Preset>> {
        return presetDao.getAllPresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPresetById(id: Long): Flow<Preset?> {
        return presetDao.getPresetById(id).map { it?.toDomain() }
    }
    
    override fun getFavoritePresets(): Flow<List<Preset>> {
        return presetDao.getFavoritePresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun savePreset(preset: Preset): Long {
        return presetDao.insertPreset(PresetEntity.fromDomain(preset))
    }
    
    override suspend fun updatePreset(preset: Preset) {
        presetDao.updatePreset(PresetEntity.fromDomain(preset))
    }
    
    override suspend fun deletePreset(id: Long) {
        presetDao.deletePreset(id)
    }
    
    override suspend fun setFavorite(id: Long, isFavorite: Boolean) {
        presetDao.setFavorite(id, isFavorite)
    }
    
    override suspend fun updateLastUsed(id: Long) {
        presetDao.updateLastUsed(id, System.currentTimeMillis())
    }
}
