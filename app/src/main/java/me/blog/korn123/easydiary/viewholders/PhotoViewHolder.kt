package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import me.blog.korn123.easydiary.R

class PhotoViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.photo)
    
    internal fun bindTo(photoPath: String) {
        Glide.with(imageView.context)
                .load(photoPath)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
                .into(imageView)
    }
}
