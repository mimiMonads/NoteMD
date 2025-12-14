package com.example.notemd.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE tokenHash = :tokenHash ORDER BY lastUpdated DESC")
    fun observeNotes(tokenHash: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND tokenHash = :tokenHash")
    fun observeNoteById(id: Long, tokenHash: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id AND tokenHash = :tokenHash")
    suspend fun getNoteById(id: Long, tokenHash: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity): Long

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id AND tokenHash = :tokenHash")
    suspend fun deleteById(id: Long, tokenHash: String)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}
