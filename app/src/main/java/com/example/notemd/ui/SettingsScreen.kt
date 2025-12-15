package com.example.notemd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme

/**
 * Simple settings surface.
 * TODO: add some real info
 */
@Composable
fun SettingsScreen(
    darkThemeEnabled: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit,
    shakeResetEnabled: Boolean,
    onShakeResetToggle: (Boolean) -> Unit,
    onSimulateShake: () -> Unit,
    onResetTokens: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SettingsToggleRow(
            title = stringResource(id = R.string.settings_dark_mode),
            subtitle = stringResource(id = R.string.settings_dark_mode_description),
            checked = darkThemeEnabled,
            onCheckedChange = onDarkThemeChanged
        )

        SettingsToggleRow(
            title = stringResource(id = R.string.settings_shake_reset_title),
            subtitle = stringResource(id = R.string.settings_shake_reset_description),
            checked = shakeResetEnabled,
            onCheckedChange = onShakeResetToggle
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSimulateShake,
                modifier = Modifier.fillMaxWidth(),
                enabled = shakeResetEnabled
            ) {
                Text(text = stringResource(id = R.string.settings_debug_simulate_shake))
            }
            Text(
                text = stringResource(id = R.string.settings_debug_simulate_shake_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onResetTokens,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.settings_reset_tokens))
        }

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

/**
 * Reusable row for any toggle-able setting with a title + supporting copy.
 */
@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    // Shared layout for switches so future additions stay consistent.
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSettingsScreen() {
    NoteMDTheme {
        SettingsScreen(
            darkThemeEnabled = true,
            onDarkThemeChanged = {},
            shakeResetEnabled = true,
            onShakeResetToggle = {},
            onSimulateShake = {},
            onResetTokens = {}
        )
    }
}
