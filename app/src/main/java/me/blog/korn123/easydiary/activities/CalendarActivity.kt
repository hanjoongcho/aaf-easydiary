package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidListener
import kotlinx.android.synthetic.main.activity_calendar.*
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryCalendarItemAdapter
import me.blog.korn123.easydiary.fragments.CaldroidCustomFragment
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hanjoong on 2017-03-28.
 */

class CalendarActivity : EasyDiaryActivity() {
    private lateinit var calendarFragment: CaldroidFragment
    private lateinit var mCurrentDate: Date
    private var mDiaryList: MutableList<DiaryDto> = mutableListOf()
    private var mArrayAdapterDiary: ArrayAdapter<DiaryDto>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.calendar_title)
            setDisplayHomeAsUpEnabled(true)    
        }
        
        setFontsStyle()
        mCurrentDate = Calendar.getInstance().time
        val cal = Calendar.getInstance()
        val currentDate = cal.time
        refreshList(currentDate)
        mArrayAdapterDiary = DiaryCalendarItemAdapter(this, R.layout.item_diary_calendar, this.mDiaryList!!)
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
            startActivity(detailIntent)
        }

        // Setup caldroid fragment
        // **** If you want normal CaldroidFragment, use below line ****
        //        calendarFragment = new CaldroidFragment();

        // //////////////////////////////////////////////////////////////////////
        // **** This is to show customized fragment. If you want customized
        // version, uncomment below line ****
        calendarFragment = CaldroidCustomFragment()

        // Setup arguments

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            calendarFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE")
        } else {
            val args = Bundle()
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1)
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR))
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true)
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true)

            // Uncomment this to customize startDayOfWeek
            // args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
            // CaldroidFragment.TUESDAY); // Tuesday

            // Uncomment this line to use Caldroid in compact mode
            // args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);

            // Uncomment this line to use dark theme
            //            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);

            calendarFragment.arguments = args
        }// If activity is created from fresh

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
                val text = "month: $month year: $year"
                //                Toast.makeText(getApplicationContext(), text,
                //                        Toast.LENGTH_SHORT).show();
            }

            override fun onLongClickDate(date: Date?, view: View?) {
                //                Toast.makeText(getApplicationContext(),
                //                        "Long click " + formatter.format(date),
                //                        Toast.LENGTH_SHORT).show();
            }

            override fun onCaldroidViewCreated() {
                if (calendarFragment.leftArrowButton != null) {
                    //                    Toast.makeText(getApplicationContext(),
                    //                            "Caldroid view is created", Toast.LENGTH_SHORT)
                    //                            .show();
                }
            }

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
    override fun onSaveInstanceState(outState: Bundle?) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState)
        calendarFragment.saveStatesToKey(outState!!, "CALDROID_SAVED_STATE")
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<ViewGroup>(android.R.id.content))
    }

    fun refreshList(date: Date) {
        val formatter = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH)

        mDiaryList.clear()
        mDiaryList.addAll(EasyDiaryDbHelper.readDiaryByDateString(formatter.format(date)))
        mArrayAdapterDiary?.notifyDataSetChanged()

        if (mDiaryList.size > 0) {
            selectedList.visibility = View.VISIBLE
            emptyInfo.visibility = View.GONE
        } else {
            selectedList.visibility = View.GONE
            emptyInfo.visibility = View.VISIBLE
        }
    }
}
