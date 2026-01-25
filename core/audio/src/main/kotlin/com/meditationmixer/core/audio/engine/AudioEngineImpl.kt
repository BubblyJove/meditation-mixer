package com.meditationmixer.core.audio.engine

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.meditationmixer.core.audio.engine.NoiseGenerator
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
    private val ambienceNoiseGenerator = NoiseGenerator()
    private var ambienceUsesNoise = false
    
    private var masterVolume = 1.0f
    private var toneVolume = 0.5f
    private var userAudioVolume = 0.7f
    private var ambienceVolume = 0.4f

    private var toneEnabled = true
    private var userAudioEnabled = true
    private var ambienceEnabled = true

    private var userAudioLoop = true
    private var userAudioStartOffsetMs = 0L
    private var userAudioLoopRestartJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val userAudioLoopListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState != Player.STATE_ENDED) return
            if (!userAudioLoop) return
            if (!userAudioEnabled) return
            if (!_isPlaying.value) return
            if (userAudioStartOffsetMs <= 0L) return

            userAudioLoopRestartJob?.cancel()
            userAudioLoopRestartJob = scope.launch {
                delay(userAudioStartOffsetMs)
                val player = userAudioPlayer ?: return@launch
                if (!_isPlaying.value || !userAudioEnabled || !userAudioLoop) return@launch
                player.seekTo(0)
                player.play()
            }
        }
    }

    
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
                    toneEnabled = layer.enabled
                    layer.frequency?.let { toneGenerator.setFrequency(it) }
                    toneVolume = layer.volume
                    toneGenerator.setVolume(if (toneEnabled) toneVolume * masterVolume else 0f)
                }
                LayerType.USER_AUDIO -> {
                    userAudioEnabled = layer.enabled
                    userAudioLoop = layer.loop
                    userAudioStartOffsetMs = layer.startOffsetMs

                    layer.sourceUri?.let { uri ->
                        userAudioPlayer = createExoPlayer().apply {
                            setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
                            repeatMode = Player.REPEAT_MODE_OFF
                            volume = if (userAudioEnabled) layer.volume * masterVolume else 0f
                            addListener(userAudioLoopListener)
                            prepare()
                        }
                        userAudioVolume = layer.volume
                        applyUserAudioLooping()
                    }
                }
                LayerType.AMBIENCE -> {
                    ambienceEnabled = layer.enabled
                    layer.assetId?.let { assetId ->
                        ambienceVolume = layer.volume

                        val assetPath = "ambience/$assetId.ogg"
                        val hasAsset = runCatching { context.assets.open(assetPath).close() }.isSuccess

                        if (hasAsset) {
                            ambienceUsesNoise = false
                            val assetUri = "asset:///ambience/$assetId.ogg"
                            ambiencePlayer = createExoPlayer().apply {
                                setMediaItem(MediaItem.fromUri(assetUri))
                                repeatMode = if (layer.loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
                                volume = if (ambienceEnabled) ambienceVolume * masterVolume else 0f
                                prepare()
                            }
                            ambienceNoiseGenerator.stop()
                        } else {
                            ambienceUsesNoise = true
                            ambiencePlayer?.release()
                            ambiencePlayer = null
                            ambienceNoiseGenerator.setVolume(if (ambienceEnabled) ambienceVolume * masterVolume else 0f)
                        }
                    }
                }
            }
        }
        
        _duration.value = preset.timerDurationMs
    }

    private fun applyUserAudioLooping() {
        val player = userAudioPlayer ?: return

        userAudioLoopRestartJob?.cancel()

        player.repeatMode = if (userAudioLoop && userAudioStartOffsetMs <= 0L) {
            Player.REPEAT_MODE_ALL
        } else {
            Player.REPEAT_MODE_OFF
        }
    }
    
    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
    }
    
    override suspend fun play() {
        if (toneEnabled) {
            toneGenerator.start()
        }

        if (userAudioEnabled) {
            userAudioPlayer?.play()
        }

        if (ambienceEnabled) {
            if (ambienceUsesNoise) {
                ambienceNoiseGenerator.start()
            } else {
                ambiencePlayer?.play()
            }
        }

        _isPlaying.value = true
        startPositionUpdates()
    }
    
    override suspend fun pause() {
        toneGenerator.pause()
        userAudioPlayer?.pause()
        ambiencePlayer?.pause()
        ambienceNoiseGenerator.pause()
        _isPlaying.value = false
        stopPositionUpdates()
    }
    
    override suspend fun stop() {
        toneGenerator.stop()
        userAudioPlayer?.stop()
        ambiencePlayer?.stop()
        ambienceNoiseGenerator.stop()
        userAudioLoopRestartJob?.cancel()
        userAudioLoopRestartJob = null
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
        toneGenerator.setVolume(if (toneEnabled) toneVolume * masterVolume else 0f)
        userAudioPlayer?.volume = if (userAudioEnabled) userAudioVolume * masterVolume else 0f
        if (ambienceUsesNoise) {
            ambienceNoiseGenerator.setVolume(if (ambienceEnabled) ambienceVolume * masterVolume else 0f)
        } else {
            ambiencePlayer?.volume = if (ambienceEnabled) ambienceVolume * masterVolume else 0f
        }
    }
    
    override suspend fun setLayerVolume(type: LayerType, volume: Float) {
        val adjustedVolume = volume.coerceIn(0f, 1f)
        when (type) {
            LayerType.TONE -> {
                toneVolume = adjustedVolume
                toneGenerator.setVolume(if (toneEnabled) toneVolume * masterVolume else 0f)
            }
            LayerType.USER_AUDIO -> {
                userAudioVolume = adjustedVolume
                userAudioPlayer?.volume = if (userAudioEnabled) adjustedVolume * masterVolume else 0f
            }
            LayerType.AMBIENCE -> {
                ambienceVolume = adjustedVolume
                if (ambienceUsesNoise) {
                    ambienceNoiseGenerator.setVolume(if (ambienceEnabled) adjustedVolume * masterVolume else 0f)
                } else {
                    ambiencePlayer?.volume = if (ambienceEnabled) adjustedVolume * masterVolume else 0f
                }
            }
        }
    }
    
    override suspend fun setLayerLoop(type: LayerType, loop: Boolean) {
        val repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        when (type) {
            LayerType.TONE -> { /* Tone always loops */ }
            LayerType.USER_AUDIO -> {
                userAudioLoop = loop
                applyUserAudioLooping()
            }
            LayerType.AMBIENCE -> ambiencePlayer?.repeatMode = repeatMode
        }
    }

    override suspend fun setLayerEnabled(type: LayerType, enabled: Boolean) {
        when (type) {
            LayerType.TONE -> {
                toneEnabled = enabled
                toneGenerator.setVolume(if (toneEnabled) toneVolume * masterVolume else 0f)
                if (_isPlaying.value) {
                    if (toneEnabled) toneGenerator.start() else toneGenerator.pause()
                }
            }
            LayerType.USER_AUDIO -> {
                userAudioEnabled = enabled
                userAudioPlayer?.volume = if (userAudioEnabled) userAudioVolume * masterVolume else 0f
                userAudioLoopRestartJob?.cancel()
                userAudioLoopRestartJob = null
                if (_isPlaying.value) {
                    if (userAudioEnabled) userAudioPlayer?.play() else userAudioPlayer?.pause()
                }
            }
            LayerType.AMBIENCE -> {
                ambienceEnabled = enabled
                if (ambienceUsesNoise) {
                    ambienceNoiseGenerator.setVolume(if (ambienceEnabled) ambienceVolume * masterVolume else 0f)
                    if (_isPlaying.value) {
                        if (ambienceEnabled) ambienceNoiseGenerator.start() else ambienceNoiseGenerator.pause()
                    }
                } else {
                    ambiencePlayer?.volume = if (ambienceEnabled) ambienceVolume * masterVolume else 0f
                    if (_isPlaying.value) {
                        if (ambienceEnabled) ambiencePlayer?.play() else ambiencePlayer?.pause()
                    }
                }
            }
        }
    }

    override suspend fun setLayerStartOffset(type: LayerType, startOffsetMs: Long) {
        if (type != LayerType.USER_AUDIO) return
        userAudioStartOffsetMs = startOffsetMs.coerceAtLeast(0L)
        applyUserAudioLooping()
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
        ambienceNoiseGenerator.release()
        ambienceUsesNoise = false
        userAudioLoopRestartJob?.cancel()
        userAudioLoopRestartJob = null
        stopPositionUpdates()
    }
}
