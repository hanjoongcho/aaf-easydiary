package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_NAME
import me.blog.korn123.easydiary.receivers.AlarmReceiver

class DevActivity : EasyDiaryActivity() {
    var alarmSequence = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }

        test01.setOnClickListener {
            makeSnackBar("test01...")

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val targetMS = System.currentTimeMillis() + (1000 * 5)
            val alarm = Alarm(alarmSequence++, 0, 0, isEnabled = false, vibrate = false, soundTitle = "", soundUri = "", label = "")
            AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(), getAlarmIntent(alarm))
        }
    }

    companion object {
        const val ALARM_ID = "alarm_id"
    }
}

data class Alarm(var id: Int, var timeInMinutes: Int, var days: Int, var isEnabled: Boolean, var vibrate: Boolean, var soundTitle: String,
                 var soundUri: String, var label: String)

fun Context.isScreenOn() = (getSystemService(Context.POWER_SERVICE) as PowerManager).isScreenOn

fun Context.getOpenAlarmTabIntent(): PendingIntent {
    val diaryMainIntent = Intent(this, DiaryMainActivity::class.java)
    return PendingIntent.getActivity(this, 1000, diaryMainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.getAlarmIntent(alarm: Alarm): PendingIntent {
    val intent = Intent(this, AlarmReceiver::class.java)
    intent.putExtra(DevActivity.ALARM_ID, alarm.id)
    return PendingIntent.getBroadcast(this, alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
}

fun Context.showAlarmNotification(alarm: Alarm) {
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notification)
}

@SuppressLint("NewApi")
fun Context.getAlarmNotification(pendingIntent: PendingIntent, alarm: Alarm): Notification {

    if (isOreoPlus()) {
        // Create the NotificationChannel
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance)
        mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    val resultNotificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
    resultNotificationBuilder
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle("title")
            .setContentText("content")
            .setContentIntent(pendingIntent)
    return resultNotificationBuilder.build()
}

fun Activity.showOverLockScreen() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
}
