package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.item_realm_file.view.*

/**
 * Refactored code on 2019-12-25.
 *
 */
class RealmFileItemAdapter(
        activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context).inflate(this.layoutResourceId, parent, false)

        when (itemView.tag is ViewHolder) {
            true -> itemView.tag as ViewHolder
            false -> {
                val holder = ViewHolder(itemView.fileName, itemView.createdTime)
                itemView.tag = holder
                holder
            }
        }.run {
            name.text = list[position]["name"]
            createdTime.text = list[position]["createdTime"]
        }

        return itemView
    }

    class ViewHolder(val name: TextView, val createdTime: TextView)
}
