package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.DiarySymbol
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.helper.*
import java.util.*

/**
 * Created by hanjoong on 2017-04-30.
 */

object FlavorUtils {
    fun sequenceToSymbolResourceId(sequence: Int) =
        when (sequence) {
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

    fun initWeatherView(
        context: Context,
        imageView: ImageView?,
        weatherFlag: Int,
        isShowEmptyWeatherView: Boolean = false,
        applyWhiteFilter: Boolean = false,
    ) {
        val filterColor =
            when (applyWhiteFilter) {
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
            if (weatherFlag < SYMBOL_USER_CUSTOM_START) {
                setImageResource(sequenceToSymbolResourceId(weatherFlag))
            } else {
                // FIXME: WIP START
                if (context.config.enableDebugMode) {
                    val targetIndex = weatherFlag.minus(SYMBOL_USER_CUSTOM_START)

                    val filePath =
                        if (context.config.customSymbolPaths.size > targetIndex) {
                            context.config.customSymbolPaths[targetIndex].getFilePath()
                        } else {
                            ""
                        }
//                    setImageBitmap(BitmapFactory.decodeFile(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath))
                    Glide
                        .with(this)
                        .load(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath)
                        .listener(
                            object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                                    isFirstResource: Boolean,
                                ): Boolean = false

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable?>?,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean,
                                ): Boolean = false
                            },
                        ).transform(CenterCrop(), RoundedCorners(context.dpToPixel(5F)))
                        .into(this)
                } else {
                    setImageResource(0)
                }
                // FIXME: WIP END
            }
        }
    }

    fun getDiarySymbolMap(context: Context): HashMap<Int, String> {
        val symbolMap = hashMapOf<Int, String>()
        val symbolArray =
            arrayOf(
                *context.resources.getStringArray(R.array.weather_item_array),
                *context.resources.getStringArray(R.array.tasks_item_array),
            )

        symbolArray.map { item ->
            val symbolItem = DiarySymbol(item)
            symbolMap.put(symbolItem.sequence, symbolItem.description)
        }
        return symbolMap
    }
}
