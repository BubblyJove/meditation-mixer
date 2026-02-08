package com.meditationmixer.core.domain.usecase

import com.meditationmixer.core.domain.repository.AudioRepository
import java.io.OutputStream
import javax.inject.Inject

class SaveToneUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(
        outputStream: OutputStream,
        durationSeconds: Int,
        frequencyHz: Float,
        volume: Float,
        binaural: Boolean
    ) {
        audioRepository.generateToneWav(
            outputStream = outputStream,
            durationSeconds = durationSeconds,
            frequencyHz = frequencyHz,
            volume = volume,
            binaural = binaural
        )
    }
}
