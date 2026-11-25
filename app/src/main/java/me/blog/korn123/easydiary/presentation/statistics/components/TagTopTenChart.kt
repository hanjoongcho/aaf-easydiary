package me.blog.korn123.easydiary.presentation.statistics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagStatistics
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.statistics.TagTopTenChartUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * The tag chart of the statistics screen (issue #270): the tags the most entries carry, strongest first.
 *
 * A bar chart was picked over the pie chart the symbols use because a tag name is longer than a symbol name and
 * an entry carries several tags, so the shares of a pie would be unreadable and would not add up to anything the
 * writer asks about. Here every tag gets a row of its own: its name over the full width of the card, so a long
 * Korean or English name is not cut in half, the count at the right end of that line, and the bar under it
 * measured against the tag used the most. The rows are drawn rather than handed to the chart library the symbol
 * charts use, because that library turns the names into axis labels that cannot be wrapped or shortened per row.
 *
 * The chart paints nothing of its own: the card it sits in carries the background, and the colors come from the
 * theme the rest of the app is drawn with, so a theme change or a different font setting reaches it as well.
 */
@Composable
fun TagTopTenChart(
    uiState: TagTopTenChartUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val textColor = Color(context.config.textColor)
    val primaryColor = Color(context.config.primaryColor)
    // The track a bar grows in is the text color at a low alpha, so it reads the same on the light, the dark and
    // the green theme the app ships instead of depending on how far the background was darkened.
    val trackColor = textColor.copy(alpha = TRACK_ALPHA)
    val labelFontSize = context.config.settingFontSize

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = CHART_HORIZONTAL_PADDING, vertical = CHART_VERTICAL_PADDING),
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(PROGRESS_SIZE),
                    color = primaryColor,
                )
            }

            uiState.tags.isEmpty() -> {
                SimpleText(
                    text = stringResource(R.string.statistics_tag_empty),
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    alpha = EMPTY_STATE_ALPHA,
                    fontSize = labelFontSize,
                    fontColor = textColor,
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                val maxCount = TagStatistics.maxCount(uiState.tags)
                // Ten rows are taller than the card on a landscape phone or at a large font size, so the ranking
                // scrolls instead of losing the tags below the card's edge.
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                ) {
                    uiState.tags.forEachIndexed { index, tag ->
                        TagRankRow(
                            tag = tag,
                            maxCount = maxCount,
                            labelFontSize = labelFontSize,
                            textColor = textColor,
                            barColor = primaryColor,
                            trackColor = trackColor,
                        )
                        if (index != uiState.tags.lastIndex) Spacer(modifier = Modifier.height(ROW_SPACING))
                    }
                }
            }
        }
    }
}

/**
 * One tag of the ranking: the name and its count on a line, the bar of the count under them.
 *
 * The bar grows from nothing when the chart is first drawn, the way the other charts animate, and keeps a small
 * share of the track even for a tag whose count is far below the top one, so a row always shows that it is there.
 */
@Composable
private fun TagRankRow(
    tag: TagWithCount,
    maxCount: Int,
    labelFontSize: Float,
    textColor: Color,
    barColor: Color,
    trackColor: Color,
) {
    val share = if (maxCount > 0) tag.diaryCount.toFloat() / maxCount else 0F
    val animatedShare by animateFloatAsState(
        targetValue = share,
        animationSpec = tween(durationMillis = BAR_ANIMATION_MILLIS),
        label = "tagRankBar",
    )
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            SimpleText(
                text = tag.tag.label,
                modifier = Modifier.weight(1F),
                fontSize = labelFontSize,
                fontColor = textColor,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(COUNT_GAP))
            SimpleText(
                text = tag.diaryCount.toString(),
                modifier = Modifier.widthIn(min = COUNT_MIN_WIDTH),
                fontSize = labelFontSize * COUNT_FONT_SCALE,
                fontWeight = FontWeight.SemiBold,
                fontColor = barColor,
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
        Spacer(modifier = Modifier.height(LABEL_BAR_GAP))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(BAR_HEIGHT)
                    .clip(shape)
                    .background(trackColor),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedShare.coerceIn(MIN_BAR_SHARE, 1F))
                        .clip(shape)
                        .background(barColor),
            )
        }
    }
}

private val CHART_HORIZONTAL_PADDING = 16.dp
private val CHART_VERTICAL_PADDING = 16.dp
private val ROW_SPACING = 12.dp
private val LABEL_BAR_GAP = 4.dp
private val BAR_HEIGHT = 10.dp
private val COUNT_GAP = 8.dp
private val COUNT_MIN_WIDTH = 32.dp
private val PROGRESS_SIZE = 28.dp
private const val EMPTY_STATE_ALPHA = 0.7F
private const val TRACK_ALPHA = 0.12F
private const val COUNT_FONT_SCALE = 0.9F
private const val MIN_BAR_SHARE = 0.04F
private const val BAR_ANIMATION_MILLIS = 500

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TagTopTenChartPreview() {
    AppTheme {
        TagTopTenChart(
            uiState =
                TagTopTenChartUiState(
                    tags =
                        listOf(
                            TagWithCount(tag = Tag(tagId = 1L, name = "travel with family"), diaryCount = 14),
                            TagWithCount(tag = Tag(tagId = 2L, name = "work"), diaryCount = 9),
                            TagWithCount(tag = Tag(tagId = 3L, name = "reading"), diaryCount = 5),
                            TagWithCount(tag = Tag(tagId = 4L, name = "diet"), diaryCount = 2),
                        ),
                    isLoading = false,
                ),
            modifier = Modifier.height(260.dp),
        )
    }
}
