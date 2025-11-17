package com.example.notemd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notemd.NoteMDApplication
import com.example.notemd.data.Note
import com.example.notemd.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false
) {
    val isEditing: Boolean get() = noteId != null
    val canSave: Boolean get() = title.isNotBlank() || content.isNotBlank()
}

class NoteEditorViewModel(
    private val repository: NoteRepository,
    private val initialNoteId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NoteEditorUiState(noteId = initialNoteId)
    )
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        initialNoteId?.let { id ->
            viewModelScope.launch {
                repository.getNote(id)?.let { note ->
                    _uiState.update {
                        it.copy(
                            noteId = note.id,
                            title = note.title,
                            content = note.content
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

    fun save(onSaved: () -> Unit) {
        val current = _uiState.value
        if (!current.canSave || current.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val note = Note(
                id = current.noteId ?: 0L,
                title = current.title.trim(),
                content = current.content.trim(),
                tags = emptyList()
            )
            val newId = repository.upsertNote(note)
            _uiState.update {
                it.copy(
                    noteId = it.noteId ?: newId,
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
            repository.deleteNote(id)
            _uiState.value = NoteEditorUiState()
            onDeleted()
        }
    }

    companion object {
        fun Factory(noteId: Long?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NoteMDApplication)
                NoteEditorViewModel(
                    repository = application.container.noteRepository,
                    initialNoteId = noteId
                )
            }
        }
    }
}
