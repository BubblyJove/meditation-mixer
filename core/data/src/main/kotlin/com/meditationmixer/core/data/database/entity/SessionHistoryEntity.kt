package com.meditationmixer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meditationmixer.core.domain.model.SessionHistory

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val presetId: Long,
    val startedAt: Long,
    val durationMs: Long,
    val completed: Boolean
) {
    fun toDomain(): SessionHistory = SessionHistory(
        id = id,
        presetId = presetId,
        startedAt = startedAt,
        durationMs = durationMs,
        completed = completed
    )
    
    companion object {
        fun fromDomain(session: SessionHistory): SessionHistoryEntity = SessionHistoryEntity(
            id = session.id,
            presetId = session.presetId,
            startedAt = session.startedAt,
            durationMs = session.durationMs,
            completed = session.completed
        )
    }
}
