package me.blog.korn123.easydiary.helper

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity
import me.blog.korn123.easydiary.extensions.isScreenOn
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.extensions.reExecuteGmsBackup
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.fetchData
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.services.FullBackupService
import java.util.Calendar

class AlarmWorkExecutor(context: Context) : BaseAlarmWorkExecutor(context) {

    override fun executeWork(alarm: Alarm) {
        super.executeWork(alarm)

        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_GMS -> {
//                    executeGmsBackup(alarm)
                    scheduleNextAlarm(alarm, isScreenOn())
                    GoogleOAuthHelper.getGoogleSignAccount(this)?.account?.let { account ->
                        DriveServiceHelper(this, account).run {
                            initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                                if (photoFolderId != null) {
                                    Intent(context, FullBackupService::class.java).apply {
                                        putExtra(DriveServiceHelper.WORKING_FOLDER_ID, photoFolderId)
                                        putExtra(SettingsScheduleFragment.ALARM_ID, alarm.id)
                                        ContextCompat.startForegroundService(context, this)
                                    }
                                } else {
                                    reExecuteGmsBackup(alarm, "The photo folder ID not valid.", AlarmWorkExecutor::class.java.name)
                                }
                            }
                        }
                    } ?: reExecuteGmsBackup(alarm, "Authentication token is not valid.", AlarmWorkExecutor::class.java.name)

                }
                Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> {
                    val calendarService =
                        GoogleOAuthHelper.getCalendarService(context, GoogleOAuthHelper.getCalendarCredential(context))
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = calendarService.calendarList().list().execute()
                        result.items.forEach { calendar ->
                            fetchData(context, calendarService, calendar.id, null)
                        }
                    }
                    openNotification(alarm)
                }
                else -> {}
            }
        }
    }
}