package com.example.notemd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notemd.NoteMDApplication
import com.example.notemd.data.Note
import com.example.notemd.data.NoteRepository
import com.example.notemd.token.TokenSessionManager
import com.example.notemd.token.TokenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val tagsInput: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false
) {
    val isEditing: Boolean get() = noteId != null
    val canSave: Boolean get() = title.isNotBlank() || content.isNotBlank()
    val hasLocation: Boolean get() = latitude != null && longitude != null
}

class NoteEditorViewModel(
    private val repository: NoteRepository,
    private val tokenSessionManager: TokenSessionManager,
    private val initialNoteId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NoteEditorUiState(noteId = initialNoteId)
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        initialNoteId?.let { id ->
            viewModelScope.launch {
                val tokenHash = tokenSessionManager.getActiveOrDefault()
                repository.getNote(id, tokenHash)?.let { note ->
                    _uiState.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            content = note.content,
                            tagsInput = note.tags.joinToString(", "),
                            latitude = note.latitude,
                            longitude = note.longitude
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onContentChange(value: String) {
        _uiState.update { it.copy(content = value) }
    }

    fun onTagsChange(value: String) {
        _uiState.update { it.copy(tagsInput = value) }
    }

    fun save(onSaved: () -> Unit) {
        val current = _uiState.value
        if (!current.canSave || current.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val tokenHash = tokenSessionManager.getActiveOrDefault()
            val note = Note(
                id = current.noteId ?: 0L,
                title = current.title.trim(),
                content = current.content.trim(),
                tags = current.tagsInput.toTagList().distinct(),
                latitude = current.latitude,
                longitude = current.longitude,
                tokenHash = tokenHash
            )
            val newId = repository.upsertNote(note, tokenHash)
            _uiState.update {
                it.copy(
                    noteId = it.noteId ?: newId,
                    tagsInput = note.tags.joinToString(", "),
                    isSaving = false
                )
            }
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = _uiState.value.noteId ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            val tokenHash = tokenSessionManager.getActiveOrDefault()
            repository.deleteNote(id, tokenHash)
            _uiState.value = NoteEditorUiState()
            onDeleted()
        }
    }

    fun setLocation(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    fun clearLocation() {
        _uiState.update { it.copy(latitude = null, longitude = null) }
    }

    companion object {
        fun Factory(noteId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteMDApplication)
                NoteEditorViewModel(
                    repository = application.container.noteRepository,
                    tokenSessionManager = application.container.tokenSessionManager,
                    initialNoteId = noteId
                )
            }
        }
    }
}

private fun String.toTagList(): List<String> =
    split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
