package com.example.notemd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notemd.ui.NoteMDApp
import com.example.notemd.ui.SettingsUiState
import com.example.notemd.ui.SettingsViewModel
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Host for the Compose application.
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            NoteMDTheme(useDarkTheme = settingsUiState.darkThemeEnabled) {
                // Keep the real app entry the same as the previews for consistency.
                NoteMDApp(
                    settingsUiState = settingsUiState,
                    onDarkModeToggle = settingsViewModel::setDarkThemeEnabled
                )
            }
        }
    }
}

/**
 * Sanity check scaffolding changes quickly.
 */
@Preview(showBackground = true)
@Composable
fun PreviewMainInterface() {
    NoteMDTheme {
        NoteMDApp(
            modifier = Modifier,
            settingsUiState = SettingsUiState()
        )
    }
}
