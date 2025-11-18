package com.example.notemd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.notemd.R

private val SeedWordSaver = listSaver<List<String>, String>(
    save = { it },
    restore = { it }
)

/**
 * TODO:
 * Small drag-and-drop playground.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TokenPracticeScreen(
    modifier: Modifier = Modifier
) {

    // TODO: import a list and divide it
    val defaultTokens = remember {
        listOf("orbit", "ember", "solstice", "lumen", "grove", "delta", "radial", "cinder", "kepler", "breeze", "cobalt", "zenith")
    }
    val tokenOrder = remember { defaultTokens.withIndex().associate { it.value to it.index } }

    var trayTokens by rememberSaveable(stateSaver = SeedWordSaver) { mutableStateOf(defaultTokens) }
    var droppedTokens by rememberSaveable(stateSaver = SeedWordSaver) { mutableStateOf(emptyList<String>()) }
    var dropBounds by remember { mutableStateOf<Rect?>(null) }
    var trayBounds by remember { mutableStateOf<Rect?>(null) }
    var dropActive by remember { mutableStateOf(false) }
    var trayActive by remember { mutableStateOf(false) }

    // Helper to keep tokens sorted even after we stuff them back into the tray.
    fun List<String>.sortedByOriginalOrder(): List<String> =
        sortedBy { tokenOrder[it] ?: Int.MAX_VALUE }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(id = R.string.tokens_instructions),
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords -> trayBounds = coords.boundsInRoot() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            border = if (trayActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.tokens_tray_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (trayTokens.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.tokens_empty_tray),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        trayTokens.forEach { token ->
                            DraggableRecoveryToken(
                                token = token,
                                dropBounds = dropBounds,
                                onDropped = { dropped ->
                                    if (dropped !in droppedTokens) {
                                        droppedTokens = droppedTokens + dropped
                                        trayTokens = trayTokens.filterNot { it == dropped }
                                    }
                                },
                                onDragOverDropZone = { dropActive = it }
                            )
                        }
                    }
                }
            }
        }

        TokenDropZone(
            tokens = droppedTokens,
            isActive = dropActive,
            trayBounds = trayBounds,
            onBoundsReady = { dropBounds = it },
            onTokenRemoved = { token ->
                droppedTokens = droppedTokens.filterNot { it == token }
                trayTokens = (trayTokens + token).sortedByOriginalOrder()
            },
            onTrayHoverChange = { trayActive = it }
        )

        if (droppedTokens.isNotEmpty() || trayTokens.size != defaultTokens.size) {
            OutlinedButton(
                onClick = {
                    trayTokens = defaultTokens
                    droppedTokens = emptyList()
                }
            ) {
                Text(text = stringResource(id = R.string.tokens_reset))
            }
        }
    }
}

/**
 * Visual drop target that reports its bounds so drag gestures know when to hand off tokens.
 * Drop off
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TokenDropZone(
    tokens: List<String>,
    isActive: Boolean,
    trayBounds: Rect?,
    onBoundsReady: (Rect) -> Unit,
    onTokenRemoved: (String) -> Unit,
    onTrayHoverChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp)
            .onGloballyPositioned { coords -> onBoundsReady(coords.boundsInRoot()) },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (tokens.isEmpty()) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.tokens_drop_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (tokens.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.tokens_drop_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tokens.forEach { token ->
                        DraggableDroppedToken(
                            token = token,
                            trayBounds = trayBounds,
                            onDropped = { onTokenRemoved(token) },
                            onDragOverTray = onTrayHoverChange,
                            onClick = { onTokenRemoved(token) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual chip that tracks its own drag offset so the gesture feels immediate.
 */
@Composable
private fun DraggableRecoveryToken(
    token: String,
    dropBounds: Rect?,
    onDropped: (String) -> Unit,
    onDragOverDropZone: (Boolean) -> Unit
) {
    DraggableTokenChip(
        targetBounds = dropBounds,
        onDropped = { onDropped(token) },
        onDragOverTarget = onDragOverDropZone
    ) { dragModifier ->
        Surface(
            modifier = dragModifier,
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        )
                )
                Text(
                    text = token,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun DraggableDroppedToken(
    token: String,
    trayBounds: Rect?,
    onDropped: () -> Unit,
    onDragOverTray: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    DraggableTokenChip(
        targetBounds = trayBounds,
        onDropped = onDropped,
        onDragOverTarget = onDragOverTray
    ) { dragModifier ->
        AssistChip(
            modifier = dragModifier,
            onClick = onClick,
            label = { Text(text = token) }
        )
    }
}

/**
 * Shared chip wrapper that handles drag gestures and translation for both trays.
 */
@Composable
private fun DraggableTokenChip(
    modifier: Modifier = Modifier,
    targetBounds: Rect?,
    onDropped: () -> Unit,
    onDragOverTarget: (Boolean) -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    var chipCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    val dragModifier = modifier
        .zIndex(if (dragOffset == Offset.Zero) 0f else 1f)
        .onGloballyPositioned { chipCoords = it }
        .graphicsLayer {
            translationX = dragOffset.x
            translationY = dragOffset.y
        }
        .pointerInput(targetBounds) {
            detectDragGestures(
                onDragStart = {
                    onDragOverTarget(false)
                },
                onDragEnd = {
                    val finalCenter = chipCoords?.boundsInRoot()?.center?.plus(dragOffset)
                    val hitTarget = finalCenter?.let { center ->
                        targetBounds?.contains(center)
                    } == true

                    if (hitTarget) {
                        onDropped()
                    }
                    dragOffset = Offset.Zero
                    onDragOverTarget(false)
                },
                onDragCancel = {
                    dragOffset = Offset.Zero
                    onDragOverTarget(false)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragOffset += dragAmount
                    val currentCenter = chipCoords?.boundsInRoot()?.center?.plus(dragOffset)
                    val overTarget = currentCenter?.let { targetBounds?.contains(it) } == true
                    onDragOverTarget(overTarget)
                }
            )
        }

    content(dragModifier)
}
