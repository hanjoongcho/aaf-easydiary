package me.blog.korn123.easydiary.utils

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixelFloatValue
import me.blog.korn123.easydiary.extensions.getBaseName
import me.blog.korn123.easydiary.extensions.preferencesContains
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.views.FixedTextView
import java.io.File
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
object FontUtils {
    private var sTypeface: Typeface? = null

    private fun setTypeface(
        context: Context,
        viewGroup: ViewGroup,
        typeface: Typeface?,
        customLineSpacing: Boolean,
    ) {
        for (i in 0 until viewGroup.childCount) {
            when (val targetView = viewGroup.getChildAt(i)) {
                is ViewGroup -> {
                    setTypeface(context, targetView, typeface, customLineSpacing)
                }

                is TextView -> {
                    targetView.also {
                        it.typeface = typeface
                        if (customLineSpacing) {
                            it.setLineSpacing(0F, context.config.lineSpacingScaleFactor)
                        }

                        if (it is FixedTextView && it.applyHighLight) EasyDiaryUtils.highlightString(it)
                        if (it is FixedTextView && it.applyBoldStyle) it.setTypeface(typeface, Typeface.BOLD)
                    }
                }

                else -> {}
            }
        }
    }

    private fun isValidTypeface(
        fontArray: Array<String>?,
        fontName: String?,
    ): Boolean {
        var result = false
        if (fontArray != null) {
            for (name in fontArray) {
                if (name.equals(fontName, ignoreCase = true)) {
                    result = true
                    break
                }
            }
        }
        return result
    }

    private fun initDefaultFontSetting(activity: Activity) {
        activity.run {
            // Initial font typeface setting
            if (!preferencesContains(SETTING_FONT_NAME)) {
                config.settingFontName = CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT
            }

            // Initial font size setting
            if (!preferencesContains(SETTING_FONT_SIZE)) {
                config.settingFontSize = spToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())
            }
        }
    }

    private fun initNanumPenFontSetting(activity: Activity) {
        activity.run {
            // Initial font size setting
            if (!preferencesContains(SETTING_FONT_SIZE)) {
                config.settingFontSize = dpToPixelFloatValue(SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())
            }
        }
    }

    fun getCommonTypeface(context: Context): Typeface {
        if (sTypeface == null) {
            setCommonTypeface(context)
        }
        return sTypeface ?: Typeface.DEFAULT
    }

    fun setTypefaceDefault(view: TextView) {
        view.typeface = Typeface.DEFAULT
    }

    fun setCommonTypeface(context: Context) {
        val commonFontName = context.config.settingFontName
        sTypeface = getTypeface(context, commonFontName)
    }

    fun setFontsTypeface(
        context: Context,
        customFontName: String?,
        rootView: ViewGroup?,
        customLineSpacing: Boolean = true,
    ) {
        val typeface = if (customFontName?.isNotEmpty() == true) getTypeface(context, customFontName) else getCommonTypeface(context)
        rootView?.let {
            setTypeface(context, it, typeface, customLineSpacing)
        }
    }

    fun getTypeface(
        context: Context,
        fontName: String?,
    ): Typeface {
        return try {
            val assetsFonts = context.resources.getStringArray(R.array.pref_list_fonts_values)
            val userFonts = File(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY).list()
            when {
                isValidTypeface(assetsFonts, fontName) -> {
                    if (fontName.equals(CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT, ignoreCase = true)) {
                        Typeface.DEFAULT
                    } else {
                        try {
                            Typeface.createFromAsset(context.assets, "fonts/" + fontName)
                        } catch (_: Throwable) {
                            try {
                                Typeface.createFromAsset(context.applicationContext.assets, "fonts/" + fontName)
                            } catch (_: Throwable) {
                                Typeface.DEFAULT
                            }
                        }
                    }
                }

                isValidTypeface(userFonts, fontName) -> {
                    try {
                        Typeface.createFromFile(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY + fontName)
                    } catch (_: Throwable) {
                        Typeface.DEFAULT
                    }
                }

                else -> {
                    Typeface.DEFAULT
                }
            }
        } catch (_: Throwable) {
            Typeface.DEFAULT
        }
    }

    fun fontFileNameToDisplayName(
        context: Context,
        fontFileName: String,
    ): String {
        var displayName: String? = null
        val fontNames = context.resources.getStringArray(R.array.pref_list_fonts_values)
        val displayNames = context.resources.getStringArray(R.array.pref_list_fonts_title)
        for (i in fontNames.indices) {
            if (fontFileName.equals(fontNames[i], ignoreCase = true)) {
                displayName = displayNames[i]
                break
            }
        }
        return displayName ?: fontFileName.getBaseName()
    }

    fun measureTextWidth(
        context: Context,
        paint: Paint,
        text: String,
        scaleFactor: Float = 1.9f,
    ): Int =
        paint
            .apply {
                typeface = getCommonTypeface(context)
            }.measureText(text)
            .toInt()
            .times(scaleFactor)
            .toInt()

    fun checkFontSetting(activity: Activity) {
        activity.run {
            // determine device language
            if (!Locale.getDefault().language.matches(CUSTOM_FONTS_SUPPORT_LANGUAGE.toRegex())) {
//                initNanumPenFontSetting(this)
                initDefaultFontSetting(this)
            } else {
                initDefaultFontSetting(this)
            }
        }
    }

    fun isDeviceSettingFont(context: Context): Boolean = context.config.settingFontName == CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT

    fun getCommonFontFile(context: Context): FontFamily? {
        val fontName = context.config.settingFontName
        return try {
            val assetsFonts = context.resources.getStringArray(R.array.pref_list_fonts_values)
            val userFonts = File(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY).list()
            when {
                isValidTypeface(assetsFonts, fontName) -> {
                    if (fontName.equals(CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT, ignoreCase = true)) {
                        null
                    } else {
                        val typeface = try {
                            Typeface.createFromAsset(context.assets, "fonts/" + fontName)
                        } catch (_: Throwable) {
                            try {
                                Typeface.createFromAsset(context.applicationContext.assets, "fonts/" + fontName)
                            } catch (_: Throwable) {
                                null
                            }
                        }
                        typeface?.let { FontFamily(it) }
                    }
                }

                isValidTypeface(userFonts, fontName) -> {
                    try {
                        val file = File(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY + context.config.settingFontName)
                        if (file.exists()) {
                            FontFamily(Font(file))
                        } else {
                            null
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }

                else -> {
                    null
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    fun getComposeFontFamily(context: Context): FontFamily? = if (isDeviceSettingFont(context)) null else getCommonFontFile(context)
}
