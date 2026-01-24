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
        volume: Float? = null,
        loop: Boolean? = null,
        sourceUri: String? = null,
        assetId: String? = null,
        frequency: Float? = null
    ) {
        audioRepository.updateLayer(type, volume, loop, sourceUri, assetId, frequency)
        
        // Check for importer achievement
        if (type == LayerType.USER_AUDIO && sourceUri != null) {
            streakRepository.unlockAchievement("importer")
        }
    }
}
