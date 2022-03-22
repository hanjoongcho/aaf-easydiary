package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import io.realm.Sort
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DDayAdapter
import me.blog.korn123.easydiary.databinding.FragmentDdayBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
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
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mSafeFlexboxLayoutManager: SafeFlexboxLayoutManager
    private var mDDayItems: MutableList<DDay> = mutableListOf()
    private var mDDaySortOrder = Sort.DESCENDING

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
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

        mDDayAdapter = DDayAdapter(requireActivity(), mDDayItems) { updateDDayList(mDDaySortOrder) }
        mLinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mSafeFlexboxLayoutManager = SafeFlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }
        mBinging.run {
            recyclerDays.apply {
                layoutManager = getDDayLayoutManager()
                adapter = mDDayAdapter
            }
            flexboxOptionSwitcher.setOnCheckedChangeListener { _, isChecked ->
                config.enableDDayFlexboxLayout = isChecked
                recyclerDays.layoutManager = getDDayLayoutManager()
            }
            flexboxOptionSwitcher.isChecked = config.enableDDayFlexboxLayout
            requireActivity().updateDrawableColorInnerCardView(imageDDaySortOrder)
            imageDDaySortOrder.setOnClickListener {
                mDDaySortOrder = when (mDDaySortOrder) {
                    Sort.ASCENDING -> {
                        imageDDaySortOrder.setImageResource(R.drawable.ic_sorting_desc)
                        Sort.DESCENDING
                    }
                    Sort.DESCENDING -> {
                        imageDDaySortOrder.setImageResource(R.drawable.ic_sorting_asc)
                        Sort.ASCENDING
                    }
                }
                updateDDayList(mDDaySortOrder)
            }
        }
        updateDDayList(mDDaySortOrder)
    }

    private fun getDDayLayoutManager(): RecyclerView.LayoutManager = if (config.enableDDayFlexboxLayout) mSafeFlexboxLayoutManager else mLinearLayoutManager

    private fun updateDDayList(sortOrder: Sort) {
        mDDayItems.run {
            clear()
            addAll(EasyDiaryDbHelper.findDDayAll(sortOrder))
            add(DDay("New D-Day!!!"))
        }
        mDDayAdapter.notifyDataSetChanged()
    }
}