package me.blog.korn123.easydiary.presentation.dev.components

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixelFloatValue
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.navigationBarHeight
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.extensions.startReviewFlow
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.extensions.updateStatusBarAppearance
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP
import me.blog.korn123.easydiary.presentation.dev.BaseDevViewModel
import me.blog.korn123.easydiary.presentation.dev.Demo1Activity
import me.blog.korn123.easydiary.presentation.dev.SelfDevelopmentRepoActivity
import me.blog.korn123.easydiary.presentation.main.DiaryMainActivity as PresentationDiaryMainActivity
import me.blog.korn123.easydiary.presentation.settings.components.AlarmCard
import me.blog.korn123.easydiary.presentation.settings.components.CategoryTitleCard
import me.blog.korn123.easydiary.presentation.settings.components.ScrollableCard
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCard
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCardWithImage
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCard
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCardWithImage
import me.blog.korn123.easydiary.presentation.settings.components.SymbolCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.easydiary.utils.DateUtils
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun RefactoringBacklog(
    modifier: Modifier,
    maxItemsInEachRow: Int,
) {
    val currentContext = LocalContext.current
    val currentActivity = LocalActivity.current

    CategoryTitleCard(title = "Refactoring Backlog", marginTop = 0)
    FlowRow(
        maxItemsInEachRow = maxItemsInEachRow,
        modifier = Modifier,
    ) {
        SimpleCard(
            "Self Development",
            "Self Development Repository",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, SelfDevelopmentRepoActivity::class.java),
            )
        }
        SimpleCard(
            "Mig DiaryMain",
            "Migrated DiaryMain screen to Jetpack Compose",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, PresentationDiaryMainActivity::class.java),
            )
        }
    }
}

@Composable
fun Etc(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    lifecycleScope: LifecycleCoroutineScope,
    actionLogHandler: () -> Unit,
    pickMediaLauncher: () -> Unit,
) {
    val currentContext = LocalContext.current
    val currentActivity = LocalActivity.current
    CategoryTitleCard(title = "Etc.")
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            "StatusBar",
            "Dark Icons",
            modifier = modifier,
        ) { currentActivity?.updateStatusBarAppearance(android.graphics.Color.WHITE) }
        SimpleCard(
            "StatusBar",
            "Light Icons",
            modifier = modifier,
        ) { currentActivity?.updateStatusBarAppearance(android.graphics.Color.BLACK) }
        SimpleCard(
            "Check Build Info",
            "Device information checkable via android.os.Build package",
            modifier = modifier,
        ) {
            currentContext.showAlertDialog(
                """
                Manufacturer: ${Build.MANUFACTURER}
                Model: ${Build.MODEL}
                OS: ${Build.VERSION.RELEASE}
                SDK: ${Build.VERSION.SDK_INT}
                """.trimIndent(),
            )
        }
        SimpleCard(
            "Action Log",
            "Open dialog about action log",
            modifier = modifier,
        ) {
            actionLogHandler()
        }
        SimpleCard(
            "ReviewFlow",
            "Starts the review flow using com.google.android.play.core.review.ReviewManagerFactory.",
            modifier = modifier,
        ) { currentActivity?.startReviewFlow() }
        SimpleCard(
            "Reset Font Size",
            null,
            modifier = modifier,
        ) {
            currentContext.config.settingFontSize =
                currentContext.spToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())
            currentContext.makeToast(
                "DP:${
                    currentContext.dpToPixelFloatValue(
                        UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat(),
                    )
                } , SP:${
                    currentContext.spToPixelFloatValue(
                        UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat(),
                    )
                }",
            )
        }
        SimpleCard(
            "Check Force Release URL",
            "https://raw.githubusercontent.com/AAFactory/aafactory-commons/master/data/test.json",
            modifier = modifier,
        ) {
            lifecycleScope.launch(Dispatchers.IO) {
                val url =
                    URL("https://raw.githubusercontent.com/AAFactory/aafactory-commons/master/data/test.json")
                val httpConn = url.openConnection() as HttpURLConnection
                val responseCode = httpConn.responseCode

                withContext(Dispatchers.Main) {
                    currentContext.makeToast("Response Code: $responseCode")
                }
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val lines = httpConn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readLines() }
                    withContext(Dispatchers.Main) {
                        currentContext.makeToast(lines[0])
                    }
                }
                httpConn.disconnect()
            }
        }
        SimpleCard(
            "InApp Browser",
            "CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build().run {\n" +
                "                        launchUrl(\n" +
                "                            this@BaseDevActivity,\n" +
                "                            Uri.parse(\"https://aafactory.github.io/\"),\n" +
                "                        )           \n" +
                "                    }",
            modifier = modifier,
        ) {
            CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build().run {
                launchUrl(
                    currentContext,
                    "https://aafactory.github.io/".toUri(),
                )
            }
        }
        SimpleCard(
            "PickMultipleVisualMedia",
            null,
            modifier = modifier,
        ) {
            pickMediaLauncher()
        }
        SimpleCard(
            "Check Next Alarm",
            null,
            modifier = modifier,
        ) {
            val alarmManager = currentContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.run {
                val alarmInfo = nextAlarmClock
                alarmInfo?.let {
                    currentContext.makeToast(
                        "Next: ${
                            DateUtils.getDateTimeStringFromTimeMillis(
                                alarmInfo.triggerTime,
                            )
                        }",
                    )
                } ?: currentContext.makeToast("Next schedule does not exist.")
            }
        }
        SimpleCard(
            "NavigationBar Height",
            null,
            modifier = modifier,
        ) {
            currentContext.makeToast("${currentActivity?.navigationBarHeight() ?: 0}")
        }
    }
}

