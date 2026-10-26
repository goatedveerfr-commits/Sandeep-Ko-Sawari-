package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class DesiGameAudioPlayer {

    private val sampleRate = 22050
    private var bgmTrack: AudioTrack? = null
    private var sfxTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private val audioScope = CoroutineScope(Dispatchers.Default)

    var isMuted: Boolean = false
        set(value) {
            field = value
            try {
                val vol = if (value) 0f else 1f
                bgmTrack?.setVolume(vol * 0.45f)
                sfxTrack?.setVolume(vol * 0.85f)
            } catch (e: Exception) {
                Log.w("AudioPlayer", "Error adjusting volume: ${e.message}")
            }
        }

    init {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            bgmTrack = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            sfxTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(format)
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            bgmTrack?.play()
            sfxTrack?.play()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to init AudioTrack: ${e.message}")
        }
    }

    fun startCatchyBgm() {
        if (bgmJob?.isActive == true) return
        bgmJob = audioScope.launch {
            // Upbeat Desi Arcade Theme: Dhol beat rhythm (Dhina-dhin-dha) + catchy melody
            // Frequencies in Hz for a lively Punjabi folk scale (D major / Bilawal)
            val notes = intArrayOf(
                294, 330, 370, 440,  // D4, E4, F#4, A4
                494, 440, 370, 330,  // B4, A4, F#4, E4
                294, 370, 440, 587,  // D4, F#4, A4, D5
                494, 440, 370, 294   // B4, A4, F#4, D4
            )
            val noteDurationSamples = sampleRate / 6 // ~166ms per 16th note step
            val buffer = ShortArray(noteDurationSamples)

            var step = 0
            while (isActive) {
                if (isMuted) {
                    kotlinx.coroutines.delay(100)
                    continue
                }

                val currentFreq = notes[step % notes.size]
                val isBassBeat = (step % 4 == 0) // Heavy dhol bass
                val isSnareBeat = (step % 2 == 1) // High snap

                for (i in 0 until noteDurationSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / noteDurationSamples
                    val decay = (1.0 - progress).coerceIn(0.0, 1.0)

                    // Melody sine wave + slight harmonic for retro chiptune organ sound
                    var sampleVal = (sin(2.0 * Math.PI * currentFreq * t) * 0.45 +
                            sin(4.0 * Math.PI * currentFreq * t) * 0.15) * decay

                    // Add synthesized Dhol Drum beat
                    if (isBassBeat) {
                        // Pitch drop 130Hz -> 55Hz
                        val dholFreq = 130.0 - (progress * 75.0)
                        sampleVal += sin(2.0 * Math.PI * dholFreq * t) * (1.0 - progress) * 0.6
                    } else if (isSnareBeat) {
                        // High crack
                        val snareNoise = (Math.random() * 2.0 - 1.0) * (1.0 - progress) * 0.35
                        sampleVal += snareNoise
                    }

                    // Soft clip to 16-bit PCM
                    val pcmVal = (sampleVal * 16000.0).toInt().coerceIn(-32000, 32000)
                    buffer[i] = pcmVal.toShort()
                }

                try {
                    bgmTrack?.write(buffer, 0, noteDurationSamples)
                } catch (e: Exception) {
                    // Ignore transient write errors
                }
                step++
            }
        }
    }

    fun playHorn() {
        if (isMuted) return
        audioScope.launch {
            // Classic Indian dual-tone loud honk ("PEE-PEEP!")
            val duration = (sampleRate * 0.28).toInt()
            val buf = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                // Dual horn acoustic harmony: 440Hz + 554Hz (Major 3rd)
                val wave = (sin(2.0 * Math.PI * 440.0 * t) + sin(2.0 * Math.PI * 554.0 * t) * 0.8) * 0.5
                // Add horn stutter modulation
                val mod = if ((i / 800) % 2 == 0) 1.0 else 0.85
                buf[i] = (wave * mod * 22000.0).toInt().coerceIn(-32000, 32000).toShort()
            }
            sfxTrack?.write(buf, 0, duration)
        }
    }

    fun playCoin() {
        if (isMuted) return
        audioScope.launch {
            // Bright cheerful coin chime
            val duration = (sampleRate * 0.12).toInt()
            val buf = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val freq = if (i < duration / 2) 988.0 else 1318.0 // B5 -> E6 chime
                val decay = 1.0 - (i.toDouble() / duration)
                val wave = sin(2.0 * Math.PI * freq * t) * decay * 0.7
                buf[i] = (wave * 20000.0).toInt().coerceIn(-32000, 32000).toShort()
            }
            sfxTrack?.write(buf, 0, duration)
        }
    }

    fun playNitro() {
        if (isMuted) return
        audioScope.launch {
            // Rushing rocket whoosh
            val duration = (sampleRate * 0.35).toInt()
            val buf = ShortArray(duration)
            for (i in 0 until duration) {
                val progress = i.toDouble() / duration
                val freq = 200.0 + progress * 800.0
                val noise = (Math.random() * 2.0 - 1.0) * 0.5
                val wave = (sin(2.0 * Math.PI * freq * (i.toDouble() / sampleRate)) * 0.5 + noise) * (1.0 - progress * 0.5)
                buf[i] = (wave * 18000.0).toInt().coerceIn(-32000, 32000).toShort()
            }
            sfxTrack?.write(buf, 0, duration)
        }
    }

    fun playCrash() {
        if (isMuted) return
        audioScope.launch {
            // Impact thud
            val duration = (sampleRate * 0.25).toInt()
            val buf = ShortArray(duration)
            for (i in 0 until duration) {
                val progress = i.toDouble() / duration
                val decay = (1.0 - progress) * (1.0 - progress)
                val noise = (Math.random() * 2.0 - 1.0) * 0.8
                val lowThud = sin(2.0 * Math.PI * 60.0 * (i.toDouble() / sampleRate)) * 0.7
                val wave = (lowThud + noise) * decay
                buf[i] = (wave * 26000.0).toInt().coerceIn(-32000, 32000).toShort()
            }
            sfxTrack?.write(buf, 0, duration)
        }
    }

    fun playShieldBlock() {
        if (isMuted) return
        audioScope.launch {
            // Protective resonant energy zap
            val duration = (sampleRate * 0.22).toInt()
            val buf = ShortArray(duration)
            for (i in 0 until duration) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / duration)
                val wave = sin(2.0 * Math.PI * 880.0 * t) * decay * 0.6
                buf[i] = (wave * 22000.0).toInt().coerceIn(-32000, 32000).toShort()
            }
            sfxTrack?.write(buf, 0, duration)
        }
    }

    fun playVictory() {
        if (isMuted) return
        audioScope.launch {
            // Fanfare
            val notes = intArrayOf(523, 659, 784, 1046) // C5, E5, G5, C6
            val noteLen = (sampleRate * 0.1).toInt()
            val buf = ShortArray(noteLen)
            for (freq in notes) {
                for (i in 0 until noteLen) {
                    val t = i.toDouble() / sampleRate
                    val wave = sin(2.0 * Math.PI * freq * t) * 0.6
                    buf[i] = (wave * 20000.0).toInt().coerceIn(-32000, 32000).toShort()
                }
                sfxTrack?.write(buf, 0, noteLen)
            }
        }
    }

    fun release() {
        bgmJob?.cancel()
        try {
            bgmTrack?.stop()
            bgmTrack?.release()
            sfxTrack?.stop()
            sfxTrack?.release()
        } catch (e: Exception) {
            // Ignore on cleanup
        }
    }
}
