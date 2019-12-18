package me.blog.korn123.easydiary.views

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

class CompatPaddingCardView : FixedCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}