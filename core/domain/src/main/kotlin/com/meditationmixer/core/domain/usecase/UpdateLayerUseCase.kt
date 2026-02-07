package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.repository.AudioRepository
import com.meditationmixer.core.domain.repository.StreakRepository
import javax.inject.Inject

class UpdateLayerUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(
        type: LayerType,
        enabled: Boolean? = null,
        volume: Float? = null,
        loop: Boolean? = null,
        sourceUri: String? = null,
        assetId: String? = null,
        frequency: Float? = null,
        startOffsetMs: Long? = null,
        binaural: Boolean? = null
    ) {
        audioRepository.updateLayer(
            type = type,
            enabled = enabled,
            volume = volume,
            loop = loop,
            sourceUri = sourceUri,
            assetId = assetId,
            frequency = frequency,
            startOffsetMs = startOffsetMs,
            binaural = binaural
        )
        
        // Check for importer achievement
        if (type == LayerType.USER_AUDIO && sourceUri != null) {
            streakRepository.unlockAchievement("importer")
        }
    }
}
