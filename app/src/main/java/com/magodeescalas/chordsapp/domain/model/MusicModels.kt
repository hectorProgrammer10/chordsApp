package com.magodeescalas.chordsapp.domain.model

enum class ScaleType {
    Major, Minor
}

enum class ChordQuality {
    Major, Minor, Diminished
}

data class Chord(
    val root: String,
    val quality: ChordQuality,
    val notes: List<String>,
    val shape: ChordShape? = null
)

data class Scale(
    val root: String,
    val type: ScaleType,
    val notes: List<String>,
    val chords: List<Chord>
)

data class FretboardNote(
    val note: String,
    val interval: Int, // fret number
    val isRoot: Boolean,
    val isInScale: Boolean,
    val scaleDegree: Int? = null
)

data class ChordShape(
    val frets: List<Int>, // 6 numbers. -1 for mute, 0 for open.
    val baseFret: Int,
    val fingers: List<Int>? = null
)
