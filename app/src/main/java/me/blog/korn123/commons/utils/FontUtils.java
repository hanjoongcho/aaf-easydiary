package me.blog.korn123.commons.utils;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.icu.text.DisplayContext;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

public class FontUtils {

    private static Typeface mTypeface;

    public static void setTypefaceDefault(TextView view) {
        view.setTypeface(Typeface.DEFAULT);
    }

    public static void setTypeface(AssetManager assetManager, TextView view) {
        view.setTypeface(getTypeface(assetManager));
    }

    public static Typeface getTypeface(AssetManager assetManager) {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(assetManager, "fonts/NanumPen.ttf");
        }
        return  mTypeface;
    }

    public static void setToolbarTypeface(Toolbar toolbar, AssetManager assetManager) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof  TextView) {
                FontUtils.setTypeface(assetManager, (TextView)view);
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
}
