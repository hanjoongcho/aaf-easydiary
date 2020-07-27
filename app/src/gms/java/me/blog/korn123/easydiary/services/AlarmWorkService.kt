package me.blog.korn123.easydiary.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.getAlarmNotification
import me.blog.korn123.easydiary.extensions.getOpenAlarmTabIntent
import me.blog.korn123.easydiary.extensions.isScreenOn
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import java.util.*

class AlarmWorkService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    private lateinit var mPhotoPath: String

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = googleSignInAccount?.account
        val googleDriveService: com.google.api.services.drive.Drive = com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
        mDriveServiceHelper = DriveServiceHelper(applicationContext, googleDriveService)
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_upload")
        mPhotoPath = "${EasyDiaryUtils.getApplicationDataDirectory(this)}$DIARY_PHOTO_DIRECTORY"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_upload", "${NOTIFICATION_CHANNEL_NAME}_upload", importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            val alarmId = it.getIntExtra(SettingsScheduleFragment.ALARM_ID, -1)
            when {
                alarmId > -1 -> {
                    EasyDiaryDbHelper.readAlarmBy(alarmId)?.let { alarm ->
                        executeWork(alarm)
                    }
                }
                else -> {
                    stopSelf()
                }
            }

        } ?: stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }
    

    private fun executeWork(alarm: Alarm) {
        EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmWorkService", "executeJob", "INFO: ", "Start"), this)
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.InboxStyle())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_upload))
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.task_progress_message))
                .setProgress(0, 0, true)
        startForeground(NOTIFICATION_FOREGROUND_GMS_BACKUP_ID, notificationBuilder.build())

        GoogleOAuthHelper.getGoogleSignAccount(this)?.account?.let { account ->
            DriveServiceHelper(this, account).run {
                initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) { id ->
                    id?.let {
                        createFile(
                                it, EasyDiaryDbHelper.getRealmPath(),
                                DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                                EasyDiaryUtils.easyDiaryMimeType
                        ).addOnSuccessListener {
                            val pendingIntent = getOpenAlarmTabIntent(alarm)
                            val notification = getAlarmNotification(pendingIntent, alarm)
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(alarm.id, notification)

                            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                            if (isScreenOn()) {
                                scheduleNextAlarm(alarm, true)
                            } else {
                                scheduleNextAlarm(alarm, false)
                                powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "myApp:notificationLock").apply {
                                    acquire(3000)
                                }
                            }
                            stopSelf()
                        }.addOnFailureListener { e ->
                            stopSelf()
                            EasyDiaryDbHelper.insertActionLog(ActionLog("AlarmWorkService", "executeJob", "error", e.message), context)
                        }
                    } ?: stopSelf()
                }
            }
        }
    }
}