@Composable
fun ExportRealmToJson(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    diaryViewModel: me.blog.korn123.easydiary.presentation.diary.DiaryViewModel,
    lifecycleScope: LifecycleCoroutineScope,
    onExportDiary: () -> Unit,
    onExportDiaryBySymbol: (Int) -> Unit,
    onExportAlarm: () -> Unit,
    onExportActionLog: () -> Unit,
    onExportDDay: () -> Unit,
) {
    val currentActivity = LocalActivity.current
    CategoryTitleCard(title = "Export Database To Json")
    var symbolSequenceInput by remember { mutableStateOf("0") }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(HORIZONTAL_PADDING.dp, 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = symbolSequenceInput,
            onValueChange = { symbolSequenceInput = it },
            label = { Text("Symbol Sequence") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
        )
        IconButton(
            onClick = {
                lifecycleScope.launch {
                    currentActivity?.openFeelingSymbolDialog(
                        "Select Symbol Sequence",
                        selectedSymbolSequence = symbolSequenceInput.toIntOrNull() ?: 0,
                        diaryViewModel.getSymbolUsedCountMap(true),
                    ) { selectedSequence: Int ->
                        symbolSequenceInput = selectedSequence.toString()
                    }
                }
            },
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_select_symbol),
                contentDescription = "Select Symbol",
                modifier = Modifier.size(32.dp),
            )
        }
    }
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            "export diary data",
            "Export diary data to external storage using SAF in domain model json format.",
            modifier = modifier,
        ) {
            onExportDiary()
        }
        SimpleCard(
            "export diary by symbol",
            "Export diary data filtered by the specified symbolSequence condition.",
            modifier = modifier,
        ) {
            val seq = symbolSequenceInput.toIntOrNull() ?: 0
            onExportDiaryBySymbol(seq)
        }
        SimpleCard(
            "export alarm data",
            "Export alarm data to external storage using SAF in domain model json format.",
            modifier = modifier,
        ) {
            onExportAlarm()
        }
        SimpleCard(
            "export action log data",
            "Export action log data to external storage using SAF in domain model json format.",
            modifier = modifier,
        ) {
            onExportActionLog()
        }
        SimpleCard(
            "export d-day data",
            "Export d-day data to external storage using SAF in domain model json format.",
            modifier = modifier,
        ) {
            onExportDDay()
        }
    }
}

