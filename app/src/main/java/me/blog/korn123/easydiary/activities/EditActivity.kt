package me.blog.korn123.easydiary.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.werb.pickphotoview.PickPhotoView
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmList
import kotlinx.android.synthetic.main.partial_bottom_toolbar.*
import kotlinx.android.synthetic.main.partial_edit_contents.*
import kotlinx.android.synthetic.main.partial_edit_photo_container.*
import kotlinx.android.synthetic.main.partial_edit_toolbar_sub.*
import kotlinx.android.synthetic.main.viewholder_photo.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList

abstract class EditActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRecognizerIntent: Intent
    private lateinit var mDatePickerDialog: DatePickerDialog
    private lateinit var mTimePickerDialog: TimePickerDialog
    private lateinit var mSecondsPickerDialog: AlertDialog
    private val mCalendar = Calendar.getInstance(Locale.getDefault())
    private val mRemoveIndexes = ArrayList<Int>()
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val mNetworkLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            if (config.enableDebugMode) makeToast("Network location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mGPSLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            if (config.enableDebugMode) makeToast("GPS location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }

    protected val mPhotoUris: RealmList<PhotoUriDto> = RealmList()
    protected var mCurrentTimeMillis: Long = 0
    protected var mYear = mCalendar.get(Calendar.YEAR)
    protected var mLocation: me.blog.korn123.easydiary.models.Location? = null
    protected var mIsDiarySaved = false

    /**
     * mMonth is not Calendar.MONTH
     * mMonth range is 1 ~ 12
     */
    var mMonth = mCalendar.get(Calendar.MONTH).plus(1)
    var mDayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH)
    var mHourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY)
    var mMinute = mCalendar.get(Calendar.MINUTE)
    var mSecond = mCalendar.get(Calendar.SECOND)
    var mSelectedItemPosition = 0
    var mCurrentCursor = 0

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

    val mClickListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()

        when (view.id) {
            R.id.photoView -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                callImagePicker()
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
            }
            R.id.captureCamera -> {
                val captureFile = createTemporaryPhotoFile()
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(captureFile))
                startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMERA)
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
                    mSecond = Integer.valueOf(itemMap["value"]!!)
                    setDateTime()
                    mSecondsPickerDialog.cancel()
                }
                val builder = EasyDiaryUtils.createSecondsPickerBuilder(this, itemClickListener, mSecond)
                mSecondsPickerDialog = builder.create()
                mSecondsPickerDialog.show()
            }
            R.id.microphone -> showSpeechDialog()
            R.id.locationContainer -> {
                setLocationInfo()
            }
        }
    }

    val mTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_UP -> view.performClick()
        }

        mCurrentCursor = if (view.id == R.id.diaryTitle) FOCUS_TITLE else FOCUS_CONTENTS
        false
    }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeDrawableIconColor(Color.WHITE, R.drawable.calendar_4_w)

        if (config.enableLocationInfo && checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION))) {
            mLocationManager.run {
                if (isProviderEnabled(LocationManager.GPS_PROVIDER)) requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mGPSLocationListener)
                if (isProviderEnabled(LocationManager.NETWORK_PROVIDER)) requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, mNetworkLocationListener)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        applyRemoveIndex()
        outState.run {
            val listUriString = arrayListOf<String>()
            mPhotoUris.map { model ->
                listUriString.add(model.photoUri ?: "")
            }
            putStringArrayList(LIST_URI_STRING, listUriString)
            putInt(SELECTED_YEAR, mYear)
            putInt(SELECTED_MONTH, mMonth)
            putInt(SELECTED_DAY, mDayOfMonth)
            putInt(SELECTED_HOUR, mHourOfDay)
            putInt(SELECTED_MINUTE, mMinute)
            putInt(SELECTED_SECOND, mSecond)
            putInt(SYMBOL_SEQUENCE, mSelectedItemPosition)
        }
        super.onSaveInstanceState(outState)
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
                DialogInterface.OnClickListener { _, _ ->
                    if (isAccessFromOutside()) {
                        startMainActivityWithClearTask()
                    } else {
                        super.onBackPressed()
                    }
                },
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

    override fun onDestroy() {
        super.onDestroy()
        if (config.enableLocationInfo) {
            mLocationManager.run {
                removeUpdates(mGPSLocationListener)
                removeUpdates(mNetworkLocationListener)
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }
    }

    private fun callImagePicker() {
        when (config.multiPickerEnable) {
            true -> {
                var colorPrimaryDark: TypedValue = TypedValue()
                var colorPrimary: TypedValue = TypedValue()
                theme.resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDark, true)
                theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)
                PickPhotoView.Builder(this)
                        .setShowCamera(false)
                        .setHasPhotoSize(0)
                        .setPickPhotoSize(15)
                        .setAllPhotoSize(15)
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

    protected fun saveTemporaryDiary(originSequence: Int) {
        val diaryTemp = DiaryDto(
                DIARY_SEQUENCE_INIT,
                mCurrentTimeMillis,
                diaryTitle.text.toString(),
                diaryContents.text.toString(),
                mSelectedItemPosition,
                allDay.isChecked
        ).apply {
            this.originSequence = originSequence
            photoUris = mPhotoUris
        }
        if (StringUtils.isNotEmpty(diaryTemp.title)
                || StringUtils.isNotEmpty(diaryTemp.contents)
                || diaryTemp.photoUris?.isNotEmpty() == true) {
            if (mLocation != null) diaryTemp.location = mLocation
            EasyDiaryDbHelper.insertTemporaryDiary(diaryTemp)
        }
    }

    protected fun checkTemporaryDiary(originSequence: Int) {
        EasyDiaryDbHelper.findTemporaryDiaryBy(originSequence)?.let {
            showAlertDialog("임시저장 다이어리 불러오기", "임시저장된 다이어리가 있습니다. 임시저장된 다이어리를 불러오시겠습니까?"
                    , { _, _ ->
                initData(it)
                initBottomToolbar()
                EasyDiaryDbHelper.deleteTemporaryDiaryBy(DIARY_SEQUENCE_TEMPORARY)
            }
                    , { _, _ -> EasyDiaryDbHelper.deleteDiaryBy(DIARY_SEQUENCE_TEMPORARY) }, false
            )
        }
    }

    protected fun addTextWatcher() {
        if (config.enableCountCharacters) {
            contentsLength?.run {
//                setTextColor(config.textColor)
//                background = getLabelBackground()
//                visibility = View.VISIBLE
                text = getString(R.string.diary_contents_length, 0)
            }
            diaryContents.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    contentsLength.text = getString(R.string.diary_contents_length, p0?.length ?: 0)
                }
            })
            contentsLengthContainer.visibility = View.VISIBLE
        } else {
            contentsLengthContainer.visibility = View.GONE
        }
    }

    protected fun toggleSimpleLayout() {
        when (photoContainerScrollView.visibility) {
            View.VISIBLE -> {
                photoContainerScrollView.visibility = View.GONE
                titleCard.visibility = View.VISIBLE
                togglePhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.expand))
                supportActionBar?.hide()
            }
            View.GONE -> {
                photoContainerScrollView.visibility = View.VISIBLE
                titleCard.visibility = View.GONE
                togglePhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.collapse))
                supportActionBar?.show()
            }
            else -> {}
        }
    }

    protected fun setLocationInfo() {
        if (config.enableLocationInfo) {
            locationProgress.visibility = View.VISIBLE
            getLastKnownLocation()?.let { knownLocation ->
                var locationInfo: String? = null
                getFromLocation(knownLocation.latitude, knownLocation.longitude, 1)?.let { address ->
                    if (address.isNotEmpty()) {
                        locationInfo = fullAddress(address[0])
                        mLocation = me.blog.korn123.easydiary.models.Location(locationInfo, knownLocation.latitude, knownLocation.longitude)
                    }
                }
                locationLabel.text = locationInfo
            } ?: {
                makeToast(getString(R.string.location_info_error_message))
            } ()
            locationProgress.visibility = View.GONE
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
        captureCamera.layoutParams = layoutParams
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

    protected fun setDateTime() {
        try {
            mCurrentTimeMillis = EasyDiaryUtils.datePickerToTimeMillis(
                    mDayOfMonth, mMonth - 1, mYear,
                    false,
                    mHourOfDay, mMinute, mSecond
            )
            supportActionBar?.run {
                title = DateUtils.getFullPatternDate(mCurrentTimeMillis)
                subtitle = if (allDay.isChecked) "No time information" else DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    protected fun attachPhotos(selectPaths: ArrayList<String>, isUriString: Boolean) {
        setVisiblePhotoProgress(true)
        Thread(Runnable {
            selectPaths.map { item ->
                val photoPath = EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                try {
                    val mimeType: String = when (isUriString) {
                        true -> EasyDiaryUtils.downSamplingImage(this, Uri.parse(item), File(photoPath))
                        false -> {
                            EasyDiaryUtils.downSamplingImage(this, File(item), File(photoPath))
                            MIME_TYPE_JPEG
                        }
                    }
                    val photoUriDto = PhotoUriDto(FILE_URI_PREFIX + photoPath, mimeType)
                    mPhotoUris.add(photoUriDto)
                    val currentIndex = mPhotoUris.size - 1
                    runOnUiThread {
                        val imageView = when (isLandScape()) {
                            true -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 0F, 3F)
                            false -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 3F, 0F)
                        }
                        imageView.setOnClickListener(PhotoClickListener(currentIndex))
                        photoContainer.addView(imageView, photoContainer.childCount - 1)
                        initBottomToolbar()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                when (isLandScape()) {
                    true -> photoContainer.postDelayed({ (photoContainerScrollView.getChildAt(0) as ScrollView).fullScroll(ScrollView.FOCUS_DOWN) }, 100L)
                    false -> photoContainer.postDelayed({ (photoContainerScrollView.getChildAt(0) as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                }
                setVisiblePhotoProgress(false)
            }
        }).start()
    }

    protected fun initBottomToolbar() {
        (bottomTitle as TextView).text = if (isLandScape()) "x${photoContainer.childCount.minus(1)}" else getString(R.string.attached_photo_count, photoContainer.childCount.minus(1))
    }

    protected fun selectFeelingSymbol(index: Int) {
        mSelectedItemPosition = index
        when (mSelectedItemPosition == 0) {
            true -> symbolText.visibility = View.VISIBLE
            false -> symbolText.visibility = View.GONE
        }
        FlavorUtils.initWeatherView(this, symbol, mSelectedItemPosition, false)
    }

    protected fun initDateTime() {
        intent?.run {
            if (hasExtra(DiaryInsertActivity.INITIALIZE_TIME_MILLIS)) {
                mCurrentTimeMillis = getLongExtra(DiaryInsertActivity.INITIALIZE_TIME_MILLIS, 0)
            }
        }

        if (mCurrentTimeMillis > 0) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = mCurrentTimeMillis
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH).plus(1)
            mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
            mSecond = calendar.get(Calendar.SECOND)
        }
    }

    protected fun restoreContents(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            mPhotoUris.clear()
            val attachView = photoContainer.getChildAt(photoContainer.childCount.minus(1))
            photoContainer.removeAllViews()
            photoContainer.addView(attachView)

            getStringArrayList(LIST_URI_STRING)?.map { uriString ->
                mPhotoUris.add(PhotoUriDto(uriString))
            }
            mYear = getInt(SELECTED_YEAR, mYear)
            mMonth = getInt(SELECTED_MONTH, mMonth)
            mDayOfMonth = getInt(SELECTED_DAY, mDayOfMonth)
            mHourOfDay = getInt(SELECTED_HOUR, mHourOfDay)
            mMinute = getInt(SELECTED_MINUTE, mMinute)
            mSecond = getInt(SELECTED_SECOND, mSecond)

            mPhotoUris.forEachIndexed { index, photoUriDto ->
                val imageView = when (isLandScape()) {
                    true -> EasyDiaryUtils.createAttachedPhotoView(this@EditActivity, photoUriDto, 0F, 0F, 0F, 3F)
                    false -> EasyDiaryUtils.createAttachedPhotoView(this@EditActivity, photoUriDto, 0F, 0F, 3F, 0F)
                }
                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
            
            selectFeelingSymbol(getInt(SYMBOL_SEQUENCE, 0))
        }
    }

    protected fun initData(diaryDto: DiaryDto) {
        val attachedPhotos = photoContainer.childCount
        if (config.enableDebugMode) makeToast("attachedPhotos: $attachedPhotos")
        if (attachedPhotos > 1) {
            for (i in attachedPhotos downTo 2) {
                photoContainer.removeViewAt(i.minus(2))
            }
        }
        mPhotoUris.clear()

        if (diaryDto.isAllDay) {
            allDay.isChecked = true
            toggleTimePickerTool()
        }

        val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
        when (encryptionPass == null) {
            true -> {
                diaryTitle.setText(diaryDto.title)
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                diaryContents.setText(diaryDto.contents)
            }
            false -> {
                diaryTitle.setText(JasyptUtils.decrypt(diaryDto.title ?: "", encryptionPass))
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                diaryContents.setText(JasyptUtils.decrypt(diaryDto.contents ?: "", encryptionPass))
            }
        }

        mCurrentTimeMillis = diaryDto.currentTimeMillis
        if (config.holdPositionEnterEditScreen) {
            Handler().post {
                contentsContainer.scrollY = intent.getIntExtra(DIARY_CONTENTS_SCROLL_Y, 0) - (feelingSymbolButton.parent.parent as ViewGroup).measuredHeight
            }
        } else {
            diaryContents.requestFocus()
        }

        // TODO fixme elegance
        diaryDto.photoUris?.let {
            mPhotoUris.addAll(it)
        }

        mPhotoUris.let {
            val thumbnailSize = config.settingThumbnailSize
            it.forEachIndexed { index, photoUriDto ->
                val imageView = when (isLandScape()) {
                    true -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 0F, 3F)
                    false -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 3F, 0F)
                }

                imageView.setOnClickListener(PhotoClickListener(index))
                photoContainer.addView(imageView, photoContainer.childCount - 1)
            }
        }

//        initSpinner()
        selectFeelingSymbol(diaryDto.weather)
        if (config.enableLocationInfo) {
//            locationLabel.setTextColor(config.textColor)
//            locationContainer.background = getLabelBackground()
            diaryDto.location?.let {
                locationContainer.visibility = View.VISIBLE
                locationLabel.text = it.address
                mLocation = it
            } ?: {
                setLocationInfo()
                mLocation?.let {
                    locationContainer.visibility = View.VISIBLE
                    locationLabel.text = it.address
                }
            } ()
        }
    }


    /***************************************************************************************************
     *   abstract functions
     *
     ***************************************************************************************************/
    abstract fun setVisiblePhotoProgress(isVisible: Boolean)
    

    /***************************************************************************************************
     *   inner class
     *
     ***************************************************************************************************/
    inner class PhotoClickListener(var index: Int) : View.OnClickListener {
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

    companion object {
        const val FOCUS_TITLE = 0
        const val FOCUS_CONTENTS = 1
        const val DIARY_SEQUENCE_TEMPORARY = -1
        const val DIARY_SEQUENCE_INIT = 0
        const val DIARY_ORIGIN_SEQUENCE_INIT = 0
    }
}