package me.blog.korn123.easydiary.gms.drive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.drive.*
import com.google.android.gms.drive.events.OpenFileCallback
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class RecoverPhotoActivity : BaseDriveActivity() {
    private var currentCount: Int = 0
    private var remoteDriveFileCount = 0
    private var duplicateFileCount = 0
    private val targetIndexes = arrayListOf<Int>()
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    override fun onDriveClientReady() {
        pickFolder()
    }

    override fun addListener() {
        mTask?.let {
            it.addOnSuccessListener(this) { driveId ->
                listFilesInFolder(driveId.asDriveFolder())
            }.addOnFailureListener(this) { e ->
                showMessage(getString(R.string.folder_not_selected))
                finish()
            }
        }
    }

    override fun showDialog() {
        showAlertDialog(
                getString(R.string.recover_attach_photo_title),
                getString(R.string.notification_msg_download_empty),
                DialogInterface.OnClickListener { _, _ ->  finish() }
        )
    }

    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private fun listFilesInFolder(folder: DriveFolder) {
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, AAF_EASY_DIARY_PHOTO))
                .build()
        // [START drive_android_query_children]
        val queryTask = driveResourceClient?.queryChildren(folder, query)
        // END drive_android_query_children]
        queryTask?.let {
            it.addOnSuccessListener(this) { metadataBuffer ->
                when (metadataBuffer.count == 0) {
                    true -> {
                        visibleDialog = true
                        showDialog()
                    }
                    false -> {
//                        recoverByMainThread(metadataBuffer)
                        recoverByForegroundService(folder.driveId.encodeToString())
                    }
                }
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "Error retrieving files", e)
                finish()
            }
        }
    }

    private fun recoverByMainThread(metadataBuffer: MetadataBuffer) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        val dismissIntent = Intent(this, NotificationService::class.java).apply {
            action = NotificationService.ACTION_DISMISS
        }
        val diaryMainIntent = Intent(this, DiaryMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.cloud_download)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
//                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                .setOnlyAlertOnce(true)
                .setContentTitle(getString(R.string.recover_attach_photo_title))
                .setContentIntent(PendingIntent.getActivity(this, 0, diaryMainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(R.drawable.cloud_download, getString(R.string.dismiss), PendingIntent.getService(this, 0, dismissIntent, 0))
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val photoPath = "${Environment.getExternalStorageDirectory().absolutePath}$AAF_EASY_DIARY_PHOTO_DIRECTORY"
        metadataBuffer.forEachIndexed { index, metadata ->
            if (!File("$photoPath${metadata.title}").exists()) targetIndexes.add(index)
        }

        remoteDriveFileCount = metadataBuffer.count
        duplicateFileCount = remoteDriveFileCount - targetIndexes.size
        targetIndexes.map { metaDataIndex ->
            retrieveContents(metadataBuffer[metaDataIndex].driveId.asDriveFile(), "$photoPath${metadataBuffer[metaDataIndex].title}")
        }

        if (targetIndexes.size == 0) updateNotification()
    }

    private fun recoverByForegroundService(encodedDriveId: String) {
        val recoverPhotoService = Intent(this, RecoverPhotoService::class.java)
        recoverPhotoService.putExtra(NOTIFICATION_DRIVE_ID, encodedDriveId)
        startService(recoverPhotoService)
        finish()
    }

    private fun retrieveContents(file: DriveFile, destFilePath: String) {
        val openCallback = object : OpenFileCallback() {
            override fun onProgress(bytesDownloaded: Long, bytesExpected: Long) {}
            override fun onContents(driveContents: DriveContents) {
                // [START_EXCLUDE]
                try {
                    FileUtils.copyInputStreamToFile(driveContents.inputStream, File(destFilePath))
                    updateNotification()
                } catch (e: IOException) {
                    onError(e)
                }
                // [END_EXCLUDE]
            }
            override fun onError(e: Exception) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e)
                showMessage(getString(R.string.read_failed))
                finish()
                // [END_EXCLUDE]
            }
        }

        when (File(destFilePath).exists()) {
            true -> updateNotification()
            false -> driveResourceClient?.openFile(file, DriveFile.MODE_READ_ONLY, openCallback)
        }
    }

    private fun updateNotification() {
        notificationBuilder.setStyle(NotificationCompat.InboxStyle()
                .addLine("${getString(R.string.notification_msg_google_drive_file_count)}: $remoteDriveFileCount")
                .addLine("${getString(R.string.notification_msg_duplicate_file_count)}: $duplicateFileCount")
                .addLine("${getString(R.string.notification_msg_download_file_count)}: ${targetIndexes.size}"))
        if (targetIndexes.size == 0) {
            notificationBuilder.setContentText(getString(R.string.notification_msg_download_invalid))
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, notificationBuilder.build())
        } else {
            currentCount++
            val message = if (currentCount < targetIndexes.size) getString(R.string.notification_msg_download_progress) else getString(R.string.notification_msg_download_complete)
            notificationBuilder.setContentTitle("$message  $currentCount/${targetIndexes.size}")
            notificationBuilder.setProgress(targetIndexes.size, currentCount, false)
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, notificationBuilder.build())
        }
        if (currentCount == targetIndexes.size) finish()
    }

    companion object {
        private const val TAG = "RecoverPhotoActivity"
    }
}