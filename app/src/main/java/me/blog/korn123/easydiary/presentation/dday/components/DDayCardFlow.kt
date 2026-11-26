package me.blog.korn123.easydiary.presentation.dday.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.SimpleText

/**
 * The D-Day list of the dashboard in the `Flexbox`(wrap) mode, laid out the way the flexbox list laid it out:
 * from the start, a new line as soon as the next card does not fit, and the free width of every line shared among
 * the stored D-Days of that line, so the cards of a line fill it and the `ADD` card keeps its own width.
 *
 * The list is no scroll container of its own: it is as tall as the lines it draws, so a drag over the card scrolls
 * the dashboard and nothing else - the nested scrolling the recycler caused cannot happen here.
 *
 * The cards past [collapsedLineCount] lines are dropped from the list until `Show all` is tapped, and both the
 * dropping and the `Show all` / `Collapse` row are decided here, from the width of the cards themselves: the whole
 * line that does not fit is enough to know that the row is needed.
 *
 * A card is measured twice, once to learn the width it asks for and once at the width of the line it lands in,
 * because a measured card cannot be resized afterwards and a card has to fill its line to keep the look of the
 * flexbox list. A stored D-Day is then measured at the height of its line as well; the `ADD` card keeps the height
 * of its own label, as the flexbox item of it did. The two measurements are two compositions of the same remembered
 * content, so a measure of the list never composes a card by itself.
 */
