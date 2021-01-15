package me.blog.korn123.easydiary.helper

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import me.blog.korn123.commons.utils.FlavorUtils

object BindingAdapter {
    @BindingAdapter("symbolSequence")
    @JvmStatic
    fun bindSrcCompat(imageView: ImageView, symbolSequence: Int) {
        imageView.setImageResource(FlavorUtils.sequenceToSymbolResourceId(symbolSequence))
    }
}