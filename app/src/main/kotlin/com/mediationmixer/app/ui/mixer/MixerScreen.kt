package com.mediationmixer.app.ui.mixer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import com.meditationmixer.core.domain.model.LayerType
import com.meditationmixer.core.ui.components.NeumorphicButton
import com.meditationmixer.core.ui.components.NeumorphicCard
import com.meditationmixer.core.ui.components.NeumorphicSlider
import com.meditationmixer.core.ui.theme.MeditationColors
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MixerScreen(
    onNavigateBack: () -> Unit,
    viewModel: MixerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            viewModel.dismissFilePicker()
        } else {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val name = queryDisplayName(context, uri)
            viewModel.onUserAudioSelected(uri.toString(), name)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTonePreview()
        }
    }

    LaunchedEffect(uiState.presetSaved, uiState.presetSaveError) {
        when {
            uiState.presetSaved -> {
                snackbarHostState.showSnackbar("Preset saved")
                viewModel.consumePresetSaveFeedback()
            }
            uiState.presetSaveError != null -> {
                snackbarHostState.showSnackbar(uiState.presetSaveError!!)
                viewModel.consumePresetSaveFeedback()
            }
        }
    }

    LaunchedEffect(uiState.tonePreviewError) {
        uiState.tonePreviewError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeTonePreviewError()
        }
    }

    LaunchedEffect(uiState.showFilePicker) {
        if (uiState.showFilePicker) {
            audioPickerLauncher.launch(arrayOf("audio/*"))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MeditationColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            MixerHeader(
                presetName = uiState.presetName,
                isFavorite = uiState.isFavorite,
                onBackClick = onNavigateBack,
                onFavoriteClick = viewModel::toggleFavorite
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Visual preview
            MixerVisual(
                toneEnabled = uiState.toneEnabled,
                toneVolume = uiState.toneVolume,
                userAudioEnabled = uiState.userAudioEnabled,
                userAudioVolume = uiState.userAudioVolume,
                ambienceEnabled = uiState.ambienceEnabled,
                ambienceVolume = uiState.ambienceVolume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Layer controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tone Generator Layer
                LayerCard(
                    icon = Icons.Default.GraphicEq,
                    title = "Tone Generator",
                    subtitle = "${uiState.toneFrequency.toInt()} Hz",
                    volume = uiState.toneVolume,
                    isLooping = true,
                    showLoop = false,
                    isEnabled = uiState.toneEnabled,
                    activityLevel = if (uiState.toneEnabled) uiState.toneVolume else 0f,
                    onEnabledToggle = viewModel::toggleToneEnabled,
                    onVolumeChange = viewModel::setToneVolume,
                    onLoopToggle = { },
                    extraContent = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            NeumorphicButton(
                                onClick = viewModel::toggleTonePreview,
                                isPressed = uiState.isTonePreviewing,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.isTonePreviewing) MeditationColors.accentGradient
                                            else MeditationColors.buttonGradient
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isTonePreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (uiState.isTonePreviewing) "Stop preview" else "Preview tone",
                                        tint = MeditationColors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        FrequencySlider(
                            frequency = uiState.toneFrequency,
                            onFrequencyChange = viewModel::setToneFrequency
                        )
                    }
                )

                // User Audio Layer
                LayerCard(
                    icon = Icons.Default.MusicNote,
                    title = "Your Audio",
                    subtitle = uiState.userAudioName ?: "Tap to import",
                    volume = uiState.userAudioVolume,
                    isLooping = uiState.userAudioLoop,
                    showLoop = uiState.userAudioName != null,
                    isEnabled = uiState.userAudioEnabled,
                    activityLevel = if (uiState.userAudioEnabled) uiState.userAudioVolume else 0f,
                    onEnabledToggle = viewModel::toggleUserAudioEnabled,
                    onVolumeChange = viewModel::setUserAudioVolume,
                    onLoopToggle = viewModel::toggleUserAudioLoop,
                    onCardClick = viewModel::importUserAudio,
                    extraContent = {
                        if (uiState.userAudioName != null && uiState.userAudioLoop) {
                            RepeatDelaySlider(
                                delayMs = uiState.userAudioRepeatDelayMs,
                                onDelayChangeMs = viewModel::setUserAudioRepeatDelayMs
                            )
                        }
                    }
                )

                // Ambience Layer
                LayerCard(
                    icon = Icons.Default.Nature,
                    title = "Ambience",
                    subtitle = uiState.ambienceName ?: "Rain Sounds",
                    volume = uiState.ambienceVolume,
                    isLooping = uiState.ambienceLoop,
                    showLoop = true,
                    isEnabled = uiState.ambienceEnabled,
                    activityLevel = if (uiState.ambienceEnabled) uiState.ambienceVolume else 0f,
                    onEnabledToggle = viewModel::toggleAmbienceEnabled,
                    onVolumeChange = viewModel::setAmbienceVolume,
                    onLoopToggle = viewModel::toggleAmbienceLoop,
                    onCardClick = viewModel::selectAmbience
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save preset button
            SavePresetButton(
                onClick = viewModel::savePreset
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )

        if (uiState.showAmbiencePicker) {
            AmbiencePickerDialog(
                currentId = uiState.ambienceAssetId,
                onDismiss = viewModel::dismissAmbiencePicker,
                onSelect = { id, name -> viewModel.onAmbienceSelected(id, name) }
            )
        }
    }
}

@Composable
private fun RepeatDelaySlider(
    delayMs: Long,
    onDelayChangeMs: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxDelayMs = 10_000L
    val normalized = (delayMs.toFloat() / maxDelayMs).coerceIn(0f, 1f)

    Column(modifier = modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Repeat delay",
                color = MeditationColors.textMuted,
                fontSize = 12.sp
            )
            Text(
                text = "${delayMs / 1000}s",
                color = MeditationColors.accentPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        NeumorphicSlider(
            value = normalized,
            onValueChange = { onDelayChangeMs((it * maxDelayMs).toLong()) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AmbiencePickerDialog(
    currentId: String?,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val options = listOf(
        "rain_light" to "Light Rain",
        "rain_heavy" to "Heavy Rain",
        "ocean_waves" to "Ocean Waves",
        "forest_night" to "Forest Night",
        "wind_soft" to "Soft Wind",
        "river_stream" to "River Stream"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Choose ambience", color = MeditationColors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { (id, name) ->
                    NeumorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        isPressed = currentId == id,
                        onClick = { onSelect(id, name) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                color = if (currentId == id) MeditationColors.textPrimary else MeditationColors.textSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = MeditationColors.accentPrimary)
            }
        }
    )
}

private fun queryDisplayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            return it.getString(nameIndex) ?: "Imported audio"
        }
    }
    return "Imported audio"
}

@Composable
private fun MixerHeader(
    presetName: String,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeumorphicButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MeditationColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = presetName.uppercase(),
            color = MeditationColors.textMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )

        NeumorphicButton(
            onClick = onFavoriteClick,
            isPressed = isFavorite,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MeditationColors.accentPrimary else MeditationColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MixerVisual(
    toneEnabled: Boolean,
    toneVolume: Float,
    userAudioEnabled: Boolean,
    userAudioVolume: Float,
    ambienceEnabled: Boolean,
    ambienceVolume: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mixerVisual")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val activity = listOf(
        if (toneEnabled) toneVolume.coerceIn(0f, 1f) else 0f,
        if (userAudioEnabled) userAudioVolume.coerceIn(0f, 1f) else 0f,
        if (ambienceEnabled) ambienceVolume.coerceIn(0f, 1f) else 0f
    )

    NeumorphicCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for audio visualization
            repeat(12) {
                val group = (it / 4).coerceIn(0, activity.lastIndex)
                val local = it % 4
                val a = activity[group]
                val wave = (sin(phase + it * 0.65f + local * 0.35f) + 1f) / 2f
                val heightDp = 10f + (60f * (0.35f + 0.65f * wave) * a)

                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(heightDp.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MeditationColors.accentGradient)
                )
            }
        }
    }
}

