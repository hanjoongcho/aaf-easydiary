package me.blog.korn123.easydiary.helper

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R


class DiaryMainWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        performUpdate(context)
    }

    private fun getComponentName(context: Context) = ComponentName(context, this::class.java)

    private fun performUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            RemoteViews(context.packageName, getProperLayout(context)).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setImageViewResource(R.id.symbol, R.drawable.ic_select_symbol)
                } else {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_select_symbol)
                    val b = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                            drawable.intrinsicHeight,
                            Bitmap.Config.ARGB_8888)
                    val c = Canvas(b)
                    drawable.setBounds(0, 0, c.width, c.height)
                    drawable.draw(c)
                    setImageViewBitmap(R.id.symbol, b)
                }

                appWidgetManager.updateAppWidget(it, this)
            }
        }
    }

    private fun getProperLayout(context: Context) = if (isOreoPlus()) {
        R.layout.widget_diary_main
    } else {
        R.layout.widget_diary_main
    }
}