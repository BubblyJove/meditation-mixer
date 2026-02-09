package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.model.ToneMode
import com.meditationmixer.core.domain.repository.AudioRepository
import javax.inject.Inject

class PreviewToneUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode = ToneMode.AM,
        carrierFrequency: Float = 200f,
        modulationDepth: Float = 0.4f
    ) {
        audioRepository.previewTone(
            frequencyHz = frequencyHz,
            volume = volume,
            toneMode = toneMode,
            carrierFrequency = carrierFrequency,
            modulationDepth = modulationDepth
        )
    }

    suspend fun stop() {
        audioRepository.stopTonePreview()
    }
}
