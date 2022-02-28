package me.blog.korn123.easydiary.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.zhpan.bannerview.constants.PageStyle
import io.github.aafactory.commons.extensions.dpToPixel
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.statusBarHeight
import me.blog.korn123.easydiary.fragments.*

class DashboardDialogFragment : DialogFragment() {
    private lateinit var mBinding: ActivityDashboardBinding

    override fun onStart() {
        super.onStart()
        requireActivity().run {
            val height = getDefaultDisplay().y
            dialog?.window?.also {
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = ActivityDashboardBinding.inflate(layoutInflater)


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.run {
            appBar.visibility = View.GONE
            close.visibility = View.VISIBLE
            close.setOnClickListener { dismiss() }

            val scaleFactor = if (requireActivity().isLandScape()) 0.5F else 1F
            (requireActivity().getDefaultDisplay().x * 0.8).toInt().also {
                val width = it.times(scaleFactor).toInt()
                lifetime.layoutParams.width = width
                lastMonth.layoutParams.width = width
                lastWeek.layoutParams.width = width
            }

            (requireActivity().getDefaultDisplay().x * 0.95).toInt().also {
                val width = it.times(scaleFactor).toInt()
                statistics1.layoutParams.width = width
                statistics2.layoutParams.width = width
                statistics3.layoutParams.width = width
                if (requireActivity().isLandScape()) {
                    val height = requireActivity().getDefaultDisplay().y - requireActivity().statusBarHeight() - requireActivity().dpToPixel(20F)
                    statistics1.layoutParams.height = height
                    statistics2.layoutParams.height = height
                    statistics3.layoutParams.height = height
                }
            }

            childFragmentManager.beginTransaction().run {
                // PhotoHighlight
                replace(R.id.photoHighlight, PhotoHighlightFragment().apply {
                    arguments = Bundle().apply {
                        putInt(PhotoHighlightFragment.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                        putFloat(PhotoHighlightFragment.REVEAL_WIDTH, 20F)
                        putFloat(PhotoHighlightFragment.PAGE_MARGIN, 5F)
                    }
                })

                // DashBoardSummary
                replace(R.id.summary, DashBoardSummaryFragment())

                // Daily Symbol
                replace(R.id.dashboard_daily_symbol, DailySymbolFragment())


                // DashBoardRank-Lifetime
                replace(R.id.lifetime, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LIFETIME)
                    arguments = args
                })

                // DashBoardRank-LastMonth
                replace(R.id.lastMonth, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_MONTH)
                    arguments = args
                })

                // DashBoardRank-LastWeek
                replace(R.id.lastWeek, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_WEEK)
                    arguments = args
                })

                // Statistics-Creation Time
                val chartTitle = getString(R.string.statistics_creation_time)
                replace(R.id.statistics1, BarChartFragment().apply {
                    val args = Bundle()
                    args.putString(BarChartFragment.CHART_TITLE, chartTitle)
                    arguments = args
                })

                // Statistics-Symbol All
                val symbolAllTitle = getString(R.string.statistics_symbol_all)
                replace(R.id.statistics2, BarChartFragmentT2().apply {
                    val args = Bundle()
                    args.putString(BarChartFragment.CHART_TITLE, symbolAllTitle)
                    arguments = args
                })

                // Statistics-Symbol TopTen
                val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
                replace(R.id.statistics3, HorizontalBarChartFragment().apply {
                    val args = Bundle()
                    args.putString(BarChartFragment.CHART_TITLE, symbolTopTenTitle)
                    arguments = args
                })

                // Commit
                commit()
            }
//            childFragmentManager.executePendingTransactions()
//            EasyDiaryUtils.disableTouchEvent(dashboardDimmer)
            dashboardDimmer.visibility = View.GONE
            dashboardProgress.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.run {
            root.setBackgroundColor(requireActivity().config.screenBackgroundColor)
//            root.setBackgroundColor(Color.RED)
            FontUtils.setFontsTypeface(requireContext(), null, root, true)
        }
    }

}