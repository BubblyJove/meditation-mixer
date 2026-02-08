package com.mediationmixer.app.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meditationmixer.core.ui.components.NeumorphicButton
import com.meditationmixer.core.ui.components.NeumorphicCard
import com.meditationmixer.core.ui.components.NeumorphicSlider
import com.meditationmixer.core.ui.theme.MeditationColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onNavigateToMixer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MeditationColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HomeHeader(
                onMenuClick = onNavigateToMixer
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main visual - Album art style
            MainVisual(
                progress = uiState.progress,
                isPlaying = uiState.isPlaying,
                elapsedFormatted = uiState.elapsedFormatted,
                presetName = uiState.currentPreset?.name ?: "",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title section
            TitleSection(
                presetName = uiState.currentPreset?.name ?: "Sleep Starter",
                subtitle = "Meditation Session"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress slider
            ProgressSection(
                progress = uiState.progress,
                elapsed = uiState.elapsedFormatted,
                remaining = uiState.remainingFormatted,
                onProgressChange = viewModel::onSeek
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Playback controls
            PlaybackControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = viewModel::onPlayPause,
                onPrevious = viewModel::onPreviousPreset,
                onNext = viewModel::onNextPreset
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeumorphicButton(
            onClick = { },
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Back",
                    tint = MeditationColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "PLAYING NOW",
            color = MeditationColors.textMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )

        NeumorphicButton(
            onClick = onMenuClick,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MeditationColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MainVisual(
    progress: Float,
    isPlaying: Boolean,
    elapsedFormatted: String,
    presetName: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "sessionProgress"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "mainVisual")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val ripplePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripplePhase"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer progress ring
        Canvas(modifier = Modifier.size(292.dp)) {
            val startAngle = -90f + if (isPlaying) rotation else 0f
            val sweep = 360f * animatedProgress

            val baseStroke = 8.dp.toPx()
            val strokeWidth = baseStroke * (1f + if (isPlaying) 0.12f * pulse else 0f)
            val trackAlpha = if (isPlaying) 0.28f else 0.18f
            val progressAlpha = if (isPlaying) 0.95f else 0.75f

            drawArc(
                color = MeditationColors.accentPrimary.copy(alpha = trackAlpha),
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = MeditationColors.accentPrimary.copy(alpha = progressAlpha),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (animatedProgress > 0.002f) {
                val angleRad = ((startAngle + sweep) * PI / 180.0)
                val radius = size.minDimension / 2f - strokeWidth / 2f
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val x = centerX + (cos(angleRad) * radius).toFloat()
                val y = centerY + (sin(angleRad) * radius).toFloat()
                drawCircle(
                    color = MeditationColors.accentPrimary.copy(alpha = progressAlpha),
                    radius = strokeWidth * 0.55f,
                    center = Offset(x, y)
                )
            }
        }

        // Inner card with visualization
        NeumorphicCard(
            modifier = Modifier.size(268.dp),
            isCircular = true
        ) {
            Box(
                modifier = Modifier
                    .size(264.dp)
                    .clip(CircleShape)
                    .background(MeditationColors.surfaceGradient),
                contentAlignment = Alignment.Center
            ) {
                // Animated ripple rings (playing) or breathing circle (idle)
                Canvas(modifier = Modifier.size(220.dp)) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val maxRadius = size.minDimension / 2f

                    if (isPlaying) {
                        // 4 concentric ripple rings expanding outward
                        for (i in 0 until 4) {
                            val phase = (ripplePhase + i * 0.25f) % 1f
                            val ringRadius = maxRadius * 0.2f + maxRadius * 0.8f * phase
                            val ringAlpha = (1f - phase) * 0.35f
                            drawCircle(
                                color = MeditationColors.accentPrimary.copy(alpha = ringAlpha),
                                radius = ringRadius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else {
                        // Gentle breathing circle
                        val breathRadius = maxRadius * 0.5f * breathe
                        drawCircle(
                            color = MeditationColors.accentPrimary.copy(alpha = 0.15f),
                            radius = breathRadius,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = MeditationColors.accentPrimary.copy(alpha = 0.08f),
                            radius = breathRadius * 1.3f,
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                // Text overlay: elapsed time + preset name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = elapsedFormatted,
                        color = MeditationColors.textPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (presetName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = presetName,
                            color = MeditationColors.textMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleSection(
    presetName: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = presetName,
            color = MeditationColors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = MeditationColors.textMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressSection(
    progress: Float,
    elapsed: String,
    remaining: String,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        NeumorphicSlider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsed,
                color = MeditationColors.textMuted,
                fontSize = 12.sp
            )
            Text(
                text = remaining,
                color = MeditationColors.textMuted,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeumorphicButton(
            onClick = onPrevious,
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MeditationColors.textMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        NeumorphicButton(
            onClick = onPlayPause,
            isPressed = isPlaying,
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) MeditationColors.accentGradient
                        else MeditationColors.buttonGradient
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MeditationColors.textPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        NeumorphicButton(
            onClick = onNext,
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = MeditationColors.textMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
