package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardViewerActivity
import java.text.SimpleDateFormat
import java.util.*

internal class PostcardViewHolder(itemView: View, val activity: Activity) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.imageview)
    private val imageContainer: ViewGroup = itemView.findViewById(R.id.imageContainer)
    
    internal fun bindTo(postCard: PostCardViewerActivity.PostCard) {
        val timeStampView = itemView.findViewById<TextView>(R.id.createdDate)
        timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, CommonUtils.dpToPixelFloatValue(activity, 10F))
        FontUtils.setFontsTypeface(activity, activity.assets, "", imageContainer)
        itemView.findViewById<CheckBox>(R.id.itemCheck).isChecked = postCard.isItemChecked
        try {
            val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            timeStampView.text = DateUtils.getFullPatternDate(format.parse(postCard.file.name.split("_")[0]).time)
        } catch (e: Exception) {
            timeStampView.text = "No information"
        }

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
