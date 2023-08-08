package me.blog.korn123.easydiary.dialogs

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zhpan.bannerview.constants.PageStyle
import kotlinx.coroutines.Job
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.getDashboardCardWidth
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.getDisplayMetrics
import me.blog.korn123.easydiary.extensions.getStatusBarColor
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.statusBarHeight
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.fragments.DDayFragment
import me.blog.korn123.easydiary.fragments.DailySymbolFragment
import me.blog.korn123.easydiary.fragments.DashBoardRankFragment
import me.blog.korn123.easydiary.fragments.DashBoardSummaryFragment
import me.blog.korn123.easydiary.fragments.DiaryFragment
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment
import me.blog.korn123.easydiary.fragments.StockLineChartFragment
import me.blog.korn123.easydiary.fragments.SymbolBarChartFragment
import me.blog.korn123.easydiary.fragments.SymbolHorizontalBarChartFragment
import me.blog.korn123.easydiary.fragments.WeightLineChartFragment
import me.blog.korn123.easydiary.fragments.WritingBarChartFragment
import me.blog.korn123.easydiary.helper.TransitionHelper

class DashboardDialogFragment : DialogFragment() {
    private lateinit var mBinding: ActivityDashboardBinding
    private lateinit var mDailySymbolFragment: DailySymbolFragment
    private var coroutineJob: Job? = null

    override fun onStart() {
        super.onStart()
        requireActivity().run activity@ {
            getDisplayMetrics().also {
                dialog?.window?.run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        statusBarColor = this@activity.getStatusBarColor(config.primaryColor)
                    }
                }
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_FullScreen
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = ActivityDashboardBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
        mDailySymbolFragment = DailySymbolFragment()

        mBinding.run {
            requireActivity().updateDrawableColorInnerCardView(close, Color.WHITE)
            appBar.visibility = View.GONE
            close.visibility = View.VISIBLE
            close.setOnClickListener { view ->
                view.postDelayed({
                    dismiss()
                }, 300)

            }

            requireActivity().getDashboardCardWidth(0.9F).also {
                lifetime.layoutParams.width = it
                lastMonth.layoutParams.width = it
                lastWeek.layoutParams.width = it
//                dDay.layoutParams.width = width
            }

            requireActivity().getDashboardCardWidth(0.95F).also {
                statistics1.layoutParams.width = it
                statistics2.layoutParams.width = it
                statistics3.layoutParams.width = it
                statistics4.layoutParams.width = it
                statistics5.layoutParams.width = it
                if (requireActivity().isLandScape()) {
                    val height = requireActivity().getDefaultDisplay().y - requireActivity().statusBarHeight() - requireActivity().dpToPixel(20F)
                    statistics1.layoutParams.height = height
                    statistics2.layoutParams.height = height
                    statistics3.layoutParams.height = height
                    statistics4.layoutParams.height = height
                    statistics5.layoutParams.height = height
                }
            }


            childFragmentManager.beginTransaction().run {
                // PhotoHighlight
                replace(R.id.photoHighlight, PhotoHighlightFragment().apply {
                    arguments = Bundle().apply {
                        putInt(PhotoHighlightFragment.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                        putFloat(PhotoHighlightFragment.REVEAL_WIDTH, 20F)
                        putFloat(PhotoHighlightFragment.PAGE_MARGIN, 5F)
                        putBoolean(PhotoHighlightFragment.AUTO_PLAY, true)
                    }
                    togglePhotoHighlightCallback = { isVisible: Boolean ->
                        photoHighlight.visibility = if (isVisible) View.VISIBLE else View.GONE
//                        if (!requireActivity().isLandScape()) cardPhotoHighlight?.visibility = if (isVisible) View.VISIBLE else View.GONE
                    }
                })

                // DDay
                replace(R.id.dDay, DDayFragment())

                // TODO
                replace(R.id.fragment_diary_todo, DiaryFragment().apply {
                    arguments = Bundle().apply {
                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_TODO)
                    }
                })

                // DOING
//                replace(R.id.fragment_diary_doing, DiaryFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_DOING)
//                    }
//                })

                // DONE
                replace(R.id.fragment_diary_done, DiaryFragment().apply {
                    arguments = Bundle().apply {
                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_DONE)
                    }
                })

                // CANCEL
//                replace(R.id.fragment_diary_cancel, DiaryFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_CANCEL)
//                    }
//                })

                // Future Diary
                replace(R.id.fragment_diary_future, DiaryFragment().apply {
                    arguments = Bundle().apply {
                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_FUTURE)
                    }
                })

                // Diary Previous 100
                replace(R.id.fragment_diary_previous100, DiaryFragment().apply {
                    arguments = Bundle().apply {
                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_PREVIOUS_100)
                    }
                })

