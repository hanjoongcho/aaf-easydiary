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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.activity.compose.LocalActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.views.MyTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.compose.Demo1Activity
import me.blog.korn123.easydiary.compose.SelfDevelopmentRepoActivity
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.dialogs.ActionLogDialog
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
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
import me.blog.korn123.easydiary.extensions.openOverDueNotification
import me.blog.korn123.easydiary.extensions.pendingIntentFlag
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.extensions.startReviewFlow
import me.blog.korn123.easydiary.extensions.syncMarkDown
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_INFO
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.ui.components.AlarmCard
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.ScrollableCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleCardWithImage
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.components.SwitchCardWithImage
import me.blog.korn123.easydiary.ui.components.SymbolCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

open class BaseDevActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var mBinding: ActivityBaseDevBinding
    private var mNotificationCount = 9000
    private var mCoroutineJob: Job? = null
    protected val mViewModel: BaseDevViewModel by viewModels()
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mNetworkLocationListener =
        object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("Network location has been updated")
                mLocationManager.removeUpdates(this)
            }

            override fun onStatusChanged(
                p0: String?,
                p1: Int,
                p2: Bundle?,
            ) {
            }

            override fun onProviderEnabled(p0: String) {}

            override fun onProviderDisabled(p0: String) {}
        }
    private val mGPSLocationListener =
        object : LocationListener {
            override fun onLocationChanged(p0: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("GPS location has been updated")
                mLocationManager.removeUpdates(this)
            }

            override fun onStatusChanged(
                p0: String?,
                p1: Int,
                p2: Bundle?,
            ) {
            }

            override fun onProviderEnabled(p0: String) {}

            override fun onProviderDisabled(p0: String) {}
        }
    private val mRequestLocationSourceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            makeSnackBar(
                if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.",
            )
        }
    protected val mFlexboxLayoutParams =
        FlexboxLayout.LayoutParams(
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
        )

    private val mPickMultipleMedia =
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

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBaseDevBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        setSupportActionBar(mBinding.partialAppbarLayout.toolbar)
//        supportActionBar?.run {
//            title = "Easy-Diary Dev Mode"
//            subtitle = String.format(Locale.getDefault(), "v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE)
//            setDisplayHomeAsUpEnabled(true)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager.run {
            removeUpdates(mGPSLocationListener)
            removeUpdates(mNetworkLocationListener)
        }
    }

    /***************************************************************************************************
     *   Define Compose
     *
     ***************************************************************************************************/
    @Composable
    protected fun Etc(
        modifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel,
    ) {
        val currentContext = LocalContext.current
        val currentActivity = LocalActivity.current
        CategoryTitleCard(title = "Etc.")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow,
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
                "Action Log",
                "Open dialog about action log",
                modifier = modifier,
            ) {
                val actionLogs: List<ActionLog> = EasyDiaryDbHelper.findActionLogAll()
                ActionLogDialog(
                    this@BaseDevActivity,
                    actionLogs,
                ) { EasyDiaryDbHelper.deleteActionLogAll() }
            }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC ALL",
                modifier = modifier,
            ) { syncMarkDown(mBinding) }
            SimpleCard(
                "ReviewFlow",
                "Starts the review flow using com.google.android.play.core.review.ReviewManagerFactory.",
                modifier = modifier,
            ) { startReviewFlow() }
            SimpleCard(
                "Reset Showcase",
                "Resets the showcase execution history.",
                modifier = modifier,
            ) {
                getSharedPreferences("showcase_internal", MODE_PRIVATE).run {
                    edit()
                        .putBoolean(
                            "hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER",
                            false,
                        ).apply()
                    edit()
                        .putBoolean(
                            "hasShot$SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER",
                            false,
                        ).apply()
                    edit()
                        .putBoolean(
                            "hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER",
                            false,
                        ).apply()
                    edit()
                        .putBoolean(
                            "hasShot$SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER",
                            false,
                        ).apply()
                }
            }
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
                        // opens input stream from the HTTP connection
                        val inputStream = httpConn.inputStream
                        val lines = IOUtils.readLines(inputStream, "UTF-8")
                        withContext(Dispatchers.Main) {
                            makeToast(lines[0])
//                            if (lines[0].contains("true")) {
//                                config.aafPinLockEnable = false
//                                config.fingerprintLockEnable = false
//                                finish()
//                            }
                        }
                        inputStream.close()
                    }
                    httpConn.disconnect()
                }
            }
            SimpleCard(
                "InApp Browser",
                null,
                modifier = modifier,
            ) {
                val customTabsIntent =
                    CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build()
                customTabsIntent.launchUrl(
                    this@BaseDevActivity,
                    Uri.parse("https://github.com/AAFactory/aafactory-commons"),
                )
            }
            SimpleCard(
                "Clear-Unused-Photo",
                null,
                modifier = modifier,
            ) {
                val localPhotoBaseNames = arrayListOf<String>()
                val unUsedPhotos = arrayListOf<String>()
                val targetFiles =
                    File(EasyDiaryUtils.getApplicationDataDirectory(this@BaseDevActivity) + DIARY_PHOTO_DIRECTORY)
                targetFiles.listFiles()?.map {
                    localPhotoBaseNames.add(it.name)
                }

                EasyDiaryDbHelper.findPhotoUriAll().map { photoUriDto ->
                    if (!localPhotoBaseNames.contains(FilenameUtils.getBaseName(photoUriDto.getFilePath()))) {
                        unUsedPhotos.add(FilenameUtils.getBaseName(photoUriDto.getFilePath()))
                    }
                }
                showAlertDialog(
                    unUsedPhotos.size.toString(),
                    null,
                    { _, _ -> },
                    DialogMode.WARNING,
                )
            }
            SimpleCard(
                "PickMultipleVisualMedia",
                null,
                modifier = modifier,
            ) {
                mPickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
            SymbolCard(
                modifier = modifier,
                viewModel,
            ) {
                viewModel.plus()
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
    protected fun DevModeSettings(
        modifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel,
    ) {
        val currentContext = LocalContext.current
        val currentActivity = LocalActivity.current
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

            SimpleCard(
                "Display Temporary Diary",
                "Turning this option on includes temporary diaries in the list for lookup.",
                modifier = modifier,
            ) {
                config.enableDebugOptionVisibleTemporaryDiary =
                    !config.enableDebugOptionVisibleTemporaryDiary
                makeSnackBar("Status: ${config.enableDebugOptionVisibleTemporaryDiary}")
            }

            SimpleCard(
                "Font Preview Emoji",
                "Turning this option on displays emojis together in the Font Preview Dialog.",
                modifier = modifier,
            ) {
                config.enableDebugOptionVisibleFontPreviewEmoji =
                    !config.enableDebugOptionVisibleFontPreviewEmoji
                makeSnackBar("Status: ${config.enableDebugOptionVisibleFontPreviewEmoji}")
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
            SimpleCard(
                "Compose Demo",
                "NestedScrollConnection",
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
                "Compose Demo",
                "FullScreen",
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
                "Compose Demo",
                "NestedScrollConnection With Auto Insets",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(currentContext, Demo1Activity::class.java).apply {
                        putExtra("mode", 3)
                    },
                )
            }
            SimpleCard(
                "Compose Demo",
                "FastScrollLazyColumnSample1",
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
                "Compose Demo",
                "FastScrollLazyColumnSample2",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(currentContext, Demo1Activity::class.java).apply {
                        putExtra("mode", 5)
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
                openOverDueNotification()
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
            SimpleCard(
                "EasyDiary Launcher",
                null,
                modifier = modifier,
            ) { toggleLauncher(Launcher.EASY_DIARY) }
            SimpleCard(
                "Dark Launcher",
                null,
                modifier = modifier,
            ) { toggleLauncher(Launcher.DARK) }
            SimpleCard(
                "Green Launcher",
                null,
                modifier = modifier,
            ) { toggleLauncher(Launcher.GREEN) }
            SimpleCard(
                "Debug Launcher",
                null,
                modifier = modifier,
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
            mViewModel.coroutine1Console = viewModel.coroutine1Console.plus("$tag: $message\n")
            coroutineScope.launch {
                state.animateScrollBy(Float.MAX_VALUE)
            }
        }
        ScrollableCard(
            "Coroutine Info",
            mViewModel.coroutine1Console,
            Modifier
                .fillMaxWidth(),
//                        .height(100.dp)
//                        .verticalScroll(state)
            state,
        )
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow,
        ) {
            SimpleCard(
                title = "[T1] Start",
                description = "0.5초 간격으로 Dispatchers.Default Scope로 Thread.currentThread().name을 참조한 후 Dispatchers.Main Scope에서 결과를 업데이트합니다.",
                modifier = modifier,
            ) {
                if (mCoroutineJob?.isActive == true) {
                    updateConsole("Job has already started.")
                } else {
                    mCoroutineJob =
                        lifecycleScope.launch(Dispatchers.Default) {
                            // launch a new coroutine and keep a reference to its Job
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
                description = "Coroutine Job을 취소합니다.",
                modifier = modifier,
            ) {
                if (mCoroutineJob?.isActive == true) {
                    lifecycleScope.launch { mCoroutineJob?.cancelAndJoin() }
                } else {
                    updateConsole("The job has been canceled")
                }
            }
            SimpleCard(
                title = "[T1] Job Status",
                description = "Coroutine Job의 상태를 확인합니다.",
                null,
                modifier = modifier,
            ) {
                mCoroutineJob?.let {
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
                description = "3개의 Coroutine을 대기없이 실행합니다.",
                modifier = modifier,
            ) {
                for (k in 1..3) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        // launch a new coroutine and keep a reference to its Job
                        for (i in 1..10) {
                            val currentThreadName = Thread.currentThread().name
                            runOnUiThread { updateConsole(i.toString(), currentThreadName) }
                            delay(100)
                        }
                    }
                }
            }
            SimpleCard(
                "[T3] CoroutineScope Sync or Async",
                "CoroutineScope 이전, 이후로 로그를 출력합니다.",
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
                "Biometric",
                null,
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
                Etc(settingCardModifier, maxItemsInEachRow.minus(1), viewModel())
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun updateGPSProvider() {
        when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            true -> {
                // hasGPSPermissions is replaceable, but check directly due to lint error
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0F,
                        mGPSLocationListener,
                    )
                }
            }

            false -> {
                makeSnackBar("GPS Provider is not available.")
            }
        }
    }

    private fun updateNetWorkProvider() {
        when (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            true -> {
                if (checkPermission(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                ) {
                    mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0F,
                        mNetworkLocationListener,
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
                mNotificationCount,
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
                mNotificationCount++,
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
                            createNotification(notification, bitmap)
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
                                    //                                        setColor(config.primaryColor)
                                    //                                        setColorized(true)
                                    //                                        setLargeIcon(BitmapFactory.decodeResource(resources, notification.largeIconResourceId))
                                    addAction(
                                        R.drawable.ic_easydiary,
                                        "Toast",
                                        PendingIntent.getService(
                                            this@BaseDevActivity,
                                            notification.id, // Private request code for the sender
                                            Intent(
                                                this@BaseDevActivity,
                                                NotificationService::class.java,
                                            ).apply {
                                                action = NotificationConstants.ACTION_DEV_TOAST
                                                putExtra(
                                                    NOTIFICATION_ID,
                                                    notification.id, // An identifier for this notification unique within your application.
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
                id = mNotificationCount++,
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
                id = mNotificationCount++,
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

    protected fun createBaseCardView(
        cardTitle: String,
        descriptionTag: String? = null,
        vararg buttons: Button,
    ): CardView {
        val titleContextTheme = ContextThemeWrapper(this, R.style.SettingsTitle)
        val descriptionContextTheme = ContextThemeWrapper(this, R.style.SettingsSummary)
        val cardContextTheme = ContextThemeWrapper(this@BaseDevActivity, R.style.AppCard_Settings)
        val linearContextTheme =
            ContextThemeWrapper(this@BaseDevActivity, R.style.LinearLayoutVertical)
        return CardView(cardContextTheme).apply {
            addView(
                LinearLayout(linearContextTheme).apply {
                    addView(
                        MyTextView(titleContextTheme).apply {
                            text = cardTitle
                        },
                    )
                    addView(
                        FlexboxLayout(this@BaseDevActivity).apply {
                            flexDirection = FlexDirection.ROW
                            flexWrap = FlexWrap.WRAP
                            buttons.forEach { addView(it) }
                        },
                    )
                    descriptionTag?.let {
                        addView(
                            MyTextView(descriptionContextTheme).apply {
                                tag = descriptionTag
                            },
                        )
                    }
                },
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
//                mBinding.root.findViewWithTag<MyTextView>(TAG_LOCATION_MANAGER).text = info
//                makeSnackBar(info)
                viewModel.locationInfo = info
            }
        }
        when (hasGPSPermissions()) {
            true -> {
                setLocationInfo()
            }

            false -> {
                acquireGPSPermissions(mRequestLocationSourceLauncher) {
                    setLocationInfo()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createNotification(
        notificationInfo: NotificationInfo,
        bitmap: Bitmap? = null,
    ): NotificationCompat.Builder {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(
                    "${NOTIFICATION_CHANNEL_ID}_dev",
                    getString(R.string.notification_channel_name_dev),
                    importance,
                )
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
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
                        notificationInfo.id, // Private request code for the sender
                        Intent(this@BaseDevActivity, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(NOTIFICATION_ID, notificationInfo.id)
                            putExtra(
                                NOTIFICATION_INFO,
                                "Test Notification Count: $mNotificationCount",
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
                    notificationInfo.id, // Private request code for the sender
                    Intent(this, NotificationService::class.java).apply {
                        action = NotificationConstants.ACTION_DEV_DISMISS
                        putExtra(
                            NOTIFICATION_ID,
                            notificationInfo.id, // An identifier for this notification unique within your application.
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
 ***************************************************************************************************/
data class NotificationInfo(
    var largeIconResourceId: Int,
    var useActionButton: Boolean = false,
    var id: Int = 0,
)

/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/
