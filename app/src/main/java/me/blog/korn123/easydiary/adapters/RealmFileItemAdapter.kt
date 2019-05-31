package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.blog.korn123.easydiary.R

class RealmFileItemAdapter(
        private val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<Map<String, String>>
) : ArrayAdapter<Map<String, String>>(activity , layoutResourceId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder
        when (row == null) {
            true -> {
                val inflater = (this.context as Activity).layoutInflater
                row = inflater.inflate(this.layoutResourceId, parent, false)
                holder = ViewHolder()
                holder.name = row.findViewById(R.id.fileName)
                holder.createdTime = row.findViewById(R.id.createdTime)
                row.tag = holder
            }
            false -> {
                holder = row.tag as ViewHolder    
            }
        }
        
        holder.name?.text = list[position]["name"]
        holder.createdTime?.text = list[position]["createdTime"]
        return row
    }

    class ViewHolder {
        var name: TextView? = null
        var createdTime: TextView? = null
    }
}
