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
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private var frequencyHz: Float = Constants.FrequencyPresets.THETA_6HZ
    private var volume: Float = 0.5f
    
    private val sampleRate = Constants.SAMPLE_RATE
    private val bufferSize = Constants.AUDIO_BUFFER_SIZE
    
    fun setFrequency(hz: Float) {
        frequencyHz = hz.coerceIn(Constants.MIN_FREQUENCY, Constants.MAX_FREQUENCY)
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
            generateTone()
        }
    }
    
    private suspend fun generateTone() {
        val samples = ShortArray(bufferSize / 2)
        var phase = 0.0
        
        while (isActive && _isPlaying.value) {
            val phaseIncrement = 2.0 * PI * frequencyHz / sampleRate
            
            for (i in samples.indices) {
                val sample = sin(phase) * Short.MAX_VALUE
                samples[i] = sample.toInt().toShort()
                phase += phaseIncrement
                
                if (phase >= 2.0 * PI) {
                    phase -= 2.0 * PI
                }
            }
            
            audioTrack?.write(samples, 0, samples.size)
        }
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
    
    fun fadeOut(durationMs: Long, onComplete: () -> Unit) {
        scope.launch {
            val startVolume = volume
            val steps = 50
            val stepDelay = durationMs / steps
            
            for (i in steps downTo 0) {
                val newVolume = startVolume * (i.toFloat() / steps)
                audioTrack?.setVolume(newVolume)
                kotlinx.coroutines.delay(stepDelay)
            }
            
            stop()
            onComplete()
        }
    }
    
    fun release() {
        stop()
    }
}
