package com.simplemobiletools.commons.helpers

import android.content.Context
import android.content.SharedPreferences
import com.simplemobiletools.commons.extensions.getSharedPrefs
import me.blog.korn123.easydiary.R

/**
 * Created by Hanjoong Cho on 2017-12-18.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

open class BaseConfig(val context: Context) {
    protected val prefs: SharedPreferences = context.getSharedPrefs()

    companion object {
        fun newInstance(context: Context) = BaseConfig(context)
    }

//    var appRunCount: Int
//        get() = prefs.getInt(APP_RUN_COUNT, 0)
//        set(appRunCount) = prefs.edit().putInt(APP_RUN_COUNT, appRunCount).apply()
//
//    var lastVersion: Int
//        get() = prefs.getInt(LAST_VERSION, 0)
//        set(lastVersion) = prefs.edit().putInt(LAST_VERSION, lastVersion).apply()
//
//    var treeUri: String
//        get() = prefs.getString(TREE_URI, "")
//        set(uri) = prefs.edit().putString(TREE_URI, uri).apply()
//
//    var sdCardPath: String
//        get() = prefs.getString(SD_CARD_PATH, "")
//        set(sdCardPath) = prefs.edit().putString(SD_CARD_PATH, sdCardPath).apply()
//
//    var internalStoragePath: String
//        get() = prefs.getString(INTERNAL_STORAGE_PATH, "")
//        set(internalStoragePath) = prefs.edit().putString(INTERNAL_STORAGE_PATH, internalStoragePath).apply()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, context.resources.getColor(R.color.default_text_color))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, context.resources.getColor(R.color.default_background_color))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, context.resources.getColor(R.color.colorPrimary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    var customTextColor: Int
        get() = prefs.getInt(CUSTOM_TEXT_COLOR, textColor)
        set(customTextColor) = prefs.edit().putInt(CUSTOM_TEXT_COLOR, customTextColor).apply()

    var customBackgroundColor: Int
        get() = prefs.getInt(CUSTOM_BACKGROUND_COLOR, backgroundColor)
        set(customBackgroundColor) = prefs.edit().putInt(CUSTOM_BACKGROUND_COLOR, customBackgroundColor).apply()

    var customPrimaryColor: Int
        get() = prefs.getInt(CUSTOM_PRIMARY_COLOR, primaryColor)
        set(customPrimaryColor) = prefs.edit().putInt(CUSTOM_PRIMARY_COLOR, customPrimaryColor).apply()

    var widgetBgColor: Int
        get() = prefs.getInt(WIDGET_BG_COLOR, 1)
        set(widgetBgColor) = prefs.edit().putInt(WIDGET_BG_COLOR, widgetBgColor).apply()

    var widgetTextColor: Int
        get() = prefs.getInt(WIDGET_TEXT_COLOR, context.resources.getColor(R.color.colorPrimary))
        set(widgetTextColor) = prefs.edit().putInt(WIDGET_TEXT_COLOR, widgetTextColor).apply()

//    // hidden folder visibility protection
//    var isPasswordProtectionOn: Boolean
//        get() = prefs.getBoolean(PASSWORD_PROTECTION, false)
//        set(isPasswordProtectionOn) = prefs.edit().putBoolean(PASSWORD_PROTECTION, isPasswordProtectionOn).apply()
//
//    var passwordHash: String
//        get() = prefs.getString(PASSWORD_HASH, "")
//        set(passwordHash) = prefs.edit().putString(PASSWORD_HASH, passwordHash).apply()
//
//    var protectionType: Int
//        get() = prefs.getInt(PROTECTION_TYPE, PROTECTION_PATTERN)
//        set(protectionType) = prefs.edit().putInt(PROTECTION_TYPE, protectionType).apply()
//
//    // whole app launch protection
//    var appPasswordProtectionOn: Boolean
//        get() = prefs.getBoolean(APP_PASSWORD_PROTECTION, false)
//        set(appPasswordProtectionOn) = prefs.edit().putBoolean(APP_PASSWORD_PROTECTION, appPasswordProtectionOn).apply()
//
//    var appPasswordHash: String
//        get() = prefs.getString(APP_PASSWORD_HASH, "")
//        set(appPasswordHash) = prefs.edit().putString(APP_PASSWORD_HASH, appPasswordHash).apply()
//
//    var appProtectionType: Int
//        get() = prefs.getInt(APP_PROTECTION_TYPE, PROTECTION_PATTERN)
//        set(appProtectionType) = prefs.edit().putInt(APP_PROTECTION_TYPE, appProtectionType).apply()
//
//    var keepLastModified: Boolean
//        get() = prefs.getBoolean(KEEP_LAST_MODIFIED, true)
//        set(keepLastModified) = prefs.edit().putBoolean(KEEP_LAST_MODIFIED, keepLastModified).apply()
//
//    var useEnglish: Boolean
//        get() = prefs.getBoolean(USE_ENGLISH, false)
//        set(useEnglish) {
//            wasUseEnglishToggled = true
//            prefs.edit().putBoolean(USE_ENGLISH, useEnglish).apply()
//        }
//
//    var wasUseEnglishToggled: Boolean
//        get() = prefs.getBoolean(WAS_USE_ENGLISH_TOGGLED, false)
//        set(wasUseEnglishToggled) = prefs.edit().putBoolean(WAS_USE_ENGLISH_TOGGLED, wasUseEnglishToggled).apply()
//
//    var wasSharedThemeEverActivated: Boolean
//        get() = prefs.getBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, false)
//        set(wasSharedThemeEverActivated) = prefs.edit().putBoolean(WAS_SHARED_THEME_EVER_ACTIVATED, wasSharedThemeEverActivated).apply()
//
    var isUsingSharedTheme: Boolean
        get() = prefs.getBoolean(IS_USING_SHARED_THEME, false)
        set(isUsingSharedTheme) = prefs.edit().putBoolean(IS_USING_SHARED_THEME, isUsingSharedTheme).apply()

//    var wasCustomThemeSwitchDescriptionShown: Boolean
//        get() = prefs.getBoolean(WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN, false)
//        set(wasCustomThemeSwitchDescriptionShown) = prefs.edit().putBoolean(WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN, wasCustomThemeSwitchDescriptionShown).apply()
//
//    var wasSharedThemeForced: Boolean
//        get() = prefs.getBoolean(WAS_SHARED_THEME_FORCED, false)
//        set(wasSharedThemeForced) = prefs.edit().putBoolean(WAS_SHARED_THEME_FORCED, wasSharedThemeForced).apply()
//
//    // used only for checking shared theme after updating to 3.0.0 from some previous version
//    var wasSharedThemeAfterUpdateChecked: Boolean
//        get() = prefs.getBoolean(WAS_SHARED_THEME_AFTER_UPDATE_CHECKED, false)
//        set(wasSharedThemeAfterUpdateChecked) = prefs.edit().putBoolean(WAS_SHARED_THEME_AFTER_UPDATE_CHECKED, wasSharedThemeAfterUpdateChecked).apply()
//
//    var showInfoBubble: Boolean
//        get() = prefs.getBoolean(SHOW_INFO_BUBBLE, true)
//        set(showInfoBubble) = prefs.edit().putBoolean(SHOW_INFO_BUBBLE, showInfoBubble).apply()
}
