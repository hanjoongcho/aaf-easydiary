package me.blog.korn123.easydiary.utils

import io.github.aafactory.commons.utils.DateUtils
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2018-03-04.
 */

class DateUtilsTest {
    companion object {
        const val TIME_MILLIS: Long = 1520149040913    
    }
    
    @Test
    fun timeMillisToDateTime() {
        Assert.assertEquals("2018-03-04", DateUtils.timeMillisToDateTime(TIME_MILLIS, DateUtils.DATE_PATTERN_DASH))
    }

    @Test
    fun timeMillisToHour() {
        Assert.assertEquals("16", DateUtils.timeMillisToDateTime(TIME_MILLIS, "HH"))
    }

//    @Test
//    fun getCurrentDateTime() {
//        Assert.assertEquals("20180304_170749", DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"))
//    }
    
    @Test
    fun getFullPatternDateWithTimeAndSeconds01() {
        Assert.assertEquals("2018년 3월 4일 일요일 16:37 20", DateUtils.getFullPatternDateWithTimeAndSeconds(TIME_MILLIS))
    }

    @Test
    fun getFullPatternDateWithTimeAndSeconds02() {
        Assert.assertEquals("dimanche 4 mars 2018 16:37 20", DateUtils.getFullPatternDateWithTimeAndSeconds(TIME_MILLIS, Locale.FRANCE))
    }

    @Test
    fun getDateStringFromTimeMillis01() {
        Assert.assertEquals("2018년 3월 4일 일요일 16:37 20", DateUtils.getDateStringFromTimeMillis(TIME_MILLIS, SimpleDateFormat.FULL))
    }
}
