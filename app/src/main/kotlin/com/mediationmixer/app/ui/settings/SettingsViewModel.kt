package com.mediationmixer.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.usecase.GetSettingsUseCase
import com.meditationmixer.core.domain.usecase.UpdateSettingsUseCase
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
    private val updateSettings: UpdateSettingsUseCase
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
                        fadeDuration = settings.fadeDurationSeconds,
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
        }
    }

    fun setFadeDuration(seconds: Int) {
        _uiState.update { it.copy(fadeDuration = seconds) }
        viewModelScope.launch {
            updateSettings.setFadeDuration(seconds)
        }
    }

    fun onSupportClick() {
        // Open donation link - handled by the UI layer
        _uiState.update { it.copy(showDonationDialog = true) }
    }

    fun dismissDonationDialog() {
        _uiState.update { it.copy(showDonationDialog = false) }
    }
}

data class SettingsUiState(
    val reminderEnabled: Boolean = false,
    val fadeDuration: Int = 30,
    val version: String = "",
    val showDonationDialog: Boolean = false
)
