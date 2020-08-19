package me.blog.korn123.easydiary.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.createBackupContentText
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import java.io.File
import java.util.*

class FullBackupService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var mDriveServiceHelper: DriveServiceHelper
    private var localDeviceFileCount = 0
    private var duplicateFileCount = 0
    private var successCount = 0
    private var failCount = 0
    private var targetFilenamesCursor = 0
    private var mInProcessJob = true
    private val remoteDriveFileNames  = mutableListOf<String>()
    private val targetFilenames = mutableListOf<String>()
    private lateinit var mPhotoPath: String
    private lateinit var mWorkingFolderId: String
    
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
        EasyDiaryDbHelper.insertActionLog(ActionLog("FullBackupService", "onStartCommand", "INFO", "step-01"), applicationContext)
        mWorkingFolderId = intent?.getStringExtra(DriveServiceHelper.WORKING_FOLDER_ID) ?: ""

        // test alarm sequence is 5
        val alarmId = intent?.getIntExtra(SettingsScheduleFragment.ALARM_ID, 5) ?: 5
        EasyDiaryDbHelper.readAlarmBy(alarmId)?.let {
            backupPhoto(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mInProcessJob = false
    }

    private fun backupPhoto(alarm: Alarm) {
        EasyDiaryDbHelper.insertActionLog(ActionLog("FullBackupService", "backupPhoto", "INFO", "step-02"), applicationContext)
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.InboxStyle())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_upload))
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.task_progress_message))
//                .setContentText(getString(R.string.task_progress_message))
                .setProgress(0, 0, true)
//                .addAction(
//                        R.drawable.ic_easydiary,
//                        getString(R.string.cancel),
//                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
//                            action = NotificationService.ACTION_FULL_BACKUP_GMS_CANCEL
//                        }, 0)
//                )
        startForeground(alarm.id, notificationBuilder.build())

        // step01. 전체 파일 목록을 조회
        determineRemoteDrivePhotos(null, alarm)
    }

    private fun determineRemoteDrivePhotos(nextPageToken: String?, alarm: Alarm) {
        EasyDiaryDbHelper.insertActionLog(ActionLog("FullBackupService", "determineRemoteDrivePhotos", "INFO", "step-03"), applicationContext)
        mDriveServiceHelper.queryFiles("mimeType = '${DriveServiceHelper.MIME_TYPE_AAF_EASY_DIARY_PHOTO}' and trashed = false",  1000, nextPageToken).run {
            addOnSuccessListener { result ->
                result.files.map { photoFile ->
                    remoteDriveFileNames.add(photoFile.name)
                }

                when (result.nextPageToken == null) {
                    true -> {
                        // step02. upload 대상 첨부사진 필터링
                        val localPhotos = File(mPhotoPath).listFiles()
                        localPhotos.map { photo ->
                            if (!remoteDriveFileNames.contains(photo.name)) {
                                targetFilenames.add(photo.name)
                            }
                        }
                        localDeviceFileCount = localPhotos.size
                        duplicateFileCount = localDeviceFileCount - targetFilenames.size
                        if (targetFilenames.size == 0) {
                            updateNotification(alarm)
                        } else {
                            uploadDiaryPhoto(alarm)
                        }
                    }
                    false -> determineRemoteDrivePhotos(result.nextPageToken, alarm)
                }
            }
            addOnFailureListener { exception ->
                EasyDiaryDbHelper.insertActionLog(ActionLog("FullBackupService", "determineRemoteDrivePhotos", "ERROR", exception.message), applicationContext)
            }
        }
    }

    private fun uploadDiaryPhoto(alarm: Alarm) {
        val fileName =  targetFilenames[targetFilenamesCursor]
        mDriveServiceHelper.createFile(mWorkingFolderId, mPhotoPath + fileName, fileName, DriveServiceHelper.MIME_TYPE_AAF_EASY_DIARY_PHOTO).run {
            addOnSuccessListener { _ ->
                targetFilenamesCursor++
                successCount++
                updateNotification(alarm)
            }
            addOnFailureListener {
                targetFilenamesCursor++
                failCount++
                updateNotification(alarm)
            }
        }
    }

    private fun updateNotification(alarm: Alarm) {
        if (targetFilenames.size == 0) {
            backupDiaryRealm(alarm)
        } else {
            val stringBuilder = createBackupContentText(localDeviceFileCount, duplicateFileCount, successCount, failCount)
            notificationBuilder
                    .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                    )
                    .setContentTitle("${getString(R.string.notification_msg_upload_progress)}  ${successCount + failCount}/${targetFilenames.size}")
                    .setProgress(targetFilenames.size, successCount + failCount, false)
            notificationManager.notify(alarm.id, notificationBuilder.build())

            if (successCount + failCount < targetFilenames.size) {
                when (mInProcessJob) {
                    true -> uploadDiaryPhoto(alarm)
                    false -> notificationManager.cancel(alarm.id)
                }
            } else {
                config.photoBackupGoogle = System.currentTimeMillis()
                backupDiaryRealm(alarm)
            }
        }
    }

    private fun backupDiaryRealm(alarm: Alarm) {
        GoogleOAuthHelper.getGoogleSignAccount(applicationContext)?.account?.let { account ->
            DriveServiceHelper(applicationContext, account).run {
                initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) { realmFolderId ->
                    val dbFileName = DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss")
                    if (realmFolderId != null) {
                        createFile(
                                realmFolderId, EasyDiaryDbHelper.getRealmPath(),
                                dbFileName,
                                EasyDiaryUtils.easyDiaryMimeType
                        ).addOnSuccessListener {
                            config.diaryBackupGoogle = System.currentTimeMillis()
                            launchCompleteNotification(alarm, dbFileName)
                        }
                    } else {
                        EasyDiaryDbHelper.insertActionLog(ActionLog("FullBackupService", "backupDiaryRealm", "ERROR", "realmFolderId is null"), applicationContext)
                    }
                }
            }
        }
    }

    private fun launchCompleteNotification(alarm: Alarm, savedFileName: String) {
        stopForeground(true)

        val stringBuilder = createBackupContentText(localDeviceFileCount, duplicateFileCount, successCount, failCount)
                .insert(0, getString(R.string.schedule_backup_gms_complete, "<br>"))
                .append("<b>\uD83D\uDCC1 Database</b><br>")
                .append("* Saved file name: $savedFileName")

        val resultNotificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_upload")
        resultNotificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_upload))
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(alarm.label)
//                .setContentText(getString(R.string.schedule_backup_gms_complete))
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                )
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )
//                .addAction(
//                        R.drawable.ic_easydiary,
//                        getString(R.string.dismiss),
//                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
//                            action = NotificationService.ACTION_PHOTO_BACKUP_GMS_DISMISS
//
//                        }, 0)
//                )
        notificationManager.notify(alarm.id, resultNotificationBuilder.build())
        localDeviceFileCount = 0
        duplicateFileCount = 0
        successCount = 0
        failCount = 0
        targetFilenames.clear()
        stopSelf()
    }
}