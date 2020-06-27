package me.blog.korn123.easydiary.services

import android.content.Intent
import android.widget.RemoteViewsService
import me.blog.korn123.easydiary.widgets.DiaryMainWidgetFactory

class DiaryMainWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DiaryMainWidgetFactory(this.applicationContext)
    }
}