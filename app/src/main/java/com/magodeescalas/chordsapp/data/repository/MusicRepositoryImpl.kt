package com.magodeescalas.chordsapp.data.repository

import com.magodeescalas.chordsapp.domain.model.*
import com.magodeescalas.chordsapp.domain.repository.MusicRepository

class MusicRepositoryImpl : MusicRepository {

    private val NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val GUITAR_STRINGS = listOf("E", "B", "G", "D", "A", "E") // High E to Low E

    private val SCALE_INTERVALS = mapOf(
        ScaleType.Major to listOf(0, 2, 4, 5, 7, 9, 11),
        ScaleType.Minor to listOf(0, 2, 3, 5, 7, 8, 10)
    )

    private val OPEN_CHORDS = mapOf(
        "C" to mapOf(ChordQuality.Major to ChordShape(listOf(-1, 3, 2, 0, 1, 0), 1)),
        "A" to mapOf(
            ChordQuality.Major to ChordShape(listOf(-1, 0, 2, 2, 2, 0), 1),
            ChordQuality.Minor to ChordShape(listOf(-1, 0, 2, 2, 1, 0), 1)
        ),
        "G" to mapOf(ChordQuality.Major to ChordShape(listOf(3, 2, 0, 0, 0, 3), 1)),
        "E" to mapOf(
            ChordQuality.Major to ChordShape(listOf(0, 2, 2, 1, 0, 0), 1),
            ChordQuality.Minor to ChordShape(listOf(0, 2, 2, 0, 0, 0), 1)
        ),
        "D" to mapOf(
            ChordQuality.Major to ChordShape(listOf(-1, -1, 0, 2, 3, 2), 1),
            ChordQuality.Minor to ChordShape(listOf(-1, -1, 0, 2, 3, 1), 1)
        )
    )
    
    // Offsets for Barre Chords (Root on String 6 - E String)
    private val ROOT_ON_E_OFFSETS = mapOf(
        "E" to 0, "F" to 1, "F#" to 2, "G" to 3, "G#" to 4, "A" to 5, "A#" to 6, "B" to 7, 
        "C" to 8, "C#" to 9, "D" to 10, "D#" to 11
    )

    // Offsets for Barre Chords (Root on String 5 - A String)
    private val ROOT_ON_A_OFFSETS = mapOf(
        "A" to 0, "A#" to 1, "B" to 2, "C" to 3, "C#" to 4, "D" to 5, "D#" to 6, 
        "E" to 7, "F" to 8, "F#" to 9, "G" to 10, "G#" to 11
    )

    override fun getScale(root: String, type: ScaleType): Scale {
        val notes = getScaleNotes(root, type)
        val chords = getScaleChords(notes, type)
        return Scale(root, type, notes, chords)
    }

    override fun getChordShape(root: String, quality: ChordQuality): ChordShape {
         // 1. Try generic Open Chord match first (Exact match)
        OPEN_CHORDS[root]?.get(quality)?.let { return it }

        // 2. Barre Chords Logic
        if (quality == ChordQuality.Diminished) {
             val offset = ROOT_ON_A_OFFSETS[root] ?: 0
             // A dim shape shifted: x-0-1-2-1-x -> with offset
             // Shape mapped visually relative to nut (0) if using simplified mapping
             // Since we use absolute frets in the web app logic, we do the same here.
             val baseShape = listOf(-1, 0, 1, 2, 1, -1)
             val shifted = baseShape.map { if (it == -1) -1 else it + offset }
             return ChordShape(shifted, 1)
        }

        if (quality == ChordQuality.Major) {
            if (listOf("E", "F", "F#", "G", "G#", "A", "A#").contains(root)) {
                // E Shape
                val offset = ROOT_ON_E_OFFSETS[root] ?: 0
                val baseShape = listOf(0, 2, 2, 1, 0, 0)
                val shifted = baseShape.map { it + offset }
                return ChordShape(shifted, 1)
            } else {
                // A Shape
                val offset = ROOT_ON_A_OFFSETS[root] ?: 0
                val baseShape = listOf(-1, 0, 2, 2, 2, 0)
                val shifted = baseShape.map { if (it == -1) -1 else it + offset }
                return ChordShape(shifted, 1)
            }
        }
        
        if (quality == ChordQuality.Minor) {
             if (listOf("E", "F", "F#", "G", "G#", "A").contains(root)) {
                 // E Shape Minor
                 val offset = ROOT_ON_E_OFFSETS[root] ?: 0
                 val baseShape = listOf(0, 2, 2, 0, 0, 0)
                 val shifted = baseShape.map { it + offset }
                 return ChordShape(shifted, 1)
             } else {
                 // A Shape Minor
                 val offset = ROOT_ON_A_OFFSETS[root] ?: 0
                 val baseShape = listOf(-1, 0, 2, 2, 1, 0)
                 val shifted = baseShape.map { if (it == -1) -1 else it + offset }
                 return ChordShape(shifted, 1)
             }
        }

        return ChordShape(listOf(-1, -1, -1, -1, -1, -1), 1)
    }

