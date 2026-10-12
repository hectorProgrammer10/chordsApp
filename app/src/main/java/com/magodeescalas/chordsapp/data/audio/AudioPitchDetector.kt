package com.magodeescalas.chordsapp.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.magodeescalas.chordsapp.domain.model.GuitarConstants
import com.magodeescalas.chordsapp.domain.model.PitchDetectionResult
import com.magodeescalas.chordsapp.domain.model.TuningStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

class AudioPitchDetector {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 4096
        private const val YIN_THRESHOLD = 0.15f
        private const val MIN_RMS_VOLUME = 0.015f // Noise gate threshold
        private const val SMOOTHING_ALPHA = 0.35f // Exponential moving average weight
    }

    private val _detectionResult = MutableStateFlow(PitchDetectionResult())
    val detectionResult: StateFlow<PitchDetectionResult> = _detectionResult.asStateFlow()

    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val yinBuffer = FloatArray(BUFFER_SIZE / 2)

    // Smoothing filter state
    private var smoothedCents: Float? = null
    private var smoothedFreq: Float? = null
    private var lastMatchedStringIndex: Int = -1

    @SuppressLint("MissingPermission")
    fun start(scope: CoroutineScope) {
        if (recordingJob?.isActive == true) return

        recordingJob = scope.launch(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val recordBufferSize = maxOf(BUFFER_SIZE * 2, minBufferSize)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    recordBufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    audioRecord?.release()
                    audioRecord = null
                    return@launch
                }

                audioRecord?.startRecording()
            } catch (e: Exception) {
                audioRecord?.release()
                audioRecord = null
                return@launch
            }

            val audioBuffer = ShortArray(BUFFER_SIZE)
            val floatBuffer = FloatArray(BUFFER_SIZE)

            while (isActive) {
                val record = audioRecord ?: break
                if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) break

                val readCount = record.read(audioBuffer, 0, BUFFER_SIZE)
                if (readCount < BUFFER_SIZE) continue

                // Convert to normalized floats (-1.0 to 1.0) and compute RMS
                var sumSquares = 0.0
                for (i in 0 until BUFFER_SIZE) {
                    val sample = audioBuffer[i] / 32768.0f
                    floatBuffer[i] = sample
                    sumSquares += (sample * sample)
                }
                val rms = sqrt(sumSquares / BUFFER_SIZE).toFloat()

                if (rms < MIN_RMS_VOLUME) {
                    // Below noise floor, silence & reset filter
                    resetFilter()
                    _detectionResult.value = PitchDetectionResult(
                        volume = rms,
                        tuningStatus = TuningStatus.IDLE
                    )
                    continue
                }

                // Detect fundamental frequency using YIN
                val detectedPitch = calculateYinPitch(floatBuffer, SAMPLE_RATE)

                if (detectedPitch > 0f) {
                    val match = GuitarConstants.matchGuitarString(detectedPitch)
                    if (match != null) {
                        val (targetString, stringIndex, rawCents) = match

                        // Reset filter if string changed
                        if (stringIndex != lastMatchedStringIndex) {
                            smoothedCents = rawCents
                            smoothedFreq = detectedPitch
                            lastMatchedStringIndex = stringIndex
                        } else {
                            val prevCents = smoothedCents ?: rawCents
                            val prevFreq = smoothedFreq ?: detectedPitch
                            smoothedCents = prevCents + SMOOTHING_ALPHA * (rawCents - prevCents)
                            smoothedFreq = prevFreq + SMOOTHING_ALPHA * (detectedPitch - prevFreq)
                        }

                        val cents = smoothedCents ?: rawCents
                        val freq = smoothedFreq ?: detectedPitch

                        val status = when {
                            abs(cents) <= GuitarConstants.IN_TUNE_TOLERANCE_CENTS -> TuningStatus.IN_TUNE
                            cents < -GuitarConstants.IN_TUNE_TOLERANCE_CENTS -> TuningStatus.TIGHTEN
                            else -> TuningStatus.LOOSEN
                        }

                        _detectionResult.value = PitchDetectionResult(
                            frequency = freq,
                            targetString = targetString,
                            stringIndex = stringIndex,
                            cents = cents,
                            tuningStatus = status,
                            volume = rms,
                            isConfident = true
                        )
                    } else {
                        // Pitch detected but not close to any guitar string
                        resetFilter()
                        _detectionResult.value = PitchDetectionResult(
                            frequency = detectedPitch,
                            targetString = null,
                            stringIndex = -1,
                            cents = 0f,
                            tuningStatus = TuningStatus.IDLE,
                            volume = rms,
                            isConfident = false
                        )
                    }
                } else {
                    resetFilter()
                    _detectionResult.value = PitchDetectionResult(
                        volume = rms,
                        tuningStatus = TuningStatus.IDLE
                    )
                }
            }
        }
    }

    private fun resetFilter() {
        smoothedCents = null
        smoothedFreq = null
        lastMatchedStringIndex = -1
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {
        } finally {
            audioRecord = null
            _detectionResult.value = PitchDetectionResult()
        }
    }

    /**
     * YIN pitch estimation algorithm implementation
     */
    private fun calculateYinPitch(buffer: FloatArray, sampleRate: Int): Float {
        val halfSize = BUFFER_SIZE / 2

        // Step 1: Difference function
        for (tau in 0 until halfSize) {
            var sum = 0.0f
            for (i in 0 until halfSize) {
                val delta = buffer[i] - buffer[i + tau]
                sum += delta * delta
            }
            yinBuffer[tau] = sum
        }

        // Step 2: Cumulative mean normalized difference function (CMNDF)
        yinBuffer[0] = 1.0f
        var runningSum = 0.0f
        for (tau in 1 until halfSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] = if (runningSum > 0f) {
                (yinBuffer[tau] * tau) / runningSum
            } else {
                1.0f
            }
        }

        // Step 3: Absolute thresholding
        var tauEstimate = -1
        for (tau in 2 until halfSize) {
            if (yinBuffer[tau] < YIN_THRESHOLD) {
                while (tau + 1 < halfSize && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    // Walk down to local minimum
                    tauEstimate = tau + 1
                    break
                }
                if (tauEstimate == -1) {
                    tauEstimate = tau
                }
                break
            }
        }

        if (tauEstimate == -1) {
            // Find global minimum if below secondary threshold
            var minTau = 2
            var minVal = yinBuffer[2]
            for (tau in 3 until halfSize) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    minTau = tau
                }
            }
            if (minVal < 0.35f) {
                tauEstimate = minTau
            } else {
                return -1.0f
            }
        }

        // Step 4: Parabolic interpolation
        val betterTau = if (tauEstimate > 0 && tauEstimate < halfSize - 1) {
            val s0 = yinBuffer[tauEstimate - 1]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[tauEstimate + 1]
            val denominator = 2.0f * (2.0f * s1 - s2 - s0)
            if (abs(denominator) > 1e-6f) {
                tauEstimate + (s2 - s0) / denominator
            } else {
                tauEstimate.toFloat()
            }
        } else {
            tauEstimate.toFloat()
        }

        return if (betterTau > 0f) {
            sampleRate / betterTau
        } else {
            -1.0f
        }
    }
}
