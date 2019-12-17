package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import kotlinx.android.synthetic.main.fragment_dashboard_rank.*
import java.text.SimpleDateFormat
import java.util.*

class DashBoardRankFragment : androidx.fragment.app.Fragment() {

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard_rank, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        context?.let {
            val symbolMap = FlavorUtils.getDiarySymbolMap(it)
            val sortedMap = when (arguments?.getString(MODE_FLAG, MODE_LIFETIME)) {
                MODE_LAST_MONTH -> {
                    val calendar = Calendar.getInstance()
                    val endMillis = calendar.timeInMillis
                    calendar.add(Calendar.DATE, -30)
                    val startMillis = calendar.timeInMillis
                    dashboardTitle.text = getString(R.string.dashboard_title_last_month)
                    diaryCount.text = EasyDiaryDbHelper.readDiary(null, true, startMillis, endMillis).size.toString()
                    val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                    val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                    val periodInfo = "$startDate - $endDate"
                    period.text = periodInfo
                    ChartUtils.getSortedMapBySymbol(true, startMillis, endMillis)
                }
                MODE_LAST_WEEK -> {
                    val calendar = Calendar.getInstance()
                    val endMillis = calendar.timeInMillis
                    calendar.add(Calendar.DATE, -7)
                    val startMillis = calendar.timeInMillis
                    dashboardTitle.text = getString(R.string.dashboard_title_last_week)
                    diaryCount.text = EasyDiaryDbHelper.readDiary(null, true, startMillis, endMillis).size.toString()
                    val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                    val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                    val periodInfo = "$startDate - $endDate"
                    period.text = periodInfo
                    ChartUtils.getSortedMapBySymbol(true, startMillis, endMillis)
                }
                else -> {
                    val firstDiary = EasyDiaryDbHelper.selectFirstDiary()
                    val endMillis = System.currentTimeMillis()
                    val startMillis = firstDiary?.currentTimeMillis ?: endMillis
                    dashboardTitle.text = getString(R.string.dashboard_title_lifetime)
                    diaryCount.text = "${EasyDiaryDbHelper.countDiaryAll()}"
                    val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                    val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                    val periodInfo = "$startDate - $endDate"
                    period.text = periodInfo
                    ChartUtils.getSortedMapBySymbol(true)
                }
            }

            if (sortedMap.entries.size > 3) {
                rankingCard.visibility = View.VISIBLE
                guideCard.visibility = View.GONE
                sortedMap.entries.forEachIndexed { index, entry ->
                    when (index) {
                        0 -> {
                            FlavorUtils.initWeatherView(it, symbolRank1, entry.key)
                            descriptionRank1.text = symbolMap[entry.key]
                            countRank1.text = "${entry.value}"
                        }
                        1 -> {
                            FlavorUtils.initWeatherView(it, symbolRank2, entry.key)
                            descriptionRank2.text = symbolMap[entry.key]
                            countRank2.text = "${entry.value}"
                        }
                        2 -> {
                            FlavorUtils.initWeatherView(it, symbolRank3, entry.key)
                            descriptionRank3.text = symbolMap[entry.key]
                            countRank3.text = "${entry.value}"
                        }
                        3 -> {
                            FlavorUtils.initWeatherView(it, symbolRank4, entry.key)
                            descriptionRank4.text = symbolMap[entry.key]
                            countRank4.text = "${entry.value}"
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MODE_FLAG = "mode"
        const val MODE_LIFETIME = "lifetime"
        const val MODE_LAST_MONTH = "lastMonth"
        const val MODE_LAST_WEEK = "lastWeek"
    }
}