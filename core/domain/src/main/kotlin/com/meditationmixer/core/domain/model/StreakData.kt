package com.meditationmixer.core.domain.model

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSessions: Int = 0,
    val lastSessionDate: Long? = null
)

data class AchievementData(
    val firstNight: Boolean = false,
    val weekender: Boolean = false,
    val builder: Boolean = false,
    val importer: Boolean = false,
    val consistent: Boolean = false
)
