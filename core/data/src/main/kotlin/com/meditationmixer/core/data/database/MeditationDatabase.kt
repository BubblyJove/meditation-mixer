package com.meditationmixer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meditationmixer.core.data.database.dao.PresetDao
import com.meditationmixer.core.data.database.dao.SessionHistoryDao
import com.meditationmixer.core.data.database.dao.StreakDao
import com.meditationmixer.core.data.database.entity.PresetEntity
import com.meditationmixer.core.data.database.entity.SessionHistoryEntity
import com.meditationmixer.core.data.database.entity.StreakEntity
import com.meditationmixer.core.data.database.entity.AchievementEntity

@Database(
    entities = [
        PresetEntity::class,
        SessionHistoryEntity::class,
        StreakEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MeditationDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
    abstract fun sessionHistoryDao(): SessionHistoryDao
    abstract fun streakDao(): StreakDao
    
    companion object {
        const val DATABASE_NAME = "meditation_mixer_db"
    }
}
