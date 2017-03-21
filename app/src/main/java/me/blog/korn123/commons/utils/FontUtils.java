package me.blog.korn123.commons.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {
    public static void setTypeface(AssetManager assetManager, TextView view)
    {
        Typeface font = Typeface.createFromAsset(assetManager, "fonts/NanumPen.ttf");
        view.setTypeface(font);
    }
}
