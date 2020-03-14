package me.blog.korn123.easydiary.workers

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simplemobiletools.commons.helpers.BACKGROUND_COLOR
import com.simplemobiletools.commons.helpers.PRIMARY_COLOR
import com.simplemobiletools.commons.helpers.SETTING_CARD_VIEW_BACKGROUND_COLOR
import com.simplemobiletools.commons.helpers.TEXT_COLOR
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.jsonFileToHashMap
import me.blog.korn123.easydiary.helper.NOTIFICATION_COMPLETE_ID
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.helper.ZipHelper
import me.blog.korn123.easydiary.viewmodels.BackupOperations
import java.io.File

class FullRecoveryWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private val mZipHelper = ZipHelper(context)

    override fun doWork(): Result {
        val uri = Uri.parse(inputData.getString(BackupOperations.URI_STRING))
        mZipHelper.decompress(uri)
        if (mZipHelper.isOnProgress) {
            val jsonFilename = EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY + "preference.json"
            if (File(jsonFilename).exists()) {
                val map = context.jsonFileToHashMap(jsonFilename)
                context.config.run {
                    primaryColor = (map[PRIMARY_COLOR] as Double).toInt()
                    backgroundColor = (map[BACKGROUND_COLOR] as Double).toInt()
                    textColor = (map[TEXT_COLOR] as Double).toInt()
                    screenBackgroundColor = (map[SETTING_CARD_VIEW_BACKGROUND_COLOR] as Double).toInt()
                    updatePreference = true
                }
            }
            mZipHelper.updateNotification("Import complete", "You can now select a restore point using the Restore Diary feature.")
        } else {}
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_COMPLETE_ID)
    }
}