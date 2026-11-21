package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.location.LocationManagerCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.formatMinutesToTimeString
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.BACKGROUND_COLOR
import com.simplemobiletools.commons.helpers.DAY_MINUTES
import com.simplemobiletools.commons.helpers.FRIDAY_BIT
import com.simplemobiletools.commons.helpers.MONDAY_BIT
import com.simplemobiletools.commons.helpers.PERMISSION_CALL_PHONE
import com.simplemobiletools.commons.helpers.PERMISSION_CAMERA
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CALENDAR
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CONTACTS
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import com.simplemobiletools.commons.helpers.PERMISSION_RECORD_AUDIO
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_CALENDAR
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_CONTACTS
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.helpers.PRIMARY_COLOR
import com.simplemobiletools.commons.helpers.SATURDAY_BIT
import com.simplemobiletools.commons.helpers.SETTING_CARD_VIEW_BACKGROUND_COLOR
import com.simplemobiletools.commons.helpers.SUNDAY_BIT
import com.simplemobiletools.commons.helpers.SUNDAY_FIRST
import com.simplemobiletools.commons.helpers.TEXT_COLOR
import com.simplemobiletools.commons.helpers.THURSDAY_BIT
import com.simplemobiletools.commons.helpers.TUESDAY_BIT
import com.simplemobiletools.commons.helpers.USE_24_HOUR_FORMAT
import com.simplemobiletools.commons.helpers.WEDNESDAY_BIT
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.views.MyAppCompatSpinner
import com.simplemobiletools.commons.views.MyButton
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.commons.views.MyFloatingActionButton
import com.simplemobiletools.commons.views.MySeekBar
import com.simplemobiletools.commons.views.MySwitchCompat
import com.simplemobiletools.commons.views.MyTextView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import io.noties.markwon.utils.Dip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.activities.DiaryWritingActivity
import me.blog.korn123.easydiary.databinding.DialogMessageBinding
import me.blog.korn123.easydiary.databinding.PartialDialogTitleBinding
import me.blog.korn123.easydiary.di.ApplicationScope
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.NotificationInfo
import me.blog.korn123.easydiary.domain.repository.ActionLogRepository
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.enums.ActionLogKey
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.enums.DateTimeFormat
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.enums.TimeFormatMode
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.helper.AlarmConstants
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.helper.BACKUP_DB_DIRECTORY
import me.blog.korn123.easydiary.helper.CAPTURE_CAMERA_FILE_NAME
import me.blog.korn123.easydiary.helper.Config
import me.blog.korn123.easydiary.helper.DAILY_TODO
import me.blog.korn123.easydiary.helper.DIARY_EXECUTION_MODE
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_SEARCH_QUERY_CASE_SENSITIVE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.DOZE_SCHEDULE
import me.blog.korn123.easydiary.helper.ENABLE_CARD_VIEW_POLICY
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_ACCESS_FROM_OUTSIDE
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.HOLD_POSITION_ENTER_EDIT_SCREEN
import me.blog.korn123.easydiary.helper.LINE_SPACING_SCALE_FACTOR
import me.blog.korn123.easydiary.helper.MIME_TYPE_BINARY
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_ID
import me.blog.korn123.easydiary.helper.NotificationConstants
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_COARSE_LOCATION
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_FINE_LOCATION
import me.blog.korn123.easydiary.helper.RoomConstants
import me.blog.korn123.easydiary.helper.SETTING_BOLD_STYLE
import me.blog.korn123.easydiary.helper.SETTING_CALENDAR_FONT_SCALE
import me.blog.korn123.easydiary.helper.SETTING_CALENDAR_SORTING
import me.blog.korn123.easydiary.helper.SETTING_CALENDAR_START_DAY
import me.blog.korn123.easydiary.helper.SETTING_CONTENTS_SUMMARY
import me.blog.korn123.easydiary.helper.SETTING_COUNT_CHARACTERS
import me.blog.korn123.easydiary.helper.SETTING_FONT_NAME
import me.blog.korn123.easydiary.helper.SETTING_FONT_SIZE
import me.blog.korn123.easydiary.helper.SETTING_SELECTED_SYMBOLS
import me.blog.korn123.easydiary.helper.SETTING_SUMMARY_MAX_LINES
import me.blog.korn123.easydiary.helper.SETTING_THUMBNAIL_SIZE
import me.blog.korn123.easydiary.helper.SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_DP
import me.blog.korn123.easydiary.helper.SettingConstants
import me.blog.korn123.easydiary.receivers.AlarmReceiver
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.utils.ClickableLinkUtils
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.DiaryLinkRange
import me.blog.korn123.easydiary.utils.DiaryLinkTarget
import me.blog.korn123.easydiary.utils.DiaryLinkUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import me.blog.korn123.easydiary.views.FixedCardView
import me.blog.korn123.easydiary.views.FixedTextView
import me.blog.korn123.easydiary.views.ItemCardView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt
import me.blog.korn123.easydiary.domain.model.ActionLog as ActionLogDomain
import me.blog.korn123.easydiary.domain.model.Alarm as AlarmDomain
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.domain.model.PhotoUri as PhotoUriDomain

/**
 * Created by CHO HANJOONG on 2018-02-06.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

/***************************************************************************************************
 *   Hilt EntryPoint for Repository access in extensions
 ***************************************************************************************************/

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmRepositoryEntryPoint {
    fun alarmRepository(): AlarmRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DiaryRepositoryEntryPoint {
    fun diaryRepository(): DiaryRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ActionLogRepositoryEntryPoint {
    fun actionLogRepository(): ActionLogRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DDayRepositoryEntryPoint {
    fun dDayRepository(): DDayRepository
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApplicationScopeEntryPoint {
    @ApplicationScope
    fun applicationScope(): CoroutineScope
}

val Context.alarmRepository: AlarmRepository
    get() =
        EntryPointAccessors
            .fromApplication(
                this,
                AlarmRepositoryEntryPoint::class.java,
            ).alarmRepository()

val Context.diaryRepository: DiaryRepository
    get() =
        EntryPointAccessors
            .fromApplication(
                applicationContext,
                DiaryRepositoryEntryPoint::class.java,
            ).diaryRepository()

val Context.actionLogRepository: ActionLogRepository
    get() =
        EntryPointAccessors
            .fromApplication(
                applicationContext,
                ActionLogRepositoryEntryPoint::class.java,
            ).actionLogRepository()

val Context.dDayRepository: DDayRepository
    get() =
        EntryPointAccessors
            .fromApplication(
                applicationContext,
                DDayRepositoryEntryPoint::class.java,
            ).dDayRepository()

val Context.applicationScope: CoroutineScope
    get() =
        EntryPointAccessors
            .fromApplication(
                applicationContext,
                ApplicationScopeEntryPoint::class.java,
            ).applicationScope()

/***************************************************************************************************
 *   Alarm Extension
 *
 ***************************************************************************************************/
fun Context.openNotification(alarm: AlarmDomain) {
    val pendingIntent = getOpenAlarmTabIntent(alarm)
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.alarmId, notification)

    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    if (isScreenOn()) {
        scheduleNextAlarm(alarm, true)
    } else {
        scheduleNextAlarm(alarm, false)
        powerManager
            .newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "myApp:notificationLock",
            ).apply {
                acquire(3000)
            }
    }
}

fun Context.reExecuteGmsBackup(
    alarm: AlarmDomain,
    errorMessage: String,
    className: String,
) {
    applicationScope.launch {
        actionLogRepository.insertActionLog(
            ActionLogDomain(
                className = className,
                signature = "reExecuteGmsBackup",
                key = ActionLogKey.WARN,
                value = errorMessage,
            ),
        )
        alarmRepository.updateAlarm(alarm.copy(retryCount = alarm.retryCount + 1))
        openSnoozeNotification(alarm)
    }
}

// fun Context.executeGmsBackup(alarm: Alarm) {
//    val realmPath = EasyDiaryDbHelper.getRealmPath()
//    GoogleOAuthHelper.getGoogleSignAccount(this)?.account?.let { account ->
//        DriveServiceHelper(this, account).run {
//            initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) { realmFolderId ->
//                if (realmFolderId != null) {
//                    createFile(
//                        realmFolderId, realmPath,
//                        DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
//                        EasyDiaryUtils.easyDiaryMimeType
//                    ).addOnSuccessListener {
//                        openNotification(alarm)
//                    }.addOnFailureListener {}
//                } else {
//                    reExecuteGmsBackup(alarm, "The realm folder ID is not valid.")
//                }
//            }
//        }
//    }
// }

@SuppressLint("NewApi", "LaunchActivityFromNotification")
fun Context.openSnoozeNotification(alarm: AlarmDomain) {
    val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    if (isOreoPlus()) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel("${NOTIFICATION_CHANNEL_ID}_alarm", getString(R.string.notification_channel_name_alarm), importance)
        channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(channel)
    }

    val builder =
        NotificationCompat
            .Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_alarm")
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_easydiary)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_schedule_error))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle(
                if (config.enableDebugOptionVisibleAlarmSequence) {
                    "[${alarm.alarmId}] ${getString(
                        R.string.schedule_gms_error_title,
                    )}"
                } else {
                    getString(R.string.schedule_gms_error_title)
                },
            ).setContentText(getString(R.string.schedule_gms_error_message))
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText(
                        getString(R.string.schedule_gms_error_message),
                    ).setSummaryText(getString(R.string.schedule_gms_error_title)),
            ).setContentIntent(
                PendingIntent.getBroadcast(
                    this,
                    alarm.alarmId,
                    Intent(this, AlarmReceiver::class.java).apply {
                        putExtra(DOZE_SCHEDULE, true)
                    },
                    pendingIntentFlag(),
                ),
            )
    notificationManager.notify(alarm.alarmId, builder.build())
}

fun Context.getOpenAlarmTabIntent(alarm: AlarmDomain): PendingIntent {
    val intent: Intent? =
        when (alarm.workMode) {
            AlarmConstants.WORK_MODE_DIARY_WRITING -> {
                Intent(this, DiaryWritingActivity::class.java).apply {
                    putExtra(DIARY_EXECUTION_MODE, EXECUTION_MODE_ACCESS_FROM_OUTSIDE)
                }
            }

            AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL, AlarmConstants.WORK_MODE_DIARY_BACKUP_GMS, AlarmConstants.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> {
                Intent(this, DiaryMainActivity::class.java)
            }

            else -> {
                null
            }
        }
    return PendingIntent.getActivity(this, alarm.alarmId, intent, pendingIntentFlag())
}

