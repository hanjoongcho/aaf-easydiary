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
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RemoteViews
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.flexbox.FlexboxLayout
import com.simplemobiletools.commons.helpers.isOreoPlus
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningBiometric
import me.blog.korn123.commons.utils.BiometricUtils.Companion.startListeningFingerprint
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
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
    private var mNotificationCount = 0
    private val mViewModel: BaseDevViewModel by viewModels()
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mNetworkLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            makeToast("Network location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mGPSLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            makeToast("GPS location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mRequestLocationSourceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.")
    }
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
        setupClearUnusedPhoto()
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
    private fun setupTestFunction() {
        mBinding.run {
            buttonReviewFlow.setOnClickListener { startReviewFlow() }
            buttonResetShowcase.setOnClickListener {
                getSharedPreferences("showcase_internal", MODE_PRIVATE).run {
                    edit().putBoolean("hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER", false).apply()
                    edit().putBoolean("hasShot$SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER", false).apply()
                    edit().putBoolean("hasShot$SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER", false).apply()
                    edit().putBoolean("hasShot$SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER", false).apply()
                }
            }
            buttonFingerprint.setOnClickListener {
                startListeningFingerprint(this@BaseDevActivity)
            }
            buttonBiometric.setOnClickListener {
                startListeningBiometric(this@BaseDevActivity)
            }
            buttonEasyDiaryLauncher.setOnClickListener {
                toggleLauncher(Launcher.EASY_DIARY)
            }

            buttonDarkLauncher.setOnClickListener {
                toggleLauncher(Launcher.DARK)
            }

            buttonGreenLauncher.setOnClickListener {
                toggleLauncher(Launcher.GREEN)
            }

            buttonDebugLauncher.setOnClickListener {
                toggleLauncher(Launcher.DEBUG)
            }
            val flexboxLayoutParams = FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT).apply {
                flexGrow = 1F
            }
            flexDefaultContainer.run {
                addView(Button(this@BaseDevActivity).apply {
                    text = "Reset Font Size"
                    layoutParams = flexboxLayoutParams
                    setOnClickListener {
                        config.settingFontSize = spToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())
                        makeToast("DP:${dpToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())} , SP:${spToPixelFloatValue(UN_SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_SP.toFloat())}")
                    }
                })
                addView(Button(this@BaseDevActivity).apply {
                    text = "Check Force Release URL"
                    layoutParams = flexboxLayoutParams
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
                })
                addView(Button(this@BaseDevActivity).apply {
                    text = "InApp Browser"
                    layoutParams = flexboxLayoutParams
                    setOnClickListener {
                        val customTabsIntent = CustomTabsIntent.Builder().setUrlBarHidingEnabled(false).build()
                        customTabsIntent.launchUrl(this@BaseDevActivity, Uri.parse("https://github.com/AAFactory/aafactory-commons"))
                    }
                })
                addView(Button(this@BaseDevActivity).apply {
                    text = "Location Toast"
                    layoutParams = flexboxLayoutParams
                    setOnClickListener {
                        config.enableDebugOptionLocationToast = !config.enableDebugOptionLocationToast
                        makeSnackBar("Status: ${config.enableDebugOptionLocationToast}")
                    }
                })
                addView(Button(this@BaseDevActivity).apply {
                    text = "Attached Photo Toast"
                    layoutParams = flexboxLayoutParams
                    setOnClickListener {
                        config.enableDebugOptionAttachedPhotoToast = !config.enableDebugOptionAttachedPhotoToast
                        makeSnackBar("Status: ${config.enableDebugOptionAttachedPhotoToast}")
                    }
                })
            }
        }
    }

    private fun setupNotification() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mBinding.buttonNotification01.setOnClickListener {
                val notification = NotificationInfo(
                    R.drawable.ic_diary_writing,
                    useActionButton = true,
                    useCustomContentView = false,
                    id = mNotificationCount++
                )
                NotificationManagerCompat.from(this).notify(notification.id, createNotification(notification))
            }
            mBinding.buttonNotification02.setOnClickListener {
                val notification = NotificationInfo(
                    R.drawable.ic_diary_backup_local,
                    useActionButton = true,
                    useCustomContentView = true,
                    mNotificationCount++)
                NotificationManagerCompat.from(this).notify(notification.id, createNotification(notification))
            }
            mBinding.buttonNotification03.setOnClickListener {
                val notification = NotificationInfo(
                    R.drawable.ic_done,
                    useActionButton = true,
                    useCustomContentView = false,
                    100)
                NotificationManagerCompat.from(this).notify(notification.id, createNotification(notification))
            }
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

    private fun setupClearUnusedPhoto() {
        mBinding.buttonClearUnusedPhoto.setOnClickListener {
            val localPhotoBaseNames = arrayListOf<String>()
            val unUsedPhotos = arrayListOf<String>()
            val targetFiles = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY)
            targetFiles.listFiles()?.map {
                localPhotoBaseNames.add(it.name)
            }

            EasyDiaryDbHelper.findPhotoUriAll().map { photoUriDto ->
                if (!localPhotoBaseNames.contains(FilenameUtils.getBaseName(photoUriDto.getFilePath()))) {
                    unUsedPhotos.add(FilenameUtils.getBaseName(photoUriDto.getFilePath()))
                }
            }
            showAlertDialog(unUsedPhotos.size.toString(), null, true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocation() {
        mBinding.buttonRequestLastLocation.setOnClickListener {
            updateLocation()
        }

        mBinding.buttonUpdateGpsProvider.setOnClickListener {
            when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mGPSLocationListener)
                }
                false -> makeSnackBar("GPS Provider is not available.")
            }
        }

        mBinding.buttonUpdateNetworkProvider.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            when (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, mNetworkLocationListener)
                }
                false -> makeSnackBar("Network Provider is not available.")
            }
        }
    }

    private var mCoroutineJob1: Job? = null
    private fun setupCoroutine() {
        fun updateConsole(message: String, tag: String = Thread.currentThread().name) {
            mBinding.textCoroutine1Console.append("$tag: $message\n")
            mBinding.scrollCoroutine.post { mBinding.scrollCoroutine.fullScroll(View.FOCUS_DOWN) }
        }
//        suspend fun doWorld() {
//            delay(1000)
//        }

        mBinding.buttonCoroutineBasicStart.setOnClickListener {
            if (mCoroutineJob1?.isActive == true) {
                updateConsole("Job has already started.")
            } else {
                mCoroutineJob1 = GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                    for (i in 1..50) {
                        if (isActive) {
                            val currentThreadName = Thread.currentThread().name
                            withContext(Dispatchers.Main) { updateConsole(i.toString(), currentThreadName) }
                            delay(500)
                        }
                    }
                }
            }
        }

        mBinding.buttonCoroutineBasicStop.setOnClickListener {
            if (mCoroutineJob1?.isActive == true) {
                runBlocking { mCoroutineJob1?.cancelAndJoin() }
            } else {
                updateConsole("The job has been canceled")
            }
        }

        mBinding.buttonCoroutineBasicStatus.setOnClickListener {
            mCoroutineJob1?.let {
                when (it.isActive) {
                    true -> updateConsole("On")
                    false -> updateConsole("Off")
                }
            } ?: run {
                updateConsole("Coroutine is not initialized.")
            }
        }

        mBinding.buttonCoroutineMultiple.setOnClickListener {
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

        mBinding.buttonRunBlocking.setOnClickListener {
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

        mBinding.buttonCoroutineScope.setOnClickListener {
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
                mBinding.textLocationConsole.text = info
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
    private fun createNotification(notificationInfo: NotificationInfo): Notification {
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

        val title = "[${notificationInfo.id}] Notification Title"
        val text = "알림에 대한 본문 내용이 들어가는 영역입니다. 기본 템플릿을 확장형 알림을 구현할 수 있습니다."
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev")
        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle("[Base] $title")
                .setContentText("[Base] $text")
                .setContentIntent(
                        PendingIntent.getActivity(this, notificationInfo.id /*Private request code for the sender*/, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(NOTIFICATION_ID, notificationInfo.id)
                            putExtra(NOTIFICATION_INFO, "Test Notification Count: $mNotificationCount")
                        }, pendingIntentFlag())
                )

        if (notificationInfo.useCustomContentView) {
            notificationBuilder
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomBigContentView(RemoteViews(applicationContext.packageName, R.layout.partial_notification))
        } else {
            notificationBuilder
                    .setLargeIcon(BitmapFactory.decodeResource(resources, notificationInfo.largeIconResourceId))
        }
        if (notificationInfo.id == 100) notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText("[BigTextStyle] $text").setSummaryText("[BigTextStyle] $title"))

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                    R.drawable.ic_easydiary,
                    getString(R.string.dismiss),
                    PendingIntent.getService(this, notificationInfo.id /*Private request code for the sender*/, Intent(this, NotificationService::class.java).apply {
                        action = BaseNotificationService.ACTION_DISMISS_DEV
                        putExtra(NOTIFICATION_ID, notificationInfo.id /*An identifier for this notification unique within your application.*/)
                    }, pendingIntentFlag())
            )
        }

        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID = "notification_id"
        const val NOTIFICATION_INFO = "notification_info"
    }
}


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/
data class NotificationInfo(
    var largeIconResourceId: Int,
    var useActionButton: Boolean = false,
    var useCustomContentView: Boolean = false,
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






