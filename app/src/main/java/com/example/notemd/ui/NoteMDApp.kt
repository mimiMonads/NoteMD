package com.example.notemd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

enum class NoteMDSection(val labelRes: Int) {
    Main(R.string.section_overview),
    Note(R.string.section_note),
    Settings(R.string.section_settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteMDApp(
    modifier: Modifier = Modifier
) {
    var currentSection by rememberSaveable { mutableStateOf(NoteMDSection.Main) }

    val topBarTitle = when (currentSection) {
        NoteMDSection.Main -> stringResource(id = R.string.title_main_screen)
        NoteMDSection.Note -> stringResource(id = R.string.title_note_screen)
        NoteMDSection.Settings -> stringResource(id = R.string.title_settings_screen)
    }

    val appBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                scrollBehavior = appBarScrollBehavior
            )
        },
        bottomBar = {
            NoteMDBottomBar(
                currentSection = currentSection,
                onSectionSelected = { currentSection = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSection) {
                NoteMDSection.Main -> MainScreen()
                NoteMDSection.Note -> NoteScreen()
                NoteMDSection.Settings -> SettingsScreen()
            }
        }
    }
}

@Composable
private fun NoteMDBottomBar(
    currentSection: NoteMDSection,
    onSectionSelected: (NoteMDSection) -> Unit
) {
    NavigationBar {
        NoteMDSection.values().forEach { section ->
            NavigationBarItem(
                selected = currentSection == section,
                onClick = { onSectionSelected(section) },
                icon = {
                    SectionIconBadge(
                        label = stringResource(id = section.labelRes),
                        isSelected = currentSection == section
                    )
                },
                label = { Text(text = stringResource(id = section.labelRes)) }
            )
        }
    }
}

@Composable
private fun SectionIconBadge(
    label: String,
    isSelected: Boolean
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = label.first().uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

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

data class NotePreview(
    val id: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
    val lastUpdated: String
)

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
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_title_label)) }
        )

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
            onClick = { /* Save action placeholder */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() || content.isNotBlank()
        ) {
            Text(text = stringResource(id = R.string.note_save))
        }
    }
}

@Composable
fun SettingsScreen() {
    var darkThemeEnabled by rememberSaveable { mutableStateOf(false) }
    var syncEnabled by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SettingsToggleRow(
            title = stringResource(id = R.string.settings_dark_mode),
            subtitle = stringResource(id = R.string.settings_dark_mode_description),
            checked = darkThemeEnabled,
            onCheckedChange = { darkThemeEnabled = it }
        )

        SettingsToggleRow(
            title = stringResource(id = R.string.settings_sync),
            subtitle = stringResource(id = R.string.settings_sync_description),
            checked = syncEnabled,
            onCheckedChange = { syncEnabled = it }
        )

        Divider()

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.settings_about_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(id = R.string.settings_about_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

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

@Preview(showBackground = true)
@Composable
private fun PreviewNoteScreen() {
    NoteMDTheme {
        NoteScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsScreen() {
    NoteMDTheme {
        SettingsScreen()
    }
}
