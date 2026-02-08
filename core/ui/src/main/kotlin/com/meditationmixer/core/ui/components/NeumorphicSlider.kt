package com.meditationmixer.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.meditationmixer.core.ui.theme.MeditationColors
import kotlin.math.roundToInt

@Composable
fun NeumorphicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val density = LocalDensity.current
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val thumbSize = 28.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val trackHeight = 8.dp

    val normalizedValue = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
        .coerceIn(0f, 1f)

    // Pre-calculate track shadow paints
    val trackBlurPx = remember { with(density) { 3.dp.toPx().coerceAtLeast(1f) } }
    val trackOffsetPx = remember(trackBlurPx) { trackBlurPx / 2f }
    val trackCornerPx = remember { with(density) { 4.dp.toPx() } }
    val trackDarkPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            color = MeditationColors.neuShadowDark.copy(alpha = 0.55f).toArgb()
            maskFilter = BlurMaskFilter(trackBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val trackLightPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            color = MeditationColors.neuShadowLight.copy(alpha = 0.25f).toArgb()
            maskFilter = BlurMaskFilter(trackBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    // Pre-calculate thumb shadow paints
    val thumbBlurPx = remember { with(density) { 6.dp.toPx().coerceAtLeast(1f) } }
    val thumbOffsetPx = remember(thumbBlurPx) { thumbBlurPx / 2f }
    val thumbDarkPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            color = Color.Black.copy(alpha = 0.35f).toArgb()
            maskFilter = BlurMaskFilter(thumbBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }
    val thumbLightPaint = remember {
        AndroidPaint().apply {
            isAntiAlias = true
            color = Color.White.copy(alpha = 0.18f).toArgb()
            maskFilter = BlurMaskFilter(thumbBlurPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val newValue = (down.position.x / sliderWidth).coerceIn(0f, 1f)
                    onValueChange(valueRange.start + newValue * (valueRange.endInclusive - valueRange.start))

                    do {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position ?: break
                        val dragValue = (pos.x / sliderWidth).coerceIn(0f, 1f)
                        onValueChange(valueRange.start + dragValue * (valueRange.endInclusive - valueRange.start))
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black)
                .drawBehind {
                    val rect = RectF(0f, 0f, size.width, size.height)

                    drawIntoCanvas { canvas ->
                        val nativeCanvas = canvas.nativeCanvas
                        nativeCanvas.drawRoundRect(
                            rect.left + trackOffsetPx,
                            rect.top + trackOffsetPx,
                            rect.right + trackOffsetPx,
                            rect.bottom + trackOffsetPx,
                            trackCornerPx,
                            trackCornerPx,
                            trackDarkPaint
                        )
                        nativeCanvas.drawRoundRect(
                            rect.left - trackOffsetPx,
                            rect.top - trackOffsetPx,
                            rect.right - trackOffsetPx,
                            rect.bottom - trackOffsetPx,
                            trackCornerPx,
                            trackCornerPx,
                            trackLightPaint
                        )
                    }
                }
        )

        // Active track (gradient fill)
        Box(
            modifier = Modifier
                .fillMaxWidth(normalizedValue)
                .height(trackHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(MeditationColors.sliderTrackGradient)
        )

        // Thumb
        Box(
            modifier = Modifier
                .offset {
                    val thumbOffset = ((sliderWidth - thumbSizePx) * normalizedValue).roundToInt()
                    IntOffset(thumbOffset, 0)
                }
                .size(thumbSize)
                .drawBehind {
                    val radius = (size.minDimension / 2f - thumbBlurPx).coerceAtLeast(0f)

                    if (radius > 0f) {
                        drawIntoCanvas { canvas ->
                            val nativeCanvas = canvas.nativeCanvas
                            nativeCanvas.drawCircle(
                                center.x + thumbOffsetPx,
                                center.y + thumbOffsetPx,
                                radius,
                                thumbDarkPaint
                            )
                            nativeCanvas.drawCircle(
                                center.x - thumbOffsetPx,
                                center.y - thumbOffsetPx,
                                radius,
                                thumbLightPaint
                            )
                        }
                    }
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MeditationColors.accentPrimary,
                            MeditationColors.accentPrimary,
                            Color(0xFF24292D),
                            Color(0xFF24292D),
                            Color(0xFF212529),
                            Color(0xFF212529)
                        ),
                        radius = thumbSizePx / 2
                    )
                )
        )
    }
}
