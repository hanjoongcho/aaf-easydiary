package me.blog.korn123.commons.utils

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by Administrator on 2017-11-06.
 */

class FontUtilsTest {
    @Test
    fun test_03() {
        assertEquals("NanumPen", FontUtils.fontFileNameToDisplayName(InstrumentationRegistry.getInstrumentation().targetContext, "NanumPen.ttf"))
    }

    @Test
    fun test_04() {
        assertEquals("consola", FontUtils.fontFileNameToDisplayName(InstrumentationRegistry.getInstrumentation().targetContext, "consola.ttf"))
    }
}
