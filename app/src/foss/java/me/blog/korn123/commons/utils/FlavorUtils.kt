package me.blog.korn123.commons.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiarySymbol
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

        DAILY_TODO -> R.drawable.ic_todo
        DAILY_DOING -> R.drawable.ic_doing
        DAILY_DONE -> R.drawable.ic_done
        DAILY_CANCEL -> R.drawable.ic_cancel

        else -> 0
    }

    fun initWeatherView(context: Context, imageView: ImageView?, weatherFlag: Int, isShowEmptyWeatherView: Boolean = false, applyWhiteFilter: Boolean = false) {
        val filterColor = when (applyWhiteFilter) {
            true -> ContextCompat.getColor(context, android.R.color.white)
            false -> context.config.textColor
        }
//        EasyDiaryUtils.changeDrawableIconColor(context, filterColor, R.drawable.ic_sunny)
//        EasyDiaryUtils.changeDrawableIconColor(context, filterColor, R.drawable.ic_clouds_and_sun)
//        EasyDiaryUtils.changeDrawableIconColor(context, filterColor, R.drawable.ic_raindrops)
//        EasyDiaryUtils.changeDrawableIconColor(context, filterColor, R.drawable.ic_bolt)
//        EasyDiaryUtils.changeDrawableIconColor(context, filterColor, R.drawable.ic_snowing)

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
