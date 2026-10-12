package com.magodeescalas.chordsapp.domain.model

import kotlin.math.log2
import kotlin.math.roundToInt

data class GuitarString(
    val number: Int, // 1 to 6 (6 is lowest pitch E2, 1 is highest E4)
    val noteName: String,
    val octave: Int,
    val targetFrequency: Float
) {
    val displayName: String get() = "$noteName$octave"
    val fullLabel: String get() = "${number}ª ($noteName$octave)"
}

enum class TuningStatus {
    IDLE,
    TIGHTEN, // Pitch is too low (< -4 cents), need to tighten string peg (↑)
    IN_TUNE, // Pitch is within tune tolerance (±4 cents) (✓)
    LOOSEN   // Pitch is too high (> +4 cents), need to loosen string peg (↓)
}

data class PitchDetectionResult(
    val frequency: Float = 0f,
    val targetString: GuitarString? = null,
    val stringIndex: Int = -1, // 0 to 5 matching STANDARD_GUITAR_STRINGS
    val cents: Float = 0f,
    val tuningStatus: TuningStatus = TuningStatus.IDLE,
    val volume: Float = 0f,
    val isConfident: Boolean = false
)

object GuitarConstants {
    val STANDARD_GUITAR_STRINGS = listOf(
        GuitarString(number = 6, noteName = "E", octave = 2, targetFrequency = 82.41f),
        GuitarString(number = 5, noteName = "A", octave = 2, targetFrequency = 110.00f),
        GuitarString(number = 4, noteName = "D", octave = 3, targetFrequency = 146.83f),
        GuitarString(number = 3, noteName = "G", octave = 3, targetFrequency = 196.00f),
        GuitarString(number = 2, noteName = "B", octave = 3, targetFrequency = 246.94f),
        GuitarString(number = 1, noteName = "E", octave = 4, targetFrequency = 329.63f)
    )

    const val IN_TUNE_TOLERANCE_CENTS = 4.0f

    /**
     * Calculates deviation in cents: 1200 * log2(f / targetF)
     * Applies a -2.0 cents inharmonicity offset for the 1st (E4) and 6th (E2) strings to match acoustic tuning curves like GuitarTuna.
     */
    fun calculateCents(frequency: Float, targetFrequency: Float, stringIndex: Int = -1): Float {
        if (frequency <= 0f || targetFrequency <= 0f) return 0f
        val rawCents = (1200.0 * (log2(frequency.toDouble() / targetFrequency.toDouble()))).toFloat()
        return if (stringIndex == 0 || stringIndex == 5) {
            rawCents - 2.0f
        } else {
            rawCents
        }
    }

    /**
     * Finds the closest guitar string for a given frequency and returns its index and status.
     */
    fun matchGuitarString(frequency: Float): Triple<GuitarString, Int, Float>? {
        if (frequency < 55f || frequency > 450f) return null

        var closestIndex = -1
        var minCentsAbs = Float.MAX_VALUE
        var closestCents = 0f

        STANDARD_GUITAR_STRINGS.forEachIndexed { index, guitarString ->
            val cents = calculateCents(frequency, guitarString.targetFrequency, index)
            val centsAbs = kotlin.math.abs(cents)
            if (centsAbs < minCentsAbs) {
                minCentsAbs = centsAbs
                closestIndex = index
                closestCents = cents
            }
        }

        // Allow matching if within +/- 150 cents of a string
        return if (closestIndex != -1 && minCentsAbs < 150f) {
            Triple(STANDARD_GUITAR_STRINGS[closestIndex], closestIndex, closestCents)
        } else {
            null
        }
    }
}
