package com.example.notemd.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Placeholder notes so the rest of the UI has something to breathe around.
 */
@Composable
fun MainScreen(
    notes: List<NotePreview> = sampleNotes(),
    onNoteSelected: (NotePreview) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.main_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (notes.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.note_empty_state),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(notes) { note ->
                NotePreviewCard(
                    notePreview = note,
                    onClick = { onNoteSelected(note) }
                )
            }
        }
    }
}

/**
 * Lightweight note details for the overview list.
 */
data class NotePreview(
    val id: Long,
    val title: String,
    val summary: String,
    val body: String,
    val tags: List<String>,
    val lastUpdated: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/**
 * Card shown inside the list; kept private since it only belongs here.
 */
@Composable
private fun NotePreviewCard(
    notePreview: NotePreview,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notePreview.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notePreview.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notePreview.tags.forEach { tag ->
                    // The chips double as gentle affordances for future filtering work.
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.main_last_updated, notePreview.lastUpdated),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Hard-coded samples so the layout still feels alive before wiring real data.
internal fun sampleNotes(): List<NotePreview> = listOf(
     // never used
)

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    NoteMDTheme {
        MainScreen()
    }
}
