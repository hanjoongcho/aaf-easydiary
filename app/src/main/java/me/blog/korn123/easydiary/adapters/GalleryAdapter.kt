package me.blog.korn123.easydiary.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Toolbar.LayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemGalleryBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.GalleryConstants
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL
import me.blog.korn123.easydiary.models.Diary
import java.io.File
import java.util.*

class GalleryAdapter(
    val activity: Activity,
    private val listPostcard: List<AttachedPhoto>,
    private val onItemClickListener: AdapterView.OnItemClickListener,
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>(),
    SectionedAdapter {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GalleryViewHolder = GalleryViewHolder(activity, ItemGalleryBinding.inflate(activity.layoutInflater, parent, false), this)

    override fun onBindViewHolder(
        holder: GalleryViewHolder,
        position: Int,
    ) {
        holder.bindTo(position)
    }

    override fun onViewRecycled(holder: GalleryViewHolder) {
        holder.recycle()
    }

    override fun getItemCount() = listPostcard.size

    @SuppressLint("DefaultLocale")
    override fun getSectionName(position: Int): String {
        val attachedPhoto = listPostcard[position]
        return attachedPhoto.diary?.let {
            DateUtils.getDateTimeStringForceFormatting(it.currentTimeMillis, activity)
        } ?: run { GalleryConstants.GUIDE_MESSAGE }
    }

    fun onItemHolderClick(itemHolder: GalleryViewHolder) {
        onItemClickListener.onItemClick(null, itemHolder.itemView, itemHolder.adapterPosition, itemHolder.itemId)
    }

    fun onItemCheckedChange(
        position: Int,
        isChecked: Boolean,
    ) {
        listPostcard[position].isItemChecked = isChecked
    }

    inner class GalleryViewHolder(
        val activity: Activity,
        private val itemGalleryBinding: ItemGalleryBinding,
        val adapter: GalleryAdapter,
    ) : RecyclerView.ViewHolder(itemGalleryBinding.root),
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
        init {
            itemGalleryBinding.run {
                activity.updateAppViews(root)
                FontUtils.setFontsTypeface(activity, null, imageContainer)
                root.setOnClickListener(this@GalleryViewHolder)
                checkItem.setOnCheckedChangeListener(this@GalleryViewHolder)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindTo(position: Int) {
            val attachedPhoto = listPostcard[position]
            val timeStampView = itemGalleryBinding.createdDate
            timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.dpToPixelFloatValue(10F))
            itemGalleryBinding.checkItem.isChecked = attachedPhoto.isItemChecked
            timeStampView.text = attachedPhoto.diary?.let {
                DateUtils.getDateTimeStringForceFormatting(it.currentTimeMillis, activity)
            } ?: run { GalleryConstants.GUIDE_MESSAGE }

            activity.run {
                val point = getDefaultDisplay()
                val spanCount = if (activity.isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait
                val targetX = point.x / spanCount
                itemGalleryBinding.imageContainer.layoutParams.height = targetX
                itemGalleryBinding.imageview.layoutParams.height = targetX
                itemGalleryBinding.imageview.layoutParams.width = targetX
                itemGalleryBinding.imageview.scaleType = ImageView.ScaleType.CENTER
                Glide
                    .with(itemGalleryBinding.imageview.context)
                    .load(if (attachedPhoto.diary?.isEncrypt == true) null else attachedPhoto.file)
                    .apply(createThumbnailGlideOptions(targetX * PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL, attachedPhoto.diary?.isEncrypt ?: false))
                    .into(itemGalleryBinding.imageview)
            }
        }

        fun recycle() {
            Glide.with(itemGalleryBinding.imageview.context).clear(itemGalleryBinding.imageview)
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(
            p0: CompoundButton,
            p1: Boolean,
        ) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }
    }

    data class AttachedPhoto(
        val file: File,
        var isItemChecked: Boolean,
        val diary: Diary?,
    )
}
