package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.werb.pickphotoview.util.PickConfig
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_diary_edit.*
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.util.*

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
        setContentView(R.layout.activity_diary_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.create_diary_title)
            setDisplayHomeAsUpEnabled(true)
        }

        addTextWatcher()
        setupRecognizer()
        setupShowcase()
        initDateTime()
        setupDialog()
        setupPhotoView()
        initBottomToolbar()
        setDateTime()
        bindEvent()
        setupKeypad()
        restoreContents(savedInstanceState)
        toggleSimpleLayout()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.let {
            val listUriString = arrayListOf<String>()
            mPhotoUris.map { model ->
                listUriString.add(model.photoUri ?: "")
            }
            it.putStringArrayList(LIST_URI_STRING, listUriString)
            it.putInt(SELECTED_YEAR, mYear)
            it.putInt(SELECTED_MONTH, mMonth)
            it.putInt(SELECTED_DAY, mDayOfMonth)
            it.putInt(SELECTED_HOUR, mHourOfDay)
            it.putInt(SELECTED_MINUTE, mMinute)
            it.putInt(SELECTED_SECOND, mSecond)
        }
        super.onSaveInstanceState(outState)
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
                    mShowcaseView.setShowcase(ViewTarget(diaryTitle), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_2))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_2))
                }
                3 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(diaryContents), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_3))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_3))
                }
                4 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(photoView), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_4))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_4))
                }

                5 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(datePicker), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_7))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_7))
                }
                6 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(timePicker), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_8))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_8))
                }
                7 -> {
                    mShowcaseView.setButtonPosition(centerParams)
                    mShowcaseView.setShowcase(ViewTarget(saveContents), true)
                    mShowcaseView.setContentTitle(getString(R.string.create_diary_showcase_title_9))
                    mShowcaseView.setContentText(getString(R.string.create_diary_showcase_message_9))
                    mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_2))
                }
                8 -> mShowcaseView.hide()
            }
            mShowcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(ViewTarget(feelingSymbolButton))
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
        val hasShot = getSharedPreferences("showcase_internal", Context.MODE_PRIVATE).getBoolean("hasShot" + SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER, false)
        if (!hasShot) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun bindEvent() {
        saveContents.setOnClickListener {
            hideSoftInputFromWindow()
            setLocationInfo()
            if (StringUtils.isEmpty(diaryContents.text)) {
                diaryContents.requestFocus()
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val diaryDto = DiaryDto(
                        -1,
                        mCurrentTimeMillis,
                        this@DiaryInsertActivity.diaryTitle.text.toString(),
                        this@DiaryInsertActivity.diaryContents.text.toString(),
                        mSelectedItemPosition,
                        allDay.isChecked
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
            }
        }

        photoView.setOnClickListener(mClickListener)
        captureCamera.setOnClickListener(mClickListener)
        datePicker.setOnClickListener(mClickListener)
        timePicker.setOnClickListener(mClickListener)
        secondsPicker.setOnClickListener(mClickListener)
        microphone.setOnClickListener(mClickListener)
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
    
    private fun restoreContents(savedInstanceState: Bundle?) {
        mPhotoUris = RealmList()
        savedInstanceState?.let {
            it.getStringArrayList(LIST_URI_STRING)?.map { uriString ->
                mPhotoUris.add(PhotoUriDto(uriString))
            }
            mYear = it.getInt(SELECTED_YEAR, mYear)
            mMonth = it.getInt(SELECTED_MONTH, mMonth)
            mDayOfMonth = it.getInt(SELECTED_DAY, mDayOfMonth)
            mHourOfDay = it.getInt(SELECTED_HOUR, mHourOfDay)
            mMinute = it.getInt(SELECTED_MINUTE, mMinute)
            mSecond = it.getInt(SELECTED_SECOND, mSecond)
            val thumbnailSize = config.settingThumbnailSize

            mPhotoUris.forEachIndexed { index, photoUriDto ->
                val imageView = EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, index)
                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
        }
        System.currentTimeMillis()
    }

    companion object {
        const val INITIALIZE_TIME_MILLIS = "initialize_time_millis"
    }
}
