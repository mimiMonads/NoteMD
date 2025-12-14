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

    fun notes(tokenHash: String): Flow<List<Note>> =
        noteDao.observeNotes(tokenHash).map { entities -> entities.map { it.toDomain() } }

    fun observeNote(id: Long, tokenHash: String): Flow<Note?> =
        noteDao.observeNoteById(id, tokenHash).map { it?.toDomain() }

    suspend fun getNote(id: Long, tokenHash: String): Note? = noteDao.getNoteById(id, tokenHash)?.toDomain()

    suspend fun upsertNote(note: Note, tokenHash: String): Long {
        val updatedNote = note.copy(
            lastUpdated = System.currentTimeMillis(),
            tokenHash = tokenHash
        )
        val entity = updatedNote.toEntity()
        val id = noteDao.upsert(entity)
        val storedId = if (entity.id != 0L) entity.id else id
        noteFileStore.write(updatedNote.copy(id = storedId))
        return id
    }

    suspend fun deleteNote(id: Long, tokenHash: String) {
        noteDao.deleteById(id, tokenHash)
        noteFileStore.delete(id, tokenHash)
    }
}
