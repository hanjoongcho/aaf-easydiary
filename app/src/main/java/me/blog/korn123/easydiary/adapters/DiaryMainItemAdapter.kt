package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
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
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.databinding.ItemDiaryMainBinding
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
        layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(activity, layoutResourceId, list) {
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: run {
            val binding = ItemDiaryMainBinding.inflate(activity.layoutInflater)
            binding.root.apply {
                tag = binding
                context.updateTextColors(this, 0, 0)
                context.updateAppViews(this)
                context.initTextSize(this)
                context.updateCardViewPolicy(this)
                FontUtils.setFontsTypeface(context, context.assets, null, this)
            }
        }

        (itemView.tag as ItemDiaryMainBinding).run {
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
                textTitle.visibility = View.GONE
            } else {
                textTitle.visibility = View.VISIBLE
            }
            textTitle.text = diaryDto.title
            textContents.text = diaryDto.contents

            // highlight current query
            if (StringUtils.isNotEmpty(currentQuery)) {
                if (context.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(textTitle, currentQuery)
                    EasyDiaryUtils.highlightString(textContents, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(textTitle, currentQuery)
                    EasyDiaryUtils.highlightStringIgnoreCase(textContents, currentQuery)
                }

            }
            EasyDiaryUtils.boldString(context, textTitle)

            textDateTime.text = when (diaryDto.isAllDay) {
                true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
            }
            FlavorUtils.initWeatherView(context, imageSymbol, diaryDto.weather)

            when (diaryDto.photoUris?.size ?: 0 > 0) {
                true -> {
                    photoViews.visibility = View.VISIBLE
                }
                false -> photoViews.visibility = View.GONE
            }

            photoViews.removeAllViews()
            if (diaryDto.photoUris?.size ?: 0 > 0) {
                val maxPhotos = CommonUtils.getDefaultDisplay(activity).x / CommonUtils.dpToPixel(activity, 40F)
                diaryDto.photoUris?.map {
                    val path = EasyDiaryUtils.getApplicationDataDirectory(context) + it.getFilePath()
                    val imageView = ImageView(activity)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(activity, 28F), CommonUtils.dpToPixel(activity, 28F))
                    layoutParams.setMargins(0, CommonUtils.dpToPixel(activity, 1F), CommonUtils.dpToPixel(activity, 3F), 0)
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
//                    if (photoViews.childCount >= maxPhotos) return@map
                    photoViews.addView(imageView)
                }
            }

            textContents.maxLines = when (activity.config.enableContentsSummary) {
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
}
