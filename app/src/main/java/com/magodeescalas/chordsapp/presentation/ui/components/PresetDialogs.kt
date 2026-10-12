package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.magodeescalas.chordsapp.domain.model.Preset
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.domain.model.TemplateData
import com.magodeescalas.chordsapp.presentation.util.AppStrings
import com.magodeescalas.chordsapp.ui.theme.WebPrimary
import com.magodeescalas.chordsapp.ui.theme.WebSecondary

/**
 * Floating bar displayed when chords or notes are selected.
 */
@Composable
fun SelectionToolbar(
    selectedChords: List<String>,
    selectedNotes: List<String>,
    currentTemplateKey: String = "A",
    strings: AppStrings,
    onSaveClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasSelection = selectedChords.isNotEmpty() || selectedNotes.isNotEmpty()
    if (!hasSelection) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.94f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(WebPrimary, WebSecondary))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Template badge and count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(WebPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Template $currentTemplateKey",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    val count = if (selectedChords.isNotEmpty()) selectedChords.size else selectedNotes.size
                    Text(
                        text = if (selectedChords.isNotEmpty()) "$count ${strings.chordsSelected.lowercase()}" else "$count ${strings.notesSelected.lowercase()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }

                // Right side: Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onClearClick,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.75f))
                    ) {
                        Text(text = strings.clearSelection, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSaveClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.savePreset, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sequence items (1, 2, 3...)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = if (selectedChords.isNotEmpty()) selectedChords else selectedNotes
                items.forEachIndexed { index, item ->
                    SelectionSequenceChip(index = index + 1, label = item)
                }
            }
        }
    }
}

@Composable
private fun SelectionSequenceChip(index: Int, label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, WebPrimary.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(WebPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Save Preset Dialog allowing the user to name the preset.
 * Displays key/tonality and summaries for templates A, B, C, D.
 */
@Composable
fun SavePresetDialog(
    root: String,
    scaleType: ScaleType,
    templates: Map<String, TemplateData> = emptyMap(),
    selectedChords: List<String> = emptyList(),
    selectedNotes: List<String> = emptyList(),
    presetToOverwrite: Preset? = null,
    strings: AppStrings,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var presetName by remember(presetToOverwrite) { mutableStateOf(presetToOverwrite?.name ?: "") }
    val scaleTypeName = if (scaleType == ScaleType.Major) strings.scaleTypeMajor else strings.scaleTypeMinor

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(WebPrimary, WebSecondary))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = WebPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (presetToOverwrite != null) "Sobrescribir Preset" else strings.savePreset,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tonality Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WebSecondary.copy(alpha = 0.2f))
                        .border(1.dp, WebSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = WebSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${strings.presetKeyLabel}: $root $scaleTypeName",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Templates summary pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("A", "B", "C", "D").forEach { key ->
                        val tData = templates[key]
                        val count = (tData?.chords?.size ?: 0) + (tData?.notes?.size ?: 0)
                        val hasItems = count > 0

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasItems) WebPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (hasItems) WebPrimary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (hasItems) "$key: $count" else "$key: -",
                                fontSize = 11.sp,
                                fontWeight = if (hasItems) FontWeight.Bold else FontWeight.Normal,
                                color = if (hasItems) WebPrimary else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preview of sequence in current template
                val items = if (selectedChords.isNotEmpty()) selectedChords else selectedNotes
                if (items.isNotEmpty()) {
                    Text(
                        text = items.mapIndexed { idx, it -> "${idx + 1}: $it" }.joinToString("  →  "),
                        fontSize = 12.sp,
                        color = WebPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(strings.presetNameLabel) },
                    placeholder = { Text(strings.presetNamePlaceholder) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WebPrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedLabelColor = WebPrimary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = strings.cancelButton, color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(presetName) },
                        colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (presetToOverwrite != null) "Sobrescribir" else strings.saveButton,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Load Presets Bottom Sheet displaying saved presets in phone memory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadPresetsSheet(
    presets: List<Preset>,
    strings: AppStrings,
    onLoadPreset: (Preset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onOverwritePreset: (Preset) -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = Color(0xFF0F172A),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = WebSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.loadPresets,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Guardados en la memoria del teléfono",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (presets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noPresetsSaved,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                presets.forEach { preset ->
                    PresetCardItem(
                        preset = preset,
                        strings = strings,
                        onLoad = { onLoadPreset(preset) },
                        onDelete = { onDeletePreset(preset.id) },
                        onOverwrite = { onOverwritePreset(preset) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun PresetCardItem(
    preset: Preset,
    strings: AppStrings,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    onOverwrite: (Preset) -> Unit
) {
    val scaleTypeName = if (preset.scaleType == ScaleType.Major) strings.scaleTypeMajor else strings.scaleTypeMinor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(WebPrimary.copy(alpha = 0.5f), WebSecondary.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = preset.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Key Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WebSecondary.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${preset.root} $scaleTypeName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WebSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Templates badges (A, B, C, D)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("A", "B", "C", "D").forEach { key ->
                    val tData = preset.templates[key]
                    val count = (tData?.chords?.size ?: 0) + (tData?.notes?.size ?: 0)
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WebPrimary.copy(alpha = 0.2f))
                                .border(1.dp, WebPrimary.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$key ($count)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WebPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sequence preview (from template A or chords)
            val aChords = preset.templates["A"]?.chords ?: preset.chords
            val aNotes = preset.templates["A"]?.notes ?: preset.notes
            val items = if (aChords.isNotEmpty()) aChords else aNotes
            if (items.isNotEmpty()) {
                Text(
                    text = items.mapIndexed { idx, item -> "${idx + 1}: $item" }.joinToString("  →  "),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = strings.deleteButton, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onLoad,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = strings.loadButton, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Overwrite button
                Button(
                    onClick = { onOverwrite(preset) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WebSecondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Sobrescribir", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}
