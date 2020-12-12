package me.blog.korn123.easydiary.adapters

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.fragments.PhotoFlexItemOptionFragment
import me.blog.korn123.easydiary.viewholders.PhotoViewHolder

class PhotoAdapter(
        val activity: AppCompatActivity,
        val postCardPhotoItems: List<PhotoViewHolder.PostCardPhotoItem>,
        private val dialogPositiveCallback: () -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<PhotoViewHolder>() {
    private val glideOptionMap = hashMapOf<Int, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_photo, parent, false)
        return PhotoViewHolder(view, activity, itemCount)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        postCardPhotoItems[position].let { postCardPhotoItem ->

            if (itemCount == 2) {
                holder.itemView.layoutParams = (holder.itemView.layoutParams as FlexboxLayoutManager.LayoutParams).apply {
                    if (position == 0) {
                        isWrapBefore = false
                        flexGrow = 1F
                    } else {
                        isWrapBefore = true
                        flexGrow = 0F
                    }
                }
            }


            holder.itemView.setOnClickListener { _ ->
                PhotoFlexItemOptionFragment.newInstance(postCardPhotoItem).apply {
                    positiveCallback = { viewMode, filterMode, forceSinglePhotoPosition ->
                        postCardPhotoItem.viewMode = viewMode
                        postCardPhotoItem.filterMode = filterMode
                        postCardPhotoItem.forceSinglePhotoPosition = forceSinglePhotoPosition
                        holder.bindTo(postCardPhotoItem)
                        dialogPositiveCallback.invoke()
                        notifyDataSetChanged()
                    }
                }.show(activity.supportFragmentManager, "")
            }

            holder.bindTo(postCardPhotoItem)
        }
    }

    override fun getItemCount() = postCardPhotoItems.size
    
//    fun getFlexDirection(): Int = when (activity.resources.configuration.orientation == ORIENTATION_PORTRAIT) {
//        true -> {
//            when (itemCount) {
//                3, 5, 6 -> FlexDirection.COLUMN
//                else -> FlexDirection.ROW
//            }
//        }
//        false -> FlexDirection.COLUMN
//    }
}
