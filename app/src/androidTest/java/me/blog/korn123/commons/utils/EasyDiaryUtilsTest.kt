package me.blog.korn123.commons.utils

import android.text.SpannedString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import com.simplemobiletools.commons.helpers.SETTING_CARD_VIEW_BACKGROUND_COLOR
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.preferenceToJsonString
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SETTING_THUMBNAIL_SIZE
import me.blog.korn123.easydiary.models.DiarySymbol
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Administrator on 2017-11-02.
 */

class EasyDiaryUtilsTest {ssss
    @Test
    fun test_01() {
        val textView = TextView(InstrumentationRegistry.getInstrumentation().targetContext)
        textView.text = "apple banana pineapple"
        EasyDiaryUtils.highlightString(textView, "APPLE")

        val spannedString = textView.text as SpannedString
        val backgroundSpans = spannedString.getSpans(0, spannedString.length, BackgroundColorSpan::class.java)
        assertFalse(backgroundSpans.size == 2)
    }

    @Test
    fun test_02() {
        val textView = TextView(InstrumentationRegistry.getInstrumentation().targetContext)
        textView.text = "apple banana pineapple"
        EasyDiaryUtils.highlightStringIgnoreCase(textView, "APPLE")

        val spannedString = textView.text as SpannedString
        val backgroundSpans = spannedString.getSpans(0, spannedString.length, BackgroundColorSpan::class.java)
        assertTrue(backgroundSpans.size == 2)
    }
    
    @Test
    fun test_03() {
        val symbolList = mutableListOf<DiarySymbol>()
        val symbolMap = hashMapOf<Int, String>()
        var symbolArray: Array<String>? = null
        InstrumentationRegistry.getInstrumentation().targetContext?.let {
            symbolArray = arrayOf(
                    *it.resources.getStringArray(R.array.weather_item_array),
                    *it.resources.getStringArray(R.array.emotion_item_array),
                    *it.resources.getStringArray(R.array.daily_item_array),
                    *it.resources.getStringArray(R.array.food_item_array),
                    *it.resources.getStringArray(R.array.leisure_item_array),
                    *it.resources.getStringArray(R.array.landscape_item_array)
            )
            
            symbolArray?.map { item ->
                val symbolItem = DiarySymbol(item)
                symbolList.add(symbolItem)
                symbolMap.put(symbolItem.sequence, symbolItem.description)
            }
        }
        symbolList.map { symbol ->  Log.i("AAF-t", "${symbol.sequence}-${symbol.description}/${symbolMap[symbol.sequence]} of ${symbolArray?.size ?: 0}")}
        assertTrue(symbolList.size == 122)
    }

    @Test
    fun test_04() {
        val jsonString = InstrumentationRegistry.getInstrumentation().targetContext.preferenceToJsonString()
        Log.i(AAF_TEST, jsonString)
        val map = EasyDiaryUtils.jsonStringToHashMap(jsonString)
        Log.i(AAF_TEST, map.toString())

        val screenBackgroundColor: Int = (map[SETTING_CARD_VIEW_BACKGROUND_COLOR] as Double).toInt()
        val settingThumbnailSize: Float = (map[SETTING_THUMBNAIL_SIZE] as Double).toFloat()

        assertEquals(screenBackgroundColor, -13882581)
        assertEquals(settingThumbnailSize, 50.0F)
    }

    @Test
    fun test_05() {
        Log.i(AAF_TEST, "Start")
        var count = 0
        EasyDiaryDbHelper.getTemporaryInstance().let {
            var items = EasyDiaryDbHelper.findDiary(null, realmInstance = it)
            items.forEach { diary ->
                Log.i(AAF_TEST, diary.title ?: "")
                count++
            }

            val symbolList = mutableListOf<DiarySymbol>()
            InstrumentationRegistry.getInstrumentation().targetContext.resources.getStringArray(R.array.leisure_item_array).map {
                val symbolItem = DiarySymbol(it)
                symbolList.add(symbolItem)
            }

            val pair = items.partition { item ->
                symbolList.find { it.sequence == item.weather } != null
            }

            Log.i(AAF_TEST, pair.first.size.toString())
            Log.i(AAF_TEST, pair.second.size.toString())
        }
        Log.i(AAF_TEST, "End")
        assertEquals(count, 200)
    }
}
