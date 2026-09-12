package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.DIARY_ENCRYPT_PASSWORD
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.toDomain
import org.apache.commons.lang3.StringUtils
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
@AndroidEntryPoint
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
            setHomeAsUpIndicator(R.drawable.ic_cross)
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
        lifecycleScope.launch {
            if (mIsDiarySaved) {
                diaryViewModel.deleteTemporaryDiaryByOriginId(mSequence)
            } else {
                saveTemporaryDiary(mSequence)
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun initData() {
        val intent = intent
        mSequence = intent.getIntExtra(DIARY_SEQUENCE, 0)
        lifecycleScope.launch {
            diaryViewModel.findDiaryById(mSequence)?.let {
                mSymbolSequence = it.symbolSequence
                mLinkedDiaries = it.linkedDiaries
                initData(it)
            }
        }
    }

    override fun saveContents() {
        lifecycleScope.launch {
            if (isExistEasterEggDiary(1)) {
                duplicatedEasterEggWarning()
            } else {
                hideSoftInputFromWindow()
                if (StringUtils.isEmpty(mBinding.partialEditContents.diaryContents.text)) {
                    mBinding.partialEditContents.diaryContents.requestFocus()
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
                } else {
                    val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
                    val diary =
                        when (encryptionPass == null) {
                            true -> {
                                DiaryDomain(
                                    diaryId = mSequence,
                                    currentTimeMillis = mCurrentTimeMillis,
                                    title =
                                        mBinding.partialEditContents.diaryTitle.text
                                            .toString(),
                                    contents =
                                        mBinding.partialEditContents.diaryContents.text
                                            .toString(),
                                    symbolSequence = mSelectedItemPosition,
                                    isAllDay = mBinding.partialEditContents.allDay.isChecked,
                                    photoUris = mPhotoUris.map { it.toDomain() },
                                    location = mLocation?.toDomain(),
                                    linkedDiaries = ArrayList(mLinkedDiaries),
                                )
                            }

                            false -> {
                                DiaryDomain(
                                    diaryId = mSequence,
                                    currentTimeMillis = mCurrentTimeMillis,
                                    title =
                                        JasyptUtils.encrypt(
                                            mBinding.partialEditContents.diaryTitle.text
                                                .toString(),
                                            encryptionPass,
                                        ),
                                    contents =
                                        JasyptUtils.encrypt(
                                            mBinding.partialEditContents.diaryContents.text
                                                .toString(),
                                            encryptionPass,
                                        ),
                                    isEncrypt = true,
                                    encryptKeyHash = JasyptUtils.sha256(encryptionPass),
                                    symbolSequence = mSelectedItemPosition,
                                    isAllDay = mBinding.partialEditContents.allDay.isChecked,
                                    photoUris = mPhotoUris.map { it.toDomain() },
                                    location = mLocation?.toDomain(),
                                    linkedDiaries = ArrayList(mLinkedDiaries),
                                )
                            }
                        }

                    applyRemoveIndex()
                    diaryViewModel.updateDiary(diary)
                    TransitionHelper.finishActivityWithTransition(this@DiaryEditingActivity)
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
