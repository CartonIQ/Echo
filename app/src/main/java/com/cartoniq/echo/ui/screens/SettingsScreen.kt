package com.cartoniq.echo.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.echo.R
import com.cartoniq.echo.ads.AdManager
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

@Composable
fun SettingsScreen(
    viewModel: EchoViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isDaylight = uiState.isDaylightTheme
    val bgColor = if (isDaylight) DaylightBackground else PureBlack
    val cardColor = if (isDaylight) DaylightCard else DarkCard
    val cardBorder = if (isDaylight) DaylightCardBorder else DarkCardBorder
    val primaryColor = if (isDaylight) DaylightPrimary else HologramBlue
    val textSecColor = if (isDaylight) DaylightTextSecondary else TextSecondary
    val warningRed = if (isDaylight) DaylightLaserRed else LaserRed
    val amberColor = if (isDaylight) DaylightCyberAmber else CyberYellow

    Scaffold(
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = EchoTypography.headlineMedium.copy(fontSize = 18.sp, color = primaryColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display & Outdoor Theme Toggle Section
            Text(
                text = stringResource(R.string.theme_section_title).uppercase(),
                style = EchoTypography.labelSmall.copy(color = textSecColor, fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardColor)
                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BrightnessMedium,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.theme_daylight_mode),
                                style = EchoTypography.titleMedium.copy(fontSize = 14.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.theme_daylight_desc),
                            style = EchoTypography.bodyMedium.copy(fontSize = 12.sp, color = textSecColor)
                        )
                    }

                    Switch(
                        checked = isDaylight,
                        onCheckedChange = { viewModel.toggleThemeMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DaylightBackground,
                            checkedTrackColor = DaylightPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = cardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            // Privacy & Architecture Card
            Text(
                text = "PRIVACY & AUDIO INTEGRITY",
                style = EchoTypography.labelSmall.copy(color = textSecColor, fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardColor)
                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.privacy_title),
                            style = EchoTypography.titleMedium.copy(fontSize = 14.sp, color = primaryColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.privacy_statement),
                        style = EchoTypography.bodyMedium.copy(fontSize = 13.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical Disclaimer Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardColor)
                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = amberColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.medical_disclaimer_title),
                            style = EchoTypography.titleMedium.copy(fontSize = 14.sp, color = amberColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.medical_disclaimer_text),
                        style = EchoTypography.bodyMedium.copy(fontSize = 13.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
// App Guide & Privacy
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(cardColor)
        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
        .padding(14.dp)
) {
    Column {
        Text(
            text = "App Guide",
            style = EchoTypography.titleMedium.copy(fontSize = 14.sp, color = primaryColor)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        var showHowToDialog by remember { mutableStateOf(false) }
        var showPrivacyDialog by remember { mutableStateOf(false) }

        Button(onClick = { showHowToDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("How to Use Echo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { showPrivacyDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Read Full Privacy Policy")
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Last updated: October 2026\n\nCartoniQ operates Echo. Echo is a 100% offline tool. We do not record, store, or transmit any audio. All audio is processed in real-time in your device's RAM.\n\nAds: We use Google AdMob to display ads. AdMob may collect your device's advertising ID.\n\nMedical: Echo is not a certified medical hearing aid.\n\nContact: cartoniq.business@gmail.com", lineHeight = 20.sp)
                    }
                },
                confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text("OK") } }
            )
        }

        if (showHowToDialog) {
            AlertDialog(
                onDismissRequest = { showHowToDialog = false },
                title = { Text("How to Use Echo", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("1. Plug in wired headphones or Bluetooth earbuds to avoid feedback (screeching).\n\n2. Tap the big STANDBY circle to start listening.\n\n3. Speak into the mic. Adjust the MIC GAIN and CLARITY dials.\n\n4. Tap the circle again to STOP.\n\n5. Watch an ad to unlock Studio DSP for 1 hour!", lineHeight = 20.sp)
                    }
                },
                confirmButton = { TextButton(onClick = { showHowToDialog = false }) { Text("Got it") } }
            )
        }
    }
}

Spacer(modifier = Modifier.height(16.dp))

            // Ad Consent Options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(cardColor)
                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.ad_consent_title),
                        style = EchoTypography.titleMedium.copy(fontSize = 14.sp, color = primaryColor)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.ad_consent_desc),
                        style = EchoTypography.bodyMedium.copy(fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            AdManager.resetConsent(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBorder,
                            contentColor = primaryColor
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = stringResource(R.string.ad_consent_reset), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EffectControlCard(
    title: String,
    isSupported: Boolean,
    isEnabled: Boolean,
    isDaylight: Boolean = false,
    onToggle: () -> Unit
) {
    val cardColor = if (isDaylight) DaylightCard else DarkCard
    val cardBorder = if (isDaylight) DaylightCardBorder else DarkCardBorder
    val primaryColor = if (isDaylight) DaylightPrimary else HologramBlue
    val textSecColor = if (isDaylight) DaylightTextSecondary else TextSecondary
    val warningRed = if (isDaylight) DaylightLaserRed else LaserRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardColor)
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = EchoTypography.titleMedium.copy(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
                        contentDescription = null,
                        tint = if (isSupported) primaryColor else warningRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSupported) {
                            stringResource(R.string.effect_supported)
                        } else {
                            stringResource(R.string.effect_unsupported)
                        },
                        style = EchoTypography.bodyMedium.copy(
                            fontSize = 11.sp,
                            color = if (isSupported) textSecColor else warningRed
                        )
                    )
                }
            }

            Switch(
                checked = isEnabled && isSupported,
                onCheckedChange = { onToggle() },
                enabled = isSupported,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isDaylight) DaylightBackground else PureBlack,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = textSecColor,
                    uncheckedTrackColor = cardBorder
                )
            )
        }
    }
}
