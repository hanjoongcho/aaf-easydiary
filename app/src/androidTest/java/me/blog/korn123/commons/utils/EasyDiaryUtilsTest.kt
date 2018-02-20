package me.blog.korn123.commons.utils

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.text.SpannedString
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

/**
 * Created by Administrator on 2017-11-02.
 */

class EasyDiaryUtilsTest {

    @Test
    fun test_01() {
        val textView = TextView(sContext)
        textView.text = "apple banana pineapple"
        EasyDiaryUtils.highlightString(textView, "APPLE")

        val spannedString = textView.text as SpannedString
        val backgroundSpans = spannedString.getSpans(0, spannedString.length, BackgroundColorSpan::class.java)
        assertFalse(backgroundSpans.size == 2)
    }

    @Test
    fun test_02() {
        val textView = TextView(sContext)
        textView.text = "apple banana pineapple"
        EasyDiaryUtils.highlightStringIgnoreCase(textView, "APPLE")

        val spannedString = textView.text as SpannedString
        val backgroundSpans = spannedString.getSpans(0, spannedString.length, BackgroundColorSpan::class.java)
        assertTrue(backgroundSpans.size == 2)
    }

    companion object {

        private var sContext: Context? = null

        @BeforeClass
        fun init() {
            sContext = InstrumentationRegistry.getTargetContext()
        }
    }

}
