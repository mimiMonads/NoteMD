package com.example.notemd.data.local

import android.content.Context
import com.example.notemd.data.Note
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Writes a lightweight JSON copy of each note to local storage for quick manipulation outside Room.
 */
class NoteFileStore(private val context: Context) {

    private val notesDir: File by lazy {
        File(context.filesDir, "notes").apply {
            if (!exists()) mkdirs()
        }
    }

    fun write(note: Note) {
        if (note.id == 0L) return
        val file = File(notesDir, "${note.id}.json")
        file.writeText(note.toJsonString())
    }

    fun delete(id: Long) {
        val file = File(notesDir, "$id.json")
        if (file.exists()) {
            file.delete()
        }
    }
}

private fun Note.toJsonString(): String {
    val json = JSONObject()
    json.put("id", id)
    json.put("title", title)
    json.put("content", content)
    json.put("tags", JSONArray(tags))
    json.put("lastUpdated", lastUpdated)
    return json.toString()
}
