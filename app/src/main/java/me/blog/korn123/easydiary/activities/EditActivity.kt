package me.blog.korn123.easydiary.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.simplemobiletools.commons.helpers.BaseConfig
import com.werb.pickphotoview.PickPhotoView
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmList
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

abstract class EditActivity : EasyDiaryActivity() {
    protected lateinit var mRecognizerIntent: Intent
    protected lateinit var mPhotoUris: RealmList<PhotoUriDto>
    protected lateinit var mDatePickerDialog: DatePickerDialog
    protected lateinit var mTimePickerDialog: TimePickerDialog
    protected lateinit var mSecondsPickerDialog: AlertDialog
    protected val mRemoveIndexes = ArrayList<Int>()
    protected var mCurrentTimeMillis: Long = 0
    protected var mYear = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.YEAR_PATTERN))
    protected var mMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.MONTH_PATTERN))
    protected var mDayOfMonth = Integer.valueOf(DateUtils.getCurrentDateTime(DateUtils.DAY_PATTERN))
    protected var mHourOfDay = Integer.valueOf(DateUtils.getCurrentDateTime("HH"))
    protected var mMinute = Integer.valueOf(DateUtils.getCurrentDateTime("mm"))
    protected var mSecond = Integer.valueOf(DateUtils.getCurrentDateTime("ss"))
    protected var mSelectedItemPosition = 0
    
    internal val mEditListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()

        when (view.id) {
            R.id.photoView -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                callImagePicker()
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
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
                val builder = EasyDiaryUtils.createSecondsPickerBuilder(this, itemClickListener, mSecond)
                mSecondsPickerDialog = builder.create()
                mSecondsPickerDialog.show()
            }
            R.id.microphone -> showSpeechDialog()
        }
    }

    protected var mStartDateListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        mYear = year
        mMonth = month + 1
        mDayOfMonth = dayOfMonth
        setDateTime()
    }

    protected var mTimeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
        mHourOfDay = hourOfDay
        mMinute = minute
        setDateTime()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                showAlertDialog(getString(R.string.back_pressed_confirm),
                        DialogInterface.OnClickListener { _, _ -> super.onBackPressed() },
                        null
                )
        }
        return true
    }

    override fun onBackPressed() {
        showAlertDialog(getString(R.string.back_pressed_confirm),
                DialogInterface.OnClickListener { _, _ -> super.onBackPressed() },
                null
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                callImagePicker()
            } else {
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {
            }
        }
    }

    protected fun applyRemoveIndex() {
        Collections.sort(mRemoveIndexes, Collections.reverseOrder())
        for (index in mRemoveIndexes) {
            mPhotoUris.removeAt(index)
        }
        mRemoveIndexes.clear()
    }
    
    protected fun toggleTimePickerTool() {
        when (allDay.isChecked) {
            true -> {
                timePicker.visibility = View.GONE
                secondsPicker.visibility = View.GONE
                mHourOfDay = 0
                mMinute = 0
                mSecond = 0
            }
            false -> {
                timePicker.visibility = View.VISIBLE
                secondsPicker.visibility = View.VISIBLE
            }
        }
        setDateTime()
    }
    
    protected fun setupPhotoView() {
        val thumbnailSize = config.settingThumbnailSize
        val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(applicationContext, thumbnailSize), CommonUtils.dpToPixel(applicationContext, thumbnailSize))
        photoView.layoutParams = layoutParams
    }

