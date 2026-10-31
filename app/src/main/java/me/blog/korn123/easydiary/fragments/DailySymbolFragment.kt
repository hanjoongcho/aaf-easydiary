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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.activities.DashboardActivity
import me.blog.korn123.easydiary.activities.SymbolFilterPickerActivity
import me.blog.korn123.easydiary.adapters.DailySymbolAdapter
import me.blog.korn123.easydiary.databinding.FragmentDailySymbolBinding
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.presentation.calendar.CalendarContent
import me.blog.korn123.easydiary.presentation.calendar.CalendarViewModel
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DailySymbolFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentDailySymbolBinding
    private lateinit var dailySymbolAdapter: DailySymbolAdapter
    private val diaryViewModel: DiaryViewModel by viewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()
    private var dailySymbolList: ArrayList<DailySymbolAdapter.DailySymbol> = arrayListOf()
    private val requestUpdateDailySymbol =
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
        binding = FragmentDailySymbolBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            initializeDailySymbol()
            binding.run {
                editSymbolFilter.setOnClickListener {
                    Intent(requireContext(), SymbolFilterPickerActivity::class.java).apply {
                        requestUpdateDailySymbol.launch(this)
                    }
                }
                switchCalendar.setOnCheckedChangeListener { _, isChecked ->
                    config.enableDashboardCalendar = isChecked
                    composeView.visibility = if (isChecked) View.VISIBLE else View.GONE
                }
                switchCalendar.isChecked = config.enableDashboardCalendar

                composeView.setContent {
                    AppTheme {
                        CalendarContent(calendarViewModel = calendarViewModel, enableScroll = false)
                    }
                }
            }

            childFragmentManager.beginTransaction().run {
                commitNow()
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    val map: MutableMap<String, Pair<List<Diary>, List<Diary>>> = mutableMapOf()

    private fun initializeDailySymbol() {
        dailySymbolAdapter =
            DailySymbolAdapter(
                requireActivity(),
                dailySymbolList,
            )
        binding.dailyCardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = dailySymbolAdapter
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        recyclerView: RecyclerView,
                        newState: Int,
                    ) {
                        binding.month.text =
                            dailySymbolList[(binding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()].date
                        binding.dailyCardRecyclerView.minimumHeight = 0
                        val position =
                            (binding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        Log.i(
                            AAF_TEST,
                            "pos: $position, newState: $newState, ${binding.dailyCardRecyclerView.minimumHeight}/${binding.dailyCardRecyclerView.height}",
                        )
                        if (newState == SCROLL_STATE_IDLE) {
                            requireActivity().run {
                                if (this is DashboardActivity) {
                                    this.showProgressContainer()
                                }
                                binding.dailyCardRecyclerView.minimumHeight =
                                    binding.dailyCardRecyclerView.height
                                Handler(Looper.getMainLooper()).postDelayed(
                                    { dailySymbolAdapter.notifyDataSetChanged() },
                                    200,
                                )
                                if (this is DashboardActivity) {
                                    this.hideProgressContainer()
                                }
                            }
                        }
                    }
                },
            )
        }
        updateDailyCard()
    }

    private suspend fun init365Day() {
        val cal = Calendar.getInstance()
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(DateUtilConstants.DATE_PATTERN_DASH, Locale.getDefault())
        cal.time = Date()

        // 1. Create a local list to avoid concurrent modification of the member variable
        val localDailyList = ArrayList<DailySymbolAdapter.DailySymbol>()

        for (num in 1..365) {
            localDailyList.add(
                DailySymbolAdapter.DailySymbol(
                    dateFormat.format(cal.time),
                    cal.get(Calendar.DAY_OF_WEEK),
                    cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: "",
                    dayOfMonth.format(cal.time),
                    cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.uppercase() ?: "",
                ),
            )
            cal.add(Calendar.DATE, -1)
        }

        val startDateString = localDailyList[localDailyList.size.minus(1)].dateString
        val endDateString = localDailyList[0].dateString
        val periodGroup =
            diaryViewModel
                .findDiary(
                    query = null,
                    startTimeMillis =
                        DateUtils.dateToTimeMillis(
                            startDateString,
                            DateUtilConstants.DATE_PATTERN_DASH,
                        ),
                    endTimeMillis =
                        DateUtils.dateToTimeMillis(
                            endDateString,
                            DateUtilConstants.DATE_PATTERN_DASH,
                        ),
                ).groupBy { it.dateString }

        // 2. Perform the heavy mapping on the local list
        val selectedSymbolIds = config.selectedSymbols.split(",").mapNotNull { it.toIntOrNull() }
        val localMap =
            localDailyList.associateBy(
                keySelector = { it.dateString },
                valueTransform = { item ->
                    (periodGroup[item.dateString] ?: emptyList()).partition { diary ->
                        selectedSymbolIds.contains(diary.symbolSequence)
                    }
                },
            )

        // 3. Update the shared member variables and UI on the Main thread
        withContext(Dispatchers.Main) {
            if (binding.month.text.isEmpty()) {
                val currentCal = Calendar.getInstance()
                binding.month.text = currentCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.uppercase()
            }

            dailySymbolList.clear()
            dailySymbolList.addAll(localDailyList)

            dailySymbolAdapter.map.clear()
            dailySymbolAdapter.map.putAll(localMap)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateDailySymbol() {
        withContext(Dispatchers.Default) {
            init365Day()
        }
        binding.dailyCardRecyclerView.minimumHeight = binding.dailyCardRecyclerView.height
        dailySymbolAdapter.notifyDataSetChanged()
    }

    private fun updateDailyCard() {
        binding.run {
            selectedSymbolFlexBox.removeAllViews()

            config.selectedSymbols.split(",").map { sequence ->
                val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(layoutInflater)
                FlavorUtils.initWeatherView(
                    requireContext(),
                    partialDailySymbolBinding.dailySymbol,
                    sequence.toInt(),
                )
                requireActivity().updateDashboardInnerCard(partialDailySymbolBinding.root)
                selectedSymbolFlexBox.addView(partialDailySymbolBinding.root)
            }

            dailyCardRecyclerView.minimumHeight = dailyCardRecyclerView.height
            dailySymbolAdapter.notifyDataSetChanged()
            dailyCardRecyclerView.visibility = View.VISIBLE
        }
    }
}
