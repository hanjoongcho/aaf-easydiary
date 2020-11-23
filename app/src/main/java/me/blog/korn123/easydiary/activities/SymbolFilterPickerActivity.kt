package me.blog.korn123.easydiary.activities

import android.os.Bundle
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SymbolFilterAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import kotlinx.android.synthetic.main.activity_symbol_filter_picker.*
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
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
