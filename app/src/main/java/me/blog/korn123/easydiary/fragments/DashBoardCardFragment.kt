package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.blog.korn123.commons.utils.ChartUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import kotlinx.android.synthetic.main.fragment_dashboard_card.*

class DashBoardCardFragment : androidx.fragment.app.Fragment() {

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard_card, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        context?.let {
            val symbolMap = FlavorUtils.getDiarySymbolMap(it)
            val sortedMap = ChartUtils.getSortedMapBySymbol(true)
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
                countDiary.text = EasyDiaryDbHelper.readDiary(null).size.toString()
            }
        }
    }
}