package me.blog.korn123.easydiary.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.TimelineItemAdapter
import me.blog.korn123.easydiary.databinding.ActivityTimelineBinding
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.FILTER_END_DATE
import me.blog.korn123.easydiary.helper.FILTER_END_ENABLE
import me.blog.korn123.easydiary.helper.FILTER_END_MONTH
import me.blog.korn123.easydiary.helper.FILTER_END_YEAR
import me.blog.korn123.easydiary.helper.FILTER_QUERY
import me.blog.korn123.easydiary.helper.FILTER_START_DATE
import me.blog.korn123.easydiary.helper.FILTER_START_ENABLE
import me.blog.korn123.easydiary.helper.FILTER_START_MONTH
import me.blog.korn123.easydiary.helper.FILTER_START_YEAR
import me.blog.korn123.easydiary.helper.FILTER_VIEW_VISIBLE
import me.blog.korn123.easydiary.helper.PREVIOUS_ACTIVITY_CREATE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.theme.AppTheme
import java.util.Calendar
import java.util.Locale
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by hanjoong on 2017-07-16.
 */
@AndroidEntryPoint
class TimelineActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityTimelineBinding
    private lateinit var mSDatePickerDialog: DatePickerDialog
    private lateinit var mEDatePickerDialog: DatePickerDialog
    private var mTimelineItemAdapter: TimelineItemAdapter? = null
    private var mDiaryList: ArrayList<DiaryDomain> = arrayListOf()
    private var mFirstTouch = 0F
    private val mCalendar = Calendar.getInstance(Locale.getDefault())
    private var mSymbolSequence = SYMBOL_SELECT_ALL
    private var mRefreshJob: Job? = null

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding =
            ActivityTimelineBinding.inflate(layoutInflater).apply {
                setContentView(root)
                setSupportActionBar(toolbar)
                partialComposeLoadingScreen.composeView.setContent {
                    AppTheme {
                        AnimatedVisibility(
                            visible = diaryViewModel.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            LoadingScreen(message = diaryViewModel.loadingMessage)
                        }
                    }
                }
            }

        supportActionBar?.run {
            title = getString(R.string.timeline_title)
            setDisplayHomeAsUpEnabled(true)
        }

