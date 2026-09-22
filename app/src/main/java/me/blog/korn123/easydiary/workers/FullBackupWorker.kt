package me.blog.korn123.easydiary.workers

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.extensions.preferenceToJsonString
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

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
                "Full data backup",
                "Preparing to backup all data ...",
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

        val uri = Uri.parse(inputData.getString(WorkerConstants.URI_STRING))
        val os = context.contentResolver.openOutputStream(uri)

        val workingPath = EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY
//        val destFileName = DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip"
//        val destFile = File(EasyDiaryUtils.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY + destFileName)
        val compressFile = File(workingPath, "bak.zip")
        if (compressFile.exists()) compressFile.delete()

        // create preference json
        val fos = FileOutputStream(workingPath + "preference.json")
        IOUtils.write(context.preferenceToJsonString(), fos, "UTF-8")
        fos.close()

        mZipHelper.determineFiles(workingPath)
//        mZipHelper.printFileNames()
        mZipHelper.compress(compressFile)
        if (mZipHelper.isOnProgress) {
//            FileUtils.moveFile(compressFile, destFile)
            FileUtils.copyFile(compressFile, os)
            os?.close()
            isExportSuccessful = true

            // WorkManager가 종료될 때 생성해둔 포그라운드 노티가 제거되므로,
            // 별도의 일반 Notification ID(NOTIFICATION_GMS_BACKUP_COMPLETE_ID)로 상단 바에 알림을 새로 띄워 유지시킵니다.
            mZipHelper.updateNotification(NOTIFICATION_COMPRESS_COMPLETE_ID, "Export complete", "The exported file size is ${FileUtils.byteCountToDisplaySize(compressFile.length())}")
        } else {
            compressFile.delete()
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        if (!isExportSuccessful) {
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPRESS_ID)
        }
    }
}
