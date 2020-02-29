package com.roomorama.caldroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.Time
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager

import com.antonyt.infiniteviewpager.InfinitePagerAdapter
import com.antonyt.infiniteviewpager.InfiniteViewPager
import com.caldroid.R

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Formatter
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

import hirondelle.date4j.DateTime

/**
 * Caldroid is a fragment that display calendar with dates in a month. Caldroid
 * can be used as embedded fragment, or as dialog fragment. <br></br>
 * <br></br>
 * Caldroid fragment includes 4 main parts:<br></br>
 * <br></br>
 * 1) Month title view: show the month and year (e.g MARCH, 2013) <br></br>
 * <br></br>
 * 2) Navigation arrows: to navigate to next month or previous month <br></br>
 * <br></br>
 * 3) Weekday gridview: contains only 1 row and 7 columns. To display
 * "SUN, MON, TUE, WED, THU, FRI, SAT" <br></br>
 * <br></br>
 * 4) An infinite view pager that allow user to swipe left/right to change
 * month. This library is taken from
 * https://github.com/antonyt/InfiniteViewPager <br></br>
 * <br></br>
 * This infinite view pager recycles 4 fragment, each fragment contains a grid
 * view with 7 columns to display the dates in month. Whenever user swipes
 * different screen, the date grid views are updated. <br></br>
 * <br></br>
 * Caldroid fragment supports setting min/max date, selecting dates in a range,
 * setting disabled dates, highlighting today. It includes convenient methods to
 * work with date and string, enable or disable the navigation arrows. User can
 * also swipe left/right to change months.<br></br>
 * <br></br>
 * Caldroid code is simple and clean partly because of powerful Date4J DateTime
 * library!
 *
 * @author thomasdao
 */

@SuppressLint("DefaultLocale")
abstract class CaldroidFragmentEx : DialogFragment() {

    /**
     * First day of month time
     */
    private val firstMonthTime = Time()

    /**
     * Reuse formatter to print "MMMM yyyy" format
     */
    private val monthYearStringBuilder = StringBuilder(50)
    private val monthYearFormatter = Formatter(
            monthYearStringBuilder, Locale.getDefault())

    /**
     * Caldroid view components
     */
    /**
     * To let user customize the navigation buttons
     */
    var leftArrowButton: Button? = null
        private set
    var rightArrowButton: Button? = null
        private set
    /**
     * To let client customize month title textview
     */
    var monthTitleTextView: TextView? = null
    /**
     * For client to customize the weekDayGridView
     *
     * @return
     */
    var weekdayGridView: GridView? = null
        private set
    /**
     * For client wants to access dateViewPager
     *
     * @return
     */
    var dateViewPager: InfiniteViewPager? = null
        private set
    private var pageChangeListener: DatePageChangeListener? = null
    /**
     * For client to access array of rotating fragments
     */
    var fragments: ArrayList<DateGridFragment>? = null
        private set

    var themeResource = R.style.CaldroidDefault

    /**
     * Initial data
     */
    protected var dialogTitle: String? = null
    /**
     * Retrieve current month
     * @return
     */
    var month = -1
        protected set
    /**
     * Retrieve current year
     * @return
     */
    var year = -1
        protected set
    protected var mDisableDates: ArrayList<DateTime>? = ArrayList()
    protected var selectedDates: ArrayList<DateTime>? = ArrayList()
    protected var minDateTime: DateTime? = null
    protected var maxDateTime: DateTime? = null
    protected lateinit var dateInMonthsList: ArrayList<DateTime>

    /**
     * mCaldroidData belongs to Caldroid
     */
    protected var mCaldroidData: MutableMap<String, Any?> = HashMap()

    /**
     * extraData belongs to client
     */
    /**
     * Extra data is data belong to Client
     *
     * @return
     */
    /**
     * Client can set custom data in this HashMap
     *
     * @param extraData
     */
    var extraData: Map<String, Any> = HashMap()

    /**
     * backgroundForDateMap holds background resource for each date
     */
    protected var mBackgroundForDateTimeMap: MutableMap<DateTime, Drawable> = HashMap()

    /**
     * textColorForDateMap holds color for text for each date
     */
    protected var mTextColorForDateTimeMap: MutableMap<DateTime, Int> = HashMap()

    /**
     * First column of calendar is Sunday
     */
    protected var startDayOfWeek = SUNDAY

    /**
     * A calendar height is not fixed, it may have 5 or 6 rows. Set fitAllMonths
     * to true so that the calendar will always have 6 rows
     */
    private var sixWeeksInCalendar = true

    /**
     * datePagerAdapters hold 4 adapters, meant to be reused
     */
    /**
     * Get 4 adapters of the date grid views. Useful to set custom data and
     * refresh date grid view
     *
     * @return
     */
    var datePagerAdapters = ArrayList<CaldroidGridAdapter>()
        protected set

    /**
     * To control the navigation
     */
    protected var mEnableSwipe = true
    protected var mShowNavigationArrows = true
    protected var enableClickOnDisabledDates = false

    /**
     * To use SquareTextView to display Date cell.By default, it is true,
     * however in many cases with compact screen, it can be collapsed to save space
     */
    protected var squareTextViewCell: Boolean = false

    /**
     * dateItemClickListener is fired when user click on the date cell
     */
    private var dateItemClickListener: OnItemClickListener? = null

    /**
     * dateItemLongClickListener is fired when user does a longclick on the date
     * cell
     */
    private var dateItemLongClickListener: OnItemLongClickListener? = null

