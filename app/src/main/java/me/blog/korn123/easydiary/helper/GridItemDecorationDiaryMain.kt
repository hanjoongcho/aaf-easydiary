package me.blog.korn123.easydiary.helper

import android.app.Activity
import android.graphics.Rect
import android.view.View
import me.blog.korn123.easydiary.extensions.diaryMainSpanCount
import me.blog.korn123.easydiary.extensions.dpToPixel

class GridItemDecorationDiaryMain(private val space: Int, private val activity: Activity) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val spanCount = activity.diaryMainSpanCount()
        val position = parent.getChildAdapterPosition(view)
        val isTopColumn = position < spanCount
        val isStartColumn = position % spanCount == 0
        outRect.top = if (isTopColumn) 0 else space
        outRect.left = if (isStartColumn) 0 else space
        parent.adapter?.run {
            outRect.bottom = if (position == itemCount.minus(1)) activity.dpToPixel(80F) else 0
        }
    }
}