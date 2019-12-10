package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.viewholder_post_card.view.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardViewerActivity
import me.blog.korn123.easydiary.viewholders.PostcardViewHolder

internal class PostcardAdapter(
        val activity: Activity,
        private val listPostcard: List<PostCardViewerActivity.PostCard>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<PostcardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_post_card, parent, false)
        return PostcardViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
        holder.itemView.itemCheck.setOnCheckedChangeListener { _, isChecked ->
            listPostcard[position].isItemChecked = isChecked
        }
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(null, it, holder.adapterPosition, holder.itemId)
        }
        holder.bindTo(listPostcard[position])
    }

    override fun getItemCount() = listPostcard.size
}
