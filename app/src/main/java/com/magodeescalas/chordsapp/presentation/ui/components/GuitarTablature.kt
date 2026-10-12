package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.SequenceNote
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

private val STRING_LABELS = listOf("e", "B", "G", "D", "A", "E")

/**
 * Renders an authentic 6-string Guitar Tablature (Tab) reflecting selected notes in sequence.
 * Matches the style of Image 1 with e, B, G, D, A, E strings, horizontal bar lines,
 * and fret numbers plotted chronologically from left to right.
 */
@Composable
fun GuitarTablature(
    notes: List<String>,
    title: String = "Tablatura de Guitarra",
    emptyHint: String = "Toca notas en el mástil para generar la tablatura",
    onNoteClick: ((Int) -> Unit)? = null, // Callback with step index to remove or inspect note
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val parsedNotes = remember(notes) {
        notes.map { SequenceNote.fromToken(it) }
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
                        imageVector = Icons.Default.MusicNote,
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
                            contentDescription = "Limpiar Tablatura",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (parsedNotes.isEmpty()) {
                // Empty Tab Staff placeholder with instructional hint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 6 faint horizontal tab lines preview
                        for (i in 0..5) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = STRING_LABELS[i],
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(18.dp)
                                )
                                Text(
                                    text = "|-----------------------------------------------------|",
                                    color = Color(0xFF334155),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = emptyHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                // Active Tab Staff with chronological notes plotted on strings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                        .horizontalScroll(scrollState)
                ) {
                    Column {
                        // Step Numbers Header Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(36.dp)) // Offset for string labels and start bar
                            parsedNotes.forEachIndexed { stepIdx, _ ->
                                Box(
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${stepIdx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WebPrimary.copy(alpha = 0.85f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // 6 Guitar String Lines (e, B, G, D, A, E)
                        STRING_LABELS.forEachIndexed { stringIdx, label ->
                            Row(
                                modifier = Modifier.height(26.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // String Label (e, B, G, D, A, E)
                                Text(
                                    text = label,
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(16.dp)
                                )

                                // Opening Bar Line '|'
                                Text(
                                    text = "|-",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                // Sequence Steps on this string line
                                parsedNotes.forEachIndexed { stepIdx, seqNote ->
                                    val isPlayedOnThisString = (seqNote.stringIndex == stringIdx)
                                    val fretNumber = if (seqNote.fret >= 0) seqNote.fret else 0

                                    Box(
                                        modifier = Modifier
                                            .width(38.dp)
                                            .fillMaxHeight()
                                            .then(
                                                if (onNoteClick != null) Modifier.clickable { onNoteClick(stepIdx) }
                                                else Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPlayedOnThisString) {
                                            // Highlighted Fret Number on String Line
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(WebPrimary, CircleShape)
                                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$fretNumber",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        } else {
                                            // Continuous Dashed Line '--'
                                            Text(
                                                text = "---",
                                                color = Color(0xFF475569),
                                                fontSize = 13.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }

                                // Closing Bar Line '|'
                                Text(
                                    text = "-|",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
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