fun Context.getAlarmIntent(alarm: AlarmDomain): PendingIntent {
    val intent = Intent(this, AlarmReceiver::class.java)
    intent.putExtra(SettingConstants.ALARM_ID, alarm.alarmId)
    return PendingIntent.getBroadcast(this, alarm.alarmId, intent, pendingIntentFlag())
}

fun Context.cancelAlarmClock(alarm: AlarmDomain) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getAlarmIntent(alarm))
}

fun Context.scheduleNextAlarm(
    alarm: AlarmDomain,
    showToast: Boolean,
) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) return

    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    for (i in 0..7) {
        val currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val isCorrectDay = alarm.days and 2.0.pow(currentDay).toInt() != 0
        val currentTimeInMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        if (isCorrectDay && (alarm.timeInMinutes > currentTimeInMinutes || i > 0)) {
            val triggerInMinutes = alarm.timeInMinutes - currentTimeInMinutes + (i * DAY_MINUTES)
            setupAlarmClock(alarm, triggerInMinutes * 60 - calendar.get(Calendar.SECOND))

            if (showToast) {
                showRemainingTimeMessage(triggerInMinutes)
            }
            break
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun Context.setupAlarmClock(
    alarm: AlarmDomain,
    triggerInSeconds: Int,
) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(alarm), getAlarmIntent(alarm))
}

fun Context.showRemainingTimeMessage(totalMinutes: Int) {
    val fullString = String.format("Time remaining till the alarm goes off: %s", formatMinutesToTimeString(totalMinutes))
    Handler(Looper.getMainLooper()).post {
        toast(fullString, Toast.LENGTH_LONG)
    }
}

fun Context.executeScheduledTask(alarm: AlarmDomain) {
    AlarmWorkExecutor(this).run { executeWork(alarm) }
}

fun Context.rescheduleEnabledAlarms() {
    applicationScope.launch {
        alarmRepository.getAllAlarms().forEach {
            if (it.isEnabled) scheduleNextAlarm(it, false)
        }
    }
}

@SuppressLint("NewApi")
fun Context.getAlarmNotification(
    pendingIntent: PendingIntent,
    alarm: AlarmDomain,
): Notification {
    if (isOreoPlus()) {
        // Create the NotificationChannel
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel("${NOTIFICATION_CHANNEL_ID}_alarm", getString(R.string.notification_channel_name_alarm), importance)
        channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    var largeIcon: Bitmap? = null
    var description: String? = null
    when (alarm.workMode) {
        AlarmConstants.WORK_MODE_DIARY_WRITING -> {
            largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_diary_writing)
            description = getString(R.string.schedule_diary_writing_complete)
        }

        AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL -> {
            largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_diary_backup_local)
            description = getString(R.string.schedule_backup_local_complete)
        }

        AlarmConstants.WORK_MODE_DIARY_BACKUP_GMS -> {
            largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_googledrive_upload)
            description = getString(R.string.schedule_backup_gms_complete)
        }

        AlarmConstants.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> {
            largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logo_google_calendar)
            description = "Calendar schedule has been created as a diary."
        }
    }
    val builder =
        NotificationCompat
            .Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_alarm")
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_easydiary)
            .setLargeIcon(largeIcon)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle(if (config.enableDebugOptionVisibleAlarmSequence) "[${alarm.alarmId}] ${alarm.label}" else alarm.label)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notification = builder.build()
//    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getSelectedDaysString(bitMask: Int): String {
    val dayBits = arrayListOf(MONDAY_BIT, TUESDAY_BIT, WEDNESDAY_BIT, THURSDAY_BIT, FRIDAY_BIT, SATURDAY_BIT, SUNDAY_BIT)
    val weekDays = resources.getStringArray(R.array.week_days).toMutableList()

    // 일요일 시작 설정에 따른 데이터 재배치
    if (baseConfig.isSundayFirst) {
        val lastBit = dayBits.removeAt(dayBits.size - 1)
        dayBits.add(0, lastBit)

        val lastDay = weekDays.removeAt(weekDays.size - 1)
        weekDays.add(0, lastDay)
    }

    // 필터링 및 문자열 결합 (Sequence와 joinToString 활용)
    return dayBits.indices
        .filter { index -> (bitMask and dayBits[index]) != 0 }
        .joinToString(", ") { index ->
            weekDays[index].take(3) // substringTo 대신 take(3) 사용
        }
}

/***************************************************************************************************
 *   Task Notification
 *
 ***************************************************************************************************/

fun Context.openOverDueNotification(diaryList: List<DiaryDomain>) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationStartId = 9000

    fun createNotification(
        notificationInfo: NotificationInfo,
        bitmap: Bitmap? = null,
    ): NotificationCompat.Builder {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", getString(R.string.notification_channel_name_dev), importance)
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev").apply {
                setDefaults(Notification.DEFAULT_ALL)
                setWhen(System.currentTimeMillis())
                setSmallIcon(R.drawable.ic_easydiary)
                setOnlyAlertOnce(true)
                setOngoing(false)
                setAutoCancel(true)
            }

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                R.drawable.ic_easydiary,
                getString(R.string.dismiss),
                PendingIntent.getService(
                    this,
                    notificationInfo.id,
                    Intent(this, NotificationService::class.java).apply {
                        action = NotificationConstants.ACTION_DEV_DISMISS
                        putExtra(
                            NOTIFICATION_ID,
                            notificationInfo.id, // An identifier for this notification unique within your application.
                        )
                    },
                    pendingIntentFlag(),
                ),
            )
        }

        return notificationBuilder
    }

    fun sendNotification(diary: DiaryDomain) {
        val notification =
            NotificationInfo(
                if (diary.symbolSequence == DAILY_TODO) R.drawable.ic_todo else R.drawable.ic_doing,
                useActionButton = true,
                (notificationStartId + diary.diaryId).toInt(),
            )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat
                .from(this)
                .notify(
                    notification.id,
                    createNotification(notification)
                        .also {
                            val dday = DateUtils.getOnlyDayRemaining(diary.currentTimeMillis)
                            val contentTitle = "${if (dday.contains(
                                    "＋",
                                )
                            ) {
                                "\uD83D\uDD34"
                            } else {
                                "\uD83D\uDFE2"
                            }} 《$dday》 ${DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis)}"
                            val contentText = if (diary.title.isNullOrEmpty()) diary.contents!!.split("\n")[0] else diary.title
//                    contentText += diary.contents
                            it.setContentTitle(contentTitle)
                            it.setContentText(contentText)
//                    it.setLargeIcon(
//                        BitmapFactory.decodeResource(
//                            resources,
//                            notification.largeIconResourceId
//                        )
//                    )
                            it.setContentIntent(
                                PendingIntent.getActivity(
                                    this,
                                    notification.id,
                                    Intent(this, DiaryReadingActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        putExtra(DIARY_SEQUENCE, diary.diaryId)
                                    },
                                    pendingIntentFlag(),
                                ),
                            )
                        }.build(),
                )
        }
    }

    diaryList.forEach { diary ->
        sendNotification(diary)
    }
}

/***************************************************************************************************
 *   Messages
 *
 ***************************************************************************************************/
fun Context.makeToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.makeSnackBar(
    view: View,
    message: String,
) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
}

/**
 * TODO
 *
 * @param message
 * @param positiveListener
 * @param negativeListener
 * @param dialogMode
 * @param cancelable
 * @param paramTitle
 * @param positiveButtonLabel
 * @param negativeButtonLabel
 */
fun Context.showAlertDialog(
    message: String,
    positiveListener: DialogInterface.OnClickListener?,
    negativeListener: DialogInterface.OnClickListener?,
    dialogMode: DialogMode = DialogMode.DEFAULT,
    cancelable: Boolean = true,
    paramTitle: String? = null,
    positiveButtonLabel: String = getString(R.string.ok),
    negativeButtonLabel: String = getString(R.string.cancel),
) {
    showAlertDialog(
        message,
        positiveListener,
        negativeListener,
        null,
        dialogMode,
        cancelable,
        paramTitle,
        positiveButtonLabel,
        negativeButtonLabel,
    )
}

/**
 * TODO
 *
 * @param message
 * @param positiveListener
 * @param negativeListener
 * @param neutralListener
 * @param dialogMode
 * @param cancelable
 * @param paramTitle
 * @param positiveButtonLabel
 * @param negativeButtonLabel
 * @param neutralButtonLabel
 */
fun Context.showAlertDialog(
    message: String,
    positiveListener: DialogInterface.OnClickListener?,
    negativeListener: DialogInterface.OnClickListener?,
    neutralListener: DialogInterface.OnClickListener?,
    dialogMode: DialogMode = DialogMode.DEFAULT,
    cancelable: Boolean = true,
    paramTitle: String? = null,
    positiveButtonLabel: String = getString(R.string.ok),
    negativeButtonLabel: String = getString(R.string.cancel),
    neutralButtonLabel: String = "-",
) {
    var iconResourceId: Int? = null
    var title: String? = null
    when (dialogMode) {
        DialogMode.INFO -> {
            title = getString(R.string.ok)
            iconResourceId = R.drawable.ic_info
        }

        DialogMode.WARNING -> {
            title = "WARNING"
            iconResourceId = R.drawable.ic_warning
        }

        DialogMode.ERROR -> {
            title = "ERROR"
            iconResourceId = R.drawable.ic_error
        }

        DialogMode.SETTING -> {
            title = getString(R.string.ok)
            iconResourceId = R.drawable.ic_settings_7
        }

        DialogMode.DEFAULT -> {
            title = getString(R.string.app_name)
            iconResourceId = R.drawable.ic_easydiary
        }
    }

    val builder = AlertDialog.Builder(this)
    builder.setCancelable(cancelable)
    builder.setPositiveButton(positiveButtonLabel, positiveListener)
    negativeListener?.let { builder.setNegativeButton(negativeButtonLabel, it) }
    neutralListener?.let { builder.setNeutralButton(neutralButtonLabel, it) }
    builder.create().apply {
        updateAlertDialog(this, message, null, paramTitle ?: title, 255, iconResourceId)
    }
}

@Deprecated(
    message = "Legacy function",
    replaceWith =
        ReplaceWith(
            "showAlertDialogWithIcon()",
            "me.blog.korn123.easydiary.extensions.Context",
        ),
)
fun Context.showAlertDialog(
    message: String,
    positiveListener: DialogInterface.OnClickListener?,
    cancelable: Boolean = true,
) {
    showAlertDialog(message, positiveListener, null, DialogMode.INFO, cancelable)
}

fun Context.showAlertDialog(message: String) {
    showAlertDialog(message, null, null, DialogMode.INFO, true)
}

