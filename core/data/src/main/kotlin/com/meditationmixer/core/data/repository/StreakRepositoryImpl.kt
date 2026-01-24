package com.meditationmixer.core.data.repository

import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.common.TimeUtils
import com.meditationmixer.core.data.database.dao.StreakDao
import com.meditationmixer.core.data.database.entity.AchievementEntity
import com.meditationmixer.core.data.database.entity.StreakEntity
import com.meditationmixer.core.domain.model.AchievementData
import com.meditationmixer.core.domain.model.StreakData
import com.meditationmixer.core.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao
) : StreakRepository {
    
    override fun getStreakData(): Flow<StreakData> {
        return streakDao.getStreakData().map { entity ->
            entity?.let {
                StreakData(
                    currentStreak = it.currentStreak,
                    longestStreak = it.longestStreak,
                    totalSessions = it.totalSessions,
                    lastSessionDate = it.lastSessionDate
                )
            } ?: StreakData()
        }
    }
    
    override fun getAchievements(): Flow<AchievementData> {
        return streakDao.getAllAchievements().map { achievements ->
            val achievementMap = achievements.associateBy { it.id }
            AchievementData(
                firstNight = achievementMap[Constants.Achievements.FIRST_NIGHT]?.isUnlocked ?: false,
                weekender = achievementMap[Constants.Achievements.WEEKENDER]?.isUnlocked ?: false,
                builder = achievementMap[Constants.Achievements.BUILDER]?.isUnlocked ?: false,
                importer = achievementMap[Constants.Achievements.IMPORTER]?.isUnlocked ?: false,
                consistent = achievementMap[Constants.Achievements.CONSISTENT]?.isUnlocked ?: false
            )
        }
    }
    
    override suspend fun recordCompletedSession() {
        val now = System.currentTimeMillis()
        
        // Increment session count
        streakDao.incrementSessions(now)
        
        // Unlock first night achievement
        unlockAchievement(Constants.Achievements.FIRST_NIGHT)
        
        // Check and update streak
        checkAndUpdateStreak()
    }
    
    override suspend fun unlockAchievement(achievementId: String) {
        val existing = streakDao.getAchievement(achievementId)
        if (existing == null) {
            streakDao.insertAchievement(
                AchievementEntity(
                    id = achievementId,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )
            )
        } else if (!existing.isUnlocked) {
            streakDao.unlockAchievement(achievementId, System.currentTimeMillis())
        }
    }
    
    override suspend fun checkAndUpdateStreak() {
        val now = System.currentTimeMillis()
        
        // This is a simplified streak calculation
        // In production, you'd want more robust date handling
        val entity = streakDao.getStreakData().first()
        val lastDate = entity?.lastSessionDate

        val currentStreak = entity?.currentStreak ?: 0
        val newStreak = if (lastDate == null) {
            1
        } else if (TimeUtils.isSameDay(lastDate, now)) {
            currentStreak
        } else if (TimeUtils.isConsecutiveDay(lastDate, now)) {
            currentStreak + 1
        } else {
            1
        }

        streakDao.updateStreak(newStreak)

        // Check for consistent achievement
        if (newStreak >= Constants.CONSISTENT_STREAK) {
            unlockAchievement(Constants.Achievements.CONSISTENT)
        }

        // Check for weekender achievement
        val totalSessions = entity?.totalSessions ?: 0
        if (totalSessions >= Constants.WEEKENDER_SESSIONS) {
            unlockAchievement(Constants.Achievements.WEEKENDER)
        }
    }
}
