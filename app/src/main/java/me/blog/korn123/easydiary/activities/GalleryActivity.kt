package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.Dialog
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.databinding.ActivityGalleryBinding
import me.blog.korn123.easydiary.databinding.DialogSettingGalleryBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.extensions.adjustAlpha
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.openGridSettingDialog
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.helper.GridItemDecoration
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File

/**
 * Created by CHO HANJOONG on 2018-05-18.
 */

class GalleryActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityGalleryBinding
    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val attachedPhotos: ArrayList<GalleryAdapter.AttachedPhoto> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        FontUtils.getTypeface(this, config.settingFontName)?.let {
            binding.toolbarLayout.setCollapsedTitleTypeface(it)
            binding.toolbarLayout.setExpandedTitleTypeface(it)
        }

        val spacesItemDecoration =
            GridItemDecoration(resources.getDimensionPixelSize(R.dimen.component_margin_small)) {
                if (isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait
            }
        gridLayoutManager =
            GridLayoutManager(
                this,
                if (isLandScape()) config.gallerySpanCountLandscape else config.gallerySpanCountPortrait,
            )

        EasyDiaryUtils.initWorkingDirectory(this@GalleryActivity)
        galleryAdapter =
            GalleryAdapter(
                this@GalleryActivity,
                attachedPhotos,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@GalleryActivity, GalleryViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@GalleryActivity, intent)
                },
            )

        binding.contentPostCardViewer.root.apply {
            layoutManager = gridLayoutManager
            addItemDecoration(spacesItemDecoration)
            adapter = galleryAdapter
            setPopUpTypeface(FontUtils.getCommonTypeface(this@GalleryActivity))
        }

        initPostCard()
        binding.toolbarImage.setColorFilter(config.primaryColor.adjustAlpha(0.5F))
        binding.imgOpenGalleryOptions.setOnClickListener {
            showGalleryOptionsDialog()
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
                openGridSettingDialog(binding.root, GridSpanMode.GALLERY) { spanCount ->
                    gridLayoutManager.spanCount = spanCount
                    galleryAdapter.notifyDataSetChanged()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPostCard() {
        lifecycleScope.launch {
            val fetchedPhotos =
                diaryViewModel.getAttachedPhotos(this@GalleryActivity)?.filter {
                    if (config.visibleUnlinkedPhotos) true else it.diary != null
                }

            withContext(Dispatchers.Main) {
                attachedPhotos.clear()
                fetchedPhotos?.let { attachedPhotos.addAll(it) }
                galleryAdapter.notifyDataSetChanged()
                if (attachedPhotos.isEmpty()) {
                    binding.infoMessage.visibility = View.VISIBLE
                    binding.contentPostCardViewer.root.visibility = View.GONE
                    binding.appBar.setExpanded(false)
                }
                binding.progressLoadingContainer.progressLoading.visibility = View.GONE
            }
        }
    }

    private fun showGalleryOptionsDialog() {
        var dialog: Dialog? = null
        val dialogSettingGalleryBinding =
            DialogSettingGalleryBinding.inflate(layoutInflater).apply {
                closeBottomSheet.setOnClickListener { view ->
                    view.postDelayed(
                        { dialog?.dismiss() },
                        200L,
                    )
                }
                updateAppViews(root)
                changeDrawableIconColor(config.textColor, imgDeleteUnlinkedPhoto)
                FontUtils.setFontsTypeface(applicationContext, null, root, true)

                val unlinkedPhotos = arrayListOf<File>()

                fun updateInfo() {
                    lifecycleScope.launch {
                        val photos = diaryViewModel.getAttachedPhotos(this@GalleryActivity)
                        photos?.let { allPhotos ->
                            unlinkedPhotos.clear()
                            val linkedCount = allPhotos.count { it.diary != null }
                            val unlinkedList = allPhotos.filter { it.diary == null }.map { it.file }
                            unlinkedPhotos.addAll(unlinkedList)

                            textLinkedPhotoCount.text = linkedCount.toString()
                            textUnlinkedPhotoCount.text = "${unlinkedPhotos.size}"
                            textTotalPhotoCount.text = allPhotos.size.toString()
                        }
                    }
                }

                fun reloadPhotos() {
                    lifecycleScope.launch {
                        val filteredPhotos =
                            diaryViewModel.getAttachedPhotos(this@GalleryActivity)?.filter {
                                if (config.visibleUnlinkedPhotos) true else it.diary != null
                            }
                        attachedPhotos.clear()
                        filteredPhotos?.let { attachedPhotos.addAll(it) }
                        galleryAdapter.notifyDataSetChanged()
                    }
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
                            binding.progressLoadingContainer.progressLoading.visibility = View.VISIBLE
                            lifecycleScope.launch(Dispatchers.IO) {
                                unlinkedPhotos.forEach { item -> item.delete() }
                                withContext(Dispatchers.Main) {
                                    updateInfo()
                                    reloadPhotos()
                                    binding.progressLoadingContainer.progressLoading.visibility = View.GONE
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
