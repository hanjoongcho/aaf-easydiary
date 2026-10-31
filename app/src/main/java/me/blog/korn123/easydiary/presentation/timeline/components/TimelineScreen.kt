package me.blog.korn123.easydiary.presentation.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.components.LoadingScreen
import me.blog.korn123.easydiary.utils.FontUtils

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
    modifier: Modifier = Modifier,
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
        modifier = modifier,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        containerColor = Color(context.config.screenBackgroundColor),
        topBar = {
            EasyDiaryActionBar(
                title = stringResource(id = R.string.timeline_title),
                actions = {
                    IconButton(onClick = onToggleFilter) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search_10),
                            contentDescription = stringResource(id = R.string.filter_search_word),
                            tint = Color.White,
                        )
                    }
                },
                close = onBackClick,
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = onWriteDiaryClick,
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_outline_list_alt_add),
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
                            key = { _, diary -> diary.diaryId },
                        ) { index, diary ->
                            val isFirstInGroup = index == 0 || (diary.dateString != diaryList[index - 1].dateString)
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
