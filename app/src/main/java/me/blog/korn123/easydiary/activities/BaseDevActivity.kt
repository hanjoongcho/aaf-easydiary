package me.blog.korn123.easydiary.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.compose.LocalActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.GsonBuilder
import com.simplemobiletools.commons.helpers.isOreoPlus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.dialogs.ActionLogDialog
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.ExportOption
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.actionLogRepository
import me.blog.korn123.easydiary.extensions.alarmRepository
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.dpToPixelFloatValue
import me.blog.korn123.easydiary.extensions.fullAddress
import me.blog.korn123.easydiary.extensions.getFromLocation
import me.blog.korn123.easydiary.extensions.getLastKnownLocation
import me.blog.korn123.easydiary.extensions.hasGPSPermissions
import me.blog.korn123.easydiary.extensions.isLocationEnabled
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.navigationBarHeight
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.extensions.openOverDueNotification
import me.blog.korn123.easydiary.extensions.pendingIntentFlag
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.extensions.startReviewFlow
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.extensions.updateStatusBarAppearance
import me.blog.korn123.easydiary.helper.ComposeConstants.HORIZONTAL_PADDING
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_INFO
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP
import me.blog.korn123.easydiary.presentation.dev.BaseDevViewModel
import me.blog.korn123.easydiary.presentation.dev.Demo1Activity
import me.blog.korn123.easydiary.presentation.dev.SelfDevelopmentRepoActivity
import me.blog.korn123.easydiary.presentation.settings.components.AlarmCard
import me.blog.korn123.easydiary.presentation.settings.components.CategoryTitleCard
import me.blog.korn123.easydiary.presentation.settings.components.ScrollableCard
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCard
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCardWithImage
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCard
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCardWithImage
import me.blog.korn123.easydiary.presentation.settings.components.SymbolCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.net.HttpURLConnection
import java.net.URL
import me.blog.korn123.easydiary.domain.model.ActionLog as ActionLogDomain

