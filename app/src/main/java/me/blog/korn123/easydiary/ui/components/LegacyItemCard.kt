package me.blog.korn123.easydiary.ui.components

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemDiaryMainMigBinding
import me.blog.korn123.easydiary.databinding.ItemDiarySubBinding
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.ComposeConstants.VERTICAL_PADDING
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.ui.models.DiaryUiModel
import org.apache.commons.lang3.StringUtils

@SuppressLint("SetTextI18n")
@Composable
fun LegacyDiaryItemCard(
    diary: Diary,
    itemClickCallback: (diary: Diary) -> Unit,
    itemLongClickCallback: () -> Unit,
) {
    AndroidView(
        modifier =
            Modifier
                .fillMaxWidth(),
        factory = { ctx ->
            val activity = ctx as Activity
            val currentQuery = ""
            val binding =
                ItemDiaryMainMigBinding.inflate(LayoutInflater.from(ctx)).apply {
                    if (diary.currentTimeMillis > System.currentTimeMillis()) {
                        viewFutureDiaryBadge.visibility = View.VISIBLE
                        cardFutureDiaryBadge.visibility = View.VISIBLE
                        textDDayCount.text = DateUtils.getOnlyDayRemaining(diary.currentTimeMillis)
                    } else {
                        viewFutureDiaryBadge.visibility = View.GONE
                        cardFutureDiaryBadge.visibility = View.GONE
                    }

                    activity.run {
                        root.run {
                            setOnClickListener { itemClickCallback(diary) }
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
                                changeDrawableIconColor(
                                    config.primaryColor,
                                    R.drawable.ic_map_marker_2,
                                )

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
                                text =
                                    context.getString(
                                        R.string.diary_contents_length,
                                        diary.contents?.length ?: 0,
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

//                                            when ((activity as DiaryMainActivity).mDiaryMode) {
//                                                DiaryMode.READ -> selection.visibility = View.GONE
//                                                DiaryMode.DELETE -> {
//                                                    selection.visibility = View.VISIBLE
//                                                    selection.isChecked = diary.isSelected
//                                                }
//                                            }
                    selection.visibility = View.GONE

                    if (StringUtils.isEmpty(diary.title)) {
                        textTitle.visibility = View.GONE
                    } else {
                        textTitle.visibility = View.VISIBLE
                    }
                    textTitle.text = diary.title

                    activity.applyMarkDownPolicy(
                        textContents,
                        diary.contents!!,
                        false,
                        arrayListOf(),
                        true,
                    )
                    if (activity.config.enableMarkdown) {
                        textContents.tag = diary.sequence
                        EasyDiaryUtils.applyMarkDownEllipsize(textContents, diary.sequence, 500)
                    }

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

                    textDateTime.text =
                        when (diary.isAllDay) {
                            true -> {
                                DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                            }

                            false -> {
                                DateUtils.getDateTimeStringForceFormatting(
                                    diary.currentTimeMillis,
                                    activity,
                                )
                            }
                        }
                    if (activity.config.enableDebugOptionVisibleDiarySequence) {
                        textDateTime.text =
                            "[${diary.sequence}, ${diary.originSequence}] ${textDateTime.text}"
                    }
                    FlavorUtils.initWeatherView(activity, imageSymbol, diary.weather)

                    when ((diary.photoUris?.size ?: 0) > 0) {
                        true -> {
                            photoViews.visibility = View.VISIBLE
                        }

                        false -> {
                            photoViews.visibility = View.GONE
                        }
                    }

                    photoViews.removeAllViews()
                    if ((diary.photoUris?.size ?: 0) > 0) {
                        diary.photoUrisWithEncryptionPolicy()?.map {
                            val imageXY = activity.dpToPixel(32F)
                            val imageView = ImageView(activity)
                            val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
                            imageView.layoutParams = layoutParams
                            imageView.scaleType = ImageView.ScaleType.CENTER
                            val listener =
                                object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable?>,
                                        isFirstResource: Boolean,
                                    ): Boolean = false

                                    override fun onResourceReady(
                                        resource: Drawable,
                                        model: Any,
                                        target: Target<Drawable?>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean,
                                    ): Boolean = false
                                }
                            Glide
                                .with(activity)
                                .load(EasyDiaryUtils.getApplicationDataDirectory(activity) + it.getFilePath())
                                .listener(listener)
                                .apply(
                                    createThumbnailGlideOptions(
                                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                                        it.isEncrypt(),
                                    ),
                                ).into(imageView)

                            val margin = activity.dpToPixel(3F)
                            val contentPadding = activity.dpToPixel(1F)
                            val cardView =
                                me.blog.korn123.easydiary.views.FixedCardView(activity).apply {
                                    activity.updateDashboardInnerCard(this)
                                    setLayoutParams(
                                        ViewGroup
                                            .MarginLayoutParams(
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ).apply {
                                            },
                                    )

                                    radius = imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
                                    fixedAppcompatPadding = true
                                    setContentPadding(
                                        contentPadding,
                                        contentPadding,
                                        contentPadding,
                                        contentPadding,
                                    )
                                    addView(imageView)
                                }
                            photoViews.addView(cardView)
                        }
                    }

                    textContents.maxLines =
                        when (activity.config.enableContentsSummary) {
                            true -> activity.config.summaryMaxLines
                            false -> Integer.MAX_VALUE
                        }
                }

            binding.root
        },
        update = {},
    )
}

@SuppressLint("SetTextI18n")
@Composable
fun LegacyDiarySubItemCard(
    diary: DiaryUiModel,
    itemClickCallback: (diary: DiaryUiModel) -> Unit,
    itemLongClickCallback: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
        colors = CardDefaults.cardColors(Color(LocalContext.current.config.backgroundColor)),
        modifier =
            Modifier
                .padding(HORIZONTAL_PADDING.dp, VERTICAL_PADDING.dp)
                .border(
                    1.dp,
                    Color(
                        LocalContext.current.config.backgroundColor
                            .darkenColor(),
                    ).copy(1.0f),
                    shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                ).combinedClickable(
                    onClick = { itemClickCallback(diary) },
                    onLongClick = itemLongClickCallback,
                ),
        elevation = CardDefaults.cardElevation(defaultElevation = ROUNDED_CORNER_SHAPE_SIZE.dp),
    ) {
        AndroidView(
            modifier =
                Modifier
                    .fillMaxWidth(),
            factory = { ctx ->
                val activity = ctx as Activity
                val currentQuery = ""
                val binding =
                    ItemDiarySubBinding.inflate(LayoutInflater.from(ctx)).apply {
                        activity.run {
                            root.run {
//                            setOnClickListener { itemClickCallback(diary) }
//                            setOnLongClickListener {
//                                itemLongClickCallback()
//                                true
//                            }
                                updateTextColors(this, 0, 0)
                                updateAppViews(this)
                                initTextSize(this)
                                updateCardViewPolicy(this)
                                FontUtils.setFontsTypeface(context, null, this)
                            }
                        }

                        selection.visibility = View.GONE
                    }

                binding.root
            },
            update = { rootView ->
                val context = rootView.context
                ItemDiarySubBinding.bind(rootView).run {
                    if (diary.currentTimeMillis > System.currentTimeMillis()) {
                        viewFutureDiaryBadge.visibility = View.VISIBLE
                        cardFutureDiaryBadge.visibility = View.VISIBLE
                        textDDayCount.text =
                            DateUtils.getOnlyDayRemaining(diary.currentTimeMillis)
                    } else {
                        viewFutureDiaryBadge.visibility = View.GONE
                        cardFutureDiaryBadge.visibility = View.GONE
                    }

                    textDateTime.text =
                        when (diary.isAllDay) {
                            true -> {
                                DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                            }

                            false -> {
                                DateUtils.getDateTimeStringForceFormatting(
                                    diary.currentTimeMillis,
                                    context,
                                )
                            }
                        }

                    EasyDiaryUtils.boldString(context, textTitle)
                    context.applyMarkDownPolicy(
                        textTitle,
                        EasyDiaryUtils.summaryDiaryLabel(diary),
                        false,
                        arrayListOf(),
                        true,
                    )

                    FlavorUtils.initWeatherView(context, imageSymbol, diary.weather)
                }
            },
        )
    }
}
