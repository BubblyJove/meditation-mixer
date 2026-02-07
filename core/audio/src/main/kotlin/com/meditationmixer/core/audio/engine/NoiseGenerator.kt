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
import kotlin.math.sin
import kotlin.random.Random

class NoiseGenerator {

    enum class Profile {
        PINK,
        BROWN,
        RAIN_LIGHT,
        RAIN_HEAVY,
        WIND,
        OCEAN,
        FOREST,
        RIVER
    }

    private var audioTrack: AudioTrack? = null
    private var generatorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var volume: Float = 0.4f

    private var profile: Profile = Profile.PINK

    // Pink noise state
    private var pinkB0 = 0.0f
    private var pinkB1 = 0.0f
    private var pinkB2 = 0.0f
    private var pinkB3 = 0.0f
    private var pinkB4 = 0.0f
    private var pinkB5 = 0.0f
    private var pinkB6 = 0.0f

    // Shared filter state
    private var brown = 0.0f
    private var lowPass = 0.0f
    private var highPass = 0.0f
    private var oceanLfoPhase = 0.0

    // Forest state (multiple layers)
    private var cricketPhase1 = 0.0
    private var cricketPhase2 = 0.0
    private var cricketChirpTimer = 0
    private var cricketChirpActive = false
    private var frogPhase = 0.0
    private var frogTimer = 0
    private var frogActive = false
    private var forestBrown = 0.0f
    private var forestLowPass = 0.0f

    // River state
    private var riverLowPass1 = 0.0f
    private var riverLowPass2 = 0.0f
    private var riverBubblePhase = 0.0
    private var riverBubbleTimer = 0
    private var riverBubbleActive = false

    // Heavy rain state
    private var heavyLowPass = 0.0f
    private var thunderTimer = 0
    private var thunderActive = false
    private var thunderDecay = 0.0f

    private val sampleRate = Constants.SAMPLE_RATE
    private val bufferSize = Constants.AUDIO_BUFFER_SIZE

    private val random = Random(System.currentTimeMillis())

    fun setProfile(newProfile: Profile) {
        profile = newProfile
        resetState()
    }

    fun setProfileFromAssetId(assetId: String?) {
        val id = assetId?.lowercase() ?: return
        profile = when {
            id.contains("rain_heavy") || id.contains("heavy") || id.contains("thunder") -> Profile.RAIN_HEAVY
            id.contains("rain") -> Profile.RAIN_LIGHT
            id.contains("wind") -> Profile.WIND
            id.contains("ocean") || id.contains("wave") || id.contains("sea") -> Profile.OCEAN
            id.contains("forest") || id.contains("night") || id.contains("cricket") -> Profile.FOREST
            id.contains("river") || id.contains("stream") || id.contains("creek") -> Profile.RIVER
            else -> Profile.PINK
        }
        resetState()
    }

