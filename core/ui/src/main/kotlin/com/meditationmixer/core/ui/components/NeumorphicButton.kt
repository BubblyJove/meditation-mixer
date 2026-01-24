package com.meditationmixer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meditationmixer.core.ui.theme.MeditationColors

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    isCircular: Boolean = true,
    shadowRadius: Dp = 8.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val effectivePressed = isPressed || pressed
    
    val shape: Shape = if (isCircular) CircleShape else RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .neumorphicShadow(
                isPressed = effectivePressed,
                shadowRadius = shadowRadius,
                shape = shape
            )
            .clip(shape)
            .background(
                if (effectivePressed) MeditationColors.accentWrapperGradient
                else MeditationColors.buttonWrapperGradient
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun Modifier.neumorphicShadow(
    isPressed: Boolean,
    shadowRadius: Dp = 8.dp,
    shape: Shape = CircleShape,
    lightColor: Color = MeditationColors.neuShadowLight,
    darkColor: Color = MeditationColors.neuShadowDark
): Modifier = this.drawBehind {
    if (!isPressed) {
        val shadowRadiusPx = shadowRadius.toPx()
        val offsetPx = shadowRadiusPx / 2
        
        drawIntoCanvas { canvas ->
            val darkPaint = Paint().apply {
                color = darkColor
            }
            val lightPaint = Paint().apply {
                color = lightColor
            }
            
            // Dark shadow (bottom-right)
            canvas.drawCircle(
                center = Offset(center.x + offsetPx, center.y + offsetPx),
                radius = size.minDimension / 2,
                paint = darkPaint
            )
            
            // Light shadow (top-left)
            canvas.drawCircle(
                center = Offset(center.x - offsetPx, center.y - offsetPx),
                radius = size.minDimension / 2,
                paint = lightPaint
            )
        }
    }
}
