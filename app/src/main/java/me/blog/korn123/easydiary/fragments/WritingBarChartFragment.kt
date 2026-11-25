package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.FragmentWritingTimeDonutChartBinding
import me.blog.korn123.easydiary.domain.model.WritingTimeStatistics
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.presentation.statistics.WritingTimeChartUiState
import me.blog.korn123.easydiary.presentation.statistics.components.WritingTimeDonutChart
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.views.FixedCardView

/**
 * The writing time chart of the statistics screen: the entries counted by the hour of the day they were written at,
 * drawn as a ring of the eight spans of the day in the order of the clock.
 *
 * The fragment is the shell the other charts use: it fills the chart area of [StatisticsActivity], or sits in a
 * dashboard card with the chart title the dashboard hands it and an icon that opens the chart on the whole screen.
 * The drawing itself is [WritingTimeDonutChart], Compose as the rest of the new UI of this app is, so the hours of
 * the day are rows beside the ring instead of labels on an axis.
 *
 * The class keeps the name the dashboard and the statistics screen know it by, so both keep opening it without a
 * change. Its layout is a shell of its own (`fragment_writing_time_donut_chart.xml`) and not the layout of the
 * symbol chart (`fragment_writing_barchart.xml`), which the symbol chart still draws its bars in.
 *
 * The entries are counted through [DiaryViewModel.findDiary] like the bar chart this chart replaces counted them,
 * so the chart still leaves out a temporary entry and honours the settings that hide future entries.
 */
@AndroidEntryPoint
class WritingBarChartFragment : Fragment() {
    private lateinit var binding: FragmentWritingTimeDonutChartBinding
    private val diaryViewModel: DiaryViewModel by viewModels()
    private val chartState = MutableStateFlow(WritingTimeChartUiState())
    private var coroutineJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWritingTimeDonutChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applyChartTitle(view)
        binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.composeView.setContent {
            AppTheme {
                val uiState by chartState.collectAsStateWithLifecycle()
                WritingTimeDonutChart(uiState = uiState)
            }
        }
        loadWritingTime()
    }

    /**
     * Shows the title a dashboard card passes in and the icon that opens the chart on the whole screen. A chart
     * opened from the statistics screen itself passes no title and fills the chart area without the card's
     * padding, the way the other charts do.
     */
    private fun applyChartTitle(view: View) {
        val title = arguments?.getString(ChartConstants.CHART_TITLE)
        if (title == null) {
            (view as? FixedCardView)?.run {
                fixedAppcompatPadding = false
                radius = 0f
            }
            return
        }

        binding.chartTitle.run {
            text = title
            visibility = View.VISIBLE
        }
        view.findViewById<ImageView>(R.id.image_expend_chart)?.run {
            visibility = View.VISIBLE
            requireActivity().updateDrawableColorInnerCardView(this, requireContext().config.textColor)
            setOnClickListener { expandIcon ->
                expandIcon.postDelayed({
                    TransitionHelper.startActivityWithTransition(
                        requireActivity(),
                        Intent(requireActivity(), StatisticsActivity::class.java),
                    )
                }, EXPAND_TRANSITION_DELAY_MILLIS)
            }
        }
    }

    /**
     * Counts the entries of the database by the hour they were written at on a background thread and hands the
     * chart the shares of the eight spans, which keeps the ring from being drawn before there are the counts it is
     * drawn from.
     */
    private fun loadWritingTime() {
        coroutineJob =
            viewLifecycleOwner.lifecycleScope.launch {
                val uiState = withContext(Dispatchers.IO) { readWritingTime() }
                chartState.value = uiState
            }
    }

    private suspend fun readWritingTime(): WritingTimeChartUiState {
        val hours =
            diaryViewModel.findDiary(null).map { diary ->
                DateUtils.timeMillisToDate(diary.currentTimeMillis, HOUR_OF_DAY_PATTERN).toIntOrNull() ?: MIDNIGHT
            }
        return WritingTimeChartUiState(shares = WritingTimeStatistics.shares(hours), isLoading = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineJob?.run { if (isActive) cancel() }
    }

    private companion object {
        /** The hour of the day of an entry, which is what the chart counts instead of the date. */
        const val HOUR_OF_DAY_PATTERN = "HH"

        /** An hour the formatter could not read is counted at midnight rather than dropped from the total. */
        const val MIDNIGHT = 0

        /** The time the card's expand icon waits before it opens the statistics screen, as the other charts do. */
        const val EXPAND_TRANSITION_DELAY_MILLIS = 300L
    }
}
