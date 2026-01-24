package com.meditationmixer.core.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
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
            .clip(shape)
            .background(
                if (isPressed) MeditationColors.surfaceGradient
                else MeditationColors.buttonWrapperGradient
            )
            .neumorphicCardShadow(
                isPressed = isPressed,
                cornerRadius = cornerRadius,
                isCircular = isCircular
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
    darkColor: Color = MeditationColors.neuShadowDark.copy(alpha = 0.5f)
): Modifier = this.drawWithContent {
    drawContent()
    
    if (!isPressed) {
        // Draw subtle inner highlight on top-left
        drawRoundRect(
            color = lightColor,
            topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
            size = size.copy(
                width = size.width + 4.dp.toPx(),
                height = size.height + 4.dp.toPx()
            ),
            cornerRadius = if (isCircular) {
                CornerRadius(size.minDimension / 2)
            } else {
                CornerRadius(cornerRadius.toPx())
            },
            style = Stroke(width = 1.dp.toPx()),
            blendMode = BlendMode.Overlay
        )
    } else {
        // Draw inset shadow effect
        drawRoundRect(
            color = darkColor,
            topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
            size = size.copy(
                width = size.width - 4.dp.toPx(),
                height = size.height - 4.dp.toPx()
            ),
            cornerRadius = if (isCircular) {
                CornerRadius(size.minDimension / 2)
            } else {
                CornerRadius(cornerRadius.toPx())
            },
            style = Stroke(width = 2.dp.toPx()),
            blendMode = BlendMode.Overlay
        )
    }
}
