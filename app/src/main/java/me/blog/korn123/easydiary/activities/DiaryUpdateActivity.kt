package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import com.werb.pickphotoview.util.PickConfig
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_diary_edit.*
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryUpdateActivity : EditActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private var mSequence: Int = 0
    private var mWeather: Int = 0

    private val mOnClickListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()

        when (view.id) {
            R.id.saveContents -> if (StringUtils.isEmpty(diaryContents.text)) {
                diaryContents.requestFocus()
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {

                val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
                val diaryDto = when (encryptionPass == null) {
                    true -> {
                        DiaryDto(
                                mSequence,
                                mCurrentTimeMillis,
                                diaryTitle.text.toString(),
                                diaryContents.text.toString()
                        )
                    }
                    false -> {
                        DiaryDto(
                                mSequence,
                                mCurrentTimeMillis,
                                JasyptUtils.encrypt(diaryTitle.text.toString(), encryptionPass),
                                JasyptUtils.encrypt(diaryContents.text.toString(), encryptionPass),
                                true,
                                JasyptUtils.sha256(encryptionPass)
                        )
                    }
                }

                if (mLocation != null) diaryDto.location = mLocation
                diaryDto.weather = mSelectedItemPosition
                diaryDto.isAllDay = allDay.isChecked
                applyRemoveIndex()
                diaryDto.photoUris = mPhotoUris
                EasyDiaryDbHelper.updateDiary(diaryDto)
                TransitionHelper.finishActivityWithTransition(this)
            }
        }
    }


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.update_diary_title)
            setDisplayHomeAsUpEnabled(true)
        }
//        mCustomLineSpacing = false

        addTextWatcher()
        setupRecognizer()
//        setupSpinner()
        initData()
        initDateTime()
        setupDialog()
        setupPhotoView()
        initBottomToolbar()
        setDateTime()
        bindEvent()
        toggleSimpleLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && intent != null) {
                val result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (mCurrentCursor == FOCUS_TITLE) { // edit title
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
            REQUEST_CODE_IMAGE_PICKER -> if (resultCode == Activity.RESULT_OK && intent != null) {
                attachPhotos(arrayListOf(intent.data.toString()), true)
            }
            PickConfig.PICK_PHOTO_DATA -> {
                intent?.let {
                    val selectedUriPaths = it.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT) as ArrayList<String>
                    attachPhotos(selectedUriPaths, true)
                }
            }
            REQUEST_CODE_CAPTURE_CAMERA -> if (resultCode == Activity.RESULT_OK) {
                attachPhotos(arrayListOf(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + CAPTURE_CAMERA_FILE_NAME), false)
            }
        }
    }

    override fun setVisiblePhotoProgress(isVisible: Boolean) {
        when (isVisible) {
            true -> photoProgress.visibility = View.VISIBLE
            false -> photoProgress.visibility = View.GONE
        }
    }
    

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initData() {
        val intent = intent
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence)
        mWeather = diaryDto.weather
        if (diaryDto.isAllDay) {
            allDay.isChecked = true
            toggleTimePickerTool()
        }

        val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
        when (encryptionPass == null) {
            true -> {
                diaryTitle.setText(diaryDto.title)
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                diaryContents.setText(diaryDto.contents)
            }
            false -> {
                diaryTitle.setText(JasyptUtils.decrypt(diaryDto.title ?: "", encryptionPass))
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                diaryContents.setText(JasyptUtils.decrypt(diaryDto.contents ?: "", encryptionPass))
            }
        }

        mCurrentTimeMillis = diaryDto.currentTimeMillis
        if (config.holdPositionEnterEditScreen) {
            Handler().post {
                contentsContainer.scrollY = intent.getIntExtra(DIARY_CONTENTS_SCROLL_Y, 0) - (feelingSymbolButton.parent.parent as ViewGroup).measuredHeight
            }
        } else {
            diaryContents.requestFocus()
        }

        // TODO fixme elegance
        mPhotoUris = RealmList()
        diaryDto.photoUris?.let {
            mPhotoUris.addAll(it)
        }

        mPhotoUris.let {
            val thumbnailSize = config.settingThumbnailSize
            it.forEachIndexed { index, photoUriDto ->
                val imageView = EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, index)
                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
        }

//        initSpinner()
        selectFeelingSymbol(mWeather)
        if (config.enableLocationInfo) {
//            locationLabel.setTextColor(config.textColor)
//            locationContainer.background = getLabelBackground()
            diaryDto.location?.let {
                locationContainer.visibility = View.VISIBLE
                locationLabel.text = it.address
                mLocation = it
            } ?: {
                setLocationInfo()
                mLocation?.let {
                    locationContainer.visibility = View.VISIBLE
                    locationLabel.text = it.address
                }
            } ()
        }
    }

    private fun bindEvent() {
        saveContents.setOnClickListener(mOnClickListener)
        photoView.setOnClickListener(mClickListener)
        captureCamera.setOnClickListener(mClickListener)
        datePicker.setOnClickListener(mClickListener)
        timePicker.setOnClickListener(mClickListener)
        secondsPicker.setOnClickListener(mClickListener)
        microphone.setOnClickListener(mClickListener)
        locationContainer.setOnClickListener(mClickListener)
        diaryTitle.setOnTouchListener(mTouchListener)
        diaryContents.setOnTouchListener(mTouchListener)

        togglePhoto.setOnClickListener {
            toggleSimpleLayout()
        }

        allDayContainer.setOnClickListener {
            allDay.isChecked = allDay.isChecked.not()
            toggleTimePickerTool()
        }

        feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog(getString(R.string.diary_symbol_guide_message)) { symbolSequence ->
            selectFeelingSymbol(symbolSequence)
        }}
    }
}
