package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.NotificationService
import java.lang.StringBuilder


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

        updateActionLog()

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

        clearGoogleOauthToken.setOnClickListener {
            GoogleOAuthHelper.signOutGoogleOAuth(this, true)
        }

        clearLog.setOnClickListener {
            EasyDiaryDbHelper.deleteActionLogAll()
            updateActionLog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()

        when (resultCode == Activity.RESULT_OK && intent != null) {
            true -> {
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
                        val googleSignAccount = task.getResult(ApiException::class.java)
                        googleSignAccount?.account?.let {
                            GoogleOAuthHelper.callAccountCallback(it)
                        }
                    }
                }
            }
            false -> {
                when (requestCode) {
                    REQUEST_CODE_GOOGLE_SIGN_IN -> {
                        makeSnackBar("Google account verification failed.")
                    }
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initDevUI() { }

    private fun updateActionLog() {
        val actionLogs: List<ActionLog> = EasyDiaryDbHelper.readActionLogAll()
        val sb = StringBuilder()
        actionLogs.map {
            sb.append("${it.className}-${it.signature}-${it.key}: ${it.value}\n")
        }
        actionLog.text = sb.toString()
    }

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





