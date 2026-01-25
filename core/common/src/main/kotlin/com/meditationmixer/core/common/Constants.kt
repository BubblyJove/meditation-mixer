package com.meditationmixer.core.common

object Constants {
    // Session thresholds
    const val MIN_CREDIT_MINUTES = 10
    const val DEFAULT_TIMER_MINUTES = 60
    const val DEFAULT_FADE_SECONDS = 30
    
    // Timer presets in minutes
    val TIMER_PRESETS = listOf(15, 30, 60, 90, 120, 180)
    
    // Frequency ranges (Hz)
    const val MIN_FREQUENCY = 1f
    const val MAX_FREQUENCY = 40f
    
    // Frequency presets
    object FrequencyPresets {
        const val DELTA_2HZ = 2f
        const val THETA_4HZ = 4f
        const val THETA_6HZ = 6f
        const val ALPHA_10HZ = 10f
        const val BETA_20HZ = 20f
    }
    
    // Audio
    const val SAMPLE_RATE = 44100
    const val AUDIO_BUFFER_SIZE = 4096
    
    // Bedtime window defaults (24h format)
    const val DEFAULT_BEDTIME_START = 20 // 8 PM
    const val DEFAULT_BEDTIME_END = 6 // 6 AM
    
    // Notifications
    const val NOTIFICATION_ID_PLAYBACK = 1001
    
    // Achievements
    object Achievements {
        const val FIRST_NIGHT = "first_night"
        const val WEEKENDER = "weekender"
        const val BUILDER = "builder"
        const val IMPORTER = "importer"
        const val CONSISTENT = "consistent"
    }
    
    // Session requirements
    const val WEEKENDER_SESSIONS = 7
    const val CONSISTENT_STREAK = 3
}
