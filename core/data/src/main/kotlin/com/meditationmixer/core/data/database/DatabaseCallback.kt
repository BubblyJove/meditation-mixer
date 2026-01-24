package com.meditationmixer.core.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        CoroutineScope(Dispatchers.IO).launch {
            populateDefaultData(db)
        }
    }
    
    private fun populateDefaultData(db: SupportSQLiteDatabase) {
        val converters = Converters()
        val now = System.currentTimeMillis()
        
        // Sleep Starter preset
        val sleepStarterLayers = listOf(
            LayerConfig(
                type = LayerType.TONE,
                volume = 0.3f,
                loop = true,
                frequency = Constants.FrequencyPresets.DELTA_2HZ
            ),
            LayerConfig(
                type = LayerType.AMBIENCE,
                assetId = "rain_light",
                volume = 0.5f,
                loop = true
            )
        )

        insertPreset(
            db = db,
            name = "Sleep Starter",
            createdAt = now,
            lastUsedAt = now,
            layers = converters.fromLayerConfigList(sleepStarterLayers),
            timerDurationMs = 30 * 60 * 1000L,
            fadeOutDurationMs = 30 * 1000L,
            isFavorite = true
        )
        
        // Deep Relaxation preset
        val deepRelaxationLayers = listOf(
            LayerConfig(
                type = LayerType.TONE,
                volume = 0.4f,
                loop = true,
                frequency = Constants.FrequencyPresets.THETA_6HZ
            ),
            LayerConfig(
                type = LayerType.AMBIENCE,
                assetId = "ocean_waves",
                volume = 0.4f,
                loop = true
            )
        )

        insertPreset(
            db = db,
            name = "Deep Relaxation",
            createdAt = now,
            lastUsedAt = now - 1000,
            layers = converters.fromLayerConfigList(deepRelaxationLayers),
            timerDurationMs = 60 * 60 * 1000L,
            fadeOutDurationMs = 30 * 1000L,
            isFavorite = false
        )
        
        // Focus Mode preset
        val focusModeLayers = listOf(
            LayerConfig(
                type = LayerType.TONE,
                volume = 0.25f,
                loop = true,
                frequency = Constants.FrequencyPresets.ALPHA_10HZ
            ),
            LayerConfig(
                type = LayerType.AMBIENCE,
                assetId = "forest_night",
                volume = 0.3f,
                loop = true
            )
        )

        insertPreset(
            db = db,
            name = "Focus Mode",
            createdAt = now,
            lastUsedAt = now - 2000,
            layers = converters.fromLayerConfigList(focusModeLayers),
            timerDurationMs = 45 * 60 * 1000L,
            fadeOutDurationMs = 15 * 1000L,
            isFavorite = false
        )
        
        // Initialize streak data
        db.execSQL(
            "INSERT OR REPLACE INTO streak_data (id, currentStreak, longestStreak, totalSessions, lastSessionDate) VALUES (?, ?, ?, ?, ?)",
            arrayOf(1, 0, 0, 0, null)
        )
        
        // Initialize achievements
        val achievements = listOf(
            Constants.Achievements.FIRST_NIGHT,
            Constants.Achievements.WEEKENDER,
            Constants.Achievements.BUILDER,
            Constants.Achievements.IMPORTER,
            Constants.Achievements.CONSISTENT
        )
        
        achievements.forEach { achievementId ->
            db.execSQL(
                "INSERT OR REPLACE INTO achievements (id, isUnlocked, unlockedAt) VALUES (?, ?, ?)",
                arrayOf(achievementId, 0, null)
            )
        }
    }

    private fun insertPreset(
        db: SupportSQLiteDatabase,
        name: String,
        createdAt: Long,
        lastUsedAt: Long,
        layers: String,
        timerDurationMs: Long,
        fadeOutDurationMs: Long,
        isFavorite: Boolean
    ) {
        db.execSQL(
            "INSERT INTO presets (name, createdAt, lastUsedAt, layers, timerDurationMs, fadeOutDurationMs, isFavorite) VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf(
                name,
                createdAt,
                lastUsedAt,
                layers,
                timerDurationMs,
                fadeOutDurationMs,
                if (isFavorite) 1 else 0
            )
        )
    }
}
