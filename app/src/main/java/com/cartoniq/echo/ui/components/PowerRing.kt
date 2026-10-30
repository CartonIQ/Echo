package com.cartoniq.echo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cartoniq.echo.ui.theme.CyberYellow
import com.cartoniq.echo.ui.theme.DaylightCyberAmber
import com.cartoniq.echo.ui.theme.DaylightLaserRed
import com.cartoniq.echo.ui.theme.DaylightPrimary
import com.cartoniq.echo.ui.theme.HologramBlue
import com.cartoniq.echo.ui.theme.LaserRed
import com.cartoniq.echo.viewmodel.EngineState

@Composable
fun PowerRing(
    engineState: EngineState,
    rmsLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDaylight: Boolean = false,
    size: Dp = 260.dp,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "power_ring_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (engineState == EngineState.Listening) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringColor = when (engineState) {
        EngineState.Idle -> if (isDaylight) DaylightPrimary.copy(alpha = 0.5f) else HologramBlue.copy(alpha = 0.5f)
        EngineState.Starting -> if (isDaylight) DaylightCyberAmber else CyberYellow
        EngineState.Listening -> if (isDaylight) DaylightPrimary else HologramBlue
        EngineState.Stopping -> if (isDaylight) DaylightCyberAmber.copy(alpha = 0.6f) else CyberYellow.copy(alpha = 0.6f)
        EngineState.Error -> if (isDaylight) DaylightLaserRed else LaserRed
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val radius = (this.size.minDimension / 2.0f) - 14.dp.toPx()

            drawCircle(
                color = ringColor.copy(alpha = 0.2f),
                radius = radius + 8.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )

            val audioExpansion = if (engineState == EngineState.Listening) {
                (rmsLevel * 20.dp.toPx()) * pulseScale
            } else 0f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = if (engineState == EngineState.Listening) 0.35f else 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius + audioExpansion + 18.dp.toPx()
                ),
                radius = radius + audioExpansion + 18.dp.toPx()
            )

            drawCircle(
                color = ringColor,
                radius = radius,
                style = Stroke(width = if (engineState == EngineState.Listening) 4.dp.toPx() else 2.5.dp.toPx())
            )
        }

        content()
    }
}
