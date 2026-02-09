package com.meditationmixer.core.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.ToneMode
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

        data class PresetDef(
            val name: String,
            val beatHz: Float,
            val toneMode: ToneMode,
            val carrier: Float,
            val depth: Float,
            val toneVolume: Float,
            val ambienceId: String?,
            val ambienceVolume: Float,
            val timerMinutes: Int,
            val fadeSec: Int,
            val isFavorite: Boolean
        )

        val presets = listOf(
            PresetDef("Delta - Deep Sleep", 1.5f, ToneMode.AM, 220f, 0.40f, 0.30f, "rain_light", 0.5f, 30, 30, false),
            PresetDef("Delta - Heavy Sleep", 2.5f, ToneMode.AM, 200f, 0.45f, 0.30f, "rain_heavy", 0.5f, 30, 30, false),
            PresetDef("Theta - Calm", 5.0f, ToneMode.AM, 230f, 0.45f, 0.35f, "ocean_waves", 0.4f, 30, 30, false),
            PresetDef("Theta - Dreamy", 6.0f, ToneMode.AM, 230f, 0.45f, 0.40f, "ocean_waves", 0.4f, 30, 30, true),
            PresetDef("Alpha - Relax", 8.5f, ToneMode.AM, 220f, 0.40f, 0.30f, "wind_soft", 0.4f, 20, 20, false),
            PresetDef("Alpha - Flow", 10.0f, ToneMode.AM, 240f, 0.40f, 0.30f, "forest_night", 0.35f, 20, 20, false),
            PresetDef("Beta - Focus", 14.0f, ToneMode.AM, 240f, 0.30f, 0.25f, "forest_night", 0.3f, 15, 20, false),
            PresetDef("Beta - Work Mode", 18.0f, ToneMode.AM, 260f, 0.25f, 0.25f, "river_stream", 0.3f, 15, 20, false),
            PresetDef("Gamma - 40 Hz", 40.0f, ToneMode.AM, 300f, 0.20f, 0.20f, null, 0f, 10, 10, false)
        )

        presets.forEachIndexed { index, p ->
            val layers = mutableListOf(
                LayerConfig(
                    type = LayerType.TONE,
                    volume = p.toneVolume,
                    loop = true,
                    frequency = p.beatHz,
                    toneMode = p.toneMode,
                    carrierFrequency = p.carrier,
                    modulationDepth = p.depth
                )
            )
            if (p.ambienceId != null) {
                layers.add(
                    LayerConfig(
                        type = LayerType.AMBIENCE,
                        assetId = p.ambienceId,
                        volume = p.ambienceVolume,
                        loop = true
                    )
                )
            }

            insertPreset(
                db = db,
                name = p.name,
                createdAt = now,
                lastUsedAt = now - index * 1000L,
                layers = converters.fromLayerConfigList(layers),
                timerDurationMs = p.timerMinutes.toLong() * 60 * 1000L,
                fadeOutDurationMs = p.fadeSec.toLong() * 1000L,
                isFavorite = p.isFavorite
            )
        }

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
