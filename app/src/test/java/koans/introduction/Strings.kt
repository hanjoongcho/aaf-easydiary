package koans.introduction

import org.junit.Assert
import org.junit.Test

class Strings {
    private val month = "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)"

    private fun getPattern(): String = """\d{2} ${month} \d{4}"""

    @Test
    fun getPattern_test1() {
        Assert.assertTrue("18 OCT 2020".matches(getPattern().toRegex()))
    }

    @Test
    fun getPattern_test2() {
        Assert.assertTrue("18 JAN 2020".matches(getPattern().toRegex()))
    }

    @Test
    fun getPattern_test3() {
        Assert.assertFalse("18-JAN-2020".matches(getPattern().toRegex()))
    }

    @Test
    fun getPattern_test4() {
        Assert.assertFalse("18 JANUARY 2020".matches(getPattern().toRegex()))
    }
}