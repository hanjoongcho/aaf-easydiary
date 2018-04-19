package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.content.FileProvider
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import kotlinx.android.synthetic.main.activity_post_card.*
import me.blog.korn123.commons.utils.BitmapUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.*
import java.io.File

/**
 * Created by hanjoong on 2017-07-01.
 */

class PostCardActivity : EasyDiaryActivity() {
    lateinit var mShowcaseView: ShowcaseView
    lateinit var mSavedDiaryCardPath: String
    private var mSequence: Int = 0
    private var mBgColor = -0x1
    private var mTextColor = -0xb5b5b4
    private var showcaseIndex = 1

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_card)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)    
        }
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence)
        EasyDiaryUtils.initWeatherView(weather, diaryDto.weather)
        diaryTitle.text = diaryDto.title
        contents.text = diaryDto.contents
        date.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
        
        initShowcase()
    }

    override fun onResume() {
        super.onResume()
        setFontsStyle()
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                // 권한이 있는경우
                exportDiaryCard(true)
            } else {
                // 권한이 없는경우
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                // 권한이 있는경우
                exportDiaryCard(false)
            } else {
                // 권한이 없는경우
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_BACKGROUND_COLOR_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
                val hexStringColor = "#" + data.getStringExtra("color")
                contentsContainer.setBackgroundColor(Color.parseColor(hexStringColor))
            }
            REQUEST_CODE_TEXT_COLOR_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
                val hexStringColor = "#" + data.getStringExtra("color")
                diaryTitle.setTextColor(Color.parseColor(hexStringColor))
                date.setTextColor(Color.parseColor(hexStringColor))
                contents.setTextColor(Color.parseColor(hexStringColor))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_post_card, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bgColor -> ColorPickerDialogBuilder
                    .with(this@PostCardActivity)
                    //                        .setTitle("Choose Color")
                    .initialColor(mBgColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                        mBgColor = selectedColor
                        contentsContainer.setBackgroundColor(mBgColor)
                    }
                    .setNegativeButton("cancel") { dialog, which -> }
                    //						.showColorEdit(true)
                    //                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                    .build()
                    .show()
            R.id.textColor -> ColorPickerDialogBuilder
                    .with(this@PostCardActivity)
                    //                        .setTitle("Choose Color")
                    .initialColor(mTextColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok") { dialog, selectedColor, allColors ->
                        mTextColor = selectedColor
                        diaryTitle.setTextColor(mTextColor)
                        date.setTextColor(mTextColor)
                        contents.setTextColor(mTextColor)
                    }
                    .setNegativeButton("cancel") { dialog, which -> }
                    //						.showColorEdit(true)
                    //                        .setColorEditTextColor(ContextCompat.getColor(PostCardActivity.this, android.R.color.holo_blue_bright))
                    .build()
                    .show()
            R.id.save -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                exportDiaryCard(true)
            } else {
                // API Level 23 이상이면서 권한취득 안한경우
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
            }
            R.id.share -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                exportDiaryCard(false)
            } else {
                // API Level 23 이상이면서 권한취득 안한경우
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_SHARE_DIARY_CARD)
            }
        }
        return super.onOptionsItemSelected(item)
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
                    mShowcaseView.setTarget(ViewTarget(R.id.textColor, this@PostCardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_1))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_1))
                }
                2 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.bgColor, this@PostCardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_2))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_2))
                }
                3 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.save, this@PostCardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_3))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_3))
                }
                4 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setTarget(ViewTarget(R.id.share, this@PostCardActivity))
                    mShowcaseView.setContentTitle(getString(R.string.post_card_showcase_title_4))
                    mShowcaseView.setContentText(getString(R.string.post_card_showcase_message_4))
                }
                5 -> mShowcaseView.hide()
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
                .build()
        mShowcaseView.setButtonText(getString(R.string.post_card_showcase_button_1))
        mShowcaseView.setButtonPosition(centerParams)
    }

    private fun exportDiaryCard(showInfoDialog: Boolean) {
        // draw viewGroup on UI Thread
        val bitmap = BitmapUtils.diaryViewGroupToBitmap(postContainer)
        progressBar.visibility = View.VISIBLE

        // generate postcard file another thread
        Thread(Runnable {
            try {
                val diaryCardPath = WORKING_DIRECTORY + mSequence + "_" + DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".jpg"
                mSavedDiaryCardPath = Environment.getExternalStorageDirectory().absolutePath + diaryCardPath
                EasyDiaryUtils.initWorkingDirectory(Environment.getExternalStorageDirectory().absolutePath + USER_CUSTOM_FONTS_DIRECTORY)
                BitmapUtils.saveBitmapToFileCache(bitmap, mSavedDiaryCardPath)
                Handler(Looper.getMainLooper()).post {
                    progressBar.visibility = View.GONE
                    if (showInfoDialog) {
                        showAlertDialog(getString(R.string.diary_card_export_info), diaryCardPath, DialogInterface.OnClickListener { dialog, which -> })
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

    private fun shareDiary() {
        val file = File(mSavedDiaryCardPath)
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this@PostCardActivity, "$packageName.provider", file))
        shareIntent.type = "image/jpeg"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.diary_card_share_info)))
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<View>(android.R.id.content) as ViewGroup)
    }
}
