package koans.introduction

import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Single Abstract Method
 * https://play.kotlinlang.org/koans/Introduction/SAM%20conversions/Task.kt
 */
class SAMConversions {
    private fun getList(list: List<Int>): List<Int> {
        Collections.sort(list) { x, y -> y- x }
        return list
    }

    @Test
    fun getList_test1() {
        Assert.assertArrayEquals(intArrayOf(5, 2, 1), getList(listOf(1, 5, 2)).toIntArray())
    }
}