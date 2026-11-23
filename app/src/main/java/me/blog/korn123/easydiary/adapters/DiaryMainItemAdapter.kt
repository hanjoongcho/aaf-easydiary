package me.blog.korn123.easydiary.adapters

import android.animation.ArgbEvaluator
import android.app.Activity
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.databinding.ItemDiaryMainBinding
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createThumbnailGlideOptions
import me.blog.korn123.easydiary.utils.FontUtils
import me.blog.korn123.easydiary.views.FixedCardView
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.domain.model.Tag as TagDomain
import me.blog.korn123.easydiary.domain.model.TagRules

class DiaryMainItemAdapter(
    val activity: Activity,
    val lifecycle: LifecycleCoroutineScope,
    private val diaryItems: List<DiaryDomain>,
    val itemClickCallback: (diary: DiaryDomain) -> Unit,
    val itemLongClickCallback: () -> Unit,
    /** Tapping a `#tag` chip of a card filters the main screen by that tag (issue #270). */
    val itemTagClickCallback: (tag: TagDomain) -> Unit = {},
) : RecyclerView.Adapter<DiaryMainItemAdapter.ViewHolder>(),
    FastScrollRecyclerView.SectionedAdapter {
    var currentQuery: String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            activity,
            ItemDiaryMainBinding.inflate(activity.layoutInflater, parent, false),
            this,
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bindTo(diaryItems[position])
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: List<Any>,
    ) {
        if (payloads.contains(PAYLOAD_CHECKBOX)) {
            holder.bindCheckbox(diaryItems[position])
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = diaryItems.size

    override fun getSectionName(position: Int): String {
        fun ellipsis(str: String?): String {
            val maxLength = 15
            val ellipsisString =
                str?.let {
                    when (str.length > maxLength) {
                        true -> str.take(maxLength).plus("…")
                        false -> str
                    }
                } ?: String.EMPTY
            return ellipsisString
        }

        val label =
            when (diaryItems[position].title?.isNotEmpty() ?: false) {
                true -> String.format("%s", ellipsis(diaryItems[position].title))
                false -> String.format("%s", ellipsis(diaryItems[position].contents))
            }
        return label
    }

    fun getSelectedItems(): List<DiaryDomain> {
        val selectedItems = arrayListOf<DiaryDomain>()
        diaryItems.map {
            if (it.isSelected) selectedItems.add(it)
        }
        return selectedItems
    }

    fun setDiaryMode(
        mode: DiaryMode,
        clearSelection: Boolean = true,
    ) {
        (activity as DiaryMainActivity).diaryMode = mode
        if (clearSelection) {
            diaryItems.forEach { it.isSelected = false }
        }
        notifyItemRangeChanged(0, diaryItems.size, PAYLOAD_CHECKBOX)
    }

    fun toggleCheckBoxAll() {
        diaryItems.forEach { diary ->
            diary.isSelected = diary.isSelected.not()
        }
        notifyItemRangeChanged(0, diaryItems.size, PAYLOAD_CHECKBOX)
    }

    suspend fun toggleCheckBoxALl() = toggleCheckBoxAll()

    inner class ViewHolder(
        val activity: Activity,
        private val itemDiaryMainBinding: ItemDiaryMainBinding,
        val adapter: DiaryMainItemAdapter,
    ) : RecyclerView.ViewHolder(itemDiaryMainBinding.root) {
        fun bindTo(diary: DiaryDomain) {
            itemDiaryMainBinding.run {
                // 경과 일수 설정이 켜져 있으면 미래 일정이 아니어도 좌측 상단 배지에 D-Day 형식으로 표시합니다.
                if (diary.dateAnchorTimeMillis() > System.currentTimeMillis() || activity.config.enableElapsedDays) {
                    viewFutureDiaryBadge.visibility = View.VISIBLE
                    cardFutureDiaryBadge.visibility = View.VISIBLE
                    textDDayCount.text = DateUtils.getOnlyDayRemaining(diary.dateAnchorTimeMillis())
                } else {
                    viewFutureDiaryBadge.visibility = View.GONE
                    cardFutureDiaryBadge.visibility = View.GONE
                }

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

                bindCheckbox(diary)

                if (diary.title.isNullOrEmpty()) {
                    textTitle.visibility = View.GONE
                } else {
                    textTitle.visibility = View.VISIBLE
                }
                textTitle.text = diary.title
                activity.applyMarkDownPolicy(textContents, diary.contents!!, false, arrayListOf(), true)
                if (activity.config.enableMarkdown) {
                    textContents.tag = diary.diaryId
                    EasyDiaryUtils.applyMarkDownEllipsize(textContents, diary.diaryId, 500)
                }

                // highlight current query
                if (currentQuery?.isNotEmpty() == true) {
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
                            DateUtils.getDateStringFromTimeMillis(diary.dateAnchorTimeMillis())
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
                        "[${diary.diaryId}, ${diary.originDiaryId}] ${textDateTime.text}"
                }
                FlavorUtils.initWeatherView(activity, imageSymbol, diary.symbolSequence)

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
//                        layoutParams.setMargins(
//                            0,
//                            activity.dpToPixel(3F),
//                            activity.dpToPixel(3F),
//                            0
//                        )
                        imageView.layoutParams = layoutParams
//                        imageView.background = createBackgroundGradientDrawable(
//                            activity.config.primaryColor,
//                            THUMBNAIL_BACKGROUND_ALPHA,
//                            imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
//                        )
                        imageView.scaleType = ImageView.ScaleType.CENTER
//                        activity.dpToPixel(1.5F, Calculation.FLOOR).apply {
//                            imageView.setPadding(this, this, this, this)
//                        }
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
//                    if (photoViews.childCount >= maxPhotos) return@map

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
//                                setMargins(margin, margin, margin, margin)
                                        },
                                )

                                radius = imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
                                fixedAppcompatPadding = true
                                setContentPadding(contentPadding, contentPadding, contentPadding, contentPadding)
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

                bindTags(diary)
            }
        }

        /**
         * Draws the `#tag` chips of the card (issue #270). The row is gone while the entry carries no tag, so a
         * card without tags keeps the height it always had.
         */
        private fun bindTags(diary: DiaryDomain) {
            itemDiaryMainBinding.tagViews.run {
                removeAllViews()
                if (diary.tags.isEmpty()) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                    diary.tags.forEach { tag ->
                        addView(createTagChip(tag))
                    }
                }
            }
        }

        private fun createTagChip(tag: TagDomain): View {
            // The reading view's Compose TagChip carries 8dp sideways and 3dp up and down inside it, so
            // the chips of this list use the same inner padding (issue #270).
            val horizontalPadding = activity.dpToPixel(8f)
            val verticalPadding = activity.dpToPixel(3f)
            val textView =
                TextView(activity).apply {
                    text = TagRules.label(tag.name)
                    setTextColor(activity.config.textColor)
                    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                    textSize = 12f
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }
            val chip =
                FixedCardView(activity).apply {
                    // Set before the first layout: turning it on in onDraw() left the first frame with
                    // a different padding and shadow than the Compose chip (issue #270).
                    useCompatPadding = true
                    radius = activity.resources.getDimension(R.dimen.card_radius)
                    cardElevation = activity.dpToPixel(2f).toFloat()
                    layoutParams =
                        ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            marginEnd = activity.dpToPixel(4f)
                            // Keeps a wrapped row of chips apart, and keeps the bottom shadow off the
                            // edge of the row (issue #270).
                            marginBottom = activity.dpToPixel(4f)
                        }
                    addView(textView)
                }
            activity.updateDashboardInnerCard(chip)
            chip.setOnClickListener { adapter.itemTagClickCallback(tag) }
            return chip
        }

        fun bindCheckbox(diary: DiaryDomain) {
            itemDiaryMainBinding.run {
                selection.setOnCheckedChangeListener(null)
                selection.isChecked = diary.isSelected
                when ((activity as DiaryMainActivity).diaryMode) {
                    DiaryMode.READ -> {
                        selection.visibility = View.GONE
                    }

                    DiaryMode.DELETE -> {
                        selection.visibility = View.VISIBLE
                    }
                }

                selection.setOnCheckedChangeListener { _, isChecked ->
                    diary.isSelected = isChecked
                }
            }
        }
    }

    companion object {
        private const val PAYLOAD_CHECKBOX = "PAYLOAD_CHECKBOX"
    }
}