fun Context.updateAlertDialogWithIcon(
    dialogMode: DialogMode,
    alertDialog: AlertDialog,
    message: String? = null,
    customView: View? = null,
    customTitle: String? = null,
    backgroundAlpha: Int = 255,
) {
    var title: String? = null
    var iconResourceId: Int? = null
    when (dialogMode) {
        DialogMode.INFO -> {
            title = getString(R.string.ok)
            iconResourceId = R.drawable.ic_info
        }

        DialogMode.SETTING -> {
            title = getString(R.string.settings)
            iconResourceId = R.drawable.ic_settings_7
        }

        else -> {}
    }

    updateAlertDialog(
        alertDialog,
        message,
        customView,
        customTitle ?: title,
        backgroundAlpha,
        iconResourceId,
    )
}

fun Context.updateAlertDialog(
    alertDialog: AlertDialog,
    message: String? = null,
    customView: View? = null,
    customTitle: String? = null,
    backgroundAlpha: Int = 255,
    customTitleIcon: Int? = null,
) {
    alertDialog.run {
        when (customView == null) {
            true -> {
                DialogMessageBinding.inflate(layoutInflater).apply {
                    root.apply {
                        simpleMessage.text = message
                        if (this is ViewGroup) {
                            this.setBackgroundColor(config.backgroundColor)
                            initTextSize(this)
                            updateTextColors(this)
                            updateAppViews(this)
                            FontUtils.setFontsTypeface(this@updateAlertDialog, null, this)
                        }
                    }
                    setView(root)
                }
            }

            false -> {
                setView(customView)
            }
        }
//        if (!isNightMode()) window?.setBackgroundDrawable(ColorDrawable(config.backgroundColor))
        if (!isNightMode()) {
            window?.setBackgroundDrawable(
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(config.backgroundColor)
                    cornerRadius = dpToPixelFloatValue(3F)
                    alpha = backgroundAlpha
                },
            )
        }

        val globalTypeface = FontUtils.getCommonTypeface(this@updateAlertDialog)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        PartialDialogTitleBinding.inflate(layoutInflater).apply {
            textDialogTitle.run {
                text = customTitle ?: getString(R.string.app_name)
//                    if (!isNightMode()) setTextColor(config.textColor)
                if (!isNightMode()) setTextColor(Color.WHITE)
                typeface = globalTypeface
//                    val padding = dpToPixel(15F)
//                    setPadding(padding * 2, padding, padding * 2, padding)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18F)
            }
            customTitleIcon?.let {
                imgDialogTitle.run {
                    visibility = View.VISIBLE
                    setImageDrawable(ContextCompat.getDrawable(this@updateAlertDialog, it))
//                        changeDrawableIconColor(config.textColor, this)
                    changeDrawableIconColor(Color.WHITE, this)
                }
            }
            setCustomTitle(this.root)
        }
        show()
        getButton(AlertDialog.BUTTON_POSITIVE).run {
            if (!isNightMode()) setTextColor(config.textColor)
            typeface = globalTypeface
        }
        getButton(AlertDialog.BUTTON_NEGATIVE).run {
            if (!isNightMode()) setTextColor(config.textColor)
            typeface = globalTypeface
        }
        if (!isNightMode()) getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(config.textColor)
    }
}

/***************************************************************************************************
 *   ETC Extension
 *
 ***************************************************************************************************/
val Context.config: Config get() = Config.newInstance(this)

fun isBelowVanillaIceCream() = Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM

fun isVanillaIceCreamPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun isRedVelvetCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

fun Context.pendingIntentFlag() =
    if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.M
    ) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

fun Context.pendingIntentFlagMutable() =
    if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.M
    ) {
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

fun Context.isNightMode() =
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> false
        Configuration.UI_MODE_NIGHT_NO -> false
        else -> false
    }

fun Context.pauseLock() {
    if (config.aafPinLockEnable || config.fingerprintLockEnable) {
        // FIXME remove test code
//        Toast.makeText(this, "${this::class.java.simpleName}", Toast.LENGTH_LONG).show()
        config.aafPinLockPauseMillis = System.currentTimeMillis()
    }
}

fun Context.startDiaryReadingActivity(sequence: Long) {
    val diaryReadingIntent =
        Intent(this, DiaryReadingActivity::class.java).apply {
            putExtra(DIARY_SEQUENCE, sequence)
            if (this@startDiaryReadingActivity !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    startActivity(diaryReadingIntent)
}

/**
 * Opens the dialer with the number already filled in. ACTION_DIAL only prepares the call so
 * that the user still decides to place it, and the app needs no CALL_PHONE permission.
 */
fun Context.dialPhoneNumber(number: String) {
    val dialIntent =
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            if (this@dialPhoneNumber !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    startActivityOrNotify(dialIntent)
}

/** Opens a web page in a custom tab, the way the app opens its other external links. */
fun Context.openWebPage(url: String) {
    val customTabsIntent =
        CustomTabsIntent
            .Builder()
            .setToolbarColor(config.primaryColor)
            .setUrlBarHidingEnabled(true)
            .setShowTitle(false)
            .build()
    if (runCatching { customTabsIntent.launchUrl(this, Uri.parse(url)) }.isFailure) {
        makeToast(getString(R.string.no_app_found))
    }
}

private fun Context.startActivityOrNotify(intent: Intent) {
    if (runCatching { startActivity(intent) }.isFailure) {
        makeToast(getString(R.string.no_app_found))
    }
}

fun Context.updateTextColors(
    viewGroup: ViewGroup,
    tmpTextColor: Int = 0,
    tmpAccentColor: Int = 0,
) {
    if (isNightMode()) return

    val textColor = if (tmpTextColor == 0) config.textColor else tmpTextColor
    val backgroundColor = config.backgroundColor
    val accentColor =
        if (tmpAccentColor == 0) {
            if (isBlackAndWhiteTheme()) {
                Color.WHITE
            } else {
                config.primaryColor
            }
        } else {
            tmpAccentColor
        }

    val cnt = viewGroup.childCount
    (0 until cnt)
        .map { viewGroup.getChildAt(it) }
        .forEach {
            when (it) {
                is MyTextView -> {
                    it.setColors(textColor, accentColor, backgroundColor)
                }

                is FixedTextView -> {
                    if (it.applyGlobalColor) it.setColors(textColor, accentColor, backgroundColor)
                }

                is MyAppCompatSpinner -> {
                    it.setColors(textColor, accentColor, backgroundColor)
                }

                is MySwitchCompat -> {
                    it.setColors(textColor, accentColor, backgroundColor)
                }

                //                    is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
                is MyEditText -> {
                    it.setTextColor(textColor)
                    it.setHintTextColor(textColor.adjustAlpha(0.5f))
                    it.setLinkTextColor(accentColor)
                }

                is MyFloatingActionButton -> {
                    it.backgroundTintList = ColorStateList.valueOf(accentColor)
                }

                is MySeekBar -> {
                    it.setColors(textColor, accentColor, backgroundColor)
                }

                is MyButton -> {
                    it.setColors(textColor, accentColor, backgroundColor)
                }

                is ViewGroup -> {
                    updateTextColors(it, textColor, accentColor)
                }
            }
        }
}

fun Context.innerCardDarkenFactor(): Int = if (isColorLight(config.backgroundColor)) 2 else 2.unaryMinus()

fun Context.updateDashboardInnerCard(cardView: CardView) {
    cardView.setCardBackgroundColor(config.backgroundColor.darkenColor(innerCardDarkenFactor()))
}

fun Context.updateAppViews(
    viewGroup: ViewGroup,
    tmpBackgroundColor: Int = 0,
) {
    if (isNightMode()) return

    val backgroundColor = if (tmpBackgroundColor == 0) config.backgroundColor else tmpBackgroundColor
    val cnt = viewGroup.childCount
    (0 until cnt)
        .map { viewGroup.getChildAt(it) }
        .forEach {
            when (it) {
                is CardView -> {
                    when (it is FixedCardView) {
                        true -> {
                            if (it.applyCardBackgroundColor) it.setCardBackgroundColor(backgroundColor)
                            if (it.dashboardInnerCard) {
                                updateDashboardInnerCard(it)
                            }
                        }

                        false -> {
                            it.setCardBackgroundColor(backgroundColor)
                        }
                    }
                    updateAppViews(it)
                }

                is ViewGroup -> {
                    updateAppViews(it)
                }

                is RadioButton -> {
                    it.run {
                        setTextColor(config.textColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            buttonTintList =
                                ColorStateList(
                                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                    intArrayOf(
                                        config.textColor,
                                        config.textColor,
                                    ),
                                )
                        }
                    }
                }

                is CheckBox -> {
                    it.run {
                        setTextColor(config.textColor)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            buttonTintList =
                                ColorStateList(
                                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                    intArrayOf(
                                        config.textColor,
                                        config.textColor,
                                    ),
                                )
                        }
                    }
                }

                is SwitchCompat -> {
                    it.run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && config.primaryColor == config.backgroundColor) {
                            trackTintList =
                                ColorStateList(
                                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                    intArrayOf(
                                        ColorUtils.setAlphaComponent(config.textColor, 190),
                                        config.textColor,
                                    ),
                                )
                            thumbTintList =
                                ColorStateList(
                                    arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                    intArrayOf(
                                        ColorUtils.setAlphaComponent(config.textColor, 255),
                                        config.textColor,
                                    ),
                                )
                        }
                    }
                }

                is ProgressBar -> {
                    if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.LOLLIPOP
                    ) {
                        it.indeterminateTintList = ColorStateList.valueOf(config.primaryColor)
                    }
                }
            }
        }
}

fun Context.updateCardViewPolicy(viewGroup: ViewGroup) {
    if (isNightMode()) return

    val cnt = viewGroup.childCount
    (0 until cnt)
        .map { viewGroup.getChildAt(it) }
        .forEach {
            when (it) {
                is FixedCardView -> {
                    if (it.fixedAppcompatPadding) {
                        it.useCompatPadding = true
                        it.cardElevation = dpToPixelFloatValue(3F)
                        it.updateLayoutParams<MarginLayoutParams> {
                            val margin = dpToPixel(3F)
                            setMargins(margin, margin, margin, margin)
                        }
                    } else {
                        it.useCompatPadding = false
                        it.cardElevation = 0F
                    }

                    updateCardViewPolicy(it)
                }

                is CardView -> {
                    // Temporary code for Compose CardView UI/UX compatibility
                    var additionalMargin = 0

                    if (config.enableCardViewPolicy) {
                        if (it is ItemCardView && it.applyAdditionHorizontalMargin) {
                            additionalMargin = dpToPixel(3f)
                        }

                        it.useCompatPadding = true
                        it.cardElevation = dpToPixelFloatValue(2F)
                    } else {
                        it.useCompatPadding = false
                        it.cardElevation = 0F
                    }

                    val params = it.layoutParams as MarginLayoutParams
                    params.marginStart = additionalMargin
                    params.marginEnd = additionalMargin
                    it.layoutParams = params

                    updateCardViewPolicy(it)
                }

                is ViewGroup -> {
                    updateCardViewPolicy(it)
                }
            }
        }
}

