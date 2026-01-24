package com.meditationmixer.core.data.repository

import com.meditationmixer.core.data.datastore.SettingsDataStore
import com.meditationmixer.core.domain.model.Settings
import com.meditationmixer.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    
    override fun getSettings(): Flow<Settings> {
        return settingsDataStore.settings
    }
    
    override suspend fun setReminderEnabled(enabled: Boolean) {
        settingsDataStore.setReminderEnabled(enabled)
    }
    
    override suspend fun setReminderTime(hour: Int, minute: Int) {
        settingsDataStore.setReminderTime(hour, minute)
    }
    
    override suspend fun setFadeDuration(seconds: Int) {
        settingsDataStore.setFadeDuration(seconds)
    }
    
    override suspend fun setDefaultTimer(minutes: Int) {
        settingsDataStore.setDefaultTimer(minutes)
    }
    
    override suspend fun setBedtimeWindow(startHour: Int, endHour: Int) {
        settingsDataStore.setBedtimeWindow(startHour, endHour)
    }
}
