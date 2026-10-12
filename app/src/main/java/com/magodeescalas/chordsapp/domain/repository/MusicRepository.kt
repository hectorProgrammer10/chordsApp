package com.magodeescalas.chordsapp.domain.repository

import com.magodeescalas.chordsapp.domain.model.ChordShape
import com.magodeescalas.chordsapp.domain.model.FretboardNote
import com.magodeescalas.chordsapp.domain.model.Scale
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.domain.model.ChordQuality

interface MusicRepository {
    fun getScale(root: String, type: ScaleType): Scale
    fun getChordShape(root: String, quality: ChordQuality): ChordShape
    fun getFretboardMap(scaleRoot: String, scaleType: ScaleType, fretCount: Int = 13): List<List<FretboardNote>>
}
