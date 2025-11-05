package com.example.notemd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.notemd.ui.NoteMDApp
import com.example.notemd.ui.theme.NoteMDTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteMDTheme {
                NoteMDApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainInterface() {
    NoteMDTheme {
        NoteMDApp(modifier = Modifier)
    }
}
