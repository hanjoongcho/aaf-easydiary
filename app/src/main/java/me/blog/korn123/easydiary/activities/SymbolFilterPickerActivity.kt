package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_symbol_filter_picker.*
import kotlinx.android.synthetic.main.activity_symbol_filter_picker.toolbar
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SymbolFilterAdapter
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.extensions.addCategory
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import java.util.*

/**
 * Created by CHO HANJOONG on 2020-11-23.
 */

class SymbolFilterPickerActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mSymbolFilterAdapter: SymbolFilterAdapter
    private var mSymbolFilterList: ArrayList<SymbolFilterAdapter.SymbolFilter> = arrayListOf()


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_symbol_filter_picker)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Dashboard"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        config.selectedSymbols.split(",").map { sequence ->
            mSymbolFilterList.add(SymbolFilterAdapter.SymbolFilter(sequence.toInt()))
        }

        mSymbolFilterAdapter = SymbolFilterAdapter(
                this,
                mSymbolFilterList,
                null
        )

        recyclerView?.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@SymbolFilterPickerActivity, 5)
            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mSymbolFilterAdapter
        }


        val itemList = arrayListOf<Array<String>>()
        val categoryList = arrayListOf<String>()
        addCategory(itemList, categoryList, "weather_item_array", getString(R.string.category_weather))
        addCategory(itemList, categoryList, "emotion_item_array", getString(R.string.category_emotion))
        addCategory(itemList, categoryList, "daily_item_array", getString(R.string.category_daily))
        addCategory(itemList, categoryList, "tasks_item_array", getString(R.string.category_tasks))
        addCategory(itemList, categoryList, "food_item_array", getString(R.string.category_food))
        addCategory(itemList, categoryList, "leisure_item_array", getString(R.string.category_leisure))
        addCategory(itemList, categoryList, "landscape_item_array", getString(R.string.category_landscape))
        addCategory(itemList, categoryList, "symbol_item_array", getString(R.string.category_symbol))
        addCategory(itemList, categoryList, "flag_item_array", getString(R.string.category_flag))

        val symbolPagerAdapter = SymbolPagerAdapter(this, itemList, categoryList) { symbolSequence ->
            makeSnackBar(symbolSequence.toString())
        }
        viewpager.adapter = symbolPagerAdapter
        sliding_tabs.setViewPager(viewpager)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
