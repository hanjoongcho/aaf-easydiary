package me.blog.korn123.easydiary.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.createBackupContentText
import me.blog.korn123.easydiary.helper.*
import java.io.File
import java.util.*

class RecoverPhotoService(name: String = "RecoverPhotoService") : IntentService(name) {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var remoteDriveFileCount = 0
    private var duplicateFileCount = 0
    private var successCount = 0
    private var failCount = 0
    private var mInProcessJob = true
    private var targetIndexesCursor = 0
    private val targetItems = arrayListOf<HashMap<String, String>>()
    private lateinit var mPhotoPath: String
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    override fun onCreate() {
//        Handler().post { Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show() }
        super.onCreate()
        val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = googleSignInAccount?.account
        val googleDriveService: Drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build()
        mDriveServiceHelper = DriveServiceHelper(applicationContext, googleDriveService)

        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_download")
        mPhotoPath = "${EasyDiaryUtils.getApplicationDataDirectory(this)}$DIARY_PHOTO_DIRECTORY"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel("${NOTIFICATION_CHANNEL_ID}_download") == null) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_download", "${NOTIFICATION_CHANNEL_NAME}_download", importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mInProcessJob = false
    }
    
    override fun onHandleIntent(intent: Intent?) {
        mInProcessJob = true
        notificationManager.cancel(NOTIFICATION_GMS_RECOVERY_COMPLETE_ID)
        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.InboxStyle())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_download))
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.task_progress_message))
//                .setContentText(getString(R.string.task_progress_message))
                .setProgress(0, 0, true)
                .addAction(
                        R.drawable.ic_easydiary,
                        getString(R.string.cancel),
                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                            action = NotificationService.ACTION_PHOTO_RECOVER_GMS_CANCEL
                        }, 0)
                )
        startForeground(NOTIFICATION_FOREGROUND_PHOTO_RECOVERY_GMS_ID, notificationBuilder.build())

        intent?.let {
            recoverPhoto()
        }

        // FIXME Hold async job???
        while(mInProcessJob) {
            Thread.sleep(1000)
        }
    }

    private fun downloadAttachPhoto() {
        if (targetIndexesCursor < targetItems.size) {
            if (mInProcessJob) {
                val item = targetItems[targetIndexesCursor++]
                mDriveServiceHelper.downloadFile(item["id"]!!, "$mPhotoPath${item["name"]}").run {
                    addOnSuccessListener {
                        successCount++
                        updateNotification()
                        downloadAttachPhoto()
                    }
                    addOnFailureListener {
                        failCount++
                        updateNotification()
                        downloadAttachPhoto()
                    }
                }
            }
        }
    }

    private fun determineAttachPhoto(nextPageToken: String?) {
        mDriveServiceHelper.queryFiles("mimeType = '${DriveServiceHelper.MIME_TYPE_AAF_EASY_DIARY_PHOTO}' and trashed = false",  1000, nextPageToken).run {
            addOnSuccessListener { result ->
                result.files.map { photoFile ->
                    remoteDriveFileCount++
                    if (!File("$mPhotoPath${photoFile.name}").exists()) {
                        val item = hashMapOf<String, String>(Pair("id", photoFile.id), Pair("name", photoFile.name))
                        targetItems.add(item)
                    }
                }

                when (result.nextPageToken == null) {
                    true -> {
                        duplicateFileCount = remoteDriveFileCount - targetItems.size
                        if (targetItems.size == 0) {
                            updateNotification()
                        } else {
                            downloadAttachPhoto()
                        }
                    }
                    false -> determineAttachPhoto(result.nextPageToken)
                }
            }
            addOnFailureListener { exception -> exception.printStackTrace() }
        }
    }

    private fun recoverPhoto() {
        mDriveServiceHelper.queryFiles("'root' in parents and name = '${DriveServiceHelper.AAF_ROOT_FOLDER_NAME}' and trashed = false", 1, null).run {
            addOnSuccessListener { fileList ->
                when (fileList.files.size) {
                    0 -> mDriveServiceHelper.createFolder(DriveServiceHelper.AAF_ROOT_FOLDER_NAME).addOnSuccessListener { fileId -> Log.i("GSuite", "Created application folder that app id is $fileId") }
                    1 -> {
                        val appFolder = fileList.files[0]
                        Log.i("GSuite", "${appFolder.name}, ${appFolder.mimeType}, ${appFolder.id}")
                        // step04. upload attach photo sample
//                        driveServiceHelper.createFile(appFolder.id, "attach-photo-01", AAF_EASY_DIARY_PHOTO).run {
//                            val photoPath = "${Environment.getExternalStorageDirectory().absolutePath}$AAF_EASY_DIARY_PHOTO_DIRECTORY"
//                            addOnSuccessListener { fileId -> driveServiceHelper.uploadFile(fileId, "$photoPath/0ce9591f-ba7b-48f3-b724-1253d590b433", AAF_EASY_DIARY_PHOTO) }
//                        }

                        // step05. determine upload photo sample and download it
                        determineAttachPhoto(null)
                    }
                    else -> {}
                }
            }
            addOnFailureListener {
                Log.i("GSuite", "not exist application folder")
            }
        }
    }

    private fun updateNotification() {
        if (targetItems.size == 0) {
            launchCompleteNotification(getString(R.string.notification_msg_download_invalid))
        } else {
            val stringBuilder = createBackupContentText(remoteDriveFileCount, duplicateFileCount, successCount, failCount)
            notificationBuilder
                    .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                    )
                    .setContentTitle("${getString(R.string.notification_msg_download_progress)}  ${successCount + failCount}/${targetItems.size}")
                    .setProgress(targetItems.size, successCount + failCount, false)
            notificationManager.notify(NOTIFICATION_FOREGROUND_PHOTO_RECOVERY_GMS_ID, notificationBuilder.build())

            if (successCount + failCount < targetItems.size) {
                when (mInProcessJob) {
                    true -> {}
                    false -> notificationManager.cancel(NOTIFICATION_FOREGROUND_PHOTO_RECOVERY_GMS_ID)
                }
            } else {
                launchCompleteNotification(getString(R.string.notification_msg_download_complete))
            }
        }
//        if (currentCount == targetIndexes.size) finish()
    }

    private fun launchCompleteNotification(contentText: String) {
        val stringBuilder = createBackupContentText(remoteDriveFileCount, duplicateFileCount, successCount, failCount)
                .insert(0, "$contentText<br>")

        val resultNotificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_download")
        resultNotificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                )
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_download))
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.recover_attach_photo_title))
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )
                .addAction(
                        R.drawable.ic_easydiary,
                        getString(R.string.dismiss),
                        PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                            action = NotificationService.ACTION_PHOTO_RECOVER_GMS_DISMISS
                        }, 0)
                )
        notificationManager.notify(NOTIFICATION_GMS_RECOVERY_COMPLETE_ID, resultNotificationBuilder.build())
        mInProcessJob = false
        remoteDriveFileCount = 0
        duplicateFileCount = 0
        successCount = 0
        failCount = 0
        targetItems.clear()
        stopSelf()
    }
}