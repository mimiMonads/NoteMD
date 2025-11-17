package com.example.notemd.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

class NoteDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val note = NoteDetailArgs.fromIntent(intent)

        setContent {
            NoteMDTheme {
                NoteDetailScreen(
                    note = note,
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_NOTE_ID = "extra_note_id"
        private const val EXTRA_NOTE_TITLE = "extra_note_title"
        private const val EXTRA_NOTE_SUMMARY = "extra_note_summary"
        private const val EXTRA_NOTE_BODY = "extra_note_body"
        private const val EXTRA_NOTE_LAST_UPDATED = "extra_note_last_updated"
        private const val EXTRA_NOTE_TAGS = "extra_note_tags"

        fun createIntent(context: Context, note: NotePreview): Intent =
            Intent(context, NoteDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, note.id)
                putExtra(EXTRA_NOTE_TITLE, note.title)
                putExtra(EXTRA_NOTE_SUMMARY, note.summary)
                putExtra(EXTRA_NOTE_BODY, note.body)
                putExtra(EXTRA_NOTE_LAST_UPDATED, note.lastUpdated)
                putStringArrayListExtra(EXTRA_NOTE_TAGS, ArrayList(note.tags))
            }

        private fun NoteDetailArgs.Companion.fromIntent(intent: Intent?): NoteDetailArgs =
            NoteDetailArgs(
                id = intent?.getLongExtra(EXTRA_NOTE_ID, 0L) ?: 0L,
                title = intent?.getStringExtra(EXTRA_NOTE_TITLE).orEmpty(),
                summary = intent?.getStringExtra(EXTRA_NOTE_SUMMARY).orEmpty(),
                body = intent?.getStringExtra(EXTRA_NOTE_BODY).orEmpty(),
                lastUpdated = intent?.getStringExtra(EXTRA_NOTE_LAST_UPDATED).orEmpty(),
                tags = intent?.getStringArrayListExtra(EXTRA_NOTE_TAGS)?.toList().orEmpty()
            )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    note: NoteDetailArgs,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (note.title.isNotBlank()) note.title else stringResource(id = R.string.note_detail_title_fallback),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.note_detail_navigate_home)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = note.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (note.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    note.tags.forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(text = tag) })
                    }
                }
            }

            if (note.lastUpdated.isNotBlank()) {
                Text(
                    text = stringResource(id = R.string.main_last_updated, note.lastUpdated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = note.body,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

data class NoteDetailArgs(
    val id: Long = 0L,
    val title: String = "",
    val summary: String = "",
    val body: String = "",
    val lastUpdated: String = "",
    val tags: List<String> = emptyList()
) {
    companion object
}
