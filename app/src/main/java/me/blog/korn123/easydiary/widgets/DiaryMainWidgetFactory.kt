package me.blog.korn123.easydiary.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.simplemobiletools.commons.extensions.setVisibleIf
import me.blog.korn123.commons.utils.BitmapUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.getCustomSymbolPaths
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.models.Diary

class DiaryMainWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private val diaryItems: ArrayList<Diary> = arrayListOf()

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
                true -> DateUtils.getDateStringFromTimeMillis(diaryDto.currentTimeMillis)
                false -> DateUtils.getDateTimeStringFromTimeMillis(diaryDto.currentTimeMillis)
            })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (diaryDto.weather < SYMBOL_USER_CUSTOM_START) {
                    setImageViewResource(R.id.diarySymbol, FlavorUtils.sequenceToSymbolResourceId(diaryDto.weather))
                } else {
                    EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
                        val targetIndex = diaryDto.weather.minus(SYMBOL_USER_CUSTOM_START)
                        val photoUris = getCustomSymbolPaths(SYMBOL_EASTER_EGG, realmInstance)
                        val filePath = if (photoUris.size > targetIndex) photoUris[targetIndex].getFilePath() else ""
//                        setImageViewBitmap(R.id.diarySymbol, BitmapUtils.decodeFileCropCenter(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath, 300))

                        Glide
                            .with(context).asBitmap()
                            .load(EasyDiaryUtils.getApplicationDataDirectory(context) + filePath)
                            .transform(CenterCrop(), RoundedCorners(context.dpToPixel(5F)))
                            .into(object : CustomTarget<Bitmap>(300, 300) {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    setImageViewBitmap(R.id.diarySymbol, resource)

                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                        realmInstance.close()

                    }
                }
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

    override fun onDestroy() { }

    private fun setData() {
        diaryItems.clear()
        if (!context.config.aafPinLockEnable && !context.config.fingerprintLockEnable) {
            EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
                val realmList = EasyDiaryDbHelper.findDiary(null, false, 0, 0, 0, realmInstance)
                val limit = if (realmList.size > 100) 100 else realmList.size
                diaryItems.addAll(realmInstance.copyFromRealm(realmList.subList(0, limit)))
                realmInstance.close()
            }
        }
    }
}