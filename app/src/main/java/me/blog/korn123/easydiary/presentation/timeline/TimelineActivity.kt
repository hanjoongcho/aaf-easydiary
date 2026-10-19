package me.blog.korn123.easydiary.presentation.timeline

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.databinding.ItemTimelineBinding
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.findActivity
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.PREVIOUS_ACTIVITY_CREATE
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.base.EasyDiaryComposeBaseActivity
import me.blog.korn123.easydiary.ui.components.LoadingScreen
import me.blog.korn123.easydiary.ui.theme.AppTheme
import java.text.SimpleDateFormat
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by hanjoong on 2017-07-16.
 * Migrated to Jetpack Compose.
 */
@AndroidEntryPoint
class TimelineActivity : EasyDiaryComposeBaseActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private val timelineViewModel: TimelineViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timelineViewModel.restoreState(savedInstanceState)

        setContent {
            AppTheme {
                val startDateText by timelineViewModel.startDateText.collectAsStateWithLifecycle()
                val endDateText by timelineViewModel.endDateText.collectAsStateWithLifecycle()
                val queryText by timelineViewModel.queryText.collectAsStateWithLifecycle()
                val symbolSequence by timelineViewModel.symbolSequence.collectAsStateWithLifecycle()
                val isFilterVisible by timelineViewModel.isFilterVisible.collectAsStateWithLifecycle()
                val diaryList by timelineViewModel.diaryList.collectAsStateWithLifecycle()
                val isLoading by timelineViewModel.isLoading.collectAsStateWithLifecycle()

                TimelineScreen(
                    diaryList = diaryList,
                    isContainerLoading = timelineViewModel.isContainerLoading,
                    isLoading = isLoading,
                    loadingMessage = diaryViewModel.loadingMessage ?: "Initializing...",
                    isFilterVisible = isFilterVisible,
                    startDateText = startDateText,
                    endDateText = endDateText,
                    queryText = queryText,
                    symbolSequence = symbolSequence,
                    onToggleFilter = { timelineViewModel.toggleFilterVisible() },
                    onCloseFilter = { timelineViewModel.closeFilter() },
                    onStartDateClick = { showStartDatePicker() },
                    onEndDateClick = { showEndDatePicker() },
                    onQueryChange = { query -> timelineViewModel.setQueryText(query) },
                    onSymbolClick = { showSymbolDialog() },
                    onClearFilter = { timelineViewModel.clearFilter() },
                    onDiaryClick = { diary -> openDiaryReading(diary) },
                    onWriteDiaryClick = { openDiaryWriting() },
                    onBackClick = { finishActivityWithPauseLock() },
                )
            }
        }

        if (savedInstanceState == null) {
            timelineViewModel.startRefreshJob()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        timelineViewModel.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (config.previousActivity == PREVIOUS_ACTIVITY_CREATE) {
            timelineViewModel.startRefreshJob()
            config.previousActivity = -1
        } else {
            timelineViewModel.startRefreshJob()
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun showStartDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                timelineViewModel.setStartDate(year, month, dayOfMonth)
            },
            timelineViewModel.startYear,
            timelineViewModel.startMonth,
            timelineViewModel.startDay,
        ).show()
    }

    private fun showEndDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                timelineViewModel.setEndDate(year, month, dayOfMonth)
            },
            timelineViewModel.endYear,
            timelineViewModel.endMonth,
            timelineViewModel.endDay,
        ).show()
    }

    private fun showSymbolDialog() {
        lifecycleScope.launch {
            openFeelingSymbolDialog(
                getString(R.string.diary_symbol_search_message),
                selectedSymbolSequence = 0,
                diaryViewModel.getSymbolUsedCountMap(true),
            ) { symbolSequence ->
                timelineViewModel.setSymbolSequence(if (symbolSequence == 0) SYMBOL_SELECT_ALL else symbolSequence)
            }
        }
    }

    private fun openDiaryReading(diary: DiaryDomain) {
        val detailIntent =
            Intent(this, DiaryReadingActivity::class.java).apply {
                putExtra(DIARY_SEQUENCE, diary.diaryId)
                putExtra(SELECTED_SEARCH_QUERY, timelineViewModel.queryText.value)
                putExtra(SELECTED_SYMBOL_SEQUENCE, timelineViewModel.symbolSequence.value)
            }
        TransitionHelper.startActivityWithTransition(this, detailIntent)
    }

    private fun openDiaryWriting() {
        val createDiary = Intent(this, DiaryWritingActivity::class.java)
        TransitionHelper.startActivityWithTransition(this, createDiary)
    }
}

