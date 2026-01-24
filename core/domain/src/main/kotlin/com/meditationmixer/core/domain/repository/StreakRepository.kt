package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.AchievementData
import com.meditationmixer.core.domain.model.StreakData
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    fun getStreakData(): Flow<StreakData>
    fun getAchievements(): Flow<AchievementData>
    suspend fun recordCompletedSession()
    suspend fun unlockAchievement(achievementId: String)
    suspend fun checkAndUpdateStreak()
}
