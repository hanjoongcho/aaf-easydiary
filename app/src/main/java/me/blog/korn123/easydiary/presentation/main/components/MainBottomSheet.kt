package me.blog.korn123.easydiary.presentation.main.components

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.calendar.CalendarActivity
import me.blog.korn123.easydiary.presentation.components.BottomToolBarContainer
import me.blog.korn123.easydiary.presentation.components.CustomCircleIconButton
import me.blog.korn123.easydiary.presentation.components.CustomElevatedSquareButton
import me.blog.korn123.easydiary.presentation.components.SymbolIconButton
import me.blog.korn123.easydiary.presentation.dev.DevActivity
import me.blog.korn123.easydiary.presentation.settings.QuickSettingsActivity
import me.blog.korn123.easydiary.presentation.timeline.TimelineActivity
import me.blog.korn123.easydiary.presentation.tree.TreeTimelineActivity
import me.blog.korn123.easydiary.utils.FontUtils

@Composable
fun MainBottomSheet(
    title: String,
    currentQuery: String,
    symbolSequence: Int = SYMBOL_SELECT_ALL,
    enableCardViewPolicy: Boolean = LocalContext.current.config.enableCardViewPolicy,
    fontSize: Float = LocalContext.current.config.settingFontSize,
    fontColor: Color = Color(LocalContext.current.config.textColor),
    alpha: Float = 1.0f,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily? =
        if (LocalInspectionMode.current) {
            null
        } else {
            FontUtils.getComposeFontFamily(
                LocalContext.current,
            )
        },
    lineSpacingScaleFactor: Float = LocalContext.current.config.lineSpacingScaleFactor,
    onMicrophoneClick: () -> Unit = {},
    onMoveToday: () -> Unit = {},
    onFeelingSymbolClick: () -> Unit = {},
    clearFilter: () -> Unit = {},
    callbackQuery: (query: String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    onMoveTop: () -> Unit = {},
    onMoveBottom: () -> Unit = {},
    onHeightChanged: (Dp) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val density = LocalDensity.current
    var showMenuDialog by remember { mutableStateOf(false) }

    MainOptionDialog(
        showDialog = showMenuDialog,
        fontFamily = fontFamily,
        onDismiss = { showMenuDialog = false },
    )

    Column {
        var isFocused by remember { mutableStateOf(false) }
        val isHighlighted = isFocused || currentQuery.isNotEmpty()
        val contentColor = if (isHighlighted) Color.White else Color(context.config.textColor)
        val debugColor = Color.Red
        Box(
            modifier =
                Modifier
                    .imePadding()
                    .padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = RoundedCornerShape(15.dp),
                        clip = false,
                    ).background(
                        color =
                            if (isHighlighted) {
                                Color(context.config.primaryColor).copy(alpha = 0.9f)
                            } else {
                                Color(
                                    context.config.backgroundColor,
                                ).copy(alpha = 0.9f)
                            },
                        shape =
                            RoundedCornerShape(
                                ROUNDED_CORNER_SHAPE_SIZE.dp,
                                ROUNDED_CORNER_SHAPE_SIZE.dp,
                                0.dp,
                                0.dp,
                            ),
                    ).border(
                        1.dp,
                        Color(
                            context.config.primaryColor,
                        ),
                        shape = RoundedCornerShape(ROUNDED_CORNER_SHAPE_SIZE.dp),
                    ).onGloballyPositioned { coordinates ->
                        val heightDp = with(density) { coordinates.size.height.toDp() }
                        onHeightChanged(heightDp)
                    },
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(0.dp, 10.dp, 0.dp, 5.dp)
                        .navigationBarsPadding(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 5.dp, 20.dp, 1.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SymbolIconButton(
                        symbolSequence = symbolSequence,
                        onClick = { onFeelingSymbolClick.invoke() },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    CustomCircleIconButton(
                        iconResourceId = R.drawable.ic_move_top,
                        contentDescription = "Move top",
                        iconPadding = 1.dp,
                        elevation = 3.dp,
                        onClick = { onMoveTop() },
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    CustomCircleIconButton(
                        iconResourceId = R.drawable.ic_move_bottom,
                        contentDescription = "Move bottom",
                        iconPadding = 1.dp,
                        elevation = 3.dp,
                        onClick = { onMoveBottom() },
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    CustomCircleIconButton(
                        iconResourceId = R.drawable.ic_outline_mic,
                        elevation = 3.dp,
                        contentDescription = "Speech input",
                        onClick = { onMicrophoneClick() },
                    )
                }

                MainSearchInput(
                    title = title,
                    currentQuery = currentQuery,
                    symbolSequence = symbolSequence,
                    fontSize = fontSize,
                    fontColor = contentColor,
                    fontWeight = fontWeight,
                    fontFamily = fontFamily,
                    onFocusChanged = { isFocused = it },
                    callbackQuery = callbackQuery,
                    clearFilter = clearFilter,
                )
                BottomToolBarContainer(
                    isAutoPadding = false,
                ) {
                    CustomElevatedSquareButton(
                        text = activity?.getString(R.string.button_new_entry) ?: "",
                        iconResourceId = R.drawable.ic_outline_list_alt_add,
                        iconSize = 18.dp,
                    ) {
                        activity?.let {
                            val createDiary = Intent(it, DiaryWritingActivity::class.java)
                            TransitionHelper.startActivityWithTransition(
                                it,
                                createDiary,
                            )
                        }
                    }
                    CustomElevatedSquareButton(
                        text = "TODAY",
                        iconResourceId = R.drawable.ic_outline_today,
                        iconSize = 18.dp,
                    ) {
                        onMoveToday()
                    }
                    CustomElevatedSquareButton(
                        text = activity?.getString(R.string.button_tree_view) ?: "",
                        iconResourceId = R.drawable.ic_outline_tree,
                        iconSize = 18.dp,
                    ) {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, TreeTimelineActivity::class.java),
                            )
                        }
                    }
                    CustomElevatedSquareButton(
                        text = activity?.getString(R.string.menu_timeline) ?: "",
                        iconResourceId = R.drawable.ic_outline_view_timeline,
                        iconSize = 18.dp,
                    ) {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, TimelineActivity::class.java),
                            )
                        }
                    }

                    if (context.config.enableDebugMode) {
                        CustomElevatedSquareButton(
                            iconResourceId = R.drawable.ic_bug_2,
                            text = "Debug",
                            iconSize = 18.dp,
                            tint = debugColor,
                        ) {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, DevActivity::class.java),
                                )
                            }
                        }
                        CustomElevatedSquareButton(
                            text = activity?.getString(R.string.button_quick_settings) ?: "",
                            iconResourceId = R.drawable.ic_running,
                            iconSize = 18.dp,
                            tint = debugColor,
                        ) {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, QuickSettingsActivity::class.java),
                                )
                            }
                        }
                        CustomElevatedSquareButton(
                            text = "Calendar",
                            iconResourceId = R.drawable.ic_outline_calendar_month,
                            iconSize = 18.dp,
                            tint = debugColor,
                        ) {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, CalendarActivity::class.java),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
