package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import id.zelory.compressor.Compressor
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SecondItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiarySymbol
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by hanjoong on 2017-04-30.
 */

object FlavorUtils {
    fun sequenceToSymbolResourceId(sequence: Int) = when (sequence) {
        SYMBOL_SELECT_ALL -> R.drawable.ic_select_symbol
        WEATHER_SUNNY -> R.drawable.ic_sunny
        WEATHER_CLOUD_AND_SUN -> R.drawable.ic_clouds_and_sun
        WEATHER_RAIN_DROPS -> R.drawable.ic_raindrops
        WEATHER_BOLT -> R.drawable.ic_bolt
        WEATHER_SNOWING -> R.drawable.ic_snowing
        WEATHER_RAINBOW -> R.drawable.ic_rainbow
        WEATHER_UMBRELLA -> R.drawable.ic_umbrella_1
        WEATHER_STARS -> R.drawable.ic_stars_2
        WEATHER_MOON -> R.drawable.ic_moon_9
        WEATHER_NIGHT_RAIN -> R.drawable.ic_night_rain

        DAILY_TODO -> R.drawable.ic_todo
        DAILY_DOING -> R.drawable.ic_doing
        DAILY_DONE -> R.drawable.ic_done
        DAILY_CANCEL -> R.drawable.ic_cancel

        else -> 0
    }

    fun initWeatherView(context: Context, imageView: ImageView?, weatherFlag: Int, isShowEmptyWeatherView: Boolean = false, applyWhiteFilter: Boolean = false) {
        imageView?.run {
            visibility = if (!isShowEmptyWeatherView && weatherFlag < 1) View.GONE else View.VISIBLE
            setImageResource(sequenceToSymbolResourceId(weatherFlag))
        }
    }

    fun getDiarySymbolMap(context: Context): HashMap<Int, String> {
        val symbolMap = hashMapOf<Int, String>()
        val symbolArray = arrayOf(
                *context.resources.getStringArray(R.array.weather_item_array),
                *context.resources.getStringArray(R.array.tasks_item_array)
        )

        symbolArray.map { item ->
            val symbolItem = DiarySymbol(item)
            symbolMap.put(symbolItem.sequence, symbolItem.description)
        }
        return symbolMap
    }
}
