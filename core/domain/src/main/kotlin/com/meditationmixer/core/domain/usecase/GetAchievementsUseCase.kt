package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.AchievementData
import com.meditationmixer.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    operator fun invoke(): Flow<AchievementData> {
        return streakRepository.getAchievements()
    }
}
