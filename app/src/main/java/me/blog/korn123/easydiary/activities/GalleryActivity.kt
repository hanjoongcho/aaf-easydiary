package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.ColorUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.databinding.ActivityGalleryBinding
import me.blog.korn123.easydiary.databinding.DialogSettingGalleryBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.openGridSettingDialog
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.GridItemDecoration
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import java.io.File

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
//        mBinding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setSupportActionBar(mBinding.toolbar)
        FontUtils.getTypeface(this, config.settingFontName)?.let {
            mBinding.toolbarLayout.setCollapsedTitleTypeface(it)
            mBinding.toolbarLayout.setExpandedTitleTypeface(it)
        }

//        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
//            flexWrap = FlexWrap.WRAP
//            flexDirection = FlexDirection.ROW
// //            alignItems = AlignItems.FLEX_START
//            justifyContent = JustifyContent.FLEX_START
//        }

        val spacesItemDecoration =
            GridItemDecoration(resources.getDimensionPixelSize(R.dimen.component_margin_small)) {
                if (isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait
            }
        mGridLayoutManager = GridLayoutManager(this, if (isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait)

        EasyDiaryUtils.initWorkingDirectory(this@GalleryActivity)
        mGalleryAdapter =
            GalleryAdapter(
                this@GalleryActivity,
                mAttachedPhotos,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@GalleryActivity, GalleryViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@GalleryActivity, intent)
                },
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
        mBinding.imgOpenGalleryOptions.setOnClickListener {
            var dialog: Dialog? = null
            val dialogSettingGalleryBinding =
                DialogSettingGalleryBinding.inflate(layoutInflater).apply {
                    closeBottomSheet.setOnClickListener { view -> view.postDelayed({ dialog?.dismiss() }, 200L) }
                    updateAppViews(root)
                    changeDrawableIconColor(config.textColor, imgDeleteUnlinkedPhoto)
                    FontUtils.setFontsTypeface(applicationContext, null, root, true)

                    val unlinkedPhotos = arrayListOf<File>()

                    fun updateInfo() {
                        val totalPhotos = File(EasyDiaryUtils.getApplicationDataDirectory(applicationContext) + DIARY_PHOTO_DIRECTORY).listFiles()
                        totalPhotos?.let {
                            unlinkedPhotos.clear()
                            unlinkedPhotos.addAll(totalPhotos.filter { file -> EasyDiaryDbHelper.findDiaryBy(file.name) == null })
                            textLinkedPhotoCount.text = (totalPhotos.size - unlinkedPhotos.size).toString()
                            textUnlinkedPhotoCount.text = "${unlinkedPhotos.size}"
                            textTotalPhotoCount.text = totalPhotos.size.toString()
                        }
                    }

                    fun reloadPhotos() {
                        val attachedPhotos = getAttachedPhotos(this@GalleryActivity)
                        mAttachedPhotos.clear()
                        attachedPhotos?.let { mAttachedPhotos.addAll(it) }
                        mGalleryAdapter.notifyDataSetChanged()
                    }

                    switchShowUnlinkedPhoto.isChecked = config.visibleUnlinkedPhotos
                    switchShowUnlinkedPhoto.setOnCheckedChangeListener { _, isChecked ->
                        config.visibleUnlinkedPhotos = isChecked
                        reloadPhotos()
                    }

                    imgDeleteUnlinkedPhoto.setOnClickListener {
                        showAlertDialog(
                            getString(R.string.delete_unlinked_photo_confirm_message),
                            { _, _ ->
                                mBinding.progressLoadingContainer.progressLoading.visibility =
                                    View.VISIBLE
                                lifecycleScope.launch(Dispatchers.IO) {
                                    unlinkedPhotos.forEach { item -> item.delete() }
                                    withContext(Dispatchers.Main) {
                                        updateInfo()
                                        reloadPhotos()
                                        mBinding.progressLoadingContainer.progressLoading.visibility =
                                            View.GONE
                                    }
                                }
                            },
                            { _, _ -> },
                            DialogMode.WARNING,
                            false,
                        )
                    }
                    updateInfo()
                }
            dialog =
                BottomSheetDialog(this).apply {
                    setContentView(dialogSettingGalleryBinding.root)
                    setCancelable(true)
                    setCanceledOnTouchOutside(true)
                    show()
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
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
            R.id.layout -> {
                openGridSettingDialog(mBinding.root, GridSpanMode.GALLERY) {
                    mGridLayoutManager.spanCount = it
                    mGalleryAdapter.notifyDataSetChanged()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPostCard() {
        lifecycleScope.launch(Dispatchers.IO) {
            val attachedPhotos = getAttachedPhotos(this@GalleryActivity)

            withContext(Dispatchers.Main) {
                mAttachedPhotos.clear()
                attachedPhotos?.let { mAttachedPhotos.addAll(it) }
                mGalleryAdapter.notifyDataSetChanged()
                if (mAttachedPhotos.isEmpty()) {
                    mBinding.infoMessage.visibility = View.VISIBLE
                    mBinding.contentPostCardViewer.root.visibility = View.GONE
                    mBinding.appBar.setExpanded(false)
                }
                mBinding.progressLoadingContainer.progressLoading.visibility = View.GONE
            }
        }
    }

    companion object {
        fun getAttachedPhotos(context: Context): List<GalleryAdapter.AttachedPhoto>? {
            val realm = EasyDiaryDbHelper.getTemporaryInstance()
            val listPostcard =
                File(EasyDiaryUtils.getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
                    .listFiles()
                    ?.map { file ->
                        val diary = EasyDiaryDbHelper.findDiaryBy(file.name, realm)
                        GalleryAdapter.AttachedPhoto(file, false, if (diary != null) realm.copyFromRealm(diary) else null)
                    }?.filter { attachedPhoto -> attachedPhoto.diary != null || context.config.visibleUnlinkedPhotos }
                    ?.sortedByDescending { item ->
                        item.diary?.currentTimeMillis ?: 0
                    }
            realm.close()
            return listPostcard
        }
    }
}
