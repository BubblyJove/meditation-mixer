package com.meditationmixer.core.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object MeditationColors {
    // Base colors from the neumorphic design
    val backgroundDark = Color(0xFF25272A)
    val surfaceDark = Color(0xFF1E2024)
    val surfaceElevated = Color(0xFF2B2F34)
    
    // Neumorphic shadow colors
    val neuDark = Color(0xFF090A0C)
    val neuLight = Color(0xFF3E434C)
    
    // Accent colors (orange gradient)
    val accentPrimary = Color(0xFFEA5610)
    val accentSecondary = Color(0xFFD94D07)
    val accentDark = Color(0xFFB43613)
    
    // Text colors
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFBFBFBF)
    val textMuted = Color(0xFF808080)
    
    // Slider track gradient colors
    val sliderStart = Color(0xFFD93D07)
    val sliderEnd = Color(0xFF937114)
    
    // Gradients
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3E434C),
            Color(0xFF1E2024)
        )
    )
    
    val surfaceGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF31383D),
            Color(0xFF18191D)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val buttonGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2E3439),
            Color(0xFF1B1E22)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val buttonWrapperGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2B2F34),
            Color(0xFF31383D)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val accentGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDE3913),
            Color(0xFFEA510E)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val accentWrapperGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDA4D0C),
            Color(0xFFB43613)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val sliderTrackGradient = Brush.horizontalGradient(
        colors = listOf(
            sliderStart,
            sliderEnd
        )
    )
    
    val fadeGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFF1C1E22)
        )
    )
    
    // Neumorphic shadows
    val neuShadowLight = Color(0xFF4C575F)
    val neuShadowDark = Color(0xFF16191B)
}
