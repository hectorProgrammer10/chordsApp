package com.magodeescalas.chordsapp.domain.model

import java.util.UUID

data class TemplateData(
    val chords: List<String> = emptyList(),
    val notes: List<String> = emptyList()
)

data class Preset(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val root: String,
    val scaleType: ScaleType,
    val templates: Map<String, TemplateData> = mapOf(
        "A" to TemplateData(),
        "B" to TemplateData(),
        "C" to TemplateData(),
        "D" to TemplateData()
    ),
    val chords: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
