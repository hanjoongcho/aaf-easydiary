package me.blog.korn123.easydiary.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.CardView
import android.util.Log
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.views.*
import io.github.hanjoongcho.commons.views.ModalView
import me.blog.korn123.easydiary.views.DiaryCardLayout
import me.blog.korn123.easydiary.views.LabelLayout

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Context.updateTextColors(viewGroup: ViewGroup, tmpTextColor: Int = 0, tmpAccentColor: Int = 0) {
    val textColor = if (tmpTextColor == 0) baseConfig.textColor else tmpTextColor
    val backgroundColor = baseConfig.backgroundColor
    val accentColor = if (tmpAccentColor == 0) {
        if (isBlackAndWhiteTheme()) {
            Color.WHITE
        } else {
            baseConfig.primaryColor
        }
    } else {
        tmpAccentColor
    }

    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
                    is MySwitchCompat -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyEditText -> {
                        it.setTextColor(textColor)
                        it.setHintTextColor(textColor.adjustAlpha(0.5f))
                        it.setLinkTextColor(accentColor)

                    }
                    is MyFloatingActionButton -> it.backgroundTintList = ColorStateList.valueOf(accentColor)
                    is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
                    is ModalView -> it.setBackgroundColor(accentColor)
                    is LabelLayout -> it.setBackgroundColor(accentColor)
                    is CardView -> {
                        //it.setCardBackgroundColor(backgroundColor)
                        updateTextColors(it, textColor, accentColor)
                    }
                    is ViewGroup -> updateTextColors(it, textColor, accentColor)
                }
            }
}
