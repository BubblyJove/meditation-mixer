package com.meditationmixer.core.audio.repository

import com.meditationmixer.core.audio.engine.AudioEngine
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.data.database.dao.SessionHistoryDao
import com.meditationmixer.core.data.database.entity.SessionHistoryEntity
import com.meditationmixer.core.domain.model.Session
import com.meditationmixer.core.domain.model.SessionHistory
import com.meditationmixer.core.domain.model.SessionState
import com.meditationmixer.core.domain.repository.PresetRepository
import com.meditationmixer.core.domain.repository.SessionRepository
import com.meditationmixer.core.domain.repository.SettingsRepository
import com.meditationmixer.core.domain.repository.StreakRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val audioEngine: AudioEngine,
    private val presetRepository: PresetRepository,
    private val settingsRepository: SettingsRepository,
    private val streakRepository: StreakRepository,
    private val sessionHistoryDao: SessionHistoryDao
) : SessionRepository {
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    
    private val _currentSession = MutableStateFlow(Session())
    
    override fun getCurrentSession(): Flow<Session> = _currentSession.asStateFlow()
    
    override fun getSessionHistory(): Flow<List<SessionHistory>> {
        return sessionHistoryDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun startSession(presetId: Long, durationMs: Long) {
        val preset = presetRepository.getPresetById(presetId).first() ?: return

        timerJob?.cancel()
        audioEngine.stop()
        
        audioEngine.loadPreset(preset)
        audioEngine.play()
        
        presetRepository.updateLastUsed(presetId)
        
        val startTime = System.currentTimeMillis()
        
        _currentSession.value = Session(
            state = SessionState.PLAYING,
            presetId = presetId,
            startedAt = startTime,
            elapsedMs = 0,
            remainingMs = durationMs,
            progress = 0f,
            timerDurationMs = durationMs
        )
        
        startTimer(durationMs, startTime)
    }
    
    private fun startTimer(durationMs: Long, startTime: Long) {
        timerJob?.cancel()
        timerJob = scope.launch {
            val settings = settingsRepository.getSettings().first()
            val fadeOutMs = settings.fadeDurationSeconds * 1000L
            
            while (_currentSession.value.state == SessionState.PLAYING) {
                delay(1000)
                
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (durationMs - elapsed).coerceAtLeast(0)
                val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
                
                _currentSession.value = _currentSession.value.copy(
                    elapsedMs = elapsed,
                    remainingMs = remaining,
                    progress = progress
                )
                
                // Start fade out
                if (remaining <= fadeOutMs && _currentSession.value.state == SessionState.PLAYING) {
                    _currentSession.value = _currentSession.value.copy(state = SessionState.FADING_OUT)
                    audioEngine.fadeOut(remaining)
                    
                    delay(remaining)
                    completeSession()
                    break
                }
                
                // Timer complete
                if (remaining <= 0) {
                    completeSession()
                    break
                }
            }
        }
    }
    
    private suspend fun completeSession() {
        val session = _currentSession.value
        
        audioEngine.stop()
        
        _currentSession.value = session.copy(
            state = SessionState.COMPLETED,
            progress = 1f,
            remainingMs = 0
        )
        
        // Record session if it meets minimum duration
        val minCreditMs = Constants.MIN_CREDIT_MINUTES * 60 * 1000L
        if (session.elapsedMs >= minCreditMs) {
            session.presetId?.let { presetId ->
                recordSessionComplete(presetId, session.elapsedMs)
            }
            streakRepository.recordCompletedSession()
        }
    }
    
    override suspend fun pauseSession() {
        audioEngine.pause()
        timerJob?.cancel()
        
        _currentSession.value = _currentSession.value.copy(
            state = SessionState.PAUSED
        )
    }
    
    override suspend fun resumeSession() {
        audioEngine.play()
        
        val session = _currentSession.value
        _currentSession.value = session.copy(state = SessionState.PLAYING)
        
        // Resume timer from current position
        val resumeTime = System.currentTimeMillis() - session.elapsedMs
        startTimer(session.timerDurationMs, resumeTime)
    }
    
    override suspend fun stopSession() {
        audioEngine.stop()
        timerJob?.cancel()
        
        _currentSession.value = Session()
    }
    
    override suspend fun seekTo(progress: Float) {
        val session = _currentSession.value
        val newElapsed = (session.timerDurationMs * progress).toLong()
        val newRemaining = session.timerDurationMs - newElapsed
        
        _currentSession.value = session.copy(
            elapsedMs = newElapsed,
            remainingMs = newRemaining,
            progress = progress
        )
        
        audioEngine.seekTo(newElapsed)
    }
    
    override suspend fun recordSessionComplete(presetId: Long, durationMs: Long) {
        sessionHistoryDao.insertSession(
            SessionHistoryEntity(
                presetId = presetId,
                startedAt = System.currentTimeMillis() - durationMs,
                durationMs = durationMs,
                completed = true
            )
        )
    }
}
