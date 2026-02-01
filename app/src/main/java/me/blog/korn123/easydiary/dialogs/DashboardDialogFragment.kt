package me.blog.korn123.easydiary.dialogs

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.DashboardConstants
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.PhotoHighlightConstants
import me.blog.korn123.easydiary.helper.TransitionHelper

class DashboardDialogFragment : DialogFragment() {
    private lateinit var mBinding: ActivityDashboardBinding
    private lateinit var mDailySymbolFragment: DailySymbolFragment

    override fun onStart() {
        super.onStart()
        requireActivity().run activity@{
            getDisplayMetrics().also {
                dialog?.window?.run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        statusBarColor = this@activity.getStatusBarColor(config.primaryColor)
                    }
                }
            }
        }
    }

    override fun getTheme(): Int = R.style.AppTheme_FullScreen

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = ActivityDashboardBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
            layoutProgressContainer.setBackgroundColor(config.primaryColor)

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
                // 01. PhotoHighlight
                replace(
                    R.id.photoHighlight,
                    PhotoHighlightFragment().apply {
                        arguments =
                            Bundle().apply {
                                putInt(PhotoHighlightConstants.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                                putFloat(PhotoHighlightConstants.REVEAL_WIDTH, 20F)
                                putFloat(PhotoHighlightConstants.PAGE_MARGIN, 5F)
                                putBoolean(PhotoHighlightConstants.AUTO_PLAY, true)
                            }
                        togglePhotoHighlightCallback = { isVisible: Boolean ->
                            photoHighlight.visibility = if (isVisible) View.VISIBLE else View.GONE
//                        if (!requireActivity().isLandScape()) cardPhotoHighlight?.visibility = if (isVisible) View.VISIBLE else View.GONE
                        }
                    },
                )

                // 02. DDay
                replace(R.id.dDay, DDayFragment())

                // 03. TODO
                replace(
                    R.id.fragment_diary_todo,
                    DiaryFragment().apply {
                        arguments =
                            Bundle().apply {
                                putString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_TASK_TODO)
                            }
                    },
                )

                // DOING
//                replace(R.id.fragment_diary_doing, DiaryFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_DOING)
//                    }
//                })

                // 04. DONE
                replace(
                    R.id.fragment_diary_done,
                    DiaryFragment().apply {
                        arguments =
                            Bundle().apply {
                                putString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_TASK_DONE)
                            }
                    },
                )

                // CANCEL
//                replace(R.id.fragment_diary_cancel, DiaryFragment().apply {
//                    arguments = Bundle().apply {
//                        putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_CANCEL)
//                    }
//                })

                // 05. Future Diary
                replace(
                    R.id.fragment_diary_future,
                    DiaryFragment().apply {
                        arguments =
                            Bundle().apply {
                                putString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_FUTURE)
                            }
                    },
                )

                // 06. Diary Previous 100
                replace(
                    R.id.fragment_diary_previous100,
                    DiaryFragment().apply {
                        arguments =
                            Bundle().apply {
                                putString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_PREVIOUS_100)
                            }
                    },
                )

                // 07. DashBoardSummary
                replace(R.id.summary, DashBoardSummaryFragment())

                // 08. Daily Symbol
                replace(R.id.dashboard_daily_symbol, mDailySymbolFragment)

                // 09. DashBoardRank-Lifetime
                replace(
                    R.id.lifetime,
                    DashBoardRankFragment().apply {
                        val args = Bundle()
                        args.putString(
                            DashboardConstants.MODE_FLAG,
                            DashboardConstants.MODE_LIFETIME,
                        )
                        arguments = args
                    },
                )

                // 10. DashBoardRank-LastMonth
                replace(
                    R.id.lastMonth,
                    DashBoardRankFragment().apply {
                        val args = Bundle()
                        args.putString(
                            DashboardConstants.MODE_FLAG,
                            DashboardConstants.MODE_LAST_MONTH,
                        )
                        arguments = args
                    },
                )

                // 11. DashBoardRank-LastWeek
                replace(
                    R.id.lastWeek,
                    DashBoardRankFragment().apply {
                        val args = Bundle()
                        args.putString(
                            DashboardConstants.MODE_FLAG,
                            DashboardConstants.MODE_LAST_WEEK,
                        )
                        arguments = args
                    },
                )

                // 12. Statistics-Creation Time
                val chartTitle = getString(R.string.statistics_creation_time)
                replace(
                    R.id.statistics1,
                    WritingBarChartFragment().apply {
                        val args = Bundle()
                        args.putString(ChartConstants.CHART_TITLE, chartTitle)
                        arguments = args
                    },
                )

                // 13. Statistics-Symbol All
                val symbolAllTitle = getString(R.string.statistics_symbol_all)
                replace(
                    R.id.statistics2,
                    SymbolBarChartFragment().apply {
                        val args = Bundle()
                        args.putString(ChartConstants.CHART_TITLE, symbolAllTitle)
                        arguments = args
                    },
                )

                // 14. Statistics-Symbol TopTen
                val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
                replace(
                    R.id.statistics3,
                    SymbolHorizontalBarChartFragment().apply {
                        val args = Bundle()
                        args.putString(ChartConstants.CHART_TITLE, symbolTopTenTitle)
                        arguments = args
                    },
                )

                // 15. Chart Weight
                if (config.enableDebugOptionVisibleChartWeight) {
                    mBinding.statistics4.visibility = View.VISIBLE
                    replace(
                        R.id.statistics4,
                        WeightLineChartFragment().apply {
                            val args = Bundle()
                            args.putString(ChartConstants.CHART_TITLE, "Weight")
                            arguments = args
                        },
                    )
                }

                // 16. Chart Stock
                if (config.enableDebugOptionVisibleChartStock) {
                    mBinding.statistics5.visibility = View.VISIBLE
                    replace(
                        R.id.statistics5,
                        StockLineChartFragment().apply {
                            val args = Bundle()
                            args.putString(ChartConstants.CHART_TITLE, "Stock")
                            arguments = args
                        },
                    )
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
            layoutProgressContainer.visibility = View.VISIBLE
            progress.visibility = View.VISIBLE
            root.setBackgroundColor(getDashboardBackgroundColor())
            requireActivity().updateTextColors(root)
            requireActivity().updateAppViews(root)
            FontUtils.setFontsTypeface(requireContext(), null, root, true)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            mBinding.run {
                // Diary Update
                mDailySymbolFragment.updateDailySymbol()

                // FIXME:
                // This is workaround.
                // For pages that are invisible but have already been loaded, it will not be updated.
                mDailySymbolFragment.mCalendarFragment.refreshViewOnlyCurrentPage()

                Handler(Looper.getMainLooper()).postDelayed({
                    layoutProgressContainer.visibility = View.GONE
                    progress.visibility = View.GONE
                }, 300)
            }
        }, 300)
    }

    private fun getDashboardBackgroundColor() = config.screenBackgroundColor
}
