package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.zhpan.bannerview.constants.PageStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.*
import me.blog.korn123.easydiary.helper.ChartConstants
import me.blog.korn123.easydiary.helper.DashboardConstants
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.PhotoHighlightConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.components.LoadingScreen
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
@AndroidEntryPoint
class DashboardActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dailySymbolFragment: DailySymbolFragment
    private var isHighLightCallbackDone = false

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        binding =
            ActivityDashboardBinding.inflate(layoutInflater).apply {
                setContentView(root)
                setSupportActionBar(toolbar)
                partialComposeLoadingScreen.composeView.setContent {
                    AppTheme {
                        AnimatedVisibility(
                            visible = diaryViewModel.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            LoadingScreen(message = diaryViewModel.loadingMessage)
                        }
                    }
                }
            }

        supportActionBar?.run {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(true)
        }

        dailySymbolFragment = DailySymbolFragment()
        binding.close.setOnClickListener { view ->
            view.postDelayed({
                TransitionHelper.finishActivityWithTransition(this)
            }, 300)
        }

        supportFragmentManager.beginTransaction().run {
            // 01. PhotoHighlight
            replace(
                R.id.photoHighlight,
                PhotoHighlightFragment().apply {
                    arguments =
                        Bundle().apply {
                            putInt(PhotoHighlightConstants.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                            putFloat(PhotoHighlightConstants.REVEAL_WIDTH, 20F)
                            putFloat(PhotoHighlightConstants.PAGE_MARGIN, 5F)
                            putBoolean(PhotoHighlightConstants.IS_DASHBOARD, true)
                        }
                    togglePhotoHighlightCallback = { isVisible: Boolean ->
                        binding.photoHighlight.visibility = if (isVisible) View.VISIBLE else View.GONE
                        isHighLightCallbackDone = true
                        hideProgressContainer()
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
            replace(R.id.dashboard_daily_symbol, dailySymbolFragment)

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
                binding.statistics4.visibility = View.VISIBLE
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
                binding.statistics5.visibility = View.VISIBLE
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

        binding.run {
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
    }

    override fun onResume() {
        super.onResume()
        binding.run {
        }

        lifecycleScope.launch {
            showProgressContainer()
            binding.run {
                dailySymbolFragment.updateDailySymbol()

                if (isHighLightCallbackDone) hideProgressContainer()
            }
        }

        if (!isLandScape()) applyFullScreenStatusBarTheme()

        if (isVanillaIceCreamPlus()) {
            if (!isLandScape()) {
                ViewCompat.setOnApplyWindowInsetsListener(binding.contentsContainer) { v, insets ->
                    val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                    val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                    val layoutParams = v.layoutParams
                    if (layoutParams is ViewGroup.MarginLayoutParams) {
                        layoutParams.topMargin = statusBars.top
                        layoutParams.bottomMargin = navigationBars.bottom
                        v.layoutParams = layoutParams
                    }
                    insets
                }

                ViewCompat.setOnApplyWindowInsetsListener(binding.close as View) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                    val layoutParams = v.layoutParams
                    if (layoutParams is ViewGroup.MarginLayoutParams) {
                        layoutParams.bottomMargin = dpToPixel(16F).plus(systemBars.bottom)
                        v.layoutParams = layoutParams
                    }
                    insets
                }
            }
        } else {
            if (!isLandScape()) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                binding.contentsContainer.run {
                    val tempLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
                    tempLayoutParams.topMargin = statusBarHeight()
                    tempLayoutParams.bottomMargin = navigationBarHeight()
                    layoutParams = tempLayoutParams
                }

                binding.close.run {
                    val tempLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
                    tempLayoutParams.bottomMargin = dpToPixel(16F).plus(navigationBarHeight())
                    layoutParams = tempLayoutParams
                }
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun showProgressContainer() {
        diaryViewModel.isLoading = true
    }

    fun hideProgressContainer() {
        Handler(Looper.getMainLooper()).postDelayed({
            diaryViewModel.isLoading = false
        }, 300)
    }
}
