package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.BitmapUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PhotoAdapter
import me.blog.korn123.easydiary.databinding.ActivityPostcardBinding
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.extensions.actionBarHeight
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.getUriForFile
import me.blog.korn123.easydiary.extensions.makeSnackBar
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
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER
import me.blog.korn123.easydiary.helper.TransitionHelper
import java.io.File

/**
 * Created by hanjoong on 2017-07-01.
 */

class PostcardActivity : EasyDiaryActivity() {
    lateinit var mBinding: ActivityPostcardBinding
    lateinit var mSavedDiaryCardPath: String
    lateinit var mPhotoAdapter: PhotoAdapter
    private var mSequence: Long = 0
    private var mBgColor = POSTCARD_BG_COLOR_VALUE
    private var mTextColor = POSTCARD_TEXT_COLOR_VALUE
    private var showcaseIndex = 1
    private var mAddFontSize = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPostcardBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            elevation = 0F
        }
        mSequence = intent.getLongExtra(DIARY_SEQUENCE, 0)

        mBinding.run {
            lifecycleScope.launch {
                val diaryDto = diaryViewModel.findDiaryById(mSequence)!!
                FlavorUtils.initWeatherView(this@PostcardActivity, weather, diaryDto.symbolSequence)
                when (diaryDto.title.isNullOrEmpty()) {
                    true -> diaryTitle.visibility = View.GONE
                    false -> diaryTitle.text = diaryDto.title
                }
//            contents.text = diaryDto.contents
                applyMarkDownPolicy(contents, diaryDto.contents!!)
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

                diaryDto.photoUris?.let {
                    if (it.isNotEmpty()) {
                        photoContainer.visibility = View.VISIBLE

                        val postCardPhotoItems = arrayListOf<PhotoAdapter.PostCardPhotoItem>()
                        it.forEachIndexed { index, photoUriDto ->
                            postCardPhotoItems.add(PhotoAdapter.PostCardPhotoItem(EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + photoUriDto.getFilePath(), index, 2, 0))
                        }
                        mPhotoAdapter =
                            PhotoAdapter(this@PostcardActivity, postCardPhotoItems) {
                                resizePhotoGrid()
                            }

                        photoGrid.run {
                            layoutManager =
                                FlexboxLayoutManager(this@PostcardActivity).apply {
                                    flexWrap = FlexWrap.WRAP
                                    flexDirection = FlexDirection.ROW
//                            alignItems = AlignItems.STRETCH
                                }
                            adapter = mPhotoAdapter
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
                            mAddFontSize = progress - 20
                            updateTextSize(postContainer, this@PostcardActivity, mAddFontSize)
//                toolbar.title = "$mAddFontSize"
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        }
                    },
                )
            }
        }
    }

    private fun resizePhotoGrid() {
        mBinding.photoGrid.run {
            when (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                true -> {
                    if (mPhotoAdapter.postCardPhotoItems.none { item -> item.forceSinglePhotoPosition }) {
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
        outState.putInt(POSTCARD_BG_COLOR, mBgColor)
        outState.putInt(POSTCARD_TEXT_COLOR, mTextColor)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        updateTextSize(mBinding.postContainer, this@PostcardActivity, mAddFontSize)
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

            else -> {
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_postcard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bgColor -> {
                ColorPickerDialog(this, mBgColor) { color ->
                    setBackgroundColor(color)
                }
            }

            R.id.textColor -> {
                ColorPickerDialog(this, mTextColor) { color ->
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
        mBgColor = selectedColor
        mBinding.run {
            postContainer.setBackgroundColor(mBgColor)
//            photoGridContainer.setBackgroundColor(mBgColor)
        }
    }

    private fun setTextColor(selectedColor: Int) {
        mTextColor = selectedColor
        mBinding.run {
            diaryTitle.setTextColor(mTextColor)
            date.setTextColor(mTextColor)
            contents.setTextColor(mTextColor)
        }
    }

    private fun setupShowcase() {
        val margin = ((resources.displayMetrics.density * 60) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)
    }

    private fun exportDiaryCard(showInfoDialog: Boolean) {
        // draw viewGroup on UI Thread
        mBinding.run {
//            val bitmap = when (photoContainer.visibility == View.VISIBLE) {
//                true -> diaryViewGroupToBitmap(postContainer, true)
//                false -> diaryViewGroupToBitmap(postContainer, false)
//            }
            progressBar.visibility = View.VISIBLE
            // generate postcard file another thread
            applicationScope.launch {
                try {
                    val diaryCardPath = "$DIARY_POSTCARD_DIRECTORY${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}_$mSequence.jpg"
                    mSavedDiaryCardPath = EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + diaryCardPath
                    EasyDiaryUtils.initWorkingDirectory(this@PostcardActivity)
                    BitmapUtils.saveBitmapToFileCache(createBitmap(), mSavedDiaryCardPath)
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (showInfoDialog) {
//                        showAlertDialog(getString(R.string.diary_card_export_info), diaryCardPath, DialogInterface.OnClickListener { dialog, which -> })
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
                        showAlertDialog(errorInfo, DialogInterface.OnClickListener { dialog, which -> })
                    }
                }
            }
        }
    }

    private fun shareDiary() {
        val file = File(mSavedDiaryCardPath)
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, getUriForFile(file))
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.diary_card_share_info)))
    }

    private fun createBitmap(): Bitmap {
        val scrollViewBitmap = Bitmap.createBitmap(mBinding.scrollPostcard.width, mBinding.scrollPostcard.getChildAt(0).height, Bitmap.Config.ARGB_8888)
        val scrollViewCanvas = Canvas(scrollViewBitmap)
        mBinding.scrollPostcard.draw(scrollViewCanvas)
        return scrollViewBitmap
    }

    private fun diaryViewGroupToBitmap(
        viewGroup: ViewGroup,
        mergeBitmap: Boolean,
    ): Bitmap {
        val gridView = viewGroup.getChildAt(0) as ViewGroup
        val scrollView = viewGroup.getChildAt(1) as ViewGroup
        val scrollViewBitmap = Bitmap.createBitmap(scrollView.width, scrollView.getChildAt(0).height, Bitmap.Config.ARGB_8888)
        val scrollViewCanvas = Canvas(scrollViewBitmap)
        scrollView.draw(scrollViewCanvas)

        return when (mergeBitmap) {
            true -> {
                val gridViewBitmap = Bitmap.createBitmap(gridView.width, gridView.height, Bitmap.Config.ARGB_8888)
                val gridViewCanvas = Canvas(gridViewBitmap)
                gridView.draw(gridViewCanvas)
                mergeBitmap(gridViewBitmap, scrollViewBitmap)
            }

            false -> {
                scrollViewBitmap
            }
        }
    }

    private fun mergeBitmap(
        first: Bitmap,
        second: Bitmap,
    ): Bitmap {
        val bitmap: Bitmap
        val canvas: Canvas
        if (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
            bitmap = Bitmap.createBitmap(second.width, first.height + second.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(first, Matrix(), null)
            canvas.drawBitmap(second, 0f, first.height.toFloat(), null)
        } else {
            bitmap = Bitmap.createBitmap(first.width + second.width, second.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(first, Matrix(), null)
            canvas.drawBitmap(second, first.width.toFloat(), 0f, null)
        }
        return bitmap
    }

    companion object {
        fun calcPhotoGridHeight(activity: Activity): Int {
            val point = activity.getDefaultDisplay()
            return point.y - activity.actionBarHeight() - activity.statusBarHeight() - activity.dpToPixel(30F, Calculation.CEIL)
        }
    }
}
