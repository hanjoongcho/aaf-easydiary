package me.blog.korn123.easydiary.helper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.widget.RemoteViews
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryInsertActivity
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config


class DiaryMainWidget : AppWidgetProvider() {

    companion object {
        const val OPEN_WRITE_PAGE = "open_write_page"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            OPEN_WRITE_PAGE -> {
                context.startActivity(Intent(context, DiaryInsertActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            }
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        context.run {
            changeDrawableIconColor(config.textColor, R.drawable.edit)
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            RemoteViews(context.packageName, getProperLayout(context)).apply {
                var drawable = AppCompatResources.getDrawable(context!!, R.drawable.edit)
                val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        colorFilter = BlendModeColorFilter(android.R.attr.textColorPrimary, BlendMode.SRC_IN);
                    } else {
                        setColorFilter(android.R.attr.textColorPrimary, PorterDuff.Mode.SRC_IN)
                    }
                }
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                setImageViewBitmap(R.id.openWritePage, bitmap)

                setupIntent(context, this, OPEN_WRITE_PAGE, R.id.openWritePage)

                Intent(context, DiaryMainWidgetService::class.java).apply {
                    setRemoteAdapter(R.id.diaryListView, this)
                }
                setEmptyView(R.id.diaryListView, R.id.widget_event_list_empty)
                appWidgetManager.updateAppWidget(it, this)
                appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.diaryListView)
            }
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun getComponentName(context: Context) = ComponentName(context, this::class.java)

    private fun performUpdate(context: Context) {

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