    /**
     * caldroidListener inform library client of the event happens inside
     * Caldroid
     */
    /**
     * Set caldroid listener when user click on a date
     *
     * @param caldroidListener
     */
    var caldroidListener: CaldroidListener? = null

    /**
     * Get current saved sates of the Caldroid. Useful for handling rotation.
     * It does not need to save state of SQUARE_TEXT_VIEW_CELL because this
     * may change on orientation change
     */
    val savedStates: Bundle
        get() {
            val bundle = Bundle()
            bundle.putInt(MONTH, month)
            bundle.putInt(YEAR, year)

            if (dialogTitle != null) {
                bundle.putString(DIALOG_TITLE, dialogTitle)
            }

            if (selectedDates != null && selectedDates!!.size > 0) {
                bundle.putStringArrayList(SELECTED_DATES,
                        CalendarHelper.convertToStringList(selectedDates!!))
            }

            if (mDisableDates != null && mDisableDates!!.size > 0) {
                bundle.putStringArrayList(DISABLE_DATES,
                        CalendarHelper.convertToStringList(mDisableDates!!))
            }

            if (minDateTime != null) {
                bundle.putString(MIN_DATE, minDateTime!!.format("YYYY-MM-DD"))
            }

            if (maxDateTime != null) {
                bundle.putString(MAX_DATE, maxDateTime!!.format("YYYY-MM-DD"))
            }

            bundle.putBoolean(SHOW_NAVIGATION_ARROWS, mShowNavigationArrows)
            bundle.putBoolean(ENABLE_SWIPE, mEnableSwipe)
            bundle.putInt(START_DAY_OF_WEEK, startDayOfWeek)
            bundle.putBoolean(SIX_WEEKS_IN_CALENDAR, sixWeeksInCalendar)
            bundle.putInt(THEME_RESOURCE, themeResource)

            val args = arguments
            if (args != null && args.containsKey(SQUARE_TEXT_VIEW_CELL)) {
                bundle.putBoolean(SQUARE_TEXT_VIEW_CELL, args.getBoolean(SQUARE_TEXT_VIEW_CELL))
            }

            return bundle
        }

    /**
     * Get current virtual position of the month being viewed
     */
    val currentVirtualPosition: Int
        get() {
            val currentPage = dateViewPager!!.currentItem
            return pageChangeListener!!.getCurrent(currentPage)
        }

    /**
     * Check if the navigation arrow is shown
     *
     * @return
     */
    /**
     * Show or hide the navigation arrows
     *
     * @param showNavigationArrows
     */
    var isShowNavigationArrows: Boolean
        get() = mShowNavigationArrows
        set(showNavigationArrows) {
            this.mShowNavigationArrows = showNavigationArrows
            if (showNavigationArrows) {
                leftArrowButton!!.visibility = View.VISIBLE
                rightArrowButton!!.visibility = View.VISIBLE
            } else {
                leftArrowButton!!.visibility = View.INVISIBLE
                rightArrowButton!!.visibility = View.INVISIBLE
            }
        }

    /**
     * Enable / Disable swipe to navigate different months
     *
     * @return
     */
    var isEnableSwipe: Boolean
        get() = mEnableSwipe
        set(enableSwipe) {
            this.mEnableSwipe = enableSwipe
            dateViewPager!!.isEnabled = enableSwipe
        }

    var isSixWeeksInCalendar: Boolean
        get() = sixWeeksInCalendar
        set(sixWeeksInCalendar) {
            this.sixWeeksInCalendar = sixWeeksInCalendar
            dateViewPager!!.isSixWeeksInCalendar = sixWeeksInCalendar
        }

    abstract fun getBackgroundColor(): Int

    /**
     * This method can be used to provide different gridview.
     *
     * @return
     */
    protected val gridViewRes: Int
        get() = R.layout.date_grid_fragment

    /**
     * To display the week day title
     *
     * @return "SUN, MON, TUE, WED, THU, FRI, SAT"
     */
    protected// 17 Feb 2013 is Sunday
    val daysOfWeek: ArrayList<String>
        get() {
            val list = ArrayList<String>()

            val fmt = SimpleDateFormat("EEE", Locale.getDefault())
            val sunday = DateTime(2013, 2, 17, 0, 0, 0, 0)
            var nextDay = sunday.plusDays(startDayOfWeek - SUNDAY)

            for (i in 0..6) {
                val date = CalendarHelper.convertDateTimeToDate(nextDay)
                list.add(fmt.format(date).toUpperCase())
                nextDay = nextDay.plusDays(1)
            }

            return list
        }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    open fun getNewDatesGridAdapter(month: Int, year: Int): CaldroidGridAdapter {
        return CaldroidGridAdapter(activity!!, month, year,
                getCaldroidData(), extraData)
    }

    /**
     * Meant to be subclassed. User who wants to provide custom view, need to
     * provide custom adapter here
     */
    open fun getNewWeekdayAdapter(themeResource: Int): WeekdayArrayAdapter {
        return WeekdayArrayAdapter(
                activity, android.R.layout.simple_list_item_1,
                daysOfWeek, themeResource)
    }


    /*
     * For client to access background and text color maps
     */
    fun getBackgroundForDateTimeMap(): Map<DateTime, Drawable> {
        return mBackgroundForDateTimeMap
    }

    fun getTextColorForDateTimeMap(): Map<DateTime, Int> {
        return mTextColorForDateTimeMap
    }

