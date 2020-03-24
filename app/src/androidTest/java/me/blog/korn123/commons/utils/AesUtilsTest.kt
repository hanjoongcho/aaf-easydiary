package me.blog.korn123.commons.utils

import android.util.Log
import androidx.test.InstrumentationRegistry
import me.blog.korn123.easydiary.helper.AAF_TEST
import org.junit.Assert.assertEquals
import org.junit.Test

class AesUtilsTest {
    @Test
    fun test_01() {
        val context = InstrumentationRegistry.getTargetContext()
        val message = "This is a my diary"
        val cipherText = AesUtils.encryptPassword(context, message, "apple")
        Log.i(AAF_TEST, "cipherText: $cipherText")
        val plainText = AesUtils.decryptPassword(context, cipherText, "apple")
        assertEquals(message, plainText)
    }
}