                // DashBoardSummary
                replace(R.id.summary, DashBoardSummaryFragment())

                // Daily Symbol
                replace(R.id.dashboard_daily_symbol, mDailySymbolFragment)

                // DashBoardRank-Lifetime
                replace(R.id.lifetime, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(
                        DashBoardRankFragment.MODE_FLAG,
                        DashBoardRankFragment.MODE_LIFETIME
                    )
                    arguments = args
                })

                // DashBoardRank-LastMonth
                replace(R.id.lastMonth, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(
                        DashBoardRankFragment.MODE_FLAG,
                        DashBoardRankFragment.MODE_LAST_MONTH
                    )
                    arguments = args
                })

                // DashBoardRank-LastWeek
                replace(R.id.lastWeek, DashBoardRankFragment().apply {
                    val args = Bundle()
                    args.putString(
                        DashBoardRankFragment.MODE_FLAG,
                        DashBoardRankFragment.MODE_LAST_WEEK
                    )
                    arguments = args
                })

                // Statistics-Creation Time
                val chartTitle = getString(R.string.statistics_creation_time)
                replace(R.id.statistics1, WritingBarChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, chartTitle)
                    arguments = args
                })

                // Statistics-Symbol All
                val symbolAllTitle = getString(R.string.statistics_symbol_all)
                replace(R.id.statistics2, SymbolBarChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, symbolAllTitle)
                    arguments = args
                })

                // Statistics-Symbol TopTen
                val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
                replace(R.id.statistics3, SymbolHorizontalBarChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, symbolTopTenTitle)
                    arguments = args
                })

                if (config.enableDebugOptionVisibleChartWeight) {
                    mBinding.statistics4.visibility = View.VISIBLE
                    replace(R.id.statistics4, WeightLineChartFragment().apply {
                        val args = Bundle()
                        args.putString(WritingBarChartFragment.CHART_TITLE, "Weight")
                        arguments = args
                    })
                }

                if (config.enableDebugOptionVisibleChartWeight) {
                    mBinding.statistics5.visibility = View.VISIBLE
                    replace(R.id.statistics5, StockLineChartFragment().apply {
                        val args = Bundle()
                        args.putString(WritingBarChartFragment.CHART_TITLE, "Stock")
                        arguments = args
                    })
                }

                // Commit
                commit()
            }

            insertDiaryButton.post { insertDiaryButton.visibility = View.VISIBLE }
            insertDiaryButton.setOnClickListener {
                val createDiary = Intent(requireActivity(), DiaryWritingActivity::class.java)
                TransitionHelper.startActivityWithTransition(requireActivity(), createDiary)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.run {
            root.setBackgroundColor(getDashboardBackgroundColor())
            requireActivity().updateTextColors(root)
            requireActivity().updateAppViews(root)
            FontUtils.setFontsTypeface(requireContext(), null, root, true)

            // Diary Update
            mDailySymbolFragment.updateDailySymbol()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob?.let {
            if (it.isActive) it.cancel()
        }
    }

    private fun getDashboardBackgroundColor() = config.screenBackgroundColor
}