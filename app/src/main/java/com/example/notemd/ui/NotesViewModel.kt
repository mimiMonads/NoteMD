package com.example.notemd.ui

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notemd.NoteMDApplication
import com.example.notemd.data.Note
import com.example.notemd.data.NoteRepository
import com.example.notemd.token.TokenSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NoteListUiState(
    val notes: List<NotePreview> = emptyList()
)

class NotesViewModel(
    private val repository: NoteRepository,
    private val tokenSessionManager: TokenSessionManager
) : ViewModel() {

    val uiState = tokenSessionManager.activeTokenHash
        .flatMapLatest { tokenHash ->
            if (tokenHash.isNullOrEmpty()) {
                repository.notes(com.example.notemd.token.TokenUtils.defaultTokenHash)
            } else {
                repository.notes(tokenHash)
            }
        }
        .map { notes -> NoteListUiState(notes = notes.map { it.toPreview() }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteListUiState()
        )

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            val tokenHash = tokenSessionManager.activeTokenHash.value
                ?: com.example.notemd.token.TokenUtils.defaultTokenHash
            repository.deleteNote(id, tokenHash)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteMDApplication)
                NotesViewModel(
                    repository = application.container.noteRepository,
                    tokenSessionManager = application.container.tokenSessionManager
                )
            }
        }
    }
}

private fun Note.toPreview(): NotePreview {
    val summaryText = content
        .lineSequence()
        .firstOrNull { it.isNotBlank() }
        ?: content.take(160)

    val lastUpdatedText = DateUtils.getRelativeTimeSpanString(
        lastUpdated,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    return NotePreview(
        id = id,
        title = if (title.isNotBlank()) title else summaryText.take(32),
        summary = summaryText,
        body = content,
        tags = tags,
        lastUpdated = lastUpdatedText,
        latitude = latitude,
        longitude = longitude
    )
}
