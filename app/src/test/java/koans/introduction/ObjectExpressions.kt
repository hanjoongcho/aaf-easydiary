package koans.introduction

import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.Comparator

/**
 * https://play.kotlinlang.org/koans/Introduction/Object%20expressions/Task.kt
 */
class ObjectExpressions {
    private fun getListMixingJava(list: List<Int>): List<Int> {
        Collections.sort(list, object : Comparator<Int> {
            override fun compare(x: Int, y: Int) = y - x
        })
        return list
    }

    @Test
    fun getList_test1() {
        Assert.assertArrayEquals(intArrayOf(5, 2, 1), getListMixingJava(listOf(1, 5, 2)).toIntArray())
    }
}