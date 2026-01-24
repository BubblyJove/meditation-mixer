package com.meditationmixer.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MeditationColors.accentPrimary,
    onPrimary = MeditationColors.textPrimary,
    primaryContainer = MeditationColors.accentDark,
    onPrimaryContainer = MeditationColors.textPrimary,
    
    secondary = MeditationColors.accentSecondary,
    onSecondary = MeditationColors.textPrimary,
    secondaryContainer = MeditationColors.surfaceElevated,
    onSecondaryContainer = MeditationColors.textPrimary,
    
    tertiary = MeditationColors.sliderEnd,
    onTertiary = MeditationColors.textPrimary,
    
    background = MeditationColors.backgroundDark,
    onBackground = MeditationColors.textPrimary,
    
    surface = MeditationColors.surfaceDark,
    onSurface = MeditationColors.textPrimary,
    surfaceVariant = MeditationColors.surfaceElevated,
    onSurfaceVariant = MeditationColors.textSecondary,
    
    outline = MeditationColors.textMuted,
    outlineVariant = MeditationColors.neuLight,
    
    scrim = Color.Black.copy(alpha = 0.5f),
    
    inverseSurface = MeditationColors.textPrimary,
    inverseOnSurface = MeditationColors.backgroundDark,
    inversePrimary = MeditationColors.accentDark
)

@Composable
fun MeditationMixerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MeditationTypography,
        content = content
    )
}
