package me.blog.korn123.easydiary.adapters

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.PostCardViewerActivity
import me.blog.korn123.easydiary.viewholders.PostcardViewHolder

internal class PostcardAdapter(
        val activity: Activity,
        private val listPostcard: List<PostCardViewerActivity.PostCard>,
        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<PostcardViewHolder>() {
    private val TAG = this::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostcardViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_post_card, parent, false)
        return PostcardViewHolder(view, activity)
    }

    override fun onBindViewHolder(holder: PostcardViewHolder, position: Int) {
//        val pos = position % POST_CARDS.size
        holder.itemView.findViewById<CheckBox>(R.id.itemCheck).setOnCheckedChangeListener { _, isChecked ->
            Log.i(TAG, "isChecked: $isChecked")
            listPostcard[position].isItemChecked = isChecked
        }
        holder.itemView.setOnClickListener {
            //            DialogUtil.showTips(activity, "", POST_CARDS[holder.adapterPosition].absolutePath)
            onItemClickListener.onItemClick(null, it, holder.adapterPosition, holder.itemId)
        }
        holder.bindTo(listPostcard[position])
    }

    override fun getItemCount() = listPostcard.size
}
