package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import kotlinx.android.synthetic.main.item_second.view.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
class SecondItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>,
        private val mSeconds: Int
) : ArrayAdapter<Map<String, String>>(context, layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView: View? = convertView
        val viewHolder: ViewHolder
        when (itemView == null) {
            true -> {
                itemView = LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)
                viewHolder = ViewHolder()
                viewHolder.seconds = itemView.seconds
                itemView.tag = viewHolder
            }
            false -> viewHolder = itemView.tag as ViewHolder
        }

        viewHolder.seconds?.text = list[position]["label"]
        if (position == mSeconds) {
            viewHolder.seconds?.run {
                setTextColor(context.config.primaryColor)
                setTypeface(typeface, Typeface.BOLD)
            }
        } else {
            viewHolder.seconds?.run {
                setTextColor(ContextCompat.getColor(context, R.color.default_text_color))
                setTypeface(null, Typeface.NORMAL)
            }
        }

        return itemView!!
    }

    private class ViewHolder {
        var seconds: TextView? = null
    }
}
