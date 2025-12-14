package com.example.notemd.data.local

import com.example.notemd.data.Note
import com.example.notemd.token.TokenUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteCryptoTest {

    private val tokenHash = TokenUtils.hashTokens(listOf("alpha", "beta", "gamma"))

    @Test
    fun `encrypt and decrypt round trip`() {
        val note = Note(
            id = 42L,
            title = "Secret title",
            content = "Top secret content",
            tags = listOf("tag1", "tag2"),
            latitude = 10.5,
            longitude = -20.25,
            tokenHash = tokenHash,
            lastUpdated = 1234L
        )

        val encrypted = NoteCrypto.encrypt(note)
        val decrypted = NoteCrypto.decryptToNote(encrypted, tokenHash)

        assertNotNull(decrypted)
        assertEquals(note.copy(tokenHash = tokenHash), decrypted)
    }

    @Test
    fun `decrypt with wrong token fails`() {
        val note = Note(id = 1L, title = "a", content = "b", tokenHash = tokenHash)
        val encrypted = NoteCrypto.encrypt(note)

        val wrongHash = TokenUtils.hashTokens(listOf("wrong"))
        val decrypted = NoteCrypto.decryptToNote(encrypted, wrongHash)

        assertNull(decrypted)
    }

    @Test
    fun `hashed file name changes with token`() {
        val note = Note(id = 99L, title = "a", content = "b", tokenHash = tokenHash)
        val fileName = NoteCrypto.hashedFileName(note)
        val otherFileName = NoteCrypto.hashedFileName(99L, TokenUtils.hashTokens(listOf("other")))

        assertNotEquals(fileName, otherFileName)
    }
}
