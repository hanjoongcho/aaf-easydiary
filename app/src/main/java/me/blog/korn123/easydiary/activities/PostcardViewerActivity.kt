package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import io.github.aafactory.commons.utils.ColorUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.databinding.ActivityPostcardViewerBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


/**
 * Created by CHO HANJOONG on 2018-05-18.
 */

class PostcardViewerActivity : EasyDiaryActivity() {
    private lateinit var mBinding: ActivityPostcardViewerBinding
    private lateinit var mPostcardAdapter: PostcardAdapter
    private lateinit var mGridLayoutManager: GridLayoutManager
    private var mListPostcard: ArrayList<PostcardAdapter.PostCard> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPostcardViewerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setSupportActionBar(mBinding.toolbar)
        FontUtils.getTypeface(this, config.settingFontName)?.let {
            mBinding.toolbarLayout.setCollapsedTitleTypeface(it)
            mBinding.toolbarLayout.setExpandedTitleTypeface(it)
        }

//        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
//            flexWrap = FlexWrap.WRAP
//            flexDirection = FlexDirection.ROW
////            alignItems = AlignItems.FLEX_START
//            justifyContent = JustifyContent.FLEX_START 
//        }
        
        val spacesItemDecoration = SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding))
        mGridLayoutManager = GridLayoutManager(this, if (isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait)

        EasyDiaryUtils.initWorkingDirectory(this@PostcardViewerActivity)
        mPostcardAdapter = PostcardAdapter(
                this@PostcardViewerActivity,
                mListPostcard,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@PostcardViewerActivity, PostcardViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@PostcardViewerActivity, intent)
                }
        )

        mBinding.contentPostCardViewer.root.apply {
            layoutManager = mGridLayoutManager
            addItemDecoration(spacesItemDecoration)
            adapter = mPostcardAdapter
//            setHasFixedSize(true)
//            clipToPadding = false
            setPopUpTypeface(FontUtils.getCommonTypeface(this@PostcardViewerActivity))
        }

        initPostCard()
        mBinding.toolbarImage.setColorFilter(ColorUtils.adjustAlpha(config.primaryColor, 0.5F))
        mBinding.deletePostCard.setOnClickListener {
            val selectedItems = arrayListOf<PostcardAdapter.PostCard>()
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

    override fun onResume() {
        super.onResume()
        supportActionBar?.setBackgroundDrawable(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_postcard_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.layout -> openGridSettingDialog(mBinding.root, 0) {
                mGridLayoutManager.spanCount = it.toInt()
                mPostcardAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initPostCard() {
        val listPostcard = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                .filter { it.extension.equals("jpg", true)}
                .sortedDescending()
                .map { file -> PostcardAdapter.PostCard(file, false) }

        mListPostcard.clear()
        mListPostcard.addAll(listPostcard)
        mPostcardAdapter.notifyDataSetChanged()
        if (mListPostcard.isEmpty()) {
            mBinding.infoMessage.visibility = View.VISIBLE
            mBinding.contentPostCardViewer.root.visibility = View.GONE
            mBinding.appBar.setExpanded(false)
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
}