package com.example.notemd.data.local

import android.content.Context
import com.example.notemd.data.Note
import java.io.File

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
        val file = File(notesDir, NoteCrypto.hashedFileName(note))
        file.writeText(NoteCrypto.encrypt(note))
    }

    fun delete(id: Long, tokenHash: String) {
        val file = File(notesDir, NoteCrypto.hashedFileName(id, tokenHash))
        if (file.exists()) file.delete()
    }
}
