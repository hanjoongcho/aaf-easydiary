package me.blog.korn123.easydiary.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_timeline_diary.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.TimelineItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import java.util.*

/**
 * Created by hanjoong on 2017-07-16.
 */

class TimelineActivity : EasyDiaryActivity() {
    private var mTimelineItemAdapter: TimelineItemAdapter? = null
    private var mDiaryList: ArrayList<DiaryDto> = arrayListOf()
    private var mReverseSelection = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline_diary)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.timeline_title)
            setDisplayHomeAsUpEnabled(true)
        }

        mTimelineItemAdapter = TimelineItemAdapter(this, R.layout.item_timeline, mDiaryList)
        timelineList.adapter = mTimelineItemAdapter
        
        setupTimelineSearch()
        insertDiaryButton.setOnClickListener { _ ->
            val createDiary = Intent(this@TimelineActivity, DiaryInsertActivity::class.java)
            TransitionHelper.startActivityWithTransition(this@TimelineActivity, createDiary)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList(searchView.text.toString())
        
        when {
            config.previousActivity == PREVIOUS_ACTIVITY_CREATE -> {
                moveListViewScrollToBottom()
                config.previousActivity = -1
            }
            !mReverseSelection && mDiaryList.size > 0 -> {
                moveListViewScrollToBottom()
                mReverseSelection = true
            }
        }
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

        toggleToolBar.setOnClickListener {
            this.currentFocus?.let {focusView ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                supportActionBar?.run {
                    subtitle = searchView.text
                    FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById(android.R.id.content))
                }
            }
            toolbar.visibility = View.VISIBLE
            searchViewContainer.visibility = View.GONE
        }

        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                refreshList(p0.toString())
                moveListViewScrollToBottom()
            }
        })
    }
    
    private fun refreshList(query: String? = null) {
        mDiaryList.run {
            clear()
            addAll(EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive))
            reverse()
        }
        mTimelineItemAdapter?.notifyDataSetChanged()
    }
    
    private fun moveListViewScrollToBottom() {
        Handler().post { timelineList.setSelection(mDiaryList.size - 1) }
    }
}
