package me.blog.korn123.commons.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT
import me.blog.korn123.easydiary.helper.USER_CUSTOM_FONTS_DIRECTORY
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import java.io.File

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object FontUtils {
    private var sTypeface: Typeface? = null

    private fun setTypeface(context: Context, viewGroup: ViewGroup, typeface: Typeface?, customLineSpacing: Boolean) {
        for (i in 0 until viewGroup.childCount) {
            when (val targetView = viewGroup.getChildAt(i)) {
                is ViewGroup -> setTypeface(context, targetView, typeface, customLineSpacing)
                is TextView -> {
                    targetView.typeface = typeface
                    if (customLineSpacing) {
                        targetView.setLineSpacing(0F, context.config.lineSpacingScaleFactor)
                    }
                }
                else -> {}
            }
        }
    }

    private fun isValidTypeface(fontArray: Array<String>?, fontName: String?): Boolean {
        var result = false
        if (fontArray != null) {
            for (name in fontArray) {
                if (StringUtils.equalsIgnoreCase(name, fontName)) {
                    result = true
                    break
                }
            }
        }
        return result
    }

    fun getCommonTypeface(context: Context): Typeface? {
        if (sTypeface == null) {
            setCommonTypeface(context)
        }
        return sTypeface
    }
    
    fun setTypefaceDefault(view: TextView) {
        view.typeface = Typeface.DEFAULT
    }

    fun setCommonTypeface(context: Context) {
        val commonFontName = context.config.settingFontName
        sTypeface = getTypeface(context, context.assets, commonFontName)
    }

    fun setFontsTypeface(context: Context, assetManager: AssetManager, customFontName: String?, rootView: ViewGroup?) {
        setFontsTypeface(context, assetManager, customFontName, rootView, true)
    }

    fun setFontsTypeface(context: Context, assetManager: AssetManager, customFontName: String?, rootView: ViewGroup?, customLineSpacing: Boolean) {
        val typeface = if (StringUtils.isNotEmpty(customFontName)) getTypeface(context, assetManager, customFontName) else getCommonTypeface(context)
        rootView?.let {
            setTypeface(context, it, typeface, customLineSpacing)
        }
    }

    fun getTypeface(context: Context, assetManager: AssetManager, fontName: String?): Typeface? {
        val assetsFonts = context.resources.getStringArray(R.array.pref_list_fonts_values)
        val userFonts = File(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY).list()
        return when {
            isValidTypeface(assetsFonts, fontName) -> {
                if (StringUtils.equals(fontName, CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT)) {
                    Typeface.DEFAULT
                } else {
                    Typeface.createFromAsset(assetManager, "fonts/" + fontName)
                }
            }
            isValidTypeface(userFonts, fontName) -> Typeface.createFromFile(EasyDiaryUtils.getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY + fontName)
            else -> Typeface.DEFAULT
        }
    }

    fun fontFileNameToDisplayName(context: Context, fontFileName: String): String {
        var displayName: String? = null
        val fontNames = context.resources.getStringArray(R.array.pref_list_fonts_values)
        val displayNames = context.resources.getStringArray(R.array.pref_list_fonts_title)
        for (i in fontNames.indices) {
            if (StringUtils.equals(fontFileName, fontNames[i])) {
                displayName = displayNames[i]
                break
            }
        }
        return displayName ?: FilenameUtils.getBaseName(fontFileName)
    }
}
