package me.blog.korn123.easydiary.presentation.timeline.components

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemTimelineBinding
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.findActivity
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.text.SimpleDateFormat

@Composable
fun TimelineItemCard(
    diary: DiaryDomain,
    isFirstInGroup: Boolean,
    currentQuery: String?,
    onDiaryClick: (DiaryDomain) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            ItemTimelineBinding.inflate(LayoutInflater.from(ctx)).root
        },
        update = { view ->
            val binding = ItemTimelineBinding.bind(view)
            val ctx = view.context
            val activity = ctx.findActivity()

            if (isFirstInGroup) {
                binding.title.text = DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                binding.titleContainer.visibility = View.VISIBLE
                binding.topLine.visibility = View.VISIBLE
            } else {
                binding.titleContainer.visibility = View.GONE
                binding.topLine.visibility = View.GONE
            }

            FlavorUtils.initWeatherView(ctx, binding.diarySymbol, diary.symbolSequence, false)

            val lineBreakStrings = arrayListOf<String>()
            when (diary.isAllDay) {
                true -> {
                    lineBreakStrings.add(
                        if (ctx.config.enableDebugOptionVisibleDiarySequence) {
                            "[${diary.originDiaryId}] ${ctx.resources.getString(R.string.all_day)}"
                        } else {
                            ctx.resources.getString(R.string.all_day)
                        },
                    )
                }

                false -> {
                    lineBreakStrings.add(
                        if (ctx.config.enableDebugOptionVisibleDiarySequence) {
                            "[${diary.originDiaryId}] ${DateUtils.getTimeStringFromTimeMillis(diary.currentTimeMillis, SimpleDateFormat.MEDIUM)}"
                        } else {
                            DateUtils.getTimeStringFromTimeMillis(diary.currentTimeMillis, SimpleDateFormat.MEDIUM)
                        },
                    )
                }
            }
            if (diary.title?.isNotEmpty() == true) lineBreakStrings.add(diary.title)

            activity?.applyMarkDownPolicy(binding.text1, diary.contents, true, lineBreakStrings, true)

            ctx.updateTextColors(binding.itemHolder, 0, 0)
            ctx.updateAppViews(binding.itemHolder)
            ctx.updateCardViewPolicy(binding.itemHolder)
            ctx.initTextSize(binding.itemHolder)
            FontUtils.setFontsTypeface(ctx, null, binding.root)

            if (!currentQuery.isNullOrEmpty()) {
                if (ctx.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(binding.text1, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(binding.text1, currentQuery)
                }
            }

            if (ctx.config.enableContentsSummary) {
                binding.text1.maxLines = ctx.config.summaryMaxLines + 1
                binding.text1.ellipsize = TextUtils.TruncateAt.valueOf("END")
            } else {
                binding.text1.maxLines = Integer.MAX_VALUE
                binding.text1.ellipsize = null
            }

            if (diary.currentTimeMillis > System.currentTimeMillis()) {
                binding.textDDayCount.visibility = View.VISIBLE
                binding.textDDayCount.text = "（${DateUtils.getOnlyDayRemaining(diary.currentTimeMillis)}）"
            } else {
                binding.textDDayCount.visibility = View.GONE
            }

            binding.root.setOnClickListener { onDiaryClick(diary) }
        },
    )
}
