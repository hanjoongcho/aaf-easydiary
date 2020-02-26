package me.blog.korn123.easydiary.workers

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPLETE_ID
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.helper.ZipHelper
import me.blog.korn123.easydiary.viewmodels.BackupOperations
import org.apache.commons.io.FileUtils
import java.io.File

class FullBackupWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private val mZipHelper = ZipHelper(context)

    override fun doWork(): Result {
        val uri = Uri.parse(inputData.getString(BackupOperations.URI_STRING))
        val os = context.contentResolver.openOutputStream(uri)

        val workingPath =  EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY
//        val destFileName = DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip"
//        val destFile = File(EasyDiaryUtils.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY + destFileName)
        val compressFile = File(workingPath, "bak.zip")
        if (compressFile.exists()) compressFile.delete()

        mZipHelper.determineFiles(workingPath)
        mZipHelper.printFileNames()
        mZipHelper.compress(compressFile)
        if (mZipHelper.isOnProgress) {
//            FileUtils.moveFile(compressFile, destFile)
            FileUtils.copyFile(compressFile, os)
            os?.close()
            mZipHelper.updateNotification("Export complete", "The exported file size is ${FileUtils.byteCountToDisplaySize(compressFile.length())}")
        } else {
            compressFile.delete()
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPLETE_ID)
    }
}