package me.blog.korn123.easydiary.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_COARSE_LOCATION
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_FINE_LOCATION
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*


open class BaseDevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
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

        locationManager.setOnClickListener {
            getLocationWithGPSProvider { location ->
                location?.let {
                    var info = "Longitude: ${it.longitude}\nLatitude: ${it.latitude}\n"
                    getFromLocation(it.latitude, it.longitude, 1)?.let { address ->
                        if (address.isNotEmpty()) {
                            info += fullAddress(address[0])
                        }
                    }
                    locationManagerInfo.text = info
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()

        when (requestCode) {
            REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS -> {
                makeSnackBar(if (isLocationEnabled(this)) "GPS provider setting is activated." else "The request operation did not complete normally.")
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun fullAddress(address: Address): String {
        val sb = StringBuilder()
        if (address.countryName != null) sb.append(address.countryName).append(" ")
        if (address.adminArea != null) sb.append(address.adminArea).append(" ")
        if (address.locality != null) sb.append(address.locality).append(" ")
        if (address.subLocality != null) sb.append(address.subLocality).append(" ")
        if (address.thoroughfare != null) sb.append(address.thoroughfare).append(" ")
        if (address.featureName != null) sb.append(address.featureName).append(" ")
        return sb.toString()
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
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

fun EasyDiaryActivity.getLocationWithGPSProvider(callback: (location: Location?) -> Unit) {
    val gpsProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val networkProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    when (checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION))) {
        true -> {
            if (isLocationEnabled(this)) {
                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            } else {
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
            }
        }
        false -> {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) { hasCoarseLocation ->
                if (hasCoarseLocation) {
                    handlePermission(PERMISSION_ACCESS_FINE_LOCATION) { hasFineLocation ->
                        if (hasFineLocation) {
                            if (isLocationEnabled(this)) {
                                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
                            } else {
                                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Context.getFromLocation(latitude: Double, longitude: Double, maxResults: Int): List<Address>? {
//    val lat = java.lang.Double.parseDouble(String.format("%.6f", latitude))
//    val lon = java.lang.Double.parseDouble(String.format("%.7f", longitude))
    val addressList = arrayListOf<Address>()
    try {
        addressList.addAll(Geocoder(this, Locale.getDefault()).getFromLocation(latitude, longitude, maxResults))
    } catch (e: Exception) {
        toast(e.message ?: "Error")
    }
    return addressList
}





