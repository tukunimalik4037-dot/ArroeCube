package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes and plays a short frequency tone offline with zero asset dependencies.
     */
    private fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.8f) {
        audioScope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)
                val angularFreq = 2.0 * Math.PI * frequency / sampleRate

                for (i in 0 until numSamples) {
                    // Apply linear attack and decay envelope to prevent clicks
                    val envelope = when {
                        i < 100 -> i / 100f
                        i > numSamples - 200 -> (numSamples - i) / 200f
                        else -> 1.0f
                    }
                    val sample = sin(i * angularFreq) * envelope * Short.MAX_VALUE * volume
                    buffer[i] = sample.toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Wait for playback and release
                Thread.sleep(durationMs.toLong() + 50)
                track.release()
            } catch (e: Exception) {
                // Graceful fallback
            }
        }
    }

    fun playTap(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(600.0, 40, 0.4f)
    }

    fun playFlySuccess(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            playTone(523.25, 60, 0.6f) // C5
            Thread.sleep(60)
            playTone(659.25, 70, 0.6f) // E5
            Thread.sleep(60)
            playTone(783.99, 90, 0.6f) // G5
        }
    }

    fun playBlocked(soundEnabled: Boolean) {
        if (!soundEnabled) return
        playTone(180.0, 100, 0.7f) // Low thud
    }

    fun playLevelWon(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (note in notes) {
                playTone(note, 120, 0.7f)
                Thread.sleep(110)
            }
        }
    }

    fun playHint(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            playTone(880.0, 70, 0.5f)
            Thread.sleep(70)
            playTone(1174.66, 100, 0.5f)
        }
    }

    fun vibrateShort(vibrationEnabled: Boolean) {
        if (!vibrationEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun vibrateBlocked(vibrationEnabled: Boolean) {
        if (!vibrationEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 40, 30)
                val amplitudes = intArrayOf(0, 200, 0, 200)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun vibrateSuccess(vibrationEnabled: Boolean) {
        if (!vibrationEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
