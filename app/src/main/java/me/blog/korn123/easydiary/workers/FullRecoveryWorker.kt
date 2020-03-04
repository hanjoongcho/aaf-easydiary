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

class FullRecoveryWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private val mZipHelper = ZipHelper(context)

    override fun doWork(): Result {
        val uri = Uri.parse(inputData.getString(BackupOperations.URI_STRING))
        mZipHelper.decompress(uri)

        if (mZipHelper.isOnProgress) {
            mZipHelper.updateNotification("Import complete", "You can now select a restore point using the Restore Diary feature.")
        } else {
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPLETE_ID)
    }
}