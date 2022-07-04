package me.blog.korn123.easydiary.adapters

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemHistoryBinding
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.models.History

class HistoryAdapter : BaseBannerAdapter<History>() {
    override fun bindData(
        holder: BaseViewHolder<History>,
        history: History,
        position: Int,
        pageSize: Int
    ) {
        val context = holder.itemView.context
        val binding = ItemHistoryBinding.bind(holder.itemView)
        binding.run {
            if (history.attachedPhotoPath.isEmpty()) {
                context.changeDrawableIconColor(context.config.primaryColor, bannerImage)
            } else {
                bannerImage.clearColorFilter()
            }
            bannerImage.setRoundCorner(context.dpToPixel(8F))
            textDescription.typeface = FontUtils.getCommonTypeface(context)
            textDescription.text = history.date
            Glide.with(context)
                .load(history.attachedPhotoPath)
                .apply(RequestOptions()
                    .error(if (history.attachedPhotoPath.isEmpty()) R.drawable.ic_padlock else R.drawable.ic_error_7)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)
                )
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bannerImage.scaleType = ImageView.ScaleType.CENTER
                        context.changeDrawableIconColor(context.config.primaryColor, bannerImage)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        bannerImage.clearColorFilter()
                        return false
                    }
                })
                .into(bannerImage)
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_history
    }
}