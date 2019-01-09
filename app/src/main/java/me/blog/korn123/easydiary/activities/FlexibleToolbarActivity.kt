package me.blog.korn123.easydiary.activities

import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.github.ksoichiro.android.observablescrollview.ObservableListView
import kotlinx.android.synthetic.main.activity_flexible_toolbar.*
import me.blog.korn123.easydiary.R


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class FlexibleToolbarActivity : ToolbarControlBaseActivity<ObservableListView>() {

    override fun getLayoutResId(): Int {
        return R.layout.activity_flexible_toolbar
    }
    
    override fun createScrollable(): ObservableListView {
        setDummyData(diaryList, 50)

        // ObservableListView uses setOnScrollListener, but it still works.
        diaryList.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            }
        })
        return diaryList
    }

    protected fun setDummyData(listView: ListView, num: Int) {
        listView.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, getDummyData(num)))
    }

    fun getDummyData(num: Int): ArrayList<String> {
        val items = ArrayList<String>()
        for (i in 1..num) {
            items.add("Item $i")
        }
        return items
    }
}
