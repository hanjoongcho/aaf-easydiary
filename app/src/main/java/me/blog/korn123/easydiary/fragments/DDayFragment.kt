package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DDayAdapter
import me.blog.korn123.easydiary.databinding.FragmentDdayBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.views.SafeFlexboxLayoutManager
import me.blog.korn123.easydiary.domain.model.DDay as DDayDomain

class DDayFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinging: FragmentDdayBinding
    private lateinit var mDDayAdapter: DDayAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mSafeFlexboxLayoutManager: FlexboxLayoutManager
    private var mDDayItems: MutableList<DDayDomain> = mutableListOf()
    private var isReverseOrder = true

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinging = FragmentDdayBinding.inflate(layoutInflater)
        return mBinging.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        mDDayAdapter = DDayAdapter(requireActivity(), mDDayItems) { updateDDayList(isReverseOrder) }
        mLinearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        mSafeFlexboxLayoutManager =
            SafeFlexboxLayoutManager(requireContext()).apply {
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
            requireActivity().updateDrawableColorInnerCardView(imageDDaySortOrder, config.textColor)
            imageDDaySortOrder.setOnClickListener {
                isReverseOrder =
                    when (isReverseOrder) {
                        true -> {
                            imageDDaySortOrder.setImageResource(R.drawable.ic_sorting_desc)
                            false
                        }

                        false -> {
                            imageDDaySortOrder.setImageResource(R.drawable.ic_sorting_asc)
                            true
                        }
                    }
                updateDDayList(isReverseOrder)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDDayList(isReverseOrder)
    }

    private fun getDDayLayoutManager(): RecyclerView.LayoutManager = if (config.enableDDayFlexboxLayout) mSafeFlexboxLayoutManager else mLinearLayoutManager

    private fun updateDDayList(isAsc: Boolean) {
        lifecycleScope.launch {
            mDDayItems.run {
                clear()
                val dDayItems = requireContext().dDayRepository.getAllDDays(isAsc)
                if (dDayItems.isNotEmpty()) add(DDayDomain(title = "New D-Day!!!"))
                addAll(dDayItems)
                add(DDayDomain(title = "New D-Day!!!"))
            }
            mDDayAdapter.notifyDataSetChanged()
        }
    }
}
