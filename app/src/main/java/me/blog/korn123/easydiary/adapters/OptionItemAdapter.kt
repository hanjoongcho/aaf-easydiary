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
import me.blog.korn123.commons.utils.FontUtils
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
        private val selectedValueFloat: Float?,
        private val selectedValueString: String? = null
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)
        if (itemView is ViewGroup) {
            activity.run {
                initTextSize(itemView)
                updateTextColors(itemView)
                FontUtils.setFontsTypeface(this, null, itemView)
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
            val optionValue = list[position]["optionValue"] ?: "0"
            if ((selectedValueFloat != null && selectedValueFloat == optionValue.toFloat()) || selectedValueString == optionValue) {
                activity.updateDrawableColorInnerCardView(R.drawable.ic_check_mark)
                ContextCompat.getDrawable(context, R.drawable.ic_check_mark).run {
                    imageView.setImageDrawable(this)
                    imageView.alpha = 1F
                }
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ic_check_mark_off))
                imageView.alpha = 0.1F
            }
            textView.text = list[position]["optionTitle"]
        }


        return itemView
    }

    private class ViewHolder(val textView: TextView, val imageView: ImageView)
}
