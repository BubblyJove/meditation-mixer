package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<Settings>
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setReminderTime(hour: Int, minute: Int)
    suspend fun setFadeDuration(seconds: Int)
    suspend fun setDefaultTimer(minutes: Int)
    suspend fun setBedtimeWindow(startHour: Int, endHour: Int)
}