fun Context.updateTextSize(
    viewGroup: ViewGroup,
    context: Context,
    addSize: Int,
) {
    if (isNightMode()) return

    val cnt = viewGroup.childCount
    val settingFontSize: Float = config.settingFontSize + addSize
    (0 until cnt)
        .map { index -> viewGroup.getChildAt(index) }
        .forEach {
            when (it) {
                is TextView -> {
                    it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                }

                is ViewGroup -> {
                    updateTextSize(it, context, addSize)
                }
            }
        }
}

fun Context.initTextSize(viewGroup: ViewGroup) {
    if (isNightMode()) return

    val cnt = viewGroup.childCount
    val defaultFontSize: Float = dpToPixelFloatValue(SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_DP.toFloat())
    val settingFontSize: Float = config.settingFontSize
    (0 until cnt)
        .map { index -> viewGroup.getChildAt(index) }
        .forEach {
            when (it) {
                is FixedTextView -> {
                    if (it.applyGlobalSize) it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                }

                is Button -> {}

                is TextView -> {
                    if (it.tag == "tabTitle") return
                    if (it.text == "Dashboard") return
                    when (it.id) {
                        R.id.contentsLength, R.id.locationLabel -> {
                            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize * 0.7F)
                        }

                        R.id.symbolTextArrow -> {}

                        R.id.createdDate -> {}

                        else -> {
                            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
                        }
                    }
                }

                is ViewGroup -> {
                    initTextSize(it)
                }
            }
        }
}

fun Context.initTextSize(textView: TextView) {
    if (isNightMode()) return

    val defaultFontSize: Float = dpToPixelFloatValue(SUPPORT_LANGUAGE_FONT_SIZE_DEFAULT_DP.toFloat())
    val settingFontSize: Float = config.settingFontSize
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, settingFontSize)
}

fun Context.updateDrawableColorInnerCardView(
    resourceId: Int,
    color: Int = config.textColor,
) {
    if (isNightMode()) return
    changeDrawableIconColor(color, resourceId)
}

fun Context.updateDrawableColorInnerCardView(
    imageView: ImageView,
    color: Int = config.textColor,
) {
    if (isNightMode()) return
    changeDrawableIconColor(color, imageView)
}

fun Context.changeDrawableIconColor(
    color: Int,
    imageView: ImageView,
) {
    imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
}

fun Context.changeDrawableIconColor(
    color: Int,
    resourceId: Int,
) {
    AppCompatResources.getDrawable(this, resourceId)?.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}

fun Context.checkPermission(permissions: Array<String>): Boolean =
    when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissions === EXTERNAL_STORAGE_PERMISSIONS) {
        true -> {
            true
        }

        false -> {
            val listDeniedPermissions: List<String> =
                permissions.filter { permission ->
                    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
                }
            listDeniedPermissions.isEmpty()
        }
    }

fun Context.preferencesContains(key: String): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.contains(key)
}

fun Context.applyFontToMenuItem(mi: MenuItem) {
    val mNewTitle = SpannableString(mi.title)
    mNewTitle.setSpan(CustomTypefaceSpan("", FontUtils.getCommonTypeface(this)!!), 0, mNewTitle.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    mi.title = mNewTitle
}

fun Context.getUriForFile(targetFile: File): Uri {
    val authority = "${this.packageName}.provider"
    return if (Build.VERSION.SDK_INT >
        Build.VERSION_CODES.M
    ) {
        FileProvider.getUriForFile(this, authority, targetFile)
    } else {
        Uri.fromFile(targetFile)
    }
}

fun Context.createTemporaryPhotoFile(
    uri: Uri? = null,
    fromUri: Boolean = false,
): File {
    val temporaryFile = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY, CAPTURE_CAMERA_FILE_NAME)
    if (temporaryFile.exists()) temporaryFile.delete()

    when (fromUri) {
        true -> {
            contentResolver.openInputStream(uri!!)?.use { inputStream ->
                FileOutputStream(temporaryFile.absoluteFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        false -> {
            temporaryFile.createNewFile()
        }
    }

    return temporaryFile
}

fun Context.preferenceToJsonString(): HashMap<String, Any> {
    val preferenceMap: HashMap<String, Any> = hashMapOf()

    config.run {
        // Simple Mobile Tools properties
        preferenceMap[PRIMARY_COLOR] = primaryColor
        preferenceMap[BACKGROUND_COLOR] = backgroundColor
        preferenceMap[SETTING_CARD_VIEW_BACKGROUND_COLOR] = screenBackgroundColor
        preferenceMap[TEXT_COLOR] = textColor
        preferenceMap[USE_24_HOUR_FORMAT] = use24HourFormat
        preferenceMap[SUNDAY_FIRST] = isSundayFirst

        // Awesome Application Factory legacy properties
//        preferenceMap[AAF_PIN_LOCK_PAUSE_MILLIS] = aafPinLockPauseMillis
//        preferenceMap[AAF_THEME_CHANGE] = isThemeChanged

        // Dev properties
        preferenceMap[DEV_STOCK_CHART_OPTIONS_FROM_MILLIS] = devStockChartOptionFromMillis
        preferenceMap[DEV_STOCK_ENABLE_EVALUATE_PRICE] = devStockEnableEvaluatePrice
        preferenceMap[DEV_STOCK_ENABLE_PRINCIPAL_HIGHLIGHT] = devStockEnablePrincipalHighlight

        // Easy Diary properties
        preferenceMap[SETTING_FONT_NAME] = settingFontName
//        preferenceMap[APP_LOCK_SAVED_PASSWORD] = aafPinLockSavedPassword
        preferenceMap[PREVIOUS_ACTIVITY] = previousActivity
        preferenceMap[SETTING_FONT_SIZE] = settingFontSize
        preferenceMap[DIARY_SEARCH_QUERY_CASE_SENSITIVE] = diarySearchQueryCaseSensitive
//        preferenceMap[APP_LOCK_ENABLE] = aafPinLockEnable
        preferenceMap[LINE_SPACING_SCALE_FACTOR] = lineSpacingScaleFactor
        preferenceMap[SETTING_THUMBNAIL_SIZE] = settingThumbnailSize
        preferenceMap[SETTING_CALENDAR_FONT_SCALE] = settingCalendarFontScale
        preferenceMap[SETTING_BOLD_STYLE] = boldStyleEnable
//        preferenceMap[SETTING_FINGERPRINT_LOCK] = fingerprintLockEnable
//        preferenceMap[FINGERPRINT_ENCRYPT_DATA] = fingerprintEncryptData
//        preferenceMap[FINGERPRINT_ENCRYPT_DATA_IV] = fingerprintEncryptDataIV
//        preferenceMap[FINGERPRINT_AUTHENTICATION_FAIL_COUNT] = fingerprintAuthenticationFailCount
        preferenceMap[ENABLE_CARD_VIEW_POLICY] = enableCardViewPolicy
        preferenceMap[SETTING_CONTENTS_SUMMARY] = enableContentsSummary
        preferenceMap[SETTING_CALENDAR_START_DAY] = calendarStartDay
        preferenceMap[SETTING_COUNT_CHARACTERS] = enableCountCharacters
        preferenceMap[SETTING_ENABLE_ELAPSED_DAYS] = enableElapsedDays
        preferenceMap[DIARY_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE] = diaryBackupGoogle
        preferenceMap[PHOTO_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE] = photoBackupGoogle
        preferenceMap[DIARY_LAST_BACKUP_TIMESTAMP_LOCAL] = diaryBackupLocal
        preferenceMap[SETTING_CALENDAR_SORTING] = calendarSorting
        preferenceMap[UPDATE_SHARED_PREFERENCE] = updatePreference
        preferenceMap[HOLD_POSITION_ENTER_EDIT_SCREEN] = holdPositionEnterEditScreen
        preferenceMap[SETTING_SUMMARY_MAX_LINES] = summaryMaxLines
        preferenceMap[SETTING_ENABLE_DEBUG_CONSOLE] = enableDebugMode
        preferenceMap[SETTING_LOCATION_INFO] = enableLocationInfo
        preferenceMap[SETTING_TASK_SYMBOL_TOP_ORDER] = enableTaskSymbolTopOrder
        preferenceMap[SETTING_SELECTED_SYMBOLS] = selectedSymbols
        preferenceMap[APP_EXECUTION_COUNT] = appExecutionCount
        preferenceMap[SETTING_ENABLE_REVIEW_FLOW] = enableReviewFlow
        preferenceMap[SETTING_POSTCARD_SPAN_COUNT_LANDSCAPE] = postcardSpanCountLandscape
        preferenceMap[SETTING_POSTCARD_SPAN_COUNT_PORTRAIT] = postcardSpanCountPortrait
        preferenceMap[SETTING_DIARY_MAIN_SPAN_COUNT_LANDSCAPE] = diaryMainSpanCountLandscape
        preferenceMap[SETTING_DIARY_MAIN_SPAN_COUNT_PORTRAIT] = diaryMainSpanCountPortrait
        preferenceMap[SETTING_ENABLE_PHOTO_HIGHLIGHT] = enablePhotoHighlight
        preferenceMap[SETTING_ENABLE_PHOTO_HIGHLIGHT_DIARY_MAIN] = enablePhotoHighlightInDiaryMain
        preferenceMap[SETTING_ENABLE_PHOTO_HIGHLIGHT_DASHBOARD] = enablePhotoHighlightInDashboard
        preferenceMap[SETTING_ENABLE_STATUSBAR_DARKEN_COLOR] = enableStatusBarDarkenColor
        preferenceMap[SETTING_ENABLE_WELCOME_DASHBOARD_POPUP] = enableWelcomeDashboardPopup
        preferenceMap[SETTING_ENABLE_DDAY_FLEXBOX_LAYOUT] = enableDDayFlexboxLayout
        preferenceMap[SETTING_ENABLE_DASHBOARD_CALENDAR] = enableDashboardCalendar
        preferenceMap[SETTING_DATETIME_FORMAT] = settingDatetimeFormat
        preferenceMap[SETTING_ENABLE_ALL_DAY_DEFAULT] = enableAllDayDefault
        preferenceMap[SETTING_TIME_FORMAT] = settingTimeFormat
        preferenceMap[SETTING_ENABLE_MARKDOWN] = enableMarkdown
        preferenceMap[SETTING_DEBUG_OPTION_TOAST_LOCATION] = enableDebugOptionToastLocation
        preferenceMap[SETTING_DEBUG_OPTION_TOAST_ATTACHED_PHOTO] = enableDebugOptionToastAttachedPhoto
        preferenceMap[SETTING_DEBUG_OPTION_TOAST_NOTIFICATION_INFO] = enableDebugOptionToastNotificationInfo
        preferenceMap[SETTING_DEBUG_OPTION_TOAST_REVIEW_FLOW_INFO] = enableDebugOptionToastReviewFlowInfo
        preferenceMap[SETTING_DEBUG_OPTION_TOAST_PHOTO_HIGHLIGHT_UPDATE_TIME] = enableDebugOptionToastPhotoHighlightUpdateTime
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_DIARY_SEQUENCE] = enableDebugOptionVisibleDiarySequence
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_ALARM_SEQUENCE] = enableDebugOptionVisibleAlarmSequence
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_TREE_STATUS] = enableDebugOptionVisibleTreeStatus
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_CHART_WEIGHT] = enableDebugOptionVisibleChartWeight
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_CHART_STOCK] = enableDebugOptionVisibleChartStock
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_FONT_PREVIEW_EMOJI] = enableDebugOptionVisibleFontPreviewEmoji
        preferenceMap[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_TEMPORARY_DIARY] = enableDebugOptionVisibleTemporaryDiary
        preferenceMap[SETTING_ENABLE_JETPACK_ROOM_DATABASE] = enableJetpackRoomDatabase
        preferenceMap[SETTING_GALLERY_SPAN_COUNT_LANDSCAPE] = gallerySpanCountLandscape
        preferenceMap[SETTING_GALLERY_SPAN_COUNT_PORTRAIT] = gallerySpanCountPortrait
        preferenceMap[SETTING_VISIBLE_UNLINKED_PHOTOS] = visibleUnlinkedPhotos
        preferenceMap[SETTING_DISABLE_FUTURE_DIARY] = disableFutureDiary
        preferenceMap[SETTING_ENABLE_SHAKE_DETECTOR] = enableShakeDetector
        preferenceMap[SETTING_ENABLE_KEYBOARD_INCOGNITO] = enableKeyboardIncognito
        preferenceMap[SETTING_APP_LANGUAGE] = settingAppLanguage
        preferenceMap[SETTING_CUSTOM_SYMBOL_PATHS] = customSymbolPaths
    }

    return preferenceMap
}

