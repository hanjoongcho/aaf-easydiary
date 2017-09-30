package me.blog.korn123.commons.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.icu.text.DisplayContext;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {

    private static Typeface mTypeface;

    public static void setTypefaceDefault(TextView view) {
        view.setTypeface(Typeface.DEFAULT);
    }

    public static void setTypeface(Context context, AssetManager assetManager, TextView view) {
        view.setTypeface(getTypeface(context, assetManager));
    }

    public static Typeface getTypeface(Context context, AssetManager assetManager) {
        if (mTypeface == null) {
            mTypeface = setCurrentTypeface(context, assetManager);
        }
        return  mTypeface;
    }

    public static Typeface setCurrentTypeface(Context context, AssetManager assetManager) {
        String currentFont = CommonUtils.loadStringPreference(context, "font_setting", "NanumPen.ttf");
        if (StringUtils.equals(currentFont, "Default")) {
            mTypeface = Typeface.DEFAULT;
        } else {
            mTypeface = Typeface.createFromAsset(assetManager, "fonts/" + currentFont);
        }
        return mTypeface;
    }

    public static void setToolbarTypeface(Context context, Toolbar toolbar, AssetManager assetManager) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof  TextView) {
                setTypeface(context, assetManager, (TextView)view);
//                ((TextView) view).setTypeface(Typeface.DEFAULT);
            }
        }
    }

    public static void setToolbarTypeface(Toolbar toolbar, Typeface typeface) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof  TextView) {
                ((TextView) view).setTypeface(typeface);
            }
        }
    }

    public static void setFontsSize(float commonSize, float customSize, TextView... targetViews) {
        float fontSize = customSize > 0 ? customSize : commonSize;
        for (TextView textView : targetViews) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
        }
    }

    public static void setFontsTypeface(Context context, AssetManager assetManager, String customFontName, TextView... targetViews) {
        Typeface typeface = StringUtils.isNotEmpty(customFontName) ? getCustomTypeface(context, assetManager, customFontName) : getTypeface(context, assetManager);
        for (TextView textView : targetViews) {
            textView.setTypeface(typeface);
        }
    }

    public static Typeface getCustomTypeface(Context context, AssetManager assetManager, String customFontName) {
        Typeface typeface = null;
        String[] assetsFonts = context.getResources().getStringArray(R.array.pref_list_fonts_values);
        String[] userFonts = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY).list();
        if (isValidTypeface(assetsFonts, customFontName)) {
            typeface = Typeface.createFromAsset(assetManager, "fonts/" + customFontName);
        } else if (isValidTypeface(userFonts, customFontName)) {
            typeface = Typeface.createFromFile(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY + customFontName);
        } else {
            typeface = Typeface.DEFAULT;
        }
        return typeface;
    }

    public static boolean isValidTypeface(String[] fontArray, String fontName) {
        boolean result = false;
        for (String name : fontArray) {
            if (StringUtils.equalsIgnoreCase(name, fontName)) {
                result = true;
                break;
            }
        }
        return result;
    }

}
