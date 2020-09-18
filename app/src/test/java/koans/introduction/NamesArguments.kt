package koans.introduction

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Introduction/Named%20arguments/Task.kt
 */
class NamesArguments {
    private fun joinOptions(options: Collection<String>) = options.joinToString(prefix = "[", postfix = "]")

    @Test
    @Throws(Exception::class)
    fun joinOption_test() {
        Assert.assertEquals("[a, b, c]", joinOptions(listOf("a", "b", "c")))
    }
}