fun Context.jsonStringToPreference(jsonString: String) {
    val gson = Gson()
    val type = object : TypeToken<HashMap<String, Any>>() {}.type
    val map: HashMap<String, Any> = gson.fromJson(jsonString, type)
    config.run {
        map[PRIMARY_COLOR]?.let { primaryColor = (it as? Double)?.toInt() ?: primaryColor }
        map[BACKGROUND_COLOR]?.let { backgroundColor = (it as? Double)?.toInt() ?: backgroundColor }
        map[SETTING_CARD_VIEW_BACKGROUND_COLOR]?.let { screenBackgroundColor = (it as? Double)?.toInt() ?: screenBackgroundColor }
        map[TEXT_COLOR]?.let { textColor = (it as? Double)?.toInt() ?: textColor }
        map[USE_24_HOUR_FORMAT]?.let { use24HourFormat = it as? Boolean ?: use24HourFormat }
        map[SUNDAY_FIRST]?.let { isSundayFirst = it as? Boolean ?: isSundayFirst }

//        map[AAF_PIN_LOCK_PAUSE_MILLIS]?.let { aafPinLockPauseMillis = (it as? Double)?.toLong() ?: aafPinLockPauseMillis }
//        map[AAF_THEME_CHANGE]?.let { isThemeChanged = it as? Boolean ?: isThemeChanged }

        map[DEV_STOCK_CHART_OPTIONS_FROM_MILLIS]?.let { devStockChartOptionFromMillis = (it as? Double)?.toLong() ?: devStockChartOptionFromMillis }
        map[DEV_STOCK_ENABLE_EVALUATE_PRICE]?.let { devStockEnableEvaluatePrice = it as? Boolean ?: devStockEnableEvaluatePrice }
        map[DEV_STOCK_ENABLE_PRINCIPAL_HIGHLIGHT]?.let { devStockEnablePrincipalHighlight = it as? Boolean ?: devStockEnablePrincipalHighlight }

        map[SETTING_FONT_NAME]?.let { settingFontName = it as? String ?: settingFontName }
//        map[APP_LOCK_SAVED_PASSWORD]?.let { aafPinLockSavedPassword = it as? String ?: aafPinLockSavedPassword }
        map[PREVIOUS_ACTIVITY]?.let { previousActivity = (it as? Double)?.toInt() ?: previousActivity }
        map[SETTING_FONT_SIZE]?.let { settingFontSize = (it as? Double)?.toFloat() ?: settingFontSize }
        map[DIARY_SEARCH_QUERY_CASE_SENSITIVE]?.let { diarySearchQueryCaseSensitive = it as? Boolean ?: diarySearchQueryCaseSensitive }
//        map[APP_LOCK_ENABLE]?.let { aafPinLockEnable = it as? Boolean ?: aafPinLockEnable }
        map[LINE_SPACING_SCALE_FACTOR]?.let { lineSpacingScaleFactor = (it as? Double)?.toFloat() ?: lineSpacingScaleFactor }
        map[SETTING_THUMBNAIL_SIZE]?.let { settingThumbnailSize = (it as? Double)?.toFloat() ?: settingThumbnailSize }
        map[SETTING_CALENDAR_FONT_SCALE]?.let { settingCalendarFontScale = (it as? Double)?.toFloat() ?: settingCalendarFontScale }
        map[SETTING_BOLD_STYLE]?.let { boldStyleEnable = it as? Boolean ?: boldStyleEnable }
//        map[SETTING_FINGERPRINT_LOCK]?.let { fingerprintLockEnable = it as? Boolean ?: fingerprintLockEnable }
//        map[FINGERPRINT_ENCRYPT_DATA]?.let { fingerprintEncryptData = it as? String ?: fingerprintEncryptData }
//        map[FINGERPRINT_ENCRYPT_DATA_IV]?.let { fingerprintEncryptDataIV = it as? String ?: fingerprintEncryptDataIV }
//        map[FINGERPRINT_AUTHENTICATION_FAIL_COUNT]?.let { fingerprintAuthenticationFailCount = (it as? Double)?.toInt() ?: fingerprintAuthenticationFailCount }
        map[ENABLE_CARD_VIEW_POLICY]?.let { enableCardViewPolicy = it as? Boolean ?: enableCardViewPolicy }
        map[SETTING_CONTENTS_SUMMARY]?.let { enableContentsSummary = it as? Boolean ?: enableContentsSummary }
        map[SETTING_CALENDAR_START_DAY]?.let { calendarStartDay = (it as? Double)?.toInt() ?: calendarStartDay }
        map[SETTING_COUNT_CHARACTERS]?.let { enableCountCharacters = it as? Boolean ?: enableCountCharacters }
        map[SETTING_ENABLE_ELAPSED_DAYS]?.let { enableElapsedDays = it as? Boolean ?: enableElapsedDays }
        map[DIARY_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE]?.let { diaryBackupGoogle = (it as? Double)?.toLong() ?: diaryBackupGoogle }
        map[PHOTO_LAST_BACKUP_TIMESTAMP_GOOGLE_DRIVE]?.let { photoBackupGoogle = (it as? Double)?.toLong() ?: photoBackupGoogle }
        map[DIARY_LAST_BACKUP_TIMESTAMP_LOCAL]?.let { diaryBackupLocal = (it as? Double)?.toLong() ?: diaryBackupLocal }
        map[SETTING_CALENDAR_SORTING]?.let { calendarSorting = (it as? Double)?.toInt() ?: calendarSorting }
        map[UPDATE_SHARED_PREFERENCE]?.let { updatePreference = it as? Boolean ?: updatePreference }
        map[HOLD_POSITION_ENTER_EDIT_SCREEN]?.let { holdPositionEnterEditScreen = it as? Boolean ?: holdPositionEnterEditScreen }
        map[SETTING_SUMMARY_MAX_LINES]?.let { summaryMaxLines = (it as? Double)?.toInt() ?: summaryMaxLines }
        map[SETTING_ENABLE_DEBUG_CONSOLE]?.let { enableDebugMode = it as? Boolean ?: enableDebugMode }
        map[SETTING_LOCATION_INFO]?.let { enableLocationInfo = it as? Boolean ?: enableLocationInfo }
        map[SETTING_TASK_SYMBOL_TOP_ORDER]?.let { enableTaskSymbolTopOrder = it as? Boolean ?: enableTaskSymbolTopOrder }
        map[SETTING_SELECTED_SYMBOLS]?.let { selectedSymbols = it as? String ?: selectedSymbols }
        map[APP_EXECUTION_COUNT]?.let { appExecutionCount = (it as? Double)?.toInt() ?: appExecutionCount }
        map[SETTING_ENABLE_REVIEW_FLOW]?.let { enableReviewFlow = it as? Boolean ?: enableReviewFlow }
        map[SETTING_POSTCARD_SPAN_COUNT_LANDSCAPE]?.let { postcardSpanCountLandscape = (it as? Double)?.toInt() ?: postcardSpanCountLandscape }
        map[SETTING_POSTCARD_SPAN_COUNT_PORTRAIT]?.let { postcardSpanCountPortrait = (it as? Double)?.toInt() ?: postcardSpanCountPortrait }
        map[SETTING_DIARY_MAIN_SPAN_COUNT_LANDSCAPE]?.let { diaryMainSpanCountLandscape = (it as? Double)?.toInt() ?: diaryMainSpanCountLandscape }
        map[SETTING_DIARY_MAIN_SPAN_COUNT_PORTRAIT]?.let { diaryMainSpanCountPortrait = (it as? Double)?.toInt() ?: diaryMainSpanCountPortrait }
        map[SETTING_ENABLE_PHOTO_HIGHLIGHT]?.let { enablePhotoHighlight = it as? Boolean ?: enablePhotoHighlight }
        map[SETTING_ENABLE_PHOTO_HIGHLIGHT_DIARY_MAIN]?.let { enablePhotoHighlightInDiaryMain = it as? Boolean ?: enablePhotoHighlightInDiaryMain }
        map[SETTING_ENABLE_PHOTO_HIGHLIGHT_DASHBOARD]?.let { enablePhotoHighlightInDashboard = it as? Boolean ?: enablePhotoHighlightInDashboard }
        map[SETTING_ENABLE_STATUSBAR_DARKEN_COLOR]?.let { enableStatusBarDarkenColor = it as? Boolean ?: enableStatusBarDarkenColor }
        map[SETTING_ENABLE_WELCOME_DASHBOARD_POPUP]?.let { enableWelcomeDashboardPopup = it as? Boolean ?: enableWelcomeDashboardPopup }
        map[SETTING_ENABLE_DDAY_FLEXBOX_LAYOUT]?.let { enableDDayFlexboxLayout = it as? Boolean ?: enableDDayFlexboxLayout }
        map[SETTING_ENABLE_DASHBOARD_CALENDAR]?.let { enableDashboardCalendar = it as? Boolean ?: enableDashboardCalendar }
        map[SETTING_DATETIME_FORMAT]?.let { settingDatetimeFormat = it as? String ?: settingDatetimeFormat }
        map[SETTING_TIME_FORMAT]?.let { settingTimeFormat = it as? String ?: settingTimeFormat }
        map[SETTING_ENABLE_ALL_DAY_DEFAULT]?.let { enableAllDayDefault = it as? Boolean ?: enableAllDayDefault }
        map[SETTING_ENABLE_MARKDOWN]?.let { enableMarkdown = it as? Boolean ?: enableMarkdown }
        map[SETTING_DEBUG_OPTION_TOAST_LOCATION]?.let { enableDebugOptionToastLocation = it as? Boolean ?: enableDebugOptionToastLocation }
        map[SETTING_DEBUG_OPTION_TOAST_ATTACHED_PHOTO]?.let { enableDebugOptionToastAttachedPhoto = it as? Boolean ?: enableDebugOptionToastAttachedPhoto }
        map[SETTING_DEBUG_OPTION_TOAST_NOTIFICATION_INFO]?.let { enableDebugOptionToastNotificationInfo = it as? Boolean ?: enableDebugOptionToastNotificationInfo }
        map[SETTING_DEBUG_OPTION_TOAST_REVIEW_FLOW_INFO]?.let { enableDebugOptionToastReviewFlowInfo = it as? Boolean ?: enableDebugOptionToastReviewFlowInfo }
        map[SETTING_DEBUG_OPTION_TOAST_PHOTO_HIGHLIGHT_UPDATE_TIME]?.let { enableDebugOptionToastPhotoHighlightUpdateTime = it as? Boolean ?: enableDebugOptionToastPhotoHighlightUpdateTime }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_DIARY_SEQUENCE]?.let { enableDebugOptionVisibleDiarySequence = it as? Boolean ?: enableDebugOptionVisibleDiarySequence }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_ALARM_SEQUENCE]?.let { enableDebugOptionVisibleAlarmSequence = it as? Boolean ?: enableDebugOptionVisibleAlarmSequence }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_TREE_STATUS]?.let { enableDebugOptionVisibleTreeStatus = it as? Boolean ?: enableDebugOptionVisibleTreeStatus }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_CHART_WEIGHT]?.let { enableDebugOptionVisibleChartWeight = it as? Boolean ?: enableDebugOptionVisibleChartWeight }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_CHART_STOCK]?.let { enableDebugOptionVisibleChartStock = it as? Boolean ?: enableDebugOptionVisibleChartStock }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_FONT_PREVIEW_EMOJI]?.let { enableDebugOptionVisibleFontPreviewEmoji = it as? Boolean ?: enableDebugOptionVisibleFontPreviewEmoji }
        map[SETTING_DEBUG_OPTION_DISPLAY_VISIBLE_TEMPORARY_DIARY]?.let { enableDebugOptionVisibleTemporaryDiary = it as? Boolean ?: enableDebugOptionVisibleTemporaryDiary }
        map[SETTING_ENABLE_JETPACK_ROOM_DATABASE]?.let { enableJetpackRoomDatabase = it as? Boolean ?: enableJetpackRoomDatabase }
        map[SETTING_GALLERY_SPAN_COUNT_LANDSCAPE]?.let { gallerySpanCountLandscape = (it as? Double)?.toInt() ?: gallerySpanCountLandscape }
        map[SETTING_GALLERY_SPAN_COUNT_PORTRAIT]?.let { gallerySpanCountPortrait = (it as? Double)?.toInt() ?: gallerySpanCountPortrait }
        map[SETTING_VISIBLE_UNLINKED_PHOTOS]?.let { visibleUnlinkedPhotos = it as? Boolean ?: visibleUnlinkedPhotos }
        map[SETTING_DISABLE_FUTURE_DIARY]?.let { disableFutureDiary = it as? Boolean ?: disableFutureDiary }
        map[SETTING_ENABLE_SHAKE_DETECTOR]?.let { enableShakeDetector = it as? Boolean ?: enableShakeDetector }
        map[SETTING_ENABLE_KEYBOARD_INCOGNITO]?.let { enableKeyboardIncognito = it as? Boolean ?: enableKeyboardIncognito }
        map[SETTING_APP_LANGUAGE]?.let { settingAppLanguage = it as? String ?: settingAppLanguage }
        map[SETTING_CUSTOM_SYMBOL_PATHS]?.let {
            val json = gson.toJson(it)
            val customSymbolType = object : TypeToken<List<PhotoUriDomain>>() {}.type
            customSymbolPaths = gson.fromJson(json, customSymbolType)
        }

        updatePreference = true
    }
}

