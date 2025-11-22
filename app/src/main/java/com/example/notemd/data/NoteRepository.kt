package com.example.notemd.data

import com.example.notemd.data.local.NoteDao
import com.example.notemd.data.local.NoteFileStore
import com.example.notemd.data.local.toDomain
import com.example.notemd.data.local.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(
    private val noteDao: NoteDao,
    private val noteFileStore: NoteFileStore
) {

    val notes: Flow<List<Note>> =
        noteDao.observeNotes().map { entities -> entities.map { it.toDomain() } }

    fun observeNote(id: Long): Flow<Note?> =
        noteDao.observeNoteById(id).map { it?.toDomain() }

    suspend fun getNote(id: Long): Note? = noteDao.getNoteById(id)?.toDomain()

    suspend fun upsertNote(note: Note): Long {
        val updatedNote = note.copy(lastUpdated = System.currentTimeMillis())
        val entity = updatedNote.toEntity()
        val id = noteDao.upsert(entity)
        val storedId = if (entity.id != 0L) entity.id else id
        noteFileStore.write(updatedNote.copy(id = storedId))
        return id
    }

    suspend fun deleteNote(id: Long) {
        noteDao.deleteById(id)
        noteFileStore.delete(id)
    }
}
