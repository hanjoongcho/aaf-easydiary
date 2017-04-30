package me.blog.korn123.commons.constants;

import android.Manifest;

/**
 * Created by CHO HANJOONG on 2017-03-18.
 */

public class Constants {

    final static public int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    final static public int REQUEST_CODE_GOOGLE_DRIVE_UPLOAD = 11;
    final static public int REQUEST_CODE_GOOGLE_DRIVE_DOWNLOAD = 12;

    final static public String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS};
    final static public String[] EXTERNAL_STORAGE_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    final static public int SETTING_FLAG_EXPORT_GOOGLE_DRIVE = 1;
    final static public int SETTING_FLAG_IMPORT_GOOGLE_DRIVE = 2;

    // 이전화면
    final static public String PREVIOUS_ACTIVITY = "previous_activity";

    // 일기작성화면
    final static public int PREVIOUS_ACTIVITY_CREATE = 1;

    // 맑음, 화창함
    final static public int WEATHER_FLAG_SUN = 1;

    // 흐림
    final static public int WEATHER_FLAG_SUN_AND_CLOUD = 2;

    // 비
    final static public int WEATHER_FLAG_RAIN = 3;

    // 번개
    final static public int WEATHER_FLAG_THUNDER_BOLT = 4;

    // 눈
    final static public int WEATHER_FLAG_SNOW = 5;

}
