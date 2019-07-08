package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class SecondItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>,
        private val mSecond: Int
) : ArrayAdapter<Map<String, String>>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        var holder: ViewHolder? 
        if (row == null) {
            val inflater = (this.context as Activity).layoutInflater
            row = inflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView = row?.findViewById<View>(R.id.textView) as TextView
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        holder.textView?.text = list[position]["label"]
        if (position == mSecond) {
            holder.textView?.run {
                setTextColor(context.config.primaryColor)
                setTypeface(typeface, Typeface.BOLD)
            }
        } else {
            holder.textView?.run {
                setTextColor(ContextCompat.getColor(context, R.color.default_text_color))
                setTypeface(null, Typeface.NORMAL)
            }
        }
        return row
    }

    private class ViewHolder {
        internal var textView: TextView? = null
    }
}
