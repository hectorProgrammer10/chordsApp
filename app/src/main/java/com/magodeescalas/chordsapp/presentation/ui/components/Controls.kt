package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.presentation.util.AppStrings
import com.magodeescalas.chordsapp.presentation.viewmodel.DisplayMode
import com.magodeescalas.chordsapp.presentation.viewmodel.Instrument
import com.magodeescalas.chordsapp.presentation.viewmodel.InstrumentView
import com.magodeescalas.chordsapp.ui.theme.WebPrimary
import com.magodeescalas.chordsapp.ui.theme.WebSecondary

private val NATURAL_ROOTS = listOf("C", "D", "E", "F", "G", "A", "B")
private val SHARP_ROOTS = listOf("C#", "D#", "F#", "G#", "A#")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Controls(
    selectedRoot: String,
    onRootChange: (String) -> Unit,
    selectedScaleType: ScaleType,
    onScaleTypeChange: (ScaleType) -> Unit,
    selectedInstrument: Instrument = Instrument.Guitar,
    onInstrumentChange: (Instrument) -> Unit = {},
    instrumentView: InstrumentView = InstrumentView.Chords,
    onInstrumentViewChange: (InstrumentView) -> Unit = {},
    mode: DisplayMode = DisplayMode.Chords,
    onModeChange: (DisplayMode) -> Unit = {},
    strings: AppStrings,
    roots: List<String>
) {
    val isSharp = selectedRoot.contains("#")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Accidental Filter: Naturales vs Sostenidos (#)
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TonalityFilterButton(
                text = strings.filterNaturals,
                isActive = !isSharp,
                onClick = {
                    if (isSharp) {
                        val clean = selectedRoot.replace("#", "")
                        onRootChange(if (clean in NATURAL_ROOTS) clean else "C")
                    }
                }
            )
            Spacer(modifier = Modifier.width(6.dp))
            TonalityFilterButton(
                text = strings.filterSharps,
                isActive = isSharp,
                onClick = {
                    if (!isSharp) {
                        val targetSharp = when (selectedRoot) {
                            "C" -> "C#"
                            "D" -> "D#"
                            "F" -> "F#"
                            "G" -> "G#"
                            "A" -> "A#"
                            "E" -> "F#"
                            "B" -> "C#"
                            else -> "C#"
                        }
                        onRootChange(targetSharp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Root Selector Chips (Natural or Sharp based on filter)
        val currentRoots = if (isSharp) SHARP_ROOTS else NATURAL_ROOTS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            currentRoots.forEach { root ->
                val isSelected = root == selectedRoot
                RootChip(
                    text = root,
                    isSelected = isSelected,
                    onClick = { onRootChange(root) }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdowns row: Tonality and Scale Type
        // Dropdowns row: Tonality, Scale Type, and Instrument
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Root Dropdown
            Column {
                Text(
                    text = strings.root.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                var expandedRoot by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .clickable { expandedRoot = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedRoot,
                            fontWeight = FontWeight.Bold,
                            color = if (isSharp) WebPrimary else Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = expandedRoot,
                        onDismissRequest = { expandedRoot = false }
                    ) {
                        // Sostenidos group header
                        Text(
                            text = "── ${strings.filterSharps} ──",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WebPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        SHARP_ROOTS.forEach { root ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        root,
                                        fontWeight = if (root == selectedRoot) FontWeight.Bold else FontWeight.Normal,
                                        color = if (root == selectedRoot) WebPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    onRootChange(root)
                                    expandedRoot = false
                                }
                            )
                        }

                        // Naturales group header
                        Text(
                            text = "── ${strings.filterNaturals} ──",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        NATURAL_ROOTS.forEach { root ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        root,
                                        fontWeight = if (root == selectedRoot) FontWeight.Bold else FontWeight.Normal,
                                        color = if (root == selectedRoot) WebPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    onRootChange(root)
                                    expandedRoot = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Scale Type Selector (Major / Minor)
            Column {
                Text(
                    text = strings.scale.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                var expandedScale by remember { mutableStateOf(false) }
                val currentScaleName =
                    if (selectedScaleType == ScaleType.Major) strings.scaleTypeMajor
                    else strings.scaleTypeMinor

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .clickable { expandedScale = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentScaleName,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = expandedScale,
                        onDismissRequest = { expandedScale = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.scaleTypeMajor) },
                            onClick = {
                                onScaleTypeChange(ScaleType.Major)
                                expandedScale = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.scaleTypeMinor) },
                            onClick = {
                                onScaleTypeChange(ScaleType.Minor)
                                expandedScale = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Instrument Dropdown (Guitarra / Piano)
            Column {
                Text(
                    text = strings.instrument.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                var expandedInstrument by remember { mutableStateOf(false) }
                val currentInstrumentName = when (selectedInstrument) {
                    Instrument.Guitar -> strings.instrumentGuitar
                    Instrument.Piano -> strings.instrumentPiano
                }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .clickable { expandedInstrument = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selectedInstrument == Instrument.Guitar) Icons.Default.MusicNote else Icons.Default.Piano,
                            contentDescription = null,
                            tint = WebPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentInstrumentName,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = expandedInstrument,
                        onDismissRequest = { expandedInstrument = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.MusicNote, null, tint = WebPrimary) },
                            text = {
                                Text(
                                    strings.instrumentGuitar,
                                    fontWeight = if (selectedInstrument == Instrument.Guitar) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedInstrument == Instrument.Guitar) WebPrimary else Color.Unspecified
                                )
                            },
                            onClick = {
                                onInstrumentChange(Instrument.Guitar)
                                expandedInstrument = false
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Piano, null, tint = WebPrimary) },
                            text = {
                                Text(
                                    strings.instrumentPiano,
                                    fontWeight = if (selectedInstrument == Instrument.Piano) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedInstrument == Instrument.Piano) WebPrimary else Color.Unspecified
                                )
                            },
                            onClick = {
                                onInstrumentChange(Instrument.Piano)
                                expandedInstrument = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // View Mode Toggle inside Instrument: [ Acordes ] [ Mástil / Teclado ]
        Row(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ModeButton(
                text = strings.modeChords,
                isActive = instrumentView == InstrumentView.Chords,
                onClick = { onInstrumentViewChange(InstrumentView.Chords) }
            )
            Spacer(modifier = Modifier.width(6.dp))
            ModeButton(
                text = if (selectedInstrument == Instrument.Guitar) strings.modeFretboard else strings.modePianoScale,
                isActive = instrumentView == InstrumentView.Board,
                onClick = { onInstrumentViewChange(InstrumentView.Board) }
            )
        }
    }
}

@Composable
fun TonalityFilterButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isActive) 2.dp else 0.dp,
        border = if (isActive)
            androidx.compose.foundation.BorderStroke(1.5.dp, WebPrimary)
        else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isActive) WebPrimary else Color.DarkGray
        )
    }
}

@Composable
fun RootChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) WebPrimary else Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) WebPrimary else Color.Gray.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected) Color.White else Color(0xFF1E293B)
        )
    }
}

@Composable
fun ModeButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isActive) Color.White else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (isActive) 2.dp else 0.dp,
        border = if (isActive)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.DarkGray
        )
    }
}

@Composable
fun InstrumentButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) Color.White else Color.Transparent,
        shape = RoundedCornerShape(9.dp),
        shadowElevation = if (isActive) 3.dp else 0.dp,
        border = if (isActive)
            androidx.compose.foundation.BorderStroke(1.5.dp, WebPrimary)
        else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) WebPrimary else Color.DarkGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isActive) WebPrimary else Color.DarkGray
            )
        }
    }
}

