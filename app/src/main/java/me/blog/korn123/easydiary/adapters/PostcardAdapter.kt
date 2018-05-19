package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.viewholders.PostcardViewHolder
import java.io.File

internal class PostcardAdapter(
        val activity: Activity,
        val listPostcard: List<File>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<PostcardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_post_card, parent, false)
        return PostcardViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
//        val pos = position % POST_CARDS.size
        holder.bindTo(listPostcard[position])
        holder.itemView.setOnClickListener { 
//            DialogUtil.showTips(activity, "", POST_CARDS[holder.adapterPosition].absolutePath)
            onItemClickListener.onItemClick(null, it, holder.adapterPosition, holder.itemId)
        }
    }

    override fun getItemCount() = listPostcard.size
}
