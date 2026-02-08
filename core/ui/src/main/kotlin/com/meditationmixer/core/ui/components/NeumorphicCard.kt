package com.meditationmixer.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meditationmixer.core.ui.theme.MeditationColors

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    isCircular: Boolean = false,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape: Shape = if (isCircular) CircleShape else RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .neumorphicCardShadow(
                isPressed = isPressed,
                cornerRadius = cornerRadius,
                isCircular = isCircular
            )
            .clip(shape)
            .background(
                if (isPressed) MeditationColors.surfaceGradient
                else MeditationColors.buttonWrapperGradient
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .clip(shape)
                .background(MeditationColors.surfaceGradient)
        ) {
            content()
        }
    }
}

fun Modifier.neumorphicCardShadow(
    isPressed: Boolean,
    cornerRadius: Dp = 20.dp,
    isCircular: Boolean = false,
    lightColor: Color = MeditationColors.neuShadowLight.copy(alpha = 0.3f),
    darkColor: Color = MeditationColors.neuShadowDark.copy(alpha = 0.5f),
    shadowRadius: Dp = 10.dp
): Modifier = composed {
    val density = LocalDensity.current
    val blurPx = remember(shadowRadius) { with(density) { shadowRadius.toPx().coerceAtLeast(1f) } }
    val offsetPx = remember(blurPx) { blurPx / 2f }
    val cornerPx = remember(cornerRadius) { with(density) { cornerRadius.toPx().coerceAtLeast(0f) } }

    val darkPaint = remember(darkColor, blurPx) {
        AndroidPaint().apply {
            isAntiAlias = true
            color = darkColor.toArgb()
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val lightPaint = remember(lightColor, blurPx) {
        AndroidPaint().apply {
            isAntiAlias = true
            color = lightColor.toArgb()
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    this.drawBehind {
        if (!isPressed) {
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                if (isCircular) {
                    val radius = size.minDimension / 2f

                    nativeCanvas.drawCircle(
                        center.x + offsetPx,
                        center.y + offsetPx,
                        radius,
                        darkPaint
                    )
                    nativeCanvas.drawCircle(
                        center.x - offsetPx,
                        center.y - offsetPx,
                        radius,
                        lightPaint
                    )
                } else {
                    val rect = RectF(0f, 0f, size.width, size.height)

                    nativeCanvas.drawRoundRect(
                        rect.left + offsetPx,
                        rect.top + offsetPx,
                        rect.right + offsetPx,
                        rect.bottom + offsetPx,
                        cornerPx,
                        cornerPx,
                        darkPaint
                    )
                    nativeCanvas.drawRoundRect(
                        rect.left - offsetPx,
                        rect.top - offsetPx,
                        rect.right - offsetPx,
                        rect.bottom - offsetPx,
                        cornerPx,
                        cornerPx,
                        lightPaint
                    )
                }
            }
        }
    }
}
