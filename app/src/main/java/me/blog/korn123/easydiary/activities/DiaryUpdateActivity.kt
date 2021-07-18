package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.DIARY_ENCRYPT_PASSWORD
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils


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
                diaryDto.isAllDay = mBinding.partialEditToolbarSub.allDay.isChecked
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
        mBinding.partialEditToolbarSub.saveContents.setOnClickListener(mOnClickListener)
        mBinding.partialEditContents.partialEditPhotoContainer.photoView.setOnClickListener(mClickListener)
        mBinding.partialEditContents.partialEditPhotoContainer.captureCamera.setOnClickListener(mClickListener)
        mBinding.partialEditToolbarSub.datePicker.setOnClickListener(mClickListener)
        mBinding.partialEditToolbarSub.timePicker.setOnClickListener(mClickListener)
        mBinding.partialEditToolbarSub.secondsPicker.setOnClickListener(mClickListener)
        mBinding.partialEditToolbarSub.microphone.setOnClickListener(mClickListener)
        mBinding.partialEditContents.locationContainer.setOnClickListener(mClickListener)
        mBinding.partialEditContents.diaryTitle.setOnTouchListener(mTouchListener)
        mBinding.partialEditContents.diaryContents.setOnTouchListener(mTouchListener)

        mBinding.partialEditContents.partialBottomToolbar.togglePhoto.setOnClickListener {
            toggleSimpleLayout()
        }

        mBinding.partialEditToolbarSub.run {
            allDayContainer.setOnClickListener {
                allDay.isChecked = allDay.isChecked.not()
                toggleTimePickerTool()
            }
        }

        mBinding.partialEditContents.feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog(getString(R.string.diary_symbol_guide_message)) { symbolSequence ->
            selectFeelingSymbol(symbolSequence)
        }}
    }
}
