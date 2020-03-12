package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DAY_MINUTES
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.utils.DateUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.receivers.AlarmReceiver
import java.util.*
import kotlin.math.pow


class DevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    private lateinit var mAlarm: Alarm
    var mRealmInstance: Realm? = null

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }

        initProperties()
        initDevUI()
        bindEvent()

//        toast("${EasyDiaryDbHelper.countAlarmAll()}", Toast.LENGTH_LONG)
    }

    override fun onResume() {
        super.onResume()
        mRealmInstance?.beginTransaction()
    }

    override fun onPause() {
        super.onPause()
        mRealmInstance?.commitTransaction()
        mRealmInstance?.close()
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initProperties() {
        config.use24HourFormat = false
        val calendar = Calendar.getInstance(Locale.getDefault())
        var minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60
        minutes += calendar.get(Calendar.MINUTE)

        val tempAlarm = Alarm(0)
        if (EasyDiaryDbHelper.countAlarmAll() == 0L) {
            EasyDiaryDbHelper.insertAlarm(tempAlarm)
        }
        mAlarm = EasyDiaryDbHelper.readAlarmBy(1)!!
        mRealmInstance = EasyDiaryDbHelper.getTemporaryInstance()
    }

    private fun initDevUI() {
        updateAlarmTime()

        val dayLetters = resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
        val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
        dayIndexes.forEach {
            val pow = 2.0.pow(it.toDouble()).toInt()
            val day = layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
            day.text = dayLetters[it]

            val isDayChecked = mAlarm.days and pow != 0
            day.background = getProperDayDrawable(isDayChecked)

            day.setTextColor(if (isDayChecked) config.backgroundColor else config.textColor)
            day.setOnClickListener {
                val selectDay = mAlarm.days and pow == 0
                mAlarm.days = if (selectDay) {
                    mAlarm.days.addBit(pow)
                } else {
                    mAlarm.days.removeBit(pow)
                }
                day.background = getProperDayDrawable(selectDay)
                day.setTextColor(if (selectDay) config.backgroundColor else config.textColor)
            }

            edit_alarm_days_holder.addView(day)
        }

        alarm_switch.isChecked = mAlarm.isEnabled

        val resourceId = resources.getIdentifier("ic_pizza", "drawable", packageName)
        if (resourceId > 0) {
            devConsoleSymbol.setImageDrawable(ContextCompat.getDrawable(this, resourceId))
            devConsoleSymbol.visibility = View.VISIBLE
        }
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = ContextCompat.getDrawable(this, drawableId)
        drawable!!.applyColorFilter(config.textColor)
        return drawable
    }

    private fun bindEvent() {
        edit_alarm_time.setOnClickListener {
            TimePickerDialog(this, timeSetListener, mAlarm.timeInMinutes / 60, mAlarm.timeInMinutes % 60, DateFormat.is24HourFormat(this)).show()
        }
        alarm_switch.setOnCheckedChangeListener { _, isChecked ->
            mAlarm.isEnabled = isChecked
            if (isChecked) {
                scheduleNextAlarm(mAlarm, true)
            } else {
                cancelAlarmClock(mAlarm)
            }
        }

        openAlarmManager.setOnClickListener {
            TransitionHelper.startActivityWithTransition(this, Intent(this, DiaryReminderActivity::class.java).apply {
                putExtra(ALARM_ID, mAlarm.id)
            })
        }

        nextAlarmInfo.setOnClickListener {
            val nextAlarm = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val triggerTimeMillis = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: 0
                when (triggerTimeMillis > 0) {
                    true -> DateUtils.getFullPatternDateWithTime(triggerTimeMillis)
                    false -> "Alarm info is not exist."
                }
            } else {
                Settings.System.getString(contentResolver,Settings.System.NEXT_ALARM_FORMATTED)
            }

            toast(nextAlarm, Toast.LENGTH_LONG)
        }
    }

    private val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        mAlarm.timeInMinutes = hourOfDay * 60 + minute
        updateAlarmTime()
    }

    private fun updateAlarmTime() {
        edit_alarm_time.text = getFormattedTime(mAlarm.timeInMinutes * 60, false, true)
    }

    companion object {
        const val ALARM_ID = "alarm_id"
    }
}


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/
//data class Alarm(var id: Int, var timeInMinutes: Int, var days: Int, var isEnabled: Boolean, var vibrate: Boolean, var soundTitle: String, var soundUri: String, var label: String)

fun formatTime(showSeconds: Boolean, use24HourFormat: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val hoursFormat = if (use24HourFormat) "%02d" else "%01d"
    var format = "$hoursFormat:%02d"

    return if (showSeconds) {
        format += ":%02d"
        String.format(format, hours, minutes, seconds)
    } else {
        String.format(format, hours, minutes)
    }
}

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

fun Context.cancelAlarmClock(alarm: Alarm) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getAlarmIntent(alarm))
}

// step01
fun Context.scheduleNextAlarm(alarm: Alarm, showToast: Boolean) {
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

// step02
fun Context.setupAlarmClock(alarm: Alarm, triggerInSeconds: Int) {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val targetMS = System.currentTimeMillis() + triggerInSeconds * 1000
    AlarmManagerCompat.setAlarmClock(alarmManager, targetMS, getOpenAlarmTabIntent(), getAlarmIntent(alarm))
}

fun Context.showRemainingTimeMessage(totalMinutes: Int) {
    val fullString = String.format(getString(R.string.alarm_goes_off_in), formatMinutesToTimeString(totalMinutes))
    toast(fullString, Toast.LENGTH_LONG)
}

fun Context.showAlarmNotification(alarm: Alarm) {
    val pendingIntent = getOpenAlarmTabIntent()
    val notification = getAlarmNotification(pendingIntent, alarm)
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(alarm.id, notification)
    scheduleNextAlarm(alarm, false)
}

fun Context.rescheduleEnabledAlarms() {
//    toast("Reschedule enabled alarms that Easy Diary.", Toast.LENGTH_LONG)
    EasyDiaryDbHelper.readAlarmAll().forEach {
        if (it.isEnabled) scheduleNextAlarm(it, false)
    }
}

@SuppressLint("NewApi")
fun Context.getAlarmNotification(pendingIntent: PendingIntent, alarm: Alarm): Notification {
    if (isOreoPlus()) {
        // Create the NotificationChannel
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", "${NOTIFICATION_CHANNEL_NAME}_dev", importance)
        mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    val builder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev")
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_round))
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle("title")
            .setContentText("content")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notification = builder.build()
    notification.flags = notification.flags or Notification.FLAG_INSISTENT
    return notification
}

fun Context.getFormattedTime(passedSeconds: Int, showSeconds: Boolean, makeAmPmSmaller: Boolean): SpannableString {
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

fun Context.formatTo12HourFormat(showSeconds: Boolean, hours: Int, minutes: Int, seconds: Int): String {
    val appendable = getString(if (hours >= 12) R.string.p_m else R.string.a_m)
    val newHours = if (hours == 0 || hours == 12) 12 else hours % 12
    return "${formatTime(showSeconds, false, newHours, minutes, seconds)} $appendable"
}

fun Activity.showOverLockScreen() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
}
