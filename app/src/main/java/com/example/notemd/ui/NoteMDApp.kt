package com.example.notemd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.notemd.token.TokenSessionManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NoteMDSection(val labelRes: Int) {
    Main(R.string.section_overview),
    Note(R.string.section_note),
    Tokens(R.string.section_tokens),
    Settings(R.string.section_settings)
}

private enum class NoteMDNavigationType {
    BottomBar,
    NavigationRail
}

/**
 * Navbar
 * keeps track of the current section and wires up
 * the pieces of UI we stitch together elsewhere.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteMDApp(
    modifier: Modifier = Modifier,
    previewUiState: NoteListUiState? = null,
    settingsUiState: SettingsUiState = SettingsUiState(),
    onDarkModeToggle: (Boolean) -> Unit = {},
    onLocationToggle: (Boolean) -> Unit = {},
    onShakeResetToggle: (Boolean) -> Unit = {},
    tokenSessionManager: TokenSessionManager = TokenSessionManager(),
    windowSizeClass: WindowSizeClass? = null
) {
    var currentSection by rememberSaveable { mutableStateOf(NoteMDSection.Main) }
    var noteToEditId by rememberSaveable { mutableStateOf<Long?>(null) }
    var noteEditorSession by rememberSaveable { mutableStateOf(0) }
    var tokenList by remember { mutableStateOf(DefaultTokenList) }
    val activeTokenHash by tokenSessionManager.activeTokenHash
        .collectAsStateWithLifecycle(initialValue = tokenSessionManager.activeTokenHash.value)

    val notesViewModel: NotesViewModel? = if (previewUiState == null) {
        viewModel(factory = NotesViewModel.Factory)
    } else {
        null
    }
    val noteListUiState by if (previewUiState == null) {
        notesViewModel!!.uiState.collectAsStateWithLifecycle()
    } else {
        remember(previewUiState) { mutableStateOf(previewUiState) }
    }

    val topBarTitle = when (currentSection) {
        NoteMDSection.Main -> stringResource(id = R.string.title_main_screen)
        NoteMDSection.Note -> stringResource(id = R.string.title_note_screen)
        NoteMDSection.Tokens -> stringResource(id = R.string.title_tokens_screen)
        NoteMDSection.Settings -> stringResource(id = R.string.title_settings_screen)
    }

    val appBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val navigationType = when (windowSizeClass?.widthSizeClass) {
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> NoteMDNavigationType.NavigationRail
        else -> NoteMDNavigationType.BottomBar
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(topBarTitle)
                        val tokenHashForBadge = activeTokenHash
                            ?: tokenSessionManager.getActiveOrDefault()
                        tokenHashForBadge.takeIf { it.isNotEmpty() }?.let { hash ->
                            TokenBadge(hash = hash)
                        }
                    }
                },
                scrollBehavior = appBarScrollBehavior
            )
        },
        floatingActionButton = {
            if (currentSection != NoteMDSection.Note) {
                // Keep the FAB around as a gentle nudge towards note taking.
                ExtendedFloatingActionButton(
                    onClick = {
                        noteToEditId = null
                        noteEditorSession++
                        currentSection = NoteMDSection.Note
                    },
                ) {
                    Text(text = stringResource(id = R.string.title_note_screen))
                }
            }
        },
        bottomBar = {
            if (navigationType == NoteMDNavigationType.BottomBar) {
                NoteMDBottomBar(
                    currentSection = currentSection,
                    onSectionSelected = { currentSection = it }
                )
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (navigationType == NoteMDNavigationType.NavigationRail) {
                NoteMDNavigationRail(
                    currentSection = currentSection,
                    onSectionSelected = { currentSection = it }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentSection) {
                    NoteMDSection.Main -> MainScreen(
                        notes = noteListUiState.notes,
                        onNoteSelected = { note ->
                            noteToEditId = note.id
                            noteEditorSession++
                            currentSection = NoteMDSection.Note
                        }
                    )
                    NoteMDSection.Note -> NoteScreen(
                        noteId = noteToEditId,
                        editorSession = noteEditorSession,
                        // Location feature temporarily disabled.
                        allowLocation = false,
                        onSaved = {
                            noteToEditId = null
                            currentSection = NoteMDSection.Main
                        },
                        onDeleted = {
                            noteToEditId = null
                            currentSection = NoteMDSection.Main
                        }
                    )
                    NoteMDSection.Tokens -> TokenPracticeScreen(
                        tokens = tokenList,
                        onUnlockWithTokens = { tokens ->
                            tokenSessionManager.unlock(tokens)
                        }
                    )
                    NoteMDSection.Settings -> SettingsScreen(
                        darkThemeEnabled = settingsUiState.darkThemeEnabled,
                        onDarkThemeChanged = onDarkModeToggle,
                        // Location feature temporarily disabled.
                        locationAllowed = false,
                        onLocationToggle = {},
                        locationToggleEnabled = false,
                        shakeResetEnabled = settingsUiState.shakeResetEnabled,
                        onShakeResetToggle = onShakeResetToggle,
                        onSimulateShake = {
                            if (settingsUiState.shakeResetEnabled) {
                                tokenSessionManager.clear()
                            }
                        },
                        onResetTokens = {
                            tokenList = DefaultTokenList
                            tokenSessionManager.unlock(DefaultTokenList)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Shared bottom navigation that mirrors the enum values we expose to the scaffold.
 */
@Composable
private fun NoteMDBottomBar(
    currentSection: NoteMDSection,
    onSectionSelected: (NoteMDSection) -> Unit
) {
    // The note screen is accessible via FAB, so we skip it in the bottom bar.
    NavigationBar {
        NoteMDSection.values()
            .filter { it != NoteMDSection.Note }
            .forEach { section ->
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
private fun TokenBadge(hash: String) {
    val suffix = hash.takeLast(5).uppercase()
    val (background, content) = remember(hash) { tokenBadgeColors(hash) }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = background,
        contentColor = content,
        tonalElevation = 0.dp
    ) {
        Text(
            text = "Key $suffix",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun tokenBadgeColors(hash: String): Pair<Color, Color> {
    val slice = hash.takeLast(6)
    val numeric = slice.toLongOrNull(16) ?: 0L
    val hue = (numeric % 360).toFloat()
    val bg = Color.hsv(
        hue = hue,
        saturation = 0.35f,
        value = 0.92f
    )
    val content = if (bg.luminance() > 0.6f) Color.Black else Color.White
    return bg to content
}

@Composable
private fun NoteMDNavigationRail(
    currentSection: NoteMDSection,
    onSectionSelected: (NoteMDSection) -> Unit
) {
    NavigationRail {
        NoteMDSection.values()
            .filter { it != NoteMDSection.Note }
            .forEach { section ->
                NavigationRailItem(
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

/**
 * Minimal badge that uses the first letter of the section instead of icons.
 */
@Composable
private fun SectionIconBadge(
    label: String,
    isSelected: Boolean
) {
    // Using the first character keeps the navigation bar tidy on small screens.
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = CircleShape,
        color = bg,
        contentColor = fg,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            val letter = label.firstOrNull()?.uppercase() ?: ""
            Text(
                text = letter,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteMDAppPreview() {
    NoteMDTheme {
        NoteMDApp(
            previewUiState = NoteListUiState(
                notes = sampleNotes()
            )
        )
    }
}
