package me.blog.korn123.easydiary.helper

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.models.Alarm

open class BaseAlarmWorkExecutor(val context: Context?) {

    fun openNotification(alarm: Alarm) {
        context?.run {
            val pendingIntent = getOpenAlarmTabIntent(alarm)
            val notification = getAlarmNotification(pendingIntent, alarm)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(alarm.id, notification)

            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (isScreenOn()) {
                scheduleNextAlarm(alarm, true)
            } else {
                scheduleNextAlarm(alarm, false)
                powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "myApp:notificationLock").apply {
                    acquire(3000)
                }
            }
        }
    }

    open fun executeWork(alarm: Alarm) {
        context?.run {
            when (alarm.workMode) {
                Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> {
                    exportRealmFile()
                    openNotification(alarm)
                }
            }
        }
    }
}