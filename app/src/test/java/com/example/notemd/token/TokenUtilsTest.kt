package com.example.notemd.token

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenUtilsTest {

    @Test
    fun `default token hash is stable and non-empty`() {
        assertTrue(TokenUtils.defaultTokenHash.isNotEmpty())
        assertEquals(
            TokenUtils.hashTokens(DEFAULT_TOKENS),
            TokenUtils.defaultTokenHash
        )
    }

    @Test
    fun `hashTokens normalizes order and casing`() {
        val first = listOf("Alpha", " beta ", "gamma")
        val second = listOf("gamma", "ALPHA", "BETA")

        val firstHash = TokenUtils.hashTokens(first)
        val secondHash = TokenUtils.hashTokens(second)

        assertEquals(firstHash, secondHash)
    }

    @Test
    fun `stringToTokens splits on whitespace and commas`() {
        val input = "alpha, beta   gamma,delta"
        val tokens = TokenUtils.stringToTokens(input)

        assertEquals(listOf("alpha", "beta", "gamma", "delta"), tokens)
    }
}
