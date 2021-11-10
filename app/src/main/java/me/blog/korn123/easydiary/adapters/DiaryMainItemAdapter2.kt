package me.blog.korn123.easydiary.adapters

import android.animation.ArgbEvaluator
import android.app.Activity
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity2
import me.blog.korn123.easydiary.databinding.ItemDiaryMainBinding
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.lang3.StringUtils

class DiaryMainItemAdapter2(
        val activity: Activity,
        private val diaryItems: List<Diary>,
        val itemClickCallback: (diary: Diary) -> Unit,
        val itemLongClickCallback: () -> Unit
) : RecyclerView.Adapter<DiaryMainItemAdapter2.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter {
    var currentQuery: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(activity, ItemDiaryMainBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(diaryItems[position])
    }

    override fun getItemCount(): Int = diaryItems.size

    override fun getSectionName(position: Int): String {
        val label = when (diaryItems[position].title?.isNotEmpty() ?: false) {
            true -> String.format("%d. %s", position + 1, diaryItems[position].title)
            false -> String.format("%d. %s", position + 1, diaryItems[position].contents)
        }
        return label
    }

    fun getSelectedItems(): List<Diary> {
        val selectedItems = arrayListOf<Diary>()
        diaryItems.map {
            if (it.isSelected) selectedItems.add(it)
        }
        return selectedItems
    }

    inner class ViewHolder(
            val activity: Activity, private val itemDiaryMainBinding: ItemDiaryMainBinding, val adapter: DiaryMainItemAdapter2
    ) : RecyclerView.ViewHolder(itemDiaryMainBinding.root) {
        fun bindTo(diary: Diary) {
            itemDiaryMainBinding.run {
                activity.run {
                    root.run {
                        setOnClickListener { itemClickCallback.invoke(diary) }
                        setOnLongClickListener {
                            itemLongClickCallback()
                            true
                        }
                        updateTextColors(this, 0, 0)
                        updateAppViews(this)
                        initTextSize(this)
                        updateCardViewPolicy(this)
                        FontUtils.setFontsTypeface(context, context.assets, null, this)
                    }

                    if (config.enableLocationInfo) {
                        diary.location?.let {
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

                            text = context.getString(R.string.diary_contents_length, diary.contents?.length ?: 0)
                        }
                        contentsLengthContainer.visibility = View.VISIBLE
                    } else {
                        contentsLengthContainer.visibility = View.GONE
                    }
                }

                selection.setOnCheckedChangeListener { _, isChecked ->
                    diary.isSelected = isChecked
                    EasyDiaryDbHelper.updateDiaryBy(diary)
                }

                when ((activity as DiaryMainActivity2).diaryMode) {
                    DiaryMode.READ -> selection.visibility = View.GONE
                    DiaryMode.DELETE -> {
                        selection.visibility = View.VISIBLE
                        selection.isChecked = diary.isSelected
                    }
                }

                if (StringUtils.isEmpty(diary.title)) {
                    textTitle.visibility = View.GONE
                } else {
                    textTitle.visibility = View.VISIBLE
                }
                textTitle.text = diary.title
                textContents.text = diary.contents

                // highlight current query
                if (StringUtils.isNotEmpty(currentQuery)) {
                    val color = ArgbEvaluator().evaluate(0.75F, 0x000000, 0xffffff) as Int
                    if (activity.config.diarySearchQueryCaseSensitive) {
                        EasyDiaryUtils.highlightString(textTitle, currentQuery)
                        EasyDiaryUtils.highlightString(textContents, currentQuery)
                    } else {
                        EasyDiaryUtils.highlightStringIgnoreCase(textTitle, currentQuery)
                        EasyDiaryUtils.highlightStringIgnoreCase(textContents, currentQuery)
                    }

                }
                EasyDiaryUtils.boldString(activity, textTitle)

                textDateTime.text = when (diary.isAllDay) {
                    true -> DateUtils.getFullPatternDate(diary.currentTimeMillis)
                    false -> DateUtils.getFullPatternDateWithTime(diary.currentTimeMillis)
                }
                if (activity.config.enableDebugMode) textDateTime.text = "[${diary.originSequence}] ${textDateTime.text}"
                FlavorUtils.initWeatherView(activity, imageSymbol, diary.weather)

                when (diary.photoUris?.size ?: 0 > 0) {
                    true -> {
                        photoViews.visibility = View.VISIBLE
                    }
                    false -> photoViews.visibility = View.GONE
                }

                photoViews.removeAllViews()
                if (diary.photoUris?.size ?: 0 > 0) {
                    val maxPhotos = CommonUtils.getDefaultDisplay(activity).x / CommonUtils.dpToPixel(activity, 40F)
                    diary.photoUris?.map {
                        val path = EasyDiaryUtils.getApplicationDataDirectory(activity) + it.getFilePath()
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
                        Glide.with(activity).load(path).listener(listener).apply(options).into(imageView)
//                    if (photoViews.childCount >= maxPhotos) return@map
                        photoViews.addView(imageView)
                    }
                }

                textContents.maxLines = when (activity.config.enableContentsSummary) {
                    true -> activity.config.summaryMaxLines
                    false -> Integer.MAX_VALUE
                }
            }
        }
    }
}