package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PhotoAdapter
import me.blog.korn123.easydiary.databinding.ActivityPostcardBinding
import me.blog.korn123.easydiary.extensions.actionBarHeight
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.getUriForFile
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.saveBitmapToFileCache
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.statusBarHeight
import me.blog.korn123.easydiary.extensions.updateTextSize
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.POSTCARD_BG_COLOR
import me.blog.korn123.easydiary.helper.POSTCARD_BG_COLOR_VALUE
import me.blog.korn123.easydiary.helper.POSTCARD_TEXT_COLOR
import me.blog.korn123.easydiary.helper.POSTCARD_TEXT_COLOR_VALUE
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.io.File

class PostcardActivity : EasyDiaryActivity() {
    private lateinit var binding: ActivityPostcardBinding
    private lateinit var savedDiaryCardPath: String
    private lateinit var photoAdapter: PhotoAdapter
    private var sequence: Long = 0
    private var bgColor = POSTCARD_BG_COLOR_VALUE
    private var textColor = POSTCARD_TEXT_COLOR_VALUE
    private var addFontSize = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            elevation = 0F
        }
        sequence = intent.getLongExtra(DIARY_SEQUENCE, 0)

        binding.run {
            lifecycleScope.launch {
                val diaryDto = diaryViewModel.findDiaryById(sequence) ?: return@launch
                FlavorUtils.initWeatherView(this@PostcardActivity, weather, diaryDto.symbolSequence)
                when (diaryDto.title.isNullOrEmpty()) {
                    true -> diaryTitle.visibility = View.GONE
                    false -> diaryTitle.text = diaryDto.title
                }
                applyMarkDownPolicy(contents, diaryDto.contents)
                date.text =
                    when (diaryDto.isAllDay) {
                        true -> DateUtils.getDateStringFromTimeMillis(diaryDto.currentTimeMillis)
                        false -> DateUtils.getDateTimeStringForceFormatting(diaryDto.currentTimeMillis, this@PostcardActivity)
                    }
                EasyDiaryUtils.boldString(applicationContext, diaryTitle)

                setupShowcase()
                savedInstanceState?.let {
                    setBackgroundColor(it.getInt(POSTCARD_BG_COLOR, POSTCARD_BG_COLOR_VALUE))
                    setTextColor(it.getInt(POSTCARD_TEXT_COLOR, POSTCARD_TEXT_COLOR_VALUE))
                }

                diaryDto.photoUris?.let { uris ->
                    if (uris.isNotEmpty()) {
                        photoContainer.visibility = View.VISIBLE

                        val postCardPhotoItems = arrayListOf<PhotoAdapter.PostCardPhotoItem>()
                        uris.forEachIndexed { index, photoUriDto ->
                            postCardPhotoItems.add(PhotoAdapter.PostCardPhotoItem(EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + photoUriDto.getFilePath(), index, 2, 0))
                        }
                        photoAdapter =
                            PhotoAdapter(this@PostcardActivity, postCardPhotoItems) {
                                resizePhotoGrid()
                            }

                        photoGrid.run {
                            layoutManager =
                                FlexboxLayoutManager(this@PostcardActivity).apply {
                                    flexWrap = FlexWrap.WRAP
                                    flexDirection = FlexDirection.ROW
                                }
                            adapter = photoAdapter
                        }
                        resizePhotoGrid()
                    }
                }

                fontSizeSeekBar?.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            addFontSize = progress - 20
                            updateTextSize(postContainer, this@PostcardActivity, addFontSize)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    },
                )
            }
        }
    }

    private fun resizePhotoGrid() {
        binding.photoGrid.run {
            when (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                true -> {
                    if (photoAdapter.postCardPhotoItems.none { item -> item.forceSinglePhotoPosition }) {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    } else {
                        layoutParams.height = getDefaultDisplay().x
                    }
                }

                false -> {
                    val height = calcPhotoGridHeight(this@PostcardActivity)
                    layoutParams.width = height
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(POSTCARD_BG_COLOR, bgColor)
        outState.putInt(POSTCARD_TEXT_COLOR, textColor)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        updateTextSize(binding.postContainer, this@PostcardActivity, addFontSize)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> {
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportDiaryCard(true)
                } else {
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
                }
            }

            REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD -> {
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportDiaryCard(false)
                } else {
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
                }
            }

            else -> {}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_postcard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bgColor -> {
                ColorPickerDialog(this, bgColor) { color ->
                    setBackgroundColor(color)
                }
            }

            R.id.textColor -> {
                ColorPickerDialog(this, textColor) { color ->
                    setTextColor(color)
                }
            }

            R.id.save -> {
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportDiaryCard(true)
                } else {
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                }
            }

            R.id.share -> {
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportDiaryCard(false)
                } else {
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setBackgroundColor(selectedColor: Int) {
        bgColor = selectedColor
        binding.run {
            postContainer.setBackgroundColor(bgColor)
        }
    }

    private fun setTextColor(selectedColor: Int) {
        textColor = selectedColor
        binding.run {
            diaryTitle.setTextColor(textColor)
            date.setTextColor(textColor)
            contents.setTextColor(textColor)
        }
    }

    private fun setupShowcase() {
        // Showcase setup helper
    }

    private fun exportDiaryCard(showInfoDialog: Boolean) {
        binding.run {
            progressBar.visibility = View.VISIBLE
            applicationScope.launch {
                try {
                    val diaryCardPath = "$DIARY_POSTCARD_DIRECTORY${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}_$sequence.jpg"
                    savedDiaryCardPath = EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + diaryCardPath
                    EasyDiaryUtils.initWorkingDirectory(this@PostcardActivity)
                    createBitmap().saveBitmapToFileCache(savedDiaryCardPath)
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (showInfoDialog) {
                            val postCardViewer = Intent(this@PostcardActivity, PostcardViewerActivity::class.java)
                            TransitionHelper.startActivityWithTransition(this@PostcardActivity, postCardViewer)
                        } else {
                            shareDiary()
                        }
                    }
                } catch (e: Exception) {
                    val errorMessage = e.message
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        val errorInfo = String.format("%s\n\n[ERROR: %s]", getString(R.string.diary_card_export_error_message), errorMessage)
                        showAlertDialog(errorInfo, DialogInterface.OnClickListener { _, _ -> })
                    }
                }
            }
        }
    }

    private fun shareDiary() {
        val file = File(savedDiaryCardPath)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, getUriForFile(file))
            type = "image/jpeg"
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.diary_card_share_info)))
    }

    private fun createBitmap(): Bitmap {
        val scrollViewBitmap = Bitmap.createBitmap(binding.scrollPostcard.width, binding.scrollPostcard.getChildAt(0).height, Bitmap.Config.ARGB_8888)
        val scrollViewCanvas = Canvas(scrollViewBitmap)
        binding.scrollPostcard.draw(scrollViewCanvas)
        return scrollViewBitmap
    }

    companion object {
        fun calcPhotoGridHeight(activity: Activity): Int {
            val point = activity.getDefaultDisplay()
            return point.y - activity.actionBarHeight() - activity.statusBarHeight() - activity.dpToPixel(30F, me.blog.korn123.easydiary.enums.Calculation.CEIL)
        }
    }
}
