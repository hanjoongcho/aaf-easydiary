package me.blog.korn123.easydiary.workers

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.openBackupDestinationUnavailableNotification
import me.blog.korn123.easydiary.extensions.toDisplaySize
import me.blog.korn123.easydiary.helper.AlarmConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_FULL_BACKUP_FILE_BASE_NAME
import me.blog.korn123.easydiary.helper.EXTERNAL_FULL_BACKUP_TIMESTAMP_PATTERN
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPRESS_COMPLETE_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPRESS_ID
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.helper.WorkerConstants
import me.blog.korn123.easydiary.helper.ZipHelper
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.ExternalBackupFolder
import java.io.File

class FullBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val mZipHelper = ZipHelper(context)
    private var isExportSuccessful = false

    override fun doWork(): Result {
        val notification =
            mZipHelper.createNotification(
                NOTIFICATION_COMPRESS_ID,
                context.getString(R.string.backup_full_notification_title),
                context.getString(R.string.backup_full_notification_preparing),
                NotificationConstants.ACTION_FULL_BACKUP_CANCEL,
            )
        try {
            setForegroundAsync(
                ForegroundInfo(
                    NOTIFICATION_COMPRESS_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                ),
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // A scheduled run hands over the folder it writes into; the manual [BETA] Backup full data hands over the one
        // document the user created. Both produce the same archive, and only the way it reaches the destination differs.
        val folderUriString = inputData.getString(WorkerConstants.FOLDER_URI_STRING)
        val uri = inputData.getString(WorkerConstants.URI_STRING)?.let(Uri::parse)
        val password = inputData.getString(WorkerConstants.PASSWORD_STRING)

        val workingPath = EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY
//        val destFileName = DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip"
//        val destFile = File(EasyDiaryUtils.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY + destFileName)
        val compressFile = File(workingPath, "bak.zip")
        if (compressFile.exists()) compressFile.delete()

        mZipHelper.determineFiles(workingPath)
//        mZipHelper.printFileNames()
        mZipHelper.compress(compressFile, password)
        if (mZipHelper.isOnProgress && compressFile.exists()) {
            val compressFileSize = compressFile.length().toDisplaySize()
            val isDelivered =
                if (folderUriString != null) {
                    deliverToFolder(folderUriString, compressFile)
                } else {
                    deliverToDocument(uri, compressFile)
                }
            compressFile.delete()

            if (isDelivered) {
                isExportSuccessful = true

                // WorkManager가 종료될 때 생성해둔 포그라운드 노티가 제거되므로,
                // 별도의 일반 Notification ID(NOTIFICATION_COMPRESS_COMPLETE_ID)로 상단 바에 알림을 새로 띄워 유지시킵니다.
                mZipHelper.updateNotification(
                    NOTIFICATION_COMPRESS_COMPLETE_ID,
                    context.getString(R.string.backup_full_complete_title),
                    context.getString(R.string.backup_full_complete_message, compressFileSize),
                )
            } else {
                NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
            }
        } else {
            compressFile.delete()
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
        }
        return Result.success()
    }

    /** Copies the archive to the document the manual backup flow created, exactly as this worker always has. */
    private fun deliverToDocument(
        uri: Uri?,
        compressFile: File,
    ): Boolean {
        if (uri == null) return false
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                compressFile.inputStream().use { input -> input.copyTo(output) }
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Writes the archive into the folder a schedule keeps, and reports through the schedule's own notification when
     * that folder cannot be written: a run that starts without the grant, or after the folder was deleted, must not
     * fail silently.
     */
    private fun deliverToFolder(
        folderUriString: String,
        compressFile: File,
    ): Boolean {
        val fileName = externalArchiveFileName()
        val isWritten = ExternalBackupFolder.writeArchive(context, folderUriString, fileName, compressFile) != null
        if (!isWritten) {
            context.openBackupDestinationUnavailableNotification(
                inputData.getInt(WorkerConstants.ALARM_ID, 0),
                inputData.getString(WorkerConstants.ALARM_LABEL),
            )
        }
        return isWritten
    }

    /** `easy_diary_backup.zip` when the fixed name is chosen, `easy_diary_backup_<timestamp>.zip` when a new file per run is. */
    private fun externalArchiveFileName(): String =
        ExternalBackupFolder.archiveFileName(
            EXTERNAL_FULL_BACKUP_FILE_BASE_NAME,
            inputData.getInt(WorkerConstants.FILE_NAME_MODE, AlarmConstants.BACKUP_FILE_MODE_OVERWRITE),
            DateUtils.getCurrentDateTime(EXTERNAL_FULL_BACKUP_TIMESTAMP_PATTERN),
        )

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        if (!isExportSuccessful) {
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
        }
    }
}
