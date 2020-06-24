package me.blog.korn123.easydiary.helper

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.DiaryDto


class DiaryMainWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private val diaryItems: ArrayList<DiaryDto> = arrayListOf()

    override fun onCreate() {
        setData()
    }

    override fun getLoadingView() = null

    override fun getItemId(position: Int) = position.toLong()

    override fun onDataSetChanged() {
        setData()
    }

    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {
        val listViewWidget = RemoteViews(context.packageName, R.layout.widget_item_diary_main)
        val diaryDto = diaryItems[position]
        listViewWidget.setTextViewText(R.id.text1, diaryDto.title)
        listViewWidget.setTextViewText(R.id.text3, when (diaryDto.isAllDay) {
            true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
            false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
        })

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
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val items = arrayListOf<DiaryDto>()
        EasyDiaryDbHelper.readDiary(null, false, 0, 0, 0, realmInstance).map {
            items.add(realmInstance.copyFromRealm(it))
        }
        diaryItems.clear()
        diaryItems.addAll(items)
        realmInstance.close()
    }
}