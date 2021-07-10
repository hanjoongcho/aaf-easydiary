package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import com.werb.pickphotoview.util.PickConfig
import kotlinx.android.synthetic.main.partial_bottom_toolbar.*
import kotlinx.android.synthetic.main.partial_edit_photo_container.*
import kotlinx.android.synthetic.main.partial_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.extensions.pauseLock
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
    private val mOnClickListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()
        when (view.id) {
            R.id.saveContents -> if (StringUtils.isEmpty(mBinding.partialEditContents.diaryContents.text)) {
                mBinding.partialEditContents.diaryContents.requestFocus()
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
                val diaryDto = when (encryptionPass == null) {
                    true -> {
                        DiaryDto(
                                mSequence,
                                mCurrentTimeMillis,
                                mBinding.partialEditContents.diaryTitle.text.toString(),
                                mBinding.partialEditContents.diaryContents.text.toString()
                        )
                    }
                    false -> {
                        DiaryDto(
                                mSequence,
                                mCurrentTimeMillis,
                                JasyptUtils.encrypt(mBinding.partialEditContents.diaryTitle.text.toString(), encryptionPass),
                                JasyptUtils.encrypt(mBinding.partialEditContents.diaryContents.text.toString(), encryptionPass),
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
                EasyDiaryDbHelper.updateDiaryBy(diaryDto)
                TransitionHelper.finishActivityWithTransition(this)
                mIsDiarySaved = true
            }
        }
    }


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(mBinding.toolbar)
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
        setDateTime()
        bindEvent()
        savedInstanceState?.let { restoreContents(it) } ?: run { checkTemporaryDiary(mSequence) }
        initBottomToolbar()
        toggleSimpleLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && intent != null) {
                mBinding.partialEditContents.run {
                    intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                        if (mCurrentCursor == FOCUS_TITLE) { // edit title
                            val title = diaryTitle.text.toString()
                            val sb = StringBuilder(title)
                            sb.insert(diaryTitle.selectionStart, it[0])
                            val cursorPosition = diaryTitle.selectionStart + it[0].length
                            diaryTitle.setText(sb.toString())
                            diaryTitle.setSelection(cursorPosition)
                        } else {                   // edit contents
                            val contents = diaryContents.text.toString()
                            val sb = StringBuilder(contents)
                            sb.insert(diaryContents.selectionStart, it[0])
                            val cursorPosition = diaryContents.selectionStart + it[0].length
                            diaryContents.setText(sb.toString())
                            diaryContents.setSelection(cursorPosition)
                        }
                    }
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
            true -> mBinding.photoProgress.visibility = View.VISIBLE
            false -> mBinding.photoProgress.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (mIsDiarySaved) {
            EasyDiaryDbHelper.deleteTemporaryDiaryBy(mSequence)
        } else {
            saveTemporaryDiary(mSequence)
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initData() {
        val intent = intent
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.findDiaryBy(mSequence)!!
        initData(diaryDto)
    }

    private fun bindEvent() {
        saveContents.setOnClickListener(mOnClickListener)
        photoView.setOnClickListener(mClickListener)
        captureCamera.setOnClickListener(mClickListener)
        datePicker.setOnClickListener(mClickListener)
        timePicker.setOnClickListener(mClickListener)
        secondsPicker.setOnClickListener(mClickListener)
        microphone.setOnClickListener(mClickListener)
        mBinding.partialEditContents.locationContainer.setOnClickListener(mClickListener)
        mBinding.partialEditContents.diaryTitle.setOnTouchListener(mTouchListener)
        mBinding.partialEditContents.diaryContents.setOnTouchListener(mTouchListener)

        togglePhoto.setOnClickListener {
            toggleSimpleLayout()
        }

        allDayContainer.setOnClickListener {
            allDay.isChecked = allDay.isChecked.not()
            toggleTimePickerTool()
        }

        mBinding.partialEditContents.feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog(getString(R.string.diary_symbol_guide_message)) { symbolSequence ->
            selectFeelingSymbol(symbolSequence)
        }}
    }
}