@Composable
fun DevModeSettings(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    viewModel: BaseDevViewModel,
) {
    CategoryTitleCard(title = "DevMode Settings")
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SwitchCard(
            "Display Diary Sequence",
            "Turning this option on will display the diary's sequence information.",
            modifier = modifier,
            viewModel.enableDebugOptionVisibleDiarySequence,
        ) {
            viewModel.toggleDebugOptionVisibleDiarySequence()
        }

        SwitchCard(
            "Display Alarm Sequence",
            "Turning this option on will display the alarm sequence information.",
            modifier = modifier,
            viewModel.enableDebugOptionVisibleAlarmSequence,
        ) {
            viewModel.toggleDebugOptionVisibleAlarmSequence()
        }

        SwitchCard(
            "Display Tree Status",
            "Turning this option on allows you to see the status information of the tree component.",
            modifier = modifier,
            viewModel.enableDebugOptionVisibleTreeStatus,
        ) {
            viewModel.toggleDebugOptionVisibleTreeStatus()
        }

        SwitchCard(
            "Stock",
            "Turning this option on activates the unofficial Stock Chart on the dashboard.",
            modifier = modifier,
            viewModel.enableDebugOptionVisibleChartStock,
        ) {
            viewModel.toggleDebugOptionVisibleChartStock()
        }

        SwitchCard(
            "Weight",
            "Turning this option on activates the unofficial Weight Chart on the dashboard.",
            modifier = modifier,
            viewModel.enableDebugOptionVisibleChartWeight,
        ) {
            viewModel.toggleDebugOptionVisibleChartWeight()
        }

        SwitchCard(
            title = "Display Temporary Diary",
            description = "Turning this option on includes temporary diaries in the list for lookup.",
            modifier = modifier,
            isOn = viewModel.enableDebugOptionVisibleTemporaryDiary,
        ) {
            viewModel.toggleDebugOptionVisibleTemporaryDiary()
        }

        SwitchCard(
            title = "Font Preview Emoji",
            description = "Turning this option on displays emojis together in the Font Preview Dialog.",
            modifier = modifier,
            isOn = viewModel.enableDebugOptionVisibleFontPreviewEmoji,
        ) {
            viewModel.toggleDebugOptionVisibleFontPreviewEmoji()
        }
    }
}

@Composable
fun ComposeDemo(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    viewModel: BaseDevViewModel,
) {
    val currentContext = LocalContext.current
    val currentActivity = LocalActivity.current
    CategoryTitleCard(title = "ComposeDemo")
    AlarmCard(
        alarmTime = 7 * 60,
        alarmDays = "Mon, Tue, Wed, Thu, Fri",
        alarmDescription = "Google Calendar sync",
        modifier = Modifier,
        isOn = true,
        checkedChangeCallback = {},
    ) {}
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SwitchCardWithImage(
            title = "SwitchCardWithImage",
            description = "Description...",
            modifier = modifier,
            isOn = true,
            imageResourceId = R.drawable.ic_select_symbol,
        ) {}
        SimpleCardWithImage(
            title = "SimpleCardWithImage",
            description = "Description...",
            modifier = modifier,
            imageResourceId = R.drawable.ic_select_symbol,
        ) {}
        SymbolCard(
            modifier = modifier,
            viewModel,
        ) {
            viewModel.plus()
        }
        SimpleCard(
            "scrollBehavior & contentPadding & contentWindowInsets",
            "contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 1)
                },
            )
        }
        SimpleCard(
            "enableEdgeToEdge",
            "contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 2)
                },
            )
        }
        SimpleCard(
            "FastScroll + LazyColumn + SimpleCard",
            "contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical)",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 4)
                },
            )
        }
        SimpleCard(
            "FastScroll + LazyColumn + LegacyDiaryItemCard",
            "contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical)",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 5)
                },
            )
        }
        SimpleCard(
            "Full Size FastScroll + LazyColumn + LegacyDiaryItemCard",
            "contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 6)
                },
            )
        }
        SimpleCard(
            "LargeTopAppBar & scrollBehavior",
            "LargeTopAppBar + exitUntilCollapsedScrollBehavior",
            modifier = modifier,
        ) {
            TransitionHelper.startActivityWithTransition(
                currentActivity,
                Intent(currentContext, Demo1Activity::class.java).apply {
                    putExtra("mode", 7)
                },
            )
        }
    }
}

