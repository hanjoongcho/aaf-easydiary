package me.blog.korn123.easydiary.presentation.dev

import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.extensions.actionLogRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.openOverDueNotification
import me.blog.korn123.easydiary.extensions.updateNavigationBarAppearance
import me.blog.korn123.easydiary.extensions.updateStatusBarAppearance
import me.blog.korn123.easydiary.presentation.components.EasyDiaryActionBar
import me.blog.korn123.easydiary.presentation.components.LoadingScreen
import me.blog.korn123.easydiary.presentation.dev.components.*
import me.blog.korn123.easydiary.presentation.settings.components.CardContainer
import me.blog.korn123.easydiary.presentation.theme.AppTheme

class DevActivity : BaseDevActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: BaseDevViewModel by viewModels()

        // This handles the status bar and nav bar automatically
        // Android 15 now enforces edge-to-edge by default.
        // Manually setting Color.Black or Color.
        // Transparent can lead to inconsistent behavior across different OS versions (like the "scrim" appearing on older versions).
        enableEdgeToEdge()

        binding.composeView.setContent {
            AppTheme {
                updateStatusBarAppearance()
                updateNavigationBarAppearance()

                val configuration = LocalConfiguration.current
                val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
                val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                        topBar = {
                            EasyDiaryActionBar(
                                title = "Easy-Diary Dev Mode",
                                subTitle = String.format(LocalLocale.current.platformLocale, "v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE),
                            ) {
                                finishActivityWithPauseLock()
                            }
                        },
                        containerColor = Color(config.screenBackgroundColor),
                        content = { innerPadding ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                CardContainer(
                                    modifier = Modifier.padding(innerPadding),
                                ) {
                                    val settingCardModifier =
                                        Modifier
                                            .weight(1f)

                                    RefactoringBacklog(settingCardModifier, maxItemsInEachRow)
                                    ExportRealmToJson(
                                        settingCardModifier,
                                        maxItemsInEachRow,
                                        diaryViewModel,
                                        lifecycleScope,
                                        onExportDiary = {
                                            val fileName = "diary_export_${me.blog.korn123.easydiary.utils.DateUtils.getCurrentDateTime(me.blog.korn123.easydiary.helper.DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                                            exportJsonLauncher.launch(fileName)
                                        },
                                        onExportDiaryBySymbol = { seq ->
                                            val fileName = "diary_export_symbol_${seq}_${me.blog.korn123.easydiary.utils.DateUtils.getCurrentDateTime(me.blog.korn123.easydiary.helper.DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                                            exportJsonLauncher.launch(fileName)
                                        },
                                        onExportAlarm = {
                                            val fileName = "alarm_export_${me.blog.korn123.easydiary.utils.DateUtils.getCurrentDateTime(me.blog.korn123.easydiary.helper.DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                                            exportJsonLauncher.launch(fileName)
                                        },
                                        onExportActionLog = {
                                            val fileName = "action_log_export_${me.blog.korn123.easydiary.utils.DateUtils.getCurrentDateTime(me.blog.korn123.easydiary.helper.DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                                            exportJsonLauncher.launch(fileName)
                                        },
                                        onExportDDay = {
                                            val fileName = "d-day_export_${me.blog.korn123.easydiary.utils.DateUtils.getCurrentDateTime(me.blog.korn123.easydiary.helper.DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                                            exportJsonLauncher.launch(fileName)
                                        }
                                    )
                                    CustomLauncher(settingCardModifier, maxItemsInEachRow)

                                    DevModeSettings(settingCardModifier, maxItemsInEachRow, viewModel)
                                    DebugToast(settingCardModifier, maxItemsInEachRow)

                                    Etc(
                                        settingCardModifier,
                                        maxItemsInEachRow,
                                        lifecycleScope,
                                        actionLogHandler = {
                                            lifecycleScope.launch {
                                                val actionLogs = actionLogRepository.getAllActionLogs()
                                                me.blog.korn123.easydiary.dialogs.ActionLogDialog(
                                                    this@DevActivity,
                                                    actionLogs,
                                                ) {
                                                    lifecycleScope.launch {
                                                        actionLogRepository.deleteAllActionLogs()
                                                    }
                                                }
                                            }
                                        },
                                        pickMediaLauncher = {
                                            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        }
                                    )
                                    ComposeDemo(settingCardModifier, maxItemsInEachRow, viewModel)
                                    Notification(
                                        settingCardModifier,
                                        maxItemsInEachRow,
                                        onCreateBasicNotification = { createNotificationBasic() },
                                        onCreateBitmapNotification = { createNotificationBasicWithBitmapIcon(this@DevActivity) },
                                        onCreateCustomNotification = { createNotificationCustomView(this@DevActivity) },
                                        onCreateBigTextNotification = { createNotificationBigTextStyle() },
                                        onOpenOverDueNotification = {
                                            lifecycleScope.launch {
                                                openOverDueNotification(
                                                    diaryViewModel
                                                        .findDiary(
                                                            null,
                                                            false,
                                                            0,
                                                            0,
                                                            0,
                                                        ).filter { item -> item.symbolSequence in 80..81 },
                                                )
                                            }
                                        },
                                        onCancelAllNotifications = {
                                            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
                                        }
                                    )
                                    AlertDialog(settingCardModifier, maxItemsInEachRow)

                                    LocationManager(
                                        settingCardModifier,
                                        maxItemsInEachRow,
                                        viewModel,
                                        onUpdateLocation = { updateLocation(viewModel) },
                                        onUpdateGps = { updateGPSProvider() },
                                        onUpdateNetwork = { updateNetWorkProvider() }
                                    )
                                    var jobState by remember { mutableStateOf<Job?>(null) }
                                    Coroutine(
                                        settingCardModifier,
                                        maxItemsInEachRow,
                                        viewModel,
                                        lifecycleScope,
                                        jobState,
                                        onJobChanged = { jobState = it }
                                    )
                                    FingerPrint(settingCardModifier, maxItemsInEachRow)

                                    Spacer(
                                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, bottomPadding),
                                    )
                                }
                            }
                        },
                    )
                    AnimatedVisibility(
                        visible = baseDevViewModel.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LoadingScreen(message = viewModel.loadingMessage)
                    }
                }
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}
