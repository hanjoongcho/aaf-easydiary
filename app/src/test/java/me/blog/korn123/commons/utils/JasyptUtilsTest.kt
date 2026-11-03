package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.utils.JasyptUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Modernized unit test suite for JasyptUtils.
 * Includes tests for text encryption/decryption round-trips, empty inputs, and cryptographic hashing (MD5, SHA-256).
 */
class JasyptUtilsTest {

    private val plainText = "apple and banana"
    private val keyString = "test-password-string"

    @Test
    fun `encrypt and decrypt roundtrip restores original plain text`() {
        val cipherText = JasyptUtils.encrypt(plainText, keyString)
        assertNotNull(cipherText)
        assertNotEquals(plainText, cipherText)

        val decryptedText = JasyptUtils.decrypt(cipherText, keyString)
        assertEquals(plainText, decryptedText)
    }

    @Test
    fun `encrypt with empty plain text returns empty string`() {
        val cipherText = JasyptUtils.encrypt("", keyString)
        assertEquals("", cipherText)
    }

    @Test
    fun `decrypt with empty cipher text returns empty string`() {
        val decryptedText = JasyptUtils.decrypt("", keyString)
        assertEquals("", decryptedText)
    }

    @Test
    fun `sha256 returns correct hash for input string`() {
        val input = "test"
        val expectedHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
        val result = JasyptUtils.sha256(input)
        assertEquals(expectedHash, result)
    }

    @Test
    fun `md5 returns correct hash for input string`() {
        val input = "test"
        val expectedHash = "098f6bcd4621d373cade4e832627b4f6"
        val result = JasyptUtils.md5(input)
        assertEquals(expectedHash, result)
    }
}
