package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.openFeelingSymbolDialog
import me.blog.korn123.easydiary.helper.DIARY_ENCRYPT_PASSWORD
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.utils.JasyptUtils
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

@AndroidEntryPoint
class DiaryEditingActivity : BaseDiaryEditingActivity() {
    private var diarySequence: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        lifecycleScope.launch {
            addTextWatcher()
            setupRecognizer()
            initData()
            initDateTime()
            setupDialog()
            setupPhotoView()
            setDateTime()
            bindEvent()
            savedInstanceState?.let { restoreContents(it) } ?: run { checkTemporaryDiary(diarySequence) }
            initBottomToolbar()
            toggleSimpleLayout()
        }
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
                diaryRepository.deleteTemporaryDiaryByOriginId(diarySequence)
            } else {
                saveTemporaryDiary(diarySequence)
            }
        }
    }

    private suspend fun initData() {
        diarySequence = intent.getLongExtra(DIARY_SEQUENCE, 0)
        diaryViewModel.findDiaryById(diarySequence)?.let { diary ->
            symbolSequence = diary.symbolSequence
            linkedDiaries = diary.linkedDiaries
            initData(diary)
        }
    }

    override fun saveContents() {
        lifecycleScope.launch {
            if (isExistEasterEggDiary(1)) {
                duplicatedEasterEggWarning()
            } else {
                hideSoftInputFromWindow()
                if (binding.partialEditContents.diaryContents.text.isNullOrEmpty()) {
                    binding.partialEditContents.diaryContents.requestFocus()
                    makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
                } else {
                    applyRemoveIndex()
                    val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
                    val diary =
                        when (encryptionPass == null) {
                            true -> {
                                DiaryDomain(
                                    diaryId = diarySequence,
                                    currentTimeMillis = currentTimeMillis,
                                    title = binding.partialEditContents.diaryTitle.text.toString(),
                                    contents = binding.partialEditContents.diaryContents.text.toString(),
                                    symbolSequence = selectedItemPosition,
                                    isAllDay = binding.partialEditContents.allDay.isChecked,
                                    photoUris = photoUris.map { it },
                                    location = location,
                                    linkedDiaries = ArrayList(linkedDiaries),
                                )
                            }

                            false -> {
                                DiaryDomain(
                                    diaryId = diarySequence,
                                    currentTimeMillis = currentTimeMillis,
                                    title = JasyptUtils.encrypt(
                                        binding.partialEditContents.diaryTitle.text.toString(),
                                        encryptionPass,
                                    ),
                                    contents = JasyptUtils.encrypt(
                                        binding.partialEditContents.diaryContents.text.toString(),
                                        encryptionPass,
                                    ),
                                    isEncrypt = true,
                                    encryptKeyHash = JasyptUtils.sha256(encryptionPass),
                                    symbolSequence = selectedItemPosition,
                                    isAllDay = binding.partialEditContents.allDay.isChecked,
                                    photoUris = photoUris.map { it },
                                    location = location,
                                    linkedDiaries = ArrayList(linkedDiaries),
                                )
                            }
                        }

                    diaryRepository.updateDiaryWithPhotos(diary)
                    TransitionHelper.finishActivityWithTransition(this@DiaryEditingActivity)
                    isDiarySaved = true
                }
            }
        }
    }

    private fun bindEvent() {
        binding.partialEditContents.partialEditPhotoContainer.photoView.setOnClickListener(clickListener)
        binding.partialEditContents.partialEditPhotoContainer.captureCamera.setOnClickListener(clickListener)
        binding.partialEditContents.locationContainer.setOnClickListener(clickListener)
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
