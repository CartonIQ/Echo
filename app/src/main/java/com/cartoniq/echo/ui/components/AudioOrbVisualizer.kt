package com.cartoniq.echo.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.echo.R
import com.cartoniq.echo.ui.theme.CyberYellow
import com.cartoniq.echo.ui.theme.DaylightBackground
import com.cartoniq.echo.ui.theme.DaylightCyberAmber
import com.cartoniq.echo.ui.theme.DaylightLaserRed
import com.cartoniq.echo.ui.theme.DaylightPrimary
import com.cartoniq.echo.ui.theme.DaylightSurface
import com.cartoniq.echo.ui.theme.EchoTypography
import com.cartoniq.echo.ui.theme.HologramBlue
import com.cartoniq.echo.ui.theme.LaserRed
import com.cartoniq.echo.ui.theme.PureBlack
import com.cartoniq.echo.viewmodel.EngineState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AudioOrbVisualizer(
    engineState: EngineState,
    rmsLevel: Float,
    peakLevel: Float,
    isClipping: Boolean,
    modifier: Modifier = Modifier,
    isDaylight: Boolean = false,
    size: Dp = 200.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rot"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = this.center
            val baseRadius = this.size.minDimension * 0.32f

            val activeBoost = if (engineState == EngineState.Listening) {
                rmsLevel * (this.size.minDimension * 0.18f)
            } else 0f
            val currentRadius = baseRadius + activeBoost

            val primaryColor = when {
                isClipping -> if (isDaylight) DaylightLaserRed else LaserRed
                engineState == EngineState.Listening -> if (isDaylight) DaylightPrimary else HologramBlue
                engineState == EngineState.Starting -> if (isDaylight) DaylightCyberAmber else CyberYellow
                engineState == EngineState.Error -> if (isDaylight) DaylightLaserRed else LaserRed
                else -> (if (isDaylight) DaylightPrimary else HologramBlue).copy(alpha = 0.4f)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (engineState == EngineState.Listening) 0.45f else 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.5f
                ),
                radius = currentRadius * 1.5f
            )

            drawCircle(
                color = if (isDaylight) DaylightSurface else PureBlack,
                radius = currentRadius,
                center = center
            )

            val path = Path()
            val points = 32
            val angleStep = (2 * Math.PI) / points

            for (i in 0 until points) {
                val angle = i * angleStep + Math.toRadians(rotationAngle.toDouble())
                val harmonic = if (engineState == EngineState.Listening) {
                    sin(angle * 4.0 + rmsLevel * 10.0) * (peakLevel * 18.dp.toPx())
                } else 0.0

                val r = currentRadius + harmonic
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = if (isClipping) 3.5.dp.toPx() else 2.dp.toPx())
            )

            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = currentRadius * 0.6f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = currentRadius * 0.3f,
                style = Stroke(width = 1.dp.toPx())
            )

            drawCircle(
                color = if (isClipping) (if (isDaylight) DaylightLaserRed else LaserRed) else primaryColor,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        val stateText = when (engineState) {
            EngineState.Idle -> stringResource(R.string.state_idle)
            EngineState.Starting -> stringResource(R.string.state_starting)
            EngineState.Listening -> stringResource(R.string.state_listening)
            EngineState.Stopping -> stringResource(R.string.state_stopping)
            EngineState.Error -> stringResource(R.string.state_error)
        }

        Text(
            text = stateText,
            style = EchoTypography.labelSmall.copy(
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = if (isClipping) (if (isDaylight) DaylightLaserRed else LaserRed)
                        else (if (isDaylight) DaylightPrimary else HologramBlue)
            )
        )
    }
}