fun Context.shareFile(targetFile: File) {
    shareFile(targetFile, contentResolver.getType(getUriForFile(targetFile)) ?: MIME_TYPE_BINARY)
}

fun Context.shareFile(
    targetFile: File,
    mimeType: String,
) {
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, getUriForFile(targetFile))
        type = mimeType
        startActivity(Intent.createChooser(this, getString(R.string.diary_card_share_info)))
    }
}

fun Context.formatTime(
    showSeconds: Boolean,
    use24HourFormat: Boolean,
    hours: Int,
    minutes: Int,
    seconds: Int,
): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

fun Context.getFormattedTime(
    passedSeconds: Int,
    showSeconds: Boolean,
    makeAmPmSmaller: Boolean,
): SpannableString {
    val use24HourFormat = config.use24HourFormat
    val hours = (passedSeconds / 3600) % 24
    val minutes = (passedSeconds / 60) % 60
    val seconds = passedSeconds % 60

    return if (!use24HourFormat) {
        val formattedTime = formatTo12HourFormat(showSeconds, hours, minutes, seconds)
        val spannableTime = SpannableString(formattedTime)
        val amPmMultiplier = if (makeAmPmSmaller) 0.4f else 1f
        spannableTime.setSpan(RelativeSizeSpan(amPmMultiplier), spannableTime.length - 5, spannableTime.length, 0)
        spannableTime
    } else {
        val formattedTime = formatTime(showSeconds, use24HourFormat, hours, minutes, seconds)
        SpannableString(formattedTime)
    }
}

fun Context.formatTo12HourFormat(
    showSeconds: Boolean,
    hours: Int,
    minutes: Int,
    seconds: Int,
): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn

@ColorInt
@SuppressLint("ResourceAsColor")
fun Context.getColorResCompat(
    @AttrRes id: Int,
): Int {
    val resolvedAttr = TypedValue()
    theme.resolveAttribute(id, resolvedAttr, true)
    val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
    return ContextCompat.getColor(this, colorRes)
}

fun Context.changeBitmapColor(
    drawableResourceId: Int,
    color: Int,
): Bitmap {
    val drawable = AppCompatResources.getDrawable(this, drawableResourceId)
    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
    }
    return bitmap
}

fun Context.createBackupContentText(
    localDeviceFileCount: Int,
    duplicateFileCount: Int,
    successCount: Int,
    failCount: Int,
): StringBuilder =
    StringBuilder()
        .append("<b>\uD83D\uDCF7 Attached Photos</b><br>")
        .append(getString(R.string.notification_msg_device_file_count, "*", localDeviceFileCount, "<br>"))
        .append(getString(R.string.notification_msg_duplicate_file_count, "*", duplicateFileCount, "<br>"))
        .append(getString(R.string.notification_msg_upload_success, "*", successCount, "<br>"))
        .append(getString(R.string.notification_msg_upload_fail, "*", failCount, "<br>"))

fun Context.createRecoveryContentText(
    remoteDriveFileCount: Int,
    duplicateFileCount: Int,
    successCount: Int,
    failCount: Int,
): StringBuilder =
    StringBuilder()
        .append("<b>\uD83D\uDCF7 Attached Photos</b><br>")
        .append(getString(R.string.notification_msg_google_drive_file_count, "*", remoteDriveFileCount, "<br>"))
        .append(getString(R.string.notification_msg_duplicate_file_count, "*", duplicateFileCount, "<br>"))
        .append(getString(R.string.notification_msg_download_success, "*", successCount, "<br>"))
        .append(getString(R.string.notification_msg_download_fail, "*", failCount, "<br>"))

fun Context.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(locationManager)
}

fun Context.hasGPSPermissions() = checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) && isLocationEnabled()

fun Context.getLastKnownLocation(): Location? {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return when (
        checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) &&
            isLocationEnabled()
    ) {
        true -> {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.elapsedRealtimeNanos - networkLocation.elapsedRealtimeNanos > 0) {
                        if (config.enableDebugOptionToastLocation) toast("GPS Location > Network Location")
                        gpsLocation
                    } else {
                        if (config.enableDebugOptionToastLocation) toast("Network Location > GPS Location")
                        networkLocation
                    }
                }

                gpsLocation != null -> {
                    if (config.enableDebugOptionToastLocation) toast("GPS Location")
                    gpsLocation
                }

                networkLocation != null -> {
                    if (config.enableDebugOptionToastLocation) toast("Network Location")
                    networkLocation
                }

                else -> {
                    null
                }
            }
        }

        false -> {
            null
        }
    }
}

fun Context.getFromLocation(
    latitude: Double,
    longitude: Double,
    maxResults: Int,
): List<Address>? {
//    val lat = java.lang.Double.parseDouble(String.format("%.6f", latitude))
//    val lon = java.lang.Double.parseDouble(String.format("%.7f", longitude))
    val addressList = arrayListOf<Address>()
    try {
        addressList.addAll(Geocoder(this, Locale.getDefault()).getFromLocation(latitude, longitude, maxResults)!!)
    } catch (e: Exception) {
        toast(e.message ?: "Error")
    }
    return addressList
}

