package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.PresetRepository
import javax.inject.Inject

class SetFavoritePresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    suspend operator fun invoke(presetId: Long, isFavorite: Boolean) {
        presetRepository.setFavorite(id = presetId, isFavorite = isFavorite)
    }
}
