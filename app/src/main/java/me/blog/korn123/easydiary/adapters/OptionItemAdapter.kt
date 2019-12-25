package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import kotlinx.android.synthetic.main.item_check_label.view.*

class OptionItemAdapter(
        val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>,
        private val selectedValue: Float
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView: View? = convertView
        val viewHolder: ViewHolder
        when (itemView == null) {
            true -> {
                itemView = activity.layoutInflater.inflate(this.layoutResourceId, parent, false)
                viewHolder = ViewHolder()
                viewHolder.textView = itemView.textView
                viewHolder.imageView = itemView.checkIcon
                itemView.tag = viewHolder
            }
            false -> {
                viewHolder = itemView.tag as ViewHolder
            }
        }

        val size = list[position]["optionValue"] ?: "0"
        if (selectedValue == size.toFloat()) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.check_mark)
            drawable?.let {
                it.setColorFilter(context.config.primaryColor, PorterDuff.Mode.SRC_IN)
                viewHolder.imageView?.setImageDrawable(it)
            }
        } else {
            viewHolder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
        }
        viewHolder.textView?.text = list[position]["optionTitle"]

        return itemView!!
    }

    private class ViewHolder {
        var textView: TextView? = null
        var imageView: ImageView? = null
    }
}