@Composable
private fun MiniWaveIndicator(
    activityLevel: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "miniWave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val a = activityLevel.coerceIn(0f, 1f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val wave = (sin(phase + i * 1.1f) + 1f) / 2f
            val h = 6f + (10f * wave * a)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        MeditationColors.accentPrimary.copy(alpha = 0.35f + 0.65f * a)
                    )
            )
            if (i < 2) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
private fun LayerCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    volume: Float,
    isLooping: Boolean,
    showLoop: Boolean,
    isEnabled: Boolean,
    activityLevel: Float,
    onEnabledToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLoopToggle: () -> Unit,
    onCardClick: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MeditationColors.accentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MiniWaveIndicator(activityLevel = activityLevel)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = MeditationColors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = subtitle,
                            color = MeditationColors.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                if (showLoop) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Loop,
                            contentDescription = "Loop",
                            tint = if (isLooping) MeditationColors.accentPrimary else MeditationColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isLooping,
                            onCheckedChange = { onLoopToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MeditationColors.accentPrimary,
                                checkedTrackColor = MeditationColors.accentDark,
                                uncheckedThumbColor = MeditationColors.textMuted,
                                uncheckedTrackColor = MeditationColors.surfaceDark
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Enabled",
                        tint = if (isEnabled) MeditationColors.accentPrimary else MeditationColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onEnabledToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MeditationColors.accentPrimary,
                            checkedTrackColor = MeditationColors.accentDark,
                            uncheckedThumbColor = MeditationColors.textMuted,
                            uncheckedTrackColor = MeditationColors.surfaceDark
                        )
                    )
                }
            }

            extraContent?.invoke()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vol",
                    color = MeditationColors.textMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                NeumorphicSlider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = MeditationColors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun FrequencySlider(
    frequency: Float,
    onFrequencyChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Frequency",
                color = MeditationColors.textMuted,
                fontSize = 12.sp
            )
            Text(
                text = "${frequency.toInt()} Hz",
                color = MeditationColors.accentPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        NeumorphicSlider(
            value = (frequency - 1f) / 39f, // 1-40 Hz range
            onValueChange = { onFrequencyChange(it * 39f + 1f) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1 Hz", color = MeditationColors.textMuted, fontSize = 10.sp)
            Text(text = "Delta", color = MeditationColors.textMuted, fontSize = 10.sp)
            Text(text = "Theta", color = MeditationColors.textMuted, fontSize = 10.sp)
            Text(text = "Alpha", color = MeditationColors.textMuted, fontSize = 10.sp)
            Text(text = "40 Hz", color = MeditationColors.textMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SavePresetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicButton(
        onClick = onClick,
        isCircular = false,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(MeditationColors.buttonGradient),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = MeditationColors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Preset",
                color = MeditationColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
