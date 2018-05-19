package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.activity_post_card_viewer.*
import kotlinx.android.synthetic.main.content_post_card_viewer.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import java.io.File

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

        val listPostcard = File(Environment.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY)
                .listFiles()
                .filter { it.extension.equals("jpg", true)}
                .sortedDescending()
        recyclerview.apply {
            layoutManager = flexboxLayoutManager
            adapter = PostcardAdapter(
                    this@PostCardViewerActivity,
                    listPostcard,
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        val intent = Intent(this@PostCardViewerActivity, PostcardViewPagerActivity::class.java)
                        intent.putExtra(POSTCARD_SEQUENCE, position)
                        TransitionHelper.startActivityWithTransition(this@PostCardViewerActivity, intent)
                    }
            )
        }
        if (listPostcard.isEmpty()) {
            infoMessage.visibility = View.VISIBLE
            recyclerViewHolder.visibility = View.GONE
            app_bar.setExpanded(false)
        }
    }
}