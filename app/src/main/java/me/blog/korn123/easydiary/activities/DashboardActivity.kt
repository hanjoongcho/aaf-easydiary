package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DailySymbolAdapter
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DashboardActivity : AppCompatActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityDashboardBinding
    private lateinit var mDailySymbolAdapter: DailySymbolAdapter
    private var mDailySymbolList: ArrayList<DailySymbolAdapter.DailySymbol> = arrayListOf()
    private val mRequestUpdateDailySymbol = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) updateDailyCard()
    }


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        // FIXME: Fixed a background thread processing error inside fragment when rotating the screen
        setTheme(getThemeId())
        super.onCreate(null)

        mBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        setSupportActionBar(mBinding.toolbar)
//        supportActionBar?.run {
//            title = "Dashboard"
//            setDisplayHomeAsUpEnabled(true)
//        }

        updateStatusBarColor(config.screenBackgroundColor.darkenColor())
        mBinding.root.setBackgroundColor(config.screenBackgroundColor.darkenColor())

        supportFragmentManager.beginTransaction().run {
            replace(R.id.photoHighlight, PhotoHighlightFragment())

            // DashBoardSummaryFragment
            replace(R.id.summary, DashBoardSummaryFragment())

            // Commit
            commit()
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
            replace(R.id.statistics1, BarChartFragment().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, chartTitle)
                arguments = args
            })
            commit()
        }

        val symbolAllTitle = getString(R.string.statistics_symbol_all)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics2, BarChartFragmentT2().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, symbolAllTitle)
                arguments = args
            })
            commit()
        }

        val symbolTopTenTitle = getString(R.string.statistics_symbol_top_ten)
        supportFragmentManager.beginTransaction().run {
            replace(R.id.statistics3, HorizontalBarChartFragment().apply {
                val args = Bundle()
                args.putString(BarChartFragment.CHART_TITLE, symbolTopTenTitle)
                arguments = args
            })
            commit()
        }

        supportFragmentManager.executePendingTransactions()
        initializeDailySymbol()

        mBinding.editSymbolFilter.setOnClickListener {
            Intent(this, SymbolFilterPickerActivity::class.java).apply {
                mRequestUpdateDailySymbol.launch(this)
            }
        }

        mBinding.close.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        mBinding.also {
            FontUtils.setFontsTypeface(this, null, it.root, true)
            it.close.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initializeDailySymbol() {
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH, Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = Date()
        mBinding.month.text = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

        mDailySymbolAdapter = DailySymbolAdapter(
                this,
                mDailySymbolList
        )
        mBinding.dailyCardRecyclerView.apply {
//            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@DashboardActivity, 1)
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
//            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mDailySymbolAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    mBinding.month.text = mDailySymbolList[(mBinding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()].date
                }
            })
        }

        CoroutineScope(Dispatchers.IO).launch { // launch a new coroutine and keep a reference to its Job
            for (num in 1..365) {
                mDailySymbolList.add(DailySymbolAdapter.DailySymbol(dateFormat.format(cal.time), cal.get(Calendar.DAY_OF_WEEK), cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())!!, dayOfMonth.format(cal.time), cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!))
                cal.add(Calendar.DATE, -1)
            }
            withContext(Dispatchers.Main) {
                updateDailyCard()
            }
        }
    }

    private fun updateDailyCard() {
        mBinding.run {
            month.visibility = View.GONE
            dailyCardRecyclerView.visibility = View.GONE
            dailyCardProgressBar.visibility = View.VISIBLE
            selectedSymbolFlexBox.removeAllViews()

            CoroutineScope(Dispatchers.IO).launch {
                config.selectedSymbols.split(",").map { sequence ->
                    val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(layoutInflater)
                    withContext(Dispatchers.Main) {
                        FlavorUtils.initWeatherView(this@DashboardActivity, partialDailySymbolBinding.dailySymbol, sequence.toInt())
                        selectedSymbolFlexBox.addView(partialDailySymbolBinding.root)
                    }
                }
                runOnUiThread {
                    mDailySymbolAdapter.notifyDataSetChanged()
                    month.visibility = View.VISIBLE
                    dailyCardRecyclerView.visibility = View.VISIBLE
                    dailyCardProgressBar.visibility = View.GONE
                }
            }
        }
    }
}
