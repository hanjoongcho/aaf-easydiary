package me.blog.korn123.easydiary.helper

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import me.blog.korn123.commons.utils.FlavorUtils

object BindingAdapter {
    @BindingAdapter("symbolId")
    @JvmStatic
    fun bindSrcCompat(imageView: ImageView, symbolId: Int) {
        imageView.setImageResource(FlavorUtils.sequenceToSymbolResourceId(symbolId))
        android.util.Log.i(AAF_TEST, "symbolId: $symbolId, imageView: $imageView")
    }
}