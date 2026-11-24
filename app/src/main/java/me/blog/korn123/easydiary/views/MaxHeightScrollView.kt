package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * A [ScrollView] that never measures taller than [maxContentHeight] pixels.
 *
 * A popup window sized to its content stays the size of the card wrapped around this view, while a
 * menu taller than the space left on screen is scrolled instead of running past the bottom edge
 * (landscape, large font). The bound is read at every measure pass, so items that become visible
 * later still keep the card inside the screen.
 */
class MaxHeightScrollView : ScrollView {
    var maxContentHeight: Int = Int.MAX_VALUE
        set(value) {
            field = value
            requestLayout()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
    ) {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        if (maxContentHeight == Int.MAX_VALUE || (mode != MeasureSpec.UNSPECIFIED && size <= maxContentHeight)) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val cappedHeightSpec =
            MeasureSpec.makeMeasureSpec(
                maxContentHeight,
                if (mode == MeasureSpec.UNSPECIFIED) MeasureSpec.AT_MOST else mode,
            )
        super.onMeasure(widthMeasureSpec, cappedHeightSpec)
    }
}
