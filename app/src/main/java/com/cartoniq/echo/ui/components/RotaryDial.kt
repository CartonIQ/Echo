package com.cartoniq.echo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.echo.ui.theme.DarkCard
import com.cartoniq.echo.ui.theme.DarkCardBorder
import com.cartoniq.echo.ui.theme.DaylightCard
import com.cartoniq.echo.ui.theme.DaylightCardBorder
import com.cartoniq.echo.ui.theme.DaylightPrimary
import com.cartoniq.echo.ui.theme.DaylightTextSecondary
import com.cartoniq.echo.ui.theme.EchoTypography
import com.cartoniq.echo.ui.theme.HologramBlue
import com.cartoniq.echo.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryDial(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    title: String,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier,
    isDaylight: Boolean = false,
    dialSize: Dp = 100.dp
) {
    val haptic = LocalHapticFeedback.current
    var lastHapticStep by remember { mutableFloatStateOf(value) }

    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)

    val startAngle = 135f
    val sweepAngle = 270f
    val currentAngle = startAngle + (fraction * sweepAngle)

    val primaryColor = if (isDaylight) DaylightPrimary else HologramBlue
    val cardColor = if (isDaylight) DaylightCard else DarkCard
    val borderColor = if (isDaylight) DaylightCardBorder else DarkCardBorder
    val trackBgColor = if (isDaylight) Color(0xFFCBD5E1) else Color(0xFF1C2530)
    val labelColor = if (isDaylight) DaylightTextSecondary else TextSecondary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = EchoTypography.labelSmall.copy(color = labelColor, fontSize = 10.sp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(dialSize)
                .pointerInput(range) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y * 0.006f
                        val newFraction = (fraction + delta).coerceIn(0f, 1f)
                        val newValue = range.start + newFraction * (range.endInclusive - range.start)

                        if (kotlin.math.abs(newValue - lastHapticStep) >= (range.endInclusive - range.start) * 0.10f) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            lastHapticStep = newValue
                        }

                        onValueChange(newValue)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(dialSize)) {
                val center = this.center
                val radius = this.size.minDimension / 2.0f - 6.dp.toPx()

                drawCircle(
                    color = cardColor,
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = borderColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                drawArc(
                    color = trackBgColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    color = primaryColor,
                    startAngle = startAngle,
                    sweepAngle = fraction * sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                val angleRad = Math.toRadians(currentAngle.toDouble())
                val innerPoint = Offset(
                    (center.x + (radius * 0.45f) * cos(angleRad)).toFloat(),
                    (center.y + (radius * 0.45f) * sin(angleRad)).toFloat()
                )
                val outerPoint = Offset(
                    (center.x + (radius * 0.85f) * cos(angleRad)).toFloat(),
                    (center.y + (radius * 0.85f) * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = primaryColor,
                    start = innerPoint,
                    end = outerPoint,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = valueFormatter(value),
            style = EchoTypography.headlineMedium.copy(fontSize = 15.sp, color = primaryColor)
        )
    }
}
