package me.blog.korn123.easydiary.activities

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_dev.*
import me.blog.korn123.easydiary.R


class DevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initDevUI() { }
}

class SpacesItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        when (position == 0) {
            true -> outRect.top = 0
            false -> outRect.top = space
        }
    }
}


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/





