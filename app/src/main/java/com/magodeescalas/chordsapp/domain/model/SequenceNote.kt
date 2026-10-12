package com.magodeescalas.chordsapp.domain.model

/**
 * Normalizes enarmonics (e.g. Db -> C#, Eb -> D#, etc.)
 */
fun normalizePitch(note: String): String {
    return when (note) {
        "Db" -> "C#"
        "Eb" -> "D#"
        "Gb" -> "F#"
        "Ab" -> "G#"
        "Bb" -> "A#"
        else -> note
    }
}

/**
 * Standard open string MIDI numbers for standard guitar tuning (EADGBE):
 * String 0 (High e): E4 (MIDI 64)
 * String 1 (B):      B3 (MIDI 59)
 * String 2 (G):      G3 (MIDI 55)
 * String 3 (D):      D3 (MIDI 50)
 * String 4 (A):      A2 (MIDI 45)
 * String 5 (Low E):  E2 (MIDI 40)
 */
private val OPEN_STRING_MIDI = listOf(64, 59, 55, 50, 45, 40)
private val CHROMATIC_NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

/**
 * Represents a musical note in a sequence for both Guitar Tablature and Piano Roll.
 */
data class SequenceNote(
    val note: String,          // e.g. "C", "F#"
    val octave: Int = 4,       // e.g. 4 for C4
    val stringIndex: Int = -1, // 0 = e, 1 = B, 2 = G, 3 = D, 4 = A, 5 = E (-1 if unspecified)
    val fret: Int = -1         // 0..14 (-1 if unspecified)
) {
    val normalizedNote: String
        get() = normalizePitch(note)

    val midiNoteNumber: Int
        get() = (octave + 1) * 12 + CHROMATIC_NOTES.indexOf(normalizedNote).coerceAtLeast(0)

    val stringLabel: String
        get() = when (stringIndex) {
            0 -> "e"
            1 -> "B"
            2 -> "G"
            3 -> "D"
            4 -> "A"
            5 -> "E"
            else -> ""
        }

    /**
     * Checks if this sequence note matches a guitar fret position.
     */
    fun matchesGuitar(checkString: Int, checkFret: Int, checkNote: String): Boolean {
        if (stringIndex >= 0 && fret >= 0) {
            return stringIndex == checkString && fret == checkFret
        }
        return normalizedNote == normalizePitch(checkNote)
    }

    /**
     * Checks if this sequence note matches a piano key.
     */
    fun matchesPiano(checkNote: String, checkOctave: Int): Boolean {
        if (octave >= 0) {
            return normalizedNote == normalizePitch(checkNote) && octave == checkOctave
        }
        return normalizedNote == normalizePitch(checkNote)
    }

    /**
     * Serializes to a compact string token saved in presets and template notes.
     */
    fun toToken(): String {
        return "$note:$octave:$stringIndex:$fret"
    }

    companion object {
        /**
         * Creates a SequenceNote from a guitar fret click.
         * Computes the exact octave and note name using guitar tuning.
         */
        fun fromGuitar(stringIndex: Int, fret: Int, noteName: String): SequenceNote {
            val baseMidi = OPEN_STRING_MIDI.getOrElse(stringIndex) { 64 }
            val totalMidi = baseMidi + fret
            val octave = (totalMidi / 12) - 1
            val noteIndex = totalMidi % 12
            val calculatedNote = CHROMATIC_NOTES.getOrElse(noteIndex) { noteName }
            return SequenceNote(
                note = calculatedNote,
                octave = octave,
                stringIndex = stringIndex,
                fret = fret
            )
        }

        /**
         * Creates a SequenceNote from a piano key click.
         * Automatically finds the most natural guitar string and fret.
         */
        fun fromPiano(noteName: String, octave: Int): SequenceNote {
            val norm = normalizePitch(noteName)
            val noteIdx = CHROMATIC_NOTES.indexOf(norm).coerceAtLeast(0)
            val midi = (octave + 1) * 12 + noteIdx

            // Find best guitar position (prefer strings 0 to 3, frets 0 to 12)
            var bestString = -1
            var bestFret = -1
            for (s in 0..5) {
                val openMidi = OPEN_STRING_MIDI[s]
                val f = midi - openMidi
                if (f in 0..14) {
                    bestString = s
                    bestFret = f
                    break
                }
            }

            return SequenceNote(
                note = norm,
                octave = octave,
                stringIndex = bestString,
                fret = bestFret
            )
        }

        /**
         * Deserializes from a token string. Handles both new format "$note:$octave:$stringIndex:$fret"
         * and legacy plain note strings like "C" or "C#" seamlessly.
         */
        fun fromToken(raw: String): SequenceNote {
            val parts = raw.split(":")
            return when {
                parts.size == 4 -> {
                    SequenceNote(
                        note = parts[0],
                        octave = parts[1].toIntOrNull() ?: 4,
                        stringIndex = parts[2].toIntOrNull() ?: -1,
                        fret = parts[3].toIntOrNull() ?: -1
                    )
                }
                parts.size == 2 -> {
                    SequenceNote(
                        note = parts[0],
                        octave = parts[1].toIntOrNull() ?: 4
                    )
                }
                else -> {
                    // Legacy format: raw note name (e.g. "C")
                    fromPiano(raw, 4)
                }
            }
        }
    }
}
