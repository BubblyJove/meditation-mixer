package com.meditationmixer.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
                isCircular = isCircular,
                cornerRadius = cornerRadius
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
    isCircular: Boolean = true,
    cornerRadius: Dp = 16.dp,
    lightColor: Color = MeditationColors.neuShadowLight,
    darkColor: Color = MeditationColors.neuShadowDark
): Modifier = this.drawBehind {
    if (!isPressed) {
        val blurPx = shadowRadius.toPx().coerceAtLeast(1f)
        val offsetPx = blurPx / 2f

        val darkPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = darkColor.toArgb()
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        val lightPaint = AndroidPaint().apply {
            isAntiAlias = true
            color = lightColor.toArgb()
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas

            if (isCircular) {
                val radius = size.minDimension / 2f

                // Dark shadow (bottom-right)
                nativeCanvas.drawCircle(
                    center.x + offsetPx,
                    center.y + offsetPx,
                    radius,
                    darkPaint
                )

                // Light shadow (top-left)
                nativeCanvas.drawCircle(
                    center.x - offsetPx,
                    center.y - offsetPx,
                    radius,
                    lightPaint
                )
            } else {
                val r = cornerRadius.toPx().coerceAtLeast(0f)
                val rect = RectF(0f, 0f, size.width, size.height)

                // Dark shadow (bottom-right)
                nativeCanvas.drawRoundRect(
                    rect.left + offsetPx,
                    rect.top + offsetPx,
                    rect.right + offsetPx,
                    rect.bottom + offsetPx,
                    r,
                    r,
                    darkPaint
                )

                // Light shadow (top-left)
                nativeCanvas.drawRoundRect(
                    rect.left - offsetPx,
                    rect.top - offsetPx,
                    rect.right - offsetPx,
                    rect.bottom - offsetPx,
                    r,
                    r,
                    lightPaint
                )
            }
        }
    }
}
