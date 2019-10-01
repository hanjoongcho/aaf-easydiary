package me.blog.korn123.commons.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import android.os.Environment
import android.util.Log
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
            val targetView = viewGroup.getChildAt(i)
            if (targetView is ViewGroup) {
                setTypeface(context, targetView, typeface, customLineSpacing)
            } else if (targetView is TextView) {
//                Log.i("fontInfo", targetView.text.toString())
                targetView.typeface = typeface
                if (customLineSpacing) {
                    targetView.setLineSpacing(0F, context.config.lineSpacingScaleFactor)
                }
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

    fun getCommonTypeface(context: Context, assetManager: AssetManager): Typeface? {
        if (sTypeface == null) {
            setCommonTypeface(context, assetManager)
        }
        return sTypeface
    }
    
    fun setTypefaceDefault(view: TextView) {
        view.typeface = Typeface.DEFAULT
    }

    fun setCommonTypeface(context: Context, assetManager: AssetManager) {
        val commonFontName = context.config.settingFontName
        sTypeface = getTypeface(context, assetManager, commonFontName)
    }

    fun setFontsTypeface(context: Context, assetManager: AssetManager, customFontName: String?, rootView: ViewGroup?) {
        setFontsTypeface(context, assetManager, customFontName, rootView, true)
    }

    fun setFontsTypeface(context: Context, assetManager: AssetManager, customFontName: String?, rootView: ViewGroup?, customLineSpacing: Boolean) {
        val typeface = if (StringUtils.isNotEmpty(customFontName)) getTypeface(context, assetManager, customFontName) else getCommonTypeface(context, assetManager)
        rootView?.let {
            setTypeface(context, it, typeface, customLineSpacing)
        }
    }

    fun getTypeface(context: Context, assetManager: AssetManager, fontName: String?): Typeface? {
        val assetsFonts = context.resources.getStringArray(R.array.pref_list_fonts_values)
        val userFonts = File(EasyDiaryUtils.getStorageBasePath() + USER_CUSTOM_FONTS_DIRECTORY).list()
        return when {
            isValidTypeface(assetsFonts, fontName) -> {
                if (StringUtils.equals(fontName, CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT)) {
                    Typeface.DEFAULT
                } else {
                    Typeface.createFromAsset(assetManager, "fonts/" + fontName)
                }
            }
            isValidTypeface(userFonts, fontName) -> Typeface.createFromFile(EasyDiaryUtils.getStorageBasePath() + USER_CUSTOM_FONTS_DIRECTORY + fontName)
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
