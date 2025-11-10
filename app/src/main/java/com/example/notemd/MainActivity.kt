package com.example.notemd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.notemd.ui.NoteMDApp
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Host for the Compose application.
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteMDTheme {
                // Keep the real app entry the same as the previews for consistency.
                NoteMDApp()
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
        NoteMDApp(modifier = Modifier)
    }
}