    /**
     * mCaldroidData return data belong to Caldroid
     *
     * @return
     */
    fun getCaldroidData(): Map<String, Any?> {
        mCaldroidData.clear()
        mCaldroidData[DISABLE_DATES] = mDisableDates
        mCaldroidData[SELECTED_DATES] = selectedDates
        mCaldroidData[_MIN_DATE_TIME] = minDateTime
        mCaldroidData[_MAX_DATE_TIME] = maxDateTime
        mCaldroidData[START_DAY_OF_WEEK] = startDayOfWeek
        mCaldroidData[SIX_WEEKS_IN_CALENDAR] = sixWeeksInCalendar
        mCaldroidData[SQUARE_TEXT_VIEW_CELL] = squareTextViewCell
        mCaldroidData[THEME_RESOURCE] = themeResource


        // For internal use
        mCaldroidData[_BACKGROUND_FOR_DATETIME_MAP] = mBackgroundForDateTimeMap
        mCaldroidData[_TEXT_COLOR_FOR_DATETIME_MAP] = mTextColorForDateTimeMap

        return mCaldroidData
    }

    /**
     * Set backgroundForDateMap
     */
    fun setBackgroundDrawableForDates(
            backgroundForDateMap: Map<Date, Drawable>?) {
        if (backgroundForDateMap == null || backgroundForDateMap.size == 0) {
            return
        }

        mBackgroundForDateTimeMap.clear()

        for (date in backgroundForDateMap.keys) {
            val drawable = backgroundForDateMap[date]
            val dateTime = CalendarHelper.convertDateToDateTime(date)
            mBackgroundForDateTimeMap[dateTime] = drawable!!
        }
    }

    fun clearBackgroundDrawableForDates(dates: List<Date>?) {
        if (dates == null || dates.size == 0) {
            return
        }

        for (date in dates) {
            clearBackgroundDrawableForDate(date)
        }
    }

    fun setBackgroundDrawableForDateTimes(
            backgroundForDateTimeMap: Map<DateTime, Drawable>) {
        this.mBackgroundForDateTimeMap.putAll(backgroundForDateTimeMap)
    }

    fun clearBackgroundDrawableForDateTimes(dateTimes: List<DateTime>?) {
        if (dateTimes == null || dateTimes.size == 0) return

        for (dateTime in dateTimes) {
            mBackgroundForDateTimeMap.remove(dateTime)
        }
    }

