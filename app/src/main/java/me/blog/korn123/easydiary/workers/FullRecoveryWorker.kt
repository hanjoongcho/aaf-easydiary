package me.blog.korn123.easydiary.workers

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.blog.korn123.easydiary.R
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
                context.getString(R.string.recovery_full_notification_title),
                context.getString(R.string.recovery_full_notification_progress),
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
        when (mZipHelper.decompress(uri, inputData.getString(WorkerConstants.PASSWORD_STRING))) {
            ZipHelper.ExtractionResult.COMPLETED -> {
                when (mZipHelper.isOnProgress) {
                    true -> {
                        FontUtils.setCommonTypeface(context)
                        isRecoverySuccessful = true
                        mZipHelper.updateNotification(
                            NOTIFICATION_COMPRESS_COMPLETE_ID,
                            context.getString(R.string.recovery_full_complete_title),
                            context.getString(R.string.recovery_full_complete_message),
                        )
                    }

                    false -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
                }
            }

            ZipHelper.ExtractionResult.WRONG_PASSWORD ->
                mZipHelper.updateNotification(
                    NOTIFICATION_DECOMPRESS_ID,
                    context.getString(R.string.recovery_full_failed_title),
                    context.getString(R.string.recovery_full_password_wrong),
                )

            ZipHelper.ExtractionResult.INVALID_ARCHIVE ->
                mZipHelper.updateNotification(
                    NOTIFICATION_DECOMPRESS_ID,
                    context.getString(R.string.recovery_full_failed_title),
                    context.getString(R.string.recovery_full_invalid_archive),
                )

            else -> NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
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
