package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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

@AndroidEntryPoint
class DiaryWritingActivity : BaseDiaryEditingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
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
            true -> binding.photoProgress.visibility = View.VISIBLE
            false -> binding.photoProgress.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            if (isDiarySaved) {
                diaryRepository.deleteTemporaryDiaryByOriginId(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
            } else {
                saveTemporaryDiary(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
            }
        }
    }

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
                if (binding.partialEditContents.diaryContents.text.isNullOrEmpty()) {
                    binding.partialEditContents.diaryContents.requestFocus()
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
                } else {
                    applyRemoveIndex()
                    val diary =
                        Diary(
                            currentTimeMillis = currentTimeMillis,
                            title = binding.partialEditContents.diaryTitle.text.toString(),
                            contents = binding.partialEditContents.diaryContents.text.toString(),
                            symbolSequence = selectedItemPosition,
                            isAllDay = binding.partialEditContents.allDay.isChecked,
                            photoUris = photoUris.map { it },
                            location = location,
                        )
                    diaryRepository.insertDiary(diary)
                    config.previousActivity = PREVIOUS_ACTIVITY_CREATE
                    if (isAccessFromOutside()) {
                        startMainActivityWithClearTask()
                    } else {
                        TransitionHelper.finishActivityWithTransition(this@DiaryWritingActivity)
                    }
                    isDiarySaved = true
                }
            }
        }
    }

    private fun bindEvent() {
        binding.partialEditContents.partialEditPhotoContainer.photoView.setOnClickListener(clickListener)
        binding.partialEditContents.partialEditPhotoContainer.captureCamera.setOnClickListener(clickListener)
        binding.partialEditContents.diaryTitle.setOnTouchListener(touchListener)
        binding.partialEditContents.diaryContents.setOnTouchListener(touchListener)

        binding.partialEditContents.partialBottomToolbar.togglePhoto.setOnClickListener {
            toggleSimpleLayout()
        }

        binding.partialEditContents.run {
            allDayContainer.setOnClickListener {
                allDay.isChecked = allDay.isChecked.not()
                toggleTimePickerTool()
            }
        }

        binding.partialEditContents.feelingSymbolButton.setOnClickListener {
            binding.partialEditContents.diaryContents.clearFocus()
            lifecycleScope.launch {
                openFeelingSymbolDialog(
                    getString(R.string.diary_symbol_guide_message),
                    selectedItemPosition,
                    diaryViewModel.getSymbolUsedCountMap(true),
                ) { sequence ->
                    selectFeelingSymbol(sequence)
                }
            }
        }
    }
}
