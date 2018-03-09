package me.blog.korn123.easydiary.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_timeline_diary.*
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.TimelineItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.DIARY_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.DIARY_SEARCH_QUERY_CASE_SENSITIVE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto
import java.util.*

/**
 * Created by hanjoong on 2017-07-16.
 */

class TimelineActivity : EasyDiaryActivity() {
    private var mTimelineItemAdapter: TimelineItemAdapter? = null
    private var mDiaryList: ArrayList<DiaryDto>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline_diary)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.timeline_title)
            setDisplayHomeAsUpEnabled(true)
        }

        mDiaryList = EasyDiaryDbHelper.readDiary(null)
        mDiaryList?.let {
            Collections.reverse(it)
            mTimelineItemAdapter = TimelineItemAdapter(this, R.layout.item_timeline, it)
            timelineList.adapter = mTimelineItemAdapter
            timelineList.setSelection(it.size - 1)
        }

        setupTimelineSearch()
    }

    override fun onResume() {
        super.onResume()
        refreshList(searchView.text.toString())
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_timeline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                toolbar.visibility = View.GONE
                searchViewContainer.visibility = View.VISIBLE
                searchView.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun setupTimelineSearch() {
        timelineList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val diaryDto = adapterView.adapter.getItem(i) as DiaryDto
            val detailIntent = Intent(this@TimelineActivity, DiaryReadActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.sequence)
            detailIntent.putExtra(DIARY_SEARCH_QUERY, searchView.text.toString())
            startActivity(detailIntent)
        }

        toggleToolBar.setOnClickListener({
            toolbar.visibility = View.VISIBLE
            searchViewContainer.visibility = View.GONE
            val focusView = this.currentFocus
            focusView?.let {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
                supportActionBar?.run {
                    subtitle = searchView.text
                    FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
                }
            }
        })

        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                refreshList(p0.toString())
            }
        })
    }
    
    private fun refreshList(query: String?) {
        mDiaryList?.clear()
        mDiaryList?.addAll(EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive))
        Collections.reverse(mDiaryList)
        mTimelineItemAdapter?.notifyDataSetChanged()
    }
}
