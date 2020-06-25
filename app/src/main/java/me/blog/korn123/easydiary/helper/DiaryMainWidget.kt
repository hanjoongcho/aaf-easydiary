package me.blog.korn123.easydiary.helper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.widget.RemoteViews
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryInsertActivity
import me.blog.korn123.easydiary.extensions.changeBitmapColor
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config


class DiaryMainWidget : AppWidgetProvider() {

    companion object {
        const val OPEN_WRITE_PAGE = "open_write_page"
        const val UPDATE_WIDGET = "update_widget"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            OPEN_WRITE_PAGE -> {
                context.startActivity(Intent(context, DiaryInsertActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            }
            UPDATE_WIDGET -> {
                performUpdate(context)
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        performUpdate(context)
    }

    private fun getComponentName(context: Context) = ComponentName(context, this::class.java)

    private fun performUpdate(context: Context) {
        var iconColor = 0
        var toolbarColor = 0
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                toolbarColor = Color.parseColor("#ff121212")
                iconColor = Color.parseColor("#ffffffff")
            }
            else -> {
                toolbarColor = Color.parseColor("#ffa3a3a3")
                iconColor = Color.parseColor("#ffffffff")
            }
        }
        context.run {
            changeDrawableIconColor(config.textColor, R.drawable.edit)
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            RemoteViews(context.packageName, getProperLayout(context)).apply {
                setInt(R.id.widgetToolbar, "setBackgroundColor", toolbarColor)
                setImageViewBitmap(R.id.openWritePage, context.changeBitmapColor(R.drawable.edit, iconColor))
                setImageViewBitmap(R.id.updateWidget, context.changeBitmapColor(R.drawable.update, iconColor))

                setupIntent(context, this, OPEN_WRITE_PAGE, R.id.openWritePage)
                setupIntent(context, this, UPDATE_WIDGET, R.id.updateWidget)

                Intent(context, DiaryMainWidgetService::class.java).apply {
                    setRemoteAdapter(R.id.diaryListView, this)
                }
                setEmptyView(R.id.diaryListView, R.id.widget_event_list_empty)
                appWidgetManager.updateAppWidget(it, this)
                appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.diaryListView)
            }
        }
    }

    private fun setupIntent(context: Context, views: RemoteViews, action: String, id: Int) {
        Intent(context, DiaryMainWidget::class.java).apply {
            this.action = action
            val pendingIntent = PendingIntent.getBroadcast(context, 0, this, 0)
            views.setOnClickPendingIntent(id, pendingIntent)
        }
    }

    private fun getProperLayout(context: Context) = if (isOreoPlus()) {
        R.layout.widget_diary_main
    } else {
        R.layout.widget_diary_main
    }
}