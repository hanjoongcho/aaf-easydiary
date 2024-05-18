package me.blog.korn123.easydiary.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RemoteViews
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.Release
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
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.api.models.Contents
import me.blog.korn123.easydiary.api.services.GitHubRepos
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.dialogs.ActionLogDialog
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog
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
import me.blog.korn123.easydiary.extensions.pendingIntentFlag
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.extensions.startReviewFlow
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.ui.components.CategoryTitleCard
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
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
    private var mNotificationCount = 9000
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
    protected lateinit var mBinding: ActivityBaseDevBinding
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
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_dev)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mViewModel

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }

        mBinding.run {
            composeView.setContent {
                AppTheme {
                    DevTools(false)
                }
            }
        }

        setupCoroutine()
        setupTestFunction()
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
    fun DevTools(isPreview: Boolean = false) {
        val currentContext = LocalContext.current
        val pixelValue = currentContext.config.settingFontSize
        val density = LocalDensity.current
        val currentTextUnit = with (density) {
            val temp = pixelValue.toDp()
            temp.toSp()
        }

        Column {
            val settingCardModifier = Modifier
                .fillMaxWidth()
                .weight(1f)

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Etc.")
            Row {
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Action Log",
                    "Open Action Log",
                    settingCardModifier,
                ) {
                    val actionLogs: List<ActionLog> = EasyDiaryDbHelper.findActionLogAll()
                    ActionLogDialog(this@BaseDevActivity, actionLogs) { EasyDiaryDbHelper.deleteActionLogAll() }
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "GitHub MarkDown Page",
                    "🛸 SYNC",
                    settingCardModifier,
                ) { syncMarkDown() }
            }

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Notification")
            FlowRow(
                modifier = Modifier,
                maxItemsInEachRow = 2
            ) {
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Notification-01",
                    "Basic",
                    settingCardModifier,
                ) {
                    createNotificationBasic()
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Notification-02",
                    "Basic(Bitmap Icon)",
                    settingCardModifier,
                ) {
                    createNotificationBasicWithBitmapIcon(this@BaseDevActivity)
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Notification-03",
                    "CustomContentView",
                    settingCardModifier,
                ) {
                    createNotificationCustomView(this@BaseDevActivity)
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Notification-04",
                    "BigTextStyle",
                    settingCardModifier,
                ) {
                    createNotificationBigTextStyle()
                }
            }

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Location Manager")
            if (!isPreview) {
                val locationInfo by mViewModel.locationInfo.observeAsState("")
                SimpleCard(
                    currentTextUnit,
                    false,
                    "Location Info",
                    locationInfo,
                    Modifier.fillMaxWidth(),
                ) {}
            }
            FlowRow(
                modifier = Modifier,
                maxItemsInEachRow = 2
            ) {
                var enableDebugOptionToastLocation by remember { mutableStateOf(currentContext.config.enableDebugOptionToastLocation) }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    "Toast Message",
                    "Location Toast",
                    settingCardModifier,
                    enableDebugOptionToastLocation
                ) {
                    enableDebugOptionToastLocation = enableDebugOptionToastLocation.not()
                    config.enableDebugOptionToastLocation = enableDebugOptionToastLocation
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Location Manager",
                    "Last-Location",
                    settingCardModifier,
                ) { updateLocation() }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Location Manager",
                    "Update-GPS",
                    settingCardModifier,
                ) {
                    updateGPSProvider()
                }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Location Manager",
                    "Update-Network",
                    settingCardModifier,
                ) {
                    updateNetWorkProvider()
                }
            }

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Alert Dialog")
            FlowRow(
                modifier = Modifier,
                maxItemsInEachRow = 2
            ) {
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "알림(DEFAULT)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, null, DialogMode.DEFAULT, false) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "알림(INFO)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, null, DialogMode.INFO, false) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "알림(WARNING)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, null, DialogMode.WARNING, false) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "알림(ERROR)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, null, DialogMode.ERROR, false) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "알림(SETTING)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, null, DialogMode.SETTING, false) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dialog",
                    "확인(INFO)",
                    settingCardModifier,
                ) { showAlertDialog("message", null, { _,_ -> }, DialogMode.INFO) }
            }

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Debug Toast")
            FlowRow(
                maxItemsInEachRow = 2
            ) {
                var enableDebugOptionToastAttachedPhoto by remember { mutableStateOf(currentContext.config.enableDebugOptionToastAttachedPhoto) }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
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
                    currentTextUnit,
                    isPreview,
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
                    currentTextUnit,
                    isPreview,
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
                    currentTextUnit,
                    isPreview,
                    "Photo-Highlight Update Time",
                    null,
                    settingCardModifier,
                    enableDebugOptionToastPhotoHighlightUpdateTime
                ) {
                    enableDebugOptionToastPhotoHighlightUpdateTime = enableDebugOptionToastPhotoHighlightUpdateTime.not()
                    config.enableDebugOptionToastPhotoHighlightUpdateTime = enableDebugOptionToastPhotoHighlightUpdateTime
                }
                var enableDebugOptionVisibleChartStock by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleChartStock) }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    "Stock",
                    null,
                    settingCardModifier,
                    enableDebugOptionVisibleChartStock
                ) {
                    enableDebugOptionVisibleChartStock = enableDebugOptionVisibleChartStock.not()
                    config.enableDebugOptionVisibleChartStock = enableDebugOptionVisibleChartStock
                }
                var enableDebugOptionVisibleChartWeight by remember { mutableStateOf(currentContext.config.enableDebugOptionVisibleChartWeight) }
                SwitchCard(
                    currentTextUnit,
                    isPreview,
                    "Weight",
                    null,
                    settingCardModifier,
                    enableDebugOptionVisibleChartWeight
                ) {
                    enableDebugOptionVisibleChartWeight = enableDebugOptionVisibleChartWeight.not()
                    config.enableDebugOptionVisibleChartWeight = enableDebugOptionVisibleChartWeight
                }
            }

            CategoryTitleCard(textUnit = currentTextUnit, isPreview = isPreview, title = "Custom Launcher")
            FlowRow(
                maxItemsInEachRow = 2
            ) {
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "EasyDiary Launcher",
                    null,
                    settingCardModifier,
                ) { toggleLauncher(Launcher.EASY_DIARY) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Dark Launcher",
                    null,
                    settingCardModifier,
                ) { toggleLauncher(Launcher.DARK) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Green Launcher",
                    null,
                    settingCardModifier,
                ) { toggleLauncher(Launcher.GREEN) }
                SimpleCard(
                    currentTextUnit,
                    isPreview,
                    "Debug Launcher",
                    null,
                    settingCardModifier,
                ) { toggleLauncher(Launcher.DEBUG) }
            }
        }
    }

    @Preview(heightDp = 2000)
    @Composable
    private fun DevToolsPreview() {
        AppTheme {
            DevTools(true)
        }
    }


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

    private fun setupTestFunction() {
        mBinding.run {
            linearDevContainer.addView(
                // Biometric authentication
                createBaseCardView(
                    "Finger Print"
                    , null, Button(this@BaseDevActivity).apply {
                        text = "Fingerprint"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { startListeningFingerprint(this@BaseDevActivity) }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "Biometric"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { startListeningBiometric(this@BaseDevActivity) }
                    }
                )
            )
            linearDevContainer.addView(
                // Setting Custom Chart
                createBaseCardView(
                    "Custom Chart", null, Button(this@BaseDevActivity).apply {
                        text = "Stock"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleChartStock =
                                !config.enableDebugOptionVisibleChartStock
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleChartStock}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Weight"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleChartWeight =
                                !config.enableDebugOptionVisibleChartWeight
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleChartWeight}")
                        }
                    }
                )
            )
            linearDevContainer.addView(
                // ETC.
                createBaseCardView(
                    "ETC.", null,
                    Button(this@BaseDevActivity).apply {
                        text = "ReviewFlow"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { startReviewFlow() }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Reset Showcase"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
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
                    }, Button(this@BaseDevActivity).apply {
                        text = "Reset Font Size"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
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
                    }, Button(this@BaseDevActivity).apply {
                        text = "Check Force Release URL"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
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
                    }, Button(this@BaseDevActivity).apply {
                        text = "InApp Browser"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            val customTabsIntent =
                                CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build()
                            customTabsIntent.launchUrl(
                                this@BaseDevActivity,
                                Uri.parse("https://github.com/AAFactory/aafactory-commons")
                            )
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Display Diary Sequence"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleDiarySequence =
                                !config.enableDebugOptionVisibleDiarySequence
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleDiarySequence}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Display Alarm Sequence"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleAlarmSequence =
                                !config.enableDebugOptionVisibleAlarmSequence
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleAlarmSequence}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text ="Clear-Unused-Photo"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            val localPhotoBaseNames = arrayListOf<String>()
                            val unUsedPhotos = arrayListOf<String>()
                            val targetFiles = File(EasyDiaryUtils.getApplicationDataDirectory(this@BaseDevActivity) + DIARY_PHOTO_DIRECTORY)
                            targetFiles.listFiles()?.map {
                                localPhotoBaseNames.add(it.name)
                            }

                            EasyDiaryDbHelper.findPhotoUriAll().map { photoUriDto ->
                                if (!localPhotoBaseNames.contains(FilenameUtils.getBaseName(photoUriDto.getFilePath()))) {
                                    unUsedPhotos.add(FilenameUtils.getBaseName(photoUriDto.getFilePath()))
                                }
                            }
                            showAlertDialog(unUsedPhotos.size.toString(), null,
                                { _, _ -> }, DialogMode.WARNING
                            )
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text ="Font Preview Emoji"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleFontPreviewEmoji =
                                !config.enableDebugOptionVisibleFontPreviewEmoji
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleFontPreviewEmoji}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text ="Display Temporary Diary"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionVisibleTemporaryDiary =
                                !config.enableDebugOptionVisibleTemporaryDiary
                            makeSnackBar("Status: ${config.enableDebugOptionVisibleTemporaryDiary}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text ="PickMultipleVisualMedia"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            mPickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                )
            )
        }
    }

    private var mCoroutineJob1: Job? = null
    private fun setupCoroutine() {
        fun updateConsole(message: String, tag: String = Thread.currentThread().name) {
            mBinding.textCoroutine1Console.append("$tag: $message\n")
            mBinding.scrollCoroutine.post { mBinding.scrollCoroutine.fullScroll(View.FOCUS_DOWN) }
        }

        mBinding.linearDevContainer.addView(
            // Coroutine
            createBaseCardView(
                "Coroutine", null,
                Button(this@BaseDevActivity).apply {
                    text = "[T1] Start"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
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
                },
                Button(this@BaseDevActivity).apply {
                    text = "[T1] Stop"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
                        if (mCoroutineJob1?.isActive == true) {
                            runBlocking { mCoroutineJob1?.cancelAndJoin() }
                        } else {
                            updateConsole("The job has been canceled")
                        }
                    }
                },
                Button(this@BaseDevActivity).apply {
                    text = "[T1] Job Status"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
                        mCoroutineJob1?.let {
                            when (it.isActive) {
                                true -> updateConsole("On")
                                false -> updateConsole("Off")
                            }
                        } ?: run {
                            updateConsole("Coroutine is not initialized.")
                        }
                    }
                },
                Button(this@BaseDevActivity).apply {
                    text = "[T2] Multiple"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
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
                },
                Button(this@BaseDevActivity).apply {
                    text = "[T3] runBlocking"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
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
                },
                Button(this@BaseDevActivity).apply {
                    text = "[T4] CoroutineScope"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
                        updateConsole("1")
                        CoroutineScope(Dispatchers.IO).launch {
                            val name = Thread.currentThread().name
                            withContext(Dispatchers.Main) { updateConsole("3", name) }
                            delay(2000)
                            withContext(Dispatchers.Main) { updateConsole("4", name) }
                        }
                        updateConsole("2")
                    }
                },
            ).also {
                mBinding.scrollCoroutine.run {
                    (parent as ViewGroup).removeView(this)
                    (it.getChildAt(0) as ViewGroup).addView(this)
                }
            }
        )
    }

    private fun updateLocation() {
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
                mViewModel.locationInfo.value = info
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

    private fun syncMarkDown() {
        mBinding.partialSettingsProgress.progressContainer.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val baseUrl = "https://api.github.com"
            var token: String? = null
            var tokenInfo: List<Diary>?
            var size = 0
            EasyDiaryDbHelper.getTemporaryInstance().run {
                tokenInfo = EasyDiaryDbHelper.findDiary("GitHub Personal Access Token", false, 0, 0, 0, this)
                tokenInfo?.let {
                    size = it.size
                    if (size > 0) token = it[0].contents
                }
                close()
            }

            if (size != 1) {
                runOnUiThread { makeToast("No Data") }
            } else {
                val retrofitApi: Retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val downloadApi: Retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                val retrofitApiService = retrofitApi.create(GitHubRepos::class.java)
                val downloadApiService = downloadApi.create(GitHubRepos::class.java)
                fun fetchContents(path: String, usingPathTitle: Boolean, symbolSequence: Int = SYMBOL_USER_CUSTOM_SYNC) {
                    val call = retrofitApiService.findContents(token!!, "hanjoongcho", "self-development", path)
                    val response = call.execute()
                    val contentsItems: List<Contents>? = response.body()
                    contentsItems?.forEach { content ->
                        if (content.download_url == null) {
                            fetchContents(content.path, usingPathTitle, symbolSequence)
                        } else {
                            EasyDiaryDbHelper.getTemporaryInstance().run {
                                val title = when (usingPathTitle) {
                                    true -> content.path
                                    false -> if (usingPathTitle) content.name else content.name.split(".")[0]
                                }

                                val items = EasyDiaryDbHelper.findMarkdownSyncTargetDiary(title, this)
                                if (items.size == 1) {
                                    runOnUiThread {
                                        mBinding.partialSettingsProgress.message.text = "Sync ${content.name}…"
                                    }
                                    val re = downloadApiService.downloadContents(token!!, content.download_url).execute()
                                    val diary = items[0]
                                    this.beginTransaction()
                                    diary.contents = re.body()
                                    diary.weather = symbolSequence
                                    this.commitTransaction()
                                } else if (items.isEmpty()) {
                                    runOnUiThread {
                                        mBinding.partialSettingsProgress.message.text = "Download ${content.name}…"
                                    }
                                    val re = downloadApiService.downloadContents(token!!, content.download_url).execute()
                                    EasyDiaryDbHelper.insertDiary(Diary(
                                        BaseDiaryEditingActivity.DIARY_SEQUENCE_INIT,
                                        System.currentTimeMillis()
                                        , title
                                        , re.body()!!
                                        , symbolSequence
                                        ,true
                                    ), this)
                                }
                                this.close()
                            }
                        }
                    }
                }
                fetchContents("dev", true)
                fetchContents("etc", true)
                fetchContents("life", true)
                fetchContents("stock/KOSPI", false, 10014)
                fetchContents("stock/KOSDAQ", false, 10014)
                fetchContents("stock/knowledge", true)
                withContext(Dispatchers.Main) {
                    mBinding.partialSettingsProgress.progressContainer.visibility = View.GONE
                }
            }
        }
    }



    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_INFO = "notification_info"
        const val TAG_LOCATION_MANAGER = "tag_location_manager"
        const val SYMBOL_USER_CUSTOM_SYNC = 10025
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






