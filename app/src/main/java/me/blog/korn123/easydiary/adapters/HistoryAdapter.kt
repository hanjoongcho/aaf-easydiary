package me.blog.korn123.easydiary.adapters

import android.util.TypedValue
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import io.github.aafactory.commons.extensions.dpToPixel
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ItemHistoryBinding
import me.blog.korn123.easydiary.extensions.config
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
            bannerImage.setRoundCorner(context.dpToPixel(8F))
            textDescription.typeface = FontUtils.getCommonTypeface(context)
            textDescription.text = history.title
            textDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.config.settingFontSize)
            val options = RequestOptions()
                .error(R.drawable.ic_error_7)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
            Glide.with(context)
                .load(history.attachedPhotoPath)
                .apply(options)
                .into(bannerImage)
        }
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_history
    }
}