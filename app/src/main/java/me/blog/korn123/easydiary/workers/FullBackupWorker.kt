package me.blog.korn123.easydiary.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.helper.ZipHelper
import org.apache.commons.io.FileUtils
import java.io.File

class FullBackupWorker(val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    private val mZipHelper = ZipHelper(appContext)

    override fun doWork(): Result {
        val workingPath =  EasyDiaryUtils.getApplicationDataDirectory(appContext) + WORKING_DIRECTORY
        val destFileName = DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip"
        val destFile = File(EasyDiaryUtils.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY + destFileName)
        val compressFile = File(workingPath, "bak.zip")
        if (compressFile.exists()) compressFile.delete()

        mZipHelper.determineFiles(workingPath)
        mZipHelper.printFileNames()
        mZipHelper.compress(compressFile)
        if (mZipHelper.isOnProgress) {
            FileUtils.moveFile(compressFile, destFile)
            mZipHelper.updateNotification("Export complete", WORKING_DIRECTORY + destFileName)
        } else {
            compressFile.delete()
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
    }
}