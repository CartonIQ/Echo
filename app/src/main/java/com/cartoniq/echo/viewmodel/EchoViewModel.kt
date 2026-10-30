package com.cartoniq.echo.viewmodel

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.echo.ads.AdManager
import com.cartoniq.echo.audio.AudioMetrics
import com.cartoniq.echo.audio.EchoService
import com.cartoniq.echo.audio.HardwareEffectsStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EngineState {
    Idle,
    Starting,
    Listening,
    Stopping,
    Error
}

data class EchoUiState(
    val engineState: EngineState = EngineState.Idle,
    val micGain: Float = 1.2f,
    val processingIntensity: Float = 0.5f,
    val isStudioModeActive: Boolean = false,
    val studioModeRemainingMinutes: Int = 0,
    val rmsLevel: Float = 0f,
    val peakLevel: Float = 0f,
    val isClipping: Boolean = false,
    val outputDeviceDescription: String = "Detecting audio route...",
    val isHeadphoneConnected: Boolean = false,
    val hardwareEffects: HardwareEffectsStatus = HardwareEffectsStatus(),
    val isNoiseSuppressorEnabled: Boolean = true,
    val isEchoCancelerEnabled: Boolean = true,
    val isAgcEnabled: Boolean = true,
    val isDaylightTheme: Boolean = false,
    val errorMessage: String? = null
)

class EchoViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_UI = "echo_ui_prefs"
        private const val KEY_THEME_DAYLIGHT = "is_daylight_theme"
    }

    private val _uiState = MutableStateFlow(EchoUiState())
    val uiState: StateFlow<EchoUiState> = _uiState.asStateFlow()

    private var echoService: EchoService? = null
    private var isBound = false
    private var sessionStartTime: Long = 0L

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val echoBinder = binder as? EchoService.EchoBinder ?: return
            echoService = echoBinder.getService()
            isBound = true

            val engine = echoService?.audioEngine ?: return
            engine.micGain = _uiState.value.micGain
            engine.processingIntensity = _uiState.value.processingIntensity
            engine.isStudioModeActive = _uiState.value.isStudioModeActive

            engine.metricsCallback = { metrics ->
                _uiState.value = _uiState.value.copy(
                    rmsLevel = metrics.rms,
                    peakLevel = metrics.peak,
                    isClipping = metrics.isClipping
                )
            }

            engine.onErrorCallback = { err ->
                _uiState.value = _uiState.value.copy(
                    engineState = EngineState.Error,
                    errorMessage = err
                )
            }

            val effects = engine.getHardwareEffectsStatus()
            val routeDesc = engine.getOutputDeviceDescription()
            val headphones = engine.isHeadsetConnected()

            _uiState.value = _uiState.value.copy(
                hardwareEffects = effects,
                outputDeviceDescription = routeDesc,
                isHeadphoneConnected = headphones,
                engineState = EngineState.Listening
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            echoService = null
            isBound = false
            _uiState.value = _uiState.value.copy(
                engineState = EngineState.Idle,
                rmsLevel = 0f,
                peakLevel = 0f,
                isClipping = false
            )
        }
    }

    init {
        loadThemePreference()
        checkStudioModeStatus()
        startStudioModeTimer()
    }

    private fun loadThemePreference() {
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE)
        val isDaylight = prefs.getBoolean(KEY_THEME_DAYLIGHT, false)
        _uiState.value = _uiState.value.copy(isDaylightTheme = isDaylight)
    }

    fun toggleThemeMode() {
        val newTheme = !_uiState.value.isDaylightTheme
        _uiState.value = _uiState.value.copy(isDaylightTheme = newTheme)
        val prefs = getApplication<Application>().getSharedPreferences(PREFS_UI, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_THEME_DAYLIGHT, newTheme).apply()
    }

    fun onMicPermissionGranted(context: Context) {
        startAudioService(context)
    }

    fun onMicPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            engineState = EngineState.Error,
            errorMessage = "Microphone permission is required to capture and amplify audio."
        )
    }

    fun toggleAudioSession(activity: Activity) {
        when (_uiState.value.engineState) {
            EngineState.Idle, EngineState.Error -> {
                startAudioService(activity)
            }
            EngineState.Listening -> {
                stopAudioSession(activity)
            }
            EngineState.Starting, EngineState.Stopping -> {}
        }
    }

    private fun startAudioService(context: Context) {
        _uiState.value = _uiState.value.copy(
            engineState = EngineState.Starting,
            errorMessage = null
        )
        sessionStartTime = System.currentTimeMillis()

        val intent = Intent(context, EchoService::class.java).apply {
            action = EchoService.ACTION_START
        }
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun stopAudioSession(activity: Activity) {
        _uiState.value = _uiState.value.copy(engineState = EngineState.Stopping)
        val sessionDuration = System.currentTimeMillis() - sessionStartTime

        if (isBound) {
            try {
                activity.unbindService(serviceConnection)
            } catch (e: Exception) {
                // Ignore
            }
            isBound = false
        }

        val stopIntent = Intent(activity, EchoService::class.java).apply {
            action = EchoService.ACTION_STOP
        }
        activity.startService(stopIntent)

        _uiState.value = _uiState.value.copy(
            engineState = EngineState.Idle,
            rmsLevel = 0f,
            peakLevel = 0f,
            isClipping = false
        )

        AdManager.maybeShowInterstitialAfterSession(activity, sessionDuration)
    }

    fun setMicGain(gain: Float) {
        val clamped = gain.coerceIn(0.5f, 4.0f)
        _uiState.value = _uiState.value.copy(micGain = clamped)
        echoService?.audioEngine?.micGain = clamped
    }

    fun setProcessingIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.0f, 1.0f)
        _uiState.value = _uiState.value.copy(processingIntensity = clamped)
        echoService?.audioEngine?.processingIntensity = clamped
    }

    fun toggleNoiseSuppressor() {
        val newState = !_uiState.value.isNoiseSuppressorEnabled
        _uiState.value = _uiState.value.copy(isNoiseSuppressorEnabled = newState)
        echoService?.audioEngine?.setNoiseSuppressorEnabled(newState)
    }

    fun toggleEchoCanceler() {
        val newState = !_uiState.value.isEchoCancelerEnabled
        _uiState.value = _uiState.value.copy(isEchoCancelerEnabled = newState)
        echoService?.audioEngine?.setEchoCancelerEnabled(newState)
    }

    fun toggleAgc() {
        val newState = !_uiState.value.isAgcEnabled
        _uiState.value = _uiState.value.copy(isAgcEnabled = newState)
        echoService?.audioEngine?.setAutomaticGainControlEnabled(newState)
    }

    fun watchRewardedAdForStudioMode(activity: Activity) {
        AdManager.showRewardedAd(
            activity,
            onRewardEarned = { checkStudioModeStatus() },
            onAdFailed = { err ->
                _uiState.value = _uiState.value.copy(errorMessage = err)
            }
        )
    }

    fun checkStudioModeStatus() {
        val expiry = AdManager.getStudioModeExpiry(getApplication())
        val now = System.currentTimeMillis()
        if (expiry > now) {
            val remainingMins = ((expiry - now) / 60000L).toInt()
            _uiState.value = _uiState.value.copy(
                isStudioModeActive = true,
                studioModeRemainingMinutes = remainingMins
            )
            echoService?.audioEngine?.isStudioModeActive = true
        } else {
            _uiState.value = _uiState.value.copy(
                isStudioModeActive = false,
                studioModeRemainingMinutes = 0
            )
            echoService?.audioEngine?.isStudioModeActive = false
        }
    }

    private fun startStudioModeTimer() {
        viewModelScope.launch {
            while (true) {
                delay(30000L)
                checkStudioModeStatus()
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
            } catch (e: Exception) {
                // Ignore
            }
            isBound = false
        }
    }
}
