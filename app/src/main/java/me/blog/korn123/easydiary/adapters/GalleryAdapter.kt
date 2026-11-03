package me.blog.korn123.easydiary.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import me.blog.korn123.easydiary.databinding.ItemGalleryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixelFloatValue
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.helper.GalleryConstants
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

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
        private val binding: ItemGalleryBinding,
        val adapter: GalleryAdapter,
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
        init {
            binding.run {
                activity.updateAppViews(root)
                FontUtils.setFontsTypeface(activity, null, imageContainer)
                root.setOnClickListener(this@GalleryViewHolder)
                checkItem.setOnCheckedChangeListener(this@GalleryViewHolder)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bindTo(position: Int) {
            val attachedPhoto = listPostcard[position]
            val timeStampView = binding.createdDate
            timeStampView.setTextSize(TypedValue.COMPLEX_UNIT_PX, activity.dpToPixelFloatValue(10F))
            binding.checkItem.isChecked = attachedPhoto.isItemChecked
            timeStampView.text = attachedPhoto.diary?.let {
                DateUtils.getDateTimeStringForceFormatting(it.currentTimeMillis, activity)
            } ?: run { GalleryConstants.GUIDE_MESSAGE }

            activity.run {
                val point = getDefaultDisplay()
                val spanCount = if (activity.isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait
                val targetX = point.x / spanCount
                binding.imageContainer.layoutParams.height = targetX
                binding.imageview.layoutParams.height = targetX
                binding.imageview.layoutParams.width = targetX
                binding.imageview.scaleType = ImageView.ScaleType.CENTER
                Glide
                    .with(binding.imageview.context)
                    .load(if (attachedPhoto.diary?.isEncrypt == true) null else attachedPhoto.file)
                    .apply(createThumbnailGlideOptions(targetX * PHOTO_CORNER_RADIUS_SCALE_FACTOR_SMALL, attachedPhoto.diary?.isEncrypt ?: false))
                    .into(binding.imageview)
            }
        }

        fun recycle() {
            Glide.with(binding.imageview.context).clear(binding.imageview)
        }

        override fun onClick(v: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(
            buttonView: CompoundButton,
            isChecked: Boolean,
        ) {
            adapter.onItemCheckedChange(this.adapterPosition, isChecked)
        }
    }

    data class AttachedPhoto(
        val file: File,
        var isItemChecked: Boolean,
        val diary: DiaryDomain?,
    )
}
