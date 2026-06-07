package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class AmbientSynth {
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val sampleRate = 44100

    fun startPlaying(mode: String, scope: CoroutineScope) {
        stopPlaying()
        Log.d("AmbientSynth", "startPlaying requested for mode: $mode")

        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            var minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufSize <= 0) {
                minBufSize = 4096
            }
            val finalBufferSize = (minBufSize * 2).coerceAtLeast(4096)

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(finalBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (t: Throwable) {
                Log.e("AmbientSynth", "AudioTrack.Builder failed, trying fallback", t)
                audioTrack = null
            }

            // Universal fallback to the legacy constructor for maximum virtual/emulated/hardware target compatibility
            if (audioTrack == null || audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                audioTrack?.release()
                Log.d("AmbientSynth", "Using legacy AudioTrack constructor fallback")
                try {
                    @Suppress("DEPRECATION")
                    audioTrack = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        finalBufferSize,
                        AudioTrack.MODE_STREAM
                    )
                } catch (t: Throwable) {
                    Log.e("AmbientSynth", "Legacy AudioTrack constructor failed too", t)
                    audioTrack = null
                }
            }

            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                Log.e("AmbientSynth", "AudioTrack failed to initialize on this device container.")
                audioTrack?.release()
                audioTrack = null
                return
            }

            // Explicitly set a clear media play volume so it's fully audible on real phone hardware
            audioTrack?.setVolume(1.0f)
            audioTrack?.play()
            Log.d("AmbientSynth", "AudioTrack successfully initialized and playing.")
        } catch (e: Throwable) {
            Log.e("AmbientSynth", "Uncaught exception in startPlaying setup", e)
            audioTrack = null
            return
        }

        val trackRef = audioTrack ?: return

        synthJob = scope.launch(Dispatchers.Default) {
            val localTrack = trackRef // Robust thread-local encapsulation prevents audio stream mixing
            val buffer = ShortArray(1024)
            var phase = 0.0

            // Base frequencies depending on soundscape mode
            val freq1 = when (mode) {
                "Rainy Sketchbook" -> 110.0 // Low A Comfort chord
                "Campfire" -> 146.83        // D3 warmth
                "Space Journey" -> 220.0    // A3 drone
                "Ocean Drift" -> 164.81     // E3 wave swell
                "Wizard Library" -> 196.0   // G3 pentatonic chords
                "Cyberpunk Disco" -> 146.83 // Low D3 techno steps
                "Lofi Meadow" -> 174.61     // F3 Major calmness
                "Zen Bamboo Stream" -> 130.81 // Low C3 organic water
                "Cosmic Aurora" -> 220.0     // A3 celestial drone
                else -> 130.81              // C3 retro arcade
            }

            while (isActive) {
                if (localTrack.state != AudioTrack.STATE_INITIALIZED) break
                for (i in buffer.indices) {
                    val angle = 2.0 * Math.PI * freq1 / sampleRate
                    var value = sin(phase)

                    when (mode) {
                        "Rainy Sketchbook" -> {
                            // Rain patters overlaid on soft drone
                            if (Random.nextFloat() > 0.9965f) {
                                value += (Random.nextFloat() * 1.2f - 0.6f)
                            }
                        }
                        "Campfire" -> {
                            // Warm wood crackles
                            if (Random.nextFloat() > 0.9975f) {
                                value += (Random.nextFloat() * 1.5f - 0.75f)
                            }
                        }
                        "Space Journey" -> {
                            // Slow galactic phase sweep
                            val sweep = sin(2.0 * Math.PI * 0.1 * phase / sampleRate) * 30.0
                            val targetAngle = 2.0 * Math.PI * (freq1 + sweep) / sampleRate
                            phase += targetAngle
                            value = sin(phase)
                        }
                        "Ocean Drift" -> {
                            // Gradual high-fidelity swell volume
                            val swell = (sin(2.0 * Math.PI * 0.15 * phase / sampleRate) + 1.2) / 2.2
                            value *= swell
                        }
                        "Wizard Library" -> {
                            // Arpeggiated mystical spell steps
                            val noteIdx = (System.currentTimeMillis() / 450) % 5
                            val mult = when (noteIdx) {
                                0L -> 1.0
                                1L -> 1.2    // Bb3 (Minor third)
                                2L -> 1.5    // D4 (Fifth)
                                3L -> 1.8    // G4 (Octave)
                                else -> 2.25 // Bb4 (High notes)
                            }
                            val targetAngle = 2.0 * Math.PI * (freq1 * mult) / sampleRate
                            phase += targetAngle
                            value = sin(phase) * (0.8 + 0.2 * sin(2.0 * Math.PI * 6.0 * phase / sampleRate)) // vibrato
                        }
                        "Cyberpunk Disco" -> {
                            // Heavy techno square synth feel
                            val noteIdx = (System.currentTimeMillis() / 250) % 4
                            val synthFreq = when (noteIdx) {
                                0L -> freq1
                                1L -> freq1 * 1.5
                                2L -> freq1 * 0.75
                                else -> freq1 * 1.87
                            }
                            val targetAngle = 2.0 * Math.PI * synthFreq / sampleRate
                            phase += targetAngle
                            // Approximate Square Wave by taking sign
                            val rawValue = sin(phase)
                            value = if (rawValue > 0) 0.4 else -0.4
                        }
                        "Lofi Meadow" -> {
                            // Swelling chord waves with slight noise crackle
                            val swell = (sin(2.0 * Math.PI * 0.08 * phase / sampleRate) + 1.1) / 2.1
                            val chordSwell1 = sin(phase)
                            val chordSwell2 = sin(phase * 1.2) // Major 3rd
                            val chordSwell3 = sin(phase * 1.5)  // Perfect 5th
                            value = ((chordSwell1 + chordSwell2 + chordSwell3) / 3.0) * swell
                            if (Random.nextFloat() > 0.9985f) {
                                value += (Random.nextFloat() * 0.4f - 0.2f) // warm vinyl dust
                            }
                        }
                        "Zen Bamboo Stream" -> {
                            // Soft water droplets + gentle breathing flute vibrato
                            val vibrato = 1.0 + 0.03 * sin(2.0 * Math.PI * 4.0 * phase / sampleRate) // 4Hz gentle breath
                            val targetAngle = 2.0 * Math.PI * (freq1 * vibrato) / sampleRate
                            phase += targetAngle
                            value = sin(phase) * 0.7
                            // Gentle water splash drops
                            if (Random.nextFloat() > 0.997f) {
                                value += (Random.nextFloat() * 0.8f - 0.4f)
                            }
                        }
                        "Cosmic Aurora" -> {
                            // Swelling gold-ray chorus with slow sub-bass and high octave trails
                            val modulation = sin(2.0 * Math.PI * 0.12 * phase / sampleRate) // ultra-slow swell
                            val f1 = sin(phase)
                            val f2 = sin(phase * 2.0) // one octave up
                            val f3 = sin(phase * 0.5) // sub-bass fifth support
                            val f4 = sin(phase * 3.0) // high shimmer spark
                            val targetAngle = 2.0 * Math.PI * freq1 / sampleRate
                            phase += targetAngle
                            value = ((f1 * 0.4 + f2 * 0.3 + f3 * 0.2 + f4 * 0.1) * (0.8 + 0.2 * modulation))
                        }
                        "Retro Arcade" -> {
                            // Gentle nostalgic retro bleeps
                            val noteIdx = (System.currentTimeMillis() / 300) % 4
                            val bleepFreq = when (noteIdx) {
                                0L -> freq1
                                1L -> freq1 * 1.25
                                2L -> freq1 * 1.5
                                else -> freq1 * 1.87
                            }
                            val bleepAngle = 2.0 * Math.PI * bleepFreq / sampleRate
                            phase += bleepAngle
                            value = sin(phase)
                        }
                    }

                    if (mode != "Space Journey" && mode != "Retro Arcade" && mode != "Wizard Library" && mode != "Cyberpunk Disco" && mode != "Zen Bamboo Stream" && mode != "Cosmic Aurora") {
                        phase += angle
                    }

                    // Keep phase bound to avoid floating precision deterioration over long usage
                    if (phase > 2.0 * Math.PI * 1000.0) {
                        phase %= (2.0 * Math.PI)
                    }

                    // Map synthesised wave to 16-bit short audio range (expanded to 28000.0 for rich, clear, loud audibility in browser stream and speakers)
                    val shortVal = (value * 28000.0).toInt().coerceIn(-32768, 32767).toShort()
                    buffer[i] = shortVal
                }
                
                try {
                    val result = localTrack.write(buffer, 0, buffer.size)
                    if (result < 0) {
                        break
                    }
                } catch (e: Throwable) {
                    break
                }
                delay(12)
            }
        }
    }

    fun stopPlaying() {
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Throwable) {
            // Ignore
        }
        audioTrack = null
    }
}
