package com.mediationmixer.app.ui.streaks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.usecase.GetAchievementsUseCase
import com.meditationmixer.core.domain.usecase.GetStreakDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StreaksViewModel @Inject constructor(
    private val getStreakData: GetStreakDataUseCase,
    private val getAchievements: GetAchievementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreaksUiState())
    val uiState: StateFlow<StreaksUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getStreakData().collect { streakData ->
                _uiState.update {
                    it.copy(
                        currentStreak = streakData.currentStreak,
                        longestStreak = streakData.longestStreak,
                        totalSessions = streakData.totalSessions
                    )
                }
            }
        }

        viewModelScope.launch {
            getAchievements().collect { achievementData ->
                val achievements = listOf(
                    Achievement(
                        id = "first_night",
                        name = "First Night",
                        description = "Complete your first session",
                        icon = Icons.Default.NightsStay,
                        isUnlocked = achievementData.firstNight
                    ),
                    Achievement(
                        id = "weekender",
                        name = "Weekender",
                        description = "Complete 7 sessions",
                        icon = Icons.Default.EmojiEvents,
                        isUnlocked = achievementData.weekender
                    ),
                    Achievement(
                        id = "builder",
                        name = "Builder",
                        description = "Save your first preset",
                        icon = Icons.Default.Tune,
                        isUnlocked = achievementData.builder
                    ),
                    Achievement(
                        id = "importer",
                        name = "Importer",
                        description = "Import your first audio",
                        icon = Icons.Default.Upload,
                        isUnlocked = achievementData.importer
                    ),
                    Achievement(
                        id = "consistent",
                        name = "Consistent",
                        description = "3 nights in a row",
                        icon = Icons.Default.Star,
                        isUnlocked = achievementData.consistent
                    )
                )
                _uiState.update { it.copy(achievements = achievements) }
            }
        }
    }
}

data class StreaksUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSessions: Int = 0,
    val achievements: List<Achievement> = emptyList()
)
