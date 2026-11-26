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

    /** Every item of the repository, the `New D-Day` cards included. */
    private var dDayItems: MutableList<DDayDomain> = mutableListOf()

    /** The items the adapter binds: all of [dDayItems] or its first [collapsedItemCount] while collapsed. */
    private val visibleDDayItems: MutableList<DDayDomain> = mutableListOf()
    private var isReverseOrder = true
    private var isExpanded = false

    /** Number of items that fit in the first [COLLAPSED_LINE_COUNT] flex lines, [Int.MAX_VALUE] when all of them fit. */
    private var collapsedItemCount: Int = Int.MAX_VALUE

    /** Whether [collapsedItemCount] was measured against the current [dDayItems] and recycler width. */
    private var isCollapsedItemCountValid = false
    private var lastMeasuredWidth = 0
    private var dDayItemSignature: List<Pair<Int, String>> = emptyList()
    private var expandButtonLabel = 0

    companion object {
        private const val COLLAPSED_LINE_COUNT = 4
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
        binding = FragmentDdayBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        dDayAdapter = DDayAdapter(requireActivity(), visibleDDayItems) { updateDDayList(isReverseOrder) }
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
                addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
                    val width = right - left
                    if (width > 0) {
                        // A different width re-wraps the flex lines, so the line count is measured again.
                        if (lastMeasuredWidth != 0 && lastMeasuredWidth != width) invalidateCollapsedItemCount()
                        lastMeasuredWidth = width
                    }
                    if (canMeasureCollapsedItemCount()) {
                        // The items are laid out here, so this is the moment the flex lines can be counted.
                        measureCollapsedItemCount()
                        binding.recyclerDays.post { applyDDayDisplayState() }
                    }
                }
            }
            buttonDdayExpand.setOnClickListener {
                isExpanded = !isExpanded
                applyDDayDisplayState()
            }
            flexboxOptionSwitcher.setOnCheckedChangeListener { _, isChecked ->
                config.enableDDayFlexboxLayout = isChecked
                recyclerDays.layoutManager = getDDayLayoutManager()
                isExpanded = false
                isCollapsedItemCountValid = false
                applyDDayDisplayState()
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
            val storedDDays = requireContext().dDayRepository.getAllDDays(isAsc)
            val items = mutableListOf<DDayDomain>()
            if (storedDDays.isNotEmpty()) items.add(DDayDomain(title = getString(R.string.dashboard_dday_new_item)))
            items.addAll(storedDDays)
            items.add(DDayDomain(title = getString(R.string.dashboard_dday_new_item)))

            val signature = items.map { it.id to it.title }
            val isSameItems = dDayItemSignature == signature
            dDayItems.run {
                clear()
                addAll(items)
            }
            if (!isSameItems) {
                // A different set of items or titles re-wraps the flex lines, so the line count is measured again.
                dDayItemSignature = signature
                isExpanded = false
                collapsedItemCount = Int.MAX_VALUE
                isCollapsedItemCountValid = false
            }
            applyDDayDisplayState(isRefresh = true)
        }
    }

    /**
     * Shows every item, or only the first [collapsedItemCount] ones while collapsed, and decides whether
     * `Show all` / `Collapse` is needed: only in the flexbox(wrap) mode, and only when the items take more
     * than [COLLAPSED_LINE_COUNT] flex lines.
     *
     * @param isRefresh rebinds every item even when the list did not change, because the `D-N` texts depend on the day
     */
    private fun applyDDayDisplayState(isRefresh: Boolean = false) {
        if (view == null) return
        val flexboxEnabled = config.enableDDayFlexboxLayout
        val hasHiddenItems = flexboxEnabled && isCollapsedItemCountValid && collapsedItemCount < dDayItems.size
        val isCollapsed = hasHiddenItems && !isExpanded
        val itemsToShow = when (isCollapsed) {
            true -> dDayItems.take(collapsedItemCount)
            false -> dDayItems.toList()
        }

        if (isRefresh || visibleDDayItems != itemsToShow) {
            visibleDDayItems.run {
                clear()
                addAll(itemsToShow)
            }
            dDayAdapter.notifyDataSetChanged()
        }

        binding.buttonDdayExpand.run {
            visibility = if (hasHiddenItems) View.VISIBLE else View.GONE
            val label = if (isExpanded) R.string.dashboard_dday_collapse else R.string.dashboard_dday_show_all
            if (expandButtonLabel != label) {
                expandButtonLabel = label
                text = getString(label)
            }
        }

        scheduleCollapsedItemCount()
    }

    /** The flex lines can be counted only on a layout in which every item is bound, so never while something is hidden. */
    private fun canMeasureCollapsedItemCount(): Boolean =
        config.enableDDayFlexboxLayout &&
            !isCollapsedItemCountValid &&
            visibleDDayItems.size == dDayItems.size &&
            binding.recyclerDays.childCount > 0

    private fun invalidateCollapsedItemCount() {
        if (!isCollapsedItemCountValid) return
        isCollapsedItemCountValid = false
        if (!config.enableDDayFlexboxLayout) return
        // While collapsed the hidden items have to come back before the flex lines can be counted again.
        binding.recyclerDays.post { applyDDayDisplayState() }
    }

    /**
     * A laid out recycler measures itself, the layout listener is what normally triggers the counting. Only when the
     * list did not change and no layout is pending is the measurement started from here, so that it is never read
     * from a layout that is about to be replaced.
     */
    private fun scheduleCollapsedItemCount() {
        if (!canMeasureCollapsedItemCount()) return
        if (binding.recyclerDays.isLayoutRequested) return
        binding.recyclerDays.post {
            if (!canMeasureCollapsedItemCount() || binding.recyclerDays.isLayoutRequested) return@post
            measureCollapsedItemCount()
            applyDDayDisplayState()
        }
    }

    /**
     * Counts how many items sit in the first [COLLAPSED_LINE_COUNT] flex lines of the laid out recycler, so that
     * the collapsed card shows whole lines instead of a half-cut one. Items are dropped from the adapter for that,
     * which keeps the recycler height equal to its contents: the list itself never scrolls, the dashboard does.
     */
    private fun measureCollapsedItemCount() {
        if (view == null) return
        if (dDayItems.isEmpty()) {
            collapsedItemCount = Int.MAX_VALUE
            isCollapsedItemCountValid = true
            return
        }

        val recyclerView = binding.recyclerDays
        val children =
            (0 until recyclerView.childCount)
                .map { recyclerView.getChildAt(it) }
                .filter { recyclerView.getChildAdapterPosition(it) != RecyclerView.NO_POSITION }
                .sortedBy { recyclerView.getChildAdapterPosition(it) }
        if (children.isEmpty()) return

        var lineIndex = -1
        var lineTop = Int.MIN_VALUE
        var itemCountInCollapsedLines = 0
        children.forEach { child ->
            if (child.top != lineTop) {
                lineIndex++
                lineTop = child.top
            }
            if (lineIndex < COLLAPSED_LINE_COUNT) itemCountInCollapsedLines++
        }

        collapsedItemCount = when (lineIndex + 1 > COLLAPSED_LINE_COUNT) {
            true -> itemCountInCollapsedLines
            false -> Int.MAX_VALUE
        }
        isCollapsedItemCountValid = true
    }
}
