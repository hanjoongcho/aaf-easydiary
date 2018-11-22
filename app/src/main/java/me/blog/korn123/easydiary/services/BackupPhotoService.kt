package me.blog.korn123.easydiary.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.simplemobiletools.commons.extensions.getFileCount
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import java.io.File

class BackupPhotoService : Service() {
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var driveResourceClient: DriveResourceClient? = null
    private var localDeviceFileCount = 0
    private var duplicateFileCount = 0
    private val targetFilenames = mutableListOf<String>()
    private var currentCount: Int = 0
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        Handler().post { Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show() }
        GoogleSignIn.getLastSignedInAccount(this)?.let {
            driveResourceClient = Drive.getDriveResourceClient(this, it)
        }
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        
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
        Handler().post { Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show() }

        intent?.let {
            backupPhoto(DriveId.decodeFromString(it.getStringExtra(NOTIFICATION_DRIVE_ID)).asDriveFolder())
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Handler().post { Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show() }
    }
    
    private fun backupPhoto(folder: DriveFolder) {
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, AAF_EASY_DIARY_PHOTO))
                .build()
        val queryTask = driveResourceClient?.queryChildren(folder, query)
        queryTask?.addOnSuccessListener { metadataBuffer ->
            val photoPath = "${Environment.getExternalStorageDirectory().absolutePath}$AAF_EASY_DIARY_PHOTO_DIRECTORY"
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
                    .setSmallIcon(R.drawable.cloud_upload)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
                    .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but you can still use for below 26. check below update for 26 API
                    .setOnlyAlertOnce(true)
                    .setContentTitle(getString(R.string.backup_attach_photo_title))
                    .setContentIntent(PendingIntent.getActivity(this, 0, diaryMainIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .addAction(R.drawable.cloud_upload, getString(R.string.dismiss), PendingIntent.getService(this, 0, dismissIntent, 0))
            notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val titles = mutableListOf<String>()
            metadataBuffer.forEachIndexed { _, metadata ->
                titles.add(metadata.title)
            }

            File(photoPath).listFiles().forEachIndexed { _, file ->
                if (!titles.contains(file.name)) targetFilenames.add(file.name)
            }

            localDeviceFileCount = File(photoPath).getFileCount(true)
            duplicateFileCount = localDeviceFileCount - targetFilenames.size

            targetFilenames.map { filename ->
                uploadDiaryPhoto(File("$photoPath$filename"), folder)
            }

            if (targetFilenames.size == 0) updateNotification()
        }
    }

    private fun uploadDiaryPhoto(file: File, folder: DriveFolder) {
        driveResourceClient?.let {
            it.createContents().continueWithTask<DriveFile> { task ->
                val contents = task.result
                val outputStream = contents.outputStream
                FileUtils.copyFile(file, outputStream)
//            OutputStreamWriter(outputStream).use { writer -> writer.write("Hello World!") }
                val changeSet = MetadataChangeSet.Builder()
                        .setTitle(file.name)
                        .setMimeType(AAF_EASY_DIARY_PHOTO)
                        .setStarred(true)
                        .build()
                it.createFile(folder, changeSet, contents)
            }.addOnSuccessListener { _ ->
                updateNotification()
            }.addOnFailureListener { e ->
//                Log.e(BackupPhotoActivity.TAG, "Unable to create file", e)
//                showMessage(getString(R.string.file_create_error))
            }
        }
    }

    private fun updateNotification() {
        notificationBuilder.setStyle(NotificationCompat.InboxStyle()
                .addLine("${getString(R.string.notification_msg_device_file_count)}: $localDeviceFileCount")
                .addLine("${getString(R.string.notification_msg_duplicate_file_count)}: $duplicateFileCount")
                .addLine("${getString(R.string.notification_msg_upload_file_count)}: ${targetFilenames.size}"))

        if (targetFilenames.size == 0) {
            notificationBuilder.setContentText(getString(R.string.notification_msg_upload_invalid))
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, notificationBuilder.build())
        } else {
            currentCount++
            val message = if (currentCount < targetFilenames.size) getString(R.string.notification_msg_upload_progress) else getString(R.string.notification_msg_upload_complete)
            notificationBuilder.setContentTitle("$message  $currentCount/${targetFilenames.size}")
            notificationBuilder.setProgress(targetFilenames.size, currentCount, false)
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, notificationBuilder.build())
        }

        if (currentCount == targetFilenames.size) stopSelf()
    }
}