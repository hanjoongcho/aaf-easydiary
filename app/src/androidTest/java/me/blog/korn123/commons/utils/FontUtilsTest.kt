package me.blog.korn123.commons.utils

import android.content.Context
import android.support.test.InstrumentationRegistry

import org.junit.BeforeClass
import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Created by Administrator on 2017-11-06.
 */

class FontUtilsTest {

    @Test
    fun test_03() {
        assertEquals("NanumPen", FontUtils.fontFileNameToDisplayName(sContext, "NanumPen.ttf"))
    }

    @Test
    fun test_04() {
        assertEquals("consola", FontUtils.fontFileNameToDisplayName(sContext, "consola.ttf"))
    }

    companion object {

        private var sContext: Context? = null

        @BeforeClass
        fun init() {
            sContext = InstrumentationRegistry.getTargetContext()
        }
    }

}
