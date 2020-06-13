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
import me.blog.korn123.commons.utils.EasyDiaryUtils.jsonFileToHashMap
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
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
                val map = jsonFileToHashMap(jsonFilename)
                context.config.run {
                    // Settings Basic
                    primaryColor = (map[PRIMARY_COLOR] as Double).toInt()
                    backgroundColor = (map[BACKGROUND_COLOR] as Double).toInt()
                    screenBackgroundColor = (map[SETTING_CARD_VIEW_BACKGROUND_COLOR] as Double).toInt()
                    textColor = (map[TEXT_COLOR] as Double).toInt()
                    settingThumbnailSize = (map[SETTING_THUMBNAIL_SIZE] as Double).toFloat()
                    enableContentsSummary = map[SETTING_CONTENTS_SUMMARY] as Boolean
                    summaryMaxLines =(map[SETTING_SUMMARY_MAX_LINES] as Double).toInt()
                    enableCardViewPolicy = map[ENABLE_CARD_VIEW_POLICY] as Boolean
                    multiPickerEnable = map[SETTING_MULTIPLE_PICKER] as Boolean
                    diarySearchQueryCaseSensitive = map[DIARY_SEARCH_QUERY_CASE_SENSITIVE] as Boolean
                    calendarStartDay = (map[SETTING_CALENDAR_START_DAY] as Double).toInt()
                    calendarSorting = (map[SETTING_CALENDAR_SORTING] as Double).toInt()
                    enableCountCharacters = map[SETTING_COUNT_CHARACTERS] as Boolean
                    holdPositionEnterEditScreen = map[HOLD_POSITION_ENTER_EDIT_SCREEN] as Boolean

                    // Settings font
                    settingFontName = map[SETTING_FONT_NAME] as String
                    lineSpacingScaleFactor = (map[LINE_SPACING_SCALE_FACTOR] as Double).toFloat()
                    settingFontSize = (map[SETTING_FONT_SIZE] as Double).toFloat()
                    settingCalendarFontScale = (map[SETTING_CALENDAR_FONT_SCALE] as Double).toFloat()
                    boldStyleEnable = map[SETTING_BOLD_STYLE] as Boolean

                    // Settings Lock

                    updatePreference = true
                }
            }
            FontUtils.setCommonTypeface(context)
            mZipHelper.updateNotification(NOTIFICATION_DECOMPRESS_ID, "Import complete", "You can now select a restore point using the Restore Diary feature.")
        } else {
            NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        mZipHelper.isOnProgress = false
        NotificationManagerCompat.from(applicationContext).cancel(NOTIFICATION_DECOMPRESS_ID)
    }
}