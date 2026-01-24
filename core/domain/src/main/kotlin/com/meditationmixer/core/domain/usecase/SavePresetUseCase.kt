package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.repository.PresetRepository
import com.meditationmixer.core.domain.repository.StreakRepository
import javax.inject.Inject

class SavePresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository,
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(preset: Preset): Long {
        val id = presetRepository.savePreset(preset)
        streakRepository.unlockAchievement("builder")
        return id
    }
}
