package com.magodeescalas.chordsapp.presentation.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.domain.model.TemplateData
import com.magodeescalas.chordsapp.presentation.ui.components.ChordCard
import com.magodeescalas.chordsapp.presentation.ui.components.Controls
import com.magodeescalas.chordsapp.presentation.ui.components.Fretboard
import com.magodeescalas.chordsapp.presentation.ui.components.GuitarTablature
import com.magodeescalas.chordsapp.presentation.ui.components.GuitarTunerSheet
import com.magodeescalas.chordsapp.presentation.ui.components.LoadPresetsSheet
import com.magodeescalas.chordsapp.presentation.ui.components.PianoChordCard
import com.magodeescalas.chordsapp.presentation.ui.components.PianoRoll
import com.magodeescalas.chordsapp.presentation.ui.components.PianoScaleBoard
import com.magodeescalas.chordsapp.presentation.ui.components.SavePresetDialog
import com.magodeescalas.chordsapp.presentation.ui.components.SelectionToolbar
import com.magodeescalas.chordsapp.presentation.ui.components.TemplateSelector
import com.magodeescalas.chordsapp.presentation.util.AppResources
import com.magodeescalas.chordsapp.presentation.viewmodel.DisplayMode
import com.magodeescalas.chordsapp.presentation.viewmodel.Instrument
import com.magodeescalas.chordsapp.presentation.viewmodel.InstrumentView
import com.magodeescalas.chordsapp.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private val ROOTS = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val strings = if (uiState.language == "en") AppResources.en else AppResources.es

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    // Sync HorizontalPager page swipe to ViewModel template
    LaunchedEffect(pagerState.currentPage) {
        if (uiState.currentTemplateIndex != pagerState.currentPage) {
            viewModel.selectTemplate(pagerState.currentPage)
        }
    }

    // Sync ViewModel template change to HorizontalPager
    LaunchedEffect(uiState.currentTemplateIndex) {
        if (pagerState.currentPage != uiState.currentTemplateIndex) {
            pagerState.animateScrollToPage(uiState.currentTemplateIndex)
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Tuner and Presets buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TunerButton(text = strings.tunerButton) {
                        viewModel.openTuner()
                    }

                    PresetsNavButton(text = strings.presetsButton) {
                        viewModel.openLoadPresetsSheet(context)
                    }
                }

                // Right side: Language Selector
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LanguageButton(
                        text = "EN",
                        isActive = uiState.language == "en",
                        onClick = { viewModel.onLanguageChange("en") }
                    )
                    LanguageButton(
                        text = "ES",
                        isActive = uiState.language == "es",
                        onClick = { viewModel.onLanguageChange("es") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Header (App Title)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF2EAA1E),
                                        Color(0xFF489DEC)
                                    )
                                )
                            )
                        ) {
                            append("Chords")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1E293B))) {
                            append("App")
                        }
                    },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )

                Text(
                    text = strings.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(19.dp))

            // Universal Controls (Root, Scale, Instrument Selector, View Mode)
            Controls(
                selectedRoot = uiState.root,
                onRootChange = viewModel::onRootChange,
                selectedScaleType = uiState.scaleType,
                onScaleTypeChange = viewModel::onScaleTypeChange,
                selectedInstrument = uiState.instrument,
                onInstrumentChange = viewModel::onInstrumentChange,
                instrumentView = uiState.instrumentView,
                onInstrumentViewChange = viewModel::onInstrumentViewChange,
                mode = uiState.mode,
                onModeChange = viewModel::onModeChange,
                strings = strings,
                roots = ROOTS
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Title of current instrument, view & key
            val scaleTypeName =
                if (uiState.scaleType == ScaleType.Major)
                    strings.scaleTypeMajor
                else strings.scaleTypeMinor

            val modeTitle = when (uiState.instrument) {
                Instrument.Guitar -> {
                    if (uiState.instrumentView == InstrumentView.Chords) {
                        "${strings.headersChordsIn} ${uiState.root} $scaleTypeName"
                    } else {
                        "${strings.headersScaleOnFretboard}: ${uiState.root} $scaleTypeName"
                    }
                }
                Instrument.Piano -> {
                    if (uiState.instrumentView == InstrumentView.Chords) {
                        "${strings.headersChordsInPiano}${uiState.root} $scaleTypeName"
                    } else {
                        "${strings.headersScaleOnPiano}: ${uiState.root} $scaleTypeName"
                    }
                }
            }

            Text(
                text = modeTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            // Interactive Selection Toolbar placed right below the chords title
            SelectionToolbar(
                selectedChords = uiState.selectedChords,
                selectedNotes = uiState.selectedNotes,
                currentTemplateKey = uiState.currentTemplateKey,
                strings = strings,
                onSaveClick = { viewModel.openSavePresetDialog() },
                onClearClick = { viewModel.clearSelection() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Diagram Area with Horizontal Pager (Swipes between templates A, B, C, D)
            AnimatedVisibility(visible = true, enter = fadeIn()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val templateKey = listOf("A", "B", "C", "D")[page]
                    val templateData = uiState.templates[templateKey] ?: TemplateData()
                    val pageChords = templateData.chords
                    val pageNotes = templateData.notes

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (uiState.instrument) {
                            Instrument.Guitar -> {
                                if (uiState.instrumentView == InstrumentView.Chords) {
                                    // Guitar Chords Grid
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(19.dp),
                                        maxItemsInEachRow = 4,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        uiState.scaleData?.chords?.forEach { chord ->
                                            val chordKey = "${chord.root} ${chord.quality.name}"
                                            val isSelected = pageChords.contains(chordKey)
                                            val selectionIndex = if (isSelected) pageChords.indexOf(chordKey) + 1 else null

                                            ChordCard(
                                                chord = chord,
                                                isSelected = isSelected,
                                                selectionIndex = selectionIndex,
                                                onClick = { viewModel.toggleChordSelection(chordKey, templateKey) }
                                            )
                                        }
                                    }
                                } else {
                                    // Guitar Fretboard
                                    Fretboard(
                                        fretboardData = uiState.fretboardMap,
                                        selectedNotes = pageNotes,
                                        onNoteClick = { noteName -> viewModel.toggleNoteSelection(noteName, templateKey) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Legend
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        LegendItem(
                                            color = MaterialTheme.colorScheme.secondary,
                                            text = strings.legendRootNote
                                        )
                                        LegendItem(
                                            color = MaterialTheme.colorScheme.primary,
                                            text = strings.legendScaleNote
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Guitar Tablature (sequence visualization below fretboard)
                                    GuitarTablature(
                                        notes = pageNotes,
                                        onNoteClick = { stepIndex ->
                                            viewModel.removeNoteAtIndex(stepIndex, templateKey)
                                        },
                                        onClear = { viewModel.clearTemplateNotes(templateKey) }
                                    )
                                }
                            }
                            Instrument.Piano -> {
                                if (uiState.instrumentView == InstrumentView.Chords) {
                                    // Piano Chords Grid
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(19.dp),
                                        maxItemsInEachRow = 4,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        uiState.scaleData?.chords?.forEach { chord ->
                                            val chordKey = "${chord.root} ${chord.quality.name}"
                                            val isSelected = pageChords.contains(chordKey)
                                            val selectionIndex = if (isSelected) pageChords.indexOf(chordKey) + 1 else null

                                            PianoChordCard(
                                                chord = chord,
                                                isSelected = isSelected,
                                                selectionIndex = selectionIndex,
                                                onClick = { viewModel.toggleChordSelection(chordKey, templateKey) }
                                            )
                                        }
                                    }
                                } else {
                                    // Full Piano Scale Keyboard
                                    PianoScaleBoard(
                                        scaleNotes = uiState.scaleData?.notes ?: emptyList(),
                                        rootNote = uiState.root,
                                        selectedNotes = pageNotes,
                                        onNoteClick = { noteName -> viewModel.toggleNoteSelection(noteName, templateKey) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Legend
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        LegendItem(
                                            color = MaterialTheme.colorScheme.secondary,
                                            text = strings.legendRootNote
                                        )
                                        LegendItem(
                                            color = MaterialTheme.colorScheme.primary,
                                            text = strings.legendScaleNote
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Piano Roll (sequence visualization below piano keyboard)
                                    PianoRoll(
                                        notes = pageNotes,
                                        onNoteClick = { stepIndex ->
                                            viewModel.removeNoteAtIndex(stepIndex, templateKey)
                                        },
                                        onClear = { viewModel.clearTemplateNotes(templateKey) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Template Selector [ A | B | C | D ] positioned right below the diagrams
            TemplateSelector(
                selectedIndex = pagerState.currentPage,
                templates = uiState.templates,
                onSelectTemplate = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Modal Overlays

    if (uiState.isTunerOpen) {
        GuitarTunerSheet(
            uiState = uiState,
            strings = strings,
            onClose = viewModel::closeTuner,
            onReset = viewModel::resetTunerState,
            onPermissionResult = viewModel::onAudioPermissionResult
        )
    }

    if (uiState.isSavePresetDialogOpen) {
        val presetToOverwrite = uiState.presets.find { it.id == uiState.presetIdToOverwrite }
        SavePresetDialog(
            root = uiState.root,
            scaleType = uiState.scaleType,
            templates = uiState.templates,
            selectedChords = uiState.selectedChords,
            selectedNotes = uiState.selectedNotes,
            presetToOverwrite = presetToOverwrite,
            strings = strings,
            onSave = { name -> viewModel.saveCurrentTemplate(name, context, uiState.presetIdToOverwrite) },
            onDismiss = { viewModel.closeSavePresetDialog() }
        )
    }

    if (uiState.isLoadPresetsSheetOpen) {
        LoadPresetsSheet(
            presets = uiState.presets,
            strings = strings,
            onLoadPreset = { preset -> viewModel.loadPreset(preset) },
            onDeletePreset = { presetId -> viewModel.deletePreset(presetId, context) },
            onOverwritePreset = { preset ->
                viewModel.closeLoadPresetsSheet()
                viewModel.openSavePresetDialog(preset.id)
            },
            onClose = { viewModel.closeLoadPresetsSheet() }
        )
    }
}

@Composable
fun LanguageButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color.White else Color.Transparent,
            contentColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray
        ),
        border = if (isActive)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    }
}

@Composable
fun TunerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF2EAA1E),
                    Color(0xFF489DEC)
                )
            )
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        shape = RoundedCornerShape(17.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF2EAA1E)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PresetsNavButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF489DEC),
                    Color(0xFF8B5CF6)
                )
            )
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = Modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
        shape = RoundedCornerShape(17.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF489DEC)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
