package com.example.notemd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Text-first screen for drafting a note; state stays local so previews remain predictable.
 */
@Composable
fun NoteScreen() {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

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
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_title_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_title_placeholder)) }
        )

        /**
         * Tags
         * */



        /**
         * Tittle
         * */
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text(text = stringResource(id = R.string.note_content_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_content_placeholder)) }
        )

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            // Let people tap save even with only a title or content; it's less fussy that way.
            enabled = title.isNotBlank() || content.isNotBlank()
        ) {
            Text(text = stringResource(id = R.string.note_save))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoteScreen() {
    NoteMDTheme {
        NoteScreen()
    }
}
