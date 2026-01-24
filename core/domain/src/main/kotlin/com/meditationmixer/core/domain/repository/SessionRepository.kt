package com.meditationmixer.core.domain.repository

import com.meditationmixer.core.domain.model.Session
import com.meditationmixer.core.domain.model.SessionHistory
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getCurrentSession(): Flow<Session>
    fun getSessionHistory(): Flow<List<SessionHistory>>
    suspend fun startSession(presetId: Long, durationMs: Long)
    suspend fun pauseSession()
    suspend fun resumeSession()
    suspend fun stopSession()
    suspend fun seekTo(progress: Float)
    suspend fun recordSessionComplete(presetId: Long, durationMs: Long)
}
