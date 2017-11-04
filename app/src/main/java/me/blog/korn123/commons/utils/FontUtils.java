package me.blog.korn123.commons.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Type;

import me.blog.korn123.commons.constants.Constants;
import me.blog.korn123.commons.constants.Path;
import me.blog.korn123.easydiary.R;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {

    private static Typeface sTypeface;

    public static void setTypefaceDefault(TextView view) {
        view.setTypeface(Typeface.DEFAULT);
    }

    public static Typeface getCommonTypeface(Context context, AssetManager assetManager) {
        if (sTypeface == null) {
            setCommonTypeface(context, assetManager);
        }
        return sTypeface;
    }

    public static void setCommonTypeface(Context context, AssetManager assetManager) {
        String commonFontName = CommonUtils.loadStringPreference(context, "font_setting", Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT);
        sTypeface = getTypeface(context, assetManager, commonFontName);
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
        Typeface typeface = StringUtils.isNotEmpty(customFontName) ? getTypeface(context, assetManager, customFontName) : getCommonTypeface(context, assetManager);
        for (TextView textView : targetViews) {
            textView.setTypeface(typeface);
        }
    }

    public static void setFontsTypeface(Context context, AssetManager assetManager, String customFontName, ViewGroup viewGroup) {
        Typeface typeface = StringUtils.isNotEmpty(customFontName) ? getTypeface(context, assetManager, customFontName) : getCommonTypeface(context, assetManager);
        determineView(viewGroup, typeface);
    }

    public static void determineView(ViewGroup viewGroup, Typeface typeface) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                determineView((ViewGroup)viewGroup.getChildAt(i), typeface);
            } else {
                if (viewGroup.getChildAt(i) instanceof TextView) {
                    TextView tv = (TextView) viewGroup.getChildAt(i);
                    tv.setTypeface(typeface);
                }
            }
        }
    }

    public static Typeface getTypeface(Context context, AssetManager assetManager, String fontName) {
        Typeface typeface = null;
        String[] assetsFonts = context.getResources().getStringArray(R.array.pref_list_fonts_values);
        String[] userFonts = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY).list();
        if (isValidTypeface(assetsFonts, fontName)) {
            if (StringUtils.equals(fontName, Constants.CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT)) {
                typeface = Typeface.DEFAULT;
            } else {
                typeface = Typeface.createFromAsset(assetManager, "fonts/" + fontName);
            }
        } else if (isValidTypeface(userFonts, fontName)) {
            typeface = Typeface.createFromFile(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.USER_CUSTOM_FONTS_DIRECTORY + fontName);
        } else {
            typeface = Typeface.DEFAULT;
        }
        return typeface;
    }

    public static boolean isValidTypeface(String[] fontArray, String fontName) {
        boolean result = false;
        if (fontArray != null) {
            for (String name : fontArray) {
                if (StringUtils.equalsIgnoreCase(name, fontName)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
