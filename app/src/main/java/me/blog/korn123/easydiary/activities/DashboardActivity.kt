package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.PAGE_MARGIN
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.PAGE_STYLE
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.REVEAL_WIDTH
import me.blog.korn123.easydiary.helper.TransitionHelper

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DashboardActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityDashboardBinding
    private lateinit var mDailySymbolFragment: DailySymbolFragment


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        // FIXME: Fixed a background thread processing error inside fragment when rotating the screen
//        setTheme(getThemeId())
        super.onCreate(null)

        mBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(true)
        }


        mDailySymbolFragment = DailySymbolFragment()
        mBinding.close.setOnClickListener { view ->
            view.postDelayed({
                TransitionHelper.finishActivityWithTransition(this)
            }, 300)

        }

        supportFragmentManager.beginTransaction().run {
            // 01. PhotoHighlight
            replace(R.id.photoHighlight, PhotoHighlightFragment().apply {
                arguments = Bundle().apply {
                    putInt(PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                    putFloat(REVEAL_WIDTH, 20F)
                    putFloat(PAGE_MARGIN, 5F)
                }
                togglePhotoHighlightCallback = { isVisible: Boolean ->
                    mBinding.photoHighlight.visibility = if (isVisible) View.VISIBLE else View.GONE
//                    if (!isLandScape()) mBinding.cardPhotoHighlight?.visibility = if (isVisible) View.VISIBLE else View.GONE
                }
            })

            // 02. DDay
            replace(R.id.dDay, DDayFragment())

            // 03. TODO
            replace(R.id.fragment_diary_todo, DiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_TODO)
                }
            })

            // 04. DONE
            replace(R.id.fragment_diary_done, DiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_TASK_DONE)
                }
            })

            // 05. Future Diary
            replace(R.id.fragment_diary_future, DiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_FUTURE)
                }
            })

            // 06. Diary Previous 100
            replace(R.id.fragment_diary_previous100, DiaryFragment().apply {
                arguments = Bundle().apply {
                    putString(DiaryFragment.MODE_FLAG, DiaryFragment.MODE_PREVIOUS_100)
                }
            })

            // 07. DashBoardSummary
            replace(R.id.summary, DashBoardSummaryFragment())

            // 08. Daily Symbol
            replace(R.id.dashboard_daily_symbol, mDailySymbolFragment)

            // 09. DashBoardRank-Lifetime
            replace(R.id.lifetime, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(
                    DashBoardRankFragment.MODE_FLAG,
                    DashBoardRankFragment.MODE_LIFETIME
                )
                arguments = args
            })

            // 10. DashBoardRank-LastMonth
            replace(R.id.lastMonth, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(
                    DashBoardRankFragment.MODE_FLAG,
                    DashBoardRankFragment.MODE_LAST_MONTH
                )
                arguments = args
            })

            // 11. DashBoardRank-LastWeek
            replace(R.id.lastWeek, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(
                    DashBoardRankFragment.MODE_FLAG,
                    DashBoardRankFragment.MODE_LAST_WEEK
                )
                arguments = args
            })

            // 12. Statistics-Creation Time
            val chartTitle = getString(R.string.statistics_creation_time)
            replace(R.id.statistics1, WritingBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, chartTitle)
                arguments = args
            })

            // 13. Statistics-Symbol All
            val symbolAllTitle = getString(R.string.statistics_symbol_all)
            replace(R.id.statistics2, SymbolBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, symbolAllTitle)
                arguments = args
            })

            // 14. Statistics-Symbol TopTen
            val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
            replace(R.id.statistics3, SymbolHorizontalBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, symbolTopTenTitle)
                arguments = args
            })

            // 15. Chart Weight
            if (config.enableDebugOptionVisibleChartWeight) {
                mBinding.statistics4.visibility = View.VISIBLE
                replace(R.id.statistics4, WeightLineChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, "Weight")
                    arguments = args
                })
            }

            // 16. Chart Stock
            if (config.enableDebugOptionVisibleChartStock) {
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

        mBinding.run {
            getDashboardCardWidth(0.9F).also {
                lifetime.layoutParams.width = it
                lastMonth.layoutParams.width = it
                lastWeek.layoutParams.width = it
            }

            getDashboardCardWidth(0.95F).also {
                statistics1.layoutParams.width = it
                statistics2.layoutParams.width = it
                statistics3.layoutParams.width = it
                statistics4.layoutParams.width = it
                statistics5.layoutParams.width = it
                if (isLandScape()) {
                    val height = getDefaultDisplay().y - statusBarHeight() - actionBarHeight()
                    statistics1.layoutParams.height = height
                    statistics2.layoutParams.height = height
                    statistics3.layoutParams.height = height
                    statistics4.layoutParams.height = height
                    statistics5.layoutParams.height = height
                }
            }

            insertDiaryButton.post { insertDiaryButton.visibility = View.VISIBLE }
            insertDiaryButton.setOnClickListener {
                val createDiary = Intent(this@DashboardActivity, DiaryWritingActivity::class.java)
                TransitionHelper.startActivityWithTransition(this@DashboardActivity, createDiary)
            }
        }

        supportFragmentManager.executePendingTransactions()

//        mBinding.close.setOnClickListener {
//            onBackPressed()
//        }

//        EasyDiaryUtils.disableTouchEvent(mBinding.dashboardDimmer)
//        object: Handler(this.mainLooper) {
//            override fun handleMessage(msg: Message) {
//                super.handleMessage(msg)
//                when (msg.what) {
//                    1547 -> {
//                        mBinding.dashboardDimmer.visibility = View.GONE
//                        mBinding.dashboardProgress.visibility = View.GONE
//                    }
//                    else -> {}
//                }
//            }
//        }.apply { sendEmptyMessageDelayed(1547, 1000) }
    }

//    override fun onResume() {
//        super.onResume()
//        updateStatusBarColor(config.screenBackgroundColor.darkenColor())
//        mBinding.mainHolder.setBackgroundColor(config.screenBackgroundColor.darkenColor())

//        mBinding.also {
//            FontUtils.setFontsTypeface(this, null, it.root, true)
//            it.close.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
//        }
//    }

    override fun onResume() {
        super.onResume()
        mBinding.run {
            layoutProgressContainer.visibility = View.VISIBLE
            progress.visibility = View.VISIBLE
//            root.setBackgroundColor(getDashboardBackgroundColor())
//            requireActivity().updateTextColors(root)
//            requireActivity().updateAppViews(root)
//            FontUtils.setFontsTypeface(requireContext(), null, root, true)
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


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun showProgressContainer() {
        mBinding.layoutProgressContainer.visibility = View.VISIBLE
        mBinding.progress.visibility = View.VISIBLE
    }

    fun hideProgressContainer() {
        Handler(Looper.getMainLooper()).postDelayed({
            mBinding.layoutProgressContainer.visibility = View.GONE
            mBinding.progress.visibility = View.GONE
        }, 300)
    }
}
