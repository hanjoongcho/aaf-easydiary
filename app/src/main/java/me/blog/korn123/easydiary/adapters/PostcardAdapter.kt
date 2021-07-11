package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ViewholderPostCardBinding
import me.blog.korn123.easydiary.extensions.updateAppViews
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

internal class PostcardAdapter(
        val activity: Activity,
        private val listPostcard: List<PostCard>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<PostcardAdapter.PostcardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        return PostcardViewHolder(activity, ViewholderPostCardBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
        holder.bindTo(listPostcard[position])
    }

    override fun getItemCount() = listPostcard.size

    fun onItemHolderClick(itemHolder: PostcardViewHolder) {
        onItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
    }

    fun onItemCheckedChange(position: Int, isChecked: Boolean) {
        listPostcard[position].isItemChecked = isChecked
    }

    class PostcardViewHolder(
            val activity: Activity, private val viewHolderPostCardBinding: ViewholderPostCardBinding, val adapter: PostcardAdapter
    ) : RecyclerView.ViewHolder(viewHolderPostCardBinding.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            viewHolderPostCardBinding.run {
                activity.updateAppViews(root)
                FontUtils.setFontsTypeface(activity, activity.assets, null, imageContainer)
                root.setOnClickListener(this@PostcardViewHolder)
                checkItem.setOnCheckedChangeListener(this@PostcardViewHolder)
            }
        }

        fun bindTo(postCard: PostCard) {
            val timeStampView = viewHolderPostCardBinding.createdDate
            timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, CommonUtils.dpToPixelFloatValue(activity, 10F))
            viewHolderPostCardBinding.checkItem.isChecked = postCard.isItemChecked
            try {
                val format = SimpleDateFormat(POSTCARD_DATE_FORMAT, Locale.getDefault())
                timeStampView.text = DateUtils.getFullPatternDate(format.parse(postCard.file.name.split("_")[0]).time)
            } catch (e: Exception) {
                timeStampView.text = GUIDE_MESSAGE
            }

            val point =  CommonUtils.getDefaultDisplay(activity)
            val targetX = floor((point.x - CommonUtils.dpToPixelFloatValue(viewHolderPostCardBinding.imageview.context, 9F)) / 2.0)
            viewHolderPostCardBinding.imageContainer.layoutParams.height = targetX.toInt()
            viewHolderPostCardBinding.imageview.layoutParams.height = targetX.toInt()
            Glide.with(viewHolderPostCardBinding.imageview.context)
                    .load(postCard.file)
//                .apply(RequestOptions().placeholder(R.drawable.ic_aaf_photos).fitCenter())
                    .into(viewHolderPostCardBinding.imageview)
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }

        companion object {
            const val GUIDE_MESSAGE = "No information"
            const val POSTCARD_DATE_FORMAT = "yyyyMMddHHmmss"
        }
    }

    data class PostCard(val file: File, var isItemChecked: Boolean)
}
