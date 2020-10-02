package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.item_diary_main.view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-26.
 *
 */

class DiaryMainItemAdapter(
        private val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(activity, layoutResourceId, list) {
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(
                        itemView.photoContainer, itemView.photoViews,
                        itemView.text1, itemView.text2, itemView.text3,
                        itemView.contentsLength, itemView.weather, itemView.item_holder,
                        itemView.selection, itemView.locationSymbol, itemView.locationLabel,
                        itemView.locationContainer, itemView.contentsLengthContainer
                )
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            val diaryDto = list[position]
            activity.run {
                if (config.enableLocationInfo) {
                    diaryDto.location?.let {
                        changeDrawableIconColor(config.primaryColor, R.drawable.map_marker_2)
//                        locationLabel.setTextColor(config.textColor)
//                        locationContainer.background = getLabelBackground()

                        locationLabel.text = it.address
                        locationContainer.visibility = View.VISIBLE
                    } ?: { locationContainer.visibility = View.GONE } ()
                } else {
                    locationContainer.visibility = View.GONE
                }

                if (config.enableCountCharacters) {
                    contentsLength.run {
//                        setTextColor(config.textColor)
//                        background = getLabelBackground()

                        text = context.getString(R.string.diary_contents_length, diaryDto.contents?.length ?: 0)
                    }
                    contentsLengthContainer.visibility = View.VISIBLE
                } else {
                    contentsLengthContainer.visibility = View.GONE
                }
            }

            selection.setOnCheckedChangeListener { _, isChecked ->
                diaryDto.isSelected = isChecked
                EasyDiaryDbHelper.updateDiary(diaryDto)
            }

            when ((activity as DiaryMainActivity).mDiaryMode) {
                DiaryMode.READ -> selection.visibility = View.GONE
                DiaryMode.DELETE -> {
                    selection.visibility = View.VISIBLE
                    selection.isChecked = diaryDto.isSelected
                }
            }

            if (StringUtils.isEmpty(diaryDto.title)) {
                textView1.visibility = View.GONE
            } else {
                textView1.visibility = View.VISIBLE
            }
            textView1.text = diaryDto.title
            textView2.text = diaryDto.contents

            // highlight current query
            if (StringUtils.isNotEmpty(currentQuery)) {
                if (context.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(textView1, currentQuery)
                    EasyDiaryUtils.highlightString(textView2, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(textView1, currentQuery)
                    EasyDiaryUtils.highlightStringIgnoreCase(textView2, currentQuery)
                }

            }
            EasyDiaryUtils.boldString(context, textView1)

            textView3.text = when (diaryDto.isAllDay) {
                true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
            }
            FlavorUtils.initWeatherView(context, imageView, diaryDto.weather)

            item_holder.let {
                context.updateTextColors(it, 0, 0)
                context.updateAppViews(it)
                context.initTextSize(it)
                context.updateCardViewPolicy(it)
            }

            when (diaryDto.photoUris?.size ?: 0 > 0) {
                true -> {
                    photoContainer.visibility = View.VISIBLE
                }
                false -> photoContainer.visibility = View.GONE
            }
            FontUtils.setFontsTypeface(context, context.assets, null, item_holder)
            photoViews.removeAllViews()
            if (diaryDto.photoUris?.size ?: 0 > 0) {
                val maxPhotos = CommonUtils.getDefaultDisplay(activity).x / CommonUtils.dpToPixel(activity, 40F)
                diaryDto.photoUris?.map {
                    val path = EasyDiaryUtils.getApplicationDataDirectory(context) + it.getFilePath()
                    val imageView = ImageView(activity)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(activity, 28F), CommonUtils.dpToPixel(activity, 28F))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(activity, 3F), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = ContextCompat.getDrawable(activity, R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(activity.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    CommonUtils.dpToPixel(activity, 1.5F, CALCULATION.FLOOR).apply {
                        imageView.setPadding(this, this, this, this)
                    }
                    val listener = object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            return false
                        }
                    }
                    val options = RequestOptions()
                            /*.error(R.drawable.error_7)*/
                            .placeholder(R.drawable.error_7)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .priority(Priority.HIGH)
                            .centerCrop()
                    Glide.with(context).load(path).listener(listener).apply(options).into(imageView)
                    if (photoViews.childCount >= maxPhotos) return@map
                    photoViews.addView(imageView)
                }
            }

            textView2.maxLines = when (activity.config.enableContentsSummary) {
                true -> activity.config.summaryMaxLines
                false -> Integer.MAX_VALUE
            }
        }

        return itemView
    }

    fun getSelectedItems(): List<DiaryDto> {
        val selectedItems = arrayListOf<DiaryDto>()
        list.map {
            if (it.isSelected) selectedItems.add(it)
        }
        return selectedItems
    }

    private class ViewHolder(
            val photoContainer: RelativeLayout, val photoViews: LinearLayout,
            val textView1: TextView, val textView2: TextView, val textView3: TextView,
            val contentsLength: TextView, val imageView: ImageView, val item_holder: ViewGroup,
            val selection: CheckBox, val locationSymbol:ImageView, val locationLabel: TextView,
            val locationContainer: me.blog.korn123.easydiary.views.FixedCardView, val contentsLengthContainer: me.blog.korn123.easydiary.views.FixedCardView
    )
}
