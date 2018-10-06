package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config


class ThumbnailSizeItemAdapter(val activity: Activity, private val layoutResourceId: Int, private val list: List<Map<String, String>>
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

        val size = list[position]["size"] ?: "0"
        if (context.config.settingThumbnailSize == size.toFloat()) {
            holder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark))
        } else {
            holder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
        }
        holder.textView?.text = list[position]["optionTitle"]

        return row
    }

    class ViewHolder {
        var textView: TextView? = null
        var imageView: ImageView? = null
    }
}
