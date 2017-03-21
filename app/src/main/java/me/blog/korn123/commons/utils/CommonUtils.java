package me.blog.korn123.commons.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class CommonUtils {

    public static float loadFloatPreference(Context context, String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(key, defaultValue);
    }

    public static void saveFloatPreference(Context context, String key, float value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putFloat(key, value);
        edit.commit();
    }

    public static int loadIntPreference(Context context, String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, defaultValue);
    }

    public static void saveIntPreference(Context context, String key, int value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    public static boolean loadBooleanPreference(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = preferences.getBoolean(key, false);
        return result;
    }

    public static void saveBooleanPreference(Context context, String key, boolean isEnable) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, isEnable);
        edit.commit();
    }
}
