package com.example.notemd.data.local

import android.util.Base64
import com.example.notemd.data.Note
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small helper to encrypt/decrypt note payloads using the provided token hash as the key.
 * AES-GCM with a random IV per payload; IV is prefixed to the ciphertext.
 */
object NoteCrypto {
    fun encrypt(note: Note): String {
        val json = JSONObject()
        json.put("id", note.id)
        json.put("title", note.title)
        json.put("content", note.content)
        json.put("tags", JSONArray(note.tags))
        note.latitude?.let { json.put("latitude", it) }
        note.longitude?.let { json.put("longitude", it) }
        json.put("lastUpdated", note.lastUpdated)
        val plain = json.toString()
        val key = secretKey(note.tokenHash)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val payload = iv + encrypted
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun decryptToNote(payload: String, tokenHash: String): Note? {
        val key = secretKey(tokenHash)
        val bytes = runCatching { Base64.decode(payload, Base64.NO_WRAP) }.getOrNull() ?: return null
        if (bytes.size < 13) return null
        val iv = bytes.copyOfRange(0, 12)
        val cipherText = bytes.copyOfRange(12, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        return runCatching {
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val plainBytes = cipher.doFinal(cipherText)
            val json = JSONObject(String(plainBytes, Charsets.UTF_8))
            Note(
                id = json.optLong("id"),
                title = json.optString("title"),
                content = json.optString("content"),
                tags = json.optJSONArray("tags")?.toStringList().orEmpty(),
                latitude = json.optDoubleOrNull("latitude"),
                longitude = json.optDoubleOrNull("longitude"),
                tokenHash = tokenHash,
                lastUpdated = json.optLong("lastUpdated")
            )
        }.getOrNull()
    }

    fun hashedFileName(note: Note): String = hashedFileName(note.id, note.tokenHash)

    fun hashedFileName(id: Long, tokenHash: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "${tokenHash}_$id".toByteArray()
        val hash = digest.digest(input).joinToString("") { "%02x".format(it) }
        return "$hash.note"
    }

    private fun secretKey(tokenHash: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(tokenHash.toByteArray())
        return SecretKeySpec(keyBytes, "AES")
    }
}

private fun JSONObject.optDoubleOrNull(name: String): Double? =
    if (has(name)) optDouble(name) else null

private fun JSONArray.toStringList(): List<String> =
    (0 until length()).mapNotNull { idx -> optString(idx) }
