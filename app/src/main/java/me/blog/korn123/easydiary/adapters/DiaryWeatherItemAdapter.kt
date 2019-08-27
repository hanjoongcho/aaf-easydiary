package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.DiarySymbol

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryWeatherItemAdapter(
        context: Context,
        private val mLayoutResourceId: Int,
        private val mList: List<DiarySymbol>
) : ArrayAdapter<DiarySymbol>(context, mLayoutResourceId, mList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initRow(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initRow(position, convertView, parent)
    }

    private fun initRow(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val holder: ViewHolder? 
        if (row == null) {
            val inflater = (context as Activity).layoutInflater
            row = inflater.inflate(this.mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById<View>(R.id.text1) as TextView
            holder.imageView1 = row.findViewById<View>(R.id.imageView1) as ImageView
            holder.item_holder = row.findViewById(R.id.item_holder)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        FontUtils.setFontsTypeface(context, context.assets, null, holder.item_holder)
        FlavorUtils.initWeatherView(context, holder.imageView1, mList[position].sequence, true)
        holder.textView1?.let {
            it.text = mList[position].description
//            context.initTextSize(it, context)
        }

        holder.imageView1?.let {
            if (mList[position].sequence == 0) {
                it.visibility = View.GONE
            } else {
                it.visibility = View.VISIBLE
            }
        }
        
        return row!!
    }

    private class ViewHolder {
        internal var textView1: TextView? = null
        internal var imageView1: ImageView? = null
        internal var item_holder: LinearLayout? = null
    }
}
