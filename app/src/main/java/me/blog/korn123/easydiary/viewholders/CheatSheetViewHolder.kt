package me.blog.korn123.easydiary.viewholders

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_cheat_sheet.view.*

class CheatSheetViewHolder(itemView: View, val activity: Activity) : RecyclerView.ViewHolder(itemView) {
    fun bindTo(cheatSheet: CheatSheet) {
        itemView.run {
            text_title.text = cheatSheet.title
            text_description.text = cheatSheet.description
        }
    }

    data class CheatSheet(val title: String, val description: String, val url: String)
}
