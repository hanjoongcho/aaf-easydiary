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
import android.view.View
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.views.MyTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningFingerprint
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.compose.Demo1Activity
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
import me.blog.korn123.easydiary.extensions.pushMarkDown
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.extensions.startReviewFlow
import me.blog.korn123.easydiary.extensions.syncMarkDown
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_ALL
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_DEV
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_ETC
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_LIFE
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_INFO
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.ui.components.AlarmCard
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.ScrollableCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.components.SwitchCardWithImage
import me.blog.korn123.easydiary.ui.components.SymbolCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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
    private var mCoroutineJob1: Job? = null
    private val mViewModel: BaseDevViewModel by viewModels()
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mNetworkLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            if (config.enableDebugOptionToastLocation) makeToast("Network location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mGPSLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            if (config.enableDebugOptionToastLocation) makeToast("GPS location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mRequestLocationSourceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.")
    }
    protected val mFlexboxLayoutParams = FlexboxLayout.LayoutParams(
        FlexboxLayout.LayoutParams.WRAP_CONTENT
        , FlexboxLayout.LayoutParams.WRAP_CONTENT
    )

    private val mPickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isNotEmpty()) {
            showAlertDialog(uris.joinToString(",") { uri -> uri.toString() }, null, null, DialogMode.INFO, false)
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
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun Etc(modifier: Modifier, maxItemsInEachRow: Int, viewModel: BaseDevViewModel) {
        val currentContext = LocalContext.current
        val currentActivity = LocalActivity.current
        CategoryTitleCard(title = "Etc.")
        AlarmCard(
            alarmTime = 7 * 60,
            alarmDays = "Mon, Tue, Wed, Thu, Fri",
            alarmDescription = "Google Calendar sync",
            modifier = Modifier,
            isOn = true,
            checkedChangeCallback = {}
        ) {}
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            SimpleCard(
                "Compose Demo",
                "NestedScrollConnection",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(currentContext, Demo1Activity::class.java).apply {
                        putExtra("mode", 1)
                    }
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
                    }
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
                    }
                )
            }
            SimpleCard(
                "Compose Demo",
                "CollapsingTopAppBarFullScreen",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(currentContext, Demo1Activity::class.java).apply {
                        putExtra("mode", 3)
                    }
                )
            }
            SimpleCard(
                "Compose Demo",
                "CollapsingTopAppBarFullScreenTransparentStatusBar",
                modifier = modifier,
            ) {
                TransitionHelper.startActivityWithTransition(
                    currentActivity,
                    Intent(currentContext, Demo1Activity::class.java).apply {
                        putExtra("mode", 4)
                    }
                )
            }
            SwitchCardWithImage(
                title = currentContext.getString(R.string.task_symbol_top_order_title),
                description = currentContext.getString(R.string.task_symbol_top_order_description),
                modifier = modifier,
                isOn = true,
                imageResourceId = R.drawable.ic_select_symbol
            ) {}
            var enableDebugOptionVisibleDiarySequence by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleDiarySequence) }
            SwitchCard(
                "Display Diary Sequence",
                null,
                modifier,
                enableDebugOptionVisibleDiarySequence
            ) {
                enableDebugOptionVisibleDiarySequence = enableDebugOptionVisibleDiarySequence.not()
                config.enableDebugOptionVisibleDiarySequence = enableDebugOptionVisibleDiarySequence
            }
            var enableDebugOptionVisibleAlarmSequence by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleAlarmSequence) }
            SwitchCard(
                "Display Alarm Sequence",
                null,
                modifier,
                enableDebugOptionVisibleAlarmSequence
            ) {
                enableDebugOptionVisibleAlarmSequence = enableDebugOptionVisibleAlarmSequence.not()
                config.enableDebugOptionVisibleAlarmSequence = enableDebugOptionVisibleAlarmSequence
            }
            var enableDebugOptionVisibleChartStock by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleChartStock) }
            SwitchCard(
                "Stock",
                null,
                modifier,
                enableDebugOptionVisibleChartStock
            ) {
                enableDebugOptionVisibleChartStock = enableDebugOptionVisibleChartStock.not()
                config.enableDebugOptionVisibleChartStock = enableDebugOptionVisibleChartStock
            }
            var enableDebugOptionVisibleChartWeight by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleChartWeight) }
            SwitchCard(
                "Weight",
                null,
                modifier,
                enableDebugOptionVisibleChartWeight
            ) {
                enableDebugOptionVisibleChartWeight = enableDebugOptionVisibleChartWeight.not()
                config.enableDebugOptionVisibleChartWeight = enableDebugOptionVisibleChartWeight
            }
            SimpleCard(
                "Action Log",
                "Open Action Log",
                modifier = modifier,
            ) {
                val actionLogs: List<ActionLog> = EasyDiaryDbHelper.findActionLogAll()
                ActionLogDialog(
                    this@BaseDevActivity,
                    actionLogs
                ) { EasyDiaryDbHelper.deleteActionLogAll() }
            }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC ALL",
                modifier = modifier,
            ) { syncMarkDown(mBinding) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC dev",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_DEV) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC life",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_LIFE) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC etc",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_ETC) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC stock/FICS",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_STOCK_FICS) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC stock/ETF",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_STOCK_ETF) }
            SimpleCard(
                "GitHub MarkDown Page",
                "SYNC stock/knowledge",
                modifier = modifier,
            ) { syncMarkDown(mBinding, DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE) }
            SimpleCard(
                "GitHub MarkDown Page",
                "Push",
                modifier = modifier,
            ) { pushMarkDown("test.md", "Hello World!!!") }
            SimpleCard(
                "ReviewFlow",
                null,
                modifier = modifier,
            ) { startReviewFlow() }
            SimpleCard(
                "Reset Showcase",
                null,
                modifier = modifier,
            ) {
                getSharedPreferences("showcase_internal", MODE_PRIVATE).run {
                    edit().putBoolean(
                        "hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER",
                        false
                    ).apply()
                    edit().putBoolean(
                        "hasShot$SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER",
                        false
                    ).apply()
                    edit().putBoolean(
                        "hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER",
                        false
                    ).apply()
                    edit().putBoolean(
                        "hasShot$SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER",
                        false
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
                            UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat()
                        )
                    } , SP:${
                        spToPixelFloatValue(
                            UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat()
                        )
                    }"
                )
            }
            SimpleCard(
                "Check Force Release URL",
                null,
                modifier = modifier,
            ) {
                CoroutineScope(Dispatchers.IO).launch {
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
                    Uri.parse("https://github.com/AAFactory/aafactory-commons")
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
                    unUsedPhotos.size.toString(), null,
                    { _, _ -> }, DialogMode.WARNING
                )
            }
            SimpleCard(
                "Font Preview Emoji",
                null,
                modifier = modifier,
            ) {
                config.enableDebugOptionVisibleFontPreviewEmoji =
                    !config.enableDebugOptionVisibleFontPreviewEmoji
                makeSnackBar("Status: ${config.enableDebugOptionVisibleFontPreviewEmoji}")
            }
            SimpleCard(
                "Display Temporary Diary",
                null,
                modifier = modifier,
            ) {
                config.enableDebugOptionVisibleTemporaryDiary =
                    !config.enableDebugOptionVisibleTemporaryDiary
                makeSnackBar("Status: ${config.enableDebugOptionVisibleTemporaryDiary}")
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
                                    alarmInfo.triggerTime
                                )
                            }"
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun Notification(
        modifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        CategoryTitleCard(title = "Notification")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun LocationManager(
        settingCardModifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel
    ) {
        val currentContext = LocalContext.current
        CategoryTitleCard(title = "Location Manager")
        val locationInfo by viewModel.locationInfo.observeAsState("")
        SimpleCard(
            "Location Info",
            locationInfo,
            modifier = Modifier.fillMaxWidth(),
        ) {}
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            var enableDebugOptionToastLocation by remember { mutableStateOf(currentContext.config.enableDebugOptionToastLocation) }
            SwitchCard(
                "Toast Message",
                "Location Toast",
                settingCardModifier,
                enableDebugOptionToastLocation
            ) {
                enableDebugOptionToastLocation = enableDebugOptionToastLocation.not()
                config.enableDebugOptionToastLocation = enableDebugOptionToastLocation
            }
            SimpleCard(
                "Location Manager",
                "Last-Location",
                modifier = settingCardModifier,
            ) { updateLocation(viewModel) }
            SimpleCard(
                "Location Manager",
                "Update-GPS",
                modifier = settingCardModifier,
            ) {
                updateGPSProvider()
            }
            SimpleCard(
                "Location Manager",
                "Update-Network",
                modifier = settingCardModifier,
            ) {
                updateNetWorkProvider()
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun AlertDialog(
        settingCardModifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        CategoryTitleCard(title = "Alert Dialog")
        FlowRow(
            modifier = Modifier,
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            SimpleCard(
                "Dialog",
                "알림(DEFAULT)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, null, DialogMode.DEFAULT, false) }
            SimpleCard(
                "Dialog",
                "알림(INFO)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, null, DialogMode.INFO, false) }
            SimpleCard(
                "Dialog",
                "알림(WARNING)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, null, DialogMode.WARNING, false) }
            SimpleCard(
                "Dialog",
                "알림(ERROR)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, null, DialogMode.ERROR, false) }
            SimpleCard(
                "Dialog",
                "알림(SETTING)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, null, DialogMode.SETTING, false) }
            SimpleCard(
                "Dialog",
                "확인(INFO)",
                modifier = settingCardModifier,
            ) { showAlertDialog("message", null, { _,_ -> }, DialogMode.INFO) }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun DebugToast(
        settingCardModifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        val currentContext = LocalContext.current
        CategoryTitleCard(title = "Debug Toast")
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            var enableDebugOptionToastAttachedPhoto by remember { mutableStateOf(currentContext.config.enableDebugOptionToastAttachedPhoto) }
            SwitchCard(
                "Attached Photo Toast",
                null,
                settingCardModifier,
                enableDebugOptionToastAttachedPhoto
            ) {
                enableDebugOptionToastAttachedPhoto = enableDebugOptionToastAttachedPhoto.not()
                config.enableDebugOptionToastAttachedPhoto = enableDebugOptionToastAttachedPhoto
            }
            var enableDebugOptionToastNotificationInfo by remember { mutableStateOf(currentContext.config.enableDebugOptionToastNotificationInfo) }
            SwitchCard(
                "Notification Info",
                null,
                settingCardModifier,
                enableDebugOptionToastNotificationInfo
            ) {
                enableDebugOptionToastNotificationInfo = enableDebugOptionToastNotificationInfo.not()
                config.enableDebugOptionToastNotificationInfo = enableDebugOptionToastNotificationInfo
            }
            var enableDebugOptionToastReviewFlowInfo by remember { mutableStateOf(currentContext.config.enableDebugOptionToastReviewFlowInfo) }
            SwitchCard(
                "ReviewFlow Info",
                null,
                settingCardModifier,
                enableDebugOptionToastReviewFlowInfo
            ) {
                enableDebugOptionToastReviewFlowInfo = enableDebugOptionToastReviewFlowInfo.not()
                config.enableDebugOptionToastReviewFlowInfo = enableDebugOptionToastReviewFlowInfo
            }
            var enableDebugOptionToastPhotoHighlightUpdateTime by remember { mutableStateOf(currentContext.config.enableDebugOptionToastPhotoHighlightUpdateTime) }
            SwitchCard(
                "Photo-Highlight Update Time",
                null,
                settingCardModifier,
                enableDebugOptionToastPhotoHighlightUpdateTime
            ) {
                enableDebugOptionToastPhotoHighlightUpdateTime = enableDebugOptionToastPhotoHighlightUpdateTime.not()
                config.enableDebugOptionToastPhotoHighlightUpdateTime = enableDebugOptionToastPhotoHighlightUpdateTime
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun CustomLauncher(
        modifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        CategoryTitleCard(title = "Custom Launcher", marginTop = 0)
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow,
            modifier = Modifier
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun Coroutine(
        modifier: Modifier,
        maxItemsInEachRow: Int,
        viewModel: BaseDevViewModel
    ) {
        CategoryTitleCard(title = "Coroutine")
        val coroutine1Console by viewModel.coroutine1Console.observeAsState("")
        val state = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        fun updateConsole(message: String, tag: String = Thread.currentThread().name) {
            mViewModel.coroutine1Console.value = coroutine1Console.plus("$tag: $message\n")
            coroutineScope.launch {
                state.animateScrollBy(Float.MAX_VALUE)
            }
        }
        ScrollableCard(
            "Coroutine Info",
            coroutine1Console,
            Modifier
                .fillMaxWidth()
//                        .height(100.dp)
//                        .verticalScroll(state)
            ,
            state
        )
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            SimpleCard(
                "[T1] Start",
                null,
                modifier = modifier,
            ) {
                if (mCoroutineJob1?.isActive == true) {
                    updateConsole("Job has already started.")
                } else {
                    mCoroutineJob1 =
                        GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                            for (i in 1..50) {
                                if (isActive) {
                                    val currentThreadName = Thread.currentThread().name
                                    withContext(Dispatchers.Main) {
                                        updateConsole(
                                            i.toString(),
                                            currentThreadName
                                        )
                                    }
                                    delay(500)
                                }
                            }
                        }
                }
            }
            SimpleCard(
                "[T1] Stop",
                null,
                modifier = modifier,
            ) {
                if (mCoroutineJob1?.isActive == true) {
                    runBlocking { mCoroutineJob1?.cancelAndJoin() }
                } else {
                    updateConsole("The job has been canceled")
                }
            }
            SimpleCard(
                "[T1] Job Status",
                null,
                modifier = modifier,
            ) {
                mCoroutineJob1?.let {
                    when (it.isActive) {
                        true -> updateConsole("On")
                        false -> updateConsole("Off")
                    }
                } ?: run {
                    updateConsole("Coroutine is not initialized.")
                }
            }
            SimpleCard(
                "[T2] Multiple",
                null,
                modifier = modifier,
            ) {
                for (k in 1..3) {
                    GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                        for (i in 1..10) {
                            val currentThreadName = Thread.currentThread().name
                            runOnUiThread { updateConsole(i.toString(), currentThreadName) }
                            delay(100)
                        }
                    }
                }
            }
            SimpleCard(
                "[T3] runBlocking",
                null,
                modifier = modifier,
            ) {
                updateConsole("1")
                runBlocking {
                    launch {
                        updateConsole("3")
                        delay(2000)
                        updateConsole("4")
                    }
                    updateConsole("2")
                }
            }
            SimpleCard(
                "[T4] CoroutineScope",
                null,
                modifier = modifier,
            ) {
                updateConsole("1")
                CoroutineScope(Dispatchers.IO).launch {
                    val name = Thread.currentThread().name
                    withContext(Dispatchers.Main) { updateConsole("3", name) }
                    delay(2000)
                    withContext(Dispatchers.Main) { updateConsole("4", name) }
                }
                updateConsole("2")
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    protected fun FingerPrint(
        modifier: Modifier,
        maxItemsInEachRow: Int
    ) {
        CategoryTitleCard(title = "Finger Print")
        FlowRow(
            maxItemsInEachRow = maxItemsInEachRow
        ) {
            SimpleCard(
                "Fingerprint",
                null,
                modifier = modifier,
            ) { startListeningFingerprint(this@BaseDevActivity) }
            SimpleCard(
                "Biometric",
                null,
                modifier = modifier,
            ) { startListeningBiometric(this@BaseDevActivity) }
        }
    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun CustomLauncherPreview() {
//        AppTheme {
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                CustomLauncher(settingCardModifier, 1)
//            }
//        }
//    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun NotificationPreview() {
//        AppTheme {
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                Notification(settingCardModifier, 1)
//            }
//        }
//    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun AlertDialogPreview() {
//        AppTheme {
//            val configuration = LocalConfiguration.current
//            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                AlertDialog(settingCardModifier, maxItemsInEachRow)
//            }
//        }
//    }

    @Preview(heightDp = 1200)
    @Composable
    protected fun EtcPreview() {
        AppTheme {
            val configuration = LocalConfiguration.current
            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
            Column {
                val settingCardModifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                Etc(settingCardModifier, maxItemsInEachRow.minus(1), viewModel())
            }
        }
    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun LocationManagerPreview() {
//        AppTheme {
//            val configuration = LocalConfiguration.current
//            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                LocationManager(settingCardModifier, maxItemsInEachRow, viewModel())
//            }
//        }
//    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun DebugToastPreview() {
//        AppTheme {
//            val configuration = LocalConfiguration.current
//            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                DebugToast(settingCardModifier, maxItemsInEachRow)
//            }
//        }
//    }

//    @Preview(heightDp = 600)
//    @Composable
//    protected fun CoroutinePreview() {
//        AppTheme {
//            val configuration = LocalConfiguration.current
//            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                Coroutine(settingCardModifier, maxItemsInEachRow, viewModel())
//            }
//        }
//    }

//    @Preview(heightDp = 300)
//    @Composable
//    protected fun FingerPrintPreview() {
//        AppTheme {
//            val configuration = LocalConfiguration.current
//            val maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
//            Column {
//                val settingCardModifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                FingerPrint(settingCardModifier, maxItemsInEachRow)
//            }
//        }
//    }



    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun updateGPSProvider() {
        when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            true -> {
                // hasGPSPermissions 대체가능하나 lint error 때문에 직접 체크
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0F,
                        mGPSLocationListener
                    )
                }
            }

            false -> makeSnackBar("GPS Provider is not available.")
        }
    }

    private fun updateNetWorkProvider() {
        when (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            true -> {
                if (checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION))) {
                    mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0F,
                        mNetworkLocationListener
                    )
                }
            }
            false -> makeSnackBar("Network Provider is not available.")
        }
    }

    private fun createNotificationBigTextStyle() {
        val notification = NotificationInfo(
            R.drawable.ic_done,
            useActionButton = true,
            mNotificationCount
        )
        if (ActivityCompat.checkSelfPermission(
                this@BaseDevActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this@BaseDevActivity)
                .notify(notification.id, createNotification(notification).also {
                    val contentTitle = "[${notification.id}] BigTextStyle Title"
                    val contentText =
                        "contentText 영역 입니다. 긴 메시지를 표현하려면 NotificationCompat.BigTextStyle()을 사용하면 됩니다."
                    it.setStyle(
                        NotificationCompat.BigTextStyle()
                            .setSummaryText("[BigTextStyle] $contentTitle")
                            .bigText("[BigTextStyle] $contentText")
                    )
                    it.setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            notification.largeIconResourceId
                        )
                    )
                }.build())
        }
    }



    private fun createNotificationCustomView(context: Context) {
        val notification = NotificationInfo(
            R.drawable.ic_diary_backup_local,
            useActionButton = true,
            mNotificationCount++
        )
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap =
                Glide
                    .with(context).asBitmap()
                    .load(R.drawable.bg_travel_4514822_1280)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(context.dpToPixel(5F))
                    )
                    .submit(200, 200).get()
            withContext(Dispatchers.Main) {
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@BaseDevActivity)
                        .notify(notification.id, createNotification(notification, bitmap).apply {
                            setStyle(NotificationCompat.DecoratedCustomViewStyle())
                            setCustomContentView(
                                RemoteViews(
                                    applicationContext.packageName,
                                    R.layout.partial_notification_contents
                                ).apply {
                                    setTextViewText(
                                        R.id.text_notification_content,
                                        "[${notification.id}] This package is part of the Android support library which is no longer maintained. The support library has been superseded by AndroidX which is part of Jetpack. We recommend using the AndroidX libraries in all new projects."
                                    )
                                    setImageViewBitmap(R.id.img_notification_content, bitmap)
                                })
                            setCustomBigContentView(
                                RemoteViews(
                                    applicationContext.packageName,
                                    R.layout.partial_notification
                                ).apply {
                                    setImageViewResource(
                                        R.id.img_notification_content,
                                        R.drawable.bg_travel_4514822_1280
                                    )
                                })
                            //                                        setColor(config.primaryColor)
                            //                                        setColorized(true)
                            //                                        setLargeIcon(BitmapFactory.decodeResource(resources, notification.largeIconResourceId))
                            addAction(
                                R.drawable.ic_easydiary,
                                "Toast",
                                PendingIntent.getService(
                                    this@BaseDevActivity,
                                    notification.id /*Private request code for the sender*/,
                                    Intent(
                                        this@BaseDevActivity,
                                        NotificationService::class.java
                                    ).apply {
                                        action = BaseNotificationService.ACTION_DEV_TOAST
                                        putExtra(
                                            NOTIFICATION_ID,
                                            notification.id /*An identifier for this notification unique within your application.*/
                                        )
                                    },
                                    pendingIntentFlag()
                                )
                            )
                        }.build())
                }
            }
        }
    }

    private fun createNotificationBasicWithBitmapIcon(context: Context) {
        val notification = NotificationInfo(
            R.drawable.ic_diary_writing,
            useActionButton = true,
            id = mNotificationCount++
        )
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap =
                Glide
                    .with(context).asBitmap()
                    .load(R.drawable.bg_travel_4514822_1280)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(context.dpToPixel(5F))
                    )
                    .submit(200, 200).get()
            withContext(Dispatchers.Main) {
                if (ActivityCompat.checkSelfPermission(
                        this@BaseDevActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@BaseDevActivity)
                        .notify(notification.id, createNotification(notification).also {
                            val contentTitle = "[${notification.id}] Basic Notification"
                            val contentText =
                                "기본 알림 메시지 입니다. 기본 알림용 메시지에 내용이 너무 많으면 메시지가 정상적으로 보이지 않을 수 있습니다."
                            it.setContentTitle(contentTitle)
                            it.setContentText(contentText)
                            it.setLargeIcon(bitmap)
                        }.build())
                }
            }
        }
    }

    private fun createNotificationBasic() {
        val notification = NotificationInfo(
            R.drawable.ic_diary_writing,
            useActionButton = true,
            id = mNotificationCount++
        )
        if (ActivityCompat.checkSelfPermission(
                this@BaseDevActivity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this@BaseDevActivity)
                .notify(notification.id, createNotification(notification).also {
                    val contentTitle = "[${notification.id}] Basic Notification"
                    val contentText =
                        "기본 알림 메시지 입니다. 기본 알림용 메시지에 내용이 너무 많으면 메시지가 정상적으로 보이지 않을 수 있습니다."
                    it.setContentTitle(contentTitle)
                    it.setContentText(contentText)
                    it.setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            notification.largeIconResourceId
                        )
                    )
                    it.setLargeIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            notification.largeIconResourceId
                        )
                    )
                }.build())
        }
    }

    protected fun createBaseCardView(cardTitle: String, descriptionTag: String? = null, vararg buttons: Button): CardView {
        val titleContextTheme = ContextThemeWrapper(this, R.style.SettingsTitle)
        val descriptionContextTheme = ContextThemeWrapper(this, R.style.SettingsSummary)
        val cardContextTheme = ContextThemeWrapper(this@BaseDevActivity, R.style.AppCard_Settings)
        val linearContextTheme = ContextThemeWrapper(this@BaseDevActivity, R.style.LinearLayoutVertical)
        return CardView(cardContextTheme).apply {
            addView(
                LinearLayout(linearContextTheme).apply {
                    addView(MyTextView(titleContextTheme).apply {
                        text = cardTitle
                    })
                    addView(FlexboxLayout(this@BaseDevActivity).apply {
                        flexDirection = FlexDirection.ROW
                        flexWrap = FlexWrap.WRAP
                        buttons.forEach { addView(it) }
                    })
                    descriptionTag?.let {
                        addView(MyTextView(descriptionContextTheme).apply {
                            tag = descriptionTag
                        })
                    }
                }
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
                viewModel.locationInfo.value = info
            }
        }
        when (hasGPSPermissions()) {
            true -> setLocationInfo()
            false -> {
                acquireGPSPermissions(mRequestLocationSourceLauncher) {
                    setLocationInfo()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createNotification(notificationInfo: NotificationInfo, bitmap: Bitmap? = null): NotificationCompat.Builder {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", getString(R.string.notification_channel_name_dev), importance)
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev").apply {
            setDefaults(Notification.DEFAULT_ALL)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.ic_easydiary)
            setOnlyAlertOnce(true)
            setOngoing(false)
            setAutoCancel(true)
            setContentIntent(
                PendingIntent.getActivity(
                    this@BaseDevActivity,
                    notificationInfo.id /*Private request code for the sender*/,
                    Intent(this@BaseDevActivity, DiaryMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(NOTIFICATION_ID, notificationInfo.id)
                        putExtra(
                            NOTIFICATION_INFO,
                            "Test Notification Count: $mNotificationCount"
                        )
                    },
                    pendingIntentFlag()
                )
            )
        }

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                R.drawable.ic_easydiary,
                getString(R.string.dismiss),
                PendingIntent.getService(
                    this,
                    notificationInfo.id /*Private request code for the sender*/,
                    Intent(this, NotificationService::class.java).apply {
                        action = BaseNotificationService.ACTION_DEV_DISMISS
                        putExtra(
                            NOTIFICATION_ID,
                            notificationInfo.id /*An identifier for this notification unique within your application.*/
                        )
                    },
                    pendingIntentFlag()
                )
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
    var id: Int = 0
)


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/
//fun fun1(param1: String, block: (responseData: String) -> String): String {
//    println(param1)
//    return block("")
//}
//
//fun fun2(param1: String, block: (responseData: String) -> Boolean): String {
//    println(param1)
//    var blockReturn = block(param1)
//    return param1
//}
//
//fun test1() {
//    val result = fun1("banana") { responseData ->
//        "data: $responseData"
//    }
//    println(result)
//}






