package me.blog.korn123.commons.constants;

import android.Manifest;

/**
 * Created by CHO HANJOONG on 2017-03-18.
 */

public class Constants {

    // startActivityForResult Request Code: Permission
    final static public int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    final static public int REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD = 2;

    // startActivityForResult Request Code: Google Drive
    final static public int REQUEST_CODE_GOOGLE_DRIVE_UPLOAD = 11;
    final static public int REQUEST_CODE_GOOGLE_DRIVE_DOWNLOAD = 12;

    // startActivityForResult Request Code: Etc
    final static public int REQUEST_CODE_LOCK_SETTING = 21;
    final static public int REQUEST_CODE_IMAGE_PICKER = 22;

    // startActivityForResult Request Code: ColorPicker
    final static public int REQUEST_CODE_BACKGROUND_COLOR_PICKER = 31;
    final static public int REQUEST_CODE_TEXT_COLOR_PICKER = 32;

    final static public String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS};
    final static public String[] EXTERNAL_STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    final static public int SETTING_FLAG_EXPORT_GOOGLE_DRIVE = 1;
    final static public int SETTING_FLAG_IMPORT_GOOGLE_DRIVE = 2;

    final static public int REQUEST_CODE_SPEECH_INPUT = 100;

    // 일기작성화면
    final static public int PREVIOUS_ACTIVITY_CREATE = 1;

    final static public int WEATHER_SUNNY = 1;

    final static public int WEATHER_CLOUD_AND_SUN = 2;

    final static public int WEATHER_RAIN_DROPS = 3;

    final static public int WEATHER_BOLT = 4;

    final static public int WEATHER_SNOWING = 5;

    final static public int WEATHER_RAINBOW = 6;

    final static public int WEATHER_UMBRELLA = 7;

    final static public int WEATHER_STARS = 8;

    final static public int WEATHER_MOON = 9;

    final static public int WEATHER_NIGHT_RAIN = 10;

    final static public int SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER = 0;
    final static public int SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER = 1;
    final static public int SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER = 2;
    final static public int SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER = 3;

    // custom fonts supported language
    final static public String CUSTOM_FONTS_SUPPORT_LANGUAGE = "en|ko";

    final static public String CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT = "NanumPen.ttf";

    final static public String CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT = "Default";

    // previous screen
    final static public String PREVIOUS_ACTIVITY = "previous_activity";

    // font name preference key
    final static public String SETTING_FONT_NAME = "font_setting";

    // font size preference key
    final static public String SETTING_FONT_SIZE = "font_size";

    // pause millis preference key
    final static public String SETTING_PAUSE_MILLIS = "pause_millis";

    final static public String INIT_DUMMY_DATA_FLAG = "init_dummy_data";

    final static public String APP_FINISH_FLAG = "app_finish";

    final static public String APP_LOCK_ENABLE = "application_lock";

    final static public String APP_LOCK_REQUEST_PASSWORD = "lock_password";

    final static public String APP_LOCK_SAVED_PASSWORD = "application_lock_password";

    final public static String DIARY_SEQUENCE = "diary_sequence";

    final public static String DIARY_SEARCH_QUERY = "diary_search_query";

    final public static String OPEN_URL_INFO = "open_url_info";

    final public static String DIARY_SEARCH_QUERY_CASE_SENSITIVE = "case_sensitive";

}
