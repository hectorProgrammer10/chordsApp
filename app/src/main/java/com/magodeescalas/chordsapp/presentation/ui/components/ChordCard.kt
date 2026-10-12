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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.Chord
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

@Composable
fun ChordCard(
    chord: Chord,
    isSelected: Boolean = false,
    selectionIndex: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(160.dp)
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
                    Color.White.copy(alpha = 0.9f)
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
                modifier = Modifier.fillMaxSize().padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
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

                Spacer(modifier = Modifier.height(12.dp))

                // Chord Diagram (Canvas)
                ChordDiagram(chord = chord, modifier = Modifier.weight(1f).fillMaxWidth())

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = chord.notes.joinToString(" - "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        // Bolita verde en la parte superior derecha marcando "1", "2", "3"...
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

@Composable
fun ChordDiagram(chord: Chord, modifier: Modifier = Modifier) {
    val shape = chord.shape ?: return
    val baseFret = shape.baseFret

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val startX = width * 0.15f
        val endX = width * 0.85f
        val startY = height * 0.15f
        val endY = height * 0.9f

        val stringSpacing = (endX - startX) / 5
        val fretSpacing = (endY - startY) / 5

        for (i in 0..5) {
            val x = startX + i * stringSpacing
            drawLine(
                color = if (i >= 3) Color.DarkGray else Color.Gray,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = if (i >= 3) 2f else 4f,
                cap = StrokeCap.Round
            )
        }

        for (i in 0..5) {
            val y = startY + i * fretSpacing
            val isNut = i == 0 && baseFret == 1
            drawLine(
                color = if (isNut) Color.Black else Color.Gray,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = if (isNut) 6f else 2f,
                cap = StrokeCap.Round
            )
        }

        shape.frets.forEachIndexed { stringIndex, fret ->
            val x = startX + stringIndex * stringSpacing

            if (fret == -1) {
                val y = startY - 15f
                drawLine(Color.Red, Offset(x - 5, y - 5), Offset(x + 5, y + 5), strokeWidth = 3f)
                drawLine(Color.Red, Offset(x + 5, y - 5), Offset(x - 5, y + 5), strokeWidth = 3f)
            } else if (fret == 0) {
                val y = startY - 15f
                drawCircle(
                    Color.Black,
                    radius = 6f,
                    center = Offset(x, y),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            } else {
                if (fret >= baseFret) {
                    val relativeFret = fret - baseFret + 1
                    if (relativeFret <= 5) {
                        val y = startY + (relativeFret * fretSpacing) - (fretSpacing / 2)
                        drawCircle(color = Color.Black, radius = 12f, center = Offset(x, y))
                    }
                }
            }
        }
    }
}
