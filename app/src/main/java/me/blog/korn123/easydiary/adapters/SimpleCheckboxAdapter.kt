package me.blog.korn123.easydiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_simple_checkbox.view.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateTextColors

internal class SimpleCheckboxAdapter (
        private val realmFiles: List<SimpleCheckbox>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<SimpleCheckboxViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleCheckboxViewHolder {
        val viewGroup = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.viewholder_simple_checkbox, parent, false) as ViewGroup
        return SimpleCheckboxViewHolder(viewGroup)
    }

    override fun getItemCount(): Int = realmFiles.size

    override fun onBindViewHolder(holder: SimpleCheckboxViewHolder, position: Int) {
        holder.itemView.checkbox.setOnCheckedChangeListener { _, isChecked ->
            realmFiles[position].isChecked = isChecked
        }
        holder.itemView.setOnClickListener { view ->
            view.checkbox.isChecked = !view.checkbox.isChecked
        }
        holder.bindTo(realmFiles[position])
    }
}

class SimpleCheckboxViewHolder(val viewGroup: ViewGroup) : RecyclerView.ViewHolder(viewGroup)  {
    fun bindTo(simpleCheckbox: SimpleCheckbox) {
        itemView.run {
            title.text = simpleCheckbox.title
            description.text = simpleCheckbox.description
            checkbox.isChecked = simpleCheckbox.isChecked
        }
        viewGroup.run {
            this.context.updateTextColors(this)
            this.context.initTextSize(this)
            this.context.updateAppViews(this)
            FontUtils.setFontsTypeface(this.context, this.context.assets, null, this)
        }
    }
}

data class SimpleCheckbox(var title: String, var description: String, var isChecked: Boolean = false)
