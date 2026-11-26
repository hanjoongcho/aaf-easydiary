package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemDiaryDashboardBinding
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.presentation.components.TagChip
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.easydiary.utils.FontUtils
import java.util.WeakHashMap
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.domain.model.Tag as TagDomain

class DiaryDashboardItemAdapter(
    val activity: Activity,
) : BaseBannerAdapter<DiaryDomain>() {
    /**
     * The tag chip state of each page view (issue #270). A banner page is inflated once and rebound while the
     * pager keeps it, so the Compose content of the chip row is set once per page view and a rebind only puts a
     * new list in the state instead of rebuilding the composition.
     */
    private val tagChipStates = WeakHashMap<ComposeView, MutableState<List<TagDomain>>>()

    override fun bindData(
        holder: BaseViewHolder<DiaryDomain>,
        diary: DiaryDomain,
        position: Int,
        pageSize: Int,
    ) {
        val context = holder.itemView.context
        val binding = ItemDiaryDashboardBinding.bind(holder.itemView)

        binding.root.run {
            context.updateTextColors(this, 0, 0)
            context.updateAppViews(this)
            context.initTextSize(this)
            context.updateCardViewPolicy(this)
            FontUtils.setFontsTypeface(context, null, this)
            binding.run {
                textTitle.run {
                    visibility = if (diary.title.isNullOrEmpty()) View.GONE else View.VISIBLE
                    text = diary.title
                    EasyDiaryUtils.boldString(activity, this)
                }

                // 경과 일수 설정이 켜져 있으면 미래 일정이 아니어도 좌측 상단 배지에 D-Day 형식으로 표시합니다.
                if (diary.dateAnchorTimeMillis() > System.currentTimeMillis() || context.config.enableElapsedDays) {
                    viewFutureDiaryBadge.visibility = View.VISIBLE
                    cardFutureDiaryBadge.visibility = View.VISIBLE
                    textDDayCount.text = DateUtils.getOnlyDayRemaining(diary.dateAnchorTimeMillis())
                } else {
                    viewFutureDiaryBadge.visibility = View.GONE
                    cardFutureDiaryBadge.visibility = View.GONE
                }

                textContents.maxLines =
                    when (context.config.enableContentsSummary) {
                        true -> {
                            textContents.ellipsize = TextUtils.TruncateAt.END
                            context.config.summaryMaxLines
                        }

                        false -> {
                            Integer.MAX_VALUE
                        }
                    }
                if (textContents.getTag(R.id.diary_dashboard_item) != diary.contents!!) {
                    textContents.setTag(R.id.diary_dashboard_item, diary.contents!!)
                    activity.applyMarkDownPolicy(textContents, diary.contents!!, false, arrayListOf(), true)
                    if (activity.config.enableMarkdown) {
                        textContents.tag = diary.diaryId
                        EasyDiaryUtils.applyMarkDownEllipsize(textContents, diary.diaryId)
                    }
                }
                textDateTime.text =
                    when (diary.isAllDay) {
                        true -> DateUtils.getDateStringFromTimeMillis(diary.dateAnchorTimeMillis())
                        false -> DateUtils.getDateTimeStringForceFormatting(diary.currentTimeMillis, activity)
                    }

                context.run {
                    if (config.enableLocationInfo) {
                        diary.location?.let {
                            changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
                            locationLabel.text = it.address
                            locationContainer.visibility = View.VISIBLE
                        } ?: run { locationContainer.visibility = View.GONE }
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

                    FlavorUtils.initWeatherView(this, imageSymbol, diary.symbolSequence)

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
                            val imageXY = dpToPixel(32F)
                            val imageView = ImageView(this)
                            val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
                            layoutParams.setMargins(0, dpToPixel(1F), dpToPixel(3F), 0)
                            imageView.layoutParams = layoutParams
                            imageView.background = createBackgroundGradientDrawable(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL)
                            imageView.scaleType = ImageView.ScaleType.CENTER
                            dpToPixel(1.5F, Calculation.FLOOR).apply {
                                imageView.setPadding(this, this, this, this)
                            }
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
                                .with(this)
                                .load(EasyDiaryUtils.getApplicationDataDirectory(this) + it.getFilePath())
                                .listener(listener)
                                .apply(createThumbnailGlideOptions(imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL, it.isEncrypt()))
                                .into(imageView)
                            photoViews.addView(imageView)
                        }
                    }

//                    textContents.maxLines = when (config.enableContentsSummary) {
//                        true -> config.summaryMaxLines
//                        false -> Integer.MAX_VALUE
//                    }
                }

                bindTags(binding, diary)
            }
        }
    }

    /**
     * Draws the `#tag` chips of the card under the photos and above the location and contents length chips,
     * the same place and style the main list uses (issue #270). The row is gone while the entry carries no tag,
     * so a card without tags keeps the height it always had. The chips are labels here: the dashboard has no tag
     * filter to apply, so a chip keeps the default action of [TagChip] and only holds the tap that lands on it,
     * while a tap anywhere else on the page still opens the entry through the pager's page click listener.
     */
    private fun bindTags(
        binding: ItemDiaryDashboardBinding,
        diary: DiaryDomain,
    ) {
        val tagViews = binding.tagViews
        tagViews.visibility = if (diary.tags.isEmpty()) View.GONE else View.VISIBLE
        val tagChipState = tagChipStates[tagViews] ?: mutableStateOf<List<TagDomain>>(emptyList()).also { state ->
            tagViews.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            tagViews.setContent {
                AppTheme {
                    FlowRow(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                // Room for the 2dp shadow of a chip, so the chips of the first and last column
                                // and of the last row keep their shadow inside the ComposeView (issue #270).
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        state.value.forEach { tag ->
                            TagChip(name = tag.name)
                        }
                    }
                }
            }
            tagChipStates[tagViews] = state
            state
        }
        tagChipState.value = diary.tags.toList()
    }

    override fun getLayoutId(viewType: Int): Int = R.layout.item_diary_dashboard
}
