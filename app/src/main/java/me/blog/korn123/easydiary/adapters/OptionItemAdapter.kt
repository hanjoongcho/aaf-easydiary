package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_check_label.view.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors

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
        if (itemView is ViewGroup) {
            activity.run {
                initTextSize(itemView)
                updateTextColors(itemView)
            }
        }

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
                activity.updateDrawableColorInnerCardView(R.drawable.check_mark)
                ContextCompat.getDrawable(context, R.drawable.check_mark).run {
                    imageView.setImageDrawable(this)
                    imageView.alpha = 1F
                }
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
                imageView.alpha = 0.1F
            }
            textView.text = list[position]["optionTitle"]
        }


        return itemView
    }

    private class ViewHolder(val textView: TextView, val imageView: ImageView)
}