    private fun resetState() {
        pinkB0 = 0f; pinkB1 = 0f; pinkB2 = 0f; pinkB3 = 0f
        pinkB4 = 0f; pinkB5 = 0f; pinkB6 = 0f
        brown = 0f; lowPass = 0f; highPass = 0f; oceanLfoPhase = 0.0
        cricketPhase1 = 0.0; cricketPhase2 = 0.0; cricketChirpTimer = 0
        frogPhase = 0.0; frogTimer = 0
        forestBrown = 0f; forestLowPass = 0f
        riverLowPass1 = 0f; riverLowPass2 = 0f; riverBubblePhase = 0.0; riverBubbleTimer = 0
        heavyLowPass = 0f; thunderTimer = 0; thunderDecay = 0f
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
                    Profile.RAIN_LIGHT -> rainLightNoise(white)
                    Profile.RAIN_HEAVY -> rainHeavyNoise(white)
                    Profile.WIND -> windNoise(white)
                    Profile.OCEAN -> oceanNoise(white)
                    Profile.FOREST -> forestNoise(white)
                    Profile.RIVER -> riverNoise(white)
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

    // Light rain: pink noise with high-pass filter for crisp droplet sound
    private fun rainLightNoise(white: Float): Float {
        val base = pinkNoise(white)
        lowPass = lowPass + 0.02f * (base - lowPass)
        highPass = base - lowPass

        // Add subtle random droplet patter
        val droplet = if (random.nextFloat() > 0.997f) {
            random.nextFloat() * 0.3f
        } else 0f

        return highPass * 1.2f + droplet
    }

    // Heavy rain: louder pink noise + low rumble + occasional thunder
    private fun rainHeavyNoise(white: Float): Float {
        val base = pinkNoise(white)
        heavyLowPass = heavyLowPass + 0.015f * (base - heavyLowPass)
        val rain = base * 0.7f + heavyLowPass * 0.5f

        // Thunder rumble
        thunderTimer++
        if (!thunderActive && thunderTimer > sampleRate * 4 && random.nextFloat() > 0.99998f) {
            thunderActive = true
            thunderDecay = 0.6f
            thunderTimer = 0
        }
        val thunder = if (thunderActive) {
            thunderDecay *= 0.99993f
            if (thunderDecay < 0.01f) {
                thunderActive = false
                thunderDecay = 0f
            }
            val rumble = brownNoise(white * 0.5f) * thunderDecay
            rumble
        } else 0f

        return (rain + thunder).coerceIn(-1f, 1f)
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
        val swell = (0.65 + 0.35 * sin(oceanLfoPhase)).toFloat()
        return lowPass * swell
    }

    // Forest night: low brown noise base + cricket chirps + occasional frog
    private fun forestNoise(white: Float): Float {
        // Ambient base: very quiet brown noise (wind through trees)
        forestBrown = (forestBrown + white * 0.008f).coerceIn(-1f, 1f)
        forestLowPass = forestLowPass + 0.002f * (forestBrown * 2f - forestLowPass)
        val ambient = forestLowPass * 0.3f

        // Cricket chirps: periodic bursts of high-frequency oscillation
        cricketChirpTimer++
        if (!cricketChirpActive && cricketChirpTimer > sampleRate / 4) {
            if (random.nextFloat() > 0.9997f) {
                cricketChirpActive = true
                cricketChirpTimer = 0
            }
        }
        val cricket = if (cricketChirpActive) {
            cricketPhase1 += 2.0 * Math.PI * 4200.0 / sampleRate
            cricketPhase2 += 2.0 * Math.PI * 4600.0 / sampleRate
            if (cricketPhase1 >= 2.0 * Math.PI) cricketPhase1 -= 2.0 * Math.PI
            if (cricketPhase2 >= 2.0 * Math.PI) cricketPhase2 -= 2.0 * Math.PI

            val chirpEnv = sin(Math.PI * cricketChirpTimer.toDouble() / (sampleRate * 0.06)).toFloat().coerceIn(0f, 1f)
            if (cricketChirpTimer > (sampleRate * 0.06).toInt()) {
                cricketChirpActive = false
                cricketChirpTimer = 0
            }
            ((sin(cricketPhase1) + sin(cricketPhase2)) * 0.06 * chirpEnv).toFloat()
        } else 0f

        // Occasional frog croak: low frequency burst
        frogTimer++
        if (!frogActive && frogTimer > sampleRate * 2) {
            if (random.nextFloat() > 0.9999f) {
                frogActive = true
                frogTimer = 0
            }
        }
        val frog = if (frogActive) {
            frogPhase += 2.0 * Math.PI * 180.0 / sampleRate
            if (frogPhase >= 2.0 * Math.PI) frogPhase -= 2.0 * Math.PI
            val env = sin(Math.PI * frogTimer.toDouble() / (sampleRate * 0.15)).toFloat().coerceIn(0f, 1f)
            if (frogTimer > (sampleRate * 0.15).toInt()) {
                frogActive = false
                frogTimer = 0
            }
            (sin(frogPhase) * 0.12 * env).toFloat()
        } else 0f

        return (ambient + cricket + frog).coerceIn(-1f, 1f)
    }

    // River: flowing water base + occasional bubble sounds
    private fun riverNoise(white: Float): Float {
        // Flowing water: filtered pink noise with two resonant bands
        val pink = pinkNoise(white)
        riverLowPass1 = riverLowPass1 + 0.008f * (pink - riverLowPass1)
        riverLowPass2 = riverLowPass2 + 0.003f * (pink - riverLowPass2)
        val flow = riverLowPass1 * 0.6f + (pink - riverLowPass2) * 0.35f

        // Subtle LFO variation for flow dynamics
        oceanLfoPhase += (2.0 * Math.PI * 0.25) / sampleRate
        if (oceanLfoPhase >= 2.0 * Math.PI) oceanLfoPhase -= 2.0 * Math.PI
        val flowMod = (0.8 + 0.2 * sin(oceanLfoPhase)).toFloat()

        // Bubble pops
        riverBubbleTimer++
        if (!riverBubbleActive && riverBubbleTimer > sampleRate / 8) {
            if (random.nextFloat() > 0.9995f) {
                riverBubbleActive = true
                riverBubbleTimer = 0
                riverBubblePhase = 0.0
            }
        }
        val bubble = if (riverBubbleActive) {
            val freq = 800.0 + random.nextDouble() * 400.0
            riverBubblePhase += 2.0 * Math.PI * freq / sampleRate
            if (riverBubblePhase >= 2.0 * Math.PI) riverBubblePhase -= 2.0 * Math.PI
            val env = sin(Math.PI * riverBubbleTimer.toDouble() / (sampleRate * 0.02)).toFloat().coerceIn(0f, 1f)
            if (riverBubbleTimer > (sampleRate * 0.02).toInt()) {
                riverBubbleActive = false
                riverBubbleTimer = 0
            }
            (sin(riverBubblePhase) * 0.08 * env).toFloat()
        } else 0f

        return (flow * flowMod + bubble).coerceIn(-1f, 1f)
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
