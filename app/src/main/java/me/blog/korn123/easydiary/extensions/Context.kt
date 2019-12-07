package me.blog.korn123.easydiary.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.views.*
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.views.ModalView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.Config
import me.blog.korn123.easydiary.helper.DEFAULT_CALENDAR_FONT_SCALE
import me.blog.korn123.easydiary.helper.DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE
import me.blog.korn123.easydiary.views.FixedTextView

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

val Context.config: Config get() = Config.newInstance(this)

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
//                    is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
                    is FixedTextView -> {
                        when (it.id) {
                            R.id.dashboardTitle, R.id.diaryCount -> it.setTextColor(textColor)
                        }
                    }
                    is MyEditText -> {
                        it.setTextColor(textColor)
                        it.setHintTextColor(textColor.adjustAlpha(0.5f))
                        it.setLinkTextColor(accentColor)
                    }
                    is MyFloatingActionButton -> it.backgroundTintList = ColorStateList.valueOf(accentColor)
                    is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
                    is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
                    is ModalView -> it.setBackgroundColor(accentColor)
                    is ViewGroup -> updateTextColors(it, textColor, accentColor)
                }
            }
}

fun Context.updateAppViews(viewGroup: ViewGroup, tmpBackgroundColor: Int = 0) {
    val backgroundColor = if (tmpBackgroundColor == 0) baseConfig.backgroundColor else tmpBackgroundColor
    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is CardView -> {
                        when (it.id) {
                            R.id.rank1, R.id.rank2, R.id.rank3, R.id.rank4 -> {
                                it.cardElevation = CommonUtils.dpToPixelFloatValue(this, 5F)
                            }
                            else -> {
                                it.setCardBackgroundColor(backgroundColor)
                                updateAppViews(it)
                            }
                        }
                    }
                    is ViewGroup -> updateAppViews(it)
                }
            }
}

fun Context.updateCardViewPolicy(viewGroup: ViewGroup) {
    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is CardView -> {
                        if (config.enableCardViewPolicy || it.id == R.id.rank1 || it.id == R.id.rank2 || it.id == R.id.rank3 || it.id == R.id.rank4) {
                            it.useCompatPadding = true
                            it.cardElevation = CommonUtils.dpToPixelFloatValue(this, 2F)
                        } else {
                            it.useCompatPadding = false
                            it.cardElevation = 0F
                        }
                    }
                    is ViewGroup -> updateCardViewPolicy(it)
                }
            }
}

fun Context.updateTextSize(viewGroup: ViewGroup, context: Context, addSize: Int) {
    val cnt = viewGroup.childCount
    val settingFontSize: Float = config.settingFontSize + addSize
    (0 until cnt)
            .map { index -> viewGroup.getChildAt(index) }
            .forEach {
                when (it) {
                    is TextView -> {
                        it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                    }
                    is ViewGroup -> updateTextSize(it, context, addSize)
                }
            }
}

fun Context.initTextSize(viewGroup: ViewGroup, context: Context) {
    val cnt = viewGroup.childCount
    val defaultFontSize: Float = CommonUtils.dpToPixelFloatValue(context, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE.toFloat())
    val settingFontSize: Float = config.settingFontSize
    (0 until cnt)
            .map { index -> viewGroup.getChildAt(index) }
            .forEach {
                when (it) {
                    is me.blog.korn123.easydiary.views.CalendarItem -> {
                        if (config.settingCalendarFontScale != DEFAULT_CALENDAR_FONT_SCALE) {
                            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize * config.settingCalendarFontScale)
                        }
                    }
                    is FixedTextView -> {}
                    is TextView -> { 
                        when (it.id) {
                            R.id.contentsLength -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize * 0.8F)
                            R.id.symbolTextArrow -> {}
                            R.id.createdDate -> {}
                            else -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                        }
                    }
                    is ViewGroup -> initTextSize(it, context)
                }
            }
}

fun Context.initTextSize(textView: TextView) {
    val defaultFontSize: Float = CommonUtils.dpToPixelFloatValue(this, DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE.toFloat())
    val settingFontSize: Float = config.settingFontSize
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
}

fun Context.checkPermission(permissions: Array<String>): Boolean {
    val listDeniedPermissions: List<String> = permissions.filter { permission -> 
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
    }
    return listDeniedPermissions.isEmpty()
}

fun Context.preferencesContains(key: String): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.contains(key)
}

fun Context.applyFontToMenuItem(mi: MenuItem) {
    val mNewTitle = SpannableString(mi.title)
    mNewTitle.setSpan(CustomTypefaceSpan("", FontUtils.getCommonTypeface(this, assets)!!), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    mi.title = mNewTitle
}