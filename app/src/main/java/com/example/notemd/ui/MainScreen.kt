package com.example.notemd.ui

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
 * Lightweight feed of placeholder notes so the rest of the UI has something to breathe around.
 */
@Composable
fun MainScreen(
    notes: List<NotePreview> = placeholderNotes()
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
        items(notes) { note ->
            NotePreviewCard(notePreview = note)
        }
    }
}

/**
 * Minimal representation of a note card shown on the overview screen.
 */
data class NotePreview(
    val id: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
    val lastUpdated: String
)

/**
 * One-off card used inside the list; kept private since it only belongs here.
 */
@Composable
private fun NotePreviewCard(
    notePreview: NotePreview,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
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
private fun placeholderNotes(): List<NotePreview> = listOf(
    NotePreview(
        id = "1",
        title = "Weekly planning doc",
        summary = "Capture weekly priorities, tasks, and quick victories to share during stand-up.",
        tags = listOf("planning", "work"),
        lastUpdated = "2h ago"
    ),
    NotePreview(
        id = "2",
        title = "Product ideas - autumn release",
        summary = "Brainstormed ideas pulled from customer feedback sessions and roadmap workshops.",
        tags = listOf("ideas", "product"),
        lastUpdated = "yesterday"
    ),
    NotePreview(
        id = "3",
        title = "Book highlights: Deep Work",
        summary = "Collected quotes and reflections focused on building intentional focus time habits.",
        tags = listOf("reading", "personal"),
        lastUpdated = "Jun 12"
    )
)

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    NoteMDTheme {
        MainScreen()
    }
}
