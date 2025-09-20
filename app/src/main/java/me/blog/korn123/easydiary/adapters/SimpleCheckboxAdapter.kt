package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.ItemSimpleCheckboxBinding
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateTextColors

class SimpleCheckboxAdapter (
        private val activity: Activity,
        private val realmFiles: List<SimpleCheckbox>
) : RecyclerView.Adapter<SimpleCheckboxAdapter.SimpleCheckboxViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleCheckboxViewHolder {
        return SimpleCheckboxViewHolder(activity, ItemSimpleCheckboxBinding.inflate(activity.layoutInflater, parent, false), this)
    }

    override fun getItemCount(): Int = realmFiles.size

    override fun onBindViewHolder(holder: SimpleCheckboxViewHolder, position: Int) {
        holder.bindTo(realmFiles[position])
    }

    fun onItemHolderClick(itemHolder: SimpleCheckboxViewHolder) {
        itemHolder.itemSimpleCheckboxBinding.checkbox.isChecked = itemHolder.itemSimpleCheckboxBinding.checkbox.isChecked.not()
    }

    fun onItemCheckedChange(position: Int, isChecked: Boolean) {
        realmFiles[position].isChecked = isChecked
    }

    class SimpleCheckboxViewHolder(
            activity: Activity, val itemSimpleCheckboxBinding: ItemSimpleCheckboxBinding, val adapter: SimpleCheckboxAdapter
    ) : RecyclerView.ViewHolder(itemSimpleCheckboxBinding.root), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        init {
            if (itemView is ViewGroup) {
                (itemView as ViewGroup).run {
                    activity.updateTextColors(this)
                    activity.initTextSize(this)
                    activity.updateAppViews(this)
                    FontUtils.setFontsTypeface(activity, null, this)
                    setOnClickListener(this@SimpleCheckboxViewHolder)
                    itemSimpleCheckboxBinding.checkbox.setOnCheckedChangeListener(this@SimpleCheckboxViewHolder)
                }
            }
        }

        fun bindTo(simpleCheckbox: SimpleCheckbox) {
            itemSimpleCheckboxBinding.run {
                title.text = simpleCheckbox.title
                description.text = simpleCheckbox.description
                checkbox.isChecked = simpleCheckbox.isChecked
            }
        }

        override fun onClick(p0: View?) {
            adapter.onItemHolderClick(this)
        }

        override fun onCheckedChanged(p0: CompoundButton, p1: Boolean) {
            adapter.onItemCheckedChange(this.adapterPosition, p1)
        }
    }

    data class SimpleCheckbox(var title: String, var description: String, var isChecked: Boolean = false)
}


