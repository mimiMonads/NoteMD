package com.example.notemd.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notemd.data.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val tags: String,
    val lastUpdated: Long
)

internal fun NoteEntity.toDomain(): Note =
    Note(
        id = id,
        title = title,
        content = content,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        lastUpdated = lastUpdated
    )

internal fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        content = content,
        tags = tags.joinToString(separator = ",") { it.trim() },
        lastUpdated = lastUpdated
    )
