package com.magodeescalas.chordsapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.magodeescalas.chordsapp.data.audio.AudioPitchDetector
import com.magodeescalas.chordsapp.data.repository.PresetRepository
import com.magodeescalas.chordsapp.domain.model.FretboardNote
import com.magodeescalas.chordsapp.domain.model.GuitarConstants
import com.magodeescalas.chordsapp.domain.model.GuitarString
import com.magodeescalas.chordsapp.domain.model.PitchDetectionResult
import com.magodeescalas.chordsapp.domain.model.Preset
import com.magodeescalas.chordsapp.domain.model.Scale
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.domain.model.TemplateData
import com.magodeescalas.chordsapp.domain.model.TuningStatus
import com.magodeescalas.chordsapp.domain.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
        val root: String = "C",
        val scaleType: ScaleType = ScaleType.Major,
        val instrument: Instrument = Instrument.Guitar,
        val instrumentView: InstrumentView = InstrumentView.Chords,
        val mode: DisplayMode = DisplayMode.Chords,
        val scaleData: Scale? = null,
        val fretboardMap: List<List<FretboardNote>> = emptyList(),
        val language: String = "en",

        // Guitar Tuner State
        val isTunerOpen: Boolean = false,
        val hasAudioPermission: Boolean = false,
        val detectedPitch: Float = 0f,
        val detectedCents: Float = 0f,
        val detectedStringIndex: Int = -1,
        val targetString: GuitarString? = null,
        val tuningStatus: TuningStatus = TuningStatus.IDLE,
        val tunedStrings: Set<Int> = emptySet(),
        val allStringsTuned: Boolean = false,

        // Templates State (A, B, C, D)
        val currentTemplateIndex: Int = 0, // 0 = A, 1 = B, 2 = C, 3 = D
        val templates: Map<String, TemplateData> = mapOf(
            "A" to TemplateData(),
            "B" to TemplateData(),
            "C" to TemplateData(),
            "D" to TemplateData()
        ),

        // Selection & Presets State (convenience for active template)
        val selectedChords: List<String> = emptyList(),
        val selectedNotes: List<String> = emptyList(),
        val presets: List<Preset> = emptyList(),
        val isSavePresetDialogOpen: Boolean = false,
    val presetIdToOverwrite: String? = null,
        val isLoadPresetsSheetOpen: Boolean = false,
        val userMessage: String? = null
) {
    val currentTemplateKey: String
        get() = listOf("A", "B", "C", "D").getOrElse(currentTemplateIndex) { "A" }
}

enum class Instrument {
    Guitar,
    Piano
}

enum class InstrumentView {
    Chords,
    Board
}

enum class DisplayMode {
    Chords,
    Piano,
    Fretboard
}

class MainViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val pitchDetector = AudioPitchDetector()

    init {
        updateScaleData()
    }

    // --- Tuner Methods ---

    fun openTuner() {
        _uiState.update { it.copy(isTunerOpen = true) }
        resetTunerState()
        if (_uiState.value.hasAudioPermission) {
            startPitchDetection()
        }
    }

    fun closeTuner() {
        stopPitchDetection()
        resetTunerState()
        _uiState.update { it.copy(isTunerOpen = false) }
    }

    fun onAudioPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasAudioPermission = granted) }
        if (granted && _uiState.value.isTunerOpen) {
            startPitchDetection()
        }
    }

    fun resetTunerState() {
        _uiState.update {
            it.copy(
                detectedPitch = 0f,
                detectedCents = 0f,
                detectedStringIndex = -1,
                targetString = null,
                tuningStatus = TuningStatus.IDLE,
                tunedStrings = emptySet(),
                allStringsTuned = false
            )
        }
    }

    private fun startPitchDetection() {
        pitchDetector.start(viewModelScope)
        viewModelScope.launch {
            pitchDetector.detectionResult.collect { result ->
                processPitchResult(result)
            }
        }
    }

    private fun stopPitchDetection() {
        pitchDetector.stop()
    }

    private fun processPitchResult(result: PitchDetectionResult) {
        if (!result.isConfident || result.frequency <= 0f) {
            _uiState.update {
                it.copy(
                    detectedPitch = 0f,
                    detectedCents = 0f,
                    tuningStatus = TuningStatus.IDLE
                )
            }
            return
        }

        _uiState.update { current ->
            val updatedTunedStrings = current.tunedStrings.toMutableSet()
            if (result.tuningStatus == TuningStatus.IN_TUNE && result.stringIndex >= 0) {
                updatedTunedStrings.add(result.stringIndex)
            }
            val allTuned = updatedTunedStrings.size >= GuitarConstants.STANDARD_GUITAR_STRINGS.size

            current.copy(
                detectedPitch = result.frequency,
                detectedCents = result.cents,
                detectedStringIndex = result.stringIndex,
                targetString = result.targetString,
                tuningStatus = result.tuningStatus,
                tunedStrings = updatedTunedStrings,
                allStringsTuned = allTuned
            )
        }
    }

    // --- Template & Sequence Selection Methods ---

    fun selectTemplate(index: Int) {
        _uiState.update { current ->
            val safeIndex = index.coerceIn(0, 3)
            val templateKey = listOf("A", "B", "C", "D")[safeIndex]
            val tData = current.templates[templateKey] ?: TemplateData()
            current.copy(
                currentTemplateIndex = safeIndex,
                selectedChords = tData.chords,
                selectedNotes = tData.notes
            )
        }
    }

    fun toggleChordSelection(chordKey: String, templateKey: String? = null) {
        _uiState.update { current ->
            val targetKey = templateKey ?: current.currentTemplateKey
            val currentTemplateData = current.templates[targetKey] ?: TemplateData()
            val list = currentTemplateData.chords.toMutableList()
            if (list.contains(chordKey)) {
                list.remove(chordKey)
            } else {
                list.add(chordKey)
            }
            val updatedTemplateData = currentTemplateData.copy(chords = list)
            val updatedTemplates = current.templates.toMutableMap()
            updatedTemplates[targetKey] = updatedTemplateData

            val isCurrent = (targetKey == current.currentTemplateKey)
            current.copy(
                templates = updatedTemplates,
                selectedChords = if (isCurrent) list else current.selectedChords
            )
        }
    }

    /**
     * Always adds the note to the sequence (duplicates allowed).
     * Notes are removed only via [removeNoteAtIndex] by tapping the tablature/piano roll.
     */
    fun toggleNoteSelection(noteName: String, templateKey: String? = null) {
        _uiState.update { current ->
            val targetKey = templateKey ?: current.currentTemplateKey
            val currentTemplateData = current.templates[targetKey] ?: TemplateData()
            val list = currentTemplateData.notes.toMutableList()
            list.add(noteName)
            val updatedTemplateData = currentTemplateData.copy(notes = list)
            val updatedTemplates = current.templates.toMutableMap()
            updatedTemplates[targetKey] = updatedTemplateData

            val isCurrent = (targetKey == current.currentTemplateKey)
            current.copy(
                templates = updatedTemplates,
                selectedNotes = if (isCurrent) list else current.selectedNotes
            )
        }
    }

    /**
     * Removes the note at a specific index from the sequence.
     * Called when the user taps a note block in the tablature (guitar) or piano roll (piano).
     */
    fun removeNoteAtIndex(index: Int, templateKey: String? = null) {
        _uiState.update { current ->
            val targetKey = templateKey ?: current.currentTemplateKey
            val currentTemplateData = current.templates[targetKey] ?: TemplateData()
            val list = currentTemplateData.notes.toMutableList()
            if (index in list.indices) {
                list.removeAt(index)
            }
            val updatedTemplateData = currentTemplateData.copy(notes = list)
            val updatedTemplates = current.templates.toMutableMap()
            updatedTemplates[targetKey] = updatedTemplateData

            val isCurrent = (targetKey == current.currentTemplateKey)
            current.copy(
                templates = updatedTemplates,
                selectedNotes = if (isCurrent) list else current.selectedNotes
            )
        }
    }

    fun clearSelection(templateKey: String? = null) {
        _uiState.update { current ->
            val targetKey = templateKey ?: current.currentTemplateKey
            val updatedTemplates = current.templates.toMutableMap()
            updatedTemplates[targetKey] = TemplateData()

            val isCurrent = (targetKey == current.currentTemplateKey)
            current.copy(
                templates = updatedTemplates,
                selectedChords = if (isCurrent) emptyList() else current.selectedChords,
                selectedNotes = if (isCurrent) emptyList() else current.selectedNotes
            )
        }
    }

    fun clearTemplateNotes(templateKey: String? = null) {
        _uiState.update { current ->
            val targetKey = templateKey ?: current.currentTemplateKey
            val currentTemplateData = current.templates[targetKey] ?: TemplateData()
            val updatedTemplateData = currentTemplateData.copy(notes = emptyList())
            val updatedTemplates = current.templates.toMutableMap()
            updatedTemplates[targetKey] = updatedTemplateData

            val isCurrent = (targetKey == current.currentTemplateKey)
            current.copy(
                templates = updatedTemplates,
                selectedNotes = if (isCurrent) emptyList() else current.selectedNotes
            )
        }
    }

    fun clearAllTemplates() {
        _uiState.update { current ->
            val cleared = mapOf(
                "A" to TemplateData(),
                "B" to TemplateData(),
                "C" to TemplateData(),
                "D" to TemplateData()
            )
            current.copy(
                templates = cleared,
                selectedChords = emptyList(),
                selectedNotes = emptyList()
            )
        }
    }

    // --- Preset Storage & Management ---

    fun openSavePresetDialog(presetId: String? = null) {
        _uiState.update { it.copy(isSavePresetDialogOpen = true, presetIdToOverwrite = presetId) }
    }


    fun closeSavePresetDialog() {
        _uiState.update { it.copy(isSavePresetDialogOpen = false, presetIdToOverwrite = null) }
    }


    fun openLoadPresetsSheet(context: Context) {
        _uiState.update { it.copy(isLoadPresetsSheetOpen = true) }
        loadPresetsFromStorage(context)
    }

    fun closeLoadPresetsSheet() {
        _uiState.update { it.copy(isLoadPresetsSheetOpen = false) }
    }

    fun loadPresetsFromStorage(context: Context) {
        viewModelScope.launch {
            val repository = PresetRepository(context)
            val list = repository.getPresets()
            _uiState.update { it.copy(presets = list) }
        }
    }

    fun saveCurrentPreset(name: String, context: Context) {
        viewModelScope.launch {
            val current = _uiState.value
            val preset = Preset(
                name = name.ifBlank { "Preset ${current.root} ${current.scaleType.name}" },
                root = current.root,
                scaleType = current.scaleType,
                templates = current.templates,
                chords = current.templates["A"]?.chords ?: emptyList(),
                notes = current.templates["A"]?.notes ?: emptyList()
            )

            val repository = PresetRepository(context)
            repository.savePreset(preset)
            val updatedPresets = repository.getPresets()

            _uiState.update {
                it.copy(
                    presets = updatedPresets,
                    isSavePresetDialogOpen = false,
                    userMessage = "Preset '${preset.name}' guardado"
                )
            }
        }
    }

    /**
     * Save current UI state as a preset, optionally overwriting an existing preset identified by presetId.
     */
    fun saveCurrentTemplate(name: String, context: Context, presetId: String? = null) {
        viewModelScope.launch {
            val current = _uiState.value
            val preset = Preset(
                id = presetId ?: java.util.UUID.randomUUID().toString(),
                name = name.ifBlank { "Preset ${current.root} ${current.scaleType.name}" },
                root = current.root,
                scaleType = current.scaleType,
                templates = current.templates,
                chords = current.templates["A"]?.chords ?: emptyList(),
                notes = current.templates["A"]?.notes ?: emptyList()
            )

            val repository = PresetRepository(context)
            repository.savePreset(preset)
            val updatedPresets = repository.getPresets()

            _uiState.update {
                it.copy(
                    presets = updatedPresets,
                    isSavePresetDialogOpen = false,
                    presetIdToOverwrite = null,
                    userMessage = "Preset '${preset.name}' guardado"
                )
            }
        }
    }

    fun loadPreset(preset: Preset) {
        viewModelScope.launch {
            val restoredTemplates = if (preset.templates.isNotEmpty()) {
                preset.templates
            } else {
                mapOf(
                    "A" to TemplateData(preset.chords, preset.notes),
                    "B" to TemplateData(),
                    "C" to TemplateData(),
                    "D" to TemplateData()
                )
            }

            val activeTemplate = restoredTemplates["A"] ?: TemplateData()

            _uiState.update {
                it.copy(
                    root = preset.root,
                    scaleType = preset.scaleType,
                    currentTemplateIndex = 0,
                    templates = restoredTemplates,
                    selectedChords = activeTemplate.chords,
                    selectedNotes = activeTemplate.notes,
                    isLoadPresetsSheetOpen = false,
                    userMessage = "Preset '${preset.name}' cargado"
                )
            }
            // Update the scale and fretboard data for the restored key
            val data = repository.getScale(preset.root, preset.scaleType)
            val map = repository.getFretboardMap(preset.root, preset.scaleType)
            _uiState.update { it.copy(scaleData = data, fretboardMap = map) }
        }
    }

    fun deletePreset(presetId: String, context: Context) {
        viewModelScope.launch {
            val repository = PresetRepository(context)
            repository.deletePreset(presetId)
            val updated = repository.getPresets()
            _uiState.update { it.copy(presets = updated) }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    // --- Navigation & Scale Config ---

    fun onRootChange(newRoot: String) {
        _uiState.update { it.copy(root = newRoot) }
        updateScaleData()
    }

    fun onScaleTypeChange(newType: ScaleType) {
        _uiState.update { it.copy(scaleType = newType) }
        updateScaleData()
    }

    fun onModeChange(newMode: DisplayMode) {
        _uiState.update { it.copy(mode = newMode) }
    }

    fun onInstrumentChange(newInstrument: Instrument) {
        _uiState.update { it.copy(instrument = newInstrument) }
    }

    fun onInstrumentViewChange(newView: InstrumentView) {
        _uiState.update { it.copy(instrumentView = newView) }
    }

    fun onLanguageChange(newLanguage: String) {
        _uiState.update { it.copy(language = newLanguage) }
    }

    private fun updateScaleData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val data = repository.getScale(currentState.root, currentState.scaleType)
            val map = repository.getFretboardMap(currentState.root, currentState.scaleType)
            _uiState.update { it.copy(scaleData = data, fretboardMap = map) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pitchDetector.stop()
    }

    // Factory to allow passing Repository
    class Factory(private val repository: MusicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
