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
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.StatisticsActivity
import me.blog.korn123.easydiary.databinding.FragmentTagHorizontalBarchartBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.StatisticsConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.statistics.TagTopTenChartViewModel
import me.blog.korn123.easydiary.presentation.statistics.components.TagTopTenChart
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.views.FixedCardView

/**
 * The tag chart of the statistics screen (issue #270), the fourth chart next to the writing time and the two
 * symbol charts.
 *
 * The fragment is the shell the other charts use: it fills the chart area of [StatisticsActivity], or sits in a
 * dashboard card with the chart title the dashboard hands it and an icon that opens the chart on the whole
 * screen. The drawing itself is [TagTopTenChart], Compose as new UI of this app is, so a long tag name is
 * shortened inside its own row instead of being pushed into an axis label.
 *
 * A card of the dashboard is 500dp high and loses the title's room off that, which is not enough for the ring and
 * ten full rows under it, so the title the dashboard passes is also how the chart is told to draw its compact
 * legend: the statistics screen itself opens the chart without a title and keeps the full rows.
 */
@AndroidEntryPoint
class TagHorizontalBarChartFragment : Fragment() {
    private lateinit var binding: FragmentTagHorizontalBarchartBinding
    private val tagTopTenChartViewModel: TagTopTenChartViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTagHorizontalBarchartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applyChartTitle(view)
        val isDashboardMode = arguments?.getString(ChartConstants.CHART_TITLE) != null
        binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.composeView.setContent {
            AppTheme {
                val uiState by tagTopTenChartViewModel.uiState.collectAsStateWithLifecycle()
                TagTopTenChart(uiState = uiState, isDashboardMode = isDashboardMode)
            }
        }
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
                        Intent(requireActivity(), StatisticsActivity::class.java)
                            .putExtra(
                                StatisticsConstants.CHART_MODE,
                                StatisticsConstants.MODE_SINGLE_HORIZONTAL_BAR_CHART_TAG,
                            ),
                    )
                }, 300)
            }
        }
    }
}
