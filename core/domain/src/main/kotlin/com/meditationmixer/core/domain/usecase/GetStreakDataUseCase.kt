package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.StreakData
import com.meditationmixer.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStreakDataUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    operator fun invoke(): Flow<StreakData> {
        return streakRepository.getStreakData()
    }
}
