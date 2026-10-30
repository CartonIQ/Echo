package com.cartoniq.echo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import android.util.Log
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt

data class AudioMetrics(
    val rms: Float = 0f,
    val peak: Float = 0f,
    val isClipping: Boolean = false
)

data class HardwareEffectsStatus(
    val noiseSuppressorSupported: Boolean = false,
    val acousticEchoCancelerSupported: Boolean = false,
    val automaticGainControlSupported: Boolean = false
)

class AudioEngine(private val context: Context) {

    companion object {
        private const val TAG = "EchoAudioEngine"
        private const val DEFAULT_SAMPLE_RATE = 44100
        private const val FALLBACK_SAMPLE_RATE = 16000
        private const val CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var audioExecutor: ExecutorService? = null
    private val isRunning = AtomicBoolean(false)

    // Hardware Audio Effects
    private var noiseSuppressor: NoiseSuppressor? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null
    private var automaticGainControl: AutomaticGainControl? = null

    // Audio Focus
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    // Real-time parameters
    @Volatile var micGain: Float = 1.2f
    @Volatile var processingIntensity: Float = 0.5f
    @Volatile var isStudioModeActive: Boolean = false

    var metricsCallback: ((AudioMetrics) -> Unit)? = null
    var onErrorCallback: ((String) -> Unit)? = null
    var onHeadsetDisconnectedCallback: (() -> Unit)? = null

    fun getHardwareEffectsStatus(): HardwareEffectsStatus {
        return HardwareEffectsStatus(
            noiseSuppressorSupported = NoiseSuppressor.isAvailable(),
            acousticEchoCancelerSupported = AcousticEchoCanceler.isAvailable(),
            automaticGainControlSupported = AutomaticGainControl.isAvailable()
        )
    }

    fun isHeadsetConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    device.type == AudioDeviceInfo.TYPE_USB_HEADSET
                ) {
                    return true
                }
            }
            return false
        } else {
            @Suppress("DEPRECATION")
            return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
        }
    }

    fun getOutputDeviceDescription(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                when (device.type) {
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> return "🎧 Wired Headphones Active"
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> return "📶 Bluetooth Headset Connected"
                }
            }
        } else {
            @Suppress("DEPRECATION")
            if (audioManager.isWiredHeadsetOn) return "🎧 Wired Headphones Active"
            @Suppress("DEPRECATION")
            if (audioManager.isBluetoothA2dpOn) return "📶 Bluetooth Headset Connected"
        }
        return "📢 Phone Speaker (Caution: Keep gain low)"
    }

    @Synchronized
    fun start(): Boolean {
        if (isRunning.get()) return true

        if (!requestAudioFocus()) {
            onErrorCallback?.invoke("Audio focus request denied by system.")
            return false
        }

        var sampleRate = DEFAULT_SAMPLE_RATE
        var minRecBufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_IN, AUDIO_ENCODING)
        if (minRecBufferSize <= 0) {
            sampleRate = FALLBACK_SAMPLE_RATE
            minRecBufferSize = AudioRecord.getMinBufferSize(sampleRate, CHANNEL_IN, AUDIO_ENCODING)
        }

        if (minRecBufferSize <= 0) {
            onErrorCallback?.invoke("AudioRecord minBufferSize error: $minRecBufferSize")
            abandonAudioFocus()
            return false
        }

        val recBufferSize = (minRecBufferSize * 2).coerceAtLeast(1024)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                CHANNEL_IN,
                AUDIO_ENCODING,
                recBufferSize
            )
        } catch (e: SecurityException) {
            onErrorCallback?.invoke("Missing RECORD_AUDIO permission: ${e.message}")
            abandonAudioFocus()
            return false
        } catch (e: Exception) {
            onErrorCallback?.invoke("Failed to create AudioRecord: ${e.message}")
            abandonAudioFocus()
            return false
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            onErrorCallback?.invoke("AudioRecord failed to initialize state.")
            releaseAudioRecord()
            abandonAudioFocus()
            return false
        }

        val sessionId = audioRecord?.audioSessionId ?: 0
        if (sessionId != 0) {
            attachHardwareEffects(sessionId)
        }

        var minTrackBufferSize = AudioTrack.getMinBufferSize(sampleRate, CHANNEL_OUT, AUDIO_ENCODING)
        if (minTrackBufferSize <= 0) {
            minTrackBufferSize = recBufferSize
        }
        val trackBufferSize = (minTrackBufferSize * 2).coerceAtLeast(1024)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(CHANNEL_OUT)
            .setEncoding(AUDIO_ENCODING)
            .build()

        audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            trackBufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            onErrorCallback?.invoke("AudioTrack failed to initialize state.")
            releaseAudioTrack()
            releaseAudioRecord()
            abandonAudioFocus()
            return false
        }

        try {
            audioRecord?.startRecording()
            audioTrack?.play()
        } catch (e: Exception) {
            onErrorCallback?.invoke("Error starting recording/playback: ${e.message}")
            stop()
            return false
        }

        isRunning.set(true)
        audioExecutor = Executors.newSingleThreadExecutor()
        val buffer = ShortArray(512)

        audioExecutor?.execute {
            val audioRecordRef = audioRecord
            val audioTrackRef = audioTrack

            while (isRunning.get() && audioRecordRef != null && audioTrackRef != null) {
                val readSamples = audioRecordRef.read(buffer, 0, buffer.size)
                if (readSamples <= 0) {
                    if (readSamples == AudioRecord.ERROR_INVALID_OPERATION || readSamples == AudioRecord.ERROR_BAD_VALUE) {
                        break
                    }
                    continue
                }

                val currentGain = micGain
                val isStudio = isStudioModeActive
                val intensity = processingIntensity

                var sumSquares = 0.0
                var maxPeak = 0
                var clipping = false

                for (i in 0 until readSamples) {
                    var sampleVal = buffer[i].toFloat()
                    sampleVal *= currentGain

                    if (isStudio) {
                        sampleVal *= (1.0f + 0.35f * intensity)
                    }

                    val absVal = abs(sampleVal).toInt()
                    if (absVal > maxPeak) maxPeak = absVal

                    val limitThreshold = 32000f
                    if (sampleVal > limitThreshold) {
                        sampleVal = limitThreshold
                        clipping = true
                    } else if (sampleVal < -limitThreshold) {
                        sampleVal = -limitThreshold
                        clipping = true
                    }

                    val finalSample = sampleVal.toInt().toShort()
                    buffer[i] = finalSample
                    sumSquares += finalSample * finalSample
                }

                audioTrackRef.write(buffer, 0, readSamples)

                val rms = sqrt(sumSquares / readSamples) / 32768.0
                val peak = (maxPeak / 32768.0f).coerceIn(0f, 1f)

                metricsCallback?.invoke(
                    AudioMetrics(
                        rms = rms.toFloat().coerceIn(0f, 1f),
                        peak = peak,
                        isClipping = clipping
                    )
                )
            }
        }

        return true
    }

    private fun attachHardwareEffects(sessionId: Int) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(sessionId)
                noiseSuppressor?.enabled = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "NoiseSuppressor init error: ${e.message}")
        }

        try {
            if (AcousticEchoCanceler.isAvailable()) {
                acousticEchoCanceler = AcousticEchoCanceler.create(sessionId)
                acousticEchoCanceler?.enabled = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "AcousticEchoCanceler init error: ${e.message}")
        }

        try {
            if (AutomaticGainControl.isAvailable()) {
                automaticGainControl = AutomaticGainControl.create(sessionId)
                automaticGainControl?.enabled = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "AutomaticGainControl init error: ${e.message}")
        }
    }

    fun setNoiseSuppressorEnabled(enabled: Boolean) {
        noiseSuppressor?.enabled = enabled
    }

    fun setEchoCancelerEnabled(enabled: Boolean) {
        acousticEchoCanceler?.enabled = enabled
    }

    fun setAutomaticGainControlEnabled(enabled: Boolean) {
        automaticGainControl?.enabled = enabled
    }

    @Synchronized
    fun stop() {
        if (!isRunning.getAndSet(false)) return

        audioExecutor?.shutdownNow()
        audioExecutor = null

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "AudioRecord stop exception: ${e.message}")
        }

        try {
            audioTrack?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack stop exception: ${e.message}")
        }

        releaseAudioEffects()
        releaseAudioRecord()
        releaseAudioTrack()
        abandonAudioFocus()

        metricsCallback?.invoke(AudioMetrics(0f, 0f, false))
    }

    private fun releaseAudioEffects() {
        try {
            noiseSuppressor?.release()
            noiseSuppressor = null
            acousticEchoCanceler?.release()
            acousticEchoCanceler = null
            automaticGainControl?.release()
            automaticGainControl = null
        } catch (e: Exception) {
            Log.w(TAG, "Effects release error: ${e.message}")
        }
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.w(TAG, "AudioRecord release error: ${e.message}")
        }
    }

    private fun releaseAudioTrack() {
        try {
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack release error: ${e.message}")
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true

        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    stop()
                    onErrorCallback?.invoke("Audio focus lost.")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    stop()
                }
                AudioManager.AUDIOFOCUS_GAIN -> {}
            }
        }

        hasAudioFocus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            audioFocusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        hasAudioFocus = false
    }
}
