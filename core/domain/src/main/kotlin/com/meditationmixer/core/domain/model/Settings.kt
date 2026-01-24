package com.meditationmixer.core.domain.model

data class Settings(
    val reminderEnabled: Boolean = false,
    val reminderTimeHour: Int = 22,
    val reminderTimeMinute: Int = 0,
    val fadeDurationSeconds: Int = 30,
    val defaultTimerMinutes: Int = 30,
    val bedtimeWindowStart: Int = 20,
    val bedtimeWindowEnd: Int = 6,
    val minCreditMinutes: Int = 10
)
