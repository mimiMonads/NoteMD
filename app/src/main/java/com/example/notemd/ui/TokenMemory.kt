package com.example.notemd.ui

import androidx.compose.runtime.saveable.listSaver

/**
 * Shared token defaults + saver so we can remember custom combinations in memory.
 */
val DefaultTokenList = listOf(
    "orbit",
    "ember",
    "solstice",
    "lumen",
    "grove",
    "delta",
    "radial",
    "cinder",
    "kepler",
    "breeze",
    "cobalt",
    "zenith"
)

val TokenListSaver = listSaver<List<String>, String>(
    save = { it },
    restore = { it }
)
