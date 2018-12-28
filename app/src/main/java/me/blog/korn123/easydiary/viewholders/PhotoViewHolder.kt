package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R

class PhotoViewHolder(
        itemView: View, 
        val activity: Activity,
        val itemCount: Int 
) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.photo)
    
    internal fun bindTo(photoPath: String) {
        val point =  CommonUtils.getDefaultDisplay(activity)
        val size = Math.floor(point.x  / itemCount.toDouble()).toInt()  
        imageView.layoutParams.width = size
        imageView.layoutParams.height = size
        
        Glide.with(imageView.context)
                .load(photoPath)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
//                .apply(RequestOptions().transform(RoundedCorners(20)))
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(CommonUtils.dpToPixel(imageView.context, 5F))))
                .into(imageView)
    }
}
