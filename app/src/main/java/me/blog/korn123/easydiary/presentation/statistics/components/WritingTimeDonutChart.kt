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
import me.blog.korn123.easydiary.domain.model.WritingTimeRange
import me.blog.korn123.easydiary.domain.model.WritingTimeShare
import me.blog.korn123.easydiary.domain.model.WritingTimeStatistics
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.statistics.WritingTimeChartUiState
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * The writing time chart of the statistics screen: the entries counted by the hour they were written at.
 *
 * The day is a ring the spans of [WritingTimeRange] divide between them, from one o'clock at the top round to
 * midnight, and beside it the same spans as rows that name each slice and state its count and its share. The ring
 * answers when the writer writes at a glance, and the rows state the hours the ring cannot hold: a span no entry
 * falls in stays as a row of zero, so the day reads whole instead of skipping the hours the writer does not use.
 * Choosing a slice or a row holds that span out of the ring, in the middle and in its row; choosing it again goes
 * back to the whole day.
 *
 * The order of the spans is the clock, not a rank, so the ring is drawn once in that order: a chosen slice keeps
 * the angle its share gives it and is only painted wider (issue #291), which leaves the slices where the rows say
 * they are and lets a tap land on the slice under the finger.
 *
 * The chart paints nothing of its own: the card it sits in carries the background, and the colors come from the
 * theme the rest of the app is drawn with, so a theme change or a different font setting reaches it as well.
 */
@Composable
fun WritingTimeDonutChart(
    uiState: WritingTimeChartUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val textColor = Color(context.config.textColor)
    val primaryColor = Color(context.config.primaryColor)
    // The slices have to stay apart on the white card of the light theme, on the grey card of the dark theme and
    // on the cream card of the green theme, so the palette is brightened when the card is dark instead of being
    // pinned to one theme.
    val palette = remember(context.config.backgroundColor) { writingTimePalette(Color(context.config.backgroundColor)) }
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

            uiState.shares.sumOf { share -> share.diaryCount } == 0 -> {
                SimpleText(
                    text = stringResource(R.string.statistics_creation_time_empty),
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    alpha = EMPTY_STATE_ALPHA,
                    fontSize = labelFontSize,
                    fontColor = textColor,
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                // The spans never move, so the chosen one is kept by its index in the day rather than by its angle.
                var chosenIndex by rememberSaveable { mutableStateOf(NO_INDEX) }
                WritingTimeChartBody(
                    shares = uiState.shares,
                    chosenIndex = chosenIndex,
                    palette = palette,
                    labelFontSize = labelFontSize,
                    textColor = textColor,
                    onChoose = { index -> chosenIndex = index },
                )
            }
        }
    }
}

/**
 * Puts the ring and the rows in the room the chart was given: under each other on a tall chart, beside each other
 * on a wide one, so a landscape phone keeps a ring large enough to read and still shows every hour of the day.
 */
