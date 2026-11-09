package com.simplemobiletools.commons.helpers

import android.os.Build
import android.os.Looper

const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"
const val BROADCAST_REFRESH_MEDIA = "com.simplemobiletools.REFRESH_MEDIA"
const val OTG_PATH = "otg:/"

const val HOUR_MINUTES = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val WEEK_SECONDS = WEEK_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60
const val YEAR_SECONDS = YEAR_MINUTES * 60

// shared preferences
const val PREFS_KEY = "Prefs"
const val APP_RUN_COUNT = "app_run_count"
const val LAST_VERSION = "last_version"
const val TREE_URI = "tree_uri_2"
const val OTG_TREE_URI = "otg_tree_uri"
const val OTG_BASE_PATH = "otg_base_path"
const val SD_CARD_PATH = "sd_card_path_2"
const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val CUSTOM_TEXT_COLOR = "custom_text_color"
const val CUSTOM_BACKGROUND_COLOR = "custom_background_color"
const val CUSTOM_PRIMARY_COLOR = "custom_primary_color"
const val WIDGET_BG_COLOR = "widget_bg_color"
const val WIDGET_TEXT_COLOR = "widget_text_color"
const val PASSWORD_PROTECTION = "password_protection"
const val PASSWORD_HASH = "password_hash"
const val PROTECTION_TYPE = "protection_type"
const val APP_PASSWORD_PROTECTION = "app_password_protection"
const val APP_PASSWORD_HASH = "app_password_hash"
const val APP_PROTECTION_TYPE = "app_protection_type"
const val KEEP_LAST_MODIFIED = "keep_last_modified"
const val USE_ENGLISH = "use_english"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val WAS_SHARED_THEME_EVER_ACTIVATED = "was_shared_theme_ever_activated"
const val IS_USING_SHARED_THEME = "is_using_shared_theme"
const val WAS_SHARED_THEME_FORCED = "was_shared_theme_forced"
const val WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN = "was_custom_theme_switch_description_shown"
const val WAS_SHARED_THEME_AFTER_UPDATE_CHECKED = "was_shared_theme_after_update_checked"
const val SHOW_INFO_BUBBLE = "show_info_bubble"
const val LAST_CONFLICT_RESOLUTION = "last_conflict_resolution"
const val LAST_CONFLICT_APPLY_TO_ALL = "last_conflict_apply_to_all"
const val AVOID_WHATS_NEW = "avoid_whats_new"
const val HAD_THANK_YOU_INSTALLED = "had_thank_you_installed"
const val SKIP_DELETE_CONFIRMATION = "skip_delete_confirmation"
const val ENABLE_PULL_TO_REFRESH = "enable_pull_to_refresh"
const val SCROLL_HORIZONTALLY = "scroll_horizontally"
const val PREVENT_PHONE_FROM_SLEEPING = "prevent_phone_from_sleeping"
const val LAST_USED_VIEW_PAGER_PAGE = "last_used_view_pager_page"
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val WAS_ALARM_WARNING_SHOWN = "was_alarm_warning_shown"
const val WAS_REMINDER_WARNING_SHOWN = "was_reminder_warning_shown"
const val USE_SAME_SNOOZE = "use_same_snooze"
const val SNOOZE_TIME = "snooze_delay"
const val VIBRATE_ON_BUTTON_PRESS = "vibrate_on_button_press"
const val SETTING_CARD_VIEW_BACKGROUND_COLOR = "setting_card_view_background_color"

// licenses
internal const val LICENSE_KOTLIN = 1
const val LICENSE_SUBSAMPLING = 2
const val LICENSE_GLIDE = 4
const val LICENSE_CROPPER = 8
const val LICENSE_MULTISELECT = 16
const val LICENSE_RTL = 32
const val LICENSE_JODA = 64
const val LICENSE_STETHO = 128
const val LICENSE_OTTO = 256
const val LICENSE_PHOTOVIEW = 512
const val LICENSE_PICASSO = 1024
const val LICENSE_PATTERN = 2048
const val LICENSE_REPRINT = 4096
const val LICENSE_GIF_DRAWABLE = 8192
const val LICENSE_AUTOFITTEXTVIEW = 16384
const val LICENSE_ROBOLECTRIC = 32768
const val LICENSE_ESPRESSO = 65536
const val LICENSE_GSON = 131072
const val LICENSE_LEAK_CANARY = 262144
const val LICENSE_NUMBER_PICKER = 524288

// global intents
const val OPEN_DOCUMENT_TREE = 1000
const val OPEN_DOCUMENT_TREE_OTG = 1001
const val REQUEST_SET_AS = 1002
const val REQUEST_EDIT_IMAGE = 1003

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 5096
const val SORT_BY_DURATION = 10192

// security
const val PROTECTION_PATTERN = 0
const val PROTECTION_PIN = 1
const val PROTECTION_FINGERPRINT = 2

const val SHOW_ALL_TABS = -1
const val SHOW_PATTERN = 0
const val SHOW_PIN = 1
const val SHOW_FINGERPRINT = 2

// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9

// conflict resolving
const val CONFLICT_SKIP = 1
const val CONFLICT_OVERWRITE = 2
const val CONFLICT_MERGE = 3

const val MONDAY_BIT = 1
const val TUESDAY_BIT = 2
const val WEDNESDAY_BIT = 4
const val THURSDAY_BIT = 8
const val FRIDAY_BIT = 16
const val SATURDAY_BIT = 32
const val SUNDAY_BIT = 64
const val EVERY_DAY_BIT = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT or SATURDAY_BIT or SUNDAY_BIT
const val WEEK_DAYS_BIT = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT
const val WEEKENDS_BIT = SATURDAY_BIT or SUNDAY_BIT

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun isJellyBean1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
fun isAndroidFour() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH
fun isKitkatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
fun isLollipopPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun getDateFormats() = arrayListOf(
        "yyyy-MM-dd",
        "yyyyMMdd",
        "yyyy.MM.dd",
        "yy-MM-dd",
        "yyMMdd",
        "yy.MM.dd",
        "yy/MM/dd",
        "MM-dd",
        "--MM-dd",
        "MMdd",
        "MM/dd",
        "MM.dd"
)
