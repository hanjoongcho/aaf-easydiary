package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.GridLayoutManager
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PostcardAdapter
import me.blog.korn123.easydiary.databinding.ActivityPostcardViewerBinding
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.extensions.adjustAlpha
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.openGridSettingDialog
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.GridItemDecorationPostcardViewer
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File

class PostcardViewerActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityPostcardViewerBinding
    private lateinit var postcardAdapter: PostcardAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private val listPostcard: ArrayList<PostcardAdapter.PostCard> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostcardViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        FontUtils.getTypeface(this, config.settingFontName)?.let {
            binding.toolbarLayout.setCollapsedTitleTypeface(it)
            binding.toolbarLayout.setExpandedTitleTypeface(it)
        }

        val spacesItemDecoration = GridItemDecorationPostcardViewer(resources.getDimensionPixelSize(R.dimen.component_margin_small), this)
        gridLayoutManager = GridLayoutManager(this, if (isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait)

        EasyDiaryUtils.initWorkingDirectory(this@PostcardViewerActivity)
        postcardAdapter =
            PostcardAdapter(
                this@PostcardViewerActivity,
                listPostcard,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    val intent = Intent(this@PostcardViewerActivity, PostcardViewPagerActivity::class.java)
                    intent.putExtra(POSTCARD_SEQUENCE, position)
                    TransitionHelper.startActivityWithTransition(this@PostcardViewerActivity, intent)
                },
            )

        binding.contentPostCardViewer.root.apply {
            layoutManager = gridLayoutManager
            addItemDecoration(spacesItemDecoration)
            adapter = postcardAdapter
            setPopUpTypeface(FontUtils.getCommonTypeface(this@PostcardViewerActivity))
        }

        initPostCard()
        binding.toolbarImage.setColorFilter(config.primaryColor.adjustAlpha(0.5F))
        binding.deletePostCard.setOnClickListener {
            val selectedItems = arrayListOf<PostcardAdapter.PostCard>()
            listPostcard.forEachIndexed { _, item ->
                if (item.isItemChecked) selectedItems.add(item)
            }

            when (selectedItems.size) {
                0 -> {
                    showAlertDialog("No postcard selected.", null)
                }

                else -> {
                    showAlertDialog(
                        getString(R.string.delete_confirm),
                        DialogInterface.OnClickListener { _, _ ->
                            selectedItems.forEachIndexed { _, item ->
                                if (item.file.exists()) item.file.delete()
                            }
                            initPostCard()
                        },
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
            R.id.layout -> {
                openGridSettingDialog(binding.root, GridSpanMode.POSTCARD) {
                    gridLayoutManager.spanCount = it.toInt()
                    postcardAdapter.notifyDataSetChanged()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initPostCard() {
        val postcards =
            File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                ?.filter { it.extension.equals("jpg", true) }
                ?.sortedDescending()
                ?.map { file -> PostcardAdapter.PostCard(file, false) }
                ?: emptyList()

        listPostcard.clear()
        listPostcard.addAll(postcards)
        postcardAdapter.notifyDataSetChanged()
        if (listPostcard.isEmpty()) {
            binding.infoMessage.visibility = View.VISIBLE
            binding.contentPostCardViewer.root.visibility = View.GONE
            binding.appBar.setExpanded(false)
        }
    }
}
