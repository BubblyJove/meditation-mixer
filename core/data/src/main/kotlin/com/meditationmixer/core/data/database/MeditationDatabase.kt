package com.meditationmixer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MeditationDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
    abstract fun sessionHistoryDao(): SessionHistoryDao
    abstract fun streakDao(): StreakDao

    companion object {
        const val DATABASE_NAME = "meditation_mixer_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_presets_lastUsedAt` ON `presets` (`lastUsedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_presets_isFavorite` ON `presets` (`isFavorite`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_history_startedAt` ON `session_history` (`startedAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_history_completed` ON `session_history` (`completed`)")
            }
        }
    }
}
