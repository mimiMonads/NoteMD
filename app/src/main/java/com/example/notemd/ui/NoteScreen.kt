package com.example.notemd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Text-first screen for drafting a note; state stays local so previews remain predictable.
 */
@Composable
fun NoteScreen(
    noteId: Long?,
    onSaved: () -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val editorViewModel: NoteEditorViewModel = viewModel(
        key = "noteEditor-${noteId ?: "new"}",
        factory = NoteEditorViewModel.Factory(noteId)
    )
    val uiState by editorViewModel.uiState.collectAsStateWithLifecycle()

    NoteEditorContent(
        uiState = uiState,
        onTitleChange = editorViewModel::onTitleChange,
        onContentChange = editorViewModel::onContentChange,
        onTagsChange = editorViewModel::onTagsChange,
        onSave = { editorViewModel.save(onSaved) },
        onDelete = { editorViewModel.delete(onDeleted) }
    )
}

@Composable
private fun NoteEditorContent(
    uiState: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
        * Tittle
        * */
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_title_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_title_placeholder)) }
        )

        OutlinedTextField(
            value = uiState.tagsInput,
            onValueChange = onTagsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_tags_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_tags_placeholder)) }
        )

        OutlinedTextField(
            value = uiState.content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text(text = stringResource(id = R.string.note_content_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_content_placeholder)) }
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSave && !uiState.isSaving
        ) {
            Text(
                text = stringResource(
                    id = if (uiState.isEditing) R.string.note_update else R.string.note_save
                )
            )
        }

        if (uiState.isEditing) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isDeleting
            ) {
                Text(text = stringResource(id = R.string.note_delete))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoteScreen() {
    NoteMDTheme {
        NoteEditorContent(
            uiState = NoteEditorUiState(
                noteId = 1L,
                title = "Weekly planning doc",
                content = "Capture weekly priorities, tasks...",
                tagsInput = "planning, work"
            ),
            onTitleChange = {},
            onContentChange = {},
            onTagsChange = {},
            onSave = {},
            onDelete = {}
        )
    }
}
