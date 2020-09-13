package me.blog.korn123.easydiary.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.appcompat.content.res.AppCompatResources
import com.simplemobiletools.commons.extensions.setVisibleIf
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
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
        val widgetItem = RemoteViews(context.packageName, R.layout.widget_item_diary_main)
        val diaryDto = diaryItems[position]

        widgetItem.run {
            setTextViewText(R.id.text1, diaryDto.title)
            setTextViewText(R.id.text2, diaryDto.contents)
            setTextViewText(R.id.text3, when (diaryDto.isAllDay) {
                true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
            })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setImageViewResource(R.id.diarySymbol, FlavorUtils.sequenceToSymbolResourceId(diaryDto.weather))
            } else {
                val drawable = AppCompatResources.getDrawable(context, FlavorUtils.sequenceToSymbolResourceId(diaryDto.weather))
                val b = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                        drawable.intrinsicHeight,
                        Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                drawable.setBounds(0, 0, c.width, c.height)
                drawable.draw(c)
                setImageViewBitmap(R.id.diarySymbol, b)
            }

            setVisibleIf(R.id.text1, diaryDto.title.isNullOrEmpty().not())
            setVisibleIf(R.id.diarySymbol, diaryDto.weather > 0)
        }

        Intent().apply {
            putExtra(DIARY_SEQUENCE, diaryDto.sequence)
            widgetItem.setOnClickFillInIntent(R.id.widgetItem, this)
        }

        return widgetItem
    }

    override fun getCount(): Int = diaryItems.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {}

    private fun setData() {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        diaryItems.clear()
        diaryItems.addAll(EasyDiaryDbHelper.readDiary(null, false, 0, 0, 0, realmInstance))
        realmInstance.close()
    }
}