//        changeDrawableIconColor(config.primaryColor, R.drawable.ic_calendar_4_w)
        changeDrawableIconColor(config.primaryColor, mBinding.partialTimelineFilter.startDatePicker)
        changeDrawableIconColor(config.primaryColor, mBinding.partialTimelineFilter.endDatePicker)

        mTimelineItemAdapter = TimelineItemAdapter(this, R.layout.item_timeline, mDiaryList)
        mBinding.timelineList.adapter = mTimelineItemAdapter

        setupTimelineSearch()

        bindEvent()
        initTextSize(mBinding.partialTimelineFilter.root)

        lifecycleScope.launch {
            when (savedInstanceState) {
                null -> {
                    mSDatePickerDialog =
                        DatePickerDialog(
                            this@TimelineActivity,
                            mStartDateListener,
                            mCalendar.get(Calendar.YEAR),
                            mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                        )
                    mEDatePickerDialog =
                        DatePickerDialog(
                            this@TimelineActivity,
                            mEndDateListener,
                            mCalendar.get(Calendar.YEAR),
                            mCalendar.get(Calendar.MONTH),
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                        )
                    refreshList()
                    moveListViewScrollToBottom()
                }

                else -> {
                    val filterSYear =
                        savedInstanceState.getInt(FILTER_START_YEAR, mCalendar.get(Calendar.YEAR))
                    val filterSMonth =
                        savedInstanceState.getInt(FILTER_START_MONTH, mCalendar.get(Calendar.MONTH))
                    val filterSDate =
                        savedInstanceState.getInt(
                            FILTER_START_DATE,
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                        )
                    if (savedInstanceState.getBoolean(FILTER_START_ENABLE, false)) {
                        Log.i("aaf-t", "get date $filterSYear $filterSMonth $filterSDate")
                        mBinding.partialTimelineFilter.startDate.text =
                            DateUtils.getDateStringFromTimeMillis(
                                EasyDiaryUtils.datePickerToTimeMillis(
                                    filterSDate,
                                    filterSMonth,
                                    filterSYear,
                                ),
                            )
                    }

                    val filterEYear =
                        savedInstanceState.getInt(FILTER_START_YEAR, mCalendar.get(Calendar.YEAR))
                    val filterEMonth =
                        savedInstanceState.getInt(FILTER_START_MONTH, mCalendar.get(Calendar.MONTH))
                    val filterEDate =
                        savedInstanceState.getInt(
                            FILTER_START_DATE,
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                        )
                    if (savedInstanceState.getBoolean(FILTER_END_ENABLE, false)) {
                        mBinding.partialTimelineFilter.endDate.text =
                            DateUtils.getDateStringFromTimeMillis(
                                EasyDiaryUtils.datePickerToTimeMillis(
                                    filterEDate,
                                    filterEMonth,
                                    filterEYear,
                                ),
                            )
                    }

                    mSDatePickerDialog =
                        DatePickerDialog(
                            this@TimelineActivity,
                            mStartDateListener,
                            filterSYear,
                            filterSMonth,
                            filterSDate,
                        )
                    mEDatePickerDialog =
                        DatePickerDialog(
                            this@TimelineActivity,
                            mEndDateListener,
                            filterEYear,
                            filterEMonth,
                            filterEDate,
                        )

                    if (savedInstanceState.getBoolean(
                            FILTER_VIEW_VISIBLE,
                            false,
                        )
                    ) {
                        toggleFilterView(true)
                    }

                    // refreshList call from onTextChanged listener
                    Log.i("aaf-t", "mBinding.partialTimelineFilter.query.setText")
                    mBinding.partialTimelineFilter.query.setText(
                        savedInstanceState.getString(
                            FILTER_QUERY,
                            "",
                        ),
                    )

                    diaryId = savedInstanceState.getInt(DIARY_SEQUENCE, -1)
                }
            }

            selectFeelingSymbol()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mDiaryList.isNotEmpty()) {
            outState.putInt(
                DIARY_SEQUENCE,
                mDiaryList[mBinding.timelineList.firstVisiblePosition].diaryId,
            )
            Log.i("aaf-t", "firstVisiblePosition ${mBinding.timelineList.firstVisiblePosition}")
        }

        if (mBinding.partialTimelineFilter.startDate.text
                .isNotEmpty()
        ) {
            outState.putBoolean(FILTER_START_ENABLE, true)
            outState.putInt(FILTER_START_YEAR, mSDatePickerDialog.datePicker.year)
            outState.putInt(FILTER_START_MONTH, mSDatePickerDialog.datePicker.month)
            outState.putInt(FILTER_START_DATE, mSDatePickerDialog.datePicker.dayOfMonth)
            Log.i(
                "aaf-t",
                "set date ${mSDatePickerDialog.datePicker.year} ${mSDatePickerDialog.datePicker.month} ${mSDatePickerDialog.datePicker.dayOfMonth}",
            )
        }

        if (mBinding.partialTimelineFilter.endDate.text
                .isNotEmpty()
        ) {
            outState.putBoolean(FILTER_END_ENABLE, true)
            outState.putInt(FILTER_END_YEAR, mEDatePickerDialog.datePicker.year)
            outState.putInt(FILTER_END_MONTH, mEDatePickerDialog.datePicker.month)
            outState.putInt(FILTER_END_DATE, mEDatePickerDialog.datePicker.dayOfMonth)
        }

        if (mBinding.partialTimelineFilter.filterView.translationY == 0F) {
            outState.putBoolean(
                FILTER_VIEW_VISIBLE,
                true,
            )
        }

        outState.putString(
            FILTER_QUERY,
            mBinding.partialTimelineFilter.query.text
                .toString(),
        )

        Log.i("aaf-t", "translationY ${mBinding.partialTimelineFilter.filterView.translationY}")

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (config.previousActivity == PREVIOUS_ACTIVITY_CREATE) {
            startRefreshJob()
            moveListViewScrollToBottom()
            config.previousActivity = -1
        } else {
            startRefreshJob()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_timeline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                toggleFilterView(true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    @SuppressLint("ClickableViewAccessibility")
    private fun bindEvent() {
        mBinding.partialTimelineFilter.run {
            mBinding.insertDiaryButton.setOnClickListener { _ ->
                val createDiary = Intent(this@TimelineActivity, DiaryWritingActivity::class.java)
                TransitionHelper.startActivityWithTransition(this@TimelineActivity, createDiary)
            }

            closeToolbar.setOnClickListener {
                toggleFilterView(false)
            }

            filterView.setOnTouchListener { v, motionEvent ->
                if (mFirstTouch == 0F || mFirstTouch < motionEvent.y) mFirstTouch = motionEvent.y

                Log.i("aaf-t", "${motionEvent.action} ${motionEvent.actionIndex} ${motionEvent.y}")
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    if (mFirstTouch - motionEvent.y > 100) {
                        toggleFilterView(false)
                    } else {
                        v.performClick()
                    }
                }
                true
            }

            query.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int,
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int,
                    ) {
                        startRefreshJob(debounce = 300L)
                    }

                    override fun afterTextChanged(editable: Editable) {}
                },
            )

            clearFilter.setOnClickListener {
                startDate.text = null
                endDate.text = null
                query.text = null
                mSymbolSequence = SYMBOL_SELECT_ALL
                FlavorUtils.initWeatherView(this@TimelineActivity, symbol, mSymbolSequence, false)
                startRefreshJob()
            }

            startDatePicker.setOnClickListener { mSDatePickerDialog.show() }
            endDatePicker.setOnClickListener { mEDatePickerDialog.show() }

            feelingSymbolButton.setOnClickListener {
                lifecycleScope.launch {
                    openFeelingSymbolDialog(
                        getString(R.string.diary_symbol_search_message),
                        selectedSymbolSequence = 0,
                        diaryViewModel.getSymbolUsedCountMap(true),
                    ) { symbolSequence ->
                        selectFeelingSymbol(symbolSequence)
                        startRefreshJob()
                    }
                }
            }
        }
    }

    private fun selectFeelingSymbol(index: Int = SYMBOL_SELECT_ALL) {
        mSymbolSequence = if (index == 0) SYMBOL_SELECT_ALL else index
        FlavorUtils.initWeatherView(
            this,
            mBinding.partialTimelineFilter.symbol,
            mSymbolSequence,
            false,
        )
    }

    private var mStartDateListener: DatePickerDialog.OnDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val startMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
            mBinding.partialTimelineFilter.startDate.text =
                DateUtils.getDateStringFromTimeMillis(startMillis)
            startRefreshJob()
            Log.i("aaf-t", "mStartDateListener")
        }

    private var mEndDateListener: DatePickerDialog.OnDateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val endMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
            mBinding.partialTimelineFilter.endDate.text =
                DateUtils.getDateStringFromTimeMillis(endMillis)
            startRefreshJob()
            Log.i("aaf-t", "mEndDateListener")
        }

    private fun toggleFilterView(isVisible: Boolean) {
        mFirstTouch = 0F
        val height =
            if (isVisible) {
                0F
            } else {
                mBinding.partialTimelineFilter.filterView.height
                    .toFloat()
                    .unaryMinus()
            }
        if (!isVisible) {
            this.currentFocus?.let { focusView ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            }
        }
        ObjectAnimator
            .ofFloat(mBinding.partialTimelineFilter.filterView, "translationY", height)
            .apply {
                duration = 700
                start()
            }
    }

    private fun setupTimelineSearch() {
        mBinding.timelineList.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val diaryDto = adapterView.adapter.getItem(i) as DiaryDomain
                val detailIntent = Intent(this@TimelineActivity, DiaryReadingActivity::class.java)
                detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.diaryId)
                detailIntent.putExtra(
                    SELECTED_SEARCH_QUERY,
                    mBinding.partialTimelineFilter.query.text
                        .toString(),
                )
                detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, mSymbolSequence)
                TransitionHelper.startActivityWithTransition(this@TimelineActivity, detailIntent)
            }
    }

    private fun startRefreshJob(debounce: Long = 0L) {
        Log.i("aaf-t", "startRefreshJob")
        mRefreshJob?.cancel()
        mRefreshJob =
            lifecycleScope.launch {
                if (debounce > 0) delay(debounce)
                refreshList()
            }
    }

    private var diaryId = 0

    private suspend fun refreshList() {
        diaryViewModel.isLoading = true
        Log.i("aaf-t", "refreshList")
        var startMillis = 0L
        var endMillis = 0L

        if (mBinding.partialTimelineFilter.startDate.text
                .isNotEmpty()
        ) {
            startMillis =
                EasyDiaryUtils.datePickerToTimeMillis(
                    mSDatePickerDialog.datePicker.dayOfMonth,
                    mSDatePickerDialog.datePicker.month,
                    mSDatePickerDialog.datePicker.year,
                )
        }
        if (mBinding.partialTimelineFilter.endDate.text
                .isNotEmpty()
        ) {
            endMillis =
                EasyDiaryUtils.datePickerToTimeMillis(
                    mEDatePickerDialog.datePicker.dayOfMonth,
                    mEDatePickerDialog.datePicker.month,
                    mEDatePickerDialog.datePicker.year,
                    true,
                )
        }

        val results =
            diaryViewModel.findDiary(
                mBinding.partialTimelineFilter.query.text
                    .toString(),
                config.diarySearchQueryCaseSensitive,
                startMillis,
                endMillis,
                mSymbolSequence,
                true,
            )

        mDiaryList.run {
            clear()
            addAll(results.reversed())
        }

        mTimelineItemAdapter?.run {
            currentQuery =
                mBinding.partialTimelineFilter.query.text
                    .toString()
            notifyDataSetChanged()
        }

        mBinding.run {
            if (mDiaryList.isEmpty()) {
                timelineList.visibility = View.GONE
                textNoDiary.visibility = View.VISIBLE
            } else {
                timelineList.visibility = View.VISIBLE
                textNoDiary.visibility = View.GONE
            }
        }

        val itemIndex = EasyDiaryUtils.sequenceToPageIndex(mDiaryList, diaryId)
        if (diaryId > 0 && itemIndex > -1) {
            moveListViewScrollToBottom(itemIndex)
            diaryId = 0
        }

        diaryViewModel.isLoading = false
    }

    private fun moveListViewScrollToBottom(itemIndex: Int = mDiaryList.size - 1) {
        Log.i("aaf-t", "moveListViewScrollToBottom itexIndex: $itemIndex")
        mBinding.timelineList.post { mBinding.timelineList.setSelection(itemIndex) }
    }
}
