package me.blog.korn123.commons.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

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
        String commonFontName = CommonUtils.INSTANCE.loadStringPreference(context, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT);
        sTypeface = getTypeface(context, assetManager, commonFontName);
    }

    public static void setFontsTypeface(Context context, AssetManager assetManager, String customFontName, TextView... targetViews) {
        Typeface typeface = StringUtils.isNotEmpty(customFontName) ? getTypeface(context, assetManager, customFontName) : getCommonTypeface(context, assetManager);
        for (TextView textView : targetViews) {
            textView.setTypeface(typeface);
        }
    }

    public static void setFontsTypeface(Context context, AssetManager assetManager, String customFontName, ViewGroup rootView) {
        Typeface typeface = StringUtils.isNotEmpty(customFontName) ? getTypeface(context, assetManager, customFontName) : getCommonTypeface(context, assetManager);
        setTypeface(rootView, typeface);
    }

    public static void setTypeface(ViewGroup viewGroup, Typeface typeface) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                setTypeface((ViewGroup)viewGroup.getChildAt(i), typeface);
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
        String[] userFonts = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.INSTANCE.getUSER_CUSTOM_FONTS_DIRECTORY()).list();
        if (isValidTypeface(assetsFonts, fontName)) {
            if (StringUtils.equals(fontName, Constants.CUSTOM_FONTS_UNSUPPORTED_LANGUAGE_DEFAULT)) {
                typeface = Typeface.DEFAULT;
            } else {
                typeface = Typeface.createFromAsset(assetManager, "fonts/" + fontName);
            }
        } else if (isValidTypeface(userFonts, fontName)) {
            typeface = Typeface.createFromFile(Environment.getExternalStorageDirectory().getAbsolutePath() + Path.INSTANCE.getUSER_CUSTOM_FONTS_DIRECTORY() + fontName);
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

    public static String fontFileNameToDisplayName(Context context, String fontFileName) {
        String displayName = null;
        String[] fontNames = context.getResources().getStringArray(R.array.pref_list_fonts_values);
        String[] displayNames = context.getResources().getStringArray(R.array.pref_list_fonts_title);
        for (int i = 0; i < fontNames.length; i++) {
            if (StringUtils.equals(fontFileName, fontNames[i])) {
                displayName = displayNames[i];
                break;
            }
        }

        if (displayName == null) displayName = FilenameUtils.getBaseName(fontFileName);
        return displayName;
    }
}
