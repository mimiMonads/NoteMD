package com.example.notemd.token

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TokenSessionState(
    val activeTokenHash: String? = null,
    val expiresAtMillis: Long = 0L
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean =
        activeTokenHash == null || now >= expiresAtMillis
}

/**
    Tracks which token is actively unlocked. Session expires after [sessionDurationMs] or when
    explicitly cleared (e.g., via shake gesture).
 */
class TokenSessionManager(
    private val sessionDurationMs: Long = 60 * 60 * 1000L // 1 hour
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _session = MutableStateFlow(
        TokenSessionState(
            activeTokenHash = TokenUtils.defaultTokenHash,
            expiresAtMillis = System.currentTimeMillis() + sessionDurationMs
        )
    )
    val session: StateFlow<TokenSessionState> = _session

    val activeTokenHash: StateFlow<String?> = _session
        .map { state ->
            if (state.isExpired()) {
                null
            } else {
                state.activeTokenHash
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, _session.value.activeTokenHash)

    private var expiryJob: Job? = null

    fun unlock(tokens: List<String>) {
        val hash = TokenUtils.hashTokens(tokens)
        if (hash.isEmpty()) return
        val expiresAt = System.currentTimeMillis() + sessionDurationMs
        _session.value = TokenSessionState(hash, expiresAt)
        scheduleExpiry(expiresAt)
    }

    fun clear() {
        _session.value = TokenSessionState(activeTokenHash = null, expiresAtMillis = 0L)
        expiryJob?.cancel()
    }

    fun getActiveOrDefault(): String =
        activeTokenHash.value ?: TokenUtils.defaultTokenHash

    private fun scheduleExpiry(expiresAtMillis: Long) {
        expiryJob?.cancel()
        expiryJob = scope.launch {
            val delayFor = expiresAtMillis - System.currentTimeMillis()
            if (delayFor > 0) {
                delay(delayFor)
            }
            clear()
        }
    }
}
