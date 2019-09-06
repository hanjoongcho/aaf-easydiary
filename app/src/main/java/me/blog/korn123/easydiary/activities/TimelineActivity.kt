package me.blog.korn123.easydiary.activities

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_timeline_diary.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.TimelineItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import java.util.*


/**
 * Created by hanjoong on 2017-07-16.
 */

class TimelineActivity : EasyDiaryActivity() {
    private lateinit var mSDatePickerDialog: DatePickerDialog
    private lateinit var mEDatePickerDialog: DatePickerDialog
    private var mTimelineItemAdapter: TimelineItemAdapter? = null
    private var mDiaryList: ArrayList<DiaryDto> = arrayListOf()
    private var mReverseSelection = false
    private var mYear = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.YEAR_PATTERN))
    private var mMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.MONTH_PATTERN))
    private var mDayOfMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.DAY_PATTERN))
    private var mFirstTouch = 0F
    private var mStartMillis = 0L
    private var mEndMillis = 0L

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

        EasyDiaryUtils.changeDrawableIconColor(this, config.primaryColor, R.drawable.calendar_4_w)

        insertDiaryButton.setOnClickListener { _ ->
            val createDiary = Intent(this@TimelineActivity, DiaryInsertActivity::class.java)
            TransitionHelper.startActivityWithTransition(this@TimelineActivity, createDiary)
        }

        closeToolbar.setOnClickListener {
            toggleFilterView(false)
        }
        
        filterView.setOnTouchListener { _, motionEvent ->
            if (mFirstTouch == 0F || mFirstTouch < motionEvent.y) mFirstTouch = motionEvent.y
                    
            Log.i("aaf-t", "${motionEvent.action} ${motionEvent.actionIndex} ${motionEvent.y}")
            if (motionEvent.action == MotionEvent.ACTION_UP && (mFirstTouch - motionEvent.y > 200)) {
                toggleFilterView(false)
            }
            true
        }

        mSDatePickerDialog = DatePickerDialog(this, mStartDateListener, mYear, mMonth - 1, mDayOfMonth)
        mEDatePickerDialog = DatePickerDialog(this, mEndDateListener, mYear, mMonth - 1, mDayOfMonth)
        startDatePicker.setOnClickListener { mSDatePickerDialog.show() }
        endDatePicker.setOnClickListener { mEDatePickerDialog.show() }

        query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                refreshList(charSequence.toString(), mStartMillis, mEndMillis)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        initTextSize(filterView, this)
    }
    
    private var mStartDateListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        mStartMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
        startDate.text = DateUtils.getFullPatternDate(mStartMillis)
        refreshList(null, mStartMillis, mEndMillis)
    }
    
    private var mEndDateListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        mEndMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
        endDate.text = DateUtils.getFullPatternDate(mEndMillis)
        refreshList(null, mStartMillis, mEndMillis)
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
                toggleFilterView(true)
//                toolbar.visibility = View.GONE
//                searchViewContainer.visibility = View.VISIBLE
//                searchView.requestFocus()
//                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleFilterView(isVisible: Boolean) {
        mFirstTouch = 0F
        val height = if (isVisible) 0F else filterView.height.toFloat().unaryMinus()
        if (!isVisible) {
            this.currentFocus?.let { focusView ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            }
        }
        ObjectAnimator.ofFloat(filterView, "translationY", height).apply {
            duration = 700
            start()
        }
    }

    private fun setupTimelineSearch() {
        timelineList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val diaryDto = adapterView.adapter.getItem(i) as DiaryDto
            val detailIntent = Intent(this@TimelineActivity, DiaryReadActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.sequence)
            detailIntent.putExtra(DIARY_SEARCH_QUERY, searchView.text.toString())
            TransitionHelper.startActivityWithTransition(this@TimelineActivity, detailIntent)
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
    
    private fun refreshList(query: String? = null, startTimeMillis: Long = 0, endTimeMillis: Long = 0) {
        mDiaryList.run {
            clear()
            addAll(EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive, startTimeMillis, endTimeMillis))
            reverse()
        }
        mTimelineItemAdapter?.notifyDataSetChanged()
    }
    
    private fun moveListViewScrollToBottom() {
        Handler().post { timelineList.setSelection(mDiaryList.size - 1) }
    }
}