    fun setBackgroundDrawableForDate(drawable: Drawable, date: Date) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        mBackgroundForDateTimeMap[dateTime] = drawable
    }

    fun clearBackgroundDrawableForDate(date: Date) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        mBackgroundForDateTimeMap.remove(dateTime)
    }

    fun setBackgroundDrawableForDateTime(drawable: Drawable,
                                         dateTime: DateTime) {
        mBackgroundForDateTimeMap[dateTime] = drawable
    }

    fun clearBackgroundDrawableForDateTime(dateTime: DateTime) {
        mBackgroundForDateTimeMap.remove(dateTime)
    }

    /**
     * Set textColorForDateMap
     *
     * @return
     */
    fun setTextColorForDates(textColorForDateMap: Map<Date, Int>?) {
        if (textColorForDateMap == null || textColorForDateMap.size == 0) {
            return
        }

        mTextColorForDateTimeMap.clear()

        for (date in textColorForDateMap.keys) {
            val resource = textColorForDateMap[date]
            val dateTime = CalendarHelper.convertDateToDateTime(date)
            mTextColorForDateTimeMap[dateTime] = resource!!
        }
    }

    fun clearTextColorForDates(dates: List<Date>?) {
        if (dates == null || dates.size == 0) return

        for (date in dates) {
            clearTextColorForDate(date)
        }
    }

    fun setTextColorForDateTimes(
            textColorForDateTimeMap: Map<DateTime, Int>) {
        this.mTextColorForDateTimeMap.putAll(textColorForDateTimeMap)
    }

    fun setTextColorForDate(textColorRes: Int, date: Date) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        mTextColorForDateTimeMap[dateTime] = textColorRes
    }

    fun clearTextColorForDate(date: Date) {
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        mTextColorForDateTimeMap.remove(dateTime)
    }

    fun setTextColorForDateTime(textColorRes: Int, dateTime: DateTime) {
        mTextColorForDateTimeMap[dateTime] = textColorRes
    }

    /**
     * Save current state to bundle outState
     *
     * @param outState
     * @param key
     */
    fun saveStatesToKey(outState: Bundle, key: String) {
        outState.putBundle(key, savedStates)
    }

    /**
     * Restore current states from savedInstanceState
     *
     * @param savedInstanceState
     * @param key
     */
    fun restoreStatesFromKey(savedInstanceState: Bundle?, key: String) {
        if (savedInstanceState != null && savedInstanceState.containsKey(key)) {
            val caldroidSavedState = savedInstanceState.getBundle(key)
            arguments = caldroidSavedState
        }
    }

    /**
     * Restore state for dialog
     *
     * @param savedInstanceState
     * @param key
     * @param dialogTag
     */
    fun restoreDialogStatesFromKey(manager: FragmentManager,
                                   savedInstanceState: Bundle, key: String, dialogTag: String) {
        restoreStatesFromKey(savedInstanceState, key)

        val existingDialog = manager
                .findFragmentByTag(dialogTag) as CaldroidFragment?
        if (existingDialog != null) {
            existingDialog.dismiss()
            show(manager, dialogTag)
        }
    }

    /**
     * Move calendar to the specified date
     *
     * @param date
     */
    fun moveToDate(date: Date) {
        moveToDateTime(CalendarHelper.convertDateToDateTime(date))
    }

    /**
     * Move calendar to specified dateTime, with animation
     *
     * @param dateTime
     */
    fun moveToDateTime(dateTime: DateTime) {

        val firstOfMonth = DateTime(year, month, 1, 0, 0, 0, 0)
        val lastOfMonth = firstOfMonth.endOfMonth

        // To create a swipe effect
        // Do nothing if the dateTime is in current month

        // Calendar swipe left when dateTime is in the past
        if (dateTime.lt(firstOfMonth)) {
            // Get next month of dateTime. When swipe left, month will
            // decrease
            val firstDayNextMonth = dateTime.plus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay)

            // Refresh adapters
            pageChangeListener!!.setCurrentDateTime(firstDayNextMonth)
            val currentItem = dateViewPager!!.currentItem
            pageChangeListener!!.refreshAdapters(currentItem)

            // Swipe left
            dateViewPager!!.currentItem = currentItem - 1
        } else if (dateTime.gt(lastOfMonth)) {
            // Get last month of dateTime. When swipe right, the month will
            // increase
            val firstDayLastMonth = dateTime.minus(0, 1, 0, 0, 0, 0, 0,
                    DateTime.DayOverflow.LastDay)

            // Refresh adapters
            pageChangeListener!!.setCurrentDateTime(firstDayLastMonth)
            val currentItem = dateViewPager!!.currentItem
            pageChangeListener!!.refreshAdapters(currentItem)

            // Swipe right
            dateViewPager!!.currentItem = currentItem + 1
        }// Calendar swipe right when dateTime is in the future

    }

    /**
     * Set month and year for the calendar. This is to avoid naive
     * implementation of manipulating month and year. All dates within same
     * month/year give same result
     *
     * @param date
     */
    fun setCalendarDate(date: Date) {
        setCalendarDateTime(CalendarHelper.convertDateToDateTime(date))
    }

    fun setCalendarDateTime(dateTime: DateTime) {
        month = dateTime.month!!
        year = dateTime.year!!

        // Notify listener
        if (caldroidListener != null) {
            caldroidListener!!.onChangeMonth(month, year)
        }

        refreshView()
    }

    /**
     * Set calendar to previous month
     */
    fun prevMonth() {
        dateViewPager!!.currentItem = pageChangeListener!!.currentPage - 1
    }

    /**
     * Set calendar to next month
     */
    fun nextMonth() {
        dateViewPager!!.currentItem = pageChangeListener!!.currentPage + 1
    }

    /**
     * Clear all disable dates. Notice this does not refresh the calendar, need
     * to explicitly call refreshView()
     */
    fun clearDisableDates() {
        mDisableDates!!.clear()
    }

    /**
     * Set mDisableDates from ArrayList of Date
     *
     * @param disableDateList
     */
    fun setDisableDates(disableDateList: ArrayList<Date>?) {
        if (disableDateList == null || disableDateList.size == 0) {
            return
        }

        mDisableDates!!.clear()

        for (date in disableDateList) {
            val dateTime = CalendarHelper.convertDateToDateTime(date)
            mDisableDates!!.add(dateTime)
        }

    }

    /**
     * Set mDisableDates from ArrayList of String with custom date format. For
     * example, if the date string is 06-Jan-2013, use date format dd-MMM-yyyy.
     * This method will refresh the calendar, it's not necessary to call
     * refreshView()
     *
     * @param disableDateStrings
     * @param dateFormat
     */
    @JvmOverloads
    fun setDisableDatesFromString(disableDateStrings: ArrayList<String>?,
                                  dateFormat: String? = null) {
        if (disableDateStrings == null) {
            return
        }

        mDisableDates!!.clear()

        for (dateString in disableDateStrings) {
            val dateTime = CalendarHelper.getDateTimeFromString(
                    dateString, dateFormat)
            mDisableDates!!.add(dateTime)
        }
    }

    /**
     * To clear selectedDates. This method does not refresh view, need to
     * explicitly call refreshView()
     */
    fun clearSelectedDates() {
        selectedDates!!.clear()
    }

    /**
     * Select the dates from fromDate to toDate. By default the background color
     * is holo_blue_light, and the text color is black. You can customize the
     * background by changing CaldroidFragment.selectedBackgroundDrawable, and
     * change the text color CaldroidFragment.selectedTextColor before call this
     * method. This method does not refresh view, need to call refreshView()
     *
     * @param fromDate
     * @param toDate
     */
    fun setSelectedDates(fromDate: Date?, toDate: Date?) {
        // Ensure fromDate is before toDate
        if (fromDate == null || toDate == null || fromDate.after(toDate)) {
            return
        }

        selectedDates!!.clear()

        val fromDateTime = CalendarHelper.convertDateToDateTime(fromDate)
        val toDateTime = CalendarHelper.convertDateToDateTime(toDate)

        var dateTime = fromDateTime
        while (dateTime.lt(toDateTime)) {
            selectedDates!!.add(dateTime)
            dateTime = dateTime.plusDays(1)
        }
        selectedDates!!.add(toDateTime)
    }

    /**
     * Convenient method to select dates from String
     *
     * @param fromDateString
     * @param toDateString
     * @param dateFormat
     * @throws ParseException
     */
    @Throws(ParseException::class)
    fun setSelectedDateStrings(fromDateString: String,
                               toDateString: String, dateFormat: String) {

        val fromDate = CalendarHelper.getDateFromString(fromDateString,
                dateFormat)
        val toDate = CalendarHelper
                .getDateFromString(toDateString, dateFormat)
        setSelectedDates(fromDate, toDate)
    }

    /**
     * Select single date
     * @author Alov Maxim <alovmax></alovmax>@yandex.ru>
     */
    fun setSelectedDate(date: Date?) {
        if (date == null) {
            return
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        selectedDates!!.add(dateTime)
    }

    /**
     * Clear selection of the specified date
     * @author Alov Maxim <alovmax></alovmax>@yandex.ru>
     */
    fun clearSelectedDate(date: Date?) {
        if (date == null) {
            return
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        selectedDates!!.remove(dateTime)
    }

    /**
     * Checks whether the specified date is selected
     * @author Alov Maxim <alovmax></alovmax>@yandex.ru>
     */
    fun isSelectedDate(date: Date?): Boolean {
        if (date == null) {
            return false
        }
        val dateTime = CalendarHelper.convertDateToDateTime(date)
        return selectedDates!!.contains(dateTime)
    }

    /**
     * Set min date. This method does not refresh view
     *
     * @param minDate
     */
    fun setMinDate(minDate: Date?) {
        if (minDate == null) {
            minDateTime = null
        } else {
            minDateTime = CalendarHelper.convertDateToDateTime(minDate)
        }
    }

    /**
     * Convenient method to set min date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param minDateString
     * @param dateFormat
     */
    fun setMinDateFromString(minDateString: String?, dateFormat: String) {
        if (minDateString == null) {
            setMinDate(null)
        } else {
            minDateTime = CalendarHelper.getDateTimeFromString(minDateString,
                    dateFormat)
        }
    }

    /**
     * Set max date. This method does not refresh view
     *
     * @param maxDate
     */
    fun setMaxDate(maxDate: Date?) {
        if (maxDate == null) {
            maxDateTime = null
        } else {
            maxDateTime = CalendarHelper.convertDateToDateTime(maxDate)
        }
    }

    /**
     * Convenient method to set max date from String. If dateFormat is null,
     * default format is yyyy-MM-dd
     *
     * @param maxDateString
     * @param dateFormat
     */
    fun setMaxDateFromString(maxDateString: String?, dateFormat: String) {
        if (maxDateString == null) {
            setMaxDate(null)
        } else {
            maxDateTime = CalendarHelper.getDateTimeFromString(maxDateString,
                    dateFormat)
        }
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    fun getDateItemClickListener(): OnItemClickListener {
        if (dateItemClickListener == null) {
            dateItemClickListener = OnItemClickListener { parent, view, position, id ->
                val dateTime = dateInMonthsList[position]

                if (caldroidListener != null) {
                    if (!enableClickOnDisabledDates) {
                        if (minDateTime != null && dateTime
                                        .lt(minDateTime)
                                || maxDateTime != null && dateTime
                                        .gt(maxDateTime)
                                || mDisableDates != null && mDisableDates!!
                                        .indexOf(dateTime) != -1) {
                            return@OnItemClickListener
                        }
                    }

                    val date = CalendarHelper
                            .convertDateTimeToDate(dateTime)
                    caldroidListener!!.onSelectDate(date, view)
                }
            }
        }

        return dateItemClickListener!!
    }

    /**
     * Callback to listener when date is valid (not disable, not outside of
     * min/max date)
     *
     * @return
     */
    fun getDateItemLongClickListener(): OnItemLongClickListener {
        if (dateItemLongClickListener == null) {
            dateItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
                val dateTime = dateInMonthsList[position]

                if (caldroidListener != null) {
                    if (!enableClickOnDisabledDates) {
                        if (minDateTime != null && dateTime
                                        .lt(minDateTime)
                                || maxDateTime != null && dateTime
                                        .gt(maxDateTime)
                                || mDisableDates != null && mDisableDates!!
                                        .indexOf(dateTime) != -1) {
                            return@OnItemLongClickListener false
                        }
                    }
                    val date = CalendarHelper
                            .convertDateTimeToDate(dateTime)
                    caldroidListener!!.onLongClickDate(date, view)
                }

                true
            }
        }

        return dateItemLongClickListener!!
    }

    /**
     * Refresh month title text view when user swipe
     */
    protected fun refreshMonthTitleTextView() {
        // Refresh title view
        firstMonthTime.year = year
        firstMonthTime.month = month - 1
        firstMonthTime.monthDay = 15
        val millis = firstMonthTime.toMillis(true)

        // This is the method used by the platform Calendar app to get a
        // correctly localized month name for display on a wall calendar
        monthYearStringBuilder.setLength(0)
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, millis, millis, MONTH_YEAR_FLAG).toString()

        monthTitleTextView!!.text = monthTitle.toUpperCase(Locale.getDefault())
    }

    /**
     * Refresh view when parameter changes. You should always change all
     * parameters first, then call this method.
     */
    fun refreshView() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (month == -1 || year == -1) {
            return
        }

        refreshMonthTitleTextView()

        // Refresh the date grid views
        for (adapter in datePagerAdapters) {
            // Reset caldroid data
            adapter.setCaldroidData(getCaldroidData())

            // Reset extra data
            adapter.setExtraData(extraData)

            // Update today variable
            adapter.updateToday()

            // Refresh view
            adapter.notifyDataSetChanged()
        }
    }

    fun refreshViewOnlyCurrentPage() {
        // If month and year is not yet initialized, refreshView doesn't do
        // anything
        if (month == -1 || year == -1) {
            return
        }

        refreshMonthTitleTextView()
        datePagerAdapters[currentVirtualPosition].notifyDataSetChanged()
    }

    /**
     * Retrieve initial arguments to the fragment Data can include: month, year,
     * dialogTitle, mShowNavigationArrows,(String) mDisableDates, selectedDates,
     * minDate, maxDate, squareTextViewCell
     */
    protected fun retrieveInitialArgs() {
        // Get arguments
        val args = arguments

        CalendarHelper.setup()

        if (args != null) {
            // Get month, year
            month = args.getInt(MONTH, -1)
            year = args.getInt(YEAR, -1)
            dialogTitle = args.getString(DIALOG_TITLE)
            val dialog = dialog
            if (dialog != null) {
                if (dialogTitle != null) {
                    dialog.setTitle(dialogTitle)
                } else {
                    // Don't display title bar if user did not supply
                    // dialogTitle
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                }
            }

            // Get start day of Week. Default calendar first column is SUNDAY
            startDayOfWeek = args.getInt(START_DAY_OF_WEEK, 1)
            if (startDayOfWeek > 7) {
                startDayOfWeek = startDayOfWeek % 7
            }

            // Should show arrow
            mShowNavigationArrows = args
                    .getBoolean(SHOW_NAVIGATION_ARROWS, true)

            // Should enable swipe to change month
            mEnableSwipe = args.getBoolean(ENABLE_SWIPE, true)

            // Get sixWeeksInCalendar
            sixWeeksInCalendar = args.getBoolean(SIX_WEEKS_IN_CALENDAR, true)

            // Get squareTextViewCell, by default, use square cell in portrait mode
            // and using normal cell in landscape mode
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, true)
            } else {
                squareTextViewCell = args.getBoolean(SQUARE_TEXT_VIEW_CELL, false)
            }

            // Get clickable setting
            enableClickOnDisabledDates = args.getBoolean(
                    ENABLE_CLICK_ON_DISABLED_DATES, false)

            // Get disable dates
            val disableDateStrings = args
                    .getStringArrayList(DISABLE_DATES)
            if (disableDateStrings != null && disableDateStrings.size > 0) {
                mDisableDates!!.clear()
                for (dateString in disableDateStrings) {
                    val dt = CalendarHelper.getDateTimeFromString(
                            dateString, null)
                    mDisableDates!!.add(dt)
                }
            }

            // Get selected dates
            val selectedDateStrings = args
                    .getStringArrayList(SELECTED_DATES)
            if (selectedDateStrings != null && selectedDateStrings.size > 0) {
                selectedDates!!.clear()
                for (dateString in selectedDateStrings) {
                    val dt = CalendarHelper.getDateTimeFromString(
                            dateString, null)
                    selectedDates!!.add(dt)
                }
            }

            // Get min date and max date
            val minDateTimeString = args.getString(MIN_DATE)
            if (minDateTimeString != null) {
                minDateTime = CalendarHelper.getDateTimeFromString(
                        minDateTimeString, null)
            }

            val maxDateTimeString = args.getString(MAX_DATE)
            if (maxDateTimeString != null) {
                maxDateTime = CalendarHelper.getDateTimeFromString(
                        maxDateTimeString, null)
            }

            // Get theme
            themeResource = args.getInt(THEME_RESOURCE, R.style.CaldroidDefault)
        }
        if (month == -1 || year == -1) {
            val dateTime = DateTime.today(TimeZone.getDefault())
            month = dateTime.month!!
            year = dateTime.year!!
        }
    }

    /**
     * Below code fixed the issue viewpager disappears in dialog mode on
     * orientation change
     *
     *
     * Code taken from Andy Dennie and Zsombor Erdody-Nagy
     * http://stackoverflow.com/questions/8235080/fragments-dialogfragment
     * -and-screen-rotation
     */
    override fun onDestroyView() {
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    /**
     * Setup view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        retrieveInitialArgs()

        // To support keeping instance for dialog
        if (dialog != null) {
            try {
                retainInstance = true
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

        }

        val localInflater = getThemeInflater(activity, inflater, themeResource)

        // This is a hack to fix issue localInflater doesn't use the themeResource, make Android
        // complain about layout_width and layout_height missing. I'm unsure about its impact
        // for app that wants to change theme dynamically.
        activity!!.setTheme(themeResource)

        val view = localInflater.inflate(R.layout.calendar_view, container, false)
        view.setBackgroundColor(getBackgroundColor())

        // For the monthTitleTextView
        monthTitleTextView = view
                .findViewById<View>(R.id.calendar_month_year_textview) as TextView

        // For the left arrow button
        leftArrowButton = view.findViewById<View>(R.id.calendar_left_arrow) as Button
        rightArrowButton = view
                .findViewById<View>(R.id.calendar_right_arrow) as Button

        // Navigate to previous month when user click
        leftArrowButton!!.setOnClickListener { prevMonth() }

        // Navigate to next month when user click
        rightArrowButton!!.setOnClickListener { nextMonth() }

        // Show navigation arrows depend on initial arguments
        isShowNavigationArrows = mShowNavigationArrows

        // For the weekday gridview ("SUN, MON, TUE, WED, THU, FRI, SAT")
        weekdayGridView = view.findViewById<View>(R.id.weekday_gridview) as GridView
        val weekdaysAdapter = getNewWeekdayAdapter(themeResource)
        weekdayGridView!!.adapter = weekdaysAdapter

        // Setup all the pages of date grid views. These pages are recycled
        setupDateGridPages(view)

        // Refresh view
        refreshView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inform client that all views are created and not null
        // Client should perform customization for buttons and textviews here
        if (caldroidListener != null) {
            caldroidListener!!.onCaldroidViewCreated()
        }
    }

    /**
     * Setup 4 pages contain date grid views. These pages are recycled to use
     * memory efficient
     *
     * @param view
     */
    private fun setupDateGridPages(view: View) {
        // Get current date time
        val currentDateTime = DateTime(year, month, 1, 0, 0, 0, 0)

        // Set to pageChangeListener
        pageChangeListener = DatePageChangeListener()
        pageChangeListener!!.setCurrentDateTime(currentDateTime)

        // Setup adapters for the grid views
        // Current month
        val adapter0 = getNewDatesGridAdapter(
                currentDateTime.month!!, currentDateTime.year!!)

        // Setup dateInMonthsList
        dateInMonthsList = adapter0.getDatetimeList()

        // Next month
        val nextDateTime = currentDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        val adapter1 = getNewDatesGridAdapter(
                nextDateTime.month!!, nextDateTime.year!!)

        // Next 2 month
        val next2DateTime = nextDateTime.plus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        val adapter2 = getNewDatesGridAdapter(
                next2DateTime.month!!, next2DateTime.year!!)

        // Previous month
        val prevDateTime = currentDateTime.minus(0, 1, 0, 0, 0, 0, 0,
                DateTime.DayOverflow.LastDay)
        val adapter3 = getNewDatesGridAdapter(
                prevDateTime.month!!, prevDateTime.year!!)

        // Add to the array of adapters
        datePagerAdapters.add(adapter0)
        datePagerAdapters.add(adapter1)
        datePagerAdapters.add(adapter2)
        datePagerAdapters.add(adapter3)

        // Set adapters to the pageChangeListener so it can refresh the adapter
        // when page change
        pageChangeListener!!.caldroidGridAdapters = datePagerAdapters

        // Setup InfiniteViewPager and InfinitePagerAdapter. The
        // InfinitePagerAdapter is responsible
        // for reuse the fragments
        dateViewPager = view
                .findViewById<View>(R.id.months_infinite_pager) as InfiniteViewPager

        // Set enable swipe
        dateViewPager!!.isEnabled = mEnableSwipe

        // Set if viewpager wrap around particular month or all months (6 rows)
        dateViewPager!!.isSixWeeksInCalendar = sixWeeksInCalendar

        // Set the numberOfDaysInMonth to dateViewPager so it can calculate the
        // height correctly
        dateViewPager!!.datesInMonth = dateInMonthsList

        // MonthPagerAdapter actually provides 4 real fragments. The
        // InfinitePagerAdapter only recycles fragment provided by this
        // MonthPagerAdapter
        val pagerAdapter = MonthPagerAdapter(
                childFragmentManager)

        // Provide initial data to the fragments, before they are attached to
        // view.
        fragments = pagerAdapter.fragments

        for (i in 0 until NUMBER_OF_PAGES) {
            val dateGridFragment = fragments!![i]
            val adapter = datePagerAdapters[i]
            dateGridFragment.setGridViewRes(gridViewRes)
            dateGridFragment.gridAdapter = adapter
            dateGridFragment.onItemClickListener = getDateItemClickListener()
            dateGridFragment.onItemLongClickListener = getDateItemLongClickListener()
        }

        // Setup InfinitePagerAdapter to wrap around MonthPagerAdapter
        val infinitePagerAdapter = InfinitePagerAdapter(
                pagerAdapter)

        // Use the infinitePagerAdapter to provide data for dateViewPager
        dateViewPager!!.adapter = infinitePagerAdapter

        // Setup pageChangeListener
        dateViewPager!!.setOnPageChangeListener(pageChangeListener)

        dateViewPager!!.setBackgroundColor(getBackgroundColor())
    }

    /**
     * DatePageChangeListener refresh the date grid views when user swipe the
     * calendar
     *
     * @author thomasdao
     */
    inner class DatePageChangeListener : ViewPager.OnPageChangeListener {
        /**
         * Return currentPage of the dateViewPager
         *
         * @return
         */
        var currentPage = InfiniteViewPager.OFFSET
        private var currentDateTime: DateTime? = null
        /**
         * Return 4 adapters
         *
         * @return
         */
        var caldroidGridAdapters: ArrayList<CaldroidGridAdapter>? = null

        /**
         * Return currentDateTime of the selected page
         *
         * @return
         */
        fun getCurrentDateTime(): DateTime? {
            return currentDateTime
        }

        fun setCurrentDateTime(dateTime: DateTime) {
            this.currentDateTime = dateTime
            setCalendarDateTime(currentDateTime!!)
        }

        /**
         * Return virtual next position
         *
         * @param position
         * @return
         */
        private fun getNext(position: Int): Int {
            return (position + 1) % CaldroidFragment.NUMBER_OF_PAGES
        }

        /**
         * Return virtual previous position
         *
         * @param position
         * @return
         */
        private fun getPrevious(position: Int): Int {
            return (position + 3) % CaldroidFragment.NUMBER_OF_PAGES
        }

        /**
         * Return virtual current position
         *
         * @param position
         * @return
         */
        fun getCurrent(position: Int): Int {
            return position % CaldroidFragment.NUMBER_OF_PAGES
        }

        override fun onPageScrollStateChanged(position: Int) {}

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        fun refreshAdapters(position: Int) {
            // Get adapters to refresh
            val currentAdapter = caldroidGridAdapters!![getCurrent(position)]
            val prevAdapter = caldroidGridAdapters!![getPrevious(position)]
            val nextAdapter = caldroidGridAdapters!![getNext(position)]

            if (position == currentPage) {
                // Refresh current adapter

                currentAdapter.setAdapterDateTime(currentDateTime!!)
                currentAdapter.notifyDataSetChanged()

                // Refresh previous adapter
                prevAdapter.setAdapterDateTime(currentDateTime!!.minus(0, 1, 0,
                        0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                prevAdapter.notifyDataSetChanged()

                // Refresh next adapter
                nextAdapter.setAdapterDateTime(currentDateTime!!.plus(0, 1, 0, 0,
                        0, 0, 0, DateTime.DayOverflow.LastDay))
                nextAdapter.notifyDataSetChanged()
            } else if (position > currentPage) {
                // Update current date time to next month
                currentDateTime = currentDateTime!!.plus(0, 1, 0, 0, 0, 0, 0,
                        DateTime.DayOverflow.LastDay)

                // Refresh the adapter of next gridview
                nextAdapter.setAdapterDateTime(currentDateTime!!.plus(0, 1, 0, 0,
                        0, 0, 0, DateTime.DayOverflow.LastDay))
                nextAdapter.notifyDataSetChanged()

            } else {
                // Update current date time to previous month
                currentDateTime = currentDateTime!!.minus(0, 1, 0, 0, 0, 0, 0,
                        DateTime.DayOverflow.LastDay)

                // Refresh the adapter of previous gridview
                prevAdapter.setAdapterDateTime(currentDateTime!!.minus(0, 1, 0,
                        0, 0, 0, 0, DateTime.DayOverflow.LastDay))
                prevAdapter.notifyDataSetChanged()
            }// Swipe left
            // Detect if swipe right or swipe left
            // Swipe right

            // Update current page
            currentPage = position
        }

        /**
         * Refresh the fragments
         */
        override fun onPageSelected(position: Int) {
            refreshAdapters(position)

            // Update current date time of the selected page
            setCalendarDateTime(currentDateTime!!)

            // Update all the dates inside current month
            val currentAdapter = caldroidGridAdapters!![position % CaldroidFragment.NUMBER_OF_PAGES]

            // Refresh dateInMonthsList
            dateInMonthsList.clear()
            dateInMonthsList.addAll(currentAdapter.getDatetimeList())
        }

    }

//    https://stackoverflow.com/questions/56618453/after-migrating-to-androidx-application-crashes-with-attempt-to-invoke-androidx
//    override fun onDetach() {
//        super.onDetach()
//        try {
//            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
//            childFragmentManager.isAccessible = true
//            childFragmentManager.set(this, null)
//        } catch (e: NoSuchFieldException) {
//            throw RuntimeException(e)
//        } catch (e: IllegalAccessException) {
//            throw RuntimeException(e)
//        }
//    }

    companion object {
        /**
         * Weekday conventions
         */
        var SUNDAY = 1
        var MONDAY = 2
        var TUESDAY = 3
        var WEDNESDAY = 4
        var THURSDAY = 5
        var FRIDAY = 6
        var SATURDAY = 7

        /**
         * Flags to display month
         */
        private val MONTH_YEAR_FLAG = (DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR)

        val NUMBER_OF_PAGES = 4

        /**
         * To customize the disabled background drawable and text color
         */
        var disabledBackgroundDrawable = -1
        var disabledTextColor = Color.GRAY

        /**
         * Initial params key
         */
        val DIALOG_TITLE = "dialogTitle"
        val MONTH = "month"
        val YEAR = "year"
        val SHOW_NAVIGATION_ARROWS = "showNavigationArrows"
        val DISABLE_DATES = "disableDates"
        val SELECTED_DATES = "selectedDates"
        val MIN_DATE = "minDate"
        val MAX_DATE = "maxDate"
        val ENABLE_SWIPE = "enableSwipe"
        val START_DAY_OF_WEEK = "startDayOfWeek"
        val SIX_WEEKS_IN_CALENDAR = "sixWeeksInCalendar"
        val ENABLE_CLICK_ON_DISABLED_DATES = "enableClickOnDisabledDates"
        val SQUARE_TEXT_VIEW_CELL = "squareTextViewCell"
        val THEME_RESOURCE = "themeResource"

        /**
         * For internal use
         */
        val _MIN_DATE_TIME = "_minDateTime"
        val _MAX_DATE_TIME = "_maxDateTime"
        val _BACKGROUND_FOR_DATETIME_MAP = "_backgroundForDateTimeMap"
        val _TEXT_COLOR_FOR_DATETIME_MAP = "_textColorForDateTimeMap"

        /**
         * To support faster init
         *
         * @param dialogTitle
         * @param month
         * @param year
         * @return
         */
        fun newInstance(dialogTitle: String, month: Int,
                        year: Int): CaldroidFragment {
            val f = CaldroidFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.putString(DIALOG_TITLE, dialogTitle)
            args.putInt(MONTH, month)
            args.putInt(YEAR, year)

            f.arguments = args

            return f
        }

        fun getThemeInflater(context: Context?, origInflater: LayoutInflater, themeResource: Int): LayoutInflater {
            val wrapped = ContextThemeWrapper(context, themeResource)
            return origInflater.cloneInContext(wrapped)
        }
    }
}
