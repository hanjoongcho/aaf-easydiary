package me.blog.korn123.commons.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiarySymbol

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
