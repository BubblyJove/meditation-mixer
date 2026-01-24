package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.SessionRepository
import javax.inject.Inject

class SeekSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(progress: Float) {
        sessionRepository.seekTo(progress.coerceIn(0f, 1f))
    }
}
