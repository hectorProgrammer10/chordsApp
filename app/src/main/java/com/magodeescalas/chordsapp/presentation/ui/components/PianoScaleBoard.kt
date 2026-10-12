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
 * Full interactive Piano Scale Keyboard displaying octaves C1 to C8 (50 white keys, 35 black keys).
 * Highlights all notes in the active scale, identifies the root note, and supports
 * tap-to-select note sequences.
 */
@Composable
fun PianoScaleBoard(
    scaleNotes: List<String>,
    rootNote: String,
    selectedNotes: List<String> = emptyList(),
    onNoteClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val whiteKeyWidth = 48.dp
    val whiteKeyHeight = 200.dp
    val blackKeyWidth = 30.dp
    val blackKeyHeight = 124.dp

    // Initial scroll position: centered around C3 / C4 (index 14 = C3)
    val initialScrollPx = remember {
        with(density) { (whiteKeyWidth * 14).roundToPx() }
    }
    val scrollState = rememberScrollState(initial = initialScrollPx)

    val normalizedScale = remember(scaleNotes) {
        scaleNotes.map { normalizePianoNote(it) }.toSet()
    }
    val normalizedRoot = remember(rootNote) {
        normalizePianoNote(rootNote)
    }

    val parsedSequence = remember(selectedNotes) {
        selectedNotes.map { SequenceNote.fromToken(it) }
    }

    // 50 White keys (C1..B1, C2..B2, C3..B3, C4..B4, C5..B5, C6..B6, C7..B7, C8)
    val whiteKeys = remember {
        val list = mutableListOf<WhiteKeyDef>()
        val whiteNames = listOf("C", "D", "E", "F", "G", "A", "B")
        for (oct in 1..7) {
            for (note in whiteNames) {
                list.add(WhiteKeyDef(note, oct))
            }
        }
        list.add(WhiteKeyDef("C", 8))
        list
    }

    // 35 Black keys positioned after specific white keys across octaves 1 to 7
    val blackKeys = remember {
        val list = mutableListOf<BlackKeyDef>()
        for (oct in 1..7) {
            val baseWhiteIdx = (oct - 1) * 7
            list.add(BlackKeyDef("C#", oct, baseWhiteIdx + 0))
            list.add(BlackKeyDef("D#", oct, baseWhiteIdx + 1))
            list.add(BlackKeyDef("F#", oct, baseWhiteIdx + 3))
            list.add(BlackKeyDef("G#", oct, baseWhiteIdx + 4))
            list.add(BlackKeyDef("A#", oct, baseWhiteIdx + 5))
        }
        list
    }

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
                            val displayText = if (whiteKey.note == "C") "C${whiteKey.octave}" else whiteKey.note

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
                                            text = displayText,
                                            color = Color.White,
                                            fontSize = if (whiteKey.note == "C") 10.sp else 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // Dimmed key note label for reference
                                Text(
                                    text = displayText,
                                    color = Color(0xFF94A3B8),
                                    fontSize = if (whiteKey.note == "C") 10.sp else 11.sp,
                                    fontWeight = if (whiteKey.note == "C") FontWeight.Bold else FontWeight.Medium,
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
