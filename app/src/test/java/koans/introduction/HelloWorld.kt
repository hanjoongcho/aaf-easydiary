package koans.introduction

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Introduction/Hello,%20world!/Task.kt
 */
class HelloWorld {
    private fun start() = "OK"

    @Test
    @Throws(Exception::class)
    fun start_test() {
        Assert.assertEquals("OK", start())
    }
}