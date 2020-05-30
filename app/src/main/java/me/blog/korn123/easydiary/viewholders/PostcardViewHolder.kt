package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.viewholder_post_card.view.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardViewerActivity
import me.blog.korn123.easydiary.extensions.updateAppViews
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

internal class PostcardViewHolder(itemView: View, val activity: Activity) : ViewHolder(itemView) {
    fun bindTo(postCard: PostCardViewerActivity.PostCard) {
        if (itemView is ViewGroup) activity.updateAppViews(itemView)

        val timeStampView = itemView.createdDate
        timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, CommonUtils.dpToPixelFloatValue(activity, 10F))
        FontUtils.setFontsTypeface(activity, activity.assets, "", itemView.imageContainer)
        itemView.itemCheck.isChecked = postCard.isItemChecked
        try {
            val format = SimpleDateFormat(POSTCARD_DATE_FORMAT, Locale.getDefault())
            timeStampView.text = DateUtils.getFullPatternDate(format.parse(postCard.file.name.split("_")[0]).time)
        } catch (e: Exception) {
            timeStampView.text = GUIDE_MESSAGE
        }

        val point =  CommonUtils.getDefaultDisplay(activity)
        val targetX = floor((point.x - CommonUtils.dpToPixelFloatValue(itemView.imageview.context, 9F)) / 2.0)
        itemView.imageContainer.layoutParams.height = targetX.toInt()
        itemView.imageview.layoutParams.height = targetX.toInt()
        Glide.with(itemView.imageview.context)
                .load(postCard.file)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
                .into(itemView.imageview)
    }

    companion object {
        const val GUIDE_MESSAGE = "No information"
        const val POSTCARD_DATE_FORMAT = "yyyyMMddHHmmss"
    }
}
