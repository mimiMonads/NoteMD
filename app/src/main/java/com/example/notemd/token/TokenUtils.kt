package com.example.notemd.token

import java.security.MessageDigest
import java.util.Locale

/**
 * Helpers for normalizing token lists and deriving repeatable hashes.
 */
object TokenUtils {
    fun normalizeTokens(tokens: List<String>): List<String> =
        tokens.map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.lowercase(Locale.getDefault()) }
            .sorted()

    fun hashTokens(tokens: List<String>): String {
        val normalized = normalizeTokens(tokens)
        if (normalized.isEmpty()) return ""
        val joined = normalized.joinToString(" ")
        return sha1(joined)
    }

    val defaultTokenHash: String = hashTokens(DEFAULT_TOKENS)

    fun stringToTokens(input: String): List<String> =
        input.split(Regex("[,\\s]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        return digest.digest(input.toByteArray())
            .joinToString(separator = "") { "%02x".format(it) }
    }
}

// Mirrors the default list used by the token playground so it always forms a valid key for debugging.
val DEFAULT_TOKENS = listOf(
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
