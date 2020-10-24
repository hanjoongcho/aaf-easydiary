package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
import org.apache.commons.io.FilenameUtils
import java.io.File


open class BaseDevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mNetworkLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {
            makeToast("Network location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
    }
    private val mGPSLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {
            makeToast("GPS location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String?) {}
        override fun onProviderDisabled(p0: String?) {}
    }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    @SuppressLint("MissingPermission")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }

        updateActionLog()

        nextAlarm.setOnClickListener {
            val nextAlarm = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val triggerTimeMillis = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: 0
                when (triggerTimeMillis > 0) {
                    true -> DateUtils.getFullPatternDateWithTime(triggerTimeMillis)
                    false -> "Alarm info is not exist."
                }
            } else {
                Settings.System.getString(contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
            }

            toast(nextAlarm, Toast.LENGTH_LONG)
        }

        notification1.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_DEV, createNotification(NotificationInfo(R.drawable.ic_diary_writing, true)))
            }
        }
        notification2.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_DEV, createNotification(NotificationInfo(R.drawable.ic_diary_backup_local, useActionButton = true, useCustomContentView = true)))
            }
        }

        clearLog.setOnClickListener {
            EasyDiaryDbHelper.deleteActionLogAll()
            updateActionLog()
        }

        clearUnusedPhoto.setOnClickListener {
            val localPhotoBaseNames = arrayListOf<String>()
            val unUsedPhotos = arrayListOf<String>()
            File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY).listFiles().map {
                localPhotoBaseNames.add(it.name)
            }

            EasyDiaryDbHelper.selectPhotoUriAll().map { photoUriDto ->
                if (!localPhotoBaseNames.contains(FilenameUtils.getBaseName(photoUriDto.getFilePath()))) {
                    unUsedPhotos.add(FilenameUtils.getBaseName(photoUriDto.getFilePath()))
                }
            }
            showAlertDialog(unUsedPhotos.size.toString(), null, true)
        }

        requestLastLocation.setOnClickListener {
            updateLocation()
        }

        updateGPSProvider.setOnClickListener {
            when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mGPSLocationListener)
                }
                false -> makeSnackBar("GPS Provider is not available.")
            }
        }

        updateNetworkProvider.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            when (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, mNetworkLocationListener)
                }
                false -> makeSnackBar("Network Provider is not available.")
            }
        }

        markdown1.setOnClickListener {
            TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
                putExtra(MarkDownViewActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.md")
                putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, "kotlin")
            })
        }
        markdown2.setOnClickListener {
            TransitionHelper.startActivityWithTransition(this, Intent(this, MarkDownViewActivity::class.java).apply {
                putExtra(MarkDownViewActivity.OPEN_URL_INFO, "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.collections.md")
                putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, "kotlin.collections")
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager.run {
            removeUpdates(mGPSLocationListener)
            removeUpdates(mNetworkLocationListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()

        when (requestCode) {
            REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS -> {
                makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated." else "The request operation did not complete normally.")
            }
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
                locationManagerInfo.text = info
            }
        }
        when (hasGPSPermissions()) {
            true -> setLocationInfo()
            false -> {
                acquireGPSPermissions() {
                    setLocationInfo()
                }
            }
        }
    }

    private fun updateActionLog() {
        val actionLogs: List<ActionLog> = EasyDiaryDbHelper.readActionLogAll()
        val sb = StringBuilder()
        actionLogs.map {
            sb.append("${it.className}-${it.signature}-${it.key}: ${it.value}\n")
        }
        actionLog.text = sb.toString()
    }

    @SuppressLint("NewApi")
    private fun createNotification(notificationInfo: NotificationInfo): Notification {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", "${NOTIFICATION_CHANNEL_NAME}_dev", importance)
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Notification Title"
        val text = "알림에 대한 본문 내용이 들어가는 영역입니다. 기본 템플릿을 확장형 알림을 구현할 수 있습니다."
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev")
        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, notificationInfo.largeIconResourceId))
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )


        if (notificationInfo.useCustomContentView) {
            notificationBuilder
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(RemoteViews(applicationContext.packageName, R.layout.layout_notification))
        } else {
            notificationBuilder
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text).setSummaryText(title))
        }

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                    R.drawable.ic_easydiary,
                    getString(R.string.dismiss),
                    PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                        action = BaseNotificationService.ACTION_DISMISS_DEV
                    }, 0)
            )
        }

        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID_DEV = 9999
    }
}


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/
data class NotificationInfo(var largeIconResourceId: Int, var useActionButton: Boolean = false, var useCustomContentView: Boolean = false)


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/
fun fun1(param1: String, block: (responseData: String) -> String): String {
    println(param1)
    return block("")
}

fun fun2(param1: String, block: (responseData: String) -> Boolean): String {
    println(param1)
    var blockReturn = block(param1)
    return param1
}

fun test1() {
    val result = fun1("banana") { responseData ->
        "data: $responseData"
    }
    println(result)
}






