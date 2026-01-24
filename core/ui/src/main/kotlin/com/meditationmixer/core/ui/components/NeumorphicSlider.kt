package com.meditationmixer.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize)
            .onSizeChanged { sliderWidth = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = (offset.x / sliderWidth).coerceIn(0f, 1f)
                    val mappedValue = valueRange.start + newValue * (valueRange.endInclusive - valueRange.start)
                    onValueChange(mappedValue)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    val newValue = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                    val mappedValue = valueRange.start + newValue * (valueRange.endInclusive - valueRange.start)
                    onValueChange(mappedValue)
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
                    // Inner shadow effect
                    drawRoundRect(
                        color = MeditationColors.textMuted.copy(alpha = 0.3f),
                        topLeft = Offset(0f, -1.dp.toPx()),
                        size = Size(size.width, size.height + 2.dp.toPx()),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
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
                .drawBehind {
                    // Thumb shadow and highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = size.minDimension / 2 - 2.dp.toPx(),
                        center = Offset(center.x, center.y - 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.3f),
                        radius = size.minDimension / 2 - 2.dp.toPx(),
                        center = Offset(center.x + 2.dp.toPx(), center.y + 2.dp.toPx())
                    )
                }
        )
    }
}
