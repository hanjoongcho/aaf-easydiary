package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import com.simplemobiletools.commons.helpers.BaseConfig
import com.werb.pickphotoview.util.PickConfig
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_diary_update.*
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryUpdateActivity : EditActivity() {
    private var mSequence: Int = 0
    private var mWeather: Int = 0
    private var mCurrentCursor = 1

    private val mOnClickListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()

        when (view.id) {
            R.id.saveContents -> if (StringUtils.isEmpty(diaryContents.text)) {
                diaryContents.requestFocus()
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val diaryDto = DiaryDto(
                        mSequence,
                        mCurrentTimeMillis,
                        diaryTitle.text.toString(),
                        diaryContents.text.toString()
                )
                diaryDto.weather = mSelectedItemPosition
                diaryDto.isAllDay = allDay.isChecked
                applyRemoveIndex()
                diaryDto.photoUris = mPhotoUris
                EasyDiaryDbHelper.updateDiary(diaryDto)
                TransitionHelper.finishActivityWithTransition(this)
            }
        }
    }
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_update)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.update_diary_title)
            setDisplayHomeAsUpEnabled(true)
        }
        mCustomLineSpacing = false

        setupRecognizer()
//        setupSpinner()
        initData()
        initDateTime()
        setupDialog()
        setupPhotoView()
        initBottomToolbar()
        setDateTime()
        bindEvent()
        initBottomContainer()
        toggleSimpleLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pauseLock()
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (mCurrentCursor == 0) { // edit title
                    val title = diaryTitle.text.toString()
                    val sb = StringBuilder(title)
                    sb.insert(diaryTitle.selectionStart, result[0])
                    val cursorPosition = diaryTitle.selectionStart + result[0].length
                    diaryTitle.setText(sb.toString())
                    diaryTitle.setSelection(cursorPosition)
                } else {                   // edit contents
                    val contents = diaryContents.text.toString()
                    val sb = StringBuilder(contents)
                    sb.insert(diaryContents.selectionStart, result[0])
                    val cursorPosition = diaryContents.selectionStart + result[0].length
                    diaryContents.setText(sb.toString())
                    diaryContents.setSelection(cursorPosition)
                }
            }
            REQUEST_CODE_IMAGE_PICKER -> try {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (mPhotoUris == null) mPhotoUris = RealmList()
                    val photoPath = Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                    CommonUtils.uriToFile(this, data.data, photoPath)
                    mPhotoUris?.add(PhotoUriDto(FILE_URI_PREFIX + photoPath))
                    val thumbnailSize = config.settingThumbnailSize
                    val bitmap = BitmapUtils.decodeFile(photoPath, CommonUtils.dpToPixel(this, thumbnailSize - 5), CommonUtils.dpToPixel(this, thumbnailSize - 5))
                    val imageView = ImageView(this)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, thumbnailSize), CommonUtils.dpToPixel(this, thumbnailSize))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3F), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = resources.getDrawable(R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    val currentIndex = (mPhotoUris?.size ?: 0) - 1
                    imageView.setOnClickListener(PhotoClickListener(currentIndex))
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                    initBottomToolbar()
                    photoContainer.postDelayed({ (photoContainer.parent as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            PickConfig.PICK_PHOTO_DATA -> {
                data?.let {
                    val selectPaths = it.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT) as ArrayList<String>
                    attachPhotos(selectPaths)
                }
            }
        }
    }

    override fun setVisiblePhotoProgress(isVisible: Boolean) {
        when (isVisible) {
            true -> photoProgress.visibility = View.VISIBLE
            false -> photoProgress.visibility = View.GONE
        }
    }
    
//    private fun initSpinner() {
//        weatherSpinner.setSelection(mWeather)
//    }

    private fun initData() {
        val intent = intent
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence)
        mWeather = diaryDto.weather
        if (diaryDto.isAllDay) {
            allDay.isChecked = true
            toggleTimePickerTool()
        }

        diaryTitle.setText(diaryDto.title)
        //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        diaryContents.setText(diaryDto.contents)
        mCurrentTimeMillis = diaryDto.currentTimeMillis
        diaryContents.requestFocus()

        // TODO fixme elegance
        mPhotoUris = RealmList()
        diaryDto.photoUris?.let {
            mPhotoUris?.addAll(it)
        }

        mPhotoUris?.let {
            val thumbnailSize = config.settingThumbnailSize
            for ((index, dto) in it.withIndex()) {
                val bitmap = EasyDiaryUtils.photoUriToDownSamplingBitmap(this, dto, 0, thumbnailSize.toInt() - 5, thumbnailSize.toInt() - 5)
                val imageView = ImageView(this)
                val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, thumbnailSize), CommonUtils.dpToPixel(this, thumbnailSize))
                layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3F), 0)
                imageView.layoutParams = layoutParams
                val drawable = resources.getDrawable(R.drawable.bg_card_thumbnail)
                val gradient = drawable as GradientDrawable
                gradient.setColor(ColorUtils.setAlphaComponent(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                imageView.background = gradient
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER
                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
        }

//        initSpinner()
        selectFeelingSymbol(mWeather)
    }

    private fun bindEvent() {
        saveContents.setOnClickListener(mOnClickListener)
        photoView.setOnClickListener(mEditListener)
        datePicker.setOnClickListener(mEditListener)
        timePicker.setOnClickListener(mEditListener)
        secondsPicker.setOnClickListener(mEditListener)
        microphone.setOnClickListener(mEditListener)
        
        diaryTitle.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 0
            false
        }

        diaryContents.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 1
            diaryContents.requestFocus()
            false
        }

        bottomToolbar.setOnClickListener {
            toggleSimpleLayout()
        }

        allDay.setOnClickListener { _ ->
            toggleTimePickerTool()
        }

        feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog() }
    }

    private fun initDateTime() {
        mYear = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.YEAR_PATTERN))
        mMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.MONTH_PATTERN))
        mDayOfMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.DAY_PATTERN))
        mHourOfDay = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "HH"))
        mMinute = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "mm"))
        mSecond = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "ss"))
    }
}