@Composable
fun DdayCardFlow(
    items: List<DDayCardItem>,
    collapsedLineCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (DDayCardItem) -> Unit,
) {
    val context = LocalContext.current
    val toggleTextColor = Color(context.config.textColor)
    val toggleTextSize = with(LocalDensity.current) { TOGGLE_TEXT_SIZE_SP.sp.toPx() }

    // The content of the list and of the row under it, made here and not in the measure pass: a lambda made in the
    // measure pass is a new one on every pass, and the list would compose its cards again whenever it is only
    // measured again.
    val cardsSlot: @Composable () -> Unit = {
        items.forEach { item -> itemContent(item) }
    }
    val toggleSlot: @Composable () -> Unit = {
        DdayExpandToggle(
            text = stringResource(if (isExpanded) R.string.dashboard_dday_collapse else R.string.dashboard_dday_show_all),
            textColor = toggleTextColor,
            fontSizePx = toggleTextSize,
            onClick = onToggleExpand,
        )
    }

    SubcomposeLayout(modifier = modifier) { constraints ->
        // The width the cards wrap in. A list that is not given a width is one card per line, with the cards of the
        // width they ask for, which is what the wrap mode would come to anyway.
        val wrapWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity
        if (items.isEmpty() || wrapWidth <= 0) {
            return@SubcomposeLayout layout(constraints.constrainWidth(0), constraints.constrainHeight(0)) {}
        }

        // One composition of the cards to measure them, and one to place them at the size of their line.
        val measuredCards = subcompose(MEASURE_KEY, cardsSlot)
        val placedCards = subcompose(PLACE_KEY, cardsSlot)
        // A card that has not been composed yet is left out of this measure; the next one draws it.
        val cardCount = minOf(items.size, measuredCards.size, placedCards.size)
        if (cardCount == 0) {
            return@SubcomposeLayout layout(constraints.constrainWidth(0), constraints.constrainHeight(0)) {}
        }

        // Measure every card on its own, wrapping them into lines. When the list is collapsed the first line past
        // the limit is enough: the lines above it are what is shown and the line itself is what the row shows.
        val cardWidths = IntArray(cardCount)
        val cardHeights = IntArray(cardCount)
        val lines = mutableListOf<MutableList<Int>>()
        var lineWidth = 0
        var index = 0
        while (index < cardCount) {
            val measured = measuredCards[index].measure(Constraints(maxWidth = wrapWidth))
            cardWidths[index] = measured.width
            cardHeights[index] = measured.height

            when {
                lines.isEmpty() || wrapWidth == Constraints.Infinity || lineWidth + measured.width > wrapWidth -> {
                    lines.add(mutableListOf(index))
                    lineWidth = measured.width
                }

                else -> {
                    lines.last().add(index)
                    lineWidth += measured.width
                }
            }

            index++
            if (!isExpanded && lines.size > collapsedLineCount) break
        }

        val shownLineCount = if (isExpanded) lines.size else minOf(lines.size, collapsedLineCount)
        val hasHiddenItems = lines.size > collapsedLineCount

        val lineTops = IntArray(shownLineCount)
        val lineHeights = IntArray(shownLineCount)
        var contentHeight = 0
        for (lineIndex in 0 until shownLineCount) {
            lineTops[lineIndex] = contentHeight
            lineHeights[lineIndex] = lines[lineIndex].maxOf { cardHeights[it] }
            contentHeight += lineHeights[lineIndex]
        }

        val contentWidth =
            when (wrapWidth) {
                Constraints.Infinity -> (0 until shownLineCount).maxOf { lines[it].sumOf { cardIndex -> cardWidths[cardIndex] } }
                else -> wrapWidth
            }

        val toggle =
            when (hasHiddenItems) {
                true -> subcompose(TOGGLE_KEY, toggleSlot).first().measure(Constraints(maxWidth = wrapWidth))
                false -> null
            }

        val sizedCards = arrayOfNulls<Placeable>(cardCount)
        val sizedCardWidths = IntArray(cardCount)
        for (lineIndex in 0 until shownLineCount) {
            val line = lines[lineIndex]
            val growableCards = line.filter { items[it].isStored }
            val freeWidth = (contentWidth - line.sumOf { cardWidths[it] }).coerceAtLeast(0)
            val sharedWidth = if (growableCards.isEmpty()) 0 else freeWidth / growableCards.size
            val restWidth = if (growableCards.isEmpty()) 0 else freeWidth - sharedWidth * growableCards.size

            line.forEach { cardIndex ->
                // Only a stored D-Day takes part in sharing the free width of its line; the `ADD` card keeps the
                // width it asks for, so the cards of a line never add up to more than the line itself.
                val extraWidth =
                    when {
                        !items[cardIndex].isStored || growableCards.isEmpty() -> 0
                        cardIndex == growableCards.last() -> sharedWidth + restWidth
                        else -> sharedWidth
                    }
                val cardWidth = cardWidths[cardIndex] + extraWidth
                sizedCardWidths[cardIndex] = cardWidth
                // A stored D-Day card fills the height of its line, so the cards of a line stay level. The `ADD`
                // card is not: it keeps its own height and sits at the top of the line, the way
                // `item_dday_add.xml` kept it short under the `top` gravity. Given the height of the line it would
                // draw a tall empty card beside a line of three-line cards.
                val isStored = items[cardIndex].isStored
                val minHeight = if (isStored) lineHeights[lineIndex] else 0
                val maxHeight = lineHeights[lineIndex]
                sizedCards[cardIndex] =
                    placedCards[cardIndex].measure(
                        Constraints(
                            minWidth = cardWidth,
                            maxWidth = cardWidth,
                            minHeight = minHeight,
                            maxHeight = maxHeight,
                        ),
                    )
            }
        }

        layout(constraints.constrainWidth(contentWidth), constraints.constrainHeight(contentHeight + (toggle?.height ?: 0))) {
            for (lineIndex in 0 until shownLineCount) {
                var x = 0
                lines[lineIndex].forEach { cardIndex ->
                    sizedCards[cardIndex]?.place(x, lineTops[lineIndex])
                    x += sizedCardWidths[cardIndex]
                }
            }
            toggle?.place(0, contentHeight)
        }
    }
}

/** `Show all` / `Collapse` under the list, drawn the way the `button_dday_expand` row of the card was. */
@Composable
private fun DdayExpandToggle(
    text: String,
    textColor: Color,
    fontSizePx: Float,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        SimpleText(
            text = text,
            fontSize = fontSizePx,
            fontColor = textColor,
            maxLines = 1,
            lineSpacingScaleFactor = 1F,
        )
    }
}

private const val MEASURE_KEY = "measure"
private const val PLACE_KEY = "place"
private const val TOGGLE_KEY = "toggle"

/** `basic_text_medium`, which `applyGlobalSize` left at its own size. */
private const val TOGGLE_TEXT_SIZE_SP = 15F
