package com.example.notemd.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notemd.R
import com.example.notemd.ui.theme.NoteMDTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Text-first screen for drafting a note; state stays local so previews remain predictable.
 */
@Composable
fun NoteScreen(
    noteId: Long?,
    editorSession: Int,
    allowLocation: Boolean,
    onSaved: () -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val editorViewModel: NoteEditorViewModel = viewModel(
        key = "noteEditor-${noteId ?: "new"}-$editorSession",
        factory = NoteEditorViewModel.Factory(noteId)
    )
    val uiState by editorViewModel.uiState.collectAsStateWithLifecycle()
    var locationStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var isRequestingLocation by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                captureLocation(
                    fusedLocationClient = fusedLocationClient,
                    onSuccess = { lat, lng ->
                        editorViewModel.setLocation(lat, lng)
                        locationStatus = context.getString(R.string.note_location_value, lat, lng)
                    },
                    onFailure = {
                        locationStatus = context.getString(R.string.note_location_unavailable)
                    },
                    onComplete = { isRequestingLocation = false }
                )
            }
        } else {
            locationStatus = context.getString(R.string.note_location_permission_needed)
            isRequestingLocation = false
        }
    }

    NoteEditorContent(
        uiState = uiState,
        onTitleChange = editorViewModel::onTitleChange,
        onContentChange = editorViewModel::onContentChange,
        onTagsChange = editorViewModel::onTagsChange,
        onSave = { editorViewModel.save(onSaved) },
        onDelete = { editorViewModel.delete(onDeleted) },
        onAttachLocation = {
            if (!allowLocation) return@NoteEditorContent
            locationStatus = context.getString(R.string.note_location_fetching)
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            isRequestingLocation = true
            if (hasPermission) {
                scope.launch {
                    captureLocation(
                        fusedLocationClient = fusedLocationClient,
                        onSuccess = { lat, lng ->
                            editorViewModel.setLocation(lat, lng)
                            locationStatus = context.getString(R.string.note_location_value, lat, lng)
                        },
                        onFailure = {
                            locationStatus = context.getString(R.string.note_location_unavailable)
                        },
                        onComplete = { isRequestingLocation = false }
                    )
                }
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        },
        onRemoveLocation = {
            if (!allowLocation) return@NoteEditorContent
            editorViewModel.clearLocation()
            locationStatus = null
            isRequestingLocation = false
        },
        locationStatus = locationStatus,
        isRequestingLocation = isRequestingLocation,
        allowLocation = allowLocation
    )
}

@Composable
private fun NoteEditorContent(
    uiState: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onAttachLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
    locationStatus: String?,
    isRequestingLocation: Boolean,
    allowLocation: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
        * Tittle
        * */
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_title_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_title_placeholder)) }
        )

        OutlinedTextField(
            value = uiState.tagsInput,
            onValueChange = onTagsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(id = R.string.note_tags_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_tags_placeholder)) }
        )

        if (allowLocation) {
            LocationControls(
                hasLocation = uiState.hasLocation,
                locationStatus = locationStatus,
                isRequestingLocation = isRequestingLocation,
                onAttachLocation = onAttachLocation,
                onRemoveLocation = onRemoveLocation,
                latitude = uiState.latitude,
                longitude = uiState.longitude
            )
        }

        OutlinedTextField(
            value = uiState.content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text(text = stringResource(id = R.string.note_content_label)) },
            placeholder = { Text(text = stringResource(id = R.string.note_content_placeholder)) }
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSave && !uiState.isSaving
        ) {
            Text(
                text = stringResource(
                    id = if (uiState.isEditing) R.string.note_update else R.string.note_save
                )
            )
        }

        if (uiState.isEditing) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isDeleting
            ) {
                Text(text = stringResource(id = R.string.note_delete))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoteScreen() {
    NoteMDTheme {
        NoteEditorContent(
            uiState = NoteEditorUiState(
                noteId = 1L,
                title = "Weekly planning doc",
                content = "Capture weekly priorities, tasks...",
                tagsInput = "planning, work"
            ),
            onTitleChange = {},
            onContentChange = {},
            onTagsChange = {},
            onSave = {},
            onDelete = {},
            onAttachLocation = {},
            onRemoveLocation = {},
            locationStatus = null,
            isRequestingLocation = false,
            allowLocation = true
        )
    }
}

@Composable
private fun LocationControls(
    hasLocation: Boolean,
    locationStatus: String?,
    isRequestingLocation: Boolean,
    onAttachLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
    latitude: Double?,
    longitude: Double?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(id = R.string.note_location_add))
        RowButtons(
            hasLocation = hasLocation,
            isRequestingLocation = isRequestingLocation,
            onAttachLocation = onAttachLocation,
            onRemoveLocation = onRemoveLocation
        )
        val statusText = when {
            locationStatus != null -> locationStatus
            hasLocation && latitude != null && longitude != null -> stringResource(
                id = R.string.note_location_value,
                latitude,
                longitude
            )
            else -> null
        }
        if (statusText != null) {
            Text(text = statusText)
        }
        Divider()
    }
}

@Composable
private fun RowButtons(
    hasLocation: Boolean,
    isRequestingLocation: Boolean,
    onAttachLocation: () -> Unit,
    onRemoveLocation: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onAttachLocation,
            modifier = Modifier.weight(1f),
            enabled = !isRequestingLocation
        ) {
            Text(
                text = if (isRequestingLocation) {
                    stringResource(id = R.string.note_location_fetching)
                } else {
                    stringResource(id = R.string.note_location_add)
                }
            )
        }
        OutlinedButton(
            onClick = onRemoveLocation,
            modifier = Modifier.weight(1f),
            enabled = hasLocation && !isRequestingLocation
        ) {
            Text(text = stringResource(id = R.string.note_location_remove))
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun captureLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailure: () -> Unit,
    onComplete: () -> Unit
) {
    val defaultLat = 53.3498
    val defaultLng = -6.2603
    try {
        // Request a fresh location; fall back to the cached last location if Play services
        // cannot deliver a current fix (common on emulators).
        val currentLocation = suspendCancellableCoroutine<Location?> { continuation ->
            val tokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                tokenSource.token
            )
                .addOnSuccessListener { continuation.resume(it) {} }
                .addOnFailureListener { continuation.resume(null) {} }

            continuation.invokeOnCancellation { tokenSource.cancel() }
        }

        val location = currentLocation ?: suspendCancellableCoroutine<Location?> { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) {} }
                .addOnFailureListener { continuation.resume(null) {} }
        }

        if (location?.latitude != null && location.longitude != null) {
            onSuccess(location.latitude, location.longitude)
        } else {
            // Use Dublin as a sane default if nothing is available.
            onSuccess(defaultLat, defaultLng)
        }
    } finally {
        onComplete()
    }
}
