package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.repository.SessionRepository
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(preset: Preset) {
        sessionRepository.startSession(presetId = preset.id, durationMs = preset.timerDurationMs)
    }
}
