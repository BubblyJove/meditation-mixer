package com.mediationmixer.app.ui.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meditationmixer.core.ui.components.NeumorphicButton
import com.meditationmixer.core.ui.components.NeumorphicCard
import com.meditationmixer.core.ui.components.NeumorphicSlider
import com.meditationmixer.core.ui.theme.MeditationColors

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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        NeumorphicCard(
            modifier = Modifier.size(280.dp),
            isCircular = true
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(MeditationColors.surfaceGradient),
                contentAlignment = Alignment.Center
            ) {
                // Meditation visual - could be animated waves or a calm visual
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(MeditationColors.accentGradient)
                )
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
