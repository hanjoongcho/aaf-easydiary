package me.blog.korn123.commons.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {

    private static Typeface mTypeface;

    public static void setTypeface(AssetManager assetManager, TextView view) {
        view.setTypeface(getTypeface(assetManager));
    }

    public static Typeface getTypeface(AssetManager assetManager) {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(assetManager, "fonts/NanumPen.ttf");
        }
        return  mTypeface;
    }
}
