package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.ColorUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.databinding.ActivityGalleryBinding
import me.blog.korn123.easydiary.databinding.ActivityPostcardViewerBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.openGridSettingDialog
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.GridItemDecorationPostcardViewer
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Comparator


/**
 * Created by CHO HANJOONG on 2018-05-18.
 */

class GalleryActivity : EasyDiaryActivity() {
    private lateinit var mBinding: ActivityGalleryBinding
    private lateinit var mGalleryAdapter: GalleryAdapter
    private lateinit var mGridLayoutManager: GridLayoutManager
    private var mAttachedPhotos: ArrayList<GalleryAdapter.AttachedPhoto> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityGalleryBinding.inflate(layoutInflater)
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
        
        val spacesItemDecoration = GridItemDecorationPostcardViewer(resources.getDimensionPixelSize(R.dimen.component_margin_small), this)
        mGridLayoutManager = GridLayoutManager(this, if (isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait)

        EasyDiaryUtils.initWorkingDirectory(this@GalleryActivity)
        mGalleryAdapter = GalleryAdapter(
                this@GalleryActivity,
                mAttachedPhotos,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@GalleryActivity, PostcardViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@GalleryActivity, intent)
                }
        )

        mBinding.contentPostCardViewer.root.apply {
            layoutManager = mGridLayoutManager
            addItemDecoration(spacesItemDecoration)
            adapter = mGalleryAdapter
//            setHasFixedSize(true)
//            clipToPadding = false
            setPopUpTypeface(FontUtils.getCommonTypeface(this@GalleryActivity))
        }

        initPostCard()
        mBinding.toolbarImage.setColorFilter(ColorUtils.adjustAlpha(config.primaryColor, 0.5F))
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
                mGalleryAdapter.notifyDataSetChanged()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initPostCard() {
        CoroutineScope(Dispatchers.IO).launch {
            val realm = EasyDiaryDbHelper.getTemporaryInstance()
            val listPostcard = File(EasyDiaryUtils.getApplicationDataDirectory(this@GalleryActivity) + DIARY_PHOTO_DIRECTORY)
                    .listFiles()
                    .map { file -> GalleryAdapter.AttachedPhoto(file, false, when (val diary = EasyDiaryDbHelper.findDiaryBy(FILE_URI_PREFIX + EasyDiaryUtils.getApplicationDataDirectory(this@GalleryActivity) + DIARY_PHOTO_DIRECTORY + file.name, realm)) {
                        null -> 0
                        else -> diary.currentTimeMillis
                    }) }.sortedBy { item -> item.currentTimeMillis }
            realm.close()
            withContext(Dispatchers.Main) {
                mAttachedPhotos.clear()
                mAttachedPhotos.addAll(listPostcard)
                mGalleryAdapter.notifyDataSetChanged()
                if (mAttachedPhotos.isEmpty()) {
                    mBinding.infoMessage.visibility = View.VISIBLE
                    mBinding.contentPostCardViewer.root.visibility = View.GONE
                    mBinding.appBar.setExpanded(false)
                }
            }
        }
    }
}