//    protected fun setupSpinner() {
//        val weatherArr = resources.getStringArray(R.array.weather_item_array)
//        val arrayAdapter = DiaryWeatherItemAdapter(this, R.layout.item_weather, Arrays.asList(*weatherArr))
//        weatherSpinner.adapter = arrayAdapter
//    }

    protected fun openFeelingSymbolDialog() {
        val builder = AlertDialog.Builder(this)
        var dialog: AlertDialog? = null
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.setMessage("현재 날씨 또는 다이어리의 내용과 어울리는 심볼을 선택하세요.")
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val symbolDialog = inflater.inflate(R.layout.dialog_feeling, null)
        val weatherArr = resources.getStringArray(R.array.weather_item_array)
        val activityArr = resources.getStringArray(R.array.activity_item_array)
        val mergedItems = Arrays.asList(*weatherArr, *activityArr)
        val arrayAdapter = DiaryWeatherItemAdapter(this, R.layout.item_weather, Arrays.asList(*weatherArr, *activityArr))
        val gridView = symbolDialog.findViewById<GridView>(R.id.feelingSymbols)
        gridView.adapter = arrayAdapter
        gridView.setOnItemClickListener { parent, view, position, id ->
            selectFeelingSymbol(position)
            dialog?.dismiss()
        }
        builder.setView(symbolDialog)
        dialog = builder.create()
        dialog?.show()
    }
    
    protected fun selectFeelingSymbol(index: Int) {
        mSelectedItemPosition = index
        when (mSelectedItemPosition == 0) {
            true -> symbolText.visibility = View.VISIBLE
            false -> symbolText.visibility = View.GONE
        }
        EasyDiaryUtils.initWeatherView(this, symbol, mSelectedItemPosition, false)
    }
    
    protected fun initBottomContainer() {
        // set bottom thumbnail container
        val drawable = photoView.background as GradientDrawable
        drawable.setColor(ColorUtils.setAlphaComponent(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
    }

    protected fun setupRecognizer() {
        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }
    
    protected fun hideSoftInputFromWindow() {
        val currentView = this.currentFocus
        if (currentView != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentView.windowToken, 0)
        }
    }

    protected fun setupDialog() {
        mDatePickerDialog = DatePickerDialog(this, mStartDateListener, mYear, mMonth - 1, mDayOfMonth)
        mTimePickerDialog = TimePickerDialog(this, mTimeSetListener, mHourOfDay, mMinute, DateFormat.is24HourFormat(this))
    }
    
    protected fun showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }
    }
    
    protected fun setDateTime() {
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
                title = DateUtils.getFullPatternDate(mCurrentTimeMillis)
                subtitle = if (allDay.isChecked) "No time information" else DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    
    protected fun callImagePicker() {
        when (config.multiPickerEnable) {
            true -> {
                var colorPrimaryDark: TypedValue = TypedValue()
                var colorPrimary: TypedValue = TypedValue()
                theme.resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDark, true)
                theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)
                PickPhotoView.Builder(this)
                        .setHasPhotoSize(0)
                        .setPickPhotoSize(15)
                        .setAllPhotoSize(15)
                        .setShowCamera(false)
                        .setSpanCount(4)
                        .setLightStatusBar(false)
                        .setStatusBarColor(colorPrimaryDark.resourceId)
                        .setToolbarColor(colorPrimary.resourceId)
                        .setToolbarTextColor(R.color.white)
                        .setSelectIconColor(colorPrimary.resourceId)
                        .start()
            }
            false -> {
                val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //                pickIntent.setType("image/*");
                try {
                    startActivityForResult(pickImageIntent, REQUEST_CODE_IMAGE_PICKER)
                } catch (e: ActivityNotFoundException) {
                    showAlertDialog(getString(R.string.gallery_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
                }
            }
        }
    }
    
    fun attachPhotos(selectPaths: ArrayList<String>) {
        setVisiblePhotoProgress(true)
        Thread(Runnable {
            selectPaths.map { item ->
                val photoPath = Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                try {
                    EasyDiaryUtils.downSamplingImage(this, File(item), File(photoPath))
                    mPhotoUris.add(PhotoUriDto(FILE_URI_PREFIX + photoPath))
                    val thumbnailSize = config.settingThumbnailSize
                    val bitmap = BitmapUtils.decodeFile(photoPath, CommonUtils.dpToPixel(applicationContext, thumbnailSize - 5), CommonUtils.dpToPixel(applicationContext, thumbnailSize - 5))
                    val imageView = ImageView(applicationContext)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(applicationContext, thumbnailSize), CommonUtils.dpToPixel(applicationContext, thumbnailSize))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(applicationContext, 3F), 0)
                    imageView.layoutParams = layoutParams
                    val drawable = ContextCompat.getDrawable(this, R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    val currentIndex = mPhotoUris.size - 1
                    imageView.setOnClickListener(PhotoClickListener(currentIndex))
                    runOnUiThread {
                        photoContainer.addView(imageView, photoContainer.childCount - 1)
                        initBottomToolbar()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                photoContainer.postDelayed({ (photoContainer.parent as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                setVisiblePhotoProgress(false)
            }
        }).start()
    }

    protected fun initBottomToolbar() {
        bottomTitle.text = String.format(getString(R.string.attached_photo_count), photoContainer.childCount -1)
    }
    
    abstract fun setVisiblePhotoProgress(isVisible: Boolean)
    
    internal inner class PhotoClickListener(var index: Int) : View.OnClickListener {
        override fun onClick(v: View) {
            val targetIndex = index
            showAlertDialog(
                    getString(R.string.delete_photo_confirm_message),
                    DialogInterface.OnClickListener { dialog, which ->
                        mRemoveIndexes.add(targetIndex)
                        photoContainer.removeView(v)
                        initBottomToolbar()
                    },
                    DialogInterface.OnClickListener { dialog, which -> }
            )
        }
    }
}