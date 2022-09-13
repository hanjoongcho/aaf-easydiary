package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemDiaryDashboardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.models.Diary

class DiaryDashboardItemAdapter : BaseBannerAdapter<Diary>() {
    var currentQuery: String? = null

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(activity, ItemDiaryDashboardBinding.inflate(activity.layoutInflater, parent, false), this)
//    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bindTo(diaryItems[position])
//    }
//
//    override fun getItemCount(): Int = diaryItems.size

    inner class ViewHolder(
            val activity: Activity, private val itemDiaryDashboardBinding: ItemDiaryDashboardBinding, val adapter: DiaryDashboardItemAdapter
    ) : RecyclerView.ViewHolder(itemDiaryDashboardBinding.root) {
//        fun bindTo(diary: Diary) {
//            itemDiaryDashboardBinding.run {
//                activity.run {
//                    root.run {
//                        setOnClickListener { itemClickCallback.invoke(diary) }
//                        setOnLongClickListener {
//                            itemLongClickCallback()
//                            true
//                        }
//                        updateTextColors(this, 0, 0)
//                        updateAppViews(this)
//                        initTextSize(this)
//                        updateCardViewPolicy(this)
//                        FontUtils.setFontsTypeface(context, null, this)
//                    }
//
//                    if (config.enableLocationInfo) {
//                        diary.location?.let {
//                            changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
////                        locationLabel.setTextColor(config.textColor)
////                        locationContainer.background = getLabelBackground()
//
//                            locationLabel.text = it.address
//                            locationContainer.visibility = View.VISIBLE
//                        } ?: { locationContainer.visibility = View.GONE } ()
//                    } else {
//                        locationContainer.visibility = View.GONE
//                    }
//
//                    if (config.enableCountCharacters) {
//                        contentsLength.run {
////                        setTextColor(config.textColor)
////                        background = getLabelBackground()
//
//                            text = context.getString(R.string.diary_contents_length, diary.contents?.length ?: 0)
//                        }
//                        contentsLengthContainer.visibility = View.VISIBLE
//                    } else {
//                        contentsLengthContainer.visibility = View.GONE
//                    }
//                }
//
////                textContents.text = if (diary.contents?.length ?: 0 > 10) "${diary.contents?.substring(0, 10)}..." else diary.contents
//                textContents.text = diary.contents
//                textDateTime.text = when (diary.isAllDay) {
//                    true -> DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
//                    false -> DateUtils.getDateTimeStringFromTimeMillis(diary.currentTimeMillis)
//                }
//                FlavorUtils.initWeatherView(activity, imageSymbol, diary.weather)
//
//                when (diary.photoUris?.size ?: 0 > 0) {
//                    true -> {
//                        photoViews.visibility = View.VISIBLE
//                    }
//                    false -> photoViews.visibility = View.GONE
//                }
//
//                photoViews.removeAllViews()
//                if (diary.photoUris?.size ?: 0 > 0) {
//                    diary.photoUrisWithEncryptionPolicy()?.map {
//                        val imageXY = activity.dpToPixel(32F)
//                        val imageView = ImageView(activity)
//                        val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
//                        layoutParams.setMargins(0, activity.dpToPixel(1F), activity.dpToPixel(3F), 0)
//                        imageView.layoutParams = layoutParams
//                        imageView.background = createBackgroundGradientDrawable(activity.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL)
//                        imageView.scaleType = ImageView.ScaleType.CENTER
//                        activity.dpToPixel(1.5F, Calculation.FLOOR).apply {
//                            imageView.setPadding(this, this, this, this)
//                        }
//                        val listener = object : RequestListener<Drawable> {
//                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                                return false
//                            }
//
//                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                                return false
//                            }
//                        }
//                        Glide.with(activity).load(EasyDiaryUtils.getApplicationDataDirectory(activity) + it.getFilePath())
//                            .listener(listener)
//                            .apply(createThumbnailGlideOptions(imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL, it.isEncrypt()))
//                            .into(imageView)
////                    if (photoViews.childCount >= maxPhotos) return@map
//                        photoViews.addView(imageView)
//                    }
//                }
//
//                textContents.maxLines = when (activity.config.enableContentsSummary) {
//                    true -> activity.config.summaryMaxLines
//                    false -> Integer.MAX_VALUE
//                }
//            }
//        }
    }

    override fun bindData(
        holder: BaseViewHolder<Diary>,
        diary: Diary,
        position: Int,
        pageSize: Int
    ) {
        val context = holder.itemView.context
        val binding = ItemDiaryDashboardBinding.bind(holder.itemView)

        binding.root.run {
            context.updateTextColors(this, 0, 0)
            context.updateAppViews(this)
            context.initTextSize(this)
            context.updateCardViewPolicy(this)
            FontUtils.setFontsTypeface(context, null, this)
        }
        binding.run {
            textContents.text = diary.contents
            textDateTime.text = when (diary.isAllDay) {
                true -> DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                false -> DateUtils.getDateTimeStringFromTimeMillis(diary.currentTimeMillis)
            }

            context.run {
                if (config.enableLocationInfo) {
                    diary.location?.let {
                        changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
                        locationLabel.text = it.address
                        locationContainer.visibility = View.VISIBLE
                    } ?: { locationContainer.visibility = View.GONE } ()
                } else {
                    locationContainer.visibility = View.GONE
                }

                if (config.enableCountCharacters) {
                    contentsLength.run {
                        text = context.getString(R.string.diary_contents_length, diary.contents?.length ?: 0)
                    }
                    contentsLengthContainer.visibility = View.VISIBLE
                } else {
                    contentsLengthContainer.visibility = View.GONE
                }

                FlavorUtils.initWeatherView(this, imageSymbol, diary.weather)

                textContents.maxLines = when (config.enableContentsSummary) {
                    true -> config.summaryMaxLines
                    false -> Integer.MAX_VALUE
                }
            }
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_diary_dashboard
    }

}