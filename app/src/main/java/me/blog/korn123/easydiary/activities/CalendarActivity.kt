package me.blog.korn123.easydiary.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidFragmentEx
import com.roomorama.caldroid.CaldroidListener
import io.realm.Sort
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryWritingActivity.Companion.INITIALIZE_TIME_MILLIS
import me.blog.korn123.easydiary.adapters.DiaryCalendarItemAdapter
import me.blog.korn123.easydiary.databinding.ActivityCalendarBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixelFloatValue
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.fragments.CalendarFragment
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.DEFAULT_CALENDAR_FONT_SCALE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Formatter
import java.util.Locale

/**
 * Created by Hanjoong.Cho on 2017-03-28
 * Refactored code on 2020-01-03
 *
 */

class CalendarActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityCalendarBinding
    private lateinit var mCalendarFragment: CaldroidFragmentEx
    private lateinit var mDatePickerDialog: DatePickerDialog
    private val mCalendar = Calendar.getInstance(Locale.getDefault())
    private var mDiaryList: MutableList<Diary> = mutableListOf()
    private var mArrayAdapterDiary: ArrayAdapter<Diary>? = null

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        if (config.settingCalendarFontScale != DEFAULT_CALENDAR_FONT_SCALE && !isLandScape()) {
            mBinding.calendarCard.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 6F)
            mBinding.frameBottom?.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 4F)
        }
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = getString(R.string.calendar_title)
            setDisplayHomeAsUpEnabled(true)
        }

        mDatePickerDialog =
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    mCalendar.set(year, month, dayOfMonth)
                    mCalendarFragment.moveToDate(mCalendar.time)
                    selectDateAndRefreshView()
                },
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH),
            )

        if (config.enableCardViewPolicy) {
            mBinding.calendarCard.useCompatPadding = true
            mBinding.calendarCard.cardElevation = dpToPixelFloatValue(2F)
        } else {
            mBinding.calendarCard.useCompatPadding = false
            mBinding.calendarCard.cardElevation = 0F
        }

        val cal = Calendar.getInstance()
        val currentDate = cal.time
        refreshList()
        mArrayAdapterDiary =
            DiaryCalendarItemAdapter(this, R.layout.item_diary_calendar, this.mDiaryList)
        mBinding.selectedList.adapter = mArrayAdapterDiary
        mBinding.selectedList.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val diaryDto = adapterView.adapter.getItem(i) as Diary
                val detailIntent = Intent(this@CalendarActivity, DiaryReadingActivity::class.java)
                detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.sequence)
                TransitionHelper.startActivityWithTransition(this@CalendarActivity, detailIntent)
            }

        mCalendarFragment = CalendarFragment()

        // Setup arguments
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            mCalendarFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE")
        } else {
            val args = Bundle()
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1)
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR))
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, config.calendarStartDay)
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true)
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true)

            mCalendarFragment.arguments = args
        }

        mCalendarFragment.setSelectedDate(currentDate)

        // Attach to the activity
        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.calendar1, mCalendarFragment)
        t.commit()

        mCalendarFragment.caldroidListener =
            object : CaldroidListener() {
                override fun onSelectDate(
                    date: Date,
                    view: View,
                ) {
                    mCalendar.time = date
                    syncDatePicker()
                    selectDateAndRefreshView()
                }

                override fun onChangeMonth(
                    month: Int,
                    year: Int,
                ) {
                    val monthYearFlag =
                        android.text.format.DateUtils.FORMAT_SHOW_DATE or android.text.format.DateUtils.FORMAT_NO_MONTH_DAY or android.text.format.DateUtils.FORMAT_SHOW_YEAR
                    val monthYearFormatter = Formatter(StringBuilder(50), Locale.getDefault())
                    val calendar = Calendar.getInstance(Locale.getDefault())
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month - 1)
                    calendar.set(Calendar.DATE, 1)
                    val monthTitle =
                        android.text.format.DateUtils
                            .formatDateRange(
                                this@CalendarActivity,
                                monthYearFormatter,
                                calendar.timeInMillis,
                                calendar.timeInMillis,
                                monthYearFlag,
                            ).toString()
                    supportActionBar?.subtitle = monthTitle.uppercase(Locale.getDefault())
                }

                override fun onLongClickDate(
                    date: Date?,
                    view: View?,
                ) {}

                override fun onCaldroidViewCreated() {}
            }

        mBinding.writeDiary.setOnClickListener {
            TransitionHelper.startActivityWithTransition(
                this,
                Intent(this, DiaryWritingActivity::class.java).apply {
                    putExtra(INITIALIZE_TIME_MILLIS, mCalendar.timeInMillis)
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        mCalendarFragment.refreshView()
    }

    /**
     * Save current states of the Caldroid here
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState)
        mCalendarFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_calendar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.previous -> mCalendarFragment.prevMonth()
            R.id.next -> mCalendarFragment.nextMonth()
            R.id.datePicker -> mDatePickerDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun refreshList() {
        val formatter = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH, Locale.getDefault())
        val sort: Sort =
            if (config.calendarSorting == CALENDAR_SORTING_ASC) Sort.ASCENDING else Sort.DESCENDING
        mDiaryList.clear()
        mDiaryList.addAll(
            EasyDiaryDbHelper.findDiaryByDateString(
                formatter.format(mCalendar.time),
                sort,
            ),
        )
        mArrayAdapterDiary?.notifyDataSetChanged()
        mBinding.selectedList.setSelection(0)

        if (mDiaryList.size > 0) {
            mBinding.selectedList.visibility = View.VISIBLE
            mBinding.emptyInfo.visibility = View.GONE
        } else {
            mBinding.selectedList.visibility = View.GONE
            mBinding.emptyInfo.visibility = View.VISIBLE
        }
    }

    private fun selectDateAndRefreshView() {
        mCalendarFragment.clearSelectedDates()
        mCalendarFragment.setSelectedDate(mCalendar.time)
        mCalendarFragment.refreshViewOnlyCurrentPage()
        refreshList()
    }

    private fun syncDatePicker() {
        mDatePickerDialog.updateDate(
            mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH),
            mCalendar.get(Calendar.DAY_OF_MONTH),
        )
    }
}
