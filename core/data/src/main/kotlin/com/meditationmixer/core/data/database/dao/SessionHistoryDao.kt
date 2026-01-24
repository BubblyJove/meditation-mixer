package com.meditationmixer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.meditationmixer.core.data.database.entity.SessionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionHistoryDao {
    
    @Query("SELECT * FROM session_history ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<SessionHistoryEntity>>
    
    @Query("SELECT * FROM session_history WHERE completed = 1 ORDER BY startedAt DESC")
    fun getCompletedSessions(): Flow<List<SessionHistoryEntity>>
    
    @Query("SELECT COUNT(*) FROM session_history WHERE completed = 1")
    fun getCompletedSessionCount(): Flow<Int>
    
    @Insert
    suspend fun insertSession(session: SessionHistoryEntity): Long
    
    @Query("SELECT * FROM session_history WHERE startedAt >= :startOfDay AND startedAt < :endOfDay LIMIT 1")
    suspend fun getSessionForDay(startOfDay: Long, endOfDay: Long): SessionHistoryEntity?
}
