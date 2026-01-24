package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.Session
import com.meditationmixer.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<Session> {
        return sessionRepository.getCurrentSession()
    }
}
