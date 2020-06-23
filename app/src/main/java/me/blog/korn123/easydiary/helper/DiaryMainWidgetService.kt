package me.blog.korn123.easydiary.helper

import android.content.Intent
import android.widget.RemoteViewsService

class DiaryMainWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return DiaryMainWidgetFactory(this.applicationContext)
    }
}