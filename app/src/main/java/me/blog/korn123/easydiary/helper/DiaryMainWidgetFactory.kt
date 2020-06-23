package me.blog.korn123.easydiary.helper

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.DiaryDto


class DiaryMainWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private val diaryItems: ArrayList<DiaryDto> = arrayListOf()

    override fun onCreate() {
        setData()
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(p0: Int): Long = 0

    override fun onDataSetChanged() {
        setData()
    }

    override fun hasStableIds(): Boolean = false

    override fun getViewAt(position: Int): RemoteViews {
        val listViewWidget = RemoteViews(context.packageName, R.layout.widget_item_diary_main)
        val diaryDto = diaryItems[position]
        listViewWidget.setTextViewText(R.id.text1, diaryDto.title)

//        val dataIntent = Intent()
//        dataIntent.putExtra("item_id", arrayList.get(position)._id)
//        dataIntent.putExtra("item_data", arrayList.get(position).content)
//        listViewWidget.setOnClickFillInIntent(R.id.text1, dataIntent)
        return listViewWidget
    }

    override fun getCount(): Int = diaryItems.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {}

    private fun setData() {
        diaryItems.clear()
        diaryItems.addAll(EasyDiaryDbHelper.readDiary(null))
    }
}