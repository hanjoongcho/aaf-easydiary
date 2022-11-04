package me.blog.korn123.easydiary.adapters

import android.animation.ArgbEvaluator
import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.movement.MovementMethodPlugin
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.databinding.ItemDiaryMainBinding
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.lang3.StringUtils

class DiaryMainItemAdapter(
    val activity: Activity,
    private val diaryItems: List<Diary>,
    val itemClickCallback: (diary: Diary) -> Unit,
    val itemLongClickCallback: () -> Unit
) : RecyclerView.Adapter<DiaryMainItemAdapter.ViewHolder>(),
    FastScrollRecyclerView.SectionedAdapter {
    var currentQuery: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            activity,
            ItemDiaryMainBinding.inflate(activity.layoutInflater, parent, false),
            this
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(diaryItems[position])
    }

    override fun getItemCount(): Int = diaryItems.size

    override fun getSectionName(position: Int): String {
        fun ellipsis(str: String?): String {
            val maxLength = 15
            val ellipsisString = str?.let {
                when (str.length > maxLength) {
                    true -> str.take(maxLength).plus("…")
                    false -> str
                }
            } ?: ""
            return ellipsisString
        }

        val label = when (diaryItems[position].title?.isNotEmpty() ?: false) {
            true -> String.format("%s", ellipsis(diaryItems[position].title))
            false -> String.format("%s", ellipsis(diaryItems[position].contents))
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
        val activity: Activity,
        private val itemDiaryMainBinding: ItemDiaryMainBinding,
        val adapter: DiaryMainItemAdapter
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
                        FontUtils.setFontsTypeface(context, null, this)
                    }

                    if (config.enableLocationInfo) {
                        diary.location?.let {
                            changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
//                        locationLabel.setTextColor(config.textColor)
//                        locationContainer.background = getLabelBackground()

                            locationLabel.text = it.address
                            locationContainer.visibility = View.VISIBLE
                        } ?: run {
                            locationContainer.visibility = View.GONE
                        }
                    } else {
                        locationContainer.visibility = View.GONE
                    }

                    if (config.enableCountCharacters) {
                        contentsLength.run {
//                        setTextColor(config.textColor)
//                        background = getLabelBackground()

                            text = context.getString(
                                R.string.diary_contents_length,
                                diary.contents?.length ?: 0
                            )
                        }
                        contentsLengthContainer.visibility = View.VISIBLE
                    } else {
                        contentsLengthContainer.visibility = View.GONE
                    }
                }

                selection.setOnCheckedChangeListener { _, isChecked ->
                    EasyDiaryDbHelper.beginTransaction()
                    diary.isSelected = isChecked
                    EasyDiaryDbHelper.commitTransaction()
                    // EasyDiaryDbHelper.updateDiaryBy(diary)
                }

                when ((activity as DiaryMainActivity).mDiaryMode) {
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
                activity.applyMarkDownPolicy(textContents, diary.contents!!)

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
                    true -> DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                    false -> DateUtils.getDateTimeStringFromTimeMillis(diary.currentTimeMillis)
                }
                if (activity.config.enableDebugMode) textDateTime.text =
                    "[${diary.originSequence}] ${textDateTime.text}"
                FlavorUtils.initWeatherView(activity, imageSymbol, diary.weather)

                when ((diary.photoUris?.size ?: 0) > 0) {
                    true -> {
                        photoViews.visibility = View.VISIBLE
                    }

                    false -> photoViews.visibility = View.GONE
                }

                photoViews.removeAllViews()
                if ((diary.photoUris?.size ?: 0) > 0) {
                    diary.photoUrisWithEncryptionPolicy()?.map {
                        val imageXY = activity.dpToPixel(32F)
                        val imageView = ImageView(activity)
                        val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
                        layoutParams.setMargins(
                            0,
                            activity.dpToPixel(1F),
                            activity.dpToPixel(3F),
                            0
                        )
                        imageView.layoutParams = layoutParams
                        imageView.background = createBackgroundGradientDrawable(
                            activity.config.primaryColor,
                            THUMBNAIL_BACKGROUND_ALPHA,
                            imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
                        )
                        imageView.scaleType = ImageView.ScaleType.CENTER
                        activity.dpToPixel(1.5F, Calculation.FLOOR).apply {
                            imageView.setPadding(this, this, this, this)
                        }
                        val listener = object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                        }
                        Glide.with(activity)
                            .load(EasyDiaryUtils.getApplicationDataDirectory(activity) + it.getFilePath())
                            .listener(listener)
                            .apply(
                                createThumbnailGlideOptions(
                                    imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                                    it.isEncrypt()
                                )
                            )
                            .into(imageView)
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