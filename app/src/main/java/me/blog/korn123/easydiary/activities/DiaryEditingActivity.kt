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
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.lang3.StringUtils


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryEditingActivity : BaseDiaryEditingActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private var mSequence: Int = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
//            title = getString(R.string.update_diary_title)
            setDisplayShowTitleEnabled(false)
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
        mSymbolSequence = diaryDto.weather
        initData(diaryDto)
    }

    override fun saveContents() {
        if (isExistEasterEggDiary(mSelectedItemPosition)) {
            duplicatedEasterEggWarning()
        } else {
            hideSoftInputFromWindow()
            if (StringUtils.isEmpty(mBinding.partialEditContents.diaryContents.text)) {
                mBinding.partialEditContents.diaryContents.requestFocus()
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
                val diaryDto = when (encryptionPass == null) {
                    true -> {
                        Diary(
                            mSequence,
                            mCurrentTimeMillis,
                            mBinding.partialEditContents.diaryTitle.text.toString(),
                            mBinding.partialEditContents.diaryContents.text.toString()
                        )
                    }
                    false -> {
                        Diary(
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
                diaryDto.isAllDay = mBinding.partialEditContents.allDay.isChecked
                applyRemoveIndex()
                diaryDto.photoUris = mPhotoUris
                EasyDiaryDbHelper.updateDiaryBy(diaryDto)
                TransitionHelper.finishActivityWithTransition(this)
                mIsDiarySaved = true
            }
        }
    }

    private fun bindEvent() {
        mBinding.partialEditContents.partialEditPhotoContainer.photoView.setOnClickListener(mClickListener)
        mBinding.partialEditContents.partialEditPhotoContainer.captureCamera.setOnClickListener(mClickListener)
        mBinding.partialEditContents.locationContainer.setOnClickListener(mClickListener)
        mBinding.partialEditContents.diaryTitle.setOnTouchListener(mTouchListener)
        mBinding.partialEditContents.diaryContents.setOnTouchListener(mTouchListener)

        mBinding.partialEditContents.partialBottomToolbar.togglePhoto.setOnClickListener {
            toggleSimpleLayout()
        }

        mBinding.partialEditContents.run {
            allDayContainer.setOnClickListener {
                allDay.isChecked = allDay.isChecked.not()
                toggleTimePickerTool()
            }
        }

        mBinding.partialEditContents.feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog(getString(R.string.diary_symbol_guide_message), mSelectedItemPosition) { symbolSequence ->
            selectFeelingSymbol(symbolSequence)
        }}
    }
}
