package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidFragmentEx
import com.roomorama.caldroid.CaldroidListener
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_calendar.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryCalendarItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.fragments.CalendarFragment
import me.blog.korn123.easydiary.helper.DEFAULT_CALENDAR_FONT_SCALE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hanjoong on 2017-03-28.
 */

class CalendarActivity : EasyDiaryActivity() {
    private lateinit var calendarFragment: CaldroidFragmentEx
    private lateinit var mCurrentDate: Date
    private var mDiaryList: MutableList<DiaryDto> = mutableListOf()
    private var mArrayAdapterDiary: ArrayAdapter<DiaryDto>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (config.settingCalendarFontScale == DEFAULT_CALENDAR_FONT_SCALE) {
            setContentView(R.layout.activity_calendar)
        } else {
            setContentView(R.layout.activity_calendar_scale)
        }
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.calendar_title)
            setDisplayHomeAsUpEnabled(true)    
        }

        if (config.enableCardViewPolicy) {
            calendarCard.useCompatPadding = true
            calendarCard.cardElevation = CommonUtils.dpToPixelFloatValue(this, 2F)
        } else {
            calendarCard.useCompatPadding = false
            calendarCard.cardElevation = 0F
        }
        
        mCurrentDate = Calendar.getInstance().time
        val cal = Calendar.getInstance()
        val currentDate = cal.time
        refreshList(currentDate)
        mArrayAdapterDiary = DiaryCalendarItemAdapter(this, R.layout.item_diary_calendar, this.mDiaryList)
        selectedList.adapter = mArrayAdapterDiary
        selectedList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val diaryDto = adapterView.adapter.getItem(i) as DiaryDto
            val detailIntent = Intent(this@CalendarActivity, DiaryReadActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.sequence)
            detailIntent.putExtra("title", diaryDto.title)
            detailIntent.putExtra("contents", diaryDto.contents)
            detailIntent.putExtra("date", DateUtils.timeMillisToDateTime(diaryDto.currentTimeMillis, "yyyy-MM-dd HH:mm:ss"))
            detailIntent.putExtra("current_time_millis", diaryDto.currentTimeMillis)
            detailIntent.putExtra("weather", diaryDto.weather)
            TransitionHelper.startActivityWithTransition(this@CalendarActivity, detailIntent)
        }

        calendarFragment = CalendarFragment()

        // Setup arguments
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            calendarFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE")
        } else {
            val args = Bundle()
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1)
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR))
//            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY)
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, config.calendarStartDay)
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true)
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true)

            calendarFragment.arguments = args
        }

        calendarFragment.setSelectedDate(currentDate)
        //        setCustomResourceForDates();

        // Attach to the activity
        val t = supportFragmentManager.beginTransaction()
        t.replace(R.id.calendar1, calendarFragment)
        t.commit()

        // Setup listener
        val listener = object : CaldroidListener() {

            override fun onSelectDate(date: Date, view: View) {
                //                Toast.makeText(getApplicationContext(), formatter.format(date),
                //                        Toast.LENGTH_SHORT).show();
                //                ColorDrawable green = new ColorDrawable(Color.GREEN);
                //                calendarFragment.setBackgroundDrawableForDate(green, date);
                calendarFragment.clearSelectedDates()
                calendarFragment.setSelectedDate(date)
                calendarFragment.refreshView()
                mCurrentDate = date
                refreshList(date)
            }

            override fun onChangeMonth(month: Int, year: Int) {
                val monthYearFlag = (android.text.format.DateUtils.FORMAT_SHOW_DATE
                        or android.text.format.DateUtils.FORMAT_NO_MONTH_DAY or android.text.format.DateUtils.FORMAT_SHOW_YEAR)
                val monthYearStringBuilder = StringBuilder(50)
                val monthYearFormatter = Formatter(monthYearStringBuilder, Locale.getDefault())
                val format = SimpleDateFormat("yyyyMM", Locale.getDefault())
                val dateTimeString = "$year${StringUtils.leftPad(month.toString(), 2, "0")}"
                val parsedDate = format.parse(dateTimeString).time
                val monthTitle = android.text.format.DateUtils.formatDateRange(this@CalendarActivity, monthYearFormatter, parsedDate, parsedDate, monthYearFlag).toString()
                supportActionBar?.subtitle = monthTitle.toUpperCase(Locale.getDefault())
            }
            override fun onLongClickDate(date: Date?, view: View?) { }
            override fun onCaldroidViewCreated() { }
        }

        // Setup Caldroid
        calendarFragment.caldroidListener = listener
    }

    override fun onResume() {
        super.onResume()
        refreshList(mCurrentDate)
        calendarFragment.refreshView()
    }
    
    /**
     * Save current states of the Caldroid here
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState!!)
        calendarFragment.saveStatesToKey(outState!!, "CALDROID_SAVED_STATE")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_calendar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.previous -> calendarFragment.prevMonth()
            R.id.next -> calendarFragment.nextMonth()
        }
        return super.onOptionsItemSelected(item)
    }

    fun refreshList(date: Date) {
        val formatter = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH)

        mDiaryList.clear()
        mDiaryList.addAll(EasyDiaryDbHelper.readDiaryByDateString(formatter.format(date)))
        mArrayAdapterDiary?.notifyDataSetChanged()
        selectedList.setSelection(0)

        if (mDiaryList.size > 0) {
            selectedList.visibility = View.VISIBLE
            emptyInfo.visibility = View.GONE
        } else {
            selectedList.visibility = View.GONE
            emptyInfo.visibility = View.VISIBLE
        }
    }
}
