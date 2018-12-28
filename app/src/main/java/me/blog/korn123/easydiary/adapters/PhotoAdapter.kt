package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.realm.RealmList
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.PhotoUriDto
import me.blog.korn123.easydiary.viewholders.PhotoViewHolder

class PhotoAdapter(
        val activity: Activity,
        val photoUris: RealmList<PhotoUriDto>
) : RecyclerView.Adapter<PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.viewholder_photo, parent, false)
        return PhotoViewHolder(view, activity, itemCount)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        photoUris[position]?.let {
            holder.bindTo(it.getFilePath(), position)
        }
    }

    override fun getItemCount() = photoUris.size
}
