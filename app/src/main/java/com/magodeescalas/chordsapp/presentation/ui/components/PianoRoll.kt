package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.SequenceNote
import com.magodeescalas.chordsapp.domain.model.normalizePitch
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

private data class PitchRowDef(
    val note: String,
    val octave: Int,
    val isBlackKey: Boolean
) {
    val label: String
        get() = "$note$octave"
}

/**
 * Generates piano roll pitch rows from top (high pitch C8) to bottom (low pitch C1).
 */
private fun buildPianoRollPitches(): List<PitchRowDef> {
    val chromaticDesc = listOf("B", "A#", "A", "G#", "G", "F#", "F", "E", "D#", "D", "C#", "C")
    val blackNotes = setOf("A#", "G#", "F#", "D#", "C#")
    val list = mutableListOf<PitchRowDef>()

    // Top note: C8
    list.add(PitchRowDef("C", 8, isBlackKey = false))

    // Octaves 7 down to 1 (B7..C7 to B1..C1)
    for (oct in 7 downTo 1) {
        for (note in chromaticDesc) {
            list.add(PitchRowDef(note, oct, isBlackKey = blackNotes.contains(note)))
        }
    }

    return list
}

/**
 * Renders a DAW-style Piano Roll (matching Image 2) reflecting selected notes in sequence.
 * Features a vertical piano keyboard axis on the left (with C1..C8 markers),
 * a dark grid layout with beat/step columns, and horizontal note blocks.
 */
@Composable
fun PianoRoll(
    notes: List<String>,
    title: String = "Piano Roll",
    emptyHint: String = "Toca teclas en el piano para generar la secuencia",
    onNoteClick: ((Int) -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val rowHeight = 22.dp
    val stepWidth = 64.dp
    val keyboardWidth = 52.dp

    val hScrollState = rememberScrollState()
    // Initial vertical scroll: centered around octave 4 / 5 (middle pitch)
    val initialVScrollPx = remember {
        with(density) { (rowHeight * 36).roundToPx() }
    }
    val vScrollState = rememberScrollState(initial = initialVScrollPx)

    val parsedNotes = remember(notes) {
        notes.map { SequenceNote.fromToken(it) }
    }

    val pitchRows = remember { buildPianoRollPitches() }

    // Auto-scroll vertically to the most recent note in the sequence
    androidx.compose.runtime.LaunchedEffect(parsedNotes.size) {
        if (parsedNotes.isNotEmpty()) {
            val lastNote = parsedNotes.last()
            val targetIndex = pitchRows.indexOfFirst {
                it.note == lastNote.normalizedNote && it.octave == lastNote.octave
            }
            if (targetIndex >= 0) {
                val targetPx = with(density) { (rowHeight * (targetIndex - 3).coerceAtLeast(0)).roundToPx() }
                vScrollState.animateScrollTo(targetPx)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF182234)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Icon + Title + Note Count + Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Piano,
                        contentDescription = null,
                        tint = WebPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    if (parsedNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(WebPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${parsedNotes.size} ${if (parsedNotes.size == 1) "nota" else "notas"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WebPrimary
                            )
                        }
                    }
                }

                if (parsedNotes.isNotEmpty() && onClear != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar Piano Roll",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (parsedNotes.isEmpty()) {
                // Empty Piano Roll Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Piano,
                            contentDescription = null,
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                // Active DAW Piano Roll with vertical keys and horizontal step grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 1. Vertical Keyboard Axis (Fixed on the left, scrolls vertically)
                        Column(
                            modifier = Modifier
                                .width(keyboardWidth)
                                .fillMaxHeight()
                                .verticalScroll(vScrollState)
                                .background(Color(0xFF1E293B))
                        ) {
                            pitchRows.forEach { pitch ->
                                val isC = (pitch.note == "C")
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(rowHeight)
                                        .background(
                                            if (pitch.isBlackKey) Color(0xFF1E2024)
                                            else Color(0xFFE2E8F0)
                                        )
                                        .border(0.5.dp, Color(0xFF334155)),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (isC) {
                                        Text(
                                            text = pitch.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(start = 4.dp),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    } else if (!pitch.isBlackKey) {
                                        Text(
                                            text = pitch.note,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.padding(start = 4.dp),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Vertical Separator Line between keyboard and grid
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF475569))
                        )

                        // 2. Main DAW Grid Area (Synchronized vertical scroll + horizontal step scroll)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .horizontalScroll(hScrollState)
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(stepWidth * parsedNotes.size.coerceAtLeast(4))
                                    .verticalScroll(vScrollState)
                            ) {
                                pitchRows.forEach { pitch ->
                                    val rowNormNote = normalizePitch(pitch.note)
                                    val rowOctave = pitch.octave

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(rowHeight)
                                            .background(
                                                if (pitch.isBlackKey) Color(0xFF13171F)
                                                else Color(0xFF1E2430)
                                            )
                                            .border(0.5.dp, Color(0xFF273244))
                                    ) {
                                        parsedNotes.forEachIndexed { stepIdx, seqNote ->
                                            val isNoteHere = (seqNote.normalizedNote == rowNormNote &&
                                                    (seqNote.octave == rowOctave || (seqNote.octave <= 0 && rowOctave == 4)))

                                            Box(
                                                modifier = Modifier
                                                    .width(stepWidth)
                                                    .fillMaxHeight()
                                                    .border(0.5.dp, Color(0xFF273244))
                                                    .then(
                                                        if (onNoteClick != null && isNoteHere) {
                                                            Modifier.clickable { onNoteClick(stepIdx) }
                                                        } else Modifier
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isNoteHere) {
                                                    // Horizontal Note Bar/Block
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.92f)
                                                            .fillMaxHeight(0.78f)
                                                            .background(
                                                                color = WebPrimary.copy(alpha = 0.85f),
                                                                shape = RoundedCornerShape(3.dp)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = Color.White.copy(alpha = 0.6f),
                                                                shape = RoundedCornerShape(3.dp)
                                                            )
                                                            .padding(horizontal = 4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${seqNote.note}${if (seqNote.octave > 0) seqNote.octave else ""}",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            fontFamily = FontFamily.Monospace
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
            }
        }
    }
}
