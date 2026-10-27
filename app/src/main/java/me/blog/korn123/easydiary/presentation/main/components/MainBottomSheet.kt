package me.blog.korn123.easydiary.presentation.main.components

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DashboardActivity
import me.blog.korn123.easydiary.activities.DevActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.activities.GalleryActivity
import me.blog.korn123.easydiary.activities.PostcardViewerActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.ComposeConstants.ROUNDED_CORNER_SHAPE_SIZE
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.calendar.CalendarActivity
import me.blog.korn123.easydiary.presentation.components.BottomToolBarContainer
import me.blog.korn123.easydiary.presentation.components.CustomElevatedSquareButton
import me.blog.korn123.easydiary.presentation.settings.QuickSettingsActivity
import me.blog.korn123.easydiary.presentation.timeline.TimelineActivity
import me.blog.korn123.easydiary.presentation.tree.TreeTimelineActivity
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils

private data class MenuOptionItem(
    val iconRes: Int,
    val titleRes: Int,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
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
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showMenuDialog by remember { mutableStateOf(false) }

    val menuOptionItems =
        remember {
            listOf(
                MenuOptionItem(
                    iconRes = R.drawable.ic_grid_three_up,
                    titleRes = R.string.postcard_grid_option_title,
                    onClick = {},
                ),
                MenuOptionItem(
                    iconRes = R.drawable.ic_picture_w,
                    titleRes = R.string.post_card,
                    onClick = {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, PostcardViewerActivity::class.java),
                            )
                        }
                    },
                ),
                MenuOptionItem(
                    iconRes = R.drawable.ic_stack_of_photos,
                    titleRes = R.string.attached_photo_gallery,
                    onClick = {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, GalleryActivity::class.java),
                            )
                        }
                    },
                ),
                MenuOptionItem(
                    iconRes = R.drawable.ic_combo,
                    titleRes = R.string.menu_chart,
                    onClick = {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, DashboardActivity::class.java),
                            )
                        }
                    },
                ),
                MenuOptionItem(
                    iconRes = R.drawable.ic_settings_7,
                    titleRes = R.string.settings,
                    onClick = {
                        activity?.let {
                            TransitionHelper.startActivityWithTransition(
                                it,
                                Intent(it, SettingsActivity::class.java),
                            )
                        }
                    },
                ),
            )
        }

    if (showMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { showMenuDialog = false },
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                        style =
                            TextStyle(
                                fontFamily = fontFamily,
                                color = Color(context.config.primaryColor),
                            ),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMenuDialog = false },
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel),
                        style =
                            TextStyle(
                                fontFamily = fontFamily,
                                color = Color(context.config.primaryColor),
                            ),
                    )
                }
            },
            containerColor = Color(context.config.backgroundColor),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    menuOptionItems.forEach { item ->
                        val itemTitle = stringResource(id = item.titleRes)
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        context.makeToast(itemTitle)
                                        item.onClick.invoke()
                                        showMenuDialog = false
                                    }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = itemTitle,
                                modifier = Modifier.size(24.dp),
                                tint = Color(context.config.primaryColor),
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = itemTitle,
                                style =
                                    TextStyle(
                                        fontFamily = fontFamily,
                                        color = Color(context.config.textColor),
                                    ),
                            )
                        }
                    }
                }
            },
        )
    }

    Column {
        var isFocused by remember { mutableStateOf(false) }
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
                            if (isFocused || currentQuery.isNotEmpty()) {
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
                    ),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(0.dp, 5.dp, 0.dp, 5.dp)
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
                    val symbolSize = 32.dp
                    IconButton(
                        onClick = {
                            onFeelingSymbolClick.invoke()
                        },
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                    ) {
                        if (symbolSequence < SYMBOL_USER_CUSTOM_START) {
                            val symbolResourceId = FlavorUtils.sequenceToSymbolResourceId(symbolSequence)
                            val symbolPainter = if (symbolResourceId > 0) painterResource(id = symbolResourceId) else painterResource(id = R.drawable.ic_select_symbol)
                            Image(
                                painter = symbolPainter,
                                contentDescription = "Symbol",
                                modifier = Modifier.size(symbolSize),
                            )
                        } else {
                            val targetIndex = symbolSequence.minus(SYMBOL_USER_CUSTOM_START)
                            val filePath =
                                if (context.config.customSymbolPaths.size > targetIndex) {
                                    context.config.customSymbolPaths[targetIndex].getFilePath()
                                } else {
                                    ""
                                }
                            val fullPath = EasyDiaryUtils.getApplicationDataDirectory(context) + filePath
                            val bitmap =
                                remember(symbolSequence, fullPath) {
                                    BitmapFactory.decodeFile(fullPath)
                                }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Custom Symbol",
                                    modifier = Modifier.size(symbolSize),
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_select_symbol),
                                    contentDescription = "Symbol Fallback",
                                    modifier = Modifier.size(symbolSize),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            onMicrophoneClick.invoke()
                        },
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_microphone),
                            contentDescription = "Speech input",
                            tint = Color(context.config.primaryColor),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, DashboardActivity::class.java),
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_statistics),
                            contentDescription = "Statistics",
                            tint = Color(context.config.primaryColor),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, CalendarActivity::class.java),
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_planner),
                            contentDescription = "Statistics",
                            tint = Color(context.config.primaryColor),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            showMenuDialog = true
                        },
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Statistics",
                            tint = Color(context.config.primaryColor),
                        )
                    }
                }

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var text by remember(currentQuery) { mutableStateOf(currentQuery) }
                    val density = LocalDensity.current
                    val textUnit =
                        with(density) {
                            val temp = fontSize.toDp()
                            temp.toSp()
                        }

                    val focusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val isKeyboardVisible = WindowInsets.isImeVisible

                    LaunchedEffect(isKeyboardVisible) {
                        if (!isKeyboardVisible) {
                            focusManager.clearFocus()
                        }
                    }

                    TextField(
                        value = text,
                        onValueChange = {
                            text = it
                            callbackQuery.invoke(text)
                        },
                        label = {
                            Text(
                                text = title,
                                style =
                                    TextStyle(
                                        fontFamily = fontFamily,
                                        fontWeight = fontWeight,
                                        color = if (isFocused || currentQuery.isNotEmpty()) Color.White else Color(context.config.textColor),
                                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                                    ),
                            )
                        },
                        colors =
                            TextFieldDefaults.colors(
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            ),
                        textStyle =
                            TextStyle(
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                color = if (isFocused || currentQuery.isNotEmpty()) Color.White else Color(context.config.textColor),
                                fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                            ),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                imeAction = ImeAction.Done,
                            ),
                        keyboardActions =
                            KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                onSearch = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                            ),
                        trailingIcon = {
                            if (text.isNotEmpty() || symbolSequence != SYMBOL_SELECT_ALL) {
                                IconButton(onClick = {
                                    text = ""
                                    clearFilter.invoke()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear filter",
                                        tint = if (isFocused || currentQuery.isNotEmpty()) Color.White else Color(context.config.textColor),
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(0.dp)
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                    )
                }
                BottomToolBarContainer(
                    isAutoPadding = false,
                ) {
                    CustomElevatedSquareButton(
                        text = activity?.getString(R.string.button_new_entry) ?: "",
                        iconResourceId = R.drawable.ic_edit,
                        iconSize = 16.dp,
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
                        iconResourceId = R.drawable.ic_time_8_w,
                        iconSize = 16.dp,
                    ) {
                        onMoveToday()
                    }
                    CustomElevatedSquareButton(
                        text = activity?.getString(R.string.button_tree_view) ?: "",
                        iconResourceId = R.drawable.ic_tree_structure,
                        iconSize = 16.dp,
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
                        iconResourceId = R.drawable.ic_settings_11,
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
                            text = activity?.getString(R.string.button_quick_settings) ?: "",
                            iconResourceId = R.drawable.ic_running,
                            iconSize = 16.dp,
                        ) {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, QuickSettingsActivity::class.java),
                                )
                            }
                        }
                        CustomElevatedSquareButton(
                            iconResourceId = R.drawable.ic_bug_2,
                            text = "Debug",
                            iconSize = 16.dp,
                        ) {
                            activity?.let {
                                TransitionHelper.startActivityWithTransition(
                                    it,
                                    Intent(it, DevActivity::class.java),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
