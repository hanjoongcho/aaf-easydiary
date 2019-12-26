package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import kotlinx.android.synthetic.main.item_check_label.view.*

/**
 * Refactored code on 2019-12-25.
 *
 */
class OptionItemAdapter(
        val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>,
        private val selectedValue: Float
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(itemView.textView, itemView.checkIcon)
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            val size = list[position]["optionValue"] ?: "0"
            if (selectedValue == size.toFloat()) {
                val drawable = ContextCompat.getDrawable(context, R.drawable.check_mark)
                drawable?.let {
                    it.setColorFilter(context.config.primaryColor, PorterDuff.Mode.SRC_IN)
                    imageView.setImageDrawable(it)
                }
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
            }
            textView.text = list[position]["optionTitle"]
        }


        return itemView
    }

    private class ViewHolder(val textView: TextView, val imageView: ImageView)
}
