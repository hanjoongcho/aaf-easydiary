package me.blog.korn123.easydiary.adapters

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import io.github.aafactory.commons.extensions.dpToPixel
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.PhotoUri
import me.blog.korn123.easydiary.views.CornerImageView

class HistoryAdapter(val context: Context) : BaseBannerAdapter<PhotoUri>() {
    override fun bindData(
        holder: BaseViewHolder<PhotoUri>?,
        data: PhotoUri,
        position: Int,
        pageSize: Int
    ) {
        val path = EasyDiaryUtils.getApplicationDataDirectory(context) + data.getFilePath()
        holder?.findViewById<CornerImageView>(R.id.banner_image)?.let { imageView ->
            imageView.setRoundCorner(context.dpToPixel(8F))
            val options = RequestOptions()
                .error(R.drawable.ic_error_7)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
            Glide.with(context)
                .load(path)
                .apply(options)
                .into(imageView)
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_history
    }
}