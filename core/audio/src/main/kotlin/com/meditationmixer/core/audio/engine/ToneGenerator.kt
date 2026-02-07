package com.meditationmixer.core.audio.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.meditationmixer.core.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class ToneGenerator {

    private var audioTrack: AudioTrack? = null
    private var generatorJob: Job? = null
    private var stopJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var frequencyHz: Float = Constants.FrequencyPresets.THETA_6HZ
    private var volume: Float = 0.5f
    private var carrierHz: Float = 220f
    private var binaural: Boolean = false

    private val sampleRate = Constants.SAMPLE_RATE
    private val bufferSize = Constants.AUDIO_BUFFER_SIZE

    fun setFrequency(hz: Float) {
        frequencyHz = hz.coerceIn(Constants.MIN_FREQUENCY, Constants.MAX_FREQUENCY)
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        audioTrack?.setVolume(volume)
    }

    fun setBinaural(enabled: Boolean) {
        val wasPlaying = _isPlaying.value
        if (enabled != binaural) {
            binaural = enabled
            if (wasPlaying) {
                forceStop()
                start()
            }
        }
    }

    fun start() {
        if (_isPlaying.value) return

        stopJob?.cancel()
        stopJob = null
        generatorJob?.cancel()
        generatorJob = null
        audioTrack?.release()
        audioTrack = null

        val channelMask = if (binaural) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelMask,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferSize * (if (binaural) 2 else 1), minBufferSize))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volume)
        audioTrack?.play()
        _isPlaying.value = true

        generatorJob = scope.launch {
            if (binaural) generateBinauralTone() else generateTone()
        }
    }

    private suspend fun CoroutineScope.generateTone() {
        val samples = ShortArray(bufferSize / 2)
        var carrierPhase = 0.0
        var modPhase = 0.0

        while (isActive && _isPlaying.value) {
            val carrierIncrement = 2.0 * PI * carrierHz / sampleRate
            val modIncrement = 2.0 * PI * frequencyHz / sampleRate

            for (i in samples.indices) {
                val mod = ((sin(modPhase) + 1.0) * 0.5)
                val sample = sin(carrierPhase) * mod * Short.MAX_VALUE
                samples[i] = sample.toInt().toShort()

                carrierPhase += carrierIncrement
                if (carrierPhase >= 2.0 * PI) carrierPhase -= 2.0 * PI

                modPhase += modIncrement
                if (modPhase >= 2.0 * PI) modPhase -= 2.0 * PI
            }

            audioTrack?.write(samples, 0, samples.size)
        }
    }

    private suspend fun CoroutineScope.generateBinauralTone() {
        val samplesPerChannel = bufferSize / 2
        val stereoSamples = ShortArray(samplesPerChannel * 2)
        var leftPhase = 0.0
        var rightPhase = 0.0

        while (isActive && _isPlaying.value) {
            val leftFreq = carrierHz.toDouble()
            val rightFreq = (carrierHz + frequencyHz).toDouble()
            val leftIncrement = 2.0 * PI * leftFreq / sampleRate
            val rightIncrement = 2.0 * PI * rightFreq / sampleRate

            for (i in 0 until samplesPerChannel) {
                val leftSample = (sin(leftPhase) * Short.MAX_VALUE).toInt().toShort()
                val rightSample = (sin(rightPhase) * Short.MAX_VALUE).toInt().toShort()

                stereoSamples[i * 2] = leftSample
                stereoSamples[i * 2 + 1] = rightSample

                leftPhase += leftIncrement
                if (leftPhase >= 2.0 * PI) leftPhase -= 2.0 * PI

                rightPhase += rightIncrement
                if (rightPhase >= 2.0 * PI) rightPhase -= 2.0 * PI
            }

            audioTrack?.write(stereoSamples, 0, stereoSamples.size)
        }
    }

    fun pause() {
        fadeToStop(30L)
    }

    fun stop() {
        fadeToStop(30L)
    }

    private fun forceStop() {
        _isPlaying.value = false
        generatorJob?.cancel()
        generatorJob = null
        stopJob?.cancel()
        stopJob = null
        runCatching { audioTrack?.pause() }
        runCatching { audioTrack?.flush() }
        runCatching { audioTrack?.stop() }
        runCatching { audioTrack?.release() }
        audioTrack = null
    }

    private fun fadeToStop(durationMs: Long) {
        if (!_isPlaying.value && audioTrack == null) return

        _isPlaying.value = false
        generatorJob?.cancel()
        generatorJob = null

        val track = audioTrack
        if (track == null) return

        stopJob?.cancel()
        stopJob = scope.launch {
            val steps = 12
            val stepDelay = (durationMs / steps).coerceAtLeast(1L)
            val startVolume = volume

            for (i in steps downTo 0) {
                val newVolume = startVolume * (i.toFloat() / steps)
                track.setVolume(newVolume)
                kotlinx.coroutines.delay(stepDelay)
            }

            runCatching { track.pause() }
            runCatching { track.flush() }
            runCatching { track.stop() }
            runCatching { track.release() }

            if (audioTrack === track) {
                audioTrack = null
            }
        }
    }

    fun fadeOut(durationMs: Long, onComplete: () -> Unit) {
        stopJob?.cancel()
        stopJob = scope.launch {
            val startVolume = volume
            val steps = 50
            val stepDelay = durationMs / steps

            for (i in steps downTo 0) {
                val newVolume = startVolume * (i.toFloat() / steps)
                audioTrack?.setVolume(newVolume)
                kotlinx.coroutines.delay(stepDelay)
            }

            _isPlaying.value = false
            generatorJob?.cancel()
            generatorJob = null
            runCatching { audioTrack?.pause() }
            runCatching { audioTrack?.flush() }
            runCatching { audioTrack?.stop() }
            runCatching { audioTrack?.release() }
            audioTrack = null
            onComplete()
        }
    }

    fun release() {
        stop()
    }
}
