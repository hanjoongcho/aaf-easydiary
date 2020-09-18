package koans.introduction

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Introduction/Default%20arguments/Task.kt
 */
class DefaultArguments {
    private fun foo(name: String, number: Int = 42, toUpperCase: Boolean = false) = (if (toUpperCase) name.toUpperCase() else name) + number

    @Test
    fun foo_test1() {
        Assert.assertEquals("a42", foo("a"))
    }

    @Test
    fun foo_test2() {
        Assert.assertEquals("b1", foo("b", number = 1))
    }

    @Test
    fun foo_test3() {
        Assert.assertEquals("C42", foo("c", toUpperCase = true))
    }

    @Test
    fun foo_test4() {
        Assert.assertEquals("D2", foo(name = "d", number = 2, toUpperCase = true))
    }
}