package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.Chord
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

@Composable
fun PianoChordCard(
    chord: Chord,
    isSelected: Boolean = false,
    selectionIndex: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(170.dp)
            .height(220.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    Color(0xFFE8F5E9).copy(alpha = 0.95f)
                } else {
                    Color.White.copy(alpha = 0.92f)
                }
            ),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.5.dp, WebPrimary)
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            },
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Root and Quality
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = chord.root,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) WebPrimary else Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = chord.quality.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) WebPrimary.copy(alpha = 0.8f) else Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Piano Diagram (Keyboard)
                PianoDiagram(
                    chord = chord,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes in chord
                Text(
                    text = chord.notes.joinToString(" - "),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) WebPrimary else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Green numbered badge in top-right corner ("1", "2", "3"...)
        if (isSelected && selectionIndex != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(28.dp)
                    .background(WebPrimary, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectionIndex.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Normalizes note names for enarmonics (e.g. Db -> C#, Eb -> D#, etc.)
 */
private fun normalizeNote(note: String): String {
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
 * Renders a piano keyboard with 14 white keys (2 octaves) and highlights chord notes in green.
 */
@Composable
fun PianoDiagram(chord: Chord, modifier: Modifier = Modifier) {
    val normalizedChordNotes = chord.notes.map { normalizeNote(it) }.toSet()

    // 14 White keys (2 octaves: C to B, C to B)
    val whiteNotes = listOf(
        "C", "D", "E", "F", "G", "A", "B",
        "C", "D", "E", "F", "G", "A", "B"
    )

    // Black keys positions (after which white key index)
    // In each octave: C(0), D(1), E(2), F(3), G(4), A(5), B(6)
    // Black keys are after index 0 (C#), index 1 (D#), index 3 (F#), index 4 (G#), index 5 (A#)
    data class BlackKeyInfo(val note: String, val afterWhiteIndex: Int)

    val blackKeys = listOf(
        // Octave 1
        BlackKeyInfo("C#", 0),
        BlackKeyInfo("D#", 1),
        BlackKeyInfo("F#", 3),
        BlackKeyInfo("G#", 4),
        BlackKeyInfo("A#", 5),
        // Octave 2
        BlackKeyInfo("C#", 7),
        BlackKeyInfo("D#", 8),
        BlackKeyInfo("F#", 10),
        BlackKeyInfo("G#", 11),
        BlackKeyInfo("A#", 12)
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val numWhiteKeys = whiteNotes.size
        val whiteKeyWidth = width / numWhiteKeys
        val blackKeyWidth = whiteKeyWidth * 0.62f
        val blackKeyHeight = height * 0.58f

        // Draw White Keys
        for (i in 0 until numWhiteKeys) {
            val keyNote = whiteNotes[i]
            val isHighlighted = normalizedChordNotes.contains(keyNote)
            val x = i * whiteKeyWidth

            // White Key Fill
            val fillColor = if (isHighlighted) {
                Color(0xFFC8E6C9) // Vibrant light green
            } else {
                Color(0xFFFCFCFD) // Piano white
            }

            drawRoundRect(
                color = fillColor,
                topLeft = Offset(x + 1f, 0f),
                size = Size(whiteKeyWidth - 2f, height),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Key Border
            drawRoundRect(
                color = if (isHighlighted) WebPrimary else Color(0xFFD1D5DB),
                topLeft = Offset(x + 1f, 0f),
                size = Size(whiteKeyWidth - 2f, height),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = if (isHighlighted) 2f else 1f)
            )

            // If chord note, draw green indicator dot at bottom of white key
            if (isHighlighted) {
                drawCircle(
                    color = WebPrimary,
                    radius = whiteKeyWidth * 0.28f,
                    center = Offset(x + whiteKeyWidth / 2f, height - (whiteKeyWidth * 0.38f))
                )
            }
        }

        // Draw Black Keys (overlay on top of white keys)
        blackKeys.forEach { bKey ->
            val isHighlighted = normalizedChordNotes.contains(bKey.note)
            val centerX = (bKey.afterWhiteIndex + 1) * whiteKeyWidth
            val x = centerX - (blackKeyWidth / 2f)

            val fillColor = if (isHighlighted) {
                WebPrimary // Green highlight for black key!
            } else {
                Color(0xFF1E293B) // Slate dark for piano black key
            }

            drawRoundRect(
                color = fillColor,
                topLeft = Offset(x, 0f),
                size = Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = CornerRadius(3f, 3f)
            )

            // Black key highlight line
            drawRoundRect(
                color = if (isHighlighted) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                topLeft = Offset(x + 1f, 1f),
                size = Size(blackKeyWidth - 2f, blackKeyHeight - 2f),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = 1f)
            )

            // If chord note, draw a white dot on the green black key
            if (isHighlighted) {
                drawCircle(
                    color = Color.White,
                    radius = blackKeyWidth * 0.22f,
                    center = Offset(centerX, blackKeyHeight - (blackKeyWidth * 0.35f))
                )
            }
        }
    }
}
