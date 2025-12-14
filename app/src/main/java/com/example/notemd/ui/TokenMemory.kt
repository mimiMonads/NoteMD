package com.example.notemd.ui

import androidx.compose.runtime.saveable.listSaver

/**
 * Shared token defaults + saver so we can remember custom combinations in memory.
 */
import com.example.notemd.token.DEFAULT_TOKENS

val DefaultTokenList = DEFAULT_TOKENS

val TokenListSaver = listSaver<List<String>, String>(
    save = { it },
    restore = { it }
)
