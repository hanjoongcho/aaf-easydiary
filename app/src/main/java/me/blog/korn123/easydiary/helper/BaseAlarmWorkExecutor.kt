package me.blog.korn123.easydiary.helper

import android.content.Context
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
import me.blog.korn123.easydiary.extensions.exportRealmFile
import me.blog.korn123.easydiary.extensions.openNotification
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.Diary
import java.util.Calendar

open class BaseAlarmWorkExecutor(val context: Context) {
    open fun executeWork(alarm: Alarm) {
        context.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> {
                    exportRealmFile()
                    openNotification(alarm)
                }
                Alarm.WORK_MODE_DIARY_WRITING -> openNotification(alarm)
            }
        }
    }
}