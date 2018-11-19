/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.simplemobiletools.commons.extensions.getFileCount
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_NAME
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.services.NotificationService
import org.apache.commons.io.FileUtils
import java.io.File

class BackupPhotoActivity : BaseDriveActivity() {
    private lateinit var driveId: DriveId
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var localDeviceFileCount = 0
    private var duplicateFileCount = 0
    private val targetFilenames = mutableListOf<String>()
    private var currentCount: Int = 0

    override fun onDriveClientReady() {
        pickFolder()
    }

    override fun addListener() {
        mTask?.let {
            it.addOnSuccessListener(this) { driveId ->
                this.driveId = driveId
                listFilesInFolder()
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "No folder selected", e)
                showMessage(getString(R.string.folder_not_selected))
                finish()
            }
        }
    }

    override fun showDialog() {
        showAlertDialog(
                getString(R.string.backup_attach_photo_title),
                getString(R.string.notification_msg_upload_empty),
                DialogInterface.OnClickListener { _, _ ->  finish() }
        )
    }

    private fun uploadDiaryPhoto(file: File) {
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
                it.createFile(driveId.asDriveFolder(), changeSet, contents)
            }.addOnSuccessListener(this) { _ ->
                updateNotification()
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "Unable to create file", e)
                showMessage(getString(R.string.file_create_error))
            }
        }
    }

    private fun listFilesInFolder() {
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, AAF_EASY_DIARY_PHOTO))
                .build()
        val queryTask = driveResourceClient?.queryChildren(driveId.asDriveFolder(), query)
        queryTask?.let {
            it.addOnSuccessListener { metadataBuffer ->
                val photoPath = "${Environment.getExternalStorageDirectory().absolutePath}$AAF_EASY_DIARY_PHOTO_DIRECTORY"
                when (File(photoPath).listFiles().isEmpty()) {
                    true -> {
                        visibleDialog = true
                        showDialog()
                    }
                    false -> {
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

                        File(photoPath).listFiles().forEachIndexed {_, file ->
                            if (!titles.contains(file.name)) targetFilenames.add(file.name)
                        }

                        localDeviceFileCount = File(photoPath).getFileCount(true)
                        duplicateFileCount = localDeviceFileCount - targetFilenames.size

                        targetFilenames.map { filename ->
                            uploadDiaryPhoto(File("$photoPath$filename"))
                        }

                        if (targetFilenames.size == 0) updateNotification()
                    }
                }
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
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } else {
            currentCount++
            val message = if (currentCount < targetFilenames.size) getString(R.string.notification_msg_upload_progress) else getString(R.string.notification_msg_upload_complete)
            notificationBuilder.setContentTitle("$message  $currentCount/${targetFilenames.size}")
            notificationBuilder.setProgress(targetFilenames.size, currentCount, false)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }

        if (currentCount == targetFilenames.size) finish()
    }

    companion object {
        private val TAG = "BackupPhotoActivity"
    }
}
