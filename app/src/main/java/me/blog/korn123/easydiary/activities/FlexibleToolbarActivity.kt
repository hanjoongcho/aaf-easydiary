package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.AbsListView
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.ksoichiro.android.observablescrollview.ObservableListView
import kotlinx.android.synthetic.main.activity_flexible_toolbar.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class FlexibleToolbarActivity : ToolbarControlBaseActivity<ObservableListView>() {

    private var mRecognizerIntent: Intent? = null

    private var mCurrentTimeMillis: Long = 0

    private var mDiaryMainItemAdapter: DiaryMainItemAdapter? = null

    private var mDiaryList: MutableList<DiaryDto>? = null

    private var mShowcaseIndex = 0

    private var mShowcaseView: ShowcaseView? = null
    
    override fun getLayoutResId(): Int {
        return R.layout.activity_flexible_toolbar
    }
    
    override fun createScrollable(): ObservableListView {
//        setDummyData(diaryList, 50)

        // ObservableListView uses setOnScrollListener, but it still works.
        diaryList.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            }
        })
        return diaryList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDiaryList = EasyDiaryDbHelper.readDiary(null)
        mDiaryList?.let {
            mDiaryMainItemAdapter = DiaryMainItemAdapter(this, R.layout.item_diary_main, it)
        }
        diaryList.adapter = mDiaryMainItemAdapter
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_main, menu)
        return true
    }
    
//    protected fun setDummyData(listView: ListView, num: Int) {
//        listView.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, getDummyData(num)))
//    }
//
//    fun getDummyData(num: Int): ArrayList<String> {
//        val items = ArrayList<String>()
//        for (i in 1..num) {
//            items.add("Item $i")
//        }
//        return items
//    }
}
