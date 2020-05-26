package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_check_label.view.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
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
                val viewHolder = ViewHolder(itemView.textView, itemView. checkIcon)
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            if (StringUtils.equals(context.config.settingFontName, list[position]["fontName"])) {
                activity.updateDrawableColorInnerCardView(R.drawable.check_mark)
                ContextCompat.getDrawable(context, R.drawable.check_mark).run {
                    imageView.setImageDrawable(this)
                    imageView.alpha = 1F
                }
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
                imageView.alpha = 0.1F
            }
            textView.text = StringUtils.EMPTY
//        holder.textView?.typeface = FontUtils.getTypeface(context, context.assets, list[position]["fontName"])
            this.position = position
//        Log.i("fontDialog", "$position")
            RenderTask(context, this, position).execute()
        }


        return itemView
    }

    class ViewHolder(val textView: TextView, val imageView: ImageView, var position: Int = 0)

    inner class RenderTask(val context: Context, private val holder: ViewHolder, private val position: Int) : AsyncTask<String, Void, Typeface>() {
        override fun doInBackground(vararg param: String?): Typeface? {
            var typeface: Typeface? = null
            if (position == holder.position) {
                typeface = FontUtils.getTypeface(context, context.assets, list[position]["fontName"])
            } else {
                this.cancel(true)
            }
            return typeface
        }

        override fun onPostExecute(targetTypeface: Typeface?) {
            if (holder.position == position) {
                holder.textView.run {
                    typeface = targetTypeface
                    text = list[position]["disPlayFontName"]
                }
            }
        }
    }
}
