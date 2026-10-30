package com.cartoniq.echo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DeepSpaceColorScheme = darkColorScheme(
    primary = HologramBlue,
    onPrimary = PureBlack,
    secondary = CyberYellow,
    onSecondary = PureBlack,
    tertiary = LaserRed,
    background = PureBlack,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = LaserRed
)

private val DaylightColorScheme = lightColorScheme(
    primary = DaylightPrimary,
    onPrimary = DaylightSurface,
    secondary = DaylightCyberAmber,
    onSecondary = DaylightSurface,
    tertiary = DaylightLaserRed,
    background = DaylightBackground,
    surface = DaylightSurface,
    onBackground = DaylightTextPrimary,
    onSurface = DaylightTextPrimary,
    error = DaylightLaserRed
)

@Composable
fun EchoTheme(
    daylightMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (daylightMode) DaylightColorScheme else DeepSpaceColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EchoTypography,
        content = content
    )
}
