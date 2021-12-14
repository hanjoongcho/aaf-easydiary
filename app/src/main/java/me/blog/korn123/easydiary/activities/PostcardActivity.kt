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
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PhotoAdapter
import me.blog.korn123.easydiary.databinding.ActivityPostcardBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import java.io.File

/**
 * Created by hanjoong on 2017-07-01.
 */

class
PostcardActivity : EasyDiaryActivity() {
    lateinit var mBinding: ActivityPostcardBinding
    lateinit var mShowcaseView: ShowcaseView
    lateinit var mSavedDiaryCardPath: String
    lateinit var mPhotoAdapter: PhotoAdapter
    private var mSequence: Int = 0
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
        }
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)

        mBinding.run {
            val diaryDto = EasyDiaryDbHelper.findDiaryBy(mSequence)!!
            FlavorUtils.initWeatherView(this@PostcardActivity, weather, diaryDto.weather)
            when (diaryDto.title.isNullOrEmpty()) {
                true -> diaryTitle.visibility = View.GONE
                false -> diaryTitle.text = diaryDto.title
            }
            contents.text = diaryDto.contents
            date.text = when (diaryDto.isAllDay) {
                true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
            }
            EasyDiaryUtils.boldString(applicationContext, diaryTitle)

            initShowcase()
            savedInstanceState?.let {
                setBackgroundColor(it.getInt(POSTCARD_BG_COLOR, POSTCARD_BG_COLOR_VALUE))
                setTextColor(it.getInt(POSTCARD_TEXT_COLOR, POSTCARD_TEXT_COLOR_VALUE))
            }

            diaryDto.photoUris?.let {
                if (/*resources.configuration.orientation == ORIENTATION_PORTRAIT && */it.size > 0) {
                    photoContainer.visibility = View.VISIBLE

                    val postCardPhotoItems = arrayListOf<PhotoAdapter.PostCardPhotoItem>()
                    it.forEachIndexed { index, photoUriDto ->
                        postCardPhotoItems.add(PhotoAdapter.PostCardPhotoItem(EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + photoUriDto.getFilePath(), index, 2, 0))
                    }
                    mPhotoAdapter = PhotoAdapter(this@PostcardActivity, postCardPhotoItems) {
                        resizePhotoGrid()
                    }

                    photoGrid.run {
                        layoutManager = FlexboxLayoutManager(this@PostcardActivity).apply {
                            flexWrap = FlexWrap.WRAP
                            flexDirection = FlexDirection.ROW
//                            alignItems = AlignItems.STRETCH
                        }
                        adapter = mPhotoAdapter
                    }
                    resizePhotoGrid()
                }
            }

            fontSizeSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    mAddFontSize = progress - 20
                    updateTextSize(postContainer, this@PostcardActivity, mAddFontSize)
//                toolbar.title = "$mAddFontSize"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    private fun resizePhotoGrid() {
        mBinding.photoGrid.run {
            when (resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                true -> {
                    if (mPhotoAdapter.postCardPhotoItems.none { item -> item.forceSinglePhotoPosition }) {
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    } else {
                        layoutParams.height = CommonUtils.getDefaultDisplay(this@PostcardActivity).x
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                exportDiaryCard(true)
            } else {
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                exportDiaryCard(false)
            } else {
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
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
            R.id.bgColor -> ColorPickerDialogBuilder
                    .with(this@PostcardActivity)
                    //                        .setTitle("Choose Color")
                    .initialColor(mBgColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                        setBackgroundColor(selectedColor)
                    }
                    .setNegativeButton("cancel") { dialog, which -> }
                    //						.showColorEdit(true)
                    //                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                    .build()
                    .show()
            R.id.textColor -> ColorPickerDialogBuilder
                    .with(this@PostcardActivity)
                    //                        .setTitle("Choose Color")
                    .initialColor(mTextColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                        setTextColor(selectedColor)
                    }
                    .setNegativeButton("cancel") { dialog, which -> }
                    //						.showColorEdit(true)
                    //                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                    .build()
                    .show()
            R.id.save -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                exportDiaryCard(true)
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
            }
            R.id.share -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                exportDiaryCard(false)
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun setBackgroundColor(selectedColor: Int) {
        mBgColor = selectedColor
        mBinding.run {
            contentsContainer.setBackgroundColor(mBgColor)
            photoGridContainer.setBackgroundColor(mBgColor)
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
    
    private fun initShowcase() {
        val margin = ((resources.displayMetrics.density * 12) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)

        val showcaseViewOnClickListener = View.OnClickListener {
            when (showcaseIndex) {
                1 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.textColor, this@PostcardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_1))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_1))
                }
                2 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.bgColor, this@PostcardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_2))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_2))
                }
                3 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.save, this@PostcardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_3))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_3))
                    mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_2))
                }
                4 -> mShowcaseView.hide()
            }
            showcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setContentTitle(getString(R.string.post_card_showcase_title_0))
                .setContentText(getString(R.string.post_card_showcase_message_0))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(SHOWCASE_SINGLE_SHOT_POST_CARD_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .blockAllTouches()
                .build()
        mShowcaseView.setButtonText(getString(R.string.post_card_showcase_button_1))
        mShowcaseView.setButtonPosition(centerParams)
    }

    private fun exportDiaryCard(showInfoDialog: Boolean) {
        // draw viewGroup on UI Thread
        mBinding.run {
            val bitmap = when (photoContainer.visibility == View.VISIBLE) {
                true -> diaryViewGroupToBitmap(postContainer, true)
                false -> diaryViewGroupToBitmap(postContainer, false)
            }
            progressBar.visibility = View.VISIBLE
            // generate postcard file another thread
            Thread(Runnable {
                try {
                    val diaryCardPath = "$DIARY_POSTCARD_DIRECTORY${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER)}_$mSequence.jpg"
                    mSavedDiaryCardPath = EasyDiaryUtils.getApplicationDataDirectory(this@PostcardActivity) + diaryCardPath
                    EasyDiaryUtils.initWorkingDirectory(this@PostcardActivity)
                    BitmapUtils.saveBitmapToFileCache(bitmap, mSavedDiaryCardPath)
                    Handler(Looper.getMainLooper()).post {
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
                    e.printStackTrace()
                    val errorMessage = e.message
                    Handler(Looper.getMainLooper()).post {
                        progressBar.visibility = View.GONE
                        val errorInfo = String.format("%s\n\n[ERROR: %s]", getString(R.string.diary_card_export_error_message), errorMessage)
                        showAlertDialog(errorInfo, DialogInterface.OnClickListener { dialog, which -> })
                    }
                }
            }).start()
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

    private fun diaryViewGroupToBitmap(viewGroup: ViewGroup, mergeBitmap: Boolean): Bitmap {
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

    private fun mergeBitmap(first: Bitmap, second: Bitmap): Bitmap {
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
            val point =  CommonUtils.getDefaultDisplay(activity)
            return point.y - activity.actionBarHeight() - activity.statusBarHeight() - CommonUtils.dpToPixel(activity, 30F, CALCULATION.CEIL)
        }
    }
}
