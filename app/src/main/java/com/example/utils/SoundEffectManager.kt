package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.exp

object SoundEffectManager {
    private var clickTrack: AudioTrack? = null
    private var coinTossTrack: AudioTrack? = null
    private var coinLandTrack: AudioTrack? = null
    private var isInitialized = false

    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val sampleRate = 44100

            // 1. Synthesize a clean, responsive soft click pop sound
            val clickDuration = 0.03
            val clickSamples = (sampleRate * clickDuration).toInt()
            val clickPcm = ShortArray(clickSamples)
            val clickFreq = 1100.0

            for (i in 0 until clickSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = exp(-120.0 * t)
                val value = sin(2 * PI * clickFreq * t) * envelope * 0.25
                clickPcm[i] = (value * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            // 2. Synthesize a clean, sweet, ascending retro arcade spin chime
            val coinDuration = 1.0
            val coinSamples = (sampleRate * coinDuration).toInt()
            val coinPcm = ShortArray(coinSamples)

            for (i in 0 until coinSamples) {
                val t = i.toDouble() / sampleRate
                
                // Beautiful rising pitch sweep representing the upward climb and rotation
                val freq = 800.0 + (1000.0 * (t / coinDuration))
                // Smooth vibrato/tremolo to represent the spin
                val wobble = 0.5 + 0.5 * sin(2 * PI * 18.0 * t)
                
                // Pure sine waves to guarantee no harshness or clipping
                val wave = (sin(2 * PI * freq * t) + 0.4 * sin(2 * PI * (freq * 1.5) * t)) / 1.4
                // Elegant exponential decay envelope
                val env = exp(-2.2 * t)
                
                val sampleValue = wave * env * wobble * 0.25
                coinPcm[i] = (sampleValue * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            // 3. Synthesize a beautiful, sparkling high-pitched double-tone "ding!" landing chime
            val landDuration = 0.9
            val landSamples = (sampleRate * landDuration).toInt()
            val landPcm = ShortArray(landSamples)

            for (i in 0 until landSamples) {
                val t = i.toDouble() / sampleRate
                
                // Two harmonious, sparkling high-frequency sine tones (E6 and B6 notes)
                val f1 = 1318.51 // E6 note
                val f2 = 1975.53 // B6 note
                
                val wave = (sin(2 * PI * f1 * t) + 0.6 * sin(2 * PI * f2 * t)) / 1.6
                // Smooth long decay for a satisfying ring-out tail
                val env = exp(-3.5 * t)
                
                val sampleValue = wave * env * 0.28
                landPcm[i] = (sampleValue * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            // Set up AudioAttributes & AudioFormat
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            // Initialize Static Tracks
            clickTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(clickPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            coinTossTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(coinPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            coinLandTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(landPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            // Write PCM buffers to hardware track memory
            clickTrack?.write(clickPcm, 0, clickPcm.size)
            coinTossTrack?.write(coinPcm, 0, coinPcm.size)
            coinLandTrack?.write(landPcm, 0, landPcm.size)

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrate(context: Context, durationMs: Long = 30L) {
        if (!isHapticEnabled) return
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick(context: Context? = null) {
        context?.let { vibrate(it, 25L) }
        if (!isSoundEnabled) return
        try {
            clickTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCoinToss(context: Context? = null) {
        context?.let { vibrate(it, 60L) }
        if (!isSoundEnabled) return
        try {
            coinTossTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCoinLand(context: Context? = null) {
        context?.let { vibrate(it, 40L) }
        if (!isSoundEnabled) return
        try {
            coinLandTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
