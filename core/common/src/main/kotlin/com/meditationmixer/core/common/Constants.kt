package com.meditationmixer.core.common

object Constants {
    // Session thresholds
    const val MIN_CREDIT_MINUTES = 10
    const val DEFAULT_TIMER_MINUTES = 60
    const val DEFAULT_FADE_SECONDS = 30
    
    // Timer presets in minutes
    val TIMER_PRESETS = listOf(15, 30, 60, 90, 120, 180)
    
    // Beat frequency ranges (Hz) — entrainment frequency
    const val MIN_FREQUENCY = 1f
    const val MAX_FREQUENCY = 50f
    const val MAX_BEAT_FREQUENCY = 50f

    // Carrier frequency ranges (Hz) — audible tone
    const val MIN_CARRIER_FREQUENCY = 80f
    const val MAX_CARRIER_FREQUENCY = 500f
    const val DEFAULT_CARRIER_FREQUENCY = 200f

    // Modulation depth (fraction)
    const val MIN_MODULATION_DEPTH = 0.1f
    const val MAX_MODULATION_DEPTH = 0.7f
    const val DEFAULT_MODULATION_DEPTH = 0.4f

    // Frequency presets
    object FrequencyPresets {
        const val DELTA_2HZ = 2f
        const val THETA_4HZ = 4f
        const val THETA_6HZ = 6f
        const val ALPHA_10HZ = 10f
        const val BETA_20HZ = 20f
        const val GAMMA_40HZ = 40f
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
