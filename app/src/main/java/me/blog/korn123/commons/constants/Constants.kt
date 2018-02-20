package me.blog.korn123.commons.constants

import android.Manifest

/**
 * Created by CHO HANJOONG on 2017-03-18.
 */

object Constants {
    // startActivityForResult Request Code: Permission
    const val REQUEST_CODE_EXTERNAL_STORAGE = 1
    const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD = 2
    const val REQUEST_CODE_EXTERNAL_STORAGE_WITH_FONT_SETTING = 3

    // startActivityForResult Request Code: Etc
    const val REQUEST_CODE_LOCK_SETTING = 21
    const val REQUEST_CODE_IMAGE_PICKER = 22

    // startActivityForResult Request Code: ColorPicker
    const val REQUEST_CODE_BACKGROUND_COLOR_PICKER = 31
    const val REQUEST_CODE_TEXT_COLOR_PICKER = 32

    val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)
    val EXTERNAL_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    const val SETTING_FLAG_EXPORT_GOOGLE_DRIVE = 1
    const val SETTING_FLAG_IMPORT_GOOGLE_DRIVE = 2

    const val REQUEST_CODE_SPEECH_INPUT = 100

    const val PREVIOUS_ACTIVITY_CREATE = 1

    const val WEATHER_SUNNY = 1
    const val WEATHER_CLOUD_AND_SUN = 2
    const val WEATHER_RAIN_DROPS = 3
    const val WEATHER_BOLT = 4
    const val WEATHER_SNOWING = 5
    const val WEATHER_RAINBOW = 6
    const val WEATHER_UMBRELLA = 7
    const val WEATHER_STARS = 8
    const val WEATHER_MOON = 9
    const val WEATHER_NIGHT_RAIN = 10

    const val SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER = 0
    const val SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER = 1
    const val SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER = 2
    const val SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER = 3

    // custom fonts supported language
    const val CUSTOM_FONTS_SUPPORT_LANGUAGE = "en|ko"
    const val CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT = "NanumPen.ttf"
    const val CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT = "Default"

    // previous screen
    const val PREVIOUS_ACTIVITY = "previous_activity"

    // font name preference key
    const val SETTING_FONT_NAME = "font_setting"

    // font size preference key
    const val SETTING_FONT_SIZE = "font_size"

    // pause millis preference key
    const val SETTING_PAUSE_MILLIS = "pause_millis"

    const val INIT_DUMMY_DATA_FLAG = "init_dummy_data"

    const val APP_FINISH_FLAG = "app_finish"
    const val APP_LOCK_ENABLE = "application_lock"
    const val APP_LOCK_REQUEST_PASSWORD = "lock_password"
    const val APP_LOCK_SAVED_PASSWORD = "application_lock_password"

    const val DIARY_SEQUENCE = "diary_sequence"
    const val DIARY_SEARCH_QUERY = "diary_search_query"

    const val OPEN_URL_INFO = "open_url_info"
    const val DIARY_SEARCH_QUERY_CASE_SENSITIVE = "case_sensitive"
    const val THUMBNAIL_BACKGROUND_ALPHA = 100

    const val DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE = 20
    const val DEFAULT_FONT_SIZE_UN_SUPPORT_LANGUAGE = 15
}
