package com.meditationmixer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.meditationmixer.core.data.database.entity.PresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    
    @Query("SELECT * FROM presets ORDER BY lastUsedAt DESC")
    fun getAllPresets(): Flow<List<PresetEntity>>
    
    @Query("SELECT * FROM presets WHERE id = :id")
    fun getPresetById(id: Long): Flow<PresetEntity?>
    
    @Query("SELECT * FROM presets WHERE isFavorite = 1 ORDER BY lastUsedAt DESC")
    fun getFavoritePresets(): Flow<List<PresetEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long
    
    @Update
    suspend fun updatePreset(preset: PresetEntity)
    
    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
    
    @Query("UPDATE presets SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
    
    @Query("UPDATE presets SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long)
}
