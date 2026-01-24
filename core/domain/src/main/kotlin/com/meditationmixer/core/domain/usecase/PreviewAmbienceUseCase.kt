package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.AudioRepository
import javax.inject.Inject

class PreviewAmbienceUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(assetPath: String) {
        audioRepository.previewAmbience(assetPath)
    }
    
    suspend fun stop() {
        audioRepository.stopPreview()
    }
}
