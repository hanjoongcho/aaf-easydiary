package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_second.view.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-25.
 *
 */
class SecondItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>,
        private val mSeconds: Int
) : ArrayAdapter<Map<String, String>>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val holder = ViewHolder(itemView.seconds)
                itemView.tag = holder
                holder
            }
        }.run {
            seconds.text = list[position]["label"]
            if (position == mSeconds) {
                seconds.run {
                    setTextColor(context.config.primaryColor)
                    setTypeface(typeface, Typeface.BOLD)
                }
            } else {
                seconds.run {
                    setTextColor(ContextCompat.getColor(context, R.color.default_text_color))
                    setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        return itemView
    }

    private class ViewHolder(val seconds: TextView)
}
