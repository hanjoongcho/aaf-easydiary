package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_post_card_viewer.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.extensions.config

/**
 * Created by CHO HANJOONG on 2018-05-18.
 */

class PostCardViewerActivity : EasyDiaryActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_card_viewer)
        toolbar.setNavigationOnClickListener { onBackPressed() }
//        setSupportActionBar(toolbar)
        FontUtils.getTypeface(this, assets, config.settingFontName)?.let {
            toolbar_layout.setCollapsedTitleTypeface(it)
            toolbar_layout.setExpandedTitleTypeface(it)
        }

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            alignItems = AlignItems.CENTER
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        recyclerView.apply {
            layoutManager = flexboxLayoutManager
            adapter = PostcardAdapter(this@PostCardViewerActivity)
        }
    }
}