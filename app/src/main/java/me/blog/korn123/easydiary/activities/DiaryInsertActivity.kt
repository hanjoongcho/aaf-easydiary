package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AlertDialog
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import butterknife.ButterKnife
import butterknife.OnClick
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.simplemobiletools.commons.helpers.BaseConfig
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_diary_insert.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.constants.Path
import me.blog.korn123.commons.utils.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryInsertActivity : EasyDiaryActivity() {
    private lateinit var mRecognizerIntent: Intent
    private lateinit var mDatePickerDialog: DatePickerDialog
    private lateinit var mTimePickerDialog: TimePickerDialog
    private lateinit var mSecondsPickerDialog: AlertDialog
    private lateinit var mShowcaseView: ShowcaseView
    private var mCurrentTimeMillis: Long = 0
    private var mCurrentCursor = 0
    private var mPhotoUris: RealmList<PhotoUriDto>? = null
    private val mRemoveIndexes = ArrayList<Int>()
    private var mShowcaseIndex = 2
    private var mPrimaryColor = 0
    private var mYear = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.YEAR_PATTERN))
    private var mMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.MONTH_PATTERN))
    private var mDayOfMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.DAY_PATTERN))
    private var mHourOfDay = Integer.valueOf(DateUtils.getCurrentDateTime("HH"))
    private var mMinute = Integer.valueOf(DateUtils.getCurrentDateTime("mm"))
    private var mSecond = Integer.valueOf(DateUtils.getCurrentDateTime("ss"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_insert)
        ButterKnife.bind(this)
        
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.create_diary_title)
            setDisplayHomeAsUpEnabled(true)
        }
        setDateTime()

        setupRecognizer()
        setupDialog()
        setupShowcase()
        
        initSpinner()
        initKeypad()
        bindEvent()
    }

    override fun onResume() {
        super.onResume()

        // set bottom thumbnail container
        mPrimaryColor = BaseConfig(this@DiaryInsertActivity).primaryColor
        val drawable = photoView.background as GradientDrawable
        drawable.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
        setFontsStyle()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }
    
    override fun onBackPressed() {
        DialogUtils.showAlertDialog(this@DiaryInsertActivity, getString(R.string.back_pressed_confirm),
                DialogInterface.OnClickListener { dialogInterface, i -> super@DiaryInsertActivity.onBackPressed() },
                DialogInterface.OnClickListener { dialogInterface, i -> }
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQUEST_CODE_EXTERNAL_STORAGE -> if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                // 권한이 있는경우
                callImagePicker()
            } else {
                // 권한이 없는경우
                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.config.aafPinLockPauseMillis = System.currentTimeMillis()
        when (requestCode) {
            Constants.REQUEST_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (mCurrentCursor == 0) { // edit title
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
            Constants.REQUEST_CODE_IMAGE_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
                if (mPhotoUris == null) mPhotoUris = RealmList()
                val photoPath = Environment.getExternalStorageDirectory().absolutePath + Path.DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                //                    mPhotoUris.add(new PhotoUriDto(data.getData().toString()));
                try {
                    CommonUtils.uriToFile(this, data.data!!, photoPath)
                    mPhotoUris!!.add(PhotoUriDto(FILE_URI_PREFIX + photoPath))
                    val bitmap = BitmapUtils.decodeFile(applicationContext, photoPath, CommonUtils.dpToPixel(applicationContext, 70, 1), CommonUtils.dpToPixel(applicationContext, 65, 1), CommonUtils.dpToPixel(applicationContext, 45, 1))
                    val imageView = ImageView(applicationContext)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(applicationContext, 70, 1), CommonUtils.dpToPixel(applicationContext, 50, 1))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(applicationContext, 3, 1), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = resources.getDrawable(R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    //                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    val currentIndex = mPhotoUris!!.size - 1
                    imageView.setOnClickListener(PhotoClickListener(currentIndex))
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                    photoContainer.postDelayed({ (photoContainer.parent as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            else -> {
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                //                finish();
                //                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                super.onBackPressed()
        }
        return true
        //        return super.onOptionsItemSelected(item);
    }
    
    private fun setupRecognizer() {
        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }
    
    private fun setupDialog() {
        mDatePickerDialog = DatePickerDialog(this, mStartDateListener, mYear, mMonth - 1, mDayOfMonth)
        mTimePickerDialog = TimePickerDialog(this, mTimeSetListener, mHourOfDay, mMinute, false)
    }

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
                .setTarget(ViewTarget(weatherSpinner))
                .setContentTitle(getString(R.string.create_diary_showcase_title_1))
                .setContentText(getString(R.string.create_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .build()
        mShowcaseView.setButtonText(getString(R.string.create_diary_showcase_button_1))
        mShowcaseView.setButtonPosition(centerParams)
    }

    private fun initSpinner() {
        val weatherArr = resources.getStringArray(R.array.weather_item_array)
        val arrayAdapter = DiaryWeatherItemAdapter(this@DiaryInsertActivity, R.layout.item_weather, Arrays.asList(*weatherArr))
        weatherSpinner.adapter = arrayAdapter
    }
    
    private fun initKeypad() {
        val hasShot = getSharedPreferences("showcase_internal", Context.MODE_PRIVATE).getBoolean("hasShot" + Constants.SHOWCASE_SINGLE_SHOT_CREATE_DIARY_NUMBER, false)
        if (!hasShot) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
    
    private var mStartDateListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        mYear = year
        mMonth = month + 1
        mDayOfMonth = dayOfMonth
        setDateTime()
    }

    private var mTimeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        mHourOfDay = hourOfDay
        mMinute = minute
        setDateTime()
    }

    @OnClick(R.id.saveContents, R.id.photoView, R.id.datePicker, R.id.timePicker, R.id.secondsPicker, R.id.microphone)
    fun onClick(view: View) {
        val currentView = this.currentFocus
        if (currentView != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        when (view.id) {
            R.id.saveContents -> if (StringUtils.isEmpty(diaryContents.text)) {
                diaryContents.requestFocus()
                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val diaryDto = DiaryDto(
                        -1,
                        mCurrentTimeMillis,
                        this@DiaryInsertActivity.diaryTitle.text.toString(),
                        this@DiaryInsertActivity.diaryContents.text.toString(),
                        weatherSpinner.selectedItemPosition
                )
                applyRemoveIndex()
                diaryDto.photoUris = mPhotoUris
                EasyDiaryDbHelper.insertDiary(diaryDto)
                CommonUtils.saveIntPreference(this@DiaryInsertActivity, Constants.PREVIOUS_ACTIVITY, Constants.PREVIOUS_ACTIVITY_CREATE)
                finish()
            }
            R.id.photoView -> if (PermissionUtils.checkPermission(this, Constants.EXTERNAL_STORAGE_PERMISSIONS)) {
                // API Level 22 이하이거나 API Level 23 이상이면서 권한취득 한경우
                callImagePicker()
            } else {
                // API Level 23 이상이면서 권한취득 안한경우
                PermissionUtils.confirmPermission(this, this, Constants.EXTERNAL_STORAGE_PERMISSIONS, Constants.REQUEST_CODE_EXTERNAL_STORAGE)
            }
            R.id.datePicker -> {
                mDatePickerDialog.show()
            }
            R.id.timePicker -> {
                mTimePickerDialog.show()
            }
            R.id.secondsPicker -> {
                val itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val itemMap = parent.adapter.getItem(position) as HashMap<String, String>
                    mSecond = Integer.valueOf(itemMap["value"])!!
                    setDateTime()
                    mSecondsPickerDialog.cancel()
                }
                val builder = EasyDiaryUtils.createSecondsPickerBuilder(this@DiaryInsertActivity, itemClickListener, mSecond)
                mSecondsPickerDialog = builder.create()
                mSecondsPickerDialog.show()
            }
            R.id.microphone -> showSpeechDialog()
        }
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<View>(android.R.id.content) as ViewGroup)
    }

    private fun bindEvent() {

        diaryTitle.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 0
            false
        }

        diaryContents.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 1
            false
        }
    }

    private fun setDateTime() {
        try {
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val dateTimeString = String.format(
                    "%d%s%s%s%s%s",
                    mYear,
                    StringUtils.leftPad(mMonth.toString(), 2, "0"),
                    StringUtils.leftPad(mDayOfMonth.toString(), 2, "0"),
                    StringUtils.leftPad(mHourOfDay.toString(), 2, "0"),
                    StringUtils.leftPad(mMinute.toString(), 2, "0"),
                    StringUtils.leftPad(mSecond.toString(), 2, "0")
            )
            val parsedDate = format.parse(dateTimeString)
            mCurrentTimeMillis = parsedDate.time
            supportActionBar?.run {
                subtitle = DateUtils.getFullPatternDateWithTimeAndSeconds(mCurrentTimeMillis, Locale.getDefault())
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    private fun applyRemoveIndex() {
        Collections.sort(mRemoveIndexes, Collections.reverseOrder())
        for (index in mRemoveIndexes) {
            mPhotoUris!!.removeAt(index)
        }
        mRemoveIndexes.clear()
    }

    private fun callImagePicker() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //                pickIntent.setType("image/*");
        try {
            startActivityForResult(pickImageIntent, Constants.REQUEST_CODE_IMAGE_PICKER)
        } catch (e: ActivityNotFoundException) {
            DialogUtils.showAlertDialog(this, getString(R.string.gallery_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }

    }

    private fun showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, Constants.REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            DialogUtils.showAlertDialog(this, getString(R.string.recognizer_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }

    }
    
    internal inner class PhotoClickListener(var index: Int) : View.OnClickListener {
        override fun onClick(v: View) {
            val targetIndex = index
            DialogUtils.showAlertDialog(
                    this@DiaryInsertActivity,
                    getString(R.string.delete_photo_confirm_message),
                    DialogInterface.OnClickListener { dialog, which ->
                        mRemoveIndexes.add(targetIndex)
                        photoContainer.removeView(v)
                    },
                    DialogInterface.OnClickListener { dialog, which -> }
            )
        }
    }
}