@AndroidEntryPoint
open class BaseDevActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var binding: ActivityBaseDevBinding
    private var notificationCount = 9000
    private var coroutineJob: Job? = null
    protected val baseDevViewModel: BaseDevViewModel by viewModels()
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val networkLocationListener =
        object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("Network location has been updated")
                locationManager.removeUpdates(this)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(
                p0: String?,
                p1: Int,
                p2: Bundle?,
            ) {
            }

            override fun onProviderEnabled(p0: String) {}

            override fun onProviderDisabled(p0: String) {}
        }
    private val gpsLocationListener =
        object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("GPS location has been updated")
                locationManager.removeUpdates(this)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(
                p0: String?,
                p1: Int,
                p2: Bundle?,
            ) {
            }

            override fun onProviderEnabled(p0: String) {}

            override fun onProviderDisabled(p0: String) {}
        }
    private val requestLocationSourceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            makeSnackBar(
                if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.",
            )
        }

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            if (uris.isNotEmpty()) {
                showAlertDialog(
                    uris.joinToString(",") { uri -> uri.toString() },
                    null,
                    null,
                    DialogMode.INFO,
                    false,
                )
            } else {
                makeToast("There are no photos selected.")
            }
        }

    private var exportJsonOption = ExportOption.DIARY
    private var exportJsonSymbolSequence = 0
    private val exportJsonLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            uri?.let { targetUri ->
                baseDevViewModel.isLoading = true
                lifecycleScope.launch {
                    var mapOfItems: MutableMap<String, Any> =
                        when (exportJsonOption) {
                            ExportOption.ACTION_LOG -> {
                                mutableMapOf(
                                    "ACTION_LOG" to
                                        actionLogRepository.getAllActionLogs(),
                                )
                            }

                            ExportOption.ALARM -> {
                                mutableMapOf("ALARM" to alarmRepository.getAllAlarms())
                            }

                            ExportOption.D_DAY -> {
                                mutableMapOf(
                                    "D_DAY" to
                                        dDayRepository.getAllDDays(),
                                )
                            }

                            ExportOption.DIARY -> {
                                mutableMapOf("DIARY" to diaryRepository.getDiariesWithPhotos())
                            }

                            ExportOption.DIARY_WITH_SYMBOL_SEQUENCE -> {
                                mutableMapOf(
                                    "DIARY" to
                                        diaryRepository.getDiariesWithPhotos(
                                            symbolSequence = exportJsonSymbolSequence,
                                        ),
                                )
                            }
                        }

                    mapOfItems["META"] = EasyDiaryUtils.getExportMeta()
                    try {
                        withContext(Dispatchers.IO) {
                            val jsonString =
                                GsonBuilder().setPrettyPrinting().create().toJson(mapOfItems)
                            val fileName = targetUri.path ?: ""
                            contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                            }
                        }
                        makeToast("Export successful!")
                    } catch (e: Exception) {
                        makeToast("Export failed: ${e.message}")
                    } finally {
                        baseDevViewModel.isLoading = false
                    }
                }
            }
        }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDevBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.run {
            removeUpdates(gpsLocationListener)
            removeUpdates(networkLocationListener)
        }
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    protected fun RefactoringBacklog(
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
                    Intent(
                        currentContext,
                        me.blog.korn123.easydiary.presentation.main.DiaryMainActivity::class.java,
                    ),
                )
            }
            SimpleCard(
                "Mig DiaryMain2",
                "Migrated DiaryMain screen to Jetpack Compose",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(
                        currentContext,
                        me.blog.korn123.easydiary.presentation.main.DiaryMain2Activity::class.java,
                    ),
                )
            }
        }
    }

    @Composable
    protected fun Etc(
        modifier: Modifier,
        maxItemsInEachRow: Int,
    ) {
        CategoryTitleCard(title = "Etc.")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            SimpleCard(
                "StatusBar",
                "Dark Icons",
                modifier = modifier,
            ) { updateStatusBarAppearance(android.graphics.Color.WHITE) }
            SimpleCard(
                "StatusBar",
                "Light Icons",
                modifier = modifier,
            ) { updateStatusBarAppearance(android.graphics.Color.BLACK) }
            SimpleCard(
                "Check Build Info",
                "Device information checkable via android.os.Build package",
                modifier = modifier,
            ) {
                showAlertDialog(
                    "" +
                        "Manufacturer: ${Build.MANUFACTURER}\n" +
                        "Model: ${Build.MODEL}\n" +
                        "OS: ${Build.VERSION.RELEASE}\n" +
                        "SDK: ${Build.VERSION.SDK_INT}\n" +
                        "",
                )
            }
            SimpleCard(
                "Action Log",
                "Open dialog about action log",
                modifier = modifier,
            ) {
                lifecycleScope.launch {
                    val actionLogs: List<ActionLogDomain> = actionLogRepository.getAllActionLogs()
                    ActionLogDialog(
                        this@BaseDevActivity,
                        actionLogs,
                    ) {
                        lifecycleScope.launch {
                            actionLogRepository.deleteAllActionLogs()
                        }
                    }
                }
            }
            SimpleCard(
                "ReviewFlow",
                "Starts the review flow using com.google.android.play.core.review.ReviewManagerFactory.",
                modifier = modifier,
            ) { startReviewFlow() }
            SimpleCard(
                "Reset Font Size",
                null,
                modifier = modifier,
            ) {
                config.settingFontSize =
                    spToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())
                makeToast(
                    "DP:${
                        dpToPixelFloatValue(
                            UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat(),
                        )
                    } , SP:${
                        spToPixelFloatValue(
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
                        makeToast("Response Code: $responseCode")
                    }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val lines = httpConn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readLines() }
                        withContext(Dispatchers.Main) {
                            makeToast(lines[0])
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
                        this@BaseDevActivity,
                        "https://aafactory.github.io/".toUri(),
                    )
                }
            }
            SimpleCard(
                "PickMultipleVisualMedia",
                null,
                modifier = modifier,
            ) {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            SimpleCard(
                "Check Next Alarm",
                null,
                modifier = modifier,
            ) {
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.run {
                    val alarmInfo = nextAlarmClock
                    alarmInfo?.let {
                        makeToast(
                            "Next: ${
                                DateUtils.getDateTimeStringFromTimeMillis(
                                    alarmInfo.triggerTime,
                                )
                            }",
                        )
                    } ?: makeToast("Next schedule does not exist.")
                }
            }
            SimpleCard(
                "NavigationBar Height",
                null,
                modifier = modifier,
            ) {
                makeToast("${navigationBarHeight()}")
            }
        }
    }

    @Composable
    protected fun ExportRealmToJson(
        modifier: Modifier,
        maxItemsInEachRow: Int,
    ) {
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
                        openFeelingSymbolDialog(
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
                val fileName =
                    "diary_export_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                exportJsonOption = ExportOption.DIARY
                exportJsonLauncher.launch(fileName)
            }
            SimpleCard(
                "export diary by symbol",
                "Export diary data filtered by the specified symbolSequence condition.",
                modifier = modifier,
            ) {
                val seq = symbolSequenceInput.toIntOrNull() ?: 0
                val fileName =
                    "diary_export_symbol_${seq}_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                exportJsonSymbolSequence = seq
                exportJsonOption = ExportOption.DIARY_WITH_SYMBOL_SEQUENCE
                exportJsonLauncher.launch(fileName)
            }
            SimpleCard(
                "export alarm data",
                "Export alarm data to external storage using SAF in domain model json format.",
                modifier = modifier,
            ) {
                val fileName =
                    "alarm_export_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                exportJsonOption = ExportOption.ALARM
                exportJsonLauncher.launch(fileName)
            }
            SimpleCard(
                "export action log data",
                "Export action log data to external storage using SAF in domain model json format.",
                modifier = modifier,
            ) {
                val fileName =
                    "action_log_export_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                exportJsonOption = ExportOption.ACTION_LOG
                exportJsonLauncher.launch(fileName)
            }
            SimpleCard(
                "export d-day data",
                "Export d-day data to external storage using SAF in domain model json format.",
                modifier = modifier,
            ) {
                val fileName =
                    "d-day_export_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}.json"
                exportJsonOption = ExportOption.D_DAY
                exportJsonLauncher.launch(fileName)
            }
        }
    }

    @Composable
    protected fun DevModeSettings(
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
    protected fun ComposeDemo(
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
    protected fun Notification(
        modifier: Modifier,
        maxItemsInEachRow: Int,
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
                createNotificationBasic()
            }
            SimpleCard(
                "Notification-02",
                "Basic(Bitmap Icon)",
                modifier = modifier,
            ) {
                createNotificationBasicWithBitmapIcon(this@BaseDevActivity)
            }
            SimpleCard(
                "Notification-03",
                "CustomContentView",
                modifier = modifier,
            ) {
                createNotificationCustomView(this@BaseDevActivity)
            }
            SimpleCard(
                "Notification-04",
                "BigTextStyle",
                modifier = modifier,
            ) {
                createNotificationBigTextStyle()
            }
            SimpleCard(
                "Notification-05",
                "Over-Due",
                modifier = modifier,
            ) {
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
            }
            SimpleCard(
                "Notification-05",
                "Cancel All Notification",
                modifier = modifier,
            ) {
                getSystemService<NotificationManager>()?.cancelAll()
            }
        }
    }

    @Composable
    protected fun LocationManager(
        modifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel,
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
            ) { updateLocation(viewModel) }
            SimpleCard(
                "Location Manager",
                "Update-GPS",
                modifier = modifier,
            ) {
                updateGPSProvider()
            }
            SimpleCard(
                "Location Manager",
                "Update-Network",
                modifier = modifier,
            ) {
                updateNetWorkProvider()
            }
        }
    }

    @Composable
    protected fun AlertDialog(
        modifier: Modifier,
        maxItemsInEachRow: Int,
    ) {
        CategoryTitleCard(title = "Alert Dialog")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            SimpleCard(
                "Dialog",
                "Notification (DEFAULT)",
                modifier = modifier,
            ) { showAlertDialog("message", null, null, DialogMode.DEFAULT, false) }
            SimpleCard(
                "Dialog",
                "Notification (INFO)",
                modifier = modifier,
            ) { showAlertDialog("message", null, null, DialogMode.INFO, false) }
            SimpleCard(
                "Dialog",
                "Notification (WARNING)",
                modifier = modifier,
            ) { showAlertDialog("message", null, null, DialogMode.WARNING, false) }
            SimpleCard(
                "Dialog",
                "Notification (ERROR)",
                modifier = modifier,
            ) { showAlertDialog("message", null, null, DialogMode.ERROR, false) }
            SimpleCard(
                "Dialog",
                "Notification (SETTING)",
                modifier = modifier,
            ) { showAlertDialog("message", null, null, DialogMode.SETTING, false) }
            SimpleCard(
                "Dialog",
                "Confirmation (INFO)",
                modifier = modifier,
            ) { showAlertDialog("message", null, { _, _ -> }, DialogMode.INFO) }
        }
    }

    @Composable
    protected fun DebugToast(
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
                config.enableDebugOptionToastAttachedPhoto = enableDebugOptionToastAttachedPhoto
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
                config.enableDebugOptionToastNotificationInfo =
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
                config.enableDebugOptionToastReviewFlowInfo = enableDebugOptionToastReviewFlowInfo
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
                config.enableDebugOptionToastPhotoHighlightUpdateTime =
                    enableDebugOptionToastPhotoHighlightUpdateTime
            }
        }
    }

    @Composable
    protected fun CustomLauncher(
        modifier: Modifier,
        maxItemsInEachRow: Int,
    ) {
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
            ) { toggleLauncher(Launcher.EASY_DIARY) }

            SimpleCardWithImage(
                "Dark Launcher",
                "Dark theme launcher icon",
                modifier = modifier,
                imageResourceId = R.drawable.ic_launcher_dark_round,
            ) { toggleLauncher(Launcher.DARK) }
            SimpleCardWithImage(
                "Green Launcher",
                "Green theme launcher icon",
                modifier = modifier,
                imageResourceId = R.drawable.ic_launcher_green_round,
            ) { toggleLauncher(Launcher.GREEN) }
            SimpleCardWithImage(
                "Debug Launcher",
                "Debug launcher icon",
                modifier = modifier,
                imageResourceId = R.drawable.ic_launcher_debug_round,
            ) { toggleLauncher(Launcher.DEBUG) }
        }
    }

    @Composable
    protected fun Coroutine(
        modifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel,
    ) {
        CategoryTitleCard(title = "Coroutine")
        val state = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()

        fun updateConsole(
            message: String,
            tag: String = Thread.currentThread().name,
        ) {
            baseDevViewModel.coroutine1Console =
                viewModel.coroutine1Console.plus("$tag: $message\n")
            coroutineScope.launch {
                state.animateScrollBy(Float.MAX_VALUE)
            }
        }
        ScrollableCard(
            "Coroutine Info",
            baseDevViewModel.coroutine1Console,
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
                if (coroutineJob?.isActive == true) {
                    updateConsole("Job has already started.")
                } else {
                    coroutineJob =
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
                }
            }
            SimpleCard(
                title = "[T1] Stop",
                description = "Cancels the Coroutine Job.",
                modifier = modifier,
            ) {
                if (coroutineJob?.isActive == true) {
                    lifecycleScope.launch { coroutineJob?.cancelAndJoin() }
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
                coroutineJob?.let {
                    when (it.isActive) {
                        true -> updateConsole("On")
                        false -> updateConsole("Off")
                    }
                } ?: run {
                    updateConsole("Coroutine is not initialized.")
                }
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
    protected fun FingerPrint(
        modifier: Modifier,
        maxItemsInEachRow: Int,
    ) {
        CategoryTitleCard(title = "Finger Print")
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            SimpleCard(
                "StartListeningBiometric",
                "Shows the biometric prompt to the user. The prompt survives lifecycle changes by default. To cancel authentication and dismiss the prompt, use cancelAuthentication().",
                modifier = modifier,
            ) { startListeningBiometric(this@BaseDevActivity) }
        }
    }

    @Preview(heightDp = 1200)
    @Composable
    protected fun EtcPreview() {
        AppTheme {
            val configuration = LocalConfiguration.current
            val maxItemsInEachRow =
                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
            Column {
                val settingCardModifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                Etc(settingCardModifier, maxItemsInEachRow.minus(1))
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun updateGPSProvider() {
        when (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            true -> {
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0F,
                        gpsLocationListener,
                    )
                }
            }

            false -> {
                makeSnackBar("GPS Provider is not available.")
            }
        }
    }

    private fun updateNetWorkProvider() {
        when (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            true -> {
                if (checkPermission(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0F,
                        networkLocationListener,
                    )
                }
            }

            false -> {
                makeSnackBar("Network Provider is not available.")
            }
        }
    }

    private fun createNotificationBigTextStyle() {
        val notification =
            NotificationInfo(
                R.drawable.ic_done,
                useActionButton = true,
                notificationCount,
            )
        if (ActivityCompat.checkSelfPermission(
                this@BaseDevActivity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat
                .from(this@BaseDevActivity)
                .notify(
                    notification.id,
                    createNotification(notification)
                        .also {
                            val contentTitle = "[${notification.id}] BigTextStyle Title"
                            val contentText =
                                "This is the contentText area. To express long messages, use NotificationCompat.BigTextStyle()."
                            it.setStyle(
                                NotificationCompat
                                    .BigTextStyle()
                                    .setSummaryText("[BigTextStyle] $contentTitle")
                                    .bigText("[BigTextStyle] $contentText"),
                            )
                            it.setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    notification.largeIconResourceId,
                                ),
                            )
                        }.build(),
                )
        }
    }

    private fun createNotificationCustomView(context: Context) {
        val notification =
            NotificationInfo(
                R.drawable.ic_diary_backup_local,
                useActionButton = true,
                notificationCount++,
            )
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap =
                Glide
                    .with(context)
                    .asBitmap()
                    .load(R.drawable.bg_travel_4514822_1280)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(context.dpToPixel(5F)),
                    ).submit(200, 200)
                    .get()
            withContext(Dispatchers.Main) {
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat
                        .from(this@BaseDevActivity)
                        .notify(
                            notification.id,
                            createNotification(notification)
                                .apply {
                                    setStyle(NotificationCompat.DecoratedCustomViewStyle())
                                    setCustomContentView(
                                        RemoteViews(
                                            applicationContext.packageName,
                                            R.layout.partial_notification_contents,
                                        ).apply {
                                            setTextViewText(
                                                R.id.text_notification_content,
                                                "[${notification.id}] This package is part of the Android support library which is no longer maintained. The support library has been superseded by AndroidX which is part of Jetpack. We recommend using the AndroidX libraries in all new projects.",
                                            )
                                            setImageViewBitmap(
                                                R.id.img_notification_content,
                                                bitmap,
                                            )
                                        },
                                    )
                                    setCustomBigContentView(
                                        RemoteViews(
                                            applicationContext.packageName,
                                            R.layout.partial_notification,
                                        ).apply {
                                            setImageViewResource(
                                                R.id.img_notification_content,
                                                R.drawable.bg_travel_4514822_1280,
                                            )
                                        },
                                    )
                                    addAction(
                                        R.drawable.ic_easydiary,
                                        "Toast",
                                        PendingIntent.getService(
                                            this@BaseDevActivity,
                                            notification.id,
                                            Intent(
                                                this@BaseDevActivity,
                                                NotificationService::class.java,
                                            ).apply {
                                                action = NotificationConstants.ACTION_DEV_TOAST
                                                putExtra(
                                                    NOTIFICATION_ID,
                                                    notification.id,
                                                )
                                            },
                                            pendingIntentFlag(),
                                        ),
                                    )
                                }.build(),
                        )
                }
            }
        }
    }

    private fun createNotificationBasicWithBitmapIcon(context: Context) {
        val notification =
            NotificationInfo(
                R.drawable.ic_diary_writing,
                useActionButton = true,
                id = notificationCount++,
            )
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap =
                Glide
                    .with(context)
                    .asBitmap()
                    .load(R.drawable.bg_travel_4514822_1280)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(context.dpToPixel(5F)),
                    ).submit(200, 200)
                    .get()
            withContext(Dispatchers.Main) {
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat
                        .from(this@BaseDevActivity)
                        .notify(
                            notification.id,
                            createNotification(notification)
                                .also {
                                    val contentTitle = "[${notification.id}] Basic Notification"
                                    val contentText =
                                        "This is a basic notification message. If there is too much content in a basic notification message, the message may not be displayed normally."
                                    it.setContentTitle(contentTitle)
                                    it.setContentText(contentText)
                                    it.setLargeIcon(bitmap)
                                }.build(),
                        )
                }
            }
        }
    }

    private fun createNotificationBasic() {
        val notification =
            NotificationInfo(
                R.drawable.ic_diary_writing,
                useActionButton = true,
                id = notificationCount++,
            )
        if (ActivityCompat.checkSelfPermission(
                this@BaseDevActivity,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat
                .from(this@BaseDevActivity)
                .notify(
                    notification.id,
                    createNotification(notification)
                        .also {
                            val contentTitle = "[${notification.id}] Basic Notification"
                            val contentText =
                                "This is a basic notification message. If there is too much content in a basic notification message, the message may not be displayed normally."
                            it.setContentTitle(contentTitle)
                            it.setContentText(contentText)
                            it.setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    notification.largeIconResourceId,
                                ),
                            )
                            it.setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    notification.largeIconResourceId,
                                ),
                            )
                        }.build(),
                )
        }
    }

    private fun updateLocation(viewModel: BaseDevViewModel) {
        fun setLocationInfo() {
            getLastKnownLocation()?.let {
                var info = "Longitude: ${it.longitude}\nLatitude: ${it.latitude}\n"
                getFromLocation(it.latitude, it.longitude, 1)?.let { address ->
                    if (address.isNotEmpty()) {
                        info += fullAddress(address[0])
                    }
                }
                viewModel.locationInfo = info
            }
        }
        when (hasGPSPermissions()) {
            true -> {
                setLocationInfo()
            }

            false -> {
                acquireGPSPermissions(requestLocationSourceLauncher) {
                    setLocationInfo()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createNotification(
        notificationInfo: NotificationInfo,
    ): NotificationCompat.Builder {
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(
                    "${NOTIFICATION_CHANNEL_ID}_dev",
                    getString(R.string.notification_channel_name_dev),
                    importance,
                )
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev").apply {
                setDefaults(Notification.DEFAULT_ALL)
                setWhen(System.currentTimeMillis())
                setSmallIcon(R.drawable.ic_easydiary)
                setOnlyAlertOnce(true)
                setOngoing(false)
                setAutoCancel(true)
                setContentIntent(
                    PendingIntent.getActivity(
                        this@BaseDevActivity,
                        notificationInfo.id,
                        Intent(this@BaseDevActivity, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(NOTIFICATION_ID, notificationInfo.id)
                            putExtra(
                                NOTIFICATION_INFO,
                                "Test Notification Count: $notificationCount",
                            )
                        },
                        pendingIntentFlag(),
                    ),
                )
            }

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                R.drawable.ic_easydiary,
                getString(R.string.dismiss),
                PendingIntent.getService(
                    this,
                    notificationInfo.id,
                    Intent(this, NotificationService::class.java).apply {
                        action = NotificationConstants.ACTION_DEV_DISMISS
                        putExtra(
                            NOTIFICATION_ID,
                            notificationInfo.id,
                        )
                    },
                    pendingIntentFlag(),
                ),
            )
        }

        return notificationBuilder
    }
}

/***************************************************************************************************
 *   classes
 *
 ****************************************************************************************************/
data class NotificationInfo(
    var largeIconResourceId: Int,
    var useActionButton: Boolean = false,
    var id: Int = 0,
)

/***************************************************************************************************
 *   extensions
 *
 ****************************************************************************************************/
