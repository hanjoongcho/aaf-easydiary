package me.blog.korn123.easydiary.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by CHO HANJOONG on 2017-12-19.
 */

public class LabelLayout extends LinearLayout {
    public LabelLayout(Context context) {
        super(context);
    }

    public LabelLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setColors(int textColor, int accentColor, int backgroundColor) {
        setBackgroundColor(accentColor);
    }
}
