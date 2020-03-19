package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import io.github.aafactory.commons.utils.ColorUtils
import kotlinx.android.synthetic.main.activity_post_card_viewer.*
import kotlinx.android.synthetic.main.content_post_card_viewer.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import org.apache.commons.io.FileUtils
import java.io.File


/**
 * Created by CHO HANJOONG on 2018-05-18.
 */

class PostCardViewerActivity : EasyDiaryActivity() {
    private var mListPostcard: ArrayList<PostCardViewerActivity.PostCard> = arrayListOf()
    private lateinit var mPostcardAdapter: PostcardAdapter 
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_card_viewer)
        toolbar.setNavigationOnClickListener { onBackPressed() }
//        setSupportActionBar(toolbar)
        FontUtils.getTypeface(this, assets, config.settingFontName)?.let {
            toolbar_layout.setCollapsedTitleTypeface(it)
            toolbar_layout.setExpandedTitleTypeface(it)
        }

//        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
//            flexWrap = FlexWrap.WRAP
//            flexDirection = FlexDirection.ROW
////            alignItems = AlignItems.FLEX_START
//            justifyContent = JustifyContent.FLEX_START 
//        }
        
        val spacesItemDecoration = SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding))
        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)

        EasyDiaryUtils.initWorkingDirectory(this@PostCardViewerActivity)
        mPostcardAdapter = PostcardAdapter(
                this@PostCardViewerActivity,
                mListPostcard,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@PostCardViewerActivity, PostcardViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@PostCardViewerActivity, intent)
                }
        )
        
        recyclerView.apply {
            layoutManager = gridLayoutManager
            addItemDecoration(spacesItemDecoration)
            adapter = mPostcardAdapter
//            setHasFixedSize(true)
//            clipToPadding = false
        }

        initPostCard()
        toolbarImage.setColorFilter(ColorUtils.adjustAlpha(config.primaryColor, 0.5F))
        deletePostCard.setOnClickListener {
            val selectedItems = arrayListOf<PostCardViewerActivity.PostCard>()
            mListPostcard.forEachIndexed { _, item ->
                if (item.isItemChecked) selectedItems.add(item)
            }
            
            when (selectedItems.size) {
                0 -> showAlertDialog("No post card selected.", null)
                else -> {
                    showAlertDialog(getString(R.string.delete_confirm),
                            DialogInterface.OnClickListener { _, _ ->
                                selectedItems.forEachIndexed { _, item ->
                                    FileUtils.forceDelete(item.file)
                                }
                                initPostCard()
                            }
                    )
                }
            }
        }
    }

    private fun initPostCard() {
        val listPostcard = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                .filter { it.extension.equals("jpg", true)}
                .sortedDescending()
                .map { file -> PostCard(file, false) }

        mListPostcard.clear()
        mListPostcard.addAll(listPostcard)
        mPostcardAdapter.notifyDataSetChanged()
        if (mListPostcard.isEmpty()) {
            infoMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            app_bar.setExpanded(false)
        }
    }
    
    internal class SpacesItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            when (position % 2) {
                0 -> {
                    outRect.right = space
                }
                else -> outRect.right = 0
            }
            
            when (position < 2) {
                true -> outRect.top = 0 
                false -> outRect.top = space 
            }
        }
    }
    
    data class PostCard(val file: File, var isItemChecked: Boolean)
}