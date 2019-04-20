package me.blog.korn123.easydiary.helper

import android.Manifest

/**
 * Created by CHO HANJOONG on 2018-02-09.
 */

val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)
val EXTERNAL_STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

const val START_MAIN_ACTIVITY = 0
const val APP_BACKGROUND_ALPHA = 90
const val INTRO_BACKGROUND_ALPHA = 255
const val SETTING_FONT_SIZE = "font_size"
const val SETTING_FONT_NAME = "font_setting" 
const val SETTING_THUMBNAIL_SIZE = "thumbnail_size" 
const val SETTING_BOLD_STYLE = "setting_bold_style" 
const val SETTING_MULTIPLE_PICKER = "setting_multiple_picker"
const val SETTING_FINGERPRINT_LOCK = "setting_fingerprint_lock"
const val SETTING_CONTENTS_SUMMARY = "setting_contents_summary"
const val FINGERPRINT_ENCRYPT_DATA = "fingerprint_encrypt_data"
const val FINGERPRINT_ENCRYPT_DATA_IV = "fingerprint_encrypt_data_iv"
const val FINGERPRINT_AUTHENTICATION_FAIL_COUNT = "fingerprint_authentication_fail_count"
const val ENABLE_CARD_VIEW_POLICY = "enable_card_view_policy"

const val CUSTOM_FONTS_SUPPORT_LANGUAGE = "en|ko" // custom fonts supported language
const val CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT = "NanumPen.ttf"
const val CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT = "Default"
const val DIARY_SEARCH_QUERY_CASE_SENSITIVE = "case_sensitive"
const val CONTENT_URI_PREFIX = "content:/"
const val FILE_URI_PREFIX = "file:/"
const val LINE_SPACING_SCALE_FACTOR = "line_spacing_scale_factor"
const val LINE_SPACING_SCALE_DEFAULT = 1.0F

const val WORKING_DIRECTORY = "/AAFactory/EasyDiary/"
const val DIARY_PHOTO_DIRECTORY = "/AAFactory/EasyDiary/Photos/"
const val DIARY_POSTCARD_DIRECTORY = "/AAFactory/EasyDiary/Postcards/"
const val USER_CUSTOM_FONTS_DIRECTORY = "/AAFactory/EasyDiary/Fonts/"
const val DIARY_DB_NAME = "diary.realm"

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

const val SETTING_FLAG_EXPORT_GOOGLE_DRIVE = 1
const val SETTING_FLAG_IMPORT_GOOGLE_DRIVE = 2
const val SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE = 3
const val SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE = 4

const val REQUEST_CODE_SPEECH_INPUT = 100

const val PREVIOUS_ACTIVITY = "previous_activity"
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

const val ACTIVITY_CHEST = 11
const val ACTIVITY_BALL = 12
const val ACTIVITY_MEETING = 13
const val ACTIVITY_MORE = 14
const val ACTIVITY_CONNECTION = 15
const val ACTIVITY_ENERGY = 16
const val ACTIVITY_PARTY = 17
const val ACTIVITY_FULL_BATTERY = 18
const val ACTIVITY_LUNG = 19
const val ACTIVITY_ROPE = 20
const val ACTIVITY_ALARM = 21
const val ACTIVITY_TIMER = 22
const val ACTIVITY_CLOTHES = 23
const val ACTIVITY_PUNCHING = 24
const val ACTIVITY_CAMPING = 25
const val ACTIVITY_STATIONARY_BIKE = 26
const val ACTIVITY_STEPS = 27
const val ACTIVITY_STAIRS = 28
const val ACTIVITY_HEALTH_FOODS = 29
const val ACTIVITY_ENERGY_DRINK = 30
const val ACTIVITY_DUMBBELL = 31
const val ACTIVITY_VACUUM = 32
const val ACTIVITY_LAWN_MOWER = 33
const val ACTIVITY_WATERING_CAN = 34
const val ACTIVITY_BROOM = 35
const val ACTIVITY_WATER = 36
const val ACTIVITY_MICROPHONE = 37
const val ACTIVITY_DUMBBELL2 = 38
const val ACTIVITY_SKATING = 39
const val ACTIVITY_TREADMILL = 40

const val DAILY_WORKING = 41
const val DAILY_WALLET = 42
const val DAILY_VITAMINS = 43
const val DAILY_GAME_PAD = 44
const val DAILY_SHIRT = 45
const val DAILY_GARBAGE = 46
const val DAILY_TIE = 47
const val DAILY_TICKET = 48
const val DAILY_LIKE = 49
const val DAILY_STUDY = 50
const val DAILY_SLEEP = 51
const val DAILY_SHOPPING_CART = 52
const val DAILY_REPAIR = 53
const val DAILY_LIST = 54
const val DAILY_PET = 55
const val DAILY_FATHERHOOD = 56
const val DAILY_COFFEE = 57
const val DAILY_EAT = 58

const val EMOJI_LAUGHING = 59

const val SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER = 0
const val SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER = 1
const val SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER = 2
const val SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER = 3

const val INIT_DUMMY_DATA_FLAG = "init_dummy_data"

const val APP_FINISH_FLAG = "app_finish"
const val APP_LOCK_ENABLE = "application_lock"
const val APP_LOCK_REQUEST_PASSWORD = "lock_password"
const val APP_LOCK_SAVED_PASSWORD = "application_lock_password"
const val APP_LOCK_DEFAULT_PASSWORD = "0000"

const val DIARY_SEQUENCE = "diary_sequence"
const val DIARY_SEARCH_QUERY = "diary_search_query"
const val DIARY_ATTACH_PHOTO_INDEX = "diary_attach_photo_index"
const val POSTCARD_SEQUENCE = "postcard_sequence"

const val OPEN_URL_INFO = "open_url_info"
const val THUMBNAIL_BACKGROUND_ALPHA = 170

const val DEFAULT_FONT_SIZE_SUPPORT_LANGUAGE = 20
const val DEFAULT_FONT_SIZE_UN_SUPPORT_LANGUAGE = 15
const val DEFAULT_THUMBNAIL_SIZE_DP = 50 

const val POSTCARD_BG_COLOR = "postcard_bg_color"
const val POSTCARD_TEXT_COLOR = "postcard_text_color"
const val POSTCARD_BG_COLOR_VALUE = -0x1
const val POSTCARD_TEXT_COLOR_VALUE = -0xb5b5b4
const val POSTCARD_CROP_MODE = "postcard_crop_mode"

// Save Instance State
const val LIST_URI_STRING = "list_uri_string"
const val SELECTED_YEAR = "selected_year"
const val SELECTED_MONTH = "selected_month"
const val SELECTED_DAY = "selected_day"
const val SELECTED_HOUR = "selected_hour"
const val SELECTED_MINUTE = "selected_minute"
const val SELECTED_SECOND = "selected_second"

const val NOTIFICATION_FOREGROUND_ID: Int = 1001
const val NOTIFICATION_COMPLETE_ID: Int = 1002
const val NOTIFICATION_CHANNEL_ID = "easy_diary_channel_id"
const val NOTIFICATION_CHANNEL_NAME = "Easy-Diary notification channel"
const val NOTIFICATION_CHANNEL_DESCRIPTION = "This channel is used for 'Easy-Diary' data backup and recovery operations."

const val NOTIFICATION_DRIVE_ID = "notification_drive_id"