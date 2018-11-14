package me.blog.korn123.easydiary.gms.drive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import com.simplemobiletools.commons.extensions.getFileCount
import me.blog.korn123.easydiary.R
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class RecoverPhotoActivity : BaseDriveActivity() {
    private var currentCount: Int = 0
    private var remoteDriveFileCount = 0
    private var duplicateFileCount = 0
    private var targetFileCount = 0
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

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private fun listFilesInFolder(folder: DriveFolder) {
        val channelId = "M_CH_ID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "CHANNEL_NAME"
            val descriptionText = "CHANNEL_DESCRIPTION"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, AAF_EASY_DIARY_PHOTO))
                .build()
        // [START drive_android_query_children]
        val queryTask = driveResourceClient?.queryChildren(folder, query)
        // END drive_android_query_children]
        queryTask?.let {
            it.addOnSuccessListener(this) { metadataBuffer ->
                notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.google_drive)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
                        .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                        .setOnlyAlertOnce(true)
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
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "Error retrieving files", e)
                finish()
            }
        }
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
                .addLine("Number of recover files: $remoteDriveFileCount")
                .addLine("Number of exist files: $duplicateFileCount")
                .addLine("Number of files to download: $targetFileCount"))
        if (targetFileCount == 0) {
            notificationBuilder.setContentTitle("All recovery target files already exist.")
            notificationManager.notify(1, notificationBuilder.build())
        } else {
            notificationBuilder.setContentTitle("${++currentCount}/${targetFileCount}")
            notificationBuilder.setProgress(targetFileCount, currentCount, false)
            notificationManager.notify(1, notificationBuilder.build())
        }
        if (currentCount == targetFileCount) finish()
    }

    companion object {
        private const val TAG = "RecoverPhotoActivity"
    }
}