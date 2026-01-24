package com.meditationmixer.core.domain.model

data class Session(
    val state: SessionState = SessionState.IDLE,
    val presetId: Long? = null,
    val startedAt: Long = 0,
    val elapsedMs: Long = 0,
    val remainingMs: Long = 0,
    val progress: Float = 0f,
    val timerDurationMs: Long = 0
)

enum class SessionState {
    IDLE,
    PLAYING,
    PAUSED,
    FADING_OUT,
    COMPLETED
}

data class SessionHistory(
    val id: Long = 0,
    val presetId: Long,
    val startedAt: Long,
    val durationMs: Long,
    val completed: Boolean
)
