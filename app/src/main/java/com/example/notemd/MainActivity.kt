package com.example.notemd

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.notemd.ui.NoteMDApp
import com.example.notemd.ui.SettingsUiState
import com.example.notemd.ui.SettingsViewModel
import com.example.notemd.ui.theme.NoteMDTheme
import kotlin.math.sqrt

/**
 * Host for the Compose application.
 *
 */
class MainActivity : ComponentActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    @Volatile private var shakeResetEnabled: Boolean = true

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val tokenSessionManager = (application as NoteMDApplication).container.tokenSessionManager
            LaunchedEffect(settingsUiState.shakeResetEnabled) {
                shakeResetEnabled = settingsUiState.shakeResetEnabled
            }

            NoteMDTheme(useDarkTheme = settingsUiState.darkThemeEnabled) {
                // Keep the real app entry the same as the previews for consistency.
                NoteMDApp(
                    windowSizeClass = windowSizeClass,
                    settingsUiState = settingsUiState,
                    onDarkModeToggle = settingsViewModel::setDarkThemeEnabled,
                    onShakeResetToggle = settingsViewModel::setShakeResetEnabled,
                    tokenSessionManager = tokenSessionManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { sensor ->
            sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gForce = sqrt((x * x + y * y + z * z).toDouble()) / SensorManager.GRAVITY_EARTH
        if (shakeResetEnabled && gForce > 2.7f) {
            (application as NoteMDApplication).container.tokenSessionManager.clear()
        }
    }
}

// Preview helper to spot UI tweaks without wiring up data flows.
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
