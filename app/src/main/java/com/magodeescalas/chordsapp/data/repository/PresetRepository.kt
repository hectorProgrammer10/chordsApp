package com.magodeescalas.chordsapp.data.repository

import android.content.Context
import com.magodeescalas.chordsapp.domain.model.Preset
import com.magodeescalas.chordsapp.domain.model.ScaleType
import com.magodeescalas.chordsapp.domain.model.TemplateData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class PresetRepository(private val context: Context) {

    companion object {
        private const val PRESETS_FILE_NAME = "presets.json"
        private val TEMPLATE_KEYS = listOf("A", "B", "C", "D")
    }

    private fun getInternalFile(): File {
        return File(context.filesDir, PRESETS_FILE_NAME)
    }

    private fun getExternalBackupFile(): File? {
        val externalDir = context.getExternalFilesDir(null) ?: return null
        return File(externalDir, PRESETS_FILE_NAME)
    }

    suspend fun getPresets(): List<Preset> = withContext(Dispatchers.IO) {
        val file = getInternalFile()
        if (!file.exists()) {
            val backupFile = getExternalBackupFile()
            if (backupFile != null && backupFile.exists()) {
                try {
                    backupFile.copyTo(file, overwrite = true)
                } catch (_: Exception) {}
            }
        }

        if (!file.exists()) return@withContext emptyList()

        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            val presets = mutableListOf<Preset>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val name = obj.getString("name")
                val root = obj.getString("root")
                val scaleTypeName = obj.optString("scaleType", "Major")
                val scaleType = if (scaleTypeName == "Minor") ScaleType.Minor else ScaleType.Major
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                val chordsArray = obj.optJSONArray("chords") ?: JSONArray()
                val chordsList = mutableListOf<String>()
                for (j in 0 until chordsArray.length()) {
                    chordsList.add(chordsArray.getString(j))
                }

                val notesArray = obj.optJSONArray("notes") ?: JSONArray()
                val notesList = mutableListOf<String>()
                for (j in 0 until notesArray.length()) {
                    notesList.add(notesArray.getString(j))
                }

                // Parse templates A, B, C, D
                val templatesMap = mutableMapOf<String, TemplateData>()
                val templatesObj = obj.optJSONObject("templates")
                if (templatesObj != null) {
                    TEMPLATE_KEYS.forEach { key ->
                        val tObj = templatesObj.optJSONObject(key)
                        if (tObj != null) {
                            val tChordsArr = tObj.optJSONArray("chords") ?: JSONArray()
                            val tChords = mutableListOf<String>()
                            for (k in 0 until tChordsArr.length()) {
                                tChords.add(tChordsArr.getString(k))
                            }

                            val tNotesArr = tObj.optJSONArray("notes") ?: JSONArray()
                            val tNotes = mutableListOf<String>()
                            for (k in 0 until tNotesArr.length()) {
                                tNotes.add(tNotesArr.getString(k))
                            }
                            templatesMap[key] = TemplateData(tChords, tNotes)
                        } else {
                            templatesMap[key] = TemplateData()
                        }
                    }
                } else {
                    // Backwards compatibility for older presets: assign root chords/notes to template "A"
                    templatesMap["A"] = TemplateData(chordsList, notesList)
                    templatesMap["B"] = TemplateData()
                    templatesMap["C"] = TemplateData()
                    templatesMap["D"] = TemplateData()
                }

                presets.add(
                    Preset(
                        id = id,
                        name = name,
                        root = root,
                        scaleType = scaleType,
                        templates = templatesMap,
                        chords = chordsList,
                        notes = notesList,
                        createdAt = createdAt
                    )
                )
            }
            // Sort by most recently created
            presets.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun savePreset(preset: Preset): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentPresets = getPresets().filter { it.id != preset.id }.toMutableList()
            currentPresets.add(0, preset)

            savePresetList(currentPresets)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePreset(presetId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentPresets = getPresets().filter { it.id != presetId }
            savePresetList(currentPresets)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun savePresetList(presets: List<Preset>) {
        val jsonArray = JSONArray()
        for (p in presets) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("root", p.root)
            obj.put("scaleType", p.scaleType.name)
            obj.put("createdAt", p.createdAt)

            // Save templates map: "A", "B", "C", "D"
            val templatesObj = JSONObject()
            TEMPLATE_KEYS.forEach { key ->
                val tData = p.templates[key] ?: TemplateData()
                val tObj = JSONObject()

                val tChords = JSONArray()
                tData.chords.forEach { tChords.put(it) }
                tObj.put("chords", tChords)

                val tNotes = JSONArray()
                tData.notes.forEach { tNotes.put(it) }
                tObj.put("notes", tNotes)

                templatesObj.put(key, tObj)
            }
            obj.put("templates", templatesObj)

            // Legacy fallback: template A chords and notes
            val aChords = p.templates["A"]?.chords ?: p.chords
            val aNotes = p.templates["A"]?.notes ?: p.notes

            val chordsArray = JSONArray()
            aChords.forEach { chordsArray.put(it) }
            obj.put("chords", chordsArray)

            val notesArray = JSONArray()
            aNotes.forEach { notesArray.put(it) }
            obj.put("notes", notesArray)

            jsonArray.put(obj)
        }

        val jsonString = jsonArray.toString(2)

        // Save to internal storage (persistent memory on device, NOT cache)
        val internalFile = getInternalFile()
        internalFile.writeText(jsonString)

        // Also save to external files directory for secondary persistence
        try {
            getExternalBackupFile()?.writeText(jsonString)
        } catch (_: Exception) {}
    }
}
