package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import com.zhpan.bannerview.constants.PageStyle
import io.github.aafactory.commons.extensions.dpToPixel
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.PAGE_MARGIN
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.PAGE_STYLE
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment.Companion.REVEAL_WIDTH

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DashboardActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityDashboardBinding


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

        supportFragmentManager.beginTransaction().run {
            // PhotoHighlight
            replace(R.id.photoHighlight, PhotoHighlightFragment().apply {
                arguments = Bundle().apply {
                    putInt(PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                    putFloat(REVEAL_WIDTH, 20F)
                    putFloat(PAGE_MARGIN, 5F)
                }
                togglePhotoHighlightCallback = { isVisible: Boolean ->
                    mBinding.photoHighlight.visibility = if (isVisible) View.VISIBLE else View.GONE
                    if (!isLandScape()) mBinding.cardPhotoHighlight?.visibility = if (isVisible) View.VISIBLE else View.GONE
                }
            })

            // DDay
            replace(R.id.dDay, DDayFragment())

            // DashBoardSummary
            replace(R.id.summary, DashBoardSummaryFragment())

            // Daily Symbol
            replace(R.id.dashboard_daily_symbol, DailySymbolFragment())

            // Commit
            commit()
        }

        mBinding.run {
            val scaleFactor = if (isLandScape()) 0.5F else 1F
            (getDefaultDisplay().x * 0.95).toInt().also {
                val width = it.times(scaleFactor).toInt()
                lifetime.layoutParams.width = width
                lastMonth.layoutParams.width = width
                lastWeek.layoutParams.width = width
            }

            (getDefaultDisplay().x * 0.95).toInt().also {
                val width = it.times(scaleFactor).toInt()
                statistics1.layoutParams.width = width
                statistics2.layoutParams.width = width
                statistics3.layoutParams.width = width
                statistics4.layoutParams.width = width
                statistics5.layoutParams.width = width
                if (isLandScape()) {
                    val height = getDefaultDisplay().y - statusBarHeight() - actionBarHeight()
                    statistics1.layoutParams.height = height
                    statistics2.layoutParams.height = height
                    statistics3.layoutParams.height = height
                    statistics4.layoutParams.height = height
                    statistics5.layoutParams.height = height
                }
            }
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lifetime, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LIFETIME)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastMonth, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_MONTH)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.beginTransaction().run {
            replace(R.id.lastWeek, DashBoardRankFragment().apply {
                val args = Bundle()
                args.putString(DashBoardRankFragment.MODE_FLAG, DashBoardRankFragment.MODE_LAST_WEEK)
                arguments = args
            })
            commit()
        }

        val chartTitle = getString(R.string.statistics_creation_time)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics1, WritingBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, chartTitle)
                arguments = args
            })
            commit()
        }

        val symbolAllTitle = getString(R.string.statistics_symbol_all)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics2, SymbolBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, symbolAllTitle)
                arguments = args
            })
            commit()
        }

        val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics3, SymbolHorizontalBarChartFragment().apply {
                val args = Bundle()
                args.putString(WritingBarChartFragment.CHART_TITLE, symbolTopTenTitle)
                arguments = args
            })
            commit()
        }

        if (config.enableDebugMode) {
            mBinding.statistics4.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().run {
                replace(R.id.statistics4, WeightLineChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, "Weight")
                    arguments = args
                })
                commit()
            }

            mBinding.statistics5.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().run {
                replace(R.id.statistics5, StockLineChartFragment().apply {
                    val args = Bundle()
                    args.putString(WritingBarChartFragment.CHART_TITLE, "Stock")
                    arguments = args
                })
                commit()
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


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
