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


class OptionItemAdapter(val activity: Activity, private val layoutResourceId: Int, private val list: List<Map<String, String>>, val selectedValue: Float
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row: View? = when (convertView) {
            null -> {
                activity.layoutInflater.inflate(this.layoutResourceId, parent, false)
            }
            else -> convertView
        }
        
        when (row?.tag) {
            null -> {
                row?.tag = ViewHolder().apply {
                    textView = row?.findViewById(R.id.textView)
                    imageView = row?.findViewById(R.id.checkIcon)
                }
            }    
        }
        
        val holder = row?.tag 
        if (holder is ViewHolder) {
            val size = list[position]["optionValue"] ?: "0"
            if (selectedValue == size.toFloat()) {
                val drawable = ContextCompat.getDrawable(context, R.drawable.check_mark)
                drawable?.let {
                    it.setColorFilter(context.config.primaryColor, PorterDuff.Mode.SRC_IN)
                    holder.imageView?.setImageDrawable(it)
                }
            } else {
                holder.imageView?.setImageBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.check_mark_off))
            }
            holder.textView?.text = list[position]["optionTitle"]
        }
        
        return row!!
    }

    class ViewHolder {
        var textView: TextView? = null
        var imageView: ImageView? = null
    }
}
