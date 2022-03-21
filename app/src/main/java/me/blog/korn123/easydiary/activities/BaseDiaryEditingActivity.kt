package me.blog.korn123.easydiary.activities

import android.Manifest
import android.app.Activity
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
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.werb.pickphotoview.PickPhotoViewEx
import com.werb.pickphotoview.util.PickConfig
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.DateUtils
import io.realm.RealmList
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDiaryEditingBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.models.PhotoUri
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.text.ParseException
import java.util.*

abstract class BaseDiaryEditingActivity : EasyDiaryActivity() {

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
    private val mRequestSpeechInput = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        pauseLock()
        when (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
            true -> {
                mBinding.partialEditContents.run {
                    activityResult.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                        if (mCurrentCursor == FOCUS_TITLE) { // edit title
                            val title = diaryTitle.text.toString()
                            val sb = StringBuilder(title)
                            sb.insert(diaryTitle.selectionStart, it[0])
                            val cursorPosition = diaryTitle.selectionStart + it[0].length
                            diaryTitle.setText(sb.toString())
                            diaryTitle.setSelection(cursorPosition)
                        } else {                   // edit contents
                            val contents = diaryContents.text.toString()
                            val sb = StringBuilder(contents)
                            sb.insert(diaryContents.selectionStart, it[0])
                            val cursorPosition = diaryContents.selectionStart + it[0].length
                            diaryContents.setText(sb.toString())
                            diaryContents.setSelection(cursorPosition)
                        }
                    }
                }
            }
            false -> {}
        }
    }
    private val mRequestImagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pauseLock()
        if (it.resultCode == Activity.RESULT_OK && it.data != null) attachPhotos(arrayListOf(it.data!!.data.toString()), true)
    }
    private val mRequestCaptureCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pauseLock()
        if (it.resultCode == Activity.RESULT_OK) attachPhotos(arrayListOf(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + CAPTURE_CAMERA_FILE_NAME), false)
    }
    private val mRequestPickPhotoData = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pauseLock()
        it?.data?.let { data ->
            val selectedUriPaths = data.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT) as java.util.ArrayList<String>
            attachPhotos(selectedUriPaths, true)
        }
    }
    protected lateinit var mBinding: ActivityBaseDiaryEditingBinding
    protected val mPhotoUris: RealmList<PhotoUri> = RealmList()
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
                mRequestCaptureCamera.launch(intent)
            }
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
        mBinding = ActivityBaseDiaryEditingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        changeDrawableIconColor(Color.WHITE, R.drawable.ic_calendar_4_w)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_base_diary_editing, menu)
        menu.findItem(R.id.timePicker).isVisible = mEnableTimePicker
        menu.findItem(R.id.secondsPicker).isVisible = mEnableSecondsPicker
        return true
    }

    abstract fun saveContents()
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                showAlertDialog(getString(R.string.back_pressed_confirm),
                        DialogInterface.OnClickListener { _, _ -> super.onBackPressed() },
                        null
                )
            R.id.saveContents -> saveContents()
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

    override fun onResume() {
        super.onResume()
        toggleTimePickerTool()
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
            mRequestSpeechInput.launch(mRecognizerIntent)
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
                PickPhotoViewEx.Builder(this)
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
                        .start(mRequestPickPhotoData)
            }
            false -> {
                val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //                pickIntent.setType("image/*");
                try {
                    mRequestImagePicker.launch(pickImageIntent)
                } catch (e: ActivityNotFoundException) {
                    showAlertDialog(getString(R.string.gallery_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
                }
            }
        }
    }

    protected fun saveTemporaryDiary(originSequence: Int) {
        val diaryTemp = Diary(
                DIARY_SEQUENCE_INIT,
                mCurrentTimeMillis,
                mBinding.partialEditContents.diaryTitle.text.toString(),
                mBinding.partialEditContents.diaryContents.text.toString(),
                mSelectedItemPosition,
                mBinding.partialEditContents.allDay.isChecked
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
            showAlertDialog(
                    getString(R.string.load_auto_save_diary_title),
                    getString(R.string.load_auto_save_diary_description),
                    { _, _ ->
                        initData(it)
                        initBottomToolbar()
                        EasyDiaryDbHelper.deleteTemporaryDiaryBy(DIARY_SEQUENCE_TEMPORARY)
                    },
                    { _, _ -> EasyDiaryDbHelper.deleteDiaryBy(DIARY_SEQUENCE_TEMPORARY) }, false
            )
        }
    }

    protected fun addTextWatcher() {
        mBinding.partialEditContents.run {
            if (config.enableCountCharacters) {
                contentsLength.run {
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
    }

    protected fun toggleSimpleLayout() {
        mBinding.partialEditContents.partialEditPhotoContainer.run {
            when (photoContainerScrollView.visibility) {
                View.VISIBLE -> {
                    photoContainerScrollView.visibility = View.GONE
//                    mBinding.partialEditContents.titleCard.visibility = View.VISIBLE
                    mBinding.partialEditContents.partialBottomToolbar.togglePhoto.setImageDrawable(ContextCompat.getDrawable(this@BaseDiaryEditingActivity, R.drawable.ic_expand))
//                    supportActionBar?.hide()
                }
                View.GONE -> {
                    photoContainerScrollView.visibility = View.VISIBLE
//                    mBinding.partialEditContents.titleCard.visibility = View.GONE
                    mBinding.partialEditContents.partialBottomToolbar.togglePhoto.setImageDrawable(ContextCompat.getDrawable(this@BaseDiaryEditingActivity, R.drawable.ic_collapse))
//                    supportActionBar?.show()
                }
                else -> {}
            }
        }
    }

    protected fun setLocationInfo() {
        if (config.enableLocationInfo) {
            mBinding.partialEditContents.run {
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
                } ?: run {
                    makeToast(getString(R.string.location_info_error_message))
                }
                locationProgress.visibility = View.GONE
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

    var mEnableTimePicker = true
    var mEnableSecondsPicker = true

    protected fun toggleTimePickerTool() {
        mBinding.run {
            when (partialEditContents.allDay.isChecked) {
                true -> {
                    mEnableTimePicker = false
                    mEnableSecondsPicker = false
                    mHourOfDay = 0
                    mMinute = 0
                    mSecond = 0
                }
                false -> {
                    mEnableTimePicker = true
                    mEnableSecondsPicker = true
                }
            }
            setDateTime()
            invalidateOptionsMenu()
        }
    }

    protected fun setupPhotoView() {
        val thumbnailSize = config.settingThumbnailSize
        val imageXY = CommonUtils.dpToPixel(applicationContext, thumbnailSize)
        val layoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
        mBinding.partialEditContents.partialEditPhotoContainer.run {
            photoView.run {
                setLayoutParams(layoutParams)
                background = createBackgroundGradientDrawable(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL)
            }
            captureCamera.run {
                setLayoutParams(layoutParams)
                background = createBackgroundGradientDrawable(config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL)
            }
        }
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
//            supportActionBar?.run {
//                title = DateUtils.getDateStringFromTimeMillis(mCurrentTimeMillis)
//                subtitle = if (mBinding.partialEditContents.allDay.isChecked) "No time information" else DateUtils.timeMillisToDateTime(mCurrentTimeMillis, DateUtils.TIME_PATTERN_WITH_SECONDS)
//            }
            mBinding.partialEditContents.date.text = when (mBinding.partialEditContents.allDay.isChecked) {
                true -> DateUtils.getDateStringFromTimeMillis(mCurrentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTimeAndSeconds(mCurrentTimeMillis)
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
                    val photoUriDto = PhotoUri(FILE_URI_PREFIX + photoPath, mimeType)
                    mPhotoUris.add(photoUriDto)
                    val currentIndex = mPhotoUris.size - 1
                    runOnUiThread {
                        val imageView = when (isLandScape()) {
                            true -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 0F, 3F)
                            false -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 3F, 0F)
                        }
                        imageView.setOnClickListener(PhotoClickListener(currentIndex))
                        mBinding.partialEditContents.partialEditPhotoContainer.run {
                            photoContainer.addView(imageView, photoContainer.childCount - 1)
                        }
                        initBottomToolbar()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            runOnUiThread {
                mBinding.partialEditContents.partialEditPhotoContainer.run {
                    when (isLandScape()) {
                        true -> photoContainer.postDelayed({ (photoContainerScrollView.getChildAt(0) as ScrollView).fullScroll(ScrollView.FOCUS_DOWN) }, 100L)
                        false -> photoContainer.postDelayed({ (photoContainerScrollView.getChildAt(0) as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                    }
                }
                setVisiblePhotoProgress(false)
            }
        }).start()
    }

    protected fun initBottomToolbar() {
        mBinding.partialEditContents.partialEditPhotoContainer.run {
            mBinding.partialEditContents.partialBottomToolbar.bottomTitle.text = if (isLandScape()) "x${photoContainer.childCount.minus(1)}" else getString(R.string.attached_photo_count, photoContainer.childCount.minus(1))
        }
    }

    protected fun selectFeelingSymbol(index: Int) {
        mSelectedItemPosition = index
        when (mSelectedItemPosition == 0) {
            true -> mBinding.partialEditContents.symbolText.visibility = View.VISIBLE
            false -> mBinding.partialEditContents.symbolText.visibility = View.GONE
        }
        FlavorUtils.initWeatherView(this, mBinding.partialEditContents.symbol, mSelectedItemPosition, false)
    }

    protected fun initDateTime() {
        intent?.run {
            if (hasExtra(DiaryWritingActivity.INITIALIZE_TIME_MILLIS)) {
                mCurrentTimeMillis = getLongExtra(DiaryWritingActivity.INITIALIZE_TIME_MILLIS, 0)
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
            mBinding.partialEditContents.partialEditPhotoContainer.run {
                mPhotoUris.clear()
                val attachView = photoContainer.getChildAt(photoContainer.childCount.minus(1))
                photoContainer.removeAllViews()
                photoContainer.addView(attachView)

                getStringArrayList(LIST_URI_STRING)?.map { uriString ->
                    mPhotoUris.add(PhotoUri(uriString))
                }
                mYear = getInt(SELECTED_YEAR, mYear)
                mMonth = getInt(SELECTED_MONTH, mMonth)
                mDayOfMonth = getInt(SELECTED_DAY, mDayOfMonth)
                mHourOfDay = getInt(SELECTED_HOUR, mHourOfDay)
                mMinute = getInt(SELECTED_MINUTE, mMinute)
                mSecond = getInt(SELECTED_SECOND, mSecond)

                mPhotoUris.forEachIndexed { index, photoUriDto ->
                    val imageView = when (isLandScape()) {
                        true -> EasyDiaryUtils.createAttachedPhotoView(this@BaseDiaryEditingActivity, photoUriDto, 0F, 0F, 0F, 3F)
                        false -> EasyDiaryUtils.createAttachedPhotoView(this@BaseDiaryEditingActivity, photoUriDto, 0F, 0F, 3F, 0F)
                    }
                    imageView.setOnClickListener(PhotoClickListener(index))
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                }

                selectFeelingSymbol(getInt(SYMBOL_SEQUENCE, 0))
            }
        }
    }

    protected fun initData(diary: Diary) {
        // When checkTemporaryDiary is called in edit mode, the already loaded attached photo must be cleared.
        // Start clearing
        val attachedPhotos = mBinding.partialEditContents.partialEditPhotoContainer.photoContainer.childCount
        if (config.enableDebugMode) makeToast("attachedPhotos: $attachedPhotos, ${mPhotoUris.size}")
        if (attachedPhotos > 1) {
            for (i in attachedPhotos downTo 2) {
                mBinding.partialEditContents.partialEditPhotoContainer.photoContainer.removeViewAt(i.minus(2))
            }
        }
        mPhotoUris.clear()
        // End clearing

        if (diary.isAllDay) {
            mBinding.partialEditContents.allDay.isChecked = true
            toggleTimePickerTool()
        }

        val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
        when (encryptionPass == null) {
            true -> {
                mBinding.partialEditContents.diaryTitle.setText(diary.title)
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                mBinding.partialEditContents.diaryContents.setText(diary.contents)
            }
            false -> {
                mBinding.partialEditContents.diaryTitle.setText(JasyptUtils.decrypt(diary.title ?: "", encryptionPass))
                //        getSupportActionBar().setSubtitle(DateUtils.getFullPatternDateWithTime(diaryDto.getCurrentTimeMillis()));
                mBinding.partialEditContents.diaryContents.setText(JasyptUtils.decrypt(diary.contents ?: "", encryptionPass))
            }
        }

        mCurrentTimeMillis = diary.currentTimeMillis
        if (config.holdPositionEnterEditScreen) {
            Handler().post {
                mBinding.partialEditContents.contentsContainer.scrollY = intent.getIntExtra(DIARY_CONTENTS_SCROLL_Y, 0) - (mBinding.partialEditContents.feelingSymbolButton.parent.parent as ViewGroup).measuredHeight
            }
        } else {
            mBinding.partialEditContents.diaryContents.requestFocus()
        }

        // TODO fixme elegance
        diary.photoUris?.let {
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
                mBinding.partialEditContents.partialEditPhotoContainer.run {
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                }
            }
        }

//        initSpinner()
        selectFeelingSymbol(diary.weather)
        if (config.enableLocationInfo) {
//            locationLabel.setTextColor(config.textColor)
//            locationContainer.background = getLabelBackground()
            diary.location?.let {
                mBinding.partialEditContents.locationContainer.visibility = View.VISIBLE
                mBinding.partialEditContents.locationLabel.text = it.address
                mLocation = it
            } ?: run {
                setLocationInfo()
                mLocation?.let {
                    mBinding.partialEditContents.locationContainer.visibility = View.VISIBLE
                    mBinding.partialEditContents.locationLabel.text = it.address
                }
            }
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
                    { _, _ ->
                        mRemoveIndexes.add(targetIndex)
                        mBinding.partialEditContents.partialEditPhotoContainer.photoContainer.removeView(v)
                        initBottomToolbar()
                    },
                    { _, _ -> }
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