@Composable
fun Notification(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    onCreateBasicNotification: () -> Unit,
    onCreateBitmapNotification: () -> Unit,
    onCreateCustomNotification: () -> Unit,
    onCreateBigTextNotification: () -> Unit,
    onOpenOverDueNotification: () -> Unit,
    onCancelAllNotifications: () -> Unit,
) {
    CategoryTitleCard(title = "Notification")
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            "Notification-01",
            "Basic",
            modifier = modifier,
        ) {
            onCreateBasicNotification()
        }
        SimpleCard(
            "Notification-02",
            "Basic(Bitmap Icon)",
            modifier = modifier,
        ) {
            onCreateBitmapNotification()
        }
        SimpleCard(
            "Notification-03",
            "CustomContentView",
            modifier = modifier,
        ) {
            onCreateCustomNotification()
        }
        SimpleCard(
            "Notification-04",
            "BigTextStyle",
            modifier = modifier,
        ) {
            onCreateBigTextNotification()
        }
        SimpleCard(
            "Notification-05",
            "Over-Due",
            modifier = modifier,
        ) {
            onOpenOverDueNotification()
        }
        SimpleCard(
            "Notification-05",
            "Cancel All Notification",
            modifier = modifier,
        ) {
            onCancelAllNotifications()
        }
    }
}

@Composable
fun LocationManager(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    viewModel: BaseDevViewModel,
    onUpdateLocation: () -> Unit,
    onUpdateGps: () -> Unit,
    onUpdateNetwork: () -> Unit,
) {
    CategoryTitleCard(title = "Location Manager")
    SimpleCard(
        "Location Info",
        viewModel.locationInfo,
        modifier = Modifier.fillMaxWidth(),
    ) {}
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SwitchCard(
            "Toast Message",
            "Location Toast",
            modifier,
            viewModel.enableDebugOptionToastLocation,
        ) {
            viewModel.toggleDebugOptionToastLocation()
        }
        SimpleCard(
            "Location Manager",
            "Last-Location",
            modifier = modifier,
        ) { onUpdateLocation() }
        SimpleCard(
            "Location Manager",
            "Update-GPS",
            modifier = modifier,
        ) {
            onUpdateGps()
        }
        SimpleCard(
            "Location Manager",
            "Update-Network",
            modifier = modifier,
        ) {
            onUpdateNetwork()
        }
    }
}

