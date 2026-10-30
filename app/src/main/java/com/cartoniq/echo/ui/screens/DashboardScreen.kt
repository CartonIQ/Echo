package com.cartoniq.echo.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cartoniq.echo.R
import com.cartoniq.echo.ads.AdManager
import com.cartoniq.echo.ui.components.AudioOrbVisualizer
import com.cartoniq.echo.ui.components.PowerRing
import com.cartoniq.echo.ui.components.RotaryDial
import com.cartoniq.echo.ui.theme.CyberYellow
import com.cartoniq.echo.ui.theme.DarkCard
import com.cartoniq.echo.ui.theme.DarkCardBorder
import com.cartoniq.echo.ui.theme.DaylightBackground
import com.cartoniq.echo.ui.theme.DaylightCard
import com.cartoniq.echo.ui.theme.DaylightCardBorder
import com.cartoniq.echo.ui.theme.DaylightCyberAmber
import com.cartoniq.echo.ui.theme.DaylightLaserRed
import com.cartoniq.echo.ui.theme.DaylightPrimary
import com.cartoniq.echo.ui.theme.DaylightTextPrimary
import com.cartoniq.echo.ui.theme.DaylightTextSecondary
import com.cartoniq.echo.ui.theme.EchoTypography
import com.cartoniq.echo.ui.theme.HologramBlue
import com.cartoniq.echo.ui.theme.LaserRed
import com.cartoniq.echo.ui.theme.PureBlack
import com.cartoniq.echo.ui.theme.TextPrimary
import com.cartoniq.echo.ui.theme.TextSecondary
import com.cartoniq.echo.viewmodel.EchoViewModel
import com.cartoniq.echo.viewmodel.EngineState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun DashboardScreen(
    viewModel: EchoViewModel,
    onStartAudioRequested: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    val isDaylight = uiState.isDaylightTheme
    val bgColor = if (isDaylight) DaylightBackground else PureBlack
    val cardColor = if (isDaylight) DaylightCard else DarkCard
    val cardBorder = if (isDaylight) DaylightCardBorder else DarkCardBorder
    val primaryColor = if (isDaylight) DaylightPrimary else HologramBlue
    val textColor = if (isDaylight) DaylightTextPrimary else TextPrimary
    val textSecColor = if (isDaylight) DaylightTextSecondary else TextSecondary
    val warningRed = if (isDaylight) DaylightLaserRed else LaserRed
    val amberColor = if (isDaylight) DaylightCyberAmber else CyberYellow

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
            viewModel.dismissError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Brand, Theme Toggle, State Badge & Settings Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = EchoTypography.displayLarge.copy(fontSize = 20.sp, color = primaryColor)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Switcher Button (Daylight / Deep Space)
                    IconButton(
                        onClick = { viewModel.toggleThemeMode() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDaylight) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Daylight Outdoor Theme",
                            tint = if (isDaylight) amberColor else primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (uiState.engineState) {
                                    EngineState.Listening -> primaryColor.copy(alpha = 0.2f)
                                    EngineState.Starting -> amberColor.copy(alpha = 0.2f)
                                    EngineState.Error -> warningRed.copy(alpha = 0.2f)
                                    else -> cardColor
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when (uiState.engineState) {
                                    EngineState.Listening -> primaryColor
                                    EngineState.Starting -> amberColor
                                    EngineState.Error -> warningRed
                                    else -> cardBorder
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (uiState.engineState) {
                                EngineState.Listening -> stringResource(R.string.state_listening)
                                EngineState.Starting -> stringResource(R.string.state_starting)
                                EngineState.Stopping -> stringResource(R.string.state_stopping)
                                EngineState.Error -> stringResource(R.string.state_error)
                                EngineState.Idle -> stringResource(R.string.state_idle)
                            },
                            style = EchoTypography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = when (uiState.engineState) {
                                    EngineState.Listening -> primaryColor
                                    EngineState.Starting -> amberColor
                                    EngineState.Error -> warningRed
                                    else -> textSecColor
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = textSecColor
                        )
                    }
                }
            }

            // Audio Routing Status Chip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (uiState.isHeadphoneConnected) cardColor else warningRed.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = if (uiState.isHeadphoneConnected) cardBorder else warningRed.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (uiState.isHeadphoneConnected) Icons.Default.Headphones else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (uiState.isHeadphoneConnected) primaryColor else warningRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.outputDeviceDescription,
                        style = EchoTypography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = if (uiState.isHeadphoneConnected) textSecColor else warningRed
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Stage: PowerRing surrounding AudioOrbVisualizer
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                PowerRing(
                    engineState = uiState.engineState,
                    rmsLevel = uiState.rmsLevel,
                    size = 240.dp,
                    isDaylight = isDaylight,
                    onClick = {
                        if (activity != null) {
                            if (uiState.engineState == EngineState.Idle) {
                                onStartAudioRequested()
                            } else {
                                viewModel.toggleAudioSession(activity)
                            }
                        }
                    }
                ) {
                    AudioOrbVisualizer(
                        engineState = uiState.engineState,
                        rmsLevel = uiState.rmsLevel,
                        peakLevel = uiState.peakLevel,
                        isClipping = uiState.isClipping,
                        isDaylight = isDaylight,
                        size = 190.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Peak & RMS Live Meters
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(cardColor)
                    .border(1.dp, cardBorder, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LIVE PCM METER",
                        style = EchoTypography.labelSmall.copy(fontSize = 9.sp, color = textSecColor)
                    )
                    if (uiState.isClipping) {
                        Text(
                            text = stringResource(R.string.warning_clipping),
                            style = EchoTypography.labelSmall.copy(fontSize = 9.sp, color = warningRed)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { uiState.peakLevel },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (uiState.isClipping) warningRed else primaryColor,
                    trackColor = if (isDaylight) Color(0xFFCBD5E1) else Color(0xFF1B232D)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dual Tactile Rotary Dials: Mic Gain & Clarity Boost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RotaryDial(
                    value = uiState.micGain,
                    onValueChange = { viewModel.setMicGain(it) },
                    range = 0.5f..4.0f,
                    title = stringResource(R.string.dial_gain_title),
                    valueFormatter = { String.format("%.1fx", it) },
                    isDaylight = isDaylight,
                    dialSize = 88.dp
                )

                RotaryDial(
                    value = uiState.processingIntensity,
                    onValueChange = { viewModel.setProcessingIntensity(it) },
                    range = 0.0f..1.0f,
                    title = stringResource(R.string.dial_intensity_title),
                    valueFormatter = { String.format("%d%%", (it * 100).toInt()) },
                    isDaylight = isDaylight,
                    dialSize = 88.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Studio Mode Reward Button / Active Chip
            if (uiState.isStudioModeActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(amberColor.copy(alpha = 0.15f))
                        .border(1.dp, amberColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = amberColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.studio_mode_active, uiState.studioModeRemainingMinutes),
                                style = EchoTypography.labelSmall.copy(color = amberColor, fontSize = 11.sp)
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        activity?.let { viewModel.watchRewardedAdForStudioMode(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cardColor,
                        contentColor = amberColor
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, amberColor.copy(alpha = 0.6f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = amberColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.studio_mode_action),
                            style = EchoTypography.labelSmall.copy(color = amberColor, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Ad Container for Google AdMob Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdManager.BANNER_ID
                            AdManager.setBannerView(this)
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