@Composable
private fun WritingTimeChartBody(
    shares: List<WritingTimeShare>,
    chosenIndex: Int,
    palette: List<Color>,
    labelFontSize: Float,
    textColor: Color,
    onChoose: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > maxHeight) {
            val ringSize = minOf(maxHeight, maxWidth * WIDE_RING_WIDTH_FRACTION, RING_MAX_SIZE)
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(ringSize).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    WritingTimeRing(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        ringSize = ringSize,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                    )
                }
                Spacer(modifier = Modifier.width(SECTION_GAP))
                WritingTimeLegend(
                    shares = shares,
                    chosenIndex = chosenIndex,
                    palette = palette,
                    labelFontSize = labelFontSize,
                    textColor = textColor,
                    onChoose = onChoose,
                    modifier = Modifier.weight(1F).fillMaxHeight(),
                )
            }
        } else {
            val ringSize = minOf(maxWidth, maxHeight * TALL_RING_HEIGHT_FRACTION, RING_MAX_SIZE)
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(ringSize), contentAlignment = Alignment.Center) {
                    WritingTimeRing(
                        shares = shares,
                        chosenIndex = chosenIndex,
                        palette = palette,
                        ringSize = ringSize,
                        labelFontSize = labelFontSize,
                        textColor = textColor,
                        onChoose = onChoose,
                    )
                }
                Spacer(modifier = Modifier.height(SECTION_GAP))
                // The eight rows are taller than what is left under the ring in a dashboard card or at a large
                // font size, so the rows scroll instead of losing the hours below the card's edge.
                WritingTimeLegend(
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

/**
 * The ring: one slice per span of the day, its angle the span's share of the entries, its color the span's color in
 * the rows.
 *
 * The slices grow out of nothing when the chart is first drawn, a thin gap keeps two neighbours apart, and the
 * slice of the chosen span is drawn last and a little wider while the others fade, so it stands out of the ring
 * without the ring losing its shape. A tap on a slice chooses it, and a tap inside the ring or on the chosen slice
 * again goes back to the whole day. A span no entry falls in has no slice to choose.
 */
@Composable
private fun WritingTimeRing(
    shares: List<WritingTimeShare>,
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
        label = "writingTimeRingSweep",
    )
    val dimProgress by animateFloatAsState(
        targetValue = if (chosenIndex >= 0) 1F else 0F,
        animationSpec = tween(durationMillis = CHOICE_ANIMATION_MILLIS),
        label = "writingTimeRingDim",
    )
    val strokeWidth = ringSize * RING_STROKE_FRACTION
    val chosenShare = shares.getOrNull(chosenIndex)
    val drawnSlices = shares.count { share -> share.diaryCount > 0 }

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
                        // Inside the ring there is no slice, so a tap there is how a writer lets the chosen span go
                        // and reads the whole day again.
                        val index =
                            if (distance < holePx) {
                                NO_INDEX
                            } else {
                                shares.indexAt(clockwiseAngleOf(fromCenter)).takeIf { hit -> (shares.getOrNull(hit)?.diaryCount ?: 0) > 0 } ?: NO_INDEX
                            }
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
            // Every slice keeps the angle the clock gives it, counted from one o'clock. The drawing order must not
            // move a slice: sorting the chosen one to the end and then piling the sweeps up in that order pushed it
            // to the last angle and pulled the slices behind it forward, so the ring no longer matched the rows and
            // a tap landed on a slice other than the one under the finger (issue #291).
            val sliceStarts = FloatArray(sweeps.size)
            var startAngle = RING_START_ANGLE
            sweeps.forEachIndexed { index, sweep ->
                sliceStarts[index] = startAngle
                startAngle += sweep
            }

            // The neighbours of the chosen slice are drawn first and faded, each one at its own angle.
            sweeps.forEachIndexed { index, sweep ->
                if (index != chosenIndex) {
                    val gap = sliceGap(drawnSlices, sweep)
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

            // The chosen slice is painted last and once, a little wider and centred on the ring's line, which reads
            // as the slice lifting out of the ring as the others fade. It stays at the angle the clock gave it, and
            // while the choice animates in (dimProgress at 0) it is still drawn at the ring's own width, so it never
            // disappears for a frame.
            if (chosenIndex in sweeps.indices) {
                val sweep = sweeps[chosenIndex]
                val gap = sliceGap(drawnSlices, sweep)
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

        WritingTimeRingCenter(
            chosenShare = chosenShare,
            totalCount = shares.sumOf { share -> share.diaryCount },
            innerDiameter = ringSize - strokeWidth * 2F,
            labelFontSize = labelFontSize,
            textColor = textColor,
        )
    }
}

/**
 * What the middle of the ring states: the whole day while no span is chosen, and the chosen span's hours, count and
 * percent once one is. The text is sized from the hole rather than from the screen, so it stays inside the ring on
 * a small card and at a large font size.
 */
@Composable
private fun WritingTimeRingCenter(
    chosenShare: WritingTimeShare?,
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
                text = rangeLabel(chosenShare.range),
                fontSize = nameFontSize,
                fontWeight = FontWeight.SemiBold,
                fontColor = textColor,
                textAlign = TextAlign.Center,
                maxLines = CENTER_NAME_MAX_LINES,
            )
            SimpleText(
                text = chosenShare.diaryCount.toString(),
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
 * The rows of the legend: the span's color, its hours, its count and its share of the entries, always in the order
 * of the clock.
 *
 * A row is a tap target of its own, so a span can be held out from the list as well as from the ring, and the
 * chosen row is tinted with its own color so a reader can follow one span from the ring to its line and back. A
 * span no entry falls in stays in the list as a dimmed row of zero that cannot be chosen, which keeps the day
 * whole and tells the writer where nothing has been written yet.
 */
@Composable
private fun WritingTimeLegend(
    shares: List<WritingTimeShare>,
    chosenIndex: Int,
    palette: List<Color>,
    labelFontSize: Float,
    textColor: Color,
    onChoose: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        shares.forEachIndexed { index, share ->
            WritingTimeLegendRow(
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
private fun WritingTimeLegendRow(
    share: WritingTimeShare,
    color: Color,
    isChosen: Boolean,
    labelFontSize: Float,
    textColor: Color,
    onClick: () -> Unit,
) {
    val isEmpty = share.diaryCount == 0
    val contentAlpha = if (isEmpty) EMPTY_ROW_ALPHA else 1F
    val shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (isChosen) color.copy(alpha = CHOSEN_ROW_ALPHA) else Color.Transparent)
                .clickable(enabled = !isEmpty, onClick = onClick)
                .padding(horizontal = ROW_HORIZONTAL_PADDING, vertical = ROW_VERTICAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(INDICATOR_SIZE).clip(CircleShape).background(color.copy(alpha = contentAlpha)))
        Spacer(modifier = Modifier.width(INDICATOR_GAP))
        SimpleText(
            text = rangeLabel(share.range),
            modifier = Modifier.weight(1F),
            fontSize = labelFontSize,
            fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
            fontColor = textColor,
            alpha = contentAlpha,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.width(COUNT_GAP))
        SimpleText(
            text = share.diaryCount.toString(),
            modifier = Modifier.widthIn(min = COUNT_MIN_WIDTH),
            fontSize = labelFontSize * COUNT_FONT_SCALE,
            fontWeight = FontWeight.SemiBold,
            fontColor = textColor,
            alpha = contentAlpha,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
        Spacer(modifier = Modifier.width(PERCENT_GAP))
        SimpleText(
            text = percentText(share.percent),
            modifier = Modifier.widthIn(min = PERCENT_MIN_WIDTH),
            fontSize = labelFontSize * PERCENT_FONT_SCALE,
            fontColor = textColor,
            alpha = if (isChosen) 1F else contentAlpha * PERCENT_ALPHA,
            maxLines = 1,
            textAlign = TextAlign.End,
        )
    }
}

/** The label of a span of the day, which is the hours it holds, translated by the resources of the app. */
@Composable
private fun rangeLabel(range: WritingTimeRange): String =
    stringResource(
        when (range) {
            WritingTimeRange.HOUR_1_TO_3 -> R.string.range_a
            WritingTimeRange.HOUR_4_TO_6 -> R.string.range_b
            WritingTimeRange.HOUR_7_TO_9 -> R.string.range_c
            WritingTimeRange.HOUR_10_TO_12 -> R.string.range_d
            WritingTimeRange.HOUR_13_TO_15 -> R.string.range_e
            WritingTimeRange.HOUR_16_TO_18 -> R.string.range_f
            WritingTimeRange.HOUR_19_TO_21 -> R.string.range_g
            WritingTimeRange.HOUR_22_TO_0 -> R.string.range_h
        },
    )

/** The gap kept between two drawn slices: thin, and never so wide that a small slice disappears. */
private fun sliceGap(
    drawnSlices: Int,
    sweep: Float,
): Float = if (drawnSlices > 1) minOf(RING_GAP_DEGREES, sweep * MAX_GAP_SHARE_OF_SWEEP) else 0F

/** The share of a row as it is written beside its count. */
private fun percentText(percent: Int): String = "$percent%"

/** The slice a tap at [angle] degrees clockwise from the top of the ring falls in. */
private fun List<WritingTimeShare>.indexAt(angle: Float): Int {
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
 * The eight colors the slices and the rows are painted with.
 *
 * The first color is the span from one o'clock, and the order keeps hues that look alike apart, so two
 * neighbouring slices of a small ring are still told apart. The palette is not the theme's primary color spread
 * over eight tones, because eight spans cannot be told apart with eight tones of one color: it is a set of eight
 * hues, and its dark-card variant brightens them instead of leaving the darker ones lost on the grey card.
 */
private fun writingTimePalette(cardColor: Color): List<Color> {
    val brightening = if (cardColor.luminance() < DARK_CARD_LUMINANCE) DARK_CARD_BRIGHTENING else 0F
    return WRITING_TIME_PALETTE.map { color -> if (brightening > 0F) lerp(color, Color.White, brightening) else color }
}

private val WRITING_TIME_PALETTE =
    listOf(
        Color(0xFF4E79A7), // blue
        Color(0xFFF28E2B), // orange
        Color(0xFF59A14F), // green
        Color(0xFFD37295), // rose
        Color(0xFF76B7B2), // teal
        Color(0xFFB07AA1), // purple
        Color(0xFFD9A62E), // gold
        Color(0xFFE15759), // red
    )

/** The index a tap or a row gives back when nothing is chosen, or when the chosen span is chosen again. */
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
private const val WIDE_RING_WIDTH_FRACTION = 0.40F
private const val TALL_RING_HEIGHT_FRACTION = 0.46F
private const val CENTER_WIDTH_FRACTION = 0.86F
private const val CENTER_VALUE_FONT_SCALE = 1.9F
private const val CENTER_VALUE_MAX_SHARE = 0.30F
private const val CENTER_NAME_FONT_SCALE = 0.95F
private const val CENTER_NAME_MAX_SHARE = 0.13F
private const val CENTER_CAPTION_FONT_SCALE = 0.8F
private const val CENTER_CAPTION_MAX_SHARE = 0.11F
private const val CENTER_NAME_MAX_LINES = 1
private const val CENTER_CAPTION_ALPHA = 0.7F
private const val CHOSEN_ROW_ALPHA = 0.16F
private const val EMPTY_ROW_ALPHA = 0.45F
private const val PERCENT_ALPHA = 0.7F
private const val COUNT_FONT_SCALE = 0.95F
private const val PERCENT_FONT_SCALE = 0.85F
private const val EMPTY_STATE_ALPHA = 0.7F
private val RING_MAX_SIZE = 220.dp
private val CHART_HORIZONTAL_PADDING = 16.dp
private val CHART_VERTICAL_PADDING = 16.dp
private val SECTION_GAP = 12.dp
private val PROGRESS_SIZE = 28.dp
private val INDICATOR_SIZE = 10.dp
private val INDICATOR_GAP = 10.dp
private val COUNT_GAP = 8.dp
private val PERCENT_GAP = 8.dp
private val COUNT_MIN_WIDTH = 28.dp
private val PERCENT_MIN_WIDTH = 40.dp
private val ROW_HORIZONTAL_PADDING = 6.dp
private val ROW_VERTICAL_PADDING = 4.dp

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, heightDp = 460)
@Composable
private fun WritingTimeDonutChartPreview() {
    AppTheme {
        WritingTimeDonutChart(
            uiState =
                WritingTimeChartUiState(
                    shares = WritingTimeStatistics.shares(listOf(1, 2, 3, 8, 8, 8, 8, 14, 20, 20, 20, 20, 20, 23, 0)),
                    isLoading = false,
                ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 640, heightDp = 300)
@Composable
private fun WritingTimeDonutChartWidePreview() {
    AppTheme {
        WritingTimeDonutChart(
            uiState = WritingTimeChartUiState(shares = WritingTimeStatistics.shares(List(40) { 21 }), isLoading = false),
        )
    }
}
