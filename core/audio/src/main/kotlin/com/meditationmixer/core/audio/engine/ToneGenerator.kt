package com.meditationmixer.core.audio.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.meditationmixer.core.common.Constants
import com.meditationmixer.core.domain.model.ToneMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
    private var carrierHz: Float = Constants.DEFAULT_CARRIER_FREQUENCY
    private var toneMode: ToneMode = ToneMode.AM
    private var modulationDepth: Float = Constants.DEFAULT_MODULATION_DEPTH

    // Kept for backward compat — delegates to toneMode
    private var binaural: Boolean = false

    private val sampleRate = Constants.SAMPLE_RATE
    private val bufferSize = Constants.AUDIO_BUFFER_SIZE

    companion object {
        // Maximum real modulation depth — gain floor = 1 - 2*0.15 = 0.70
        private const val MAX_REAL_DEPTH = 0.15

        // Monaural two-tone: unequal amplitude ratio (−12 dB offset tone)
        private const val MONO_OFFSET_RATIO = 0.25
        private const val MONO_NORMALIZE = 0.8 // 1.0 / (1.0 + MONO_OFFSET_RATIO)

        // Band-specific max real depth
        private fun maxDepthForBeat(beatHz: Float): Float = when {
            beatHz <= 4f  -> 0.15f  // Delta
            beatHz <= 8f  -> 0.12f  // Theta
            beatHz <= 13f -> 0.15f  // Alpha
            beatHz <= 30f -> 0.12f  // Beta
            else          -> 0.09f  // Gamma
        }

        // Low-carrier scaling — prevents subwoofer wobble
        private fun carrierScale(carrierHz: Float): Float =
            if (carrierHz < 200f) 0.7f else 1.0f

        /**
         * Converts user-facing depth (0.1–0.7) to safe real depth.
         * Real depth controls AM swing: gain oscillates in [1-2d, 1.0].
         * With MAX_REAL_DEPTH=0.15, gain never drops below 0.70.
         */
        fun effectiveDepth(userDepth: Float, beatHz: Float, carrierHz: Float): Double {
            val normalized = ((userDepth - Constants.MIN_MODULATION_DEPTH) /
                (Constants.MAX_MODULATION_DEPTH - Constants.MIN_MODULATION_DEPTH))
                .coerceIn(0f, 1f)
            val bandMax = maxDepthForBeat(beatHz)
            val real = normalized * bandMax * carrierScale(carrierHz)
            return real.toDouble().coerceIn(0.0, MAX_REAL_DEPTH.toDouble())
        }
    }

    fun setFrequency(hz: Float) {
        frequencyHz = hz.coerceIn(Constants.MIN_FREQUENCY, Constants.MAX_FREQUENCY)
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        audioTrack?.setVolume(volume)
    }

    fun setToneMode(mode: ToneMode) {
        val wasPlaying = _isPlaying.value
        if (mode != toneMode) {
            toneMode = mode
            binaural = mode == ToneMode.BINAURAL
            if (wasPlaying) {
                forceStop()
                start()
            }
        }
    }

    fun setCarrierFrequency(hz: Float) {
        carrierHz = hz.coerceIn(Constants.MIN_CARRIER_FREQUENCY, Constants.MAX_CARRIER_FREQUENCY)
    }

    fun setModulationDepth(depth: Float) {
        modulationDepth = depth.coerceIn(Constants.MIN_MODULATION_DEPTH, Constants.MAX_MODULATION_DEPTH)
    }

    @Deprecated("Use setToneMode() instead", replaceWith = ReplaceWith("setToneMode(if (enabled) ToneMode.BINAURAL else ToneMode.AM)"))
    fun setBinaural(enabled: Boolean) {
        setToneMode(if (enabled) ToneMode.BINAURAL else ToneMode.AM)
    }

    fun start() {
        if (_isPlaying.value) return

        stopJob?.cancel()
        stopJob = null
        generatorJob?.cancel()
        generatorJob = null
        audioTrack?.release()
        audioTrack = null

        val isStereo = toneMode == ToneMode.BINAURAL
        val channelMask = if (isStereo) AudioFormat.CHANNEL_OUT_STEREO else AudioFormat.CHANNEL_OUT_MONO

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
            .setBufferSizeInBytes(maxOf(bufferSize * (if (isStereo) 2 else 1), minBufferSize))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volume)
        audioTrack?.play()
        _isPlaying.value = true

        generatorJob = scope.launch {
            when (toneMode) {
                ToneMode.AM -> generateAmTone()
                ToneMode.ISOCHRONIC -> generateIsochronicTone()
                ToneMode.BINAURAL -> generateBinauralTone()
                ToneMode.MONAURAL -> generateMonauralTone()
            }
        }
    }

    /**
     * AM: gain(t) = (1-depth) + depth * sin(2π * beat * t)
     * Gain peaks at 1.0, floors at 1-2*depth (≥0.70).
     * Post-modulation envelope smoothing (15ms attack, 30ms release).
     */
    private suspend fun CoroutineScope.generateAmTone() {
        val samples = ShortArray(bufferSize / 2)
        var carrierPhase = 0.0
        var beatPhase = 0.0
        var smoothedGain = 1.0
        val twoPi = 2.0 * PI
        val attackCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.015))
        val releaseCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.030))

        while (isActive && _isPlaying.value) {
            val carrierInc = twoPi * carrierHz / sampleRate
            val beatInc = twoPi * frequencyHz / sampleRate
            val depth = effectiveDepth(modulationDepth, frequencyHz, carrierHz)
            val base = 1.0 - depth

            for (i in samples.indices) {
                val rawGain = base + depth * sin(beatPhase)
                val coeff = if (rawGain > smoothedGain) attackCoeff else releaseCoeff
                smoothedGain += coeff * (rawGain - smoothedGain)

                val carrier = sin(carrierPhase)
                val sample = (carrier * smoothedGain * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                samples[i] = sample

                carrierPhase += carrierInc
                if (carrierPhase >= twoPi) carrierPhase -= twoPi
                beatPhase += beatInc
                if (beatPhase >= twoPi) beatPhase -= twoPi
            }

            audioTrack?.write(samples, 0, samples.size)
        }
    }

    /**
     * Isochronic: Half-rectified sine pulse modulation.
     * Gain = (1-2d) + 2d * pulse, where pulse is half-rectified sin.
     * Floor = 1-2d (≥0.70), peak = 1.0.
     * Post-modulation envelope smoothing (15ms attack, 30ms release).
     */
    private suspend fun CoroutineScope.generateIsochronicTone() {
        val samples = ShortArray(bufferSize / 2)
        var carrierPhase = 0.0
        var beatPhase = 0.0
        var smoothedGain = 1.0
        val twoPi = 2.0 * PI
        val attackCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.015))
        val releaseCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.030))

        while (isActive && _isPlaying.value) {
            val carrierInc = twoPi * carrierHz / sampleRate
            val beatInc = twoPi * frequencyHz / sampleRate
            val depth = effectiveDepth(modulationDepth, frequencyHz, carrierHz)
            val floor = 1.0 - 2.0 * depth

            for (i in samples.indices) {
                val beatSin = sin(beatPhase)
                val pulse = if (beatSin > 0.0) beatSin else 0.0
                val rawGain = floor + 2.0 * depth * pulse
                val coeff = if (rawGain > smoothedGain) attackCoeff else releaseCoeff
                smoothedGain += coeff * (rawGain - smoothedGain)

                val carrier = sin(carrierPhase)
                val sample = (carrier * smoothedGain * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                samples[i] = sample

                carrierPhase += carrierInc
                if (carrierPhase >= twoPi) carrierPhase -= twoPi
                beatPhase += beatInc
                if (beatPhase >= twoPi) beatPhase -= twoPi
            }

            audioTrack?.write(samples, 0, samples.size)
        }
    }

    private suspend fun CoroutineScope.generateBinauralTone() {
        val samplesPerChannel = bufferSize / 2
        val stereoSamples = ShortArray(samplesPerChannel * 2)
        var leftPhase = 0.0
        var rightPhase = 0.0
        val twoPi = 2.0 * PI

        while (isActive && _isPlaying.value) {
            val leftFreq = carrierHz.toDouble()
            val rightFreq = (carrierHz + frequencyHz).toDouble()
            val leftIncrement = twoPi * leftFreq / sampleRate
            val rightIncrement = twoPi * rightFreq / sampleRate

            for (i in 0 until samplesPerChannel) {
                val leftSample = (sin(leftPhase) * Short.MAX_VALUE).toInt().toShort()
                val rightSample = (sin(rightPhase) * Short.MAX_VALUE).toInt().toShort()

                stereoSamples[i * 2] = leftSample
                stereoSamples[i * 2 + 1] = rightSample

                leftPhase += leftIncrement
                if (leftPhase >= twoPi) leftPhase -= twoPi

                rightPhase += rightIncrement
                if (rightPhase >= twoPi) rightPhase -= twoPi
            }

            audioTrack?.write(stereoSamples, 0, stereoSamples.size)
        }
    }

    /**
     * Monaural: Two-tone summing with unequal amplitudes (−12 dB offset).
     * Primary tone at 1.0, offset tone at 0.25, normalized by 0.8.
     * Envelope naturally beats between 0.60 and 1.0 — inherently
     * more modulated than AM, but no full cancellation.
     */
    private suspend fun CoroutineScope.generateMonauralTone() {
        val samples = ShortArray(bufferSize / 2)
        var primaryPhase = 0.0
        var offsetPhase = 0.0
        val twoPi = 2.0 * PI

        while (isActive && _isPlaying.value) {
            val primaryFreq = carrierHz.toDouble()
            val offsetFreq = (carrierHz + frequencyHz).toDouble()
            val primaryInc = twoPi * primaryFreq / sampleRate
            val offsetInc = twoPi * offsetFreq / sampleRate

            for (i in samples.indices) {
                val s1 = sin(primaryPhase)
                val s2 = sin(offsetPhase) * MONO_OFFSET_RATIO
                val rawSample = (s1 + s2) * MONO_NORMALIZE

                val sample = (rawSample * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                samples[i] = sample

                primaryPhase += primaryInc
                if (primaryPhase >= twoPi) primaryPhase -= twoPi
                offsetPhase += offsetInc
                if (offsetPhase >= twoPi) offsetPhase -= twoPi
            }

            audioTrack?.write(samples, 0, samples.size)
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

    fun generateWavToStream(
        outputStream: OutputStream,
        durationSeconds: Int,
        frequencyHz: Float,
        volume: Float,
        toneMode: ToneMode = ToneMode.AM,
        carrierFreqHz: Float = Constants.DEFAULT_CARRIER_FREQUENCY,
        modulationDepth: Float = Constants.DEFAULT_MODULATION_DEPTH,
        @Suppress("UNUSED_PARAMETER") binaural: Boolean = false
    ) {
        val isStereo = toneMode == ToneMode.BINAURAL
        val numChannels = if (isStereo) 2 else 1
        val bitsPerSample = 16
        val totalSamples = sampleRate * durationSeconds
        val dataSize = totalSamples * numChannels * (bitsPerSample / 8)

        // Write WAV header (44 bytes)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + dataSize)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // PCM chunk size
        header.putShort(1) // PCM format
        header.putShort(numChannels.toShort())
        header.putInt(sampleRate)
        header.putInt(sampleRate * numChannels * bitsPerSample / 8)
        header.putShort((numChannels * bitsPerSample / 8).toShort())
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray())
        header.putInt(dataSize)
        outputStream.write(header.array())

        val chunkSamples = 4096
        val vol = volume.coerceIn(0f, 1f)
        val twoPi = 2.0 * PI
        var samplesWritten = 0

        val depth = effectiveDepth(modulationDepth, frequencyHz, carrierFreqHz)
        val attackCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.015))
        val releaseCoeff = 1.0 - kotlin.math.exp(-1.0 / (sampleRate * 0.030))

        when (toneMode) {
            ToneMode.AM -> {
                var carrierPhase = 0.0
                var beatPhase = 0.0
                val base = 1.0 - depth
                var smoothedGain = 1.0
                val carrierInc = twoPi * carrierFreqHz / sampleRate
                val beatInc = twoPi * frequencyHz / sampleRate

                while (samplesWritten < totalSamples) {
                    val count = minOf(chunkSamples, totalSamples - samplesWritten)
                    val buf = ByteBuffer.allocate(count * numChannels * 2).order(ByteOrder.LITTLE_ENDIAN)
                    for (i in 0 until count) {
                        val rawGain = base + depth * sin(beatPhase)
                        val coeff = if (rawGain > smoothedGain) attackCoeff else releaseCoeff
                        smoothedGain += coeff * (rawGain - smoothedGain)

                        val carrier = sin(carrierPhase)
                        val sample = (carrier * smoothedGain * vol * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        buf.putShort(sample)
                        carrierPhase += carrierInc
                        if (carrierPhase >= twoPi) carrierPhase -= twoPi
                        beatPhase += beatInc
                        if (beatPhase >= twoPi) beatPhase -= twoPi
                    }
                    outputStream.write(buf.array())
                    samplesWritten += count
                }
            }

            ToneMode.ISOCHRONIC -> {
                var carrierPhase = 0.0
                var beatPhase = 0.0
                val floor = 1.0 - 2.0 * depth
                var smoothedGain = 1.0
                val carrierInc = twoPi * carrierFreqHz / sampleRate
                val beatInc = twoPi * frequencyHz / sampleRate

                while (samplesWritten < totalSamples) {
                    val count = minOf(chunkSamples, totalSamples - samplesWritten)
                    val buf = ByteBuffer.allocate(count * numChannels * 2).order(ByteOrder.LITTLE_ENDIAN)
                    for (i in 0 until count) {
                        val beatSin = sin(beatPhase)
                        val pulse = if (beatSin > 0.0) beatSin else 0.0
                        val rawGain = floor + 2.0 * depth * pulse
                        val coeff = if (rawGain > smoothedGain) attackCoeff else releaseCoeff
                        smoothedGain += coeff * (rawGain - smoothedGain)

                        val carrier = sin(carrierPhase)
                        val sample = (carrier * smoothedGain * vol * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        buf.putShort(sample)
                        carrierPhase += carrierInc
                        if (carrierPhase >= twoPi) carrierPhase -= twoPi
                        beatPhase += beatInc
                        if (beatPhase >= twoPi) beatPhase -= twoPi
                    }
                    outputStream.write(buf.array())
                    samplesWritten += count
                }
            }

            ToneMode.BINAURAL -> {
                var leftPhase = 0.0
                var rightPhase = 0.0
                val leftFreq = carrierFreqHz.toDouble()
                val rightFreq = (carrierFreqHz + frequencyHz).toDouble()
                val leftInc = twoPi * leftFreq / sampleRate
                val rightInc = twoPi * rightFreq / sampleRate

                while (samplesWritten < totalSamples) {
                    val count = minOf(chunkSamples, totalSamples - samplesWritten)
                    val buf = ByteBuffer.allocate(count * numChannels * 2).order(ByteOrder.LITTLE_ENDIAN)
                    for (i in 0 until count) {
                        val left = (sin(leftPhase) * vol * Short.MAX_VALUE).toInt().toShort()
                        val right = (sin(rightPhase) * vol * Short.MAX_VALUE).toInt().toShort()
                        buf.putShort(left)
                        buf.putShort(right)
                        leftPhase += leftInc
                        if (leftPhase >= twoPi) leftPhase -= twoPi
                        rightPhase += rightInc
                        if (rightPhase >= twoPi) rightPhase -= twoPi
                    }
                    outputStream.write(buf.array())
                    samplesWritten += count
                }
            }

            ToneMode.MONAURAL -> {
                var primaryPhase = 0.0
                var offsetPhase = 0.0
                val primaryFreq = carrierFreqHz.toDouble()
                val offsetFreq = (carrierFreqHz + frequencyHz).toDouble()
                val primaryInc = twoPi * primaryFreq / sampleRate
                val offsetInc = twoPi * offsetFreq / sampleRate

                while (samplesWritten < totalSamples) {
                    val count = minOf(chunkSamples, totalSamples - samplesWritten)
                    val buf = ByteBuffer.allocate(count * numChannels * 2).order(ByteOrder.LITTLE_ENDIAN)
                    for (i in 0 until count) {
                        val s1 = sin(primaryPhase)
                        val s2 = sin(offsetPhase) * MONO_OFFSET_RATIO
                        val rawSample = (s1 + s2) * MONO_NORMALIZE

                        val sample = (rawSample * vol * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        buf.putShort(sample)
                        primaryPhase += primaryInc
                        if (primaryPhase >= twoPi) primaryPhase -= twoPi
                        offsetPhase += offsetInc
                        if (offsetPhase >= twoPi) offsetPhase -= twoPi
                    }
                    outputStream.write(buf.array())
                    samplesWritten += count
                }
            }
        }

        outputStream.flush()
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
