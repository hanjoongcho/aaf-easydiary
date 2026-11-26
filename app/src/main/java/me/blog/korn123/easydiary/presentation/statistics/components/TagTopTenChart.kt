package me.blog.korn123.easydiary.presentation.statistics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagRankShare
import me.blog.korn123.easydiary.domain.model.TagStatistics
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.darkenColor
import me.blog.korn123.easydiary.extensions.innerCardDarkenFactor
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.statistics.TagTopTenChartUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * The tag chart of the statistics screen (issue #270): the tags the most entries carry, strongest first.
 *
 * The ranking is a ring the tags divide between them, and beside it the same tags as rows that name each slice and
 * state its count and its share of the top ten. The ring answers how much of what the writer tags belongs to one
 * tag at a glance, and the rows keep what a tag name needs, which a pie of the symbol charts could not: a long
 * Korean or English name on a line of its own, the count, and the percent. Choosing a slice or a row holds that
 * tag out of the ring, in the middle and in its row; choosing it again goes back to the whole top ten.
 *
 * [isDashboardMode] draws the same chart for the dashboard card, which is 500dp high and loses the title's room
 * off that: there the tags are chips that wrap two or three to a line under the ring, so all ten stay inside the
 * card instead of being cut off below its edge. The statistics screen opens the chart without the flag and keeps
 * the ten rows.
 *
 * The chart paints nothing of its own: the card it sits in carries the background, and the colors come from the
 * theme the rest of the app is drawn with, so a theme change or a different font setting reaches it as well.
 */
@Composable
fun TagTopTenChart(
    uiState: TagTopTenChartUiState,
    modifier: Modifier = Modifier,
    isDashboardMode: Boolean = false,
) {
    val context = LocalContext.current
    val textColor = Color(context.config.textColor)
    val primaryColor = Color(context.config.primaryColor)
    // The slices have to stay apart on the white card of the light theme, on the grey card of the dark theme and
    // on the cream card of the green theme, so the palette is brightened when the card is dark instead of being
    // pinned to one theme.
    val palette = remember(context.config.backgroundColor) { tagChartPalette(Color(context.config.backgroundColor)) }
    val labelFontSize = context.config.settingFontSize

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isDashboardMode) COMPACT_CHART_HORIZONTAL_PADDING else CHART_HORIZONTAL_PADDING,
                    vertical = if (isDashboardMode) COMPACT_CHART_VERTICAL_PADDING else CHART_VERTICAL_PADDING,
                ),
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
                // The percents are counted inside the ranking, so the slices of the ring add up to 100% instead
                // of leaving the tags below the tenth as an unaccounted rest.
                val shares = remember(uiState.tags) { TagStatistics.shares(uiState.tags) }
                val totalCount = remember(uiState.tags) { TagStatistics.totalCount(uiState.tags) }
                // The chosen tag is kept by its id, so a count that changes under the chart (a tag put on an entry
                // elsewhere) leaves the same tag held out instead of moving the choice to its neighbour.
                var chosenTagId by rememberSaveable { mutableStateOf(NO_TAG_ID) }
                val chosenIndex = shares.indexOfFirst { share -> share.ranked.tag.tagId == chosenTagId }

                TagChartBody(
                    shares = shares,
                    totalCount = totalCount,
                    chosenIndex = chosenIndex,
                    palette = palette,
                    labelFontSize = labelFontSize,
                    textColor = textColor,
                    isDashboardMode = isDashboardMode,
                    onChoose = { index -> chosenTagId = shares.getOrNull(index)?.ranked?.tag?.tagId ?: NO_TAG_ID },
                )
            }
        }
    }
}

/**
 * Puts the ring and the rows in the room the chart was given: under each other on a tall chart, beside each other
 * on a wide one, so a landscape phone keeps a ring large enough to read and still shows all ten rows.
 *
 * In dashboard mode the rows become the chips of [CompactTagLegend] and the ring is capped a little tighter, so
 * the ring and all ten tags fit the 500dp card without a scroll in both portrait and landscape.
 */