    override fun getFretboardMap(scaleRoot: String, scaleType: ScaleType, fretCount: Int): List<List<FretboardNote>> {
        val scaleNotes = getScaleNotes(scaleRoot, scaleType)
        
        return GUITAR_STRINGS.map { openStringNote ->
            val stringNotes = mutableListOf<FretboardNote>()
            var currentNoteIndex = getNoteIndex(openStringNote)
            
            for (fret in 0..fretCount) {
                val noteName = NOTES[currentNoteIndex % 12]
                val isInScale = scaleNotes.contains(noteName)
                val isRoot = noteName == scaleRoot
                val scaleDegree = if (isInScale) scaleNotes.indexOf(noteName) + 1 else null
                
                stringNotes.add(FretboardNote(noteName, fret, isRoot, isInScale, scaleDegree))
                currentNoteIndex++
            }
            stringNotes
        }
    }

    private fun getNoteIndex(note: String): Int {
        return NOTES.indexOf(note)
    }

    private fun getScaleNotes(root: String, scaleType: ScaleType): List<String> {
        val rootIndex = getNoteIndex(root)
        if (rootIndex == -1) return emptyList()

        return SCALE_INTERVALS[scaleType]!!.map { interval ->
            NOTES[(rootIndex + interval) % 12]
        }
    }
    
    private fun getChordQuality(scaleType: ScaleType, degreeIndex: Int): ChordQuality {
         if (scaleType == ScaleType.Major) {
             // Maj, min, min, Maj, Maj, min, dim
             val qualities = listOf(
                 ChordQuality.Major, ChordQuality.Minor, ChordQuality.Minor, 
                 ChordQuality.Major, ChordQuality.Major, ChordQuality.Minor, ChordQuality.Diminished
             )
             return qualities[degreeIndex]
         } else {
             // min, dim, Maj, min, min, Maj, Maj
             val qualities = listOf(
                 ChordQuality.Minor, ChordQuality.Diminished, ChordQuality.Major,
                 ChordQuality.Minor, ChordQuality.Minor, ChordQuality.Major, ChordQuality.Major
             )
             return qualities[degreeIndex]
         }
    }
    
    private fun getChordNotes(root: String, quality: ChordQuality): List<String> {
        val rootIndex = getNoteIndex(root)
        val intervals = when(quality) {
            ChordQuality.Major -> listOf(0, 4, 7)
            ChordQuality.Minor -> listOf(0, 3, 7)
            ChordQuality.Diminished -> listOf(0, 3, 6)
        }
        return intervals.map { NOTES[(rootIndex + it) % 12] }
    }
    
    private fun getScaleChords(scaleNotes: List<String>, scaleType: ScaleType): List<Chord> {
        return scaleNotes.mapIndexed { index, note ->
            val quality = getChordQuality(scaleType, index)
            val shape = getChordShape(note, quality)
            Chord(note, quality, getChordNotes(note, quality), shape)
        }
    }
}
