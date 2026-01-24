package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.model.SessionState
import com.meditationmixer.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlayPauseSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(preset: Preset) {
        val currentSession = sessionRepository.getCurrentSession().first()
        
        when (currentSession.state) {
            SessionState.IDLE, SessionState.COMPLETED -> {
                sessionRepository.startSession(preset.id, preset.timerDurationMs)
            }
            SessionState.PLAYING -> {
                sessionRepository.pauseSession()
            }
            SessionState.PAUSED -> {
                sessionRepository.resumeSession()
            }
            SessionState.FADING_OUT -> {
                // Do nothing during fade out
            }
        }
    }
}