fun Context.fullAddress(address: Address): String {
    val sb = StringBuilder()
    when (address.getAddressLine(0) != null) {
        true -> {
            sb.append(address.getAddressLine(0))
        }

        false -> {
            if (address.countryName != null) sb.append(address.countryName).append(" ")
            if (address.adminArea != null) sb.append(address.adminArea).append(" ")
            if (address.locality != null) sb.append(address.locality).append(" ")
            if (address.subLocality != null) sb.append(address.subLocality).append(" ")
            if (address.thoroughfare != null) sb.append(address.thoroughfare).append(" ")
            if (address.featureName != null) sb.append(address.featureName).append(" ")
        }
    }
    return sb.toString()
}

fun Context.isConnectedOrConnecting(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun Context.getLabelBackground(): GradientDrawable {
    val strokeWidth = dpToPixel(1F)
    val strokeColor: Int = config.textColor
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
//        cornerRadius = dpToPixel(5F).toFloat()
        setStroke(strokeWidth, strokeColor)
    }
}

val themeItems = listOf(Launcher.EASY_DIARY, Launcher.DARK, Launcher.GREEN, Launcher.DEBUG)

fun Context.toggleLauncher(launcher: Launcher) {
    themeItems.forEach {
        checkAppIconColor(it.themeName, it == launcher)
    }
}

fun Context.checkAppIconColor(
    colorName: String,
    enable: Boolean = false,
) {
    val appId = BuildConfig.APPLICATION_ID
    toggleAppIconColor(appId, -1, -1, enable, colorName)
//    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
//        getAppIconColors().forEachIndexed { index, color ->
//            toggleAppIconColor(appId, index, color, false)
//        }
//
//        getAppIconColors().forEachIndexed { index, color ->
//            if (baseConfig.appIconColor == color) {
//                toggleAppIconColor(appId, index, color, true)
//            }
//        }
//    }
}

fun Context.toggleAppIconColor(
    appId: String,
    colorIndex: Int,
    color: Int,
    enable: Boolean,
    colorName: String,
) {
//    val className = "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val className = "${appId.removeSuffix(".debug")}.activities.DiaryMainActivity.$colorName"
    val state = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(ComponentName(appId, className), state, PackageManager.DONT_KILL_APP)
//        if (enable) {
//            baseConfig.lastIconColor = color
//        }
    } catch (e: Exception) {
    }
}

fun Context.dpToPixelFloatValue(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

fun Context.spToPixelFloatValue(sp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

fun Context.dpToPixel(
    dp: Float,
    policy: Calculation = Calculation.CEIL,
): Int {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    return when (policy) {
        Calculation.CEIL -> ceil(px).toInt()
        Calculation.ROUND -> px.roundToInt()
        Calculation.FLOOR -> px.toInt()
    }
}

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PackageManager.PERMISSION_GRANTED

fun Context.getPermissionString(id: Int) =
    when (id) {
        PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
        PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        PERMISSION_CAMERA -> Manifest.permission.CAMERA
        PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
        PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
        PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
        PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
        PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
        PERMISSION_ACCESS_FINE_LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        PERMISSION_ACCESS_COARSE_LOCATION -> Manifest.permission.ACCESS_COARSE_LOCATION
        else -> ""
    }

fun Context.dp(px: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, resources.displayMetrics).toInt()

fun Context.applyMarkDownPolicy(
    contentsView: TextView,
    contents: String,
    isTimeline: Boolean = false,
    lineBreakStrings: ArrayList<String> = arrayListOf(),
    isRecyclerItem: Boolean = false,
    // Turns the URLs and phone numbers of a plain text diary into links. The reading
    // screen passes true; a card in a list keeps tapping through to the reading screen.
    applyTextLinks: Boolean = false,
    // Includes phone numbers in the markdown linkify mask. Screens whose result is captured
    // as an image (postcard) pass false so that link styling does not end up in the image.
    linkifyPhoneNumbers: Boolean = true,
) {
    when (config.enableMarkdown) {
        true -> {
            val transformLineBreak = contents.replace("\n", "  \n")
            val mergedContents =
                if (lineBreakStrings.size > 1 &&
                    lineBreakStrings[1].isNotBlank()
                ) {
                    "${lineBreakStrings[1]}  \n$transformLineBreak"
                } else {
                    transformLineBreak
                }
            val timelineTitle =
                when {
                    isTimeline && config.boldStyleEnable -> "**${lineBreakStrings[0]}**"
                    isTimeline -> lineBreakStrings[0]
                    else -> ""
                }
            val markdownContents = if (isTimeline) "$timelineTitle  \n$mergedContents" else mergedContents
            val codeBlockTheme =
                object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder
                            .headingTextSizeMultipliers(floatArrayOf(1.3F, 1.2F, 1.1F, 1.0F, .83F, .67F))
                            .headingBreakHeight(0)
                            .codeBlockTextSize(config.settingFontSize.times(0.8).toInt())
                            .codeBlockBackgroundColor(config.backgroundColor.darkenColor())
                            .codeBlockTextColor(config.textColor)
                            .codeBackgroundColor(0x9FFFCC80.toInt())
                            .codeTypeface(FontUtils.getCommonTypeface(this@applyMarkDownPolicy)!!)
                            .codeTextColor(Color.BLACK)
                            .codeTextSize(config.settingFontSize.times(0.8).toInt())
                    }

//                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
//                    // apply to `code` only
//                    builder.appendFactory(Code::class.java) { _, _ ->
//                        RoundedBackgroundSpan(
//                            bgColor = config.primaryColor,
//                            textColor = Color.WHITE,                     // 기본 텍스트
//                            cornerRadius = 12f,
//                            keyword = "3"
//                        )
//                    }
//                }
                }
            val tablePlugin =
                TablePlugin.create { builder: TableTheme.Builder ->
                    val dip: Dip = Dip.create(this)
                    builder
                        .tableBorderWidth(dip.toPx(1))
                        .tableBorderColor(Color.BLACK)
                        .tableCellPadding(dip.toPx(6))
                        .tableHeaderRowBackgroundColor(
                            io.noties.markwon.utils.ColorUtils.applyAlpha(
                                config.primaryColor,
                                50,
                            ),
                        ).tableEvenRowBackgroundColor(config.backgroundColor)
                        .tableOddRowBackgroundColor(config.backgroundColor)
                }
            val strikeoutPlugin = StrikethroughPlugin.create()

            when (isRecyclerItem) {
                true -> {
                    Markwon
                        .builder(this)
                        .usePlugin(MovementMethodPlugin.none())
                        .usePlugin(codeBlockTheme)
                        .usePlugin(ImagesPlugin.create())
                        .usePlugin(HtmlPlugin.create())
                        .usePlugin(tablePlugin)
                        .usePlugin(strikeoutPlugin)
                        .build()
                        .apply {
                            setMarkdown(contentsView, markdownContents)
                        }
                }

                false -> {
                    val linkifyMask =
                        when (linkifyPhoneNumbers) {
                            true -> Linkify.WEB_URLS or Linkify.PHONE_NUMBERS
                            false -> Linkify.WEB_URLS
                        }
                    Markwon
                        .builder(this)
                        .usePlugin(codeBlockTheme)
                        .usePlugin(ImagesPlugin.create())
                        .usePlugin(HtmlPlugin.create())
                        .usePlugin(tablePlugin)
                        .usePlugin(strikeoutPlugin)
                        .usePlugin(MovementMethodPlugin.link())
                        .usePlugin(LinkifyPlugin.create(linkifyMask))
                        .usePlugin(
                            object : AbstractMarkwonPlugin() {
                                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                                    super.configureConfiguration(builder)
                                    builder.linkResolver { view, link ->
                                        when (val target = ClickableLinkUtils.resolveTarget(link)) {
                                            is DiaryLinkTarget.Diary ->
                                                this@applyMarkDownPolicy.startDiaryReadingActivity(
                                                    target.sequence,
                                                )

                                            is DiaryLinkTarget.Phone ->
                                                this@applyMarkDownPolicy.dialPhoneNumber(
                                                    target.number,
                                                )

                                            is DiaryLinkTarget.Web ->
                                                this@applyMarkDownPolicy.openWebPage(
                                                    target.url,
                                                )

                                            is DiaryLinkTarget.External ->
                                                this@applyMarkDownPolicy.openWebPage(
                                                    target.address,
                                                )

                                            null -> Unit
                                        }
                                    }
                                }
                            },
                        ).build()
                        .apply {
                            setMarkdown(contentsView, markdownContents)
                            (contentsView.text as? Spannable)?.run {
                                removeDateOrTimeLinks()
                                // A postcard is captured as an image and keeps its text plain,
                                // so a diary link written in it is not linked there.
                                if (linkifyPhoneNumbers) linkDiaryDeepLinks()
                            }
                        }
                }
            }
        }

        false -> {
            contentsView.text =
                when (isTimeline) {
                    true -> {
                        val mergedContents =
                            if (lineBreakStrings.size > 1 &&
                                lineBreakStrings[1].isNotBlank()
                            ) {
                                "${lineBreakStrings[1]}\n$contents"
                            } else {
                                contents
                            }
                        applyBoldToDate(lineBreakStrings[0], mergedContents)
                    }

                    false -> {
                        contents
                    }
                }
            if (applyTextLinks) contentsView.applyClickableLinks()
        }
    }
}

/**
 * Turns the URLs and phone numbers of a plain text diary into links that open a browser or the
 * dialer (#167). Linkify links are replaced by [DiaryLinkSpan] so that a phone number reaches
 * the dialer and a web page opens the way the markdown links do, and so that a date which only
 * looks like a phone number (2021-10-23) can be dropped. A diary without a real link keeps its
 * text untouched and stays fully selectable.
 *
 * A diary deep link (easydiary://diary/123) is not a web address to Linkify, which reads only its
 * sequence as a phone number, so those links are found first and linked whole (#268).
 */
fun TextView.applyClickableLinks() {
    val diaryLinks = DiaryLinkUtils.findDiaryLinkRanges(text)
    val linkedContents = SpannableString(text)
    // A text that holds nothing but a diary link leaves nothing for Linkify to link, so its
    // result cannot decide whether there is a link to click.
    Linkify.addLinks(linkedContents, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS)
    if (linkedContents.replaceUrlSpansWithDiaryLinks(diaryLinks) == 0) return

    text = linkedContents
    // A click on a link needs the movement method Linkify would otherwise set by itself.
    movementMethod = LinkMovementMethod.getInstance()
}

/**
 * Linkify links are URLSpans, whose click opens whatever the system makes of the address. They
 * are replaced by [DiaryLinkSpan] so that a phone number reaches the dialer and a web page
 * opens the way the links of a markdown diary do.
 *
 * @return the number of links that were kept
 */
