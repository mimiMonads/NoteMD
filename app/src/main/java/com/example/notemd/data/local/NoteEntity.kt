package com.example.notemd.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notemd.data.Note
import org.json.JSONArray

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
        tags = tags.toTagList(),
        lastUpdated = lastUpdated
    )

internal fun Note.toEntity(): NoteEntity =
    NoteEntity(
        id = id,
        title = title,
        content = content,
        tags = tags.toJsonTagString(),
        lastUpdated = lastUpdated
    )

private fun List<String>.toJsonTagString(): String =
    JSONArray(
        this.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    ).toString()

private fun String.toTagList(): List<String> {
    if (isBlank()) return emptyList()
    val trimmed = trim()
    if (trimmed.startsWith("[")) {
        return runCatching {
            val array = JSONArray(trimmed)
            buildList {
                for (i in 0 until array.length()) {
                    val value = array.optString(i).trim()
                    if (value.isNotEmpty()) add(value)
                }
            }
        }.getOrElse { parseCommaSeparated() }
    }
    return parseCommaSeparated()
}

private fun String.parseCommaSeparated(): List<String> =
    split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
