package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import me.blog.korn123.easydiary.adapters.DDayAdapter
import me.blog.korn123.easydiary.databinding.FragmentDdayBinding
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DDay
import me.blog.korn123.easydiary.views.SafeFlexboxLayoutManager

class DDayFragment : Fragment() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinging: FragmentDdayBinding
    private lateinit var mDDayAdapter: DDayAdapter
    private var mDDayItems: MutableList<DDay> = mutableListOf()
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mSafeFlexboxLayoutManager: SafeFlexboxLayoutManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinging = FragmentDdayBinding.inflate(layoutInflater)
        return mBinging.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDDayAdapter = DDayAdapter(requireActivity(), mDDayItems) { updateDDayList() }
        mLinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mSafeFlexboxLayoutManager = SafeFlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        mBinging.run {
            recyclerDays.apply {
                layoutManager = mLinearLayoutManager
                adapter = mDDayAdapter
            }
            flexboxOptionSwitcher.setOnCheckedChangeListener { _, isChecked ->
                recyclerDays.layoutManager = if (isChecked) mSafeFlexboxLayoutManager else mLinearLayoutManager
            }
        }
        updateDDayList()
    }

    private fun updateDDayList() {
        mDDayItems.run {
            clear()
            addAll(EasyDiaryDbHelper.findDDayAll())
            add(DDay("New D-Day!!!"))
        }
        mDDayAdapter.notifyDataSetChanged()
    }
}