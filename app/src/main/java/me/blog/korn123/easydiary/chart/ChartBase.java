package me.blog.korn123.easydiary.chart;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import me.blog.korn123.easydiary.activities.EasyDiaryActivity;

/**
 * Created by CHO HANJOONG on 2017-03-23.
 */

public class ChartBase extends EasyDiaryActivity {
    protected String[] mMonths = new String[] {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"
    };

    protected String[] mParties = new String[] {
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I", "Party J", "Party K", "Party L", "Party M", "Party N", "Party O", "Party P",
            "Party Q", "Party R", "Party S", "Party T", "Party U", "Party V", "Party W", "Party X",
            "Party Y", "Party Z"
    };

    protected Typeface mTfLight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTfLight = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.ttf");
    }

    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

}
