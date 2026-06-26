package com.example.python.interpreter

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class PhoneModule(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val toneGenerator by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            null
        }
    }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { t ->
                val result = t.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                }
            }
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PyKidTTS")
        } else {
            // Fallback, re-initialize and speak
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PyKidTTS")
                }
            }
        }
    }

    fun vibrate(durationMs: Long = 200) {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        }
    }

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun playSound(soundType: String) {
        val tone = when (soundType.lowercase().trim()) {
            "beep" -> ToneGenerator.TONE_PROP_BEEP
            "laser" -> ToneGenerator.TONE_SUP_DIAL
            "chime" -> ToneGenerator.TONE_PROP_ACK
            "alarm" -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD
            "success" -> ToneGenerator.TONE_PROP_ACK
            "error" -> ToneGenerator.TONE_PROP_NACK
            else -> ToneGenerator.TONE_PROP_BEEP
        }
        try {
            toneGenerator?.startTone(tone, 150)
        } catch (e: Exception) {
            // Handle tone generator errors silently
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {
            // Silent
        }
    }
}
