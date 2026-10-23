package me.blog.korn123.easydiary.workers

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.utils.FontUtils

class FullRecoveryWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private val mZipHelper = ZipHelper(context)
    private var isRecoverySuccessful = false

    override fun doWork(): Result {
        val notification =
            mZipHelper.createNotification(
                NOTIFICATION_DECOMPRESS_ID,
                "Full data recovery",
                "Recovery of all data is in progress.",
                NotificationConstants.ACTION_FULL_RECOVERY_CANCEL,
            )
        try {
            setForegroundAsync(
                ForegroundInfo(
                    NOTIFICATION_DECOMPRESS_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                ),
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val uri = Uri.parse(inputData.getString(WorkerConstants.URI_STRING))
        mZipHelper.decompress(uri)
        if (mZipHelper.isOnProgress) {
            FontUtils.setCommonTypeface(context)
            isRecoverySuccessful = true
            mZipHelper.updateNotification(NOTIFICATION_COMPRESS_COMPLETE_ID, "Import complete", "You can now select a restore point using the Restore Diary feature.")
        } else {
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        if (!isRecoverySuccessful) {
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
        }
    }
}
