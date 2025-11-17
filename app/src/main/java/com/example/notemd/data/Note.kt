package com.example.notemd.data

/**
 * Domain-level representation of a note persisted locally.
 */
data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
