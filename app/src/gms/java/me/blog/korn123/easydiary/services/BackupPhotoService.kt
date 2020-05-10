package me.blog.korn123.easydiary.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.lang3.time.StopWatch
import java.io.File
import java.util.*

class BackupPhotoService : Service() {
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
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        mPhotoPath = "${EasyDiaryUtils.getApplicationDataDirectory(this)}$DIARY_PHOTO_DIRECTORY"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mWorkingFolderId = intent?.getStringExtra(DriveServiceHelper.WORKING_FOLDER_ID) ?: ""
        backupPhoto()
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mInProcessJob = false
    }

    var stopWatch = StopWatch()
    private fun backupPhoto() {
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.InboxStyle())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.cloud_upload)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.task_progress_message))
//                .setContentText(getString(R.string.task_progress_message))
                .setProgress(0, 0, true)
                .addAction(
                        R.drawable.cloud_upload,
                        getString(R.string.cancel),
                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                            action = NotificationService.ACTION_BACKUP_CANCEL
                        }, 0)
                )
        startForeground(NOTIFICATION_FOREGROUND_ID, notificationBuilder.build())

        stopWatch.reset()
        stopWatch.start()

        // step01. 전체 파일 목록을 조회
        determineRemoteDrivePhotos(null)
    }

    private fun determineRemoteDrivePhotos(nextPageToken: String?) {
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
                        stopWatch.stop()
                        Log.i("GSuite", "determineRemoteDrivePhotos: ${remoteDriveFileNames.size}")
                        Log.i("GSuite", "targetFilenames: ${targetFilenames.size}")
                        Log.i("GSuite", stopWatch.toString())

                        localDeviceFileCount = localPhotos.size
                        duplicateFileCount = localDeviceFileCount - targetFilenames.size
                        if (targetFilenames.size == 0) {
                            updateNotification()
                        } else {
                            uploadDiaryPhoto()
                        }
                    }
                    false -> determineRemoteDrivePhotos(result.nextPageToken)
                }
            }
            addOnFailureListener { exception -> exception.printStackTrace() }
        }
    }

    private fun uploadDiaryPhoto() {
        val fileName =  targetFilenames[targetFilenamesCursor]
        mDriveServiceHelper.createFile(mWorkingFolderId, mPhotoPath + fileName, fileName, DriveServiceHelper.MIME_TYPE_AAF_EASY_DIARY_PHOTO).run {
            addOnSuccessListener { _ ->
                targetFilenamesCursor++
                successCount++
                updateNotification()
            }
            addOnFailureListener {
                targetFilenamesCursor++
                failCount++
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        if (targetFilenames.size == 0) {
            launchCompleteNotification(getString(R.string.notification_msg_upload_invalid))
        } else {
            notificationBuilder
                    .setStyle(NotificationCompat.InboxStyle()
                            .addLine("${getString(R.string.notification_msg_device_file_count)}: $localDeviceFileCount")
                            .addLine("${getString(R.string.notification_msg_duplicate_file_count)}: $duplicateFileCount")
                            .addLine("${getString(R.string.notification_msg_upload_success)}: $successCount")
                            .addLine("${getString(R.string.notification_msg_upload_fail)}: $failCount")
                    )
                    .setContentTitle("${getString(R.string.notification_msg_upload_progress)}  ${successCount + failCount}/${targetFilenames.size}")
                    .setProgress(targetFilenames.size, successCount + failCount, false)
            notificationManager.notify(NOTIFICATION_FOREGROUND_ID, notificationBuilder.build())

            if (successCount + failCount < targetFilenames.size) {
                when (mInProcessJob) {
                    true -> uploadDiaryPhoto()
                    false -> notificationManager.cancel(NOTIFICATION_FOREGROUND_ID)
                }
            } else {
                config.photoBackupGoogle = System.currentTimeMillis()
                launchCompleteNotification(getString(R.string.notification_msg_upload_complete))
            }
        }
//        if (currentCount == targetFilenames.size) stopSelf()
    }

    private fun launchCompleteNotification(contentText: String) {
        val resultNotificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        resultNotificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.InboxStyle()
                        .addLine("${getString(R.string.notification_msg_device_file_count)}: $localDeviceFileCount")
                        .addLine("${getString(R.string.notification_msg_duplicate_file_count)}: $duplicateFileCount")
                        .addLine("${getString(R.string.notification_msg_upload_success)}: $successCount")
                        .addLine("${getString(R.string.notification_msg_upload_fail)}: $failCount")
                )
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.cloud_upload)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.backup_attach_photo_title))
                .setContentText(contentText)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.cloud_upload,
                        getString(R.string.dismiss),
                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                            action = NotificationService.ACTION_DISMISS
                        }, 0)
                )
        notificationManager.notify(NOTIFICATION_COMPLETE_ID, resultNotificationBuilder.build())
        localDeviceFileCount = 0
        duplicateFileCount = 0
        successCount = 0
        failCount = 0
        targetFilenames.clear()
//        stopForeground(true)
        stopSelf()
    }
}