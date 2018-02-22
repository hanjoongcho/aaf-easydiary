package me.blog.korn123.easydiary.helper

import android.content.Context
import io.github.hanjoongcho.commons.helpers.BaseConfig
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils

/**
 * Created by CHO HANJOONG on 2017-12-24.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var settingFontName: String
        get() = legacyPrefs.getString(SETTING_FONT_NAME, CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT)
        set(settingFontName) = legacyPrefs.edit().putString(SETTING_FONT_NAME, settingFontName).apply()

    var settingFontSize: Float
        get() = legacyPrefs.getFloat(SETTING_FONT_SIZE, CommonUtils.dpToPixel(context, Constants.DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE).toFloat())
        set(settingFontSize) = legacyPrefs.edit().putFloat(SETTING_FONT_SIZE, settingFontSize).apply()

    var diarySearchQueryCaseSensitive: Boolean
        get() = legacyPrefs.getBoolean(DIARY_SEARCH_QUERY_CASE_SENSITIVE, false)
        set(diarySearchQueryCaseSensitive) = legacyPrefs.edit().putBoolean(DIARY_SEARCH_QUERY_CASE_SENSITIVE, diarySearchQueryCaseSensitive).apply()
            
    
}