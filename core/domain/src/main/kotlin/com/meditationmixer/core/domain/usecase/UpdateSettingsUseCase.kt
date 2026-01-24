package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setReminderEnabled(enabled: Boolean) {
        settingsRepository.setReminderEnabled(enabled)
    }
    
    suspend fun setReminderTime(hour: Int, minute: Int) {
        settingsRepository.setReminderTime(hour, minute)
    }
    
    suspend fun setFadeDuration(seconds: Int) {
        settingsRepository.setFadeDuration(seconds)
    }
    
    suspend fun setDefaultTimer(minutes: Int) {
        settingsRepository.setDefaultTimer(minutes)
    }
}
