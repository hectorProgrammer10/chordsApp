package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.FretboardNote
import com.magodeescalas.chordsapp.domain.model.SequenceNote
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

@Composable
fun Fretboard(
    fretboardData: List<List<FretboardNote>>,
    selectedNotes: List<String> = emptyList(),
    onNoteClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val fretWidth = 60.dp

    val parsedSequence = remember(selectedNotes) {
        selectedNotes.map { SequenceNote.fromToken(it) }
    }

    val inlayFrets = remember { listOf(3, 5, 7, 9, 12) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .background(Color(0xFF2D2A2E))
            .padding(16.dp)
            .height(IntrinsicSize.Min)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fretPx = fretWidth.toPx()
            val heightPx = size.height
            val centerY = heightPx / 2

            inlayFrets.forEach { i ->
                val xCenter = (i * fretPx) - (fretPx / 2)

                if (i == 12) {
                    drawCircle(
                        Color.White.copy(alpha = 0.1f),
                        radius = 12.dp.toPx(),
                        center = Offset(xCenter, centerY - 40.dp.toPx())
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.1f),
                        radius = 12.dp.toPx(),
                        center = Offset(xCenter, centerY + 40.dp.toPx())
                    )
                } else {
                    drawCircle(
                        Color.White.copy(alpha = 0.1f),
                        radius = 16.dp.toPx(),
                        center = Offset(xCenter, centerY)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.width(fretWidth * 14)
        ) {
            fretboardData.forEachIndexed { stringIndex, stringNotes ->
                key(stringIndex) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (stringIndex >= 3) 2.dp else 1.dp)
                                .background(Color(0xFFA8A29E))
                        )

                        // Notes Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            stringNotes.forEachIndexed { fretIndex, note ->
                                key(fretIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(fretWidth)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (fretIndex > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .width(2.dp)
                                                    .fillMaxHeight()
                                                    .background(Color.Gray)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .width(4.dp)
                                                    .fillMaxHeight()
                                                    .background(Color.DarkGray)
                                            )
                                        }

                                        // Note Bubble
                                        if (note.isInScale) {
                                            val isSelected = parsedSequence.any {
                                                it.matchesGuitar(stringIndex, fretIndex, note.note)
                                            }
                                            val noteToken = SequenceNote.fromGuitar(stringIndex, fretIndex, note.note).toToken()

                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .align(Alignment.Center)
                                                    .then(
                                                        if (onNoteClick != null) Modifier.clickable { onNoteClick(noteToken) }
                                                        else Modifier
                                                    )
                                                    .background(
                                                        color = when {
                                                            isSelected -> WebPrimary
                                                            note.isRoot -> MaterialTheme.colorScheme.secondary
                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        shape = CircleShape
                                                    )
                                                    .then(
                                                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                                        else Modifier
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = note.note,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fret Numbers
        Row(modifier = Modifier.fillMaxWidth().padding(top = 206.dp)) {
            (0..13).forEach { i ->
                Box(
                    modifier = Modifier.width(fretWidth),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
