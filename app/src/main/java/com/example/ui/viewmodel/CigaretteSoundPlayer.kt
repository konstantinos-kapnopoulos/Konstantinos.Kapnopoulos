package com.example.ui.viewmodel

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CigaretteSoundPlayer {
    private const val SAMPLE_RATE = 22050

    // Pre-calculated sound buffers
    private val drumBuffer: ShortArray
    private val highPitchBuffer: ShortArray

    init {
        // 1. Synthesize drum (bass kick sound with rapid pitch-down sweep)
        val drumDuration = 0.15
        val drumSamplesCount = (SAMPLE_RATE * drumDuration).toInt()
        drumBuffer = ShortArray(drumSamplesCount)
        var drumPhase = 0.0
        for (i in 0 until drumSamplesCount) {
            val t = i.toDouble() / SAMPLE_RATE
            // Drop frequency from 150Hz to 40Hz over time
            val freq = 150.0 - (150.0 - 40.0) * (t / drumDuration)
            drumPhase += 2.0 * Math.PI * freq / SAMPLE_RATE
            // Exponential decay envelope
            val amplitude = 32767.0 * Math.exp(-6.0 * (t / drumDuration))
            drumBuffer[i] = (Math.sin(drumPhase) * amplitude).toInt().toShort()
        }

        // 2. Synthesize high-pitched tone (gentle ding)
        val toneDuration = 0.12
        val toneSamplesCount = (SAMPLE_RATE * toneDuration).toInt()
        highPitchBuffer = ShortArray(toneSamplesCount)
        var tonePhase = 0.0
        val toneFreq = 1200.0
        for (i in 0 until toneSamplesCount) {
            val t = i.toDouble() / SAMPLE_RATE
            tonePhase += 2.0 * Math.PI * toneFreq / SAMPLE_RATE
            // Exponential decay envelope for gentle release
            val amplitude = 32767.0 * Math.exp(-8.0 * (t / toneDuration))
            highPitchBuffer[i] = (Math.sin(tonePhase) * amplitude).toInt().toShort()
        }
    }

    fun playDrum(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            playBuffer(drumBuffer)
        }
    }

    fun playHighPitch(scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            playBuffer(highPitchBuffer)
        }
    }

    private fun playBuffer(samples: ShortArray) {
        var audioTrack: AudioTrack? = null
        try {
            val bufferSize = samples.size * 2
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(if (bufferSize > 0) bufferSize else 1024)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Wait until playback completes (duration in ms + safety padding)
            val durationMs = ((samples.size.toDouble() / SAMPLE_RATE) * 1000).toLong()
            Thread.sleep(durationMs + 50)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
