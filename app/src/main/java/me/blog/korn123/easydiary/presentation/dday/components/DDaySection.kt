package me.blog.korn123.easydiary.presentation.dday.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The D-Day cards of the dashboard, in the mode the `Flexbox` switch of the card selects.
 *
 * With the switch on, the cards wrap into lines ([DdayCardFlow]) and are held to four lines until `Show all` is
 * tapped. With the switch off, they run in one row that scrolls sideways, the way the horizontal list always did,
 * and every card keeps the width it asks for.
 */
@Composable
fun DdaySection(
    items: List<DDayCardItem>,
    isFlexboxEnabled: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onItemClick: (DDayCardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (isFlexboxEnabled) {
        true ->
            DdayCardFlow(
                items = items,
                collapsedLineCount = COLLAPSED_LINE_COUNT,
                isExpanded = isExpanded,
                onToggleExpand = onToggleExpand,
                modifier = modifier.fillMaxWidth(),
                itemContent = { item ->
                    DdayCard(item = item, onClick = { onItemClick(item) })
                },
            )

        false ->
            DdayHorizontalList(
                items = items,
                onItemClick = onItemClick,
                modifier = modifier.fillMaxWidth(),
            )
    }
}

/**
 * The horizontal list of the `Flexbox` switch turned off: one row of cards that scrolls sideways, which is the
 * only scroll the dashboard has ever had in this mode and does not touch the vertical scroll of the dashboard.
 *
 * A card is no wider than the card of the dashboard itself, so a long title is cut short instead of running off
 * the screen, as it was when the row was a recycler.
 */
@Composable
private fun DdayHorizontalList(
    items: List<DDayCardItem>,
    onItemClick: (DDayCardItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier) {
        val cardMaxWidth = maxWidth
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
            verticalAlignment = Alignment.Top,
        ) {
            items.forEach { item ->
                DdayCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.widthIn(max = cardMaxWidth),
                )
            }
        }
    }
}

/** How many lines of cards the wrap mode shows before `Show all` appears. */
private const val COLLAPSED_LINE_COUNT = 4
