package me.blog.korn123.easydiary.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import kotlinx.android.synthetic.main.layout_timeline_filter.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_DESCRIPTION
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_ID
import me.blog.korn123.easydiary.helper.NOTIFICATION_CHANNEL_NAME
import me.blog.korn123.easydiary.services.NotificationService


class DevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/


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

        nextAlarm.setOnClickListener {
            val nextAlarm = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val triggerTimeMillis = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: 0
                when (triggerTimeMillis > 0) {
                    true -> DateUtils.getFullPatternDateWithTime(triggerTimeMillis)
                    false -> "Alarm info is not exist."
                }
            } else {
                Settings.System.getString(contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
            }

            toast(nextAlarm, Toast.LENGTH_LONG)
        }

        notification1.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_01, createNotification(NotificationInfo(R.drawable.ic_diary_writing)))
            }
        }
        notification2.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_02, createNotification(NotificationInfo(R.drawable.ic_diary_backup_local, true)))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.diary_dev, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.devConsole -> {
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val popupView = (inflater.inflate(R.layout.popup_menu, null) as ViewGroup).apply {
                    updateAppViews(this)
                    initTextSize(this)
                    updateTextColors(this)
                    FontUtils.setFontsTypeface(applicationContext, assets, null, this, true)
                }
                val width = LinearLayout.LayoutParams.WRAP_CONTENT
                val height = LinearLayout.LayoutParams.WRAP_CONTENT
                val popup: PopupWindow = PopupWindow(popupView, width, height, true)
                popup.animationStyle = R.style.text_view_option_animation
//                popup.showAsDropDown(findViewById(R.id.devConsole), 0, 0)
                popup.showAtLocation(findViewById(R.id.devConsole), Gravity.TOP or Gravity.RIGHT,0, 0)
                popupView.x = 300f
                popupView.y = -300f
                val animX = ObjectAnimator.ofFloat(popupView, "x", 0f)
                val animY = ObjectAnimator.ofFloat(popupView, "y", 0f)
                AnimatorSet().apply {
                    playTogether(animX, animY)
                    duration = 150
                    start()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initDevUI() { }

    @SuppressLint("NewApi")
    private fun createNotification(notificationInfo: NotificationInfo): Notification {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", "${NOTIFICATION_CHANNEL_NAME}_dev", importance)
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev")
        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, notificationInfo.largeIconResourceId))
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle("content title")
                .setContentText("content text")
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )
        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                    R.drawable.ic_easydiary,
                    getString(R.string.dismiss),
                    PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                        action = NotificationService.ACTION_DISMISS_DEV
                    }, 0)
            )
        }
        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID_01 = 2000
        const val NOTIFICATION_ID_02 = 2001
    }
}


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/
class SpacesItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        when (position == 0) {
            true -> outRect.top = 0
            false -> outRect.top = space
        }
    }
}

data class NotificationInfo(var largeIconResourceId: Int, var useActionButton: Boolean = false)


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/





