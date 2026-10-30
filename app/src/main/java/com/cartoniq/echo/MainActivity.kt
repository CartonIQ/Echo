package com.cartoniq.echo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.cartoniq.echo.ads.AdManager
import com.cartoniq.echo.navigation.EchoNavHost
import com.cartoniq.echo.ui.theme.EchoTheme
import com.cartoniq.echo.viewmodel.EchoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EchoViewModel by viewModels()

    private val requestRecordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicPermissionGranted(this)
        } else {
            viewModel.onMicPermissionDenied()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Notification permission response handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize User Messaging Platform (UMP) consent
        AdManager.initConsent(this) { canRequestAds ->
            if (canRequestAds) {
                AdManager.preloadAds(this)
            }
        }

        // Request POST_NOTIFICATIONS on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            EchoTheme(daylightMode = uiState.isDaylightTheme) {
                EchoNavHost(
                    viewModel = viewModel,
                    onStartAudioRequested = {
                        checkAndRequestMicPermission()
                    }
                )
            }
        }
    }

    private fun checkAndRequestMicPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onMicPermissionGranted(this)
        } else {
            requestRecordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroyBanner()
    }
}
