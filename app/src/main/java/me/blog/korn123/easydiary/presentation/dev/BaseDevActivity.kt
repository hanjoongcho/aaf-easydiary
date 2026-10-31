package me.blog.korn123.easydiary.presentation.dev

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
import android.widget.RemoteViews
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.GsonBuilder
import com.simplemobiletools.commons.helpers.isOreoPlus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.ExportOption
import me.blog.korn123.easydiary.extensions.acquireGPSPermissions
import me.blog.korn123.easydiary.extensions.actionLogRepository
import me.blog.korn123.easydiary.extensions.alarmRepository
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.fullAddress
import me.blog.korn123.easydiary.extensions.getFromLocation
import me.blog.korn123.easydiary.extensions.getLastKnownLocation
import me.blog.korn123.easydiary.extensions.hasGPSPermissions
import me.blog.korn123.easydiary.extensions.isLocationEnabled
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.pendingIntentFlag
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_INFO
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.utils.EasyDiaryUtils

@AndroidEntryPoint
open class BaseDevActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var binding: ActivityBaseDevBinding
    private var notificationCount = 9000
    protected val baseDevViewModel: BaseDevViewModel by viewModels()
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }

    private fun createLocationListener(providerName: String): LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("$providerName location has been updated")
                locationManager.removeUpdates(this)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) {
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

    private val networkLocationListener = createLocationListener("Network")
    private val gpsLocationListener = createLocationListener("GPS")

    private val requestLocationSourceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            makeSnackBar(
                if (isLocationEnabled()) "GPS provider setting is activated!!!" else "The request operation did not complete normally.",
            )
        }

    val pickMultipleMedia =
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
    val exportJsonLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
            uri?.let { targetUri -> performJsonExport(targetUri) }
        }

    fun performJsonExport(targetUri: Uri) {
        baseDevViewModel.isLoading = true
        lifecycleScope.launch {
            val mapOfItems: MutableMap<String, Any> =
                when (exportJsonOption) {
                    ExportOption.ACTION_LOG -> {
                        mutableMapOf("ACTION_LOG" to actionLogRepository.getAllActionLogs())
                    }

                    ExportOption.ALARM -> {
                        mutableMapOf("ALARM" to alarmRepository.getAllAlarms())
                    }

                    ExportOption.D_DAY -> {
                        mutableMapOf("D_DAY" to dDayRepository.getAllDDays())
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
     *   etc functions
     *
     ***************************************************************************************************/
    fun updateGPSProvider() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        } else {
            makeSnackBar("GPS Provider is not available.")
        }
    }

    fun updateNetWorkProvider() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
        } else {
            makeSnackBar("Network Provider is not available.")
        }
    }

    fun createNotificationBigTextStyle() {
        val notification =
            NotificationInfo(
                R.drawable.ic_done,
                useActionButton = true,
                id = notificationCount,
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

    fun createNotificationCustomView(context: Context) {
        val notification =
            NotificationInfo(
                R.drawable.ic_diary_backup_local,
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

    fun createNotificationBasicWithBitmapIcon(context: Context) {
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

    fun createNotificationBasic() {
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
                        }.build(),
                )
        }
    }

    fun updateLocation(viewModel: BaseDevViewModel) {
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
        if (hasGPSPermissions()) {
            setLocationInfo()
        } else {
            acquireGPSPermissions(requestLocationSourceLauncher) {
                setLocationInfo()
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
