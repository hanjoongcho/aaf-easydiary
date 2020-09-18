package koans.introduction

import org.junit.Assert

class ExtensionFunctions {
    fun Int.r(): RationalNumber = RationalNumber(this, 1)
    fun Pair<Int, Int>.r(): RationalNumber = RationalNumber(first, second)

    data class RationalNumber(val numerator: Int, val denominator: Int)

    fun test1() {
        Assert.assertEquals(5, 5.r().numerator)
    }
}