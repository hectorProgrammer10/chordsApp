package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.magodeescalas.chordsapp.domain.model.SequenceNote
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

/**
 * Normalizes enarmonics (e.g. Db -> C#, Eb -> D#, etc.) for piano keyboard layout.
 */
private fun normalizePianoNote(note: String): String {
    return when (note) {
        "Db" -> "C#"
        "Eb" -> "D#"
        "Gb" -> "F#"
        "Ab" -> "G#"
        "Bb" -> "A#"
        else -> note
    }
}

private data class WhiteKeyDef(
    val note: String,
    val octave: Int
)

private data class BlackKeyDef(
    val note: String,
    val octave: Int,
    val afterWhiteKeyIndex: Int // Index of the white key this black key sits after
)

/**
 * Full interactive Piano Scale Keyboard displaying 2+ octaves (C3 to C5, 15 white keys).
 * Highlights all notes in the active scale, identifies the root note, and supports
 * tap-to-select note sequences with numbered badges ("1", "2", "3"...).
 */
@Composable
fun PianoScaleBoard(
    scaleNotes: List<String>,
    rootNote: String,
    selectedNotes: List<String> = emptyList(),
    onNoteClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val normalizedScale = remember(scaleNotes) {
        scaleNotes.map { normalizePianoNote(it) }.toSet()
    }
    val normalizedRoot = remember(rootNote) {
        normalizePianoNote(rootNote)
    }

    val parsedSequence = remember(selectedNotes) {
        selectedNotes.map { SequenceNote.fromToken(it) }
    }

    // 15 White keys (2 full octaves + 1 C: C3..B3, C4..B4, C5)
    val whiteKeys = remember {
        listOf(
            WhiteKeyDef("C", 3), WhiteKeyDef("D", 3), WhiteKeyDef("E", 3),
            WhiteKeyDef("F", 3), WhiteKeyDef("G", 3), WhiteKeyDef("A", 3), WhiteKeyDef("B", 3),
            WhiteKeyDef("C", 4), WhiteKeyDef("D", 4), WhiteKeyDef("E", 4),
            WhiteKeyDef("F", 4), WhiteKeyDef("G", 4), WhiteKeyDef("A", 4), WhiteKeyDef("B", 4),
            WhiteKeyDef("C", 5)
        )
    }

    // 10 Black keys positioned after specific white keys
    val blackKeys = remember {
        listOf(
            // Octave 3
            BlackKeyDef("C#", 3, 0),
            BlackKeyDef("D#", 3, 1),
            BlackKeyDef("F#", 3, 3),
            BlackKeyDef("G#", 3, 4),
            BlackKeyDef("A#", 3, 5),
            // Octave 4
            BlackKeyDef("C#", 4, 7),
            BlackKeyDef("D#", 4, 8),
            BlackKeyDef("F#", 4, 10),
            BlackKeyDef("G#", 4, 11),
            BlackKeyDef("A#", 4, 12)
        )
    }

    val whiteKeyWidth = 48.dp
    val whiteKeyHeight = 200.dp
    val blackKeyWidth = 30.dp
    val blackKeyHeight = 124.dp
    val totalBoardWidth = whiteKeyWidth * whiteKeys.size

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wooden / Slate Fallboard Top Bar (Acoustic Piano Frame)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )

        // Scrollable Keyboard Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(Color(0xFF0F172A))
                .padding(bottom = 12.dp)
        ) {
            // Container with exact board dimensions
            Box(
                modifier = Modifier
                    .width(totalBoardWidth)
                    .height(whiteKeyHeight)
            ) {
                // 1. Render White Keys (Base layer)
                Row(modifier = Modifier.fillMaxSize()) {
                    whiteKeys.forEachIndexed { index, whiteKey ->
                        val normNote = whiteKey.note
                        val isInScale = normalizedScale.contains(normNote)
                        val isRoot = (normNote == normalizedRoot)
                        val matchingIndex = parsedSequence.indexOfFirst {
                            it.matchesPiano(whiteKey.note, whiteKey.octave)
                        }
                        val isSelected = matchingIndex != -1
                        val selectionIndex = if (isSelected) matchingIndex + 1 else null
                        val clickToken = SequenceNote.fromPiano(whiteKey.note, whiteKey.octave).toToken()

                        Box(
                            modifier = Modifier
                                .width(whiteKeyWidth)
                                .height(whiteKeyHeight)
                                .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                                .background(
                                    color = when {
                                        isSelected -> Color(0xFFDCFCE7) // Soft mint green tint
                                        isInScale -> Color(0xFFFFFFFF)  // Bright piano white
                                        else -> Color(0xFFE2E8F0).copy(alpha = 0.6f) // Muted off-white
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) WebPrimary else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)
                                )
                                .then(
                                    if (isInScale && onNoteClick != null) {
                                        Modifier.clickable { onNoteClick(clickToken) }
                                    } else Modifier
                                )
                                .padding(horizontal = 2.dp, vertical = 6.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (isInScale) {
                                // Scale Note Bubble
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                color = when {
                                                    isSelected -> WebPrimary
                                                    isRoot -> MaterialTheme.colorScheme.secondary
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
                                            text = whiteKey.note,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Numbered selection badge ("1", "2", "3"...)
                                    if (isSelected && selectionIndex != null) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .size(16.dp)
                                                .background(WebPrimary, CircleShape)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = selectionIndex.toString(),
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Dimmed key note label for reference
                                Text(
                                    text = whiteKey.note,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Render Black Keys (Overlay on top of white keys)
                blackKeys.forEach { blackKey ->
                    val normNote = blackKey.note
                    val isInScale = normalizedScale.contains(normNote)
                    val isRoot = (normNote == normalizedRoot)
                    val matchingIndex = parsedSequence.indexOfFirst {
                        it.matchesPiano(blackKey.note, blackKey.octave)
                    }
                    val isSelected = matchingIndex != -1
                    val selectionIndex = if (isSelected) matchingIndex + 1 else null
                    val clickToken = SequenceNote.fromPiano(blackKey.note, blackKey.octave).toToken()

                    // Horizontal position: Centered on the seam after the target white key
                    val startOffset = (whiteKeyWidth * (blackKey.afterWhiteKeyIndex + 1)) - (blackKeyWidth / 2)

                    Box(
                        modifier = Modifier
                            .offset(x = startOffset, y = 0.dp)
                            .width(blackKeyWidth)
                            .height(blackKeyHeight)
                            .zIndex(10f)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                color = when {
                                    isSelected -> Color(0xFF14532D) // Dark forest green
                                    isInScale -> Color(0xFF0F172A)  // Jet black for in-scale
                                    else -> Color(0xFF475569).copy(alpha = 0.5f) // Muted slate gray
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) WebPrimary else Color(0xFF334155),
                                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                            )
                            .then(
                                if (isInScale && onNoteClick != null) {
                                    Modifier.clickable { onNoteClick(clickToken) }
                                } else Modifier
                            )
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (isInScale) {
                            // Scale Note Bubble on Black Key
                            Box(
                                modifier = Modifier.size(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = when {
                                                isSelected -> WebPrimary
                                                isRoot -> MaterialTheme.colorScheme.secondary
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            shape = CircleShape
                                        )
                                        .then(
                                            if (isSelected) Modifier.border(1.5.dp, Color.White, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = blackKey.note,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Selection badge
                                if (isSelected && selectionIndex != null) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(15.dp)
                                            .background(WebPrimary, CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = selectionIndex.toString(),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            // Dimmed sharp note label
                            Text(
                                text = blackKey.note,
                                color = Color(0xFF64748B),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
