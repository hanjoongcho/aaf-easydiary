package koans.introduction

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Introduction/Lambdas/Task.kt
 */
class Lambdas {
    private fun containsEven(collection: Collection<Int>): Boolean = collection.any { it % 2 == 0 }

    @Test
    fun containsEven_test1() {
        Assert.assertTrue(containsEven(listOf(1, 3, 5, 7, 10)))
    }

    @Test
    fun containsEven_test2() {
        Assert.assertFalse(containsEven(listOf(1, 3, 5, 7, 9)))
    }
}