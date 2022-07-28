package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.AAF_TEST
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-26.
 *
 */

class FontItemAdapter(val activity: Activity, private val layoutResourceId: Int, private val list: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)
        if (itemView is ViewGroup) {
            activity.run {
                initTextSize(itemView)
                updateTextColors(itemView)
            }
        }

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(itemView.findViewById(R.id.textView), itemView.findViewById(R.id.checkIcon))
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            if (StringUtils.equals(context.config.settingFontName, list[position]["fontName"])) {
                activity.updateDrawableColorInnerCardView(R.drawable.ic_check_mark)
                ContextCompat.getDrawable(context, R.drawable.ic_check_mark).run {
                    imageView.setImageDrawable(this)
                    imageView.alpha = 1F
                }
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ic_check_mark_off))
                imageView.alpha = 0.1F
            }
            textView.text = StringUtils.EMPTY
            Log.i(AAF_TEST, "${this.position} -> $position")
//        holder.textView?.typeface = FontUtils.getTypeface(context, context.assets, list[position]["fontName"])

            this.position = position
//        Log.i("fontDialog", "$position")
            renderJob(activity, this, position)
        }
        return itemView
    }

    class ViewHolder(val textView: TextView, val imageView: ImageView, var position: Int = 0)

    inner class FontItemRenderer(val activity: Activity, val holder: ViewHolder, val position: Int) : Thread() {
        override fun run() {
            when (holder.position == position) {
                true -> {
                    holder.textView.run {
                        val tf = FontUtils.getTypeface(context, list[position]["fontName"])
                        activity.runOnUiThread {
                            typeface = tf
                            text = list[position]["disPlayFontName"]
                        }
                    }
                    Log.i(AAF_TEST, "$position End")
                }
                false -> { Log.i(AAF_TEST, "$position Cancel") }
            }
        }
    }

    private fun renderJob(activity: Activity, holder: ViewHolder, position: Int) {
        Log.i(AAF_TEST, "$position Start")
//        FontItemRenderer(activity, holder, position).apply { start() }

        CoroutineScope(Dispatchers.IO).launch {
            when (holder.position == position) {
                true -> {
                    holder.textView.run {
                        val tf = FontUtils.getTypeface(context, list[position]["fontName"])
                        withContext(Dispatchers.Main) {
                            val label = if (activity.config.enableDebugMode) "\uD83C\uDF0E\uD83D\uDCF1${list[position]["disPlayFontName"]}" else list[position]["disPlayFontName"]
                            typeface = tf
                            text = label
                        }
                        if (activity.config.enableDebugMode) EasyDiaryUtils.highlightString(this)
                    }
                    Log.i(AAF_TEST, "${holder.position} End")
                }
                false -> { Log.i(AAF_TEST, "$position Cancel") }
            }
        }
    }
}
