package com.meditationmixer.core.audio.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.meditationmixer.core.domain.model.LayerConfig
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.domain.model.Preset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEngineImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioEngine {
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private val toneGenerator = ToneGenerator()
    private var userAudioPlayer: ExoPlayer? = null
    private var ambiencePlayer: ExoPlayer? = null
    
    private var masterVolume = 1.0f
    private var userAudioVolume = 0.7f
    private var ambienceVolume = 0.4f
    
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private var positionUpdateJob: Job? = null
    
    override suspend fun loadPreset(preset: Preset) {
        release()
        
        preset.layers.forEach { layer ->
            when (layer.type) {
                LayerType.TONE -> {
                    layer.frequency?.let { toneGenerator.setFrequency(it) }
                    toneGenerator.setVolume(layer.volume * masterVolume)
                }
                LayerType.USER_AUDIO -> {
                    layer.sourceUri?.let { uri ->
                        userAudioPlayer = createExoPlayer().apply {
                            setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
                            repeatMode = if (layer.loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                            volume = layer.volume * masterVolume
                            prepare()
                        }
                        userAudioVolume = layer.volume
                    }
                }
                LayerType.AMBIENCE -> {
                    layer.assetId?.let { assetId ->
                        val assetUri = "asset:///ambience/$assetId.ogg"
                        ambiencePlayer = createExoPlayer().apply {
                            setMediaItem(MediaItem.fromUri(assetUri))
                            repeatMode = if (layer.loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                            volume = layer.volume * masterVolume
                            prepare()
                        }
                        ambienceVolume = layer.volume
                    }
                }
            }
        }
        
        _duration.value = preset.timerDurationMs
    }
    
    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
    }
    
    override suspend fun play() {
        toneGenerator.start()
        userAudioPlayer?.play()
        ambiencePlayer?.play()
        _isPlaying.value = true
        startPositionUpdates()
    }
    
    override suspend fun pause() {
        toneGenerator.pause()
        userAudioPlayer?.pause()
        ambiencePlayer?.pause()
        _isPlaying.value = false
        stopPositionUpdates()
    }
    
    override suspend fun stop() {
        toneGenerator.stop()
        userAudioPlayer?.stop()
        ambiencePlayer?.stop()
        _isPlaying.value = false
        _currentPosition.value = 0
        stopPositionUpdates()
    }
    
    override suspend fun seekTo(positionMs: Long) {
        userAudioPlayer?.seekTo(positionMs)
        ambiencePlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }
    
    override suspend fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        toneGenerator.setVolume(toneGenerator.isPlaying.value.let { if (it) masterVolume else 0f })
        userAudioPlayer?.volume = userAudioVolume * masterVolume
        ambiencePlayer?.volume = ambienceVolume * masterVolume
    }
    
    override suspend fun setLayerVolume(type: LayerType, volume: Float) {
        val adjustedVolume = volume.coerceIn(0f, 1f)
        when (type) {
            LayerType.TONE -> toneGenerator.setVolume(adjustedVolume * masterVolume)
            LayerType.USER_AUDIO -> {
                userAudioVolume = adjustedVolume
                userAudioPlayer?.volume = adjustedVolume * masterVolume
            }
            LayerType.AMBIENCE -> {
                ambienceVolume = adjustedVolume
                ambiencePlayer?.volume = adjustedVolume * masterVolume
            }
        }
    }
    
    override suspend fun setLayerLoop(type: LayerType, loop: Boolean) {
        val repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        when (type) {
            LayerType.TONE -> { /* Tone always loops */ }
            LayerType.USER_AUDIO -> userAudioPlayer?.repeatMode = repeatMode
            LayerType.AMBIENCE -> ambiencePlayer?.repeatMode = repeatMode
        }
    }
    
    override suspend fun updateToneFrequency(frequencyHz: Float) {
        toneGenerator.setFrequency(frequencyHz)
    }
    
    override suspend fun fadeOut(durationMs: Long) {
        val steps = 50
        val stepDelay = durationMs / steps
        val startMasterVolume = masterVolume
        
        scope.launch {
            for (i in steps downTo 0) {
                val newVolume = startMasterVolume * (i.toFloat() / steps)
                setMasterVolume(newVolume)
                delay(stepDelay)
            }
            stop()
        }
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob = scope.launch {
            while (_isPlaying.value) {
                _currentPosition.value += 1000
                delay(1000)
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    override fun release() {
        toneGenerator.release()
        userAudioPlayer?.release()
        userAudioPlayer = null
        ambiencePlayer?.release()
        ambiencePlayer = null
        stopPositionUpdates()
    }
}
