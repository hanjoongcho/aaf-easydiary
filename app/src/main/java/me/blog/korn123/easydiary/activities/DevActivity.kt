package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import kotlinx.android.synthetic.main.dialog_alarm.view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.AlarmAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.exportRealmFile
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.receivers.AlarmReceiver
import java.util.*
import kotlin.math.pow


class DevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mAlarmAdapter: AlarmAdapter
    private var mAlarmList: ArrayList<Alarm> = arrayListOf()


    /***************************************************************************************************
     *   override functions
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

        mAlarmAdapter = AlarmAdapter(
            this@DevActivity,
            mAlarmList,
            AdapterView.OnItemClickListener { _, _, position, _ ->
                var rootView: View? = null
                val alarm = EasyDiaryDbHelper.duplicateAlarm(mAlarmList[position])
                var alertDialog: AlertDialog? = null
                val builder = AlertDialog.Builder(this).apply {
                    setCancelable(false)
                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                        // update alarm schedule
                        if (alarm.isEnabled) {
                            scheduleNextAlarm(alarm, true)
                        } else {
                            cancelAlarmClock(alarm)
                        }

                        // save alarm
                        alarm.label = rootView?.alarmDescription?.text.toString()
                        EasyDiaryDbHelper.updateAlarm(alarm)
                        alertDialog?.dismiss()
                        mAlarmAdapter.notifyDataSetChanged()
                    }
                    setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
                }
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                rootView = inflater.inflate(R.layout.dialog_alarm, null).apply {
                    val dayLetters = resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
                    val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
                    if (config.isSundayFirst) {
                        dayIndexes.moveLastItemToFront()
                    }
                    edit_alarm_days_holder.removeAllViews()
                    dayIndexes.forEach {
                        val pow = 2.0.pow(it.toDouble()).toInt()
                        val day = layoutInflater.inflate(R.layout.alarm_day, edit_alarm_days_holder, false) as TextView
                        day.text = dayLetters[it]

                        val isDayChecked = alarm.days and pow != 0
                        day.background = getProperDayDrawable(isDayChecked)

                        day.setTextColor(if (isDayChecked) config.backgroundColor else config.textColor)
                        day.setOnClickListener {
                            EasyDiaryDbHelper.beginTransaction()
                            val selectDay = alarm.days and pow == 0
                            alarm.days = if (selectDay) {
                                alarm.days.addBit(pow)
                            } else {
                                alarm.days.removeBit(pow)
                            }
                            day.background = getProperDayDrawable(selectDay)
                            day.setTextColor(if (selectDay) config.backgroundColor else config.textColor)
                            alarm_days.text = getSelectedDaysString(alarm.days)
                            EasyDiaryDbHelper.commitTransaction()
                        }

                        edit_alarm_days_holder.addView(day)
                    }
                    alarm_days.text = getSelectedDaysString(alarm.days)
                    alarm_days.setTextColor(config.textColor)
                    alarm_switch.isChecked = alarm.isEnabled
                    alarmDescription.setText(alarm.label)
                    edit_alarm_time.text = getFormattedTime(alarm.timeInMinutes * 60, false, true)

                    edit_alarm_time.setOnClickListener {
                        TimePickerDialog(this@DevActivity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                            alarm.timeInMinutes = hourOfDay * 60 + minute
                            edit_alarm_time.text = getFormattedTime(alarm.timeInMinutes * 60, false, true)
                        }, alarm.timeInMinutes / 60, alarm.timeInMinutes % 60, DateFormat.is24HourFormat(this@DevActivity)).show()
                    }
                    alarm_switch.setOnCheckedChangeListener { _, isChecked ->
                        alarm.isEnabled = isChecked
                    }
                    deleteAlarm.setOnClickListener {
                        alertDialog?.dismiss()
                        EasyDiaryDbHelper.beginTransaction()
                        mAlarmList[position].deleteFromRealm()
                        EasyDiaryDbHelper.commitTransaction()
                        updateAlarmList()
                    }

                    if (this is ViewGroup) {
                        this.setBackgroundColor(config.backgroundColor)
                        initTextSize(this)
                        updateTextColors(this)
                        updateAppViews(this)
                        FontUtils.setFontsTypeface(this@DevActivity, this@DevActivity.assets, null, this)
                    }
                }
                alertDialog = builder.create().apply {
                    setView(rootView)
                    window?.setBackgroundDrawable(ColorDrawable(baseConfig.backgroundColor))
                    val globalTypeface = FontUtils.getCommonTypeface(this@DevActivity, this@DevActivity.assets)
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    val customTitle = TextView(this@DevActivity).apply {
                        text = when (alarm.workMode) {
                            0 -> "다이어리 쓰기 알림 설정"
                            else -> ""
                        }
                        setTextColor(baseConfig.textColor)
                        typeface = globalTypeface
                        val padding = CommonUtils.dpToPixel(this@DevActivity, 10F)
                        setPadding(padding * 2, padding, padding * 2, padding)
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18F)
//                        setBackgroundColor(resources.getColor(android.R.color.white))
                    }
                    setCustomTitle(customTitle)
                    show()
                    getButton(AlertDialog.BUTTON_POSITIVE).run {
                        setTextColor(baseConfig.textColor)
                        typeface = globalTypeface
                    }
                    getButton(AlertDialog.BUTTON_NEGATIVE).run {
                        setTextColor(baseConfig.textColor)
                        typeface = globalTypeface
                    }
                    getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(baseConfig.textColor)
                }
            }
        )

        alarmRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(this@DevActivity, 1)
            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter = mAlarmAdapter
        }

        initProperties()
        initDevUI()
        bindEvent()
        updateAlarmList()
    }

    override fun onPause() {
        super.onPause()
        EasyDiaryUtils.changeDrawableIconColor(this, android.R.color.white, R.drawable.delete_w)
    }

    override fun onResume() {
        super.onResume()
        EasyDiaryUtils.changeDrawableIconColor(this, config.textColor, R.drawable.delete_w)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    fun updateAlarmList() {
        mAlarmList.clear()
        mAlarmList.addAll(EasyDiaryDbHelper.readAlarmAll())
        mAlarmAdapter.notifyDataSetChanged()
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.circle_background_filled else R.drawable.circle_background_stroke
        val drawable = ContextCompat.getDrawable(this, drawableId)
        drawable!!.applyColorFilter(config.textColor)
        return drawable
    }

    private fun initProperties() {
        config.use24HourFormat = false
        val calendar = Calendar.getInstance(Locale.getDefault())
        var minutes = calendar.get(Calendar.HOUR_OF_DAY) * 60
        minutes += calendar.get(Calendar.MINUTE)

        val tempAlarm = Alarm(0)
        if (EasyDiaryDbHelper.countAlarmAll() == 0L) {
            EasyDiaryDbHelper.insertAlarm(tempAlarm)
        }
    }

    private fun initDevUI() {
    }

    private fun bindEvent() {
        nextAlarm.setOnClickListener {
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

        addAlarm.setOnClickListener {
            EasyDiaryDbHelper.insertAlarm(Alarm())
            updateAlarmList()
        }
    }

    companion object {
        const val ALARM_ID = "alarm_id"
    }
}

class SpacesItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        when (position == 0) {
            true -> outRect.top = 0
            false -> outRect.top = space
        }
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
    val intent = Intent(this, DiaryInsertActivity::class.java).apply {
        putExtra(DIARY_INSERT_MODE, MODE_REMINDER)
    }
    return PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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

    // Backup diary database file
//    exportRealmFile()

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
            .setContentTitle("Easy Diary alarm: ${alarm.id}")
            .setContentText(alarm.label)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

    val notification = builder.build()
//    notification.flags = notification.flags or Notification.FLAG_INSISTENT
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

// format day bits to strings like "Mon, Tue, Wed"
fun Context.getSelectedDaysString(bitMask: Int): String {
    val dayBits = arrayListOf(MONDAY_BIT, TUESDAY_BIT, WEDNESDAY_BIT, THURSDAY_BIT, FRIDAY_BIT, SATURDAY_BIT, SUNDAY_BIT)
    val weekDays = arrayListOf("월", "화", "수", "목", "금", "토", "일")

    if (baseConfig.isSundayFirst) {
        dayBits.moveLastItemToFront()
        weekDays.moveLastItemToFront()
    }

    var days = ""
    dayBits.forEachIndexed { index, bit ->
        if (bitMask and bit != 0) {
            days += "${weekDays[index]}, "
        }
    }
    return days.trim().trimEnd(',')
}

fun Activity.showOverLockScreen() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
}
