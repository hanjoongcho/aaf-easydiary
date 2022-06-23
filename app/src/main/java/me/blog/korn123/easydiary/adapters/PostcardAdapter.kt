package me.blog.korn123.easydiary.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemPostCardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PostcardAdapter(
        val activity: Activity,
        private val listPostcard: List<PostCard>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<PostcardAdapter.PostcardViewHolder>(), SectionedAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        return PostcardViewHolder(activity, ItemPostCardBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
        holder.bindTo(listPostcard[position])
    }

    override fun getItemCount() = listPostcard.size

    @SuppressLint("DefaultLocale")
    @NonNull
    override fun getSectionName(position: Int): String {
        return String.format("%d. %s", position + 1, listPostcard[position].file.name)
    }

    fun onItemHolderClick(itemHolder: PostcardViewHolder) {
        onItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
    }

    fun onItemCheckedChange(position: Int, isChecked: Boolean) {
        listPostcard[position].isItemChecked = isChecked
    }

    class PostcardViewHolder(
            val activity: Activity, private val itemPostCardBinding: ItemPostCardBinding, val adapter: PostcardAdapter
    ) : RecyclerView.ViewHolder(itemPostCardBinding.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            itemPostCardBinding.run {
                activity.updateAppViews(root)
                FontUtils.setFontsTypeface(activity, null, imageContainer)
                root.setOnClickListener(this@PostcardViewHolder)
                checkItem.setOnCheckedChangeListener(this@PostcardViewHolder)
            }
        }

        fun bindTo(postCard: PostCard) {
            val timeStampView = itemPostCardBinding.createdDate
            timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.dpToPixelFloatValue(10F))
            itemPostCardBinding.checkItem.isChecked = postCard.isItemChecked
            try {
                val format = SimpleDateFormat(POSTCARD_DATE_FORMAT, Locale.getDefault())
                timeStampView.text = DateUtils.getDateStringFromTimeMillis(format.parse(postCard.file.name.split("_")[0]).time)
            } catch (e: Exception) {
                timeStampView.text = GUIDE_MESSAGE
            }

            activity.run {
                val point =  getDefaultDisplay()
                val spanCount = if (activity.isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait
                val targetX = point.x / spanCount
                itemPostCardBinding.imageContainer.layoutParams.height = targetX
                itemPostCardBinding.imageview.layoutParams.height = targetX
                itemPostCardBinding.imageview.scaleType = ImageView.ScaleType.CENTER
                Glide.with(itemPostCardBinding.imageview.context)
                        .load(postCard.file)
                        .apply(createThumbnailGlideOptions(targetX * PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL))
                        .into(itemPostCardBinding.imageview)
            }
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }
    }

    data class PostCard(val file: File, var isItemChecked: Boolean)

    companion object {
        const val GUIDE_MESSAGE = "No information"
        const val POSTCARD_DATE_FORMAT = "yyyyMMddHHmmss"
    }
}
