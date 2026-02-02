package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentDashboardRankBinding
import me.blog.korn123.easydiary.helper.DashboardConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import java.text.SimpleDateFormat
import java.util.*

class DashBoardRankFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBinding: FragmentDashboardRankBinding

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentDashboardRankBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.run {
            val symbolMap = FlavorUtils.getDiarySymbolMap(requireContext())
            val sortedMap =
                when (arguments?.getString(DashboardConstants.MODE_FLAG, DashboardConstants.MODE_LIFETIME)) {
                    DashboardConstants.MODE_LAST_MONTH -> {
                        val calendar = Calendar.getInstance()
                        val endMillis = calendar.timeInMillis
                        calendar.add(Calendar.DATE, -30)
                        val startMillis = calendar.timeInMillis
                        dashboardTitle.text = getString(R.string.dashboard_title_last_month)
                        diaryCount.text = EasyDiaryDbHelper.findDiary(null, true, startMillis, endMillis).size.toString()
                        val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                        val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                        val periodInfo = "$startDate - $endDate"
                        period.text = periodInfo
                        EasyDiaryUtils.getSymbolUsedCountMap(true, startMillis, endMillis)
                    }

                    DashboardConstants.MODE_LAST_WEEK -> {
                        val calendar = Calendar.getInstance()
                        val endMillis = calendar.timeInMillis
                        calendar.add(Calendar.DATE, -7)
                        val startMillis = calendar.timeInMillis
                        dashboardTitle.text = getString(R.string.dashboard_title_last_week)
                        diaryCount.text = EasyDiaryDbHelper.findDiary(null, true, startMillis, endMillis).size.toString()
                        val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                        val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                        val periodInfo = "$startDate - $endDate"
                        period.text = periodInfo
                        EasyDiaryUtils.getSymbolUsedCountMap(true, startMillis, endMillis)
                    }

                    else -> {
                        val firstDiary = EasyDiaryDbHelper.findFirstDiary()
                        val endMillis = System.currentTimeMillis()
                        val startMillis = firstDiary?.currentTimeMillis ?: endMillis
                        dashboardTitle.text = getString(R.string.dashboard_title_lifetime)
                        diaryCount.text = "${EasyDiaryDbHelper.countDiaryAll()}"
                        val startDate = DateUtils.getDateStringFromTimeMillis(startMillis, SimpleDateFormat.MEDIUM)
                        val endDate = DateUtils.getDateStringFromTimeMillis(endMillis, SimpleDateFormat.MEDIUM)
                        val periodInfo = "$startDate - $endDate"
                        period.text = periodInfo
                        EasyDiaryUtils.getSymbolUsedCountMap(true)
                    }
                }

            if (sortedMap.entries.size > 3) {
                rankingCard.visibility = View.VISIBLE
                guideCard.visibility = View.GONE
                sortedMap.entries.forEachIndexed { index, entry ->
                    when (index) {
                        0 -> {
                            FlavorUtils.initWeatherView(requireContext(), symbolRank1, entry.key)
                            descriptionRank1.text = symbolMap[entry.key]
                            countRank1.text = "${entry.value}"
                        }

                        1 -> {
                            FlavorUtils.initWeatherView(requireContext(), symbolRank2, entry.key)
                            descriptionRank2.text = symbolMap[entry.key]
                            countRank2.text = "${entry.value}"
                        }

                        2 -> {
                            FlavorUtils.initWeatherView(requireContext(), symbolRank3, entry.key)
                            descriptionRank3.text = symbolMap[entry.key]
                            countRank3.text = "${entry.value}"
                        }

                        3 -> {
                            FlavorUtils.initWeatherView(requireContext(), symbolRank4, entry.key)
                            descriptionRank4.text = symbolMap[entry.key]
                            countRank4.text = "${entry.value}"
                        }
                    }
                }
            }
        }
    }
}