/***************************************************************************************************
 *   Compose Screen & Components
 *
 ***************************************************************************************************/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    diaryList: List<DiaryDomain>,
    isContainerLoading: Boolean,
    isLoading: Boolean,
    loadingMessage: String,
    isFilterVisible: Boolean,
    startDateText: String,
    endDateText: String,
    queryText: String,
    symbolSequence: Int,
    onToggleFilter: () -> Unit,
    onCloseFilter: () -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSymbolClick: () -> Unit,
    onClearFilter: () -> Unit,
    onDiaryClick: (DiaryDomain) -> Unit,
    onWriteDiaryClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val primaryColor = Color(context.config.primaryColor)
    val listState = rememberLazyListState()

    LaunchedEffect(isContainerLoading) {
        if (diaryList.isNotEmpty()) {
            listState.scrollToItem(diaryList.size - 1)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        containerColor = Color(context.config.screenBackgroundColor),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.timeline_title),
                        style =
                            TextStyle(
                                fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(context),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFilter) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search_10),
                            contentDescription = "Search Filter",
                            tint = Color.White,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryColor,
                        scrolledContainerColor = primaryColor,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onWriteDiaryClick,
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = stringResource(id = R.string.write_diary),
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isFilterVisible,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut(),
                ) {
                    TimelineFilterPanel(
                        startDateText = startDateText,
                        endDateText = endDateText,
                        queryText = queryText,
                        symbolSequence = symbolSequence,
                        onStartDateClick = onStartDateClick,
                        onEndDateClick = onEndDateClick,
                        onQueryChange = onQueryChange,
                        onSymbolClick = onSymbolClick,
                        onClearFilter = onClearFilter,
                        onCloseFilter = onCloseFilter,
                    )
                }

                if (diaryList.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "There is no written diary.",
                            style =
                                TextStyle(
                                    fontFamily = if (LocalInspectionMode.current) null else FontUtils.getComposeFontFamily(context),
                                    color = Color(context.config.textColor),
                                    fontSize = 16.sp,
                                ),
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        itemsIndexed(
                            items = diaryList,
                            key = { _, item -> item.diaryId },
                        ) { index, diary ->
                            val isFirstInGroup = index == 0 || diary.dateString != diaryList[index - 1].dateString
                            TimelineItemCard(
                                diary = diary,
                                isFirstInGroup = isFirstInGroup,
                                currentQuery = queryText,
                                onDiaryClick = onDiaryClick,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isLoading || isContainerLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LoadingScreen(message = loadingMessage)
            }
        }
    }
}

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
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Start Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.filter_start_date),
                    style = TextStyle(fontWeight = FontWeight.Bold, color = textColor),
                    modifier = Modifier.weight(3f),
                )
                Row(
                    modifier =
                        Modifier
                            .weight(7f)
                            .clickable { onStartDateClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = startDateText.ifEmpty { stringResource(id = R.string.filter_start_date_hint) },
                        style = TextStyle(color = if (startDateText.isEmpty()) Color.Gray else textColor),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onStartDateClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_4_w),
                            contentDescription = "Start Date",
                            tint = primaryColor,
                        )
                    }
                }
            }

            // End Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.filter_end_date),
                    style = TextStyle(fontWeight = FontWeight.Bold, color = textColor),
                    modifier = Modifier.weight(3f),
                )
                Row(
                    modifier =
                        Modifier
                            .weight(7f)
                            .clickable { onEndDateClick() },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = endDateText.ifEmpty { stringResource(id = R.string.filter_end_date_hint) },
                        style = TextStyle(color = if (endDateText.isEmpty()) Color.Gray else textColor),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onEndDateClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_4_w),
                            contentDescription = "End Date",
                            tint = primaryColor,
                        )
                    }
                }
            }

            // Search Word Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.filter_search_word),
                    style = TextStyle(fontWeight = FontWeight.Bold, color = textColor),
                    modifier = Modifier.weight(3f),
                )
                OutlinedTextField(
                    value = queryText,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(7f),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.filter_search_word_hint),
                            style = TextStyle(color = Color.Gray, fontSize = 14.sp),
                        )
                    },
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray,
                        ),
                )
            }

            // Diary Symbol Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.diary_symbol),
                    style = TextStyle(fontWeight = FontWeight.Bold, color = textColor),
                    modifier = Modifier.weight(3f),
                )
                Row(
                    modifier =
                        Modifier
                            .weight(7f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(46.dp)
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
                                    .size(32.dp),
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
                        style = TextStyle(color = textColor, fontSize = 12.sp),
                    )
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onClearFilter,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(text = stringResource(id = R.string.filter_clear), color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onCloseFilter,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(text = stringResource(id = R.string.filter_close), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TimelineItemCard(
    diary: DiaryDomain,
    isFirstInGroup: Boolean,
    currentQuery: String?,
    onDiaryClick: (DiaryDomain) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            ItemTimelineBinding.inflate(LayoutInflater.from(ctx)).root
        },
        update = { view ->
            val binding = ItemTimelineBinding.bind(view)
            val ctx = view.context
            val activity = ctx.findActivity()

            if (isFirstInGroup) {
                binding.title.text = DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)
                binding.titleContainer.visibility = View.VISIBLE
                binding.topLine.visibility = View.VISIBLE
            } else {
                binding.titleContainer.visibility = View.GONE
                binding.topLine.visibility = View.GONE
            }

            FlavorUtils.initWeatherView(ctx, binding.diarySymbol, diary.symbolSequence, false)

            val lineBreakStrings = arrayListOf<String>()
            when (diary.isAllDay) {
                true -> {
                    lineBreakStrings.add(
                        if (ctx.config.enableDebugOptionVisibleDiarySequence) {
                            "[${diary.originDiaryId}] ${ctx.resources.getString(R.string.all_day)}"
                        } else {
                            ctx.resources.getString(R.string.all_day)
                        },
                    )
                }

                false -> {
                    lineBreakStrings.add(
                        if (ctx.config.enableDebugOptionVisibleDiarySequence) {
                            "[${diary.originDiaryId}] ${DateUtils.getTimeStringFromTimeMillis(diary.currentTimeMillis, SimpleDateFormat.MEDIUM)}"
                        } else {
                            DateUtils.getTimeStringFromTimeMillis(diary.currentTimeMillis, SimpleDateFormat.MEDIUM)
                        },
                    )
                }
            }
            if (diary.title?.isNotEmpty() == true) lineBreakStrings.add(diary.title)

            activity?.applyMarkDownPolicy(binding.text1, diary.contents, true, lineBreakStrings, true)

            ctx.updateTextColors(binding.itemHolder, 0, 0)
            ctx.updateAppViews(binding.itemHolder)
            ctx.updateCardViewPolicy(binding.itemHolder)
            ctx.initTextSize(binding.itemHolder)
            FontUtils.setFontsTypeface(ctx, null, binding.root)

            if (!currentQuery.isNullOrEmpty()) {
                if (ctx.config.diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(binding.text1, currentQuery)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(binding.text1, currentQuery)
                }
            }

            if (ctx.config.enableContentsSummary) {
                binding.text1.maxLines = ctx.config.summaryMaxLines.plus(1)
                binding.text1.ellipsize = TextUtils.TruncateAt.valueOf("END")
            } else {
                binding.text1.maxLines = Integer.MAX_VALUE
                binding.text1.ellipsize = null
            }

            if (diary.currentTimeMillis > System.currentTimeMillis()) {
                binding.textDDayCount.visibility = View.VISIBLE
                binding.textDDayCount.text = "（${DateUtils.getOnlyDayRemaining(diary.currentTimeMillis)}）"
            } else {
                binding.textDDayCount.visibility = View.GONE
            }

            binding.root.setOnClickListener { onDiaryClick(diary) }
        },
    )
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
