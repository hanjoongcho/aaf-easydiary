package me.blog.korn123.easydiary.activities

import android.graphics.Rect
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SymbolFilterAdapter
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.databinding.ActivitySymbolFilterPickerBinding
import me.blog.korn123.easydiary.extensions.addCategory
import me.blog.korn123.easydiary.extensions.addUserCustomSymbols
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import java.util.*

class SymbolFilterPickerActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivitySymbolFilterPickerBinding
    private lateinit var symbolFilterAdapter: SymbolFilterAdapter
    private var symbolFilterList: ArrayList<SymbolFilterAdapter.SymbolFilter> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySymbolFilterPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = "Symbol Picker"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        symbolFilterAdapter =
            SymbolFilterAdapter(
                this,
                symbolFilterList,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val filteredList =
                        config.selectedSymbols
                            .split(",")
                            .filter { sequence -> sequence.toInt() != symbolFilterList[position].sequence }
                    if (filteredList.isNotEmpty()) {
                        config.selectedSymbols = filteredList.joinToString(",")
                        updateSymbolFilter()
                    } else {
                        showAlertDialog(getString(R.string.symbol_filter_picker_remove_guide_message), null)
                    }
                },
            )

        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@SymbolFilterPickerActivity, 4)
            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.component_margin_small)))
            adapter = symbolFilterAdapter
        }
        updateSymbolFilter()

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
        addUserCustomSymbols(categoryList, itemList)

        val symbolPagerAdapter =
            SymbolPagerAdapter(this, itemList, categoryList) { symbolSequence ->
                if (symbolSequence > 0) {
                    config.selectedSymbols.split(",").find { it.toInt() == symbolSequence }?.let {
                        showAlertDialog("The selected symbol already exists in the filter list.", null)
                    } ?: run {
                        config.selectedSymbols = config.selectedSymbols + "," + symbolSequence
                        updateSymbolFilter(true)
                    }
                }
            }
        binding.run {
            viewpager.run {
                setBackgroundColor(config.backgroundColor)
                adapter = symbolPagerAdapter
                slidingTabs.setViewPager(this)
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setResult(RESULT_OK)
                    finishActivityWithPauseLock()
                }
            },
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_OK)
                finishActivityWithPauseLock()
            }
        }
        return true
    }

    private fun updateSymbolFilter(scrollToBottom: Boolean = false) {
        symbolFilterList.clear()
        config.selectedSymbols.split(",").map { sequence ->
            symbolFilterList.add(SymbolFilterAdapter.SymbolFilter(sequence.toInt()))
        }
        symbolFilterAdapter.notifyDataSetChanged()
        if (scrollToBottom) binding.recyclerView.smoothScrollToPosition(symbolFilterList.size.minus(1))
    }

    class SpacesItemDecoration(
        private val space: Int,
    ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: androidx.recyclerview.widget.RecyclerView,
            state: androidx.recyclerview.widget.RecyclerView.State,
        ) {
            outRect.top = space
            outRect.bottom = space
            outRect.left = space
            outRect.right = space
        }
    }
}
