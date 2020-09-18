package koans.conventions

import org.junit.Assert
import org.junit.Test

data class MyDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<MyDate> {
    override fun compareTo(other: MyDate): Int = when {
        year != other.year -> year - other.year
        month != other.month -> month - other.month
        else -> dayOfMonth - other.dayOfMonth
    }
}

class Comparison {
    private fun compare(date1: MyDate, date2: MyDate) = date1 < date2

    @Test
    fun compare_test1() {
        Assert.assertTrue(compare(MyDate(2020, 9, 18), MyDate(2021, 9, 18)))
    }

    @Test
    fun compare_test2() {
        Assert.assertTrue(compare(MyDate(2020, 9, 17), MyDate(2020, 9, 18)))
    }
}