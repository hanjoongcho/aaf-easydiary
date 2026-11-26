package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentDdayBinding
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.presentation.dday.components.DDayCardItem
import me.blog.korn123.easydiary.presentation.dday.components.DDayHighlight
import me.blog.korn123.easydiary.presentation.dday.components.DDaySection
import me.blog.korn123.easydiary.presentation.dday.showDDayDialog
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.DateUtils
import java.text.SimpleDateFormat

/**
 * The D-Day card of the dashboard.
 *
 * The cards of the list are Compose ([DDaySection]): the flexbox(wrap) mode wraps them into lines that stop at
 * four until `Show all` is tapped, and the horizontal mode puts them in one row that scrolls sideways. Neither is
 * a scroll container of its own, so a drag over the card scrolls the dashboard and nothing else, which is what the
 * recycler used to get in the way of.
 *
 * This fragment keeps what the card is made of outside the list: the title, the sort icon and the `Flexbox`
 * switch are the views the card has always had, it loads the D-Days of the repository and hands them over as
 * cards, and it opens the D-Day dialog for a card that is tapped.
 */
class DDayFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentDdayBinding

    /** The cards the Compose list draws. Every change of it, the mode or the expansion redraws the list. */
    private var cardItems by mutableStateOf<List<DDayCardItem>>(emptyList())
    private var isFlexboxEnabled by mutableStateOf(false)
    private var isExpanded by mutableStateOf(false)

    /** The order the list is loaded in, and the items it was last loaded with, so a changed list starts collapsed. */
    private var isReverseOrder = true
    private var lastItemSignature: List<Pair<Int, String>> = emptyList()

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

        isFlexboxEnabled = config.enableDDayFlexboxLayout

        binding.run {
            composeDday.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            composeDday.setContent {
                AppTheme {
                    DDaySection(
                        items = cardItems,
                        isFlexboxEnabled = isFlexboxEnabled,
                        isExpanded = isExpanded,
                        onToggleExpand = { isExpanded = !isExpanded },
                        onItemClick = { item -> openDDayDialog(item) },
                    )
                }
            }
            flexboxOptionSwitcher.isChecked = isFlexboxEnabled
            flexboxOptionSwitcher.setOnCheckedChangeListener { _, isChecked ->
                config.enableDDayFlexboxLayout = isChecked
                isFlexboxEnabled = isChecked
                isExpanded = false
            }
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

    /**
     * Loads the D-Days that are shown and hands them to the list, with a `New D-Day` card at the head and the tail
     * of them, the way the card has always been made up.
     */
    private fun updateDDayList(isAsc: Boolean) {
        lifecycleScope.launch {
            val storedDDays = requireContext().dDayRepository.getAllDDays(isAsc)
            val items = mutableListOf<DDayCardItem>()
            if (storedDDays.isNotEmpty()) items.add(newDDayCardItem())
            items.addAll(storedDDays.map { it.toCardItem() })
            items.add(newDDayCardItem())

            val signature = items.map { it.dDay.id to it.title }
            if (signature != lastItemSignature) {
                // A D-Day was added, changed or deleted: the list starts from the top of itself again.
                lastItemSignature = signature
                isExpanded = false
            }
            cardItems = items
        }
    }

    private fun openDDayDialog(item: DDayCardItem) {
        when (item.isNewItem) {
            true -> requireActivity().showDDayDialog(item.dDay) { updateDDayList(isReverseOrder) }
            false -> requireActivity().showDDayDialog(item.dDay.copy(), item.dDay) { updateDDayList(isReverseOrder) }
        }
    }

    private fun newDDayCardItem(): DDayCardItem =
        DDayCardItem(
            dDay = DDay(title = getString(R.string.dashboard_dday_new_item)),
            isNewItem = true,
            title = getString(R.string.dashboard_dday_new_item),
            targetDate = "",
            dayRemaining = "",
            highlight = DDayHighlight.TODAY,
        )

    /**
     * The card of a stored D-Day: the title, the target date as the list has always written it, and the remaining
     * days on which the lit light depends - today, a day that has passed, or a day still to come.
     */
    private fun DDay.toCardItem(): DDayCardItem {
        val targetDate = DateUtils.getDateStringFromTimeMillis(targetTimeStamp, SimpleDateFormat.MEDIUM)
        val today = DateUtils.getDateStringFromTimeMillis(System.currentTimeMillis(), SimpleDateFormat.MEDIUM)
        val remaining = getOnlyDayRemaining()
        return DDayCardItem(
            dDay = this,
            isNewItem = id == 0,
            title = title,
            targetDate = targetDate,
            // The card writes `D-Day` for the day itself, whatever the remaining days of the model say.
            dayRemaining = if (targetDate == today) "D-Day" else remaining,
            highlight =
                when {
                    remaining == "D-Day" -> DDayHighlight.TODAY
                    remaining.startsWith("D\uFF0B") -> DDayHighlight.ELAPSED
                    else -> DDayHighlight.UPCOMING
                },
        )
    }
}
