package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class EasyDiaryUtilsTest2 {
    @Test
    fun test_01() {
        val indexes = EasyDiaryUtils.searchWordIndexes("AppleApple", "A")
        println(indexes.joinToString(","))
        assertTrue(indexes.size == 2)
    }
}
