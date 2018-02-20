package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.ButterKnife
import butterknife.OnClick
import com.simplemobiletools.commons.helpers.BaseConfig
import io.realm.RealmList
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.activity_diary_update.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryUpdateActivity : EasyDiaryActivity() {

    private val REQUEST_CODE_SPEECH_INPUT = 100
    private var mRecognizerIntent: Intent? = null
    private var mCurrentTimeMillis: Long = 0
    private var mSequence: Int = 0
    private var mWeather: Int = 0
    private var mCurrentCursor = 1
    private var mPhotoUris: RealmList<PhotoUriDto>? = null
    private val mRemoveIndexes = ArrayList<Int>()
    private var mAlertDialog: AlertDialog? = null
    private var mPrimaryColor = 0
    private var mDatePickerDialog: DatePickerDialog? = null
    private var mTimePickerDialog: TimePickerDialog? = null
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDayOfMonth: Int = 0
    private var mHourOfDay: Int = 0
    private var mMinute: Int = 0
    private var mSecond: Int = 0

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_update)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.update_diary_title)
            setDisplayHomeAsUpEnabled(true)
        }

        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        bindEvent()
        initBottomContainer()
        initData()
        initDateTime()
        setDateTime()
    }

    @OnClick(R.id.saveContents, R.id.photoView, R.id.datePicker, R.id.timePicker, R.id.secondsPicker, R.id.microphone)
    fun onClick(view: View) {
        // Check if no view has focus:
        val currentView = this.currentFocus
        if (currentView != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        //        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        when (view.id) {
            R.id.saveContents -> if (StringUtils.isEmpty(diaryContents.text)) {
                diaryContents.requestFocus()
                DialogUtils.makeSnackBar(findViewById(android.R.id.content), getString(R.string.request_content_message))
            } else {
                val diaryDto = DiaryDto(
                        mSequence,
                        mCurrentTimeMillis,
                        diaryTitle.text.toString(),
                        diaryContents.text.toString()
                )
                diaryDto.weather = weatherSpinner.selectedItemPosition
                applyRemoveIndex()
                diaryDto.photoUris = mPhotoUris
                EasyDiaryDbHelper.updateDiary(diaryDto)
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
                if (mDatePickerDialog == null) {
                    mDatePickerDialog = DatePickerDialog(this, mStartDateListener, mYear, mMonth - 1, mDayOfMonth)
                }
                mDatePickerDialog?.show()
            }
            R.id.timePicker -> {
                if (mTimePickerDialog == null) {
                    mTimePickerDialog = TimePickerDialog(this, mTimeSetListener, mHourOfDay, mMinute, false)
                }
                mTimePickerDialog?.show()
            }
            R.id.secondsPicker -> {
                val itemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val itemMap = parent.adapter.getItem(position) as HashMap<String, String>
                    mSecond = Integer.valueOf(itemMap["value"])
                    setDateTime()
                    mAlertDialog?.cancel()
                }
                val builder = EasyDiaryUtils.createSecondsPickerBuilder(this@DiaryUpdateActivity, itemClickListener, mSecond)
                mAlertDialog = builder.create()
                mAlertDialog?.show()
            }
            R.id.microphone -> showSpeechDialog()
        }
    }

    override fun onBackPressed() {
        DialogUtils.showAlertDialog(this@DiaryUpdateActivity, getString(R.string.back_pressed_confirm),
                DialogInterface.OnClickListener { dialogInterface, i ->
                    //                        finish();
                    super@DiaryUpdateActivity.onBackPressed()
                },
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

    override fun onResume() {
        super.onResume()
        initBottomContainer()
        setFontsStyle()
    }

    private fun initBottomContainer() {
        // set bottom thumbnail container
        mPrimaryColor = BaseConfig(this@DiaryUpdateActivity).primaryColor
        val drawable = photoView.background as GradientDrawable
        drawable.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(applicationContext, assets, null, findViewById<View>(android.R.id.content) as ViewGroup)
        initSpinner()
    }

    fun initSpinner() {
        val weatherArr = resources.getStringArray(R.array.weather_item_array)
        val arrayAdapter = DiaryWeatherItemAdapter(this@DiaryUpdateActivity, R.layout.item_weather, Arrays.asList(*weatherArr))
        weatherSpinner.adapter = arrayAdapter
        weatherSpinner.setSelection(mWeather)
    }

    fun initData() {
        val intent = intent
        mSequence = intent.getIntExtra(Constants.DIARY_SEQUENCE, 0)
        val diaryDto = EasyDiaryDbHelper.readDiaryBy(mSequence)
        mWeather = diaryDto.weather

        diaryTitle.setText(diaryDto.title)
        //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
        diaryContents.setText(diaryDto.contents)
        mCurrentTimeMillis = diaryDto.currentTimeMillis
        diaryContents.requestFocus()

        // TODO fixme elegance
        mPhotoUris = RealmList()
        diaryDto.photoUris?.let {
            mPhotoUris?.addAll(it)
        }

        mPhotoUris?.let {
            for ((index, dto ) in it.withIndex()) {
                val uri = Uri.parse(dto.photoUri)
                var bitmap: Bitmap?
                try {
                    bitmap = BitmapUtils.decodeUri(this, uri, CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 65, 1), CommonUtils.dpToPixel(this, 45, 1))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.question_shield)
                } catch (se: SecurityException) {
                    se.printStackTrace()
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.question_shield)
                }

                val imageView = ImageView(this)
                val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1))
                layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0)
                imageView.layoutParams = layoutParams
                val drawable = resources.getDrawable(R.drawable.bg_card_thumbnail)
                val gradient = drawable as GradientDrawable
                gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
                imageView.background = gradient
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER
                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
        }

        initSpinner()
    }

    private fun bindEvent() {
        diaryTitle.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 0
            false
        }

        diaryContents.setOnTouchListener { view, motionEvent ->
            mCurrentCursor = 1
            diaryContents.requestFocus()
            false
        }
    }

    private fun initDateTime() {
        mYear = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.YEAR_PATTERN))
        mMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.MONTH_PATTERN))
        mDayOfMonth = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.DAY_PATTERN))
        mHourOfDay = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "HH"))
        mMinute = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "mm"))
        mSecond = Integer.valueOf(DateUtils.timeMillisToDateTime(mCurrentTimeMillis, "ss"))
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
            supportActionBar?.subtitle = DateUtils.getFullPatternDateWithTimeAndSeconds(mCurrentTimeMillis)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }

    private fun applyRemoveIndex() {
        Collections.sort(mRemoveIndexes, Collections.reverseOrder())
        for (index in mRemoveIndexes) {
            mPhotoUris?.removeAt(index)
        }
        mRemoveIndexes.clear()
    }

    private fun callImagePicker() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        try {
            startActivityForResult(pickImageIntent, Constants.REQUEST_CODE_IMAGE_PICKER)
        } catch (e: ActivityNotFoundException) {
            DialogUtils.showAlertDialog(this, getString(R.string.gallery_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }

    }

    private fun showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            DialogUtils.showAlertDialog(this, getString(R.string.recognizer_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        CommonUtils.saveLongPreference(this@DiaryUpdateActivity, Constants.SETTING_PAUSE_MILLIS, System.currentTimeMillis()) // clear screen lock policy
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && data != null) {
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
            Constants.REQUEST_CODE_IMAGE_PICKER -> try {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (mPhotoUris == null) mPhotoUris = RealmList()
                    mPhotoUris?.add(PhotoUriDto(data.data.toString()))
                    val bitmap = BitmapUtils.decodeUri(this, data.data, CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 65, 1), CommonUtils.dpToPixel(this, 45, 1))
                    val imageView = ImageView(this)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(this, 70, 1), CommonUtils.dpToPixel(this, 50, 1))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(this, 3, 1), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = resources.getDrawable(R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    val currentIndex = (mPhotoUris?.size ?: 0) - 1
                    imageView.setOnClickListener(PhotoClickListener(currentIndex))
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                    photoContainer.postDelayed({ (photoContainer.parent as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                }
            } catch (e: IOException) {
                e.printStackTrace()
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
        //        return super.onOptionsItemSelected(item);
        return true
    }

    internal inner class PhotoClickListener(var index: Int) : View.OnClickListener {

        override fun onClick(v: View) {
            val targetIndex = index
            DialogUtils.showAlertDialog(
                    this@DiaryUpdateActivity,
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
