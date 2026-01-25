package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.AudioRepository
import javax.inject.Inject

class PreviewToneUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(frequencyHz: Float, volume: Float) {
        audioRepository.previewTone(frequencyHz = frequencyHz, volume = volume)
    }

    suspend fun stop() {
        audioRepository.stopTonePreview()
    }
}
