package koans.introduction

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Introduction/Extensions%20on%20collections/Task.kt
 */
class ExtensionsOnCollections {
    private fun getList(list: List<Int>): List<Int> = list.sortedWith(Comparator { x, y -> y -x })

    @Test
    fun getList_test2() {
        Assert.assertArrayEquals(intArrayOf(5, 2, 1), getList(listOf(1, 5, 2)).toIntArray())
    }
}