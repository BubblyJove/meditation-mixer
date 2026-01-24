package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.Preset
import com.meditationmixer.core.domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresetsUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    operator fun invoke(): Flow<List<Preset>> {
        return presetRepository.getAllPresets()
    }
}
