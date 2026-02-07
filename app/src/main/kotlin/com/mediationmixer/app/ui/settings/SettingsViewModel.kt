package com.mediationmixer.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.usecase.GetSettingsUseCase
import com.meditationmixer.core.domain.usecase.UpdateSettingsUseCase
import com.mediationmixer.app.service.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettings().collect { settings ->
                _uiState.update {
                    it.copy(
                        reminderEnabled = settings.reminderEnabled,
                        reminderHour = settings.reminderTimeHour,
                        reminderMinute = settings.reminderTimeMinute,
                        fadeDuration = settings.fadeDurationSeconds,
                        defaultTimerMinutes = settings.defaultTimerMinutes,
                        version = "1.0.0"
                    )
                }
            }
        }
    }

    fun toggleReminder() {
        val newValue = !_uiState.value.reminderEnabled
        _uiState.update { it.copy(reminderEnabled = newValue) }
        viewModelScope.launch {
            updateSettings.setReminderEnabled(newValue)
            if (newValue) {
                reminderScheduler.schedule(_uiState.value.reminderHour, _uiState.value.reminderMinute)
            } else {
                reminderScheduler.cancel()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(reminderHour = hour, reminderMinute = minute, showTimePicker = false) }
        viewModelScope.launch {
            updateSettings.setReminderTime(hour, minute)
            if (_uiState.value.reminderEnabled) {
                reminderScheduler.schedule(hour, minute)
            }
        }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun dismissTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun setFadeDuration(seconds: Int) {
        _uiState.update { it.copy(fadeDuration = seconds) }
        viewModelScope.launch {
            updateSettings.setFadeDuration(seconds)
        }
    }

    fun setDefaultTimer(minutes: Int) {
        _uiState.update { it.copy(defaultTimerMinutes = minutes) }
        viewModelScope.launch {
            updateSettings.setDefaultTimer(minutes)
        }
    }

    fun onSupportClick() {
        _uiState.update { it.copy(showDonationDialog = true) }
    }

    fun dismissDonationDialog() {
        _uiState.update { it.copy(showDonationDialog = false) }
    }
}

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 22,
    val reminderMinute: Int = 0,
    val showTimePicker: Boolean = false,
    val fadeDuration: Int = Constants.DEFAULT_FADE_SECONDS,
    val defaultTimerMinutes: Int = Constants.DEFAULT_TIMER_MINUTES,
    val version: String = "",
    val showDonationDialog: Boolean = false
)
