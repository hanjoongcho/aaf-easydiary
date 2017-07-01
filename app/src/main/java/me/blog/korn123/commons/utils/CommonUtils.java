package me.blog.korn123.commons.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.TypedValue;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class CommonUtils {

    public static int getBuildVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String loadStringPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, defaultValue);
    }

    public static void saveStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static long loadLongPreference(Context context, String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(key, defaultValue);
    }

    public static void saveLongPreference(Context context, String key, long value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

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

    public static int dpToPixel(Context context, int dp) {
        return dpToPixel(context, dp, 0);
    }

    public static int dpToPixel(Context context, int dp, int policy) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        int pixel = 0;
        switch (policy) {
            case 0:
                pixel = (int) px;
                break;
            case 1:
                pixel = Math.round(px);
                break;
        }
        return pixel;
    }

    public static String uriToPath(ContentResolver contentResolver, Uri uri) {
        String path = null;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        cursor.moveToNext();
        int columnIndex = cursor.getColumnIndex("_data");
        if (columnIndex > 0) {
            path = cursor.getString(columnIndex);
        } else {
            path = uri.toString();
        }
        cursor.close();
        return path;
    }
}
