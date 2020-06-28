package me.blog.korn123.easydiary.widgets

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
import me.blog.korn123.easydiary.activities.DiaryReadActivity
import me.blog.korn123.easydiary.extensions.changeBitmapColor
import me.blog.korn123.easydiary.helper.DIARY_EXECUTION_MODE
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_ACCESS_FROM_OUTSIDE
import me.blog.korn123.easydiary.services.DiaryMainWidgetService


class DiaryMainWidget : AppWidgetProvider() {

    companion object {
        const val OPEN_WRITE_PAGE = "open_write_page"
        const val OPEN_READ_PAGE = "open_read_page"
        const val UPDATE_WIDGET = "update_widget"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            OPEN_WRITE_PAGE -> {
                context.startActivity(Intent(context, DiaryInsertActivity::class.java).apply {
                    putExtra(DIARY_EXECUTION_MODE, EXECUTION_MODE_ACCESS_FROM_OUTSIDE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
            OPEN_READ_PAGE -> {
                context.startActivity(Intent(context, DiaryReadActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(DIARY_EXECUTION_MODE, EXECUTION_MODE_ACCESS_FROM_OUTSIDE)
                    putExtra(DIARY_SEQUENCE, intent.getIntExtra(DIARY_SEQUENCE, -1))
                })
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
                toolbarColor = Color.parseColor("#ffd6d6d6")
                iconColor = Color.parseColor("#ffffffff")
            }
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


                val pendingIntent: PendingIntent = Intent(
                        context,
                        DiaryMainWidget::class.java
                ).run {
                    action = OPEN_READ_PAGE
                    PendingIntent.getBroadcast(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT)
                }
                setPendingIntentTemplate(R.id.diaryListView, pendingIntent)


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