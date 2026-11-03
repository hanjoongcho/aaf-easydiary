package me.blog.korn123.easydiary.presentation.timeline.components

import android.content.res.Configuration
import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.SimpleText
import me.blog.korn123.easydiary.presentation.theme.AppTheme

@Composable
fun TimelineFilterPanel(
    startDateText: String,
    endDateText: String,
    queryText: String,
    symbolSequence: Int,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSymbolClick: () -> Unit,
    onClearFilter: () -> Unit,
    onCloseFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val primaryColor = Color(context.config.primaryColor)
    val textColor = Color(context.config.textColor)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(0.dp),
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, primaryColor),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isLandscape) {
                // Landscape: Line 1: Start Date & End Date in 1 line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SimpleText(
                            text = stringResource(id = R.string.filter_start_date),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(3f),
                        )
                        StartDateInput(
                            startDateText = startDateText,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            onStartDateClick = onStartDateClick,
                            modifier = Modifier.weight(7f),
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SimpleText(
                            text = stringResource(id = R.string.filter_end_date),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(3f),
                        )
                        EndDateInput(
                            endDateText = endDateText,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            onEndDateClick = onEndDateClick,
                            modifier = Modifier.weight(7f),
                        )
                    }
                }

                // Landscape: Line 2: Search Word & Diary Symbol in 1 line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SimpleText(
                            text = stringResource(id = R.string.filter_search_word),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(3f),
                        )
                        SearchQueryInput(
                            queryText = queryText,
                            primaryColor = primaryColor,
                            textColor = textColor,
                            onQueryChange = onQueryChange,
                            modifier = Modifier.weight(7f),
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SimpleText(
                            text = stringResource(id = R.string.diary_symbol),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(3f),
                        )
                        DiarySymbolInput(
                            symbolSequence = symbolSequence,
                            textColor = textColor,
                            onSymbolClick = onSymbolClick,
                            modifier = Modifier.weight(7f),
                        )
                    }
                }
            } else {
                // Portrait: Keep current state (each item on its own row)
                // Start Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimpleText(
                        text = stringResource(id = R.string.filter_start_date),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                    )
                    StartDateInput(
                        startDateText = startDateText,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onStartDateClick = onStartDateClick,
                        modifier = Modifier.weight(7f),
                    )
                }

                // End Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimpleText(
                        text = stringResource(id = R.string.filter_end_date),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                    )
                    EndDateInput(
                        endDateText = endDateText,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onEndDateClick = onEndDateClick,
                        modifier = Modifier.weight(7f),
                    )
                }

                // Search Word Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimpleText(
                        text = stringResource(id = R.string.filter_search_word),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                    )
                    SearchQueryInput(
                        queryText = queryText,
                        primaryColor = primaryColor,
                        textColor = textColor,
                        onQueryChange = onQueryChange,
                        modifier = Modifier.weight(7f),
                    )
                }

                // Diary Symbol Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SimpleText(
                        text = stringResource(id = R.string.diary_symbol),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(3f),
                    )
                    DiarySymbolInput(
                        symbolSequence = symbolSequence,
                        textColor = textColor,
                        onSymbolClick = onSymbolClick,
                        modifier = Modifier.weight(7f),
                    )
                }
            }

            // Action Buttons Row
            ActionButtons(
                primaryColor = primaryColor,
                onClearFilter = onClearFilter,
                onCloseFilter = onCloseFilter,
            )
        }
    }
}

@Composable
private fun StartDateInput(
    startDateText: String,
    primaryColor: Color,
    textColor: Color,
    onStartDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable { onStartDateClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SimpleText(
            text = startDateText.ifEmpty { stringResource(id = R.string.filter_start_date_hint) },
            fontColor = if (startDateText.isEmpty()) Color.Gray else textColor,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onStartDateClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_4_w),
                contentDescription = "Start Date",
                tint = primaryColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun EndDateInput(
    endDateText: String,
    primaryColor: Color,
    textColor: Color,
    onEndDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable { onEndDateClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SimpleText(
            text = endDateText.ifEmpty { stringResource(id = R.string.filter_end_date_hint) },
            fontColor = if (endDateText.isEmpty()) Color.Gray else textColor,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onEndDateClick,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_4_w),
                contentDescription = "End Date",
                tint = primaryColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SearchQueryInput(
    queryText: String,
    primaryColor: Color,
    textColor: Color,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = queryText,
        onValueChange = onQueryChange,
        modifier = modifier.height(36.dp),
        textStyle = TextStyle(color = textColor, fontSize = 13.sp),
        singleLine = true,
        cursorBrush = SolidColor(primaryColor),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = queryText,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
                placeholder = {
                    SimpleText(
                        text = stringResource(id = R.string.filter_search_word_hint),
                        fontColor = if (queryText.isEmpty()) Color.Gray else textColor,
                    )
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = remember { MutableInteractionSource() },
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.LightGray,
                            ),
                    )
                },
            )
        },
    )
}

@Composable
private fun DiarySymbolInput(
    symbolSequence: Int,
    textColor: Color,
    onSymbolClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = onSymbolClick,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                modifier =
                    Modifier
                        .size(28.dp),
                factory = { ctx ->
                    ImageView(ctx).apply {
                        isClickable = false
                        isFocusable = false
                    }
                },
                update = { imageView ->
                    FlavorUtils.initWeatherView(
                        imageView.context,
                        imageView,
                        symbolSequence,
                        false,
                    )
                },
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "▼",
            style = TextStyle(color = textColor, fontSize = 11.sp),
        )
    }
}

@Composable
private fun ActionButtons(
    primaryColor: Color,
    onClearFilter: () -> Unit,
    onCloseFilter: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onClearFilter,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(4.dp),
        ) {
            SimpleText(
                text = stringResource(id = R.string.filter_clear),
                fontColor = Color.White,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onCloseFilter,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(4.dp),
        ) {
            SimpleText(
                text = stringResource(id = R.string.filter_close),
                fontColor = Color.White,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineFilterPanelPreview() {
    AppTheme {
        TimelineFilterPanel(
            startDateText = "2023-11-20",
            endDateText = "2023-11-25",
            queryText = "Diary search",
            symbolSequence = 0,
            onStartDateClick = {},
            onEndDateClick = {},
            onQueryChange = {},
            onSymbolClick = {},
            onClearFilter = {},
            onCloseFilter = {},
        )
    }
}
