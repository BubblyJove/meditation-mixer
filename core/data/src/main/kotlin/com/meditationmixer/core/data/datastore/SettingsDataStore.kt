package com.meditationmixer.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.model.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore
    
    val settings: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            reminderEnabled = preferences[REMINDER_ENABLED] ?: false,
            reminderTimeHour = preferences[REMINDER_HOUR] ?: 22,
            reminderTimeMinute = preferences[REMINDER_MINUTE] ?: 0,
            fadeDurationSeconds = preferences[FADE_DURATION] ?: Constants.DEFAULT_FADE_SECONDS,
            defaultTimerMinutes = preferences[DEFAULT_TIMER] ?: Constants.DEFAULT_TIMER_MINUTES,
            bedtimeWindowStart = preferences[BEDTIME_START] ?: Constants.DEFAULT_BEDTIME_START,
            bedtimeWindowEnd = preferences[BEDTIME_END] ?: Constants.DEFAULT_BEDTIME_END,
            minCreditMinutes = preferences[MIN_CREDIT] ?: Constants.MIN_CREDIT_MINUTES
        )
    }
    
    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED] = enabled
        }
    }
    
    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[REMINDER_HOUR] = hour
            preferences[REMINDER_MINUTE] = minute
        }
    }
    
    suspend fun setFadeDuration(seconds: Int) {
        dataStore.edit { preferences ->
            preferences[FADE_DURATION] = seconds
        }
    }
    
    suspend fun setDefaultTimer(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_TIMER] = minutes
        }
    }
    
    suspend fun setBedtimeWindow(startHour: Int, endHour: Int) {
        dataStore.edit { preferences ->
            preferences[BEDTIME_START] = startHour
            preferences[BEDTIME_END] = endHour
        }
    }
    
    companion object {
        private val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        private val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        private val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        private val FADE_DURATION = intPreferencesKey("fade_duration")
        private val DEFAULT_TIMER = intPreferencesKey("default_timer")
        private val BEDTIME_START = intPreferencesKey("bedtime_start")
        private val BEDTIME_END = intPreferencesKey("bedtime_end")
        private val MIN_CREDIT = intPreferencesKey("min_credit")
    }
}
