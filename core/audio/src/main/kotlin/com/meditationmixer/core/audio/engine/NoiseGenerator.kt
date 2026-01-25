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
import kotlin.random.Random

class NoiseGenerator {

    enum class Profile {
        PINK,
        BROWN,
        RAIN,
        WIND,
        OCEAN
    }

    private var audioTrack: AudioTrack? = null
    private var generatorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var volume: Float = 0.4f

    private var profile: Profile = Profile.PINK

    private var pinkB0 = 0.0f
    private var pinkB1 = 0.0f
    private var pinkB2 = 0.0f
    private var pinkB3 = 0.0f
    private var pinkB4 = 0.0f
    private var pinkB5 = 0.0f
    private var pinkB6 = 0.0f

    private var brown = 0.0f
    private var lowPass = 0.0f
    private var highPass = 0.0f
    private var oceanLfoPhase = 0.0

    private val sampleRate = Constants.SAMPLE_RATE
    private val bufferSize = Constants.AUDIO_BUFFER_SIZE

    private val random = Random(System.currentTimeMillis())

    fun setProfile(newProfile: Profile) {
        profile = newProfile
    }

    fun setProfileFromAssetId(assetId: String?) {
        val id = assetId?.lowercase() ?: return
        profile = when {
            id.contains("rain") -> Profile.RAIN
            id.contains("wind") -> Profile.WIND
            id.contains("ocean") || id.contains("wave") || id.contains("sea") -> Profile.OCEAN
            else -> Profile.PINK
        }
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        audioTrack?.setVolume(volume)
    }

    fun start() {
        if (_isPlaying.value) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
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
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferSize, minBufferSize))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volume)
        audioTrack?.play()
        _isPlaying.value = true

        generatorJob = scope.launch {
            generateNoise()
        }
    }

    private suspend fun CoroutineScope.generateNoise() {
        val samples = ShortArray(bufferSize / 2)

        while (isActive && _isPlaying.value) {
            for (i in samples.indices) {
                val white = random.nextFloat() * 2f - 1f

                val shaped = when (profile) {
                    Profile.PINK -> pinkNoise(white)
                    Profile.BROWN -> brownNoise(white)
                    Profile.RAIN -> rainNoise(white)
                    Profile.WIND -> windNoise(white)
                    Profile.OCEAN -> oceanNoise(white)
                }

                val v = (shaped.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt()
                samples[i] = v.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            audioTrack?.write(samples, 0, samples.size)
        }
    }

    private fun pinkNoise(white: Float): Float {
        pinkB0 = 0.99886f * pinkB0 + white * 0.0555179f
        pinkB1 = 0.99332f * pinkB1 + white * 0.0750759f
        pinkB2 = 0.96900f * pinkB2 + white * 0.1538520f
        pinkB3 = 0.86650f * pinkB3 + white * 0.3104856f
        pinkB4 = 0.55000f * pinkB4 + white * 0.5329522f
        pinkB5 = -0.7616f * pinkB5 - white * 0.0168980f
        val out = pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + pinkB6 + white * 0.5362f
        pinkB6 = white * 0.115926f
        return out * 0.11f
    }

    private fun brownNoise(white: Float): Float {
        brown = (brown + white * 0.02f).coerceIn(-1f, 1f)
        return brown * 3.5f
    }

    private fun rainNoise(white: Float): Float {
        val base = pinkNoise(white)
        lowPass = lowPass + 0.02f * (base - lowPass)
        highPass = base - lowPass
        return highPass * 1.4f
    }

    private fun windNoise(white: Float): Float {
        val base = brownNoise(white)
        lowPass = lowPass + 0.004f * (base - lowPass)
        return lowPass
    }

    private fun oceanNoise(white: Float): Float {
        val base = brownNoise(white)
        lowPass = lowPass + 0.0025f * (base - lowPass)

        oceanLfoPhase += (2.0 * Math.PI * 0.10) / sampleRate
        if (oceanLfoPhase >= 2.0 * Math.PI) oceanLfoPhase -= 2.0 * Math.PI
        val swell = (0.65 + 0.35 * kotlin.math.sin(oceanLfoPhase)).toFloat()
        return lowPass * swell
    }

    fun pause() {
        _isPlaying.value = false
        generatorJob?.cancel()
        audioTrack?.pause()
    }

    fun stop() {
        _isPlaying.value = false
        generatorJob?.cancel()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }

    fun release() {
        stop()
    }
}
