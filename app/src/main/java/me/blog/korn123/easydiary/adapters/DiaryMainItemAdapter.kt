package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryMainItemAdapter(
        private val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(activity, layoutResourceId, list) {
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder 
        if (row == null) {
            row = activity.layoutInflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.text1)
            holder.textView2 = row.findViewById(R.id.text2)
            holder.textView3 = row.findViewById(R.id.text3)
            holder.imageView = row.findViewById(R.id.weather)
            holder.item_holder = row.findViewById(R.id.item_holder)
            holder.photoContainer = row.findViewById(R.id.photoContainer)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val diaryDto = list[position]
        if (StringUtils.isEmpty(diaryDto.title)) {
            holder.textView1?.visibility = View.GONE
        } else {
            holder.textView1?.visibility = View.VISIBLE
        }
        holder.textView1?.text = diaryDto.title
        holder.textView2?.text = diaryDto.contents

        // highlight current query
        if (StringUtils.isNotEmpty(currentQuery)) {
            if (context.config.diarySearchQueryCaseSensitive) {
                EasyDiaryUtils.highlightString(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightString(holder.textView2, currentQuery)
            } else {
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView2, currentQuery)
            }

        }
        holder.textView3?.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.weather)

        holder.item_holder?.let {
            context.updateTextColors(it, 0, 0)
            context.initTextSize(it, context)
        }

        FontUtils.setFontsTypeface(context, context.assets, null, holder.textView1, holder.textView2, holder.textView3)

        if (diaryDto.photoUris?.size ?: 0 > 0) {
            holder.photoContainer.visibility = View.VISIBLE
            if (holder.photoContainer.childCount > 0) holder.photoContainer.removeAllViews()
            context?.let { appContext ->
                diaryDto.photoUris?.map {
                    val bitmap = CommonUtils.photoUriToDownSamplingBitmap(appContext, it, 70, 30, 30)
                    val imageView = ImageView(context)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(appContext, 33, 1), CommonUtils.dpToPixel(appContext, 33, 1))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(appContext, 3, 1), 0)
                    imageView.layoutParams = layoutParams
//                        imageView.setBackgroundResource(R.drawable.bg_card_thumbnail)
                    val drawable = ContextCompat.getDrawable(appContext, R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(context.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    holder.photoContainer.addView(imageView)
                }
            }
        } else {
            holder.photoContainer.visibility = View.GONE
        }
        
        return row
    }

    private class ViewHolder {
        lateinit var photoContainer: LinearLayout
        var textView1: TextView? = null
        var textView2: TextView? = null
        var textView3: TextView? = null
        var imageView: ImageView? = null
        var item_holder: ViewGroup? = null
    }
}
