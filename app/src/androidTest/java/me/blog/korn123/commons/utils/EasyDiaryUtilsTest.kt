package me.blog.korn123.commons.utils

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.text.SpannedString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.DiarySymbol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

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
    
    @Test
    fun test_03() {
        var symbolList = mutableListOf<DiarySymbol>()
        var symbolMap = hashMapOf<Int, String>()
        InstrumentationRegistry.getTargetContext()?.let {
            var symbolArray = arrayOf(
                    *it.resources.getStringArray(R.array.weather_item_array),
                    *it.resources.getStringArray(R.array.emotion_item_array),
                    *it.resources.getStringArray(R.array.daily_item_array),
                    *it.resources.getStringArray(R.array.food_item_array),
                    *it.resources.getStringArray(R.array.leisure_item_array),
                    *it.resources.getStringArray(R.array.landscape_item_array)
            )
            
            symbolArray.map { item ->
                var symbolItem = DiarySymbol(item) 
                symbolList.add(symbolItem)
                symbolMap.put(symbolItem.sequence, symbolItem.description)
            }
        }
        symbolList.map { symbol ->  Log.i("AAF-t", "${symbol.sequence},${symbol.description}/${symbolMap[symbol.sequence]} of ${symbolList.size}")}
        assertTrue(symbolList.size == 38)
    }

    companion object {
        private var sContext: Context? = null

        @BeforeClass
        fun init() {
            sContext = InstrumentationRegistry.getTargetContext()
        }
    }

}
