package me.blog.korn123.commons.utils

import org.junit.Assert.*
import org.junit.Test

class JasyptUtilsTest {

    @Test
    fun test_01() {
        val plainText = "apple and banana"
        val keyString = "test-password-string"
        val cipherText = JasyptUtils.encrypt(plainText, keyString)
        println(plainText)
        println(JasyptUtils.sha256(plainText))
        println(JasyptUtils.decrypt(cipherText, keyString))
        assertEquals(plainText, JasyptUtils.decrypt(cipherText, keyString))
    }

    @Test
    fun test_02() {
        val plainText = ""
        val keyString = "test-password-string"
        val cipherText = JasyptUtils.encrypt(plainText, keyString)
        println(cipherText)
        assertEquals("", plainText)
    }

    @Test
    fun test_03() {
        val cipherText = ""
        val keyString = "test-password-string"
        assertEquals("", JasyptUtils.encrypt(cipherText, keyString))
    }
}