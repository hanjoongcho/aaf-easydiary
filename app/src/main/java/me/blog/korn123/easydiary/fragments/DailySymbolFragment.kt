package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.DateUtils
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.activities.SymbolFilterPickerActivity
import me.blog.korn123.easydiary.adapters.DailySymbolAdapter
import me.blog.korn123.easydiary.databinding.FragmentDailySymbolBinding
import me.blog.korn123.easydiary.databinding.PartialDailySymbolBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
import java.text.SimpleDateFormat
import java.util.*

class DailySymbolFragment : Fragment() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentDailySymbolBinding
    private lateinit var mDailySymbolAdapter: DailySymbolAdapter
    private var mDailySymbolList: ArrayList<DailySymbolAdapter.DailySymbol> = arrayListOf()
    private val mRequestUpdateDailySymbol = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) updateDailyCard()
    }
    private var mInitializeDailySymbolJob: Job? = null
    private var mUpdateDailyCardJob: Job? = null


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDailySymbolBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeDailySymbol()
        mBinding.editSymbolFilter.setOnClickListener {
            Intent(requireContext(), SymbolFilterPickerActivity::class.java).apply {
                mRequestUpdateDailySymbol.launch(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mInitializeDailySymbolJob?.run { if (isActive) cancel() }
        mUpdateDailyCardJob?.run { if (isActive) cancel() }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initializeDailySymbol() {
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault())
        val dateFormat = SimpleDateFormat(DateUtils.DATE_PATTERN_DASH, Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = Date()
        mBinding.month.text = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()).uppercase()

        mDailySymbolAdapter = DailySymbolAdapter(
            requireActivity(),
            mDailySymbolList
        )
        mBinding.dailyCardRecyclerView.apply {
//            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@DashboardActivity, 1)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mDailySymbolAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    mBinding.month.text = mDailySymbolList[(mBinding.dailyCardRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()].date
                }
            })
        }

        mInitializeDailySymbolJob = CoroutineScope(Dispatchers.IO).launch { // launch a new coroutine and keep a reference to its Job
            for (num in 1..365) {
                mDailySymbolList.add(
                    DailySymbolAdapter.DailySymbol(
                        dateFormat.format(cal.time),
                        cal.get(Calendar.DAY_OF_WEEK),
                        cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())!!,
                        dayOfMonth.format(cal.time),
                        cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())!!.uppercase()
                    )
                )
                cal.add(Calendar.DATE, -1)
            }
            withContext(Dispatchers.Main) {
                updateDailyCard()
            }
        }
    }

    private fun updateDailyCard() {
        mBinding.run {
//            month.visibility = View.GONE
//            dailyCardRecyclerView.visibility = View.GONE
            dailyCardProgressBar.visibility = View.VISIBLE
            selectedSymbolFlexBox.removeAllViews()

            mUpdateDailyCardJob = CoroutineScope(Dispatchers.IO).launch {
                config.selectedSymbols.split(",").map { sequence ->
                    val partialDailySymbolBinding = PartialDailySymbolBinding.inflate(layoutInflater)
                    withContext(Dispatchers.Main) {
                        FlavorUtils.initWeatherView(requireContext(), partialDailySymbolBinding.dailySymbol, sequence.toInt())
                        requireActivity().updateDashboardInnerCard(partialDailySymbolBinding.root)
                        selectedSymbolFlexBox.addView(partialDailySymbolBinding.root)
                    }
                }

                withContext(Dispatchers.Main) {
                    mDailySymbolAdapter.notifyDataSetChanged()
//                    month.visibility = View.VISIBLE
//                    dailyCardRecyclerView.visibility = View.VISIBLE
                    dailyCardProgressBar.visibility = View.GONE
//                    requireActivity().updateAppViews(selectedSymbolFlexBox)
                }
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
}