private fun Spannable.replaceUrlSpansWithDiaryLinks(diaryLinks: List<DiaryLinkRange>): Int {
    var linkCount = 0
    getSpans(0, length, URLSpan::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        val spanFlags = getSpanFlags(span)
        removeSpan(span)
        // The sequence digits of a diary link are read as a phone number, and the diary link
        // below covers them.
        if (ClickableLinkUtils.overlapsDiaryLink(start, end, diaryLinks)) return@forEach
        if (ClickableLinkUtils.isDateOrTimeLike(subSequence(start, end))) return@forEach
        setSpan(DiaryLinkSpan(span.url), start, end, spanFlags)
        linkCount++
    }
    diaryLinks.forEach { diaryLink ->
        setSpan(
            DiaryLinkSpan(diaryLink.link),
            diaryLink.start,
            diaryLink.end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        linkCount++
    }
    return linkCount
}

/**
 * Links the diary deep links of a markdown diary (#268).
 *
 * Markwon leaves easydiary://diary/123 as plain text because it is not a web address, and its
 * linking rules then read the sequence as a phone number, which turns the last digits into a
 * number to call. Every diary link that no markdown link already covers whole gets a
 * [DiaryLinkSpan], and the phone link of its digits is dropped.
 *
 * @return the number of diary links that were added
 */
private fun Spannable.linkDiaryDeepLinks(): Int {
    val diaryLinks = DiaryLinkUtils.findDiaryLinkRanges(this)
    if (diaryLinks.isEmpty()) return 0

    getSpans(0, length, ClickableSpan::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        if (!ClickableLinkUtils.overlapsDiaryLink(start, end, diaryLinks)) return@forEach
        // A link written around the address, such as the label of [easydiary://diary/123](...),
        // is left as it is; a link that covers only a part of the address is what the digits
        // were mistaken for.
        val coversWholeLink = diaryLinks.any { link -> start <= link.start && link.end <= end }
        if (!coversWholeLink) removeSpan(span)
    }

    return diaryLinks.count { diaryLink ->
        val alreadyLinked =
            getSpans(diaryLink.start, diaryLink.end, ClickableSpan::class.java).any { span ->
                getSpanStart(span) <= diaryLink.start && diaryLink.end <= getSpanEnd(span)
            }
        if (!alreadyLinked) {
            setSpan(
                DiaryLinkSpan(diaryLink.link),
                diaryLink.start,
                diaryLink.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
        !alreadyLinked
    }
}

/**
 * Linkify counts the digits of a match, so 2021-10-23 or 05:14 is detected as a phone number.
 * Such a span is dropped so that a date of the diary does not turn into a number to call.
 */
private fun Spannable.removeDateOrTimeLinks() {
    getSpans(0, length, URLSpan::class.java)
        .filter { span -> ClickableLinkUtils.isDateOrTimeLike(subSequence(getSpanStart(span), getSpanEnd(span))) }
        .forEach { span -> removeSpan(span) }
}

fun Context.applyBoldToDate(
    dateString: String,
    summary: String,
): SpannableString {
    val spannableString = SpannableString("$dateString\n$summary")
    if (config.boldStyleEnable) spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, dateString.length, 0)
    return spannableString
}

fun Context.parsedMarkdownString(markdownString: String): Spanned = Markwon.builder(this).build().toMarkdown(markdownString)

fun Context.storedDatetimeFormat() = DateTimeFormat.valueOf(config.settingDatetimeFormat)

fun Context.storedTimeFormat() = TimeFormatMode.fromName(config.settingTimeFormat)

fun Context.isColorLight(color: Int): Boolean {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)

    val brightness = (0.299 * r + 0.587 * g + 0.114 * b)
    return brightness > 128
}

tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

enum class UriFileType {
    ROOM,
    UNKNOWN,
}

fun classifyUriByExtension(uriString: String): UriFileType {
    // URI 또는 파일 경로에서 마지막 파일명 및 확장자 추출
    val fileName = uriString.substringAfterLast('/')

    // 확장자 추출 (파일명 내 첫 번째 점 또는 마지막 점 기준)
    val extension =
        if (fileName.contains('.')) {
            fileName.substringAfterLast('.')
        } else {
            ""
        }

    return when {
        // 확장자가 zip이고 파일명에 db_ 문자열이 포함된 경우 (ROOM)
        extension.equals("zip", ignoreCase = true) && fileName.contains("db_", ignoreCase = true) -> UriFileType.ROOM

        else -> UriFileType.UNKNOWN
    }
}

suspend fun Context.exportRoomData(updateLocalBackupTime: Boolean = false): String {
    val destFilePath = BACKUP_DB_DIRECTORY + RoomConstants.DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss")
    val destFile = File(EasyDiaryUtils.getApplicationDataDirectory(this) + destFilePath + ".zip")
    val roomDataMap = generateRoomDataMap()
    withContext(Dispatchers.IO) {
        val jsonString =
            GsonBuilder().setPrettyPrinting().create().toJson(roomDataMap)
        FileOutputStream(destFile).use { outputStream ->
            java.util.zip.ZipOutputStream(outputStream).use { zos ->
                val entryName = RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME
                val entry = java.util.zip.ZipEntry(entryName)
                zos.putNextEntry(entry)
                zos.write(jsonString.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
    }

    if (updateLocalBackupTime) config.diaryBackupLocal = System.currentTimeMillis()
    return destFile.absolutePath
}

suspend fun Context.exportLegacyRealmFile() {
    val destFilePath = BACKUP_DB_DIRECTORY + "diary.realm_"
    withContext(Dispatchers.IO) {
        try {
            val srcFile = File(filesDir, "diary.realm")
            val destFile = File(EasyDiaryUtils.getApplicationDataDirectory(this@exportLegacyRealmFile) + destFilePath)
            if (srcFile.exists()) {
                destFile.parentFile?.let { parent ->
                    if (!parent.exists()) parent.mkdirs()
                }
                srcFile.copyTo(destFile, overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

suspend fun Context.generateRoomDataMap(): Map<String, Any> =
    mutableMapOf(
        "PREFERENCE" to preferenceToJsonString(),
        "META" to EasyDiaryUtils.getExportMeta(),
        "ACTION_LOG" to actionLogRepository.getAllActionLogs(),
        "ALARM" to alarmRepository.getAllAlarms(),
        "D_DAY" to dDayRepository.getAllDDays(),
        "DIARY" to diaryRepository.getDiariesWithPhotos(),
    )

suspend fun Context.exportRoomDataWithSAF(uri: Uri?) {
    uri?.let {
        val roomDataMap = generateRoomDataMap()
        withContext(Dispatchers.IO) {
            val jsonString =
                GsonBuilder().setPrettyPrinting().create().toJson(roomDataMap)
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                java.util.zip.ZipOutputStream(outputStream).use { zos ->
                    val entryName = RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME
                    val entry = java.util.zip.ZipEntry(entryName)
                    zos.putNextEntry(entry)
                    zos.write(jsonString.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
    }
}

suspend fun Context.importRoomData(
    jsonString: String,
    isMergeData: Boolean,
) {
    // 1. Convert to Map using Gson
    // Use TypeToken to preserve data types.
    val type =
        object :
            com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
    val dataMap: Map<String, Any> =
        Gson().fromJson(jsonString, type)

    // 2. Convert each data to domain model list and save to DB
    // Note: When Gson converts numbers to Double or uses a generic map,
    // type casting errors may occur, so a process of converting to the exact model class is required.
    val gson = Gson()

    // Save ActionLog
    dataMap["ACTION_LOG"]?.let { it ->
        val json = gson.toJson(it)
        val list: List<ActionLog> =
            gson.fromJson(
                json,
                object :
                    com.google.gson.reflect.TypeToken<List<ActionLog>>() {}.type,
            )
        actionLogRepository.deleteAllActionLogs(true)
        actionLogRepository.insertAllActionLogs(list)
    }

    // Save Alarm
    dataMap["ALARM"]?.let {
        val json = gson.toJson(it)
        val list: List<Alarm> =
            gson.fromJson(
                json,
                object :
                    com.google.gson.reflect.TypeToken<List<Alarm>>() {}.type,
            )
        alarmRepository.deleteAllAlarms()
        alarmRepository.insertAllAlarms(list)
    }

    // Save D-Day
    dataMap["D_DAY"]?.let {
        val json = gson.toJson(it)
        val list: List<DDay> =
            gson.fromJson(
                json,
                object :
                    com.google.gson.reflect.TypeToken<List<DDay>>() {}.type,
            )
        dDayRepository.deleteAllDDays()
        dDayRepository.insertAllDDays(list)
    }

    // Save Diary (including photos)
    dataMap["DIARY"]?.let {
        val json = gson.toJson(it)
        val list: List<Diary> =
            gson.fromJson(
                json,
                object :
                    com.google.gson.reflect.TypeToken<List<Diary>>() {}.type,
            )
        diaryRepository.deleteAllDiaries()
        diaryRepository.insertAllDiaries(list)
    }

    dataMap["PREFERENCE"]?.let {
        val json = gson.toJson(it)
        jsonStringToPreference(json)
    }
}

suspend fun Context.importRoomDataWithSAF(
    uri: Uri?,
    isMergeData: Boolean,
    successCallback: (String) -> Unit,
    failCallback: (String) -> Unit,
) {
    uri?.let {
        try {
            withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.util.zip.ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (entry.name == RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME) {
                                val jsonString = zis.bufferedReader().readText()
                                importRoomData(jsonString, isMergeData)
                                break // Found the file, so terminate the loop
                            }
                            entry = zis.nextEntry
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                successCallback("Import completed.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                failCallback("Error: ${e.message}")
            }
        }
    }
}

suspend fun Context.importRoomData(
    file: File,
    deleteImportedFile: Boolean,
    isMergeData: Boolean,
    successCallback: (String) -> Unit,
    failCallback: (String) -> Unit,
) {
    try {
        withContext(Dispatchers.IO) {
            FileInputStream(file).use { inputStream ->
                java.util.zip.ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name == RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME) {
                            val jsonString = zis.bufferedReader().readText()
                            importRoomData(jsonString, isMergeData)
                            break // Found the file, so terminate the loop
                        }
                        entry = zis.nextEntry
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            successCallback("Import completed.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            failCallback("Error: ${e.message}")
        }
    }

    if (deleteImportedFile) deleteTemporaryRoomFile(file.absolutePath)
}

suspend fun Context.deleteTemporaryRoomFile(roomPath: String) {
    actionLogRepository.insertActionLog(
        ActionLogDomain(
            className = this::class.java.name,
            signature = "deleteTemporaryRoomFile",
            key = ActionLogKey.INFO,
            value = roomPath,
        ),
    )
    File(roomPath).delete()
}
