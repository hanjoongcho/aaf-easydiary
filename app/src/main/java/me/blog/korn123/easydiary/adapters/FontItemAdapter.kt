package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class FontItemAdapter(val activity: Activity, private val layoutResourceId: Int, private val list: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder?
        if (row == null) {
            val inflater = (this.context as Activity).layoutInflater
            row = inflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView = row.findViewById(R.id.textView)
            holder.imageView = row.findViewById(R.id.checkIcon)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        if (StringUtils.equals(CommonUtils.loadStringPreference(context, Constants.SETTING_FONT_NAME, Constants.CUSTOM_FONTS_SUPPORTED_LANGUAGE_DEFAULT), list[position]["fontName"])) {
            holder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark))
        } else {
            holder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
        }
        holder.textView?.text = StringUtils.EMPTY
//        holder.textView?.typeface = FontUtils.getTypeface(context, context.assets, list[position]["fontName"])
        holder.position = position
//        Log.i("fontDialog", "$position")
        RenderTask(context, holder, position).execute()

        return row
    }

    class ViewHolder {
        var textView: TextView? = null
        var imageView: ImageView? = null
        var position: Int = 0
    }

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
                holder.textView?.run {
                    typeface = targetTypeface
                    text = list[position]["disPlayFontName"]
                }
            }
        }
    }
}