@Composable
fun AlertDialog(
    modifier: Modifier,
    maxItemsInEachRow: Int,
) {
    val currentContext = LocalContext.current
    CategoryTitleCard(title = "Alert Dialog")
    FlowRow(
        modifier = Modifier,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            "Dialog",
            "Notification (DEFAULT)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, null, DialogMode.DEFAULT, false) }
        SimpleCard(
            "Dialog",
            "Notification (INFO)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, null, DialogMode.INFO, false) }
        SimpleCard(
            "Dialog",
            "Notification (WARNING)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, null, DialogMode.WARNING, false) }
        SimpleCard(
            "Dialog",
            "Notification (ERROR)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, null, DialogMode.ERROR, false) }
        SimpleCard(
            "Dialog",
            "Notification (SETTING)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, null, DialogMode.SETTING, false) }
        SimpleCard(
            "Dialog",
            "Confirmation (INFO)",
            modifier = modifier,
        ) { currentContext.showAlertDialog("message", null, { _, _ -> }, DialogMode.INFO) }
    }
}

@Composable
fun DebugToast(
    modifier: Modifier,
    maxItemsInEachRow: Int,
) {
    val currentContext = LocalContext.current
    CategoryTitleCard(title = "Debug Toast")
    FlowRow(
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        var enableDebugOptionToastAttachedPhoto by remember {
            mutableStateOf(
                currentContext.config.enableDebugOptionToastAttachedPhoto,
            )
        }
        SwitchCard(
            "Attached Photo Toast",
            null,
            modifier,
            enableDebugOptionToastAttachedPhoto,
        ) {
            enableDebugOptionToastAttachedPhoto = enableDebugOptionToastAttachedPhoto.not()
            currentContext.config.enableDebugOptionToastAttachedPhoto = enableDebugOptionToastAttachedPhoto
        }
        var enableDebugOptionToastNotificationInfo by remember {
            mutableStateOf(
                currentContext.config.enableDebugOptionToastNotificationInfo,
            )
        }
        SwitchCard(
            "Notification Info",
            null,
            modifier,
            enableDebugOptionToastNotificationInfo,
        ) {
            enableDebugOptionToastNotificationInfo =
                enableDebugOptionToastNotificationInfo.not()
            currentContext.config.enableDebugOptionToastNotificationInfo =
                enableDebugOptionToastNotificationInfo
        }
        var enableDebugOptionToastReviewFlowInfo by remember {
            mutableStateOf(
                currentContext.config.enableDebugOptionToastReviewFlowInfo,
            )
        }
        SwitchCard(
            "ReviewFlow Info",
            null,
            modifier,
            enableDebugOptionToastReviewFlowInfo,
        ) {
            enableDebugOptionToastReviewFlowInfo = enableDebugOptionToastReviewFlowInfo.not()
            currentContext.config.enableDebugOptionToastReviewFlowInfo = enableDebugOptionToastReviewFlowInfo
        }
        var enableDebugOptionToastPhotoHighlightUpdateTime by remember {
            mutableStateOf(
                currentContext.config.enableDebugOptionToastPhotoHighlightUpdateTime,
            )
        }
        SwitchCard(
            "Photo-Highlight Update Time",
            null,
            modifier,
            enableDebugOptionToastPhotoHighlightUpdateTime,
        ) {
            enableDebugOptionToastPhotoHighlightUpdateTime =
                enableDebugOptionToastPhotoHighlightUpdateTime.not()
            currentContext.config.enableDebugOptionToastPhotoHighlightUpdateTime =
                enableDebugOptionToastPhotoHighlightUpdateTime
        }
    }
}

@Composable
fun CustomLauncher(
    modifier: Modifier,
    maxItemsInEachRow: Int,
) {
    val currentContext = LocalContext.current
    CategoryTitleCard(title = "Custom Launcher", marginTop = 0)
    FlowRow(
        maxItemsInEachRow = maxItemsInEachRow,
        modifier = Modifier,
    ) {
        SimpleCardWithImage(
            "EasyDiary Launcher",
            description = "Basic launcher icon",
            modifier = modifier,
            imageResourceId = R.drawable.ic_launcher_round,
        ) { currentContext.toggleLauncher(Launcher.EASY_DIARY) }

        SimpleCardWithImage(
            "Dark Launcher",
            "Dark theme launcher icon",
            modifier = modifier,
            imageResourceId = R.drawable.ic_launcher_dark_round,
        ) { currentContext.toggleLauncher(Launcher.DARK) }
        SimpleCardWithImage(
            "Green Launcher",
            "Green theme launcher icon",
            modifier = modifier,
            imageResourceId = R.drawable.ic_launcher_green_round,
        ) { currentContext.toggleLauncher(Launcher.GREEN) }
        SimpleCardWithImage(
            "Debug Launcher",
            "Debug launcher icon",
            modifier = modifier,
            imageResourceId = R.drawable.ic_launcher_debug_round,
        ) { currentContext.toggleLauncher(Launcher.DEBUG) }
    }
}

@Composable
fun Coroutine(
    modifier: Modifier,
    maxItemsInEachRow: Int,
    viewModel: BaseDevViewModel,
    lifecycleScope: LifecycleCoroutineScope,
    coroutineJobRef: Job?,
    onJobChanged: (Job?) -> Unit,
) {
    CategoryTitleCard(title = "Coroutine")
    val state = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    fun updateConsole(
        message: String,
        tag: String = Thread.currentThread().name,
    ) {
        viewModel.coroutine1Console =
            viewModel.coroutine1Console + "$tag: $message\n"
        coroutineScope.launch {
            state.animateScrollBy(Float.MAX_VALUE)
        }
    }
    ScrollableCard(
        "Coroutine Info",
        viewModel.coroutine1Console,
        Modifier
            .fillMaxWidth(),
        state,
    )
    FlowRow(
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            title = "[T1] Start",
            description = "References Thread.currentThread().name every 0.5 seconds with Dispatchers.Default Scope and then updates the result in Dispatchers.Main Scope.",
            modifier = modifier,
        ) {
            if (coroutineJobRef?.isActive == true) {
                updateConsole("Job has already started.")
            } else {
                val newJob =
                    lifecycleScope.launch(Dispatchers.Default) {
                        for (i in 1..50) {
                            if (isActive) {
                                val currentThreadName = Thread.currentThread().name
                                withContext(Dispatchers.Main) {
                                    updateConsole(
                                        i.toString(),
                                        currentThreadName,
                                    )
                                }
                                delay(500)
                            }
                        }
                    }
                onJobChanged(newJob)
            }
        }
        SimpleCard(
            title = "[T1] Stop",
            description = "Cancels the Coroutine Job.",
            modifier = modifier,
        ) {
            if (coroutineJobRef?.isActive == true) {
                lifecycleScope.launch {
                    coroutineJobRef.cancelAndJoin()
                    onJobChanged(null)
                }
            } else {
                updateConsole("The job has been canceled")
            }
        }
        SimpleCard(
            title = "[T1] Job Status",
            description = "Checks the status of the Coroutine Job.",
            null,
            modifier = modifier,
        ) {
            coroutineJobRef?.let {
                updateConsole(if (it.isActive) "On" else "Off")
            } ?: updateConsole("Coroutine is not initialized.")
        }
        SimpleCard(
            title = "[T2] Multiple",
            description = "Executes 3 Coroutines without waiting.",
            modifier = modifier,
        ) {
            repeat(3) {
                lifecycleScope.launch(Dispatchers.IO) {
                    for (i in 1..10) {
                        val currentThreadName = Thread.currentThread().name
                        withContext(Dispatchers.Main) {
                            updateConsole(i.toString(), currentThreadName)
                        }
                        delay(100)
                    }
                }
            }
        }
        SimpleCard(
            "[T3] CoroutineScope Sync or Async",
            "Outputs logs before and after the CoroutineScope.",
            modifier = modifier,
        ) {
            updateConsole("scope before launch")
            lifecycleScope.launch {
                updateConsole("inner scope before delay")
                delay(2000)
                updateConsole("inner scope after delay")
            }
            updateConsole("scope after launch")
        }
    }
}

@Composable
fun FingerPrint(
    modifier: Modifier,
    maxItemsInEachRow: Int,
) {
    val currentActivity = LocalActivity.current
    CategoryTitleCard(title = "Finger Print")
    FlowRow(
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        SimpleCard(
            "StartListeningBiometric",
            "Shows the biometric prompt to the user. The prompt survives lifecycle changes by default. To cancel authentication and dismiss the prompt, use cancelAuthentication().",
            modifier = modifier,
        ) { (currentActivity as? AppCompatActivity)?.let { startListeningBiometric(it) } }
    }
}