@Composable
private fun TagChartBody(
    shares: List<TagRankShare>,
    totalCount: Int,
    chosenIndex: Int,
    palette: List<Color>,
    labelFontSize: Float,
    textColor: Color,
    isDashboardMode: Boolean,
    onChoose: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            val ringSize =
                minOf(
                    maxHeight,
                    maxWidth * (if (isDashboardMode) COMPACT_WIDE_RING_WIDTH_FRACTION else WIDE_RING_WIDTH_FRACTION),
                )
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(ringSize).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    TagRing(
                        shares = shares,
                        totalCount = totalCount,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        ringSize = ringSize,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                    )
                }
                Spacer(modifier = Modifier.width(if (isDashboardMode) COMPACT_SECTION_GAP else SECTION_GAP))
                if (isDashboardMode) {
                    CompactTagLegend(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                        modifier = Modifier.weight(1F).fillMaxHeight(),
                    )
                } else {
                    TagLegend(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                        modifier = Modifier.weight(1F).fillMaxHeight(),
                    )
                }
            }
        } else {
            val ringSize =
                if (isDashboardMode) {
                    // The chips of the compact legend need less than half of what ten rows took, but they still
                    // need their room: the ring gives it back instead of pushing the last chips under the edge.
                    minOf(
                        maxWidth * COMPACT_TALL_RING_WIDTH_FRACTION,
                        maxHeight * COMPACT_TALL_RING_HEIGHT_FRACTION,
                        COMPACT_RING_MAX_SIZE,
                    )
                } else {
                    minOf(maxWidth, maxHeight * TALL_RING_HEIGHT_FRACTION, RING_MAX_SIZE)
                }
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(ringSize), contentAlignment = Alignment.Center) {
                    TagRing(
                        shares = shares,
                        totalCount = totalCount,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        ringSize = ringSize,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                    )
                }
                Spacer(modifier = Modifier.height(if (isDashboardMode) COMPACT_SECTION_GAP else SECTION_GAP))
                // Ten rows are taller than what is left under the ring on a landscape phone or at a large font
                // size, so the rows scroll instead of losing the tags below the card's edge.
                if (isDashboardMode) {
                    CompactTagLegend(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                        modifier = Modifier.weight(1F).fillMaxWidth(),
                    )
                } else {
                    TagLegend(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                        modifier = Modifier.weight(1F).fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * The ring: one slice per tag, its angle the tag's share of the top ten, its color the tag's color in the rows.
 *
 * The slices grow out of nothing when the chart is first drawn, a thin gap keeps two neighbours apart, and the
 * slice of the chosen tag is drawn last and a little wider while the others fade, so it stands out of the ring
 * without the ring losing its shape. A tap on a slice chooses it, and a tap inside the ring or on the chosen slice
 * again goes back to the whole top ten.
 */
@Composable
private fun TagRing(
    shares: List<TagRankShare>,
    totalCount: Int,
    chosenIndex: Int,
    palette: List<Color>,
    ringSize: Dp,
    labelFontSize: Float,
    textColor: Color,
    onChoose: (Int) -> Unit,
) {
    // A state animation starts at its target on the first composition, so the ring is opened one frame after it
    // is composed instead of appearing already full.
    var opened by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { opened = true }
    val sweepProgress by animateFloatAsState(
        targetValue = if (opened) 1F else 0F,
        animationSpec = tween(durationMillis = RING_ANIMATION_MILLIS),
        label = "tagRingSweep",
    )
    val dimProgress by animateFloatAsState(
        targetValue = if (chosenIndex >= 0) 1F else 0F,
        animationSpec = tween(durationMillis = CHOICE_ANIMATION_MILLIS),
        label = "tagRingDim",
    )
    val strokeWidth = ringSize * RING_STROKE_FRACTION
    val chosenShare = shares.getOrNull(chosenIndex)

    Box(
        modifier =
            Modifier
                .size(ringSize)
                .pointerInput(shares, chosenIndex, ringSize) {
                    val radiusPx = ringSize.toPx() / 2F
                    val holePx = (radiusPx - strokeWidth.toPx() * HOLE_ACCEPTS_TAP_FRACTION).coerceAtLeast(0F)
                    detectTapGestures { offset ->
                        val fromCenter = Offset(x = offset.x - radiusPx, y = offset.y - radiusPx)
                        val distance = sqrt(fromCenter.x * fromCenter.x + fromCenter.y * fromCenter.y)
                        // Inside the ring there is no slice, so a tap there is how a writer lets the chosen tag go
                        // and reads the whole top ten again.
                        val index = if (distance < holePx) NO_INDEX else shares.indexAt(clockwiseAngleOf(fromCenter))
                        onChoose(if (index == chosenIndex) NO_INDEX else index)
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringStrokePx = strokeWidth.toPx()
            val ringArcTopLeft = Offset(ringStrokePx / 2F, ringStrokePx / 2F)
            val ringArcSize = Size(size.width - ringStrokePx, size.height - ringStrokePx)
            // The empty ring stays under the slices, so a chart that has just opened already shows the shape the
            // slices are growing into.
            drawArc(
                color = textColor.copy(alpha = RING_TRACK_ALPHA),
                startAngle = 0F,
                sweepAngle = FULL_CIRCLE_DEGREES,
                useCenter = false,
                topLeft = ringArcTopLeft,
                size = ringArcSize,
                style = Stroke(width = ringStrokePx, cap = StrokeCap.Butt),
            )

            val sweeps = shares.map { share -> share.percent * DEGREES_PER_PERCENT * sweepProgress }
            // Every slice keeps the angle its own share gives it, counted in the order of the ranking from the top of
            // the ring. The drawing order must not move a slice: sorting the chosen one to the end and then piling
            // the sweeps up in that order pushed it to the last angle and pulled the slices behind it forward, so
            // the ring no longer matched the rows and a tap landed on a slice other than the one under the finger.
            val sliceStarts = FloatArray(sweeps.size)
            var startAngle = RING_START_ANGLE
            sweeps.forEachIndexed { index, sweep ->
                sliceStarts[index] = startAngle
                startAngle += sweep
            }

            // The neighbours of the chosen slice are drawn first and faded, each one at its own angle.
            sweeps.forEachIndexed { index, sweep ->
                if (index != chosenIndex) {
                    val gap = sliceGap(shares.size, sweep)
                    drawArc(
                        color =
                            palette[index % palette.size].copy(
                                alpha = if (chosenIndex < 0) 1F else 1F - DIMMED_ALPHA * dimProgress,
                            ),
                        startAngle = sliceStarts[index] + gap / 2F,
                        sweepAngle = (sweep - gap).coerceAtLeast(0F),
                        useCenter = false,
                        topLeft = ringArcTopLeft,
                        size = ringArcSize,
                        style = Stroke(width = ringStrokePx, cap = StrokeCap.Butt),
                    )
                }
            }

            // The chosen slice is painted last and once, a little wider and centred on the ring's line, which reads as
            // the slice lifting out of the ring as the others fade. It stays at the angle its share gave it, and while
            // the choice animates in (dimProgress at 0) it is still drawn, at the ring's own width, so it never
            // disappears for a frame.
            if (chosenIndex in sweeps.indices) {
                val sweep = sweeps[chosenIndex]
                val gap = sliceGap(shares.size, sweep)
                val widerStrokePx = ringStrokePx * (1F + CHOSEN_STROKE_GROWTH * dimProgress)
                val widerArcTopLeft = Offset(widerStrokePx / 2F, widerStrokePx / 2F)
                val widerArcSize = Size(size.width - widerStrokePx, size.height - widerStrokePx)
                drawArc(
                    color = palette[chosenIndex % palette.size],
                    startAngle = sliceStarts[chosenIndex] + gap / 2F,
                    sweepAngle = (sweep - gap).coerceAtLeast(0F),
                    useCenter = false,
                    topLeft = widerArcTopLeft,
                    size = widerArcSize,
                    style = Stroke(width = widerStrokePx, cap = StrokeCap.Butt),
                )
            }
        }

        TagRingCenter(
            chosenShare = chosenShare,
            totalCount = totalCount,
            innerDiameter = ringSize - strokeWidth * 2F,
            labelFontSize = labelFontSize,
            textColor = textColor,
        )
    }
}

/**
 * What the middle of the ring states: the whole top ten while no tag is chosen, and the chosen tag's name, count
 * and percent once one is. The text is sized from the hole rather than from the screen, so it stays inside the
 * ring on a small card and at a large font size.
 */
@Composable
private fun TagRingCenter(
    chosenShare: TagRankShare?,
    totalCount: Int,
    innerDiameter: Dp,
    labelFontSize: Float,
    textColor: Color,
) {
    // SimpleText takes its size in pixels, so the hole is converted to pixels before it caps those sizes. Comparing
    // them against the hole in dp shrank the middle text by the screen density, to a few sp on a high-density phone.
    val holePx = with(LocalDensity.current) { innerDiameter.toPx() }
    val valueFontSize = minOf(labelFontSize * CENTER_VALUE_FONT_SCALE, holePx * CENTER_VALUE_MAX_SHARE)
    val nameFontSize = minOf(labelFontSize * CENTER_NAME_FONT_SCALE, holePx * CENTER_NAME_MAX_SHARE)
    val captionFontSize = minOf(labelFontSize * CENTER_CAPTION_FONT_SCALE, holePx * CENTER_CAPTION_MAX_SHARE)

    Column(
        modifier = Modifier.width(innerDiameter * CENTER_WIDTH_FRACTION),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (chosenShare == null) {
            SimpleText(
                text = totalCount.toString(),
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                fontColor = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            SimpleText(
                text = stringResource(R.string.statistics_tag_total),
                fontSize = captionFontSize,
                fontColor = textColor,
                alpha = CENTER_CAPTION_ALPHA,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        } else {
            SimpleText(
                text = chosenShare.ranked.tag.label,
                fontSize = nameFontSize,
                fontWeight = FontWeight.SemiBold,
                fontColor = textColor,
                textAlign = TextAlign.Center,
                maxLines = CENTER_NAME_MAX_LINES,
            )
            SimpleText(
                text = chosenShare.ranked.diaryCount.toString(),
                fontSize = valueFontSize,
                fontWeight = FontWeight.Bold,
                fontColor = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            SimpleText(
                text = percentText(chosenShare.percent),
                fontSize = captionFontSize,
                fontColor = textColor,
                alpha = CENTER_CAPTION_ALPHA,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/**
 * The rows of the legend: the tag's color, its name, its count and its share of the top ten.
 *
 * A row is a tap target of its own, so a tag can be held out from the list as well as from the ring, and the
 * chosen row is tinted with its own color so a reader can follow one tag from the ring to its line and back.
 */
@Composable
private fun TagLegend(
    shares: List<TagRankShare>,
    chosenIndex: Int,
    palette: List<Color>,
    labelFontSize: Float,
    textColor: Color,
    onChoose: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        shares.forEachIndexed { index, share ->
            TagLegendRow(
                share = share,
                color = palette[index % palette.size],
                isChosen = index == chosenIndex,
                labelFontSize = labelFontSize,
                textColor = textColor,
                onClick = { onChoose(if (index == chosenIndex) NO_INDEX else index) },
            )
        }
    }
}

@Composable
private fun TagLegendRow(
    share: TagRankShare,
    color: Color,
    isChosen: Boolean,
    labelFontSize: Float,
    textColor: Color,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (isChosen) color.copy(alpha = CHOSEN_ROW_ALPHA) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(INDICATOR_SIZE).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(INDICATOR_GAP))
        SimpleText(
            text = share.ranked.tag.label,
            modifier = Modifier.weight(1F),
            fontSize = labelFontSize,
            fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
            fontColor = textColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(COUNT_GAP))
        SimpleText(
            text = share.ranked.diaryCount.toString(),
            modifier = Modifier.widthIn(min = COUNT_MIN_WIDTH),
            fontSize = labelFontSize * COUNT_FONT_SCALE,
            fontWeight = FontWeight.SemiBold,
            fontColor = textColor,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.width(PERCENT_GAP))
        SimpleText(
            text = percentText(share.percent),
            modifier = Modifier.widthIn(min = PERCENT_MIN_WIDTH),
            fontSize = labelFontSize * PERCENT_FONT_SCALE,
            fontColor = textColor,
            alpha = if (isChosen) 1F else PERCENT_ALPHA,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * The legend of the dashboard card: the same tags as chips that wrap two or three to a line.
 *
 * The card is 500dp high, and ten full rows under the ring need more than what the title leaves of that, so in the
 * card the rows become chips: a tag's color, its name, its count and its share, each chip one half of the line, so
 * two of them stand in a column and ten chips need five lines instead of ten, which is what keeps the whole top ten
 * inside the card without a scroll. A chip is a tap target of its own and tints like a row when its tag is chosen,
 * and the chips still scroll if a very large font setting makes them taller than the card.
 */
@Composable
private fun CompactTagLegend(
    shares: List<TagRankShare>,
    chosenIndex: Int,
    palette: List<Color>,
    labelFontSize: Float,
    textColor: Color,
    onChoose: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val chipBackground = Color(context.config.backgroundColor.darkenColor(context.innerCardDarkenFactor()))
    BoxWithConstraints(modifier = modifier) {
        // A chip never takes more than half of a line, however long its tag name is: the name is shortened inside
        // the chip instead of pushing its neighbour past the card's edge, so two chips always share a line.
        val chipMaxWidth = (maxWidth - COMPACT_CHIP_SPACING) / 2F        FlowRow(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(COMPACT_CHIP_SPACING),
            verticalArrangement = Arrangement.spacedBy(COMPACT_CHIP_SPACING),
        ) {
            shares.forEachIndexed { index, share ->
                CompactTagChip(
                    share = share,
                    color = palette[index % palette.size],
                    chipBackground = chipBackground,
                    isChosen = index == chosenIndex,
                    labelFontSize = labelFontSize,
                    textColor = textColor,
                    maxWidth = chipMaxWidth,
                    onClick = { onChoose(if (index == chosenIndex) NO_INDEX else index) },
                )
            }
        }
    }
}

/** One tag of the compact legend: its color, its name, its count and its share of the top ten on a small chip. */
@Composable
private fun CompactTagChip(
    share: TagRankShare,
    color: Color,
    chipBackground: Color,
    isChosen: Boolean,
    labelFontSize: Float,
    textColor: Color,
    maxWidth: Dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)
    Row(
        modifier =
            Modifier
                .widthIn(max = maxWidth)
                .clip(shape)
                .background(if (isChosen) color.copy(alpha = CHOSEN_ROW_ALPHA) else chipBackground)
                .clickable(onClick = onClick)
                .padding(horizontal = COMPACT_CHIP_HORIZONTAL_PADDING, vertical = COMPACT_CHIP_VERTICAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(COMPACT_INDICATOR_SIZE).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(COMPACT_INDICATOR_GAP))
        SimpleText(
            text = share.ranked.tag.label,
            modifier = Modifier.weight(1F),
            fontSize = labelFontSize * COMPACT_NAME_FONT_SCALE,
            fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
            fontColor = textColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(COMPACT_COUNT_GAP))
        SimpleText(
            text = share.ranked.diaryCount.toString(),
            fontSize = labelFontSize * COMPACT_COUNT_FONT_SCALE,
            fontWeight = FontWeight.SemiBold,
            fontColor = textColor,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(COMPACT_PERCENT_GAP))
        SimpleText(
            text = percentText(share.percent),
            fontSize = labelFontSize * COMPACT_PERCENT_FONT_SCALE,
            fontColor = textColor,
            alpha = if (isChosen) 1F else PERCENT_ALPHA,
            maxLines = 1,
        )
    }
}

/** The gap kept between two neighbouring slices: thin, and never so wide that a small slice disappears. */
private fun sliceGap(
    sliceCount: Int,
    sweep: Float,
): Float = if (sliceCount > 1) minOf(RING_GAP_DEGREES, sweep * MAX_GAP_SHARE_OF_SWEEP) else 0F

/** The share of a row as it is written beside its count. */
private fun percentText(percent: Int): String = "$percent%"

/** The slice a tap at [angle] degrees clockwise from the top of the ring falls in. */
private fun List<TagRankShare>.indexAt(angle: Float): Int {
    if (isEmpty()) return NO_INDEX
    var startAngle = 0F
    forEachIndexed { index, share ->
        val sweep = share.percent * DEGREES_PER_PERCENT
        if (angle < startAngle + sweep) return index
        startAngle += sweep
    }
    // A ring whose percents stop a hair short of 360 degrees still has its last slice under the pointer.
    return lastIndex
}

/** The angle of a tap at [fromCenter] counted clockwise from the top of the ring, where the first slice starts. */
private fun clockwiseAngleOf(fromCenter: Offset): Float {
    val degrees = Math.toDegrees(atan2(fromCenter.y.toDouble(), fromCenter.x.toDouble())).toFloat()
    return (degrees - RING_START_ANGLE + FULL_CIRCLE_DEGREES) % FULL_CIRCLE_DEGREES
}

/**
 * The ten colors the slices and the rows are painted with (issue #270).
 *
 * The first color is the one of the tag used the most, and the order keeps hues that look alike apart, so two
 * neighbouring slices of a small ring are still told apart. The palette is not the theme's primary color spread
 * over ten tones, because ten tags cannot be told apart with ten tones of one color: it is a set of ten hues, and
 * its dark-card variant brightens them instead of leaving the darker ones lost on the grey card.
 */
private fun tagChartPalette(cardColor: Color): List<Color> {
    val brightening = if (cardColor.luminance() < DARK_CARD_LUMINANCE) DARK_CARD_BRIGHTENING else 0F
    return TAG_PALETTE.map { color -> if (brightening > 0F) lerp(color, Color.White, brightening) else color }
}

private val TAG_PALETTE =
    listOf(
        Color(0xFF4E79A7), // blue
        Color(0xFFF28E2B), // orange
        Color(0xFF59A14F), // green
        Color(0xFFD37295), // rose
        Color(0xFF76B7B2), // teal
        Color(0xFFB07AA1), // purple
        Color(0xFFD9A62E), // gold
        Color(0xFFE15759), // red
        Color(0xFFBAB0AC), // grey
        Color(0xFF9C755F), // brown
    )

/** A tag of the database has a positive id, so a negative one stands for "no tag chosen". */
private const val NO_TAG_ID = -1L

/** The index a tap or a row gives back when nothing is chosen, or when the chosen thing is chosen again. */
private const val NO_INDEX = -1
private const val FULL_CIRCLE_DEGREES = 360F
private const val RING_START_ANGLE = -90F
private const val DEGREES_PER_PERCENT = 3.6F
private const val RING_GAP_DEGREES = 1.8F
private const val MAX_GAP_SHARE_OF_SWEEP = 0.35F
private const val RING_STROKE_FRACTION = 0.22F
private const val HOLE_ACCEPTS_TAP_FRACTION = 0.9F
private const val RING_TRACK_ALPHA = 0.10F
private const val DIMMED_ALPHA = 0.62F
private const val CHOSEN_STROKE_GROWTH = 0.14F
private const val RING_ANIMATION_MILLIS = 700
private const val CHOICE_ANIMATION_MILLIS = 220
private const val DARK_CARD_LUMINANCE = 0.35F
private const val DARK_CARD_BRIGHTENING = 0.24F
private const val WIDE_RING_WIDTH_FRACTION = 0.42F
private const val TALL_RING_HEIGHT_FRACTION = 0.5F
private const val COMPACT_WIDE_RING_WIDTH_FRACTION = 0.38F
private const val COMPACT_TALL_RING_WIDTH_FRACTION = 0.7F
private const val COMPACT_TALL_RING_HEIGHT_FRACTION = 0.5F
private const val CENTER_WIDTH_FRACTION = 0.86F
private const val CENTER_VALUE_FONT_SCALE = 1.9F
private const val CENTER_VALUE_MAX_SHARE = 0.30F
private const val CENTER_NAME_FONT_SCALE = 0.95F
private const val CENTER_NAME_MAX_SHARE = 0.13F
private const val CENTER_CAPTION_FONT_SCALE = 0.8F
private const val CENTER_CAPTION_MAX_SHARE = 0.11F
private const val CENTER_NAME_MAX_LINES = 2
private const val CENTER_CAPTION_ALPHA = 0.7F
private const val CHOSEN_ROW_ALPHA = 0.16F
private const val PERCENT_ALPHA = 0.7F
private const val COUNT_FONT_SCALE = 0.95F
private const val PERCENT_FONT_SCALE = 0.85F
private const val COMPACT_NAME_FONT_SCALE = 0.9F
private const val COMPACT_COUNT_FONT_SCALE = 0.85F
private const val COMPACT_PERCENT_FONT_SCALE = 0.8F
private const val EMPTY_STATE_ALPHA = 0.7F
private val RING_MAX_SIZE = 260.dp
private val COMPACT_RING_MAX_SIZE = 220.dp
private val CHART_HORIZONTAL_PADDING = 16.dp
private val CHART_VERTICAL_PADDING = 16.dp
private val SECTION_GAP = 16.dp
private val COMPACT_CHART_HORIZONTAL_PADDING = 12.dp
private val COMPACT_CHART_VERTICAL_PADDING = 8.dp
private val COMPACT_SECTION_GAP = 8.dp
private val COMPACT_CHIP_SPACING = 6.dp
private val COMPACT_CHIP_HORIZONTAL_PADDING = 6.dp
private val COMPACT_CHIP_VERTICAL_PADDING = 3.dp
private val COMPACT_INDICATOR_SIZE = 8.dp
private val COMPACT_INDICATOR_GAP = 4.dp
private val COMPACT_COUNT_GAP = 4.dp
private val COMPACT_PERCENT_GAP = 4.dp
private val PROGRESS_SIZE = 28.dp
private val INDICATOR_SIZE = 10.dp
private val INDICATOR_GAP = 10.dp
private val COUNT_GAP = 8.dp
private val PERCENT_GAP = 8.dp
private val COUNT_MIN_WIDTH = 28.dp
private val PERCENT_MIN_WIDTH = 40.dp
private val ROW_HORIZONTAL_PADDING = 6.dp
private val ROW_VERTICAL_PADDING = 6.dp

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 392, heightDp = 500)
@Composable
private fun TagTopTenChartDashboardPreview() {
    AppTheme {
        // The 500dp dashboard card, with all ten tags the chart can hold, to see that the chips wrap inside it.
        TagTopTenChart(
            uiState =
                TagTopTenChartUiState(
                    tags =
                        listOf(
                            TagWithCount(tag = Tag(tagId = 1L, name = "travel with family"), diaryCount = 14),
                            TagWithCount(tag = Tag(tagId = 2L, name = "work"), diaryCount = 9),
                            TagWithCount(tag = Tag(tagId = 3L, name = "reading"), diaryCount = 5),
                            TagWithCount(tag = Tag(tagId = 4L, name = "diet"), diaryCount = 2),
                            TagWithCount(tag = Tag(tagId = 5L, name = "exercise"), diaryCount = 2),
                            TagWithCount(tag = Tag(tagId = 6L, name = "family"), diaryCount = 1),
                            TagWithCount(tag = Tag(tagId = 7L, name = "friends"), diaryCount = 1),
                            TagWithCount(tag = Tag(tagId = 8L, name = "money"), diaryCount = 1),
                            TagWithCount(tag = Tag(tagId = 9L, name = "health"), diaryCount = 1),
                            TagWithCount(tag = Tag(tagId = 10L, name = "study"), diaryCount = 1),
                        ),
                    isLoading = false,
                ),
            isDashboardMode = true,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, heightDp = 460)
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
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 640, heightDp = 300)
@Composable
private fun TagTopTenChartWidePreview() {
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
        )
    }
}
