package me.blog.korn123.easydiary.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.roomorama.caldroid.CaldroidFragment
import com.roomorama.caldroid.CaldroidFragmentEx
import com.roomorama.caldroid.CaldroidListener
import io.realm.Sort
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DashboardActivity
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.SymbolFilterPickerActivity
import me.blog.korn123.easydiary.adapters.DailySymbolAdapter
import me.blog.korn123.easydiary.adapters.DiaryCalendarItemAdapter
import me.blog.korn123.easydiary.databinding.DialogDashboardCalendarItemBinding
import me.blog.korn123.easydiary.databinding.DialogOptionItemBinding
import me.blog.korn123.easydiary.databinding.FragmentDailySymbolBinding
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import java.text.SimpleDateFormat
import java.util.*

class DailySymbolFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentDailySymbolBinding
    private lateinit var mDailySymbolAdapter: DailySymbolAdapter
    lateinit var mCalendarFragment: CaldroidFragmentEx
    private var mDailySymbolList: ArrayList<DailySymbolAdapter.DailySymbol> = arrayListOf()
    private val mRequestUpdateDailySymbol =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) updateDailyCard()
        }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentDailySymbolBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val startOfWeek = config.calendarStartDay
        mCalendarFragment =
            CalendarFragment().apply {
                arguments =
                    Bundle().apply { putInt(CaldroidFragment.START_DAY_OF_WEEK, startOfWeek) }
                caldroidListener =
                    object : CaldroidListener() {
                        override fun onSelectDate(
                            date: Date,
                            view: View,
                        ) {
                            val formatter = SimpleDateFormat(DateUtilConstants.DATE_PATTERN_DASH, Locale.getDefault())
                            val selectedItems = EasyDiaryDbHelper.findDiaryByDateString(formatter.format(date), if (config.calendarSorting == CALENDAR_SORTING_ASC) Sort.ASCENDING else Sort.DESCENDING)

                            clearSelectedDates()
                            setSelectedDate(date)
                            refreshViewOnlyCurrentPage()

                            if (selectedItems.isNotEmpty()) {
                                var dialog: AlertDialog? = null
                                val builder =
                                    AlertDialog.Builder(requireActivity()).apply {
                                        setPositiveButton(getString(android.R.string.ok)) { _, _ -> }
                                    }
                                val dialogOptionItemBinding = DialogDashboardCalendarItemBinding.inflate(layoutInflater)
                                val calendarItemAdapter = DiaryCalendarItemAdapter(requireContext(), R.layout.item_diary_dashboard_calendar, selectedItems)
                                dialogOptionItemBinding.run {
                                    listView.adapter = calendarItemAdapter
                                    listView.setOnItemClickListener { parent, view, position, id ->
                                        TransitionHelper.startActivityWithTransition(
                                            requireActivity(),
                                            Intent(requireContext(), DiaryReadingActivity::class.java).apply {
                                                putExtra(DIARY_SEQUENCE, selectedItems[position].sequence)
                                            },
                                        )
                                        dialog?.dismiss()
                                    }
                                }
                                Handler(Looper.getMainLooper()).postDelayed({
                                    dialog =
                                        builder.create().apply {
                                            requireActivity().updateAlertDialogWithIcon(DialogMode.DEFAULT, this, null, dialogOptionItemBinding.root)
                                        }
                                    val layoutParams = WindowManager.LayoutParams()
                                    layoutParams.copyFrom(dialog?.window?.attributes)
                                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                                    dialog?.window?.attributes = layoutParams
                                }, 100)
                            }
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
                                        requireContext(),
                                        monthYearFormatter,
                                        calendar.timeInMillis,
                                        calendar.timeInMillis,
                                        monthYearFlag,
                                    ).toString()
                            mBinding.textCalendarDate.text = monthTitle.uppercase(Locale.getDefault())
                        }

                        override fun onLongClickDate(
                            date: Date?,
                            view: View?,
                        ) {}

                        override fun onCaldroidViewCreated() {}
                    }
            }
        initializeDailySymbol()
        mBinding.run {
            editSymbolFilter.setOnClickListener {
                Intent(requireContext(), SymbolFilterPickerActivity::class.java).apply {
                    mRequestUpdateDailySymbol.launch(this)
                }
            }
            switchCalendar.setOnCheckedChangeListener { _, isChecked ->
                config.enableDashboardCalendar = isChecked
                layoutCalendarContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
            switchCalendar.isChecked = config.enableDashboardCalendar
        }

        childFragmentManager.beginTransaction().run {
            replace(R.id.calendar, mCalendarFragment)
            commitNow()
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initializeDailySymbol() {
        mDailySymbolAdapter =
            DailySymbolAdapter(
                requireActivity(),
                mDailySymbolList,
            )
        mBinding.dailyCardRecyclerView.apply {
//            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@DashboardActivity, 1)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mDailySymbolAdapter
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                    ) {
                        mBinding.month.text = mDailySymbolList[(mBinding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()].date
                        mBinding.dailyCardRecyclerView.minimumHeight = 0
                        val position = (mBinding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                    mDailySymbolAdapter.notifyItemChanged(position)
                        Log.i(AAF_TEST, "pos: $position, newState: $newState, ${mBinding.dailyCardRecyclerView.minimumHeight}/${mBinding.dailyCardRecyclerView.height}")
                        if (newState == SCROLL_STATE_IDLE) {
                            requireActivity().run {
                                if (this is DashboardActivity) {
                                    this.showProgressContainer()
                                }
                                mBinding.dailyCardRecyclerView.minimumHeight = mBinding.dailyCardRecyclerView.height
                                Handler(Looper.getMainLooper()).postDelayed({ mDailySymbolAdapter.notifyDataSetChanged() }, 200)
                                if (this is DashboardActivity) {
                                    this.hideProgressContainer()
                                }
                            }
                        }
                    }
                },
            )
        }

        init365Day()
        updateDailyCard()
    }

    private fun init365Day() {
        val cal = Calendar.getInstance()
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(DateUtilConstants.DATE_PATTERN_DASH, Locale.getDefault())
        cal.time = Date()
        if (mBinding.month.text.isEmpty()) mBinding.month.text = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).uppercase()

        mDailySymbolList.clear()
        for (num in 1..365) {
            mDailySymbolList.add(
                DailySymbolAdapter.DailySymbol(
                    dateFormat.format(cal.time),
                    cal.get(Calendar.DAY_OF_WEEK),
                    cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())!!,
                    dayOfMonth.format(cal.time),
                    cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!.uppercase(),
                ),
            )
            cal.add(Calendar.DATE, -1)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDailySymbol() {
        init365Day()
        mBinding.dailyCardRecyclerView.minimumHeight = mBinding.dailyCardRecyclerView.height
        mDailySymbolAdapter.notifyDataSetChanged()
    }

    private fun updateDailyCard() {
        mBinding.run {
//            month.visibility = View.GONE
//            dailyCardRecyclerView.visibility = View.GONE
//            dailyCardProgressBar.visibility = View.VISIBLE
            selectedSymbolFlexBox.removeAllViews()

            config.selectedSymbols.split(",").map { sequence ->
                val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(layoutInflater)
                FlavorUtils.initWeatherView(requireContext(), partialDailySymbolBinding.dailySymbol, sequence.toInt())
                requireActivity().updateDashboardInnerCard(partialDailySymbolBinding.root)
                selectedSymbolFlexBox.addView(partialDailySymbolBinding.root)
            }

            mBinding.dailyCardRecyclerView.minimumHeight = mBinding.dailyCardRecyclerView.height
            mDailySymbolAdapter.notifyDataSetChanged()
//                    month.visibility = View.VISIBLE
            dailyCardRecyclerView.visibility = View.VISIBLE
//                    dailyCardProgressBar.visibility = View.GONE
//                    requireActivity().updateAppViews(selectedSymbolFlexBox)
//                requireActivity().runOnUiThread {
//                    mDailySymbolAdapter.notifyDataSetChanged()
//                    month.visibility = View.VISIBLE
//                    dailyCardRecyclerView.visibility = View.VISIBLE
//                    dailyCardProgressBar.visibility = View.GONE
//                    requireActivity().updateAppViews(selectedSymbolFlexBox)
//                }
        }
    }
}
