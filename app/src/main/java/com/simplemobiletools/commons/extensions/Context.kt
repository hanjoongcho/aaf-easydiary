package com.simplemobiletools.commons.extensions

import android.content.Context
import android.os.Build
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.views.MyTextView

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Context.isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()
fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.isJellyBean1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
fun Context.isAndroidFour() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH
fun Context.isKitkatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
fun Context.isLollipopPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
fun Context.isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun Context.isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

val Context.isRTLLayout: Boolean get() = if (isJellyBean1Plus()) resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL else false

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

fun Context.updateTextColors(viewGroup: ViewGroup, tmpTextColor: Int = 0, tmpAccentColor: Int = 0) {
    val textColor = if (tmpTextColor == 0) baseConfig.textColor else tmpTextColor
    val accentColor = if (tmpAccentColor == 0) baseConfig.primaryColor else tmpAccentColor
    val backgroundColor = baseConfig.backgroundColor
    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MySwitchCompat -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyEditText -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyFloatingActionButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is ViewGroup -> updateTextColors(it, textColor, accentColor)
                }
            }
}