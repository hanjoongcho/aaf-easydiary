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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.views.MyTextView
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningFingerprint
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
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

        setupActionLog()
        setupNotification()
        setupLocation()
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
     *   test functions
     *
     ***************************************************************************************************/
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
                // Dialog
                createBaseCardView(
                    "Dialog", null
                    , Button(this@BaseDevActivity).apply {
                        text = "알림(DEFAULT)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, null, DialogMode.DEFAULT, false)
                        }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "알림(INFO)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, null, DialogMode.INFO, false)
                        }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "알림(WARNING)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, null, DialogMode.WARNING, false)
                        }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "알림(ERROR)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, null, DialogMode.ERROR, false)
                        }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "알림(SETTING)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, null, DialogMode.SETTING, false)
                        }
                    }
                    , Button(this@BaseDevActivity).apply {
                        text = "확인(INFO)"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            showAlertDialog("message", null, { _,_ -> }, DialogMode.INFO)
                        }
                    }
                )
            )
            linearDevContainer.addView(
                // Setting Toast
                createBaseCardView(
                    "Toast Message", null, Button(this@BaseDevActivity).apply {
                        text = "Location Toast"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionToastLocation =
                                !config.enableDebugOptionToastLocation
                            makeSnackBar("Status: ${config.enableDebugOptionToastLocation}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Attached Photo Toast"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionToastAttachedPhoto =
                                !config.enableDebugOptionToastAttachedPhoto
                            makeSnackBar("Status: ${config.enableDebugOptionToastAttachedPhoto}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Notification Info"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionToastNotificationInfo =
                                !config.enableDebugOptionToastNotificationInfo
                            makeSnackBar("Status: ${config.enableDebugOptionToastNotificationInfo}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "ReviewFlow Info"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionToastReviewFlowInfo =
                                !config.enableDebugOptionToastReviewFlowInfo
                            makeSnackBar("Status: ${config.enableDebugOptionToastReviewFlowInfo}")
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "Photo-Highlight Update Time"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            config.enableDebugOptionToastPhotoHighlightUpdateTime =
                                !config.enableDebugOptionToastPhotoHighlightUpdateTime
                            makeSnackBar("Status: ${config.enableDebugOptionToastPhotoHighlightUpdateTime}")
                        }
                    }
                )
            )
            linearDevContainer.addView(
                // Setting Custom Launcher
                createBaseCardView(
                    "Custom Launcher"
                    , null, Button(this@BaseDevActivity).apply {
                        text = "EasyDiary Launcher"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { toggleLauncher(Launcher.EASY_DIARY) }
                    },
                    Button(this@BaseDevActivity).apply {
                        text = "Dark Launcher"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { toggleLauncher(Launcher.DARK) }
                    },
                    Button(this@BaseDevActivity).apply {
                        text = "Green Launcher"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { toggleLauncher(Launcher.GREEN) }
                    },
                    Button(this@BaseDevActivity).apply {
                        text = "Debug Launcher"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener { toggleLauncher(Launcher.DEBUG) }
                    }
                )
            )
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
                    }
                )
            )
        }
    }

    private fun setupNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mBinding.linearDevContainer.addView(
                // Notification
                createBaseCardView(
                    "Notification", null, Button(this@BaseDevActivity).apply {
                        text = "Basic"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            val notification = NotificationInfo(
                                R.drawable.ic_diary_writing,
                                useActionButton = true,
                                id = mNotificationCount++
                            )
                            NotificationManagerCompat.from(this@BaseDevActivity).notify(notification.id, createNotification(notification).also {
                                val contentTitle = "[${notification.id}] Basic Notification"
                                val contentText = "기본 알림 메시지 입니다. 기본 알림용 메시지에 내용이 너무 많으면 메시지가 정상적으로 보이지 않을 수 있습니다."
                                it.setContentTitle(contentTitle)
                                it.setContentText(contentText)
                                it.setLargeIcon(BitmapFactory.decodeResource(resources, notification.largeIconResourceId))
                            }.build())
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "CustomContentView"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            val notification = NotificationInfo(
                                R.drawable.ic_diary_backup_local,
                                useActionButton = true,
                                mNotificationCount++)
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
                                    NotificationManagerCompat.from(this@BaseDevActivity).notify(notification.id, createNotification(notification, bitmap).apply {
                                        setStyle(NotificationCompat.DecoratedCustomViewStyle())
                                        setCustomContentView(RemoteViews(applicationContext.packageName, R.layout.partial_notification_contents).apply {
                                            setTextViewText(R.id.text_notification_content, "[${notification.id}] This package is part of the Android support library which is no longer maintained. The support library has been superseded by AndroidX which is part of Jetpack. We recommend using the AndroidX libraries in all new projects.")
                                            setImageViewBitmap(R.id.img_notification_content, bitmap)
                                        })
                                        setCustomBigContentView(RemoteViews(applicationContext.packageName, R.layout.partial_notification).apply {
                                            setImageViewResource(R.id.img_notification_content, R.drawable.bg_travel_4514822_1280)
                                        })
//                                        setColor(config.primaryColor)
//                                        setColorized(true)
//                                        setLargeIcon(BitmapFactory.decodeResource(resources, notification.largeIconResourceId))
                                        addAction(
                                            R.drawable.ic_easydiary,
                                            "Toast",
                                            PendingIntent.getService(this@BaseDevActivity, notification.id /*Private request code for the sender*/, Intent(this@BaseDevActivity, NotificationService::class.java).apply {
                                                action = BaseNotificationService.ACTION_DEV_TOAST
                                                putExtra(NOTIFICATION_ID, notification.id /*An identifier for this notification unique within your application.*/)
                                            }, pendingIntentFlag())
                                        )
                                    }.build())
                                }
                            }
                        }
                    }, Button(this@BaseDevActivity).apply {
                        text = "BigTextStyle"
                        layoutParams = mFlexboxLayoutParams
                        setOnClickListener {
                            val notification = NotificationInfo(
                                R.drawable.ic_done,
                                useActionButton = true,
                                mNotificationCount)
                            NotificationManagerCompat.from(this@BaseDevActivity).notify(notification.id, createNotification(notification).also {
                                val contentTitle = "[${notification.id}] BigTextStyle Title"
                                val contentText = "contentText 영역 입니다. 긴 메시지를 표현하려면 NotificationCompat.BigTextStyle()을 사용하면 됩니다."
                                it.setStyle(NotificationCompat.BigTextStyle().setSummaryText("[BigTextStyle] $contentTitle").bigText("[BigTextStyle] $contentText"))
                                it.setLargeIcon(BitmapFactory.decodeResource(resources, notification.largeIconResourceId))
                            }.build())
                        }
                    }
                )
            )
        }
    }

    private fun setupActionLog() {
        mBinding.clearLog.setOnClickListener {
            EasyDiaryDbHelper.deleteActionLogAll()
            updateActionLog()
        }
        updateActionLog()
    }

    private fun updateActionLog() {
        val actionLogs: List<ActionLog> = EasyDiaryDbHelper.findActionLogAll()
        val sb = StringBuilder()
        actionLogs.map {
            sb.append("${it.className}-${it.signature}-${it.key}: ${it.value}\n")
        }
        mBinding.actionLog.text = sb.toString()
    }

    @SuppressLint("MissingPermission")
    private fun setupLocation() {
        mBinding.linearDevContainer.addView(
            // Location Manager
            createBaseCardView(
                "Location Manager", TAG_LOCATION_MANAGER,
                Button(this@BaseDevActivity).apply {
                    text = "Last-Location"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener { updateLocation() }
                },
                Button(this@BaseDevActivity).apply {
                    text = "Update-GPS"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
                        when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            true -> {
                                mLocationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    0,
                                    0F,
                                    mGPSLocationListener
                                )
                            }

                            false -> makeSnackBar("GPS Provider is not available.")
                        }
                    }
                },
                Button(this@BaseDevActivity).apply {
                    text = "Update-Network"
                    layoutParams = mFlexboxLayoutParams
                    setOnClickListener {
                        val locationManager =
                            getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        when (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            true -> {
                                mLocationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    0,
                                    0F,
                                    mNetworkLocationListener
                                )
                            }

                            false -> makeSnackBar("Network Provider is not available.")
                        }
                    }
                },
            )
        )
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


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun updateLocation() {
        fun setLocationInfo() {
            getLastKnownLocation()?.let {
                var info = "Longitude: ${it.longitude}\nLatitude: ${it.latitude}\n"
                getFromLocation(it.latitude, it.longitude, 1)?.let { address ->
                    if (address.isNotEmpty()) {
                        info += fullAddress(address[0])
                    }
                }
                mBinding.root.findViewWithTag<MyTextView>(TAG_LOCATION_MANAGER).text = info
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

    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_INFO = "notification_info"
        const val TAG_LOCATION_MANAGER = "tag_location_manager"
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






