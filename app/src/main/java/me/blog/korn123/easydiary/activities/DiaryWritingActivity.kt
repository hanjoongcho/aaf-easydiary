package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.isAccessFromOutside
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.extensions.startMainActivityWithClearTask
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.PREVIOUS_ACTIVITY_CREATE
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.TransitionHelper

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
@AndroidEntryPoint
class DiaryWritingActivity : BaseDiaryEditingActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private var mShowcaseIndex = 2

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
//            title = getString(R.string.create_diary_title)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_cross)
            setDisplayHomeAsUpEnabled(true)
        }

        addTextWatcher()
        setupRecognizer()
        initDateTime()
        setupDialog()
        setupPhotoView()
        setDateTime()
        bindEvent()
        setupKeypad()
        savedInstanceState?.let { restoreContents(it) } ?: run { checkTemporaryDiary(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY) }
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
        lifecycleScope.launch {
            if (mIsDiarySaved) {
                diaryRepository.deleteTemporaryDiaryByOriginId(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
            } else {
                saveTemporaryDiary(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    private fun setupKeypad() {
        val hasShot = getSharedPreferences("showcase_internal", Context.MODE_PRIVATE).getBoolean("hasShot$SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER", false)
        if (!hasShot) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun saveContents() {
        lifecycleScope.launch {
            if (isExistEasterEggDiary(0)) {
                duplicatedEasterEggWarning()
            } else {
                hideSoftInputFromWindow()
                setLocationInfo()
                if (mBinding.partialEditContents.diaryContents.text
                        .isNullOrEmpty()
                ) {
                    mBinding.partialEditContents.diaryContents.requestFocus()
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
                } else {
                    applyRemoveIndex()
                    val diary =
                        Diary(
                            currentTimeMillis = mCurrentTimeMillis,
                            title =
                                mBinding.partialEditContents.diaryTitle.text
                                    .toString(),
                            contents =
                                mBinding.partialEditContents.diaryContents.text
                                    .toString(),
                            symbolSequence = mSelectedItemPosition,
                            isAllDay = mBinding.partialEditContents.allDay.isChecked,
                            photoUris = mPhotoUris.map { it },
                            location = mLocation,
                        )
                    diaryRepository.insertDiary(diary)
                    config.previousActivity = PREVIOUS_ACTIVITY_CREATE
                    if (isAccessFromOutside()) {
                        startMainActivityWithClearTask()
                    } else {
                        TransitionHelper.finishActivityWithTransition(this@DiaryWritingActivity)
                    }
                    mIsDiarySaved = true
                }
            }
        }
    }

    private fun bindEvent() {
        mBinding.partialEditContents.partialEditPhotoContainer.photoView
            .setOnClickListener(mClickListener)
        mBinding.partialEditContents.partialEditPhotoContainer.captureCamera
            .setOnClickListener(mClickListener)
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

        mBinding.partialEditContents.feelingSymbolButton.setOnClickListener {
            mBinding.partialEditContents.diaryContents.clearFocus()
            lifecycleScope.launch {
                openFeelingSymbolDialog(
                    getString(R.string.diary_symbol_guide_message),
                    mSelectedItemPosition,
                    diaryViewModel.getSymbolUsedCountMap(true),
                ) { symbolSequence ->
                    selectFeelingSymbol(symbolSequence)
                }
            }
        }
    }
}
