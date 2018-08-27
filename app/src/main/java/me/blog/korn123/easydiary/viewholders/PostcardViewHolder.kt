package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.Glide
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardViewerActivity

internal class PostcardViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.imageview)
    private val imageContainer: ViewGroup = itemView.findViewById(R.id.imageContainer)
    
    internal fun bindTo(postCard: PostCardViewerActivity.PostCard) {
        itemView.findViewById<CheckBox>(R.id.itemCheck).isChecked = postCard.isItemChecked
        
        val point =  CommonUtils.getDefaultDisplay(activity)
        val targetX = Math.floor((point.x - CommonUtils.dpToPixelFloatValue(imageView.context, 9F)) / 2.0)
        imageContainer.layoutParams.height = targetX.toInt()
        imageView.layoutParams.height = targetX.toInt()
        Glide.with(imageView.context)
                .load(postCard.file)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
                .into(imageView)
    }
}
