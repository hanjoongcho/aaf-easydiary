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
    private lateinit var binding: FragmentDdayBinding
    private lateinit var dDayAdapter: DDayAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var safeFlexboxLayoutManager: FlexboxLayoutManager
    private var dDayItems: MutableList<DDayDomain> = mutableListOf()
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
        binding = FragmentDdayBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        dDayAdapter = DDayAdapter(requireActivity(), dDayItems) { updateDDayList(isReverseOrder) }
        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        safeFlexboxLayoutManager =
            SafeFlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
        binding.run {
            recyclerDays.apply {
                layoutManager = getDDayLayoutManager()
                adapter = dDayAdapter
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

    private fun getDDayLayoutManager(): RecyclerView.LayoutManager = if (config.enableDDayFlexboxLayout) safeFlexboxLayoutManager else linearLayoutManager

    private fun updateDDayList(isAsc: Boolean) {
        lifecycleScope.launch {
            dDayItems.run {
                clear()
                val items = requireContext().dDayRepository.getAllDDays(isAsc)
                if (items.isNotEmpty()) add(DDayDomain(title = "New D-Day!!!"))
                addAll(items)
                add(DDayDomain(title = "New D-Day!!!"))
            }
            dDayAdapter.notifyDataSetChanged()
        }
    }
}
