package me.blog.korn123.easydiary.chart

import android.graphics.Typeface
import android.os.Bundle

import me.blog.korn123.easydiary.activities.EasyDiaryActivity

/**
 * Created by CHO HANJOONG on 2017-03-23.
 */

open class ChartBase : EasyDiaryActivity() {
    protected var mTfLight: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTfLight = Typeface.createFromAsset(assets, "fonts/OpenSans-Light.ttf")
    }
}
