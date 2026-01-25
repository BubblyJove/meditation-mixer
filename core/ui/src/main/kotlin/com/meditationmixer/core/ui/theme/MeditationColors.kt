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
    
    // Accent colors (blue gradient)
    val accentPrimary = Color(0xFF2F80ED)
    val accentSecondary = Color(0xFF2D9CDB)
    val accentDark = Color(0xFF1B5FBF)
    
    // Text colors
    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFBFBFBF)
    val textMuted = Color(0xFF808080)
    
    // Slider track gradient colors
    val sliderStart = Color(0xFF2F80ED)
    val sliderEnd = Color(0xFF56CCF2)
    
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
            Color(0xFF2D9CDB),
            Color(0xFF2F80ED)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
    
    val accentWrapperGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2F80ED),
            Color(0xFF1B5FBF)
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
