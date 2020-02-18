package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.item_weather.view.*
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.models.DiarySymbol

/**
 * Created by CHO HANJOONG on 2017-03-16.
 * Refactored code on 2019-12-26.
 *
 */

class DiaryWeatherItemAdapter(
        context: Context,
        private val layoutResourceId: Int,
        private val mList: List<DiarySymbol>
) : ArrayAdapter<DiarySymbol>(context, layoutResourceId, mList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initRow(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        return initRow(position, convertView, parent)
    }

    private fun initRow(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val viewHolder = ViewHolder(itemView.text1, itemView.imageView1, itemView.item_holder)
                itemView.tag = viewHolder
                viewHolder
            }
        }.run {
            FontUtils.setFontsTypeface(context, context.assets, null, item_holder)
            FlavorUtils.initWeatherView(context, imageView1, mList[position].sequence, false)
            textView1.let {
                it.text = mList[position].description
//            context.initTextSize(it, context)
            }

            imageView1.let {
                if (mList[position].sequence == 0) {
                    it.visibility = View.GONE
                } else {
                    it.visibility = View.VISIBLE
                }
            }
        }

        return itemView
    }

    private class ViewHolder (val textView1: TextView, val imageView1: ImageView, val item_holder: LinearLayout)
}
