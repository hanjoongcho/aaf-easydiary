package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.PREVIOUS_ACTIVITY_CREATE
import me.blog.korn123.easydiary.helper.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryInsertActivity : EditActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mShowcaseView: ShowcaseView
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
            setDisplayHomeAsUpEnabled(true)
        }

        addTextWatcher()
        setupRecognizer()
        setupShowcase()
        initDateTime()
        setupDialog()
        setupPhotoView()
        setDateTime()
        bindEvent()
        setupKeypad()
        savedInstanceState?.let { restoreContents(it) } ?: run { checkTemporaryDiary(DIARY_SEQUENCE_TEMPORARY) }
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
            EasyDiaryDbHelper.deleteTemporaryDiaryBy(DIARY_SEQUENCE_TEMPORARY)
        } else {
            saveTemporaryDiary(DIARY_SEQUENCE_TEMPORARY)
        }
        if (config.enableDebugMode) makeToast("onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (config.enableDebugMode) makeToast("onDestroy")
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupShowcase() {
        val margin = ((resources.displayMetrics.density * 12) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)

        val showcaseViewOnClickListener = View.OnClickListener {
            when (mShowcaseIndex) {
                2 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(mBinding.partialEditContents.diaryTitle), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_2))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_2))
                }
                3 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(mBinding.partialEditContents.diaryContents), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_3))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_3))
                }
//                4 -> {
//                    mShowcaseView.setButtonPosition(centerParams)
//                    mShowcaseView.setShowcase(ViewTarget(mBinding.partialEditContents.partialEditPhotoContainer.photoView), true)
//                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_4))
//                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_4))
//                }
                4 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(R.id.datePicker, this), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_7))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_7))
                }
                5 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(R.id.timePicker, this), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_8))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_8))
                }
                6 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(R.id.secondsPicker, this), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_9))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_9))
                    mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_2))
                }
                7 -> mShowcaseView.hide()
            }
            mShowcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(ViewTarget(mBinding.partialEditContents.feelingSymbolButton))
                .setContentTitle(getString(R.string.create_diary_showcase_title_1))
                .setContentText(getString(R.string.create_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .build()
        mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_1))
        mShowcaseView.setButtonPosition(centerParams)
    }

    private fun setupKeypad() {
        val hasShot = getSharedPreferences("showcase_internal", Context.MODE_PRIVATE).getBoolean("hasShot$SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER", false)
        if (!hasShot) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun saveContents() {
        hideSoftInputFromWindow()
        setLocationInfo()
        if (StringUtils.isEmpty(mBinding.partialEditContents.diaryContents.text)) {
            mBinding.partialEditContents.diaryContents.requestFocus()
            makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
        } else {
            val diaryDto = DiaryDto(
                    DIARY_SEQUENCE_INIT,
                    mCurrentTimeMillis,
                    mBinding.partialEditContents.diaryTitle.text.toString(),
                    mBinding.partialEditContents.diaryContents.text.toString(),
                    mSelectedItemPosition,
                    mBinding.partialEditContents.allDay.isChecked
            )
            if (mLocation != null) diaryDto.location = mLocation
            applyRemoveIndex()
            diaryDto.photoUris = mPhotoUris
            EasyDiaryDbHelper.insertDiary(diaryDto)
            config.previousActivity = PREVIOUS_ACTIVITY_CREATE
            if (isAccessFromOutside()) {
                startMainActivityWithClearTask()
            } else {
                TransitionHelper.finishActivityWithTransition(this)
            }
            mIsDiarySaved = true
        }
    }

    private fun bindEvent() {
        mBinding.partialEditContents.partialEditPhotoContainer.photoView.setOnClickListener(mClickListener)
        mBinding.partialEditContents.partialEditPhotoContainer.captureCamera.setOnClickListener(mClickListener)
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

    companion object {
        const val INITIALIZE_TIME_MILLIS = "initialize_time_millis"
    }
}
