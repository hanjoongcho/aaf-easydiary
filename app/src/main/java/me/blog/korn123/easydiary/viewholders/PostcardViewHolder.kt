package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import java.io.File

internal class PostcardViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.imageview)

    internal fun bindTo(file: File) {
        val point =  CommonUtils.getDefaultDisplay(activity)
        val targetX = Math.floor((point.x - CommonUtils.dpToPixelFloatValue(imageView.context, 9)) / 2.0)
        imageView.layoutParams.height = targetX.toInt()
        Glide.with(imageView.context)
                .load(file)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
                .into(imageView)
    }
}
