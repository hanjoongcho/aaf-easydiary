package koans.conventions

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Conventions/In%20range/Task.kt
 */
class InRange {

    class DateRange(val start: MyDate, val endInclusive: MyDate) {
        operator fun contains(item: MyDate): Boolean = start <= item && item <= endInclusive
    }

    private fun checkInRange(date: MyDate, first: MyDate, last: MyDate): Boolean {
        return date in DateRange(first, last)
    }

    @Test
    fun checkInRange_test1() {
        Assert.assertTrue(checkInRange(MyDate(2020, 9, 19), MyDate(2020, 9, 19), MyDate(2020, 9, 20)))
    }

    @Test
    fun checkInRange_test2() {
        Assert.assertFalse(checkInRange(MyDate(2020, 9, 21), MyDate(2020, 9, 19), MyDate(2020, 9, 20)))
    }
}