package me.blog.korn123.easydiary.adapters

import com.zhpan.bannerview.BaseBannerAdapter
import com.zhpan.bannerview.BaseViewHolder
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.PhotoUri

class HistoryAdapter : BaseBannerAdapter<PhotoUri>() {
    override fun bindData(
        holder: BaseViewHolder<PhotoUri>?,
        data: PhotoUri?,
        position: Int,
        pageSize: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.item_history
    }
}