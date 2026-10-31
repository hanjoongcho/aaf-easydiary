package me.blog.korn123.easydiary.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.werb.pickphotoview.util.PickConfig
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDiaryEditingBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.applyBottomImeInsets
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.createTemporaryPhotoFile
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.fullAddress
import me.blog.korn123.easydiary.extensions.getFromLocation
import me.blog.korn123.easydiary.extensions.getLastKnownLocation
import me.blog.korn123.easydiary.extensions.getUriForFile
import me.blog.korn123.easydiary.extensions.isAccessFromOutside
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.startMainActivityWithClearTask
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_MONDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SATURDAY
import me.blog.korn123.easydiary.helper.CALENDAR_START_DAY_SUNDAY
import me.blog.korn123.easydiary.helper.CAPTURE_CAMERA_FILE_NAME
import me.blog.korn123.easydiary.helper.DIARY_CONTENTS_SCROLL_Y
import me.blog.korn123.easydiary.helper.DIARY_ENCRYPT_PASSWORD
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.LIST_URI_STRING
import me.blog.korn123.easydiary.helper.MIME_TYPE_JPEG
import me.blog.korn123.easydiary.helper.PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE
import me.blog.korn123.easydiary.helper.SELECTED_DAY
import me.blog.korn123.easydiary.helper.SELECTED_HOUR
import me.blog.korn123.easydiary.helper.SELECTED_MINUTE
import me.blog.korn123.easydiary.helper.SELECTED_MONTH
import me.blog.korn123.easydiary.helper.SELECTED_SECOND
import me.blog.korn123.easydiary.helper.SELECTED_YEAR
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.SettingConstants
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.easydiary.utils.JasyptUtils
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale
import java.util.UUID
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain
import me.blog.korn123.easydiary.domain.model.Location as LocationDomain
import me.blog.korn123.easydiary.domain.model.PhotoUri as PhotoUriDomain

abstract class BaseDiaryEditingActivity : EasyDiaryActivity() {
    /***************************************************************************************************
     *   Global properties
     ***************************************************************************************************/
    private lateinit var recognizerIntent: Intent
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var secondsPickerDialog: AlertDialog
    private val calendarInstance = Calendar.getInstance(Locale.getDefault())
    private val removeIndexes = ArrayList<Int>()
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val networkLocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("Network location has been updated")
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }
    private val gpsLocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("GPS location has been updated")
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }
    private val requestSpeechInput =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            pauseLock()
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                binding.partialEditContents.run {
                    activityResult.data
                        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.let { results ->
                            if (currentCursor == DiaryEditingConstants.FOCUS_TITLE) {
                                val title = diaryTitle.text.toString()
                                val sb = StringBuilder(title)
                                sb.insert(diaryTitle.selectionStart, results[0])
                                val cursorPosition = diaryTitle.selectionStart + results[0].length
                                diaryTitle.setText(sb.toString())
                                diaryTitle.setSelection(cursorPosition)
                            } else {
                                val contents = diaryContents.text.toString()
                                val sb = StringBuilder(contents)
                                sb.insert(diaryContents.selectionStart, results[0])
                                val cursorPosition = diaryContents.selectionStart + results[0].length
                                diaryContents.setText(sb.toString())
                                diaryContents.setSelection(cursorPosition)
                            }
                        } ?: run {
                        showAlertDialog(getString(R.string.recognizer_intent_not_found_message))
                    }
                }
            }
        }
    private val requestImagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                result.data?.data?.toString()?.let { uriString ->
                    attachPhotos(arrayListOf(uriString), true)
                }
            }
        }
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            pauseLock()
            if (uris.isNotEmpty()) {
                attachPhotos(ArrayList(uris.map { uri -> uri.toString() }), true)
            } else {
                showAlertDialog("No selected photos.")
            }
        }
    val requestCaptureCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            if (result.resultCode == Activity.RESULT_OK) {
                attachPhotos(
                    arrayListOf(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + CAPTURE_CAMERA_FILE_NAME),
                    false,
                )
            }
        }
    private val requestPickPhotoData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            result?.data?.let { data ->
                @Suppress("UNCHECKED_CAST")
                val selectedUriPaths = data.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT) as? java.util.ArrayList<String>
                selectedUriPaths?.let { attachPhotos(it, true) }
            }
        }
    protected lateinit var binding: ActivityBaseDiaryEditingBinding
    protected val photoUris: ArrayList<PhotoUriDomain> = ArrayList()
    protected var currentTimeMillis: Long = 0
    protected var year = calendarInstance.get(Calendar.YEAR)
    protected var location: LocationDomain? = null
    protected var symbolSequence = 0
    protected var isDiarySaved = false

    protected var linkedDiaries: List<Long> = arrayListOf()

    /**
     * month is not Calendar.MONTH
     * month range is 1 ~ 12
     */
    var month = calendarInstance.get(Calendar.MONTH).plus(1)
    var dayOfMonth = calendarInstance.get(Calendar.DAY_OF_MONTH)
    var hourOfDay = calendarInstance.get(Calendar.HOUR_OF_DAY)
    var minute = calendarInstance.get(Calendar.MINUTE)
    var second = calendarInstance.get(Calendar.SECOND)
    var selectedItemPosition = 0
    var currentCursor = 0

    private var startDateListener: DatePickerDialog.OnDateSetListener =
        DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            year = selectedYear
            month = selectedMonth + 1
            dayOfMonth = selectedDayOfMonth
            setDateTime()
        }

    private var timeSetListener: TimePickerDialog.OnTimeSetListener =
        TimePickerDialog.OnTimeSetListener { _, selectedHourOfDay, selectedMinute ->
            hourOfDay = selectedHourOfDay
            minute = selectedMinute
            setDateTime()
        }

    val clickListener =
        View.OnClickListener { view ->
            hideSoftInputFromWindow()

            when (view.id) {
                R.id.photoView -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        if (checkPermission(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))) {
//                            callImagePicker()
//                        } else {
//                            confirmPermission(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_EXTERNAL_STORAGE)
//                        }
                        callImagePicker()
                    } else {
                        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            callImagePicker()
                        } else {
                            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                        }
                    }
                }

                R.id.captureCamera -> {
                    val captureFile = createTemporaryPhotoFile()
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(captureFile))
                    requestCaptureCamera.launch(intent)
                }

                R.id.locationContainer -> {
                    setLocationInfo()
                }
            }
        }

    val touchListener =
        View.OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> view.performClick()
            }

            currentCursor = if (view.id == R.id.diaryTitle) DiaryEditingConstants.FOCUS_TITLE else DiaryEditingConstants.FOCUS_CONTENTS
            false
        }

    /***************************************************************************************************
     *   Override functions
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseDiaryEditingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeDrawableIconColor(Color.WHITE, R.drawable.ic_calendar_4_w)

        if (config.enableLocationInfo &&
            checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        ) {
            locationManager.run {
                if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, gpsLocationListener)
                }
                if (isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, networkLocationListener)
                }
            }
        }

        if (isLandScape()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        applyBottomImeInsets(binding.partialEditContents.root)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmFinish()
                }
            },
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        applyRemoveIndex()
        outState.run {
            val listUriString = arrayListOf<String>()
            photoUris.map { model ->
                listUriString.add(model.photoUri ?: "")
            }
            putStringArrayList(LIST_URI_STRING, listUriString)
            putInt(SELECTED_YEAR, year)
            putInt(SELECTED_MONTH, month)
            putInt(SELECTED_DAY, dayOfMonth)
            putInt(SELECTED_HOUR, hourOfDay)
            putInt(SELECTED_MINUTE, minute)
            putInt(SELECTED_SECOND, second)
            putInt(SYMBOL_SEQUENCE, selectedItemPosition)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_base_diary_editing, menu)
        menu.findItem(R.id.timePicker).isVisible = enableTimePicker
        menu.findItem(R.id.secondsPicker).isVisible = enableSecondsPicker
        return true
    }

    abstract fun saveContents()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                confirmFinish()
            }

            R.id.saveContents -> {
                saveContents()
            }

            R.id.datePicker -> {
                datePickerDialog.show()
            }

            R.id.timePicker -> {
                timePickerDialog.show()
            }

            R.id.secondsPicker -> {
                val itemClickListener =
                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                        val itemMap = parent.adapter.getItem(position) as HashMap<String, String>
                        itemMap["value"]?.let {
                            second = it.toInt()
                            setDateTime()
                        }
                        secondsPickerDialog.cancel()
                    }
                val builder = EasyDiaryUtils.createSecondsPickerBuilder(this, itemClickListener, second)
                secondsPickerDialog = builder.create()
                secondsPickerDialog.show()
            }

            R.id.microphone -> {
                showSpeechDialog()
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> {
                val permissionsToCheck =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                    } else {
                        EXTERNAL_STORAGE_PERMISSIONS
                    }
                if (checkPermission(permissionsToCheck)) {
                    callImagePicker()
                } else {
                    makeSnackBar(
                        findViewById(android.R.id.content),
                        getString(R.string.guide_message_3),
                    )
                }
            }

            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        toggleTimePickerTool()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (config.enableLocationInfo) {
            locationManager.run {
                removeUpdates(gpsLocationListener)
                removeUpdates(networkLocationListener)
            }
        }
    }

    /***************************************************************************************************
     *   Etc functions
     ***************************************************************************************************/
    private fun showSpeechDialog() {
        try {
            requestSpeechInput.launch(recognizerIntent)
        } catch (e: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message), { _, _ -> })
        }
    }

    private fun callImagePicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            try {
                requestImagePicker.launch(pickImageIntent)
            } catch (e: ActivityNotFoundException) {
                showAlertDialog(getString(R.string.gallery_intent_not_found_message), { _, _ -> })
            }
        } else {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun confirmFinish() {
        showAlertDialog(
            getString(R.string.back_pressed_confirm),
            { _, _ ->
                if (isAccessFromOutside()) {
                    startMainActivityWithClearTask()
                } else {
                    finishActivityWithPauseLock()
                }
            },
            { _, _ -> },
            DialogMode.INFO,
        )
    }

    protected suspend fun saveTemporaryDiary(originSequence: Long) {
        val diaryTemp =
            DiaryDomain(
                currentTimeMillis = currentTimeMillis,
                title =
                    binding.partialEditContents.diaryTitle.text
                        .toString(),
                contents =
                    binding.partialEditContents.diaryContents.text
                        .toString(),
                symbolSequence = selectedItemPosition,
                isAllDay = binding.partialEditContents.allDay.isChecked,
                originDiaryId = originSequence,
                photoUris = photoUris.map { it },
            )
        if (diaryTemp.title?.isNotEmpty() == true ||
            diaryTemp.contents.isNotEmpty() || diaryTemp.photoUris.isNotEmpty()
        ) {
            diaryRepository.insertTemporaryDiary(
                location?.let { diaryTemp.copy(location = it) } ?: diaryTemp,
            )
        }
    }

    protected fun checkTemporaryDiary(originSequence: Long) {
        lifecycleScope.launch {
            diaryViewModel.findTemporaryDiaryBy(originSequence)?.let { diary ->
                showAlertDialog(
                    getString(R.string.load_auto_save_diary_description),
                    { _, _ ->
                        lifecycleScope.launch {
                            initData(diary)
                            initBottomToolbar()
                            diaryRepository.deleteTemporaryDiaryByOriginId(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
                        }
                    },
                    { _, _ ->
                        lifecycleScope.launch {
                            diaryRepository.deleteDiaryById(DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY)
                        }
                    },
                    DialogMode.INFO,
                    false,
                    getString(R.string.load_auto_save_diary_title),
                )
            }
        }
    }

    protected fun addTextWatcher() {
        binding.partialEditContents.run {
            if (config.enableCountCharacters) {
                contentsLength.run {
                    text = getString(R.string.diary_contents_length, 0)
                }
                diaryContents.addTextChangedListener(
                    object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {}

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {
                            contentsLength.text = getString(R.string.diary_contents_length, s?.length ?: 0)
                        }
                    },
                )
                contentsLengthContainer.visibility = View.VISIBLE
            } else {
                contentsLengthContainer.visibility = View.GONE
            }
        }
    }

    protected fun toggleSimpleLayout() {
        binding.partialEditContents.partialEditPhotoContainer.run {
            when (photoContainerScrollView.visibility) {
                View.VISIBLE -> {
                    photoContainerScrollView.visibility = View.GONE
                    binding.partialEditContents.partialBottomToolbar.togglePhoto.setImageDrawable(
                        ContextCompat.getDrawable(this@BaseDiaryEditingActivity, R.drawable.ic_expand),
                    )
                }

                View.GONE -> {
                    photoContainerScrollView.visibility = View.VISIBLE
                    binding.partialEditContents.partialBottomToolbar.togglePhoto.setImageDrawable(
                        ContextCompat.getDrawable(this@BaseDiaryEditingActivity, R.drawable.ic_collapse),
                    )
                }

                else -> {}
            }
        }
    }

    protected fun setLocationInfo() {
        if (config.enableLocationInfo) {
            binding.partialEditContents.run {
                locationProgress.visibility = View.VISIBLE
                getLastKnownLocation()?.let { knownLocation ->
                    var locationInfo: String? = null
                    getFromLocation(knownLocation.latitude, knownLocation.longitude, 1)?.let { address ->
                        if (address.isNotEmpty()) {
                            locationInfo = fullAddress(address[0])
                            location = LocationDomain(locationInfo, knownLocation.latitude, knownLocation.longitude)
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
        Collections.sort(removeIndexes, Collections.reverseOrder())
        for (index in removeIndexes) {
            photoUris.removeAt(index)
        }
        removeIndexes.clear()
    }

    var enableTimePicker = true
    var enableSecondsPicker = true

    protected fun toggleTimePickerTool() {
        binding.run {
            when (partialEditContents.allDay.isChecked) {
                true -> {
                    enableTimePicker = false
                    enableSecondsPicker = false
                    hourOfDay = 0
                    minute = 0
                    second = 0
                }

                false -> {
                    enableTimePicker = true
                    enableSecondsPicker = true
                }
            }
            setDateTime()
            invalidateOptionsMenu()
        }
    }

    protected fun setupPhotoView() {
        val thumbnailSize = config.settingThumbnailSize
        val imageXY = applicationContext.dpToPixel(thumbnailSize)
        val imageLayoutParams = LinearLayout.LayoutParams(imageXY, imageXY)
        binding.partialEditContents.partialEditPhotoContainer.run {
            photoView.run {
                layoutParams = imageLayoutParams
                background =
                    createBackgroundGradientDrawable(
                        config.primaryColor,
                        THUMBNAIL_BACKGROUND_ALPHA,
                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                    )
            }
            captureCamera.run {
                layoutParams = imageLayoutParams
                background =
                    createBackgroundGradientDrawable(
                        config.primaryColor,
                        THUMBNAIL_BACKGROUND_ALPHA,
                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                    )
            }
        }
    }

    protected fun setupRecognizer() {
        recognizerIntent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
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
        datePickerDialog =
            DatePickerDialog(this, startDateListener, year, month - 1, dayOfMonth).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    datePicker.firstDayOfWeek =
                        when (config.calendarStartDay) {
                            CALENDAR_START_DAY_MONDAY -> Calendar.MONDAY
                            CALENDAR_START_DAY_SATURDAY -> Calendar.SATURDAY
                            else -> CALENDAR_START_DAY_SUNDAY
                        }
                }
            }
        timePickerDialog = TimePickerDialog(this, timeSetListener, hourOfDay, minute, DateFormat.is24HourFormat(this))
    }

    protected fun setDateTime() {
        try {
            currentTimeMillis =
                EasyDiaryUtils.datePickerToTimeMillis(
                    dayOfMonth,
                    month - 1,
                    year,
                    false,
                    hourOfDay,
                    minute,
                    second,
                )
            binding.partialEditContents.date.text =
                when (binding.partialEditContents.allDay.isChecked) {
                    true -> DateUtils.getDateStringFromTimeMillis(currentTimeMillis)
                    false -> DateUtils.getDateTimeStringFromTimeMillis(currentTimeMillis, SimpleDateFormat.FULL, SimpleDateFormat.MEDIUM)
                }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    protected fun attachPhotos(
        selectPaths: ArrayList<String>,
        isUriString: Boolean,
    ) {
        setVisiblePhotoProgress(true)
        Thread(
            Runnable {
                selectPaths.map { item ->
                    val photoPath = EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                    try {
                        val mimeType: String =
                            when (isUriString) {
                                true -> {
                                    EasyDiaryUtils.downSamplingImage(this, Uri.parse(item), File(photoPath))
                                }

                                false -> {
                                    EasyDiaryUtils.downSamplingImage(this, File(item), File(photoPath))
                                    MIME_TYPE_JPEG
                                }
                            }
                        val photoUriDto = PhotoUriDomain(FILE_URI_PREFIX + photoPath, mimeType)
                        photoUris.add(photoUriDto)
                        val currentIndex = photoUris.size - 1
                        runOnUiThread {
                            val imageView =
                                when (isLandScape()) {
                                    true -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 0F, 3F)
                                    false -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 3F, 0F)
                                }
                            imageView.setOnClickListener(PhotoClickListener(currentIndex))
                            binding.partialEditContents.partialEditPhotoContainer.run {
                                photoContainer.addView(imageView, photoContainer.childCount - 1)
                            }
                            initBottomToolbar()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                runOnUiThread {
                    binding.partialEditContents.partialEditPhotoContainer.run {
                        when (isLandScape()) {
                            true -> {
                                photoContainer.postDelayed(
                                    { (photoContainerScrollView.getChildAt(0) as ScrollView).fullScroll(ScrollView.FOCUS_DOWN) },
                                    100L,
                                )
                            }

                            false -> {
                                photoContainer.postDelayed({
                                    (
                                        photoContainerScrollView.getChildAt(
                                            0,
                                        ) as HorizontalScrollView
                                    ).fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                                }, 100L)
                            }
                        }
                    }
                    setVisiblePhotoProgress(false)
                }
            },
        ).start()
    }

    protected fun initBottomToolbar() {
        binding.partialEditContents.partialEditPhotoContainer.run {
            binding.partialEditContents.partialBottomToolbar.bottomTitle.text =
                if (isLandScape()) {
                    "x${photoContainer.childCount.minus(1)}"
                } else {
                    getString(R.string.attached_photo_count, photoContainer.childCount.minus(1))
                }
        }
    }

    protected fun selectFeelingSymbol(index: Int) {
        selectedItemPosition = index
        when (selectedItemPosition == 0) {
            true -> binding.partialEditContents.symbolText.visibility = View.VISIBLE
            false -> binding.partialEditContents.symbolText.visibility = View.GONE
        }
        FlavorUtils.initWeatherView(this, binding.partialEditContents.symbol, selectedItemPosition, false)
    }

    protected fun initDateTime() {
        intent?.run {
            if (hasExtra(SettingConstants.INITIALIZE_TIME_MILLIS)) {
                currentTimeMillis = getLongExtra(SettingConstants.INITIALIZE_TIME_MILLIS, 0)
            }
        }

        if (currentTimeMillis > 0) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = currentTimeMillis
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH).plus(1)
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            minute = calendar.get(Calendar.MINUTE)
            second = calendar.get(Calendar.SECOND)
        }
    }

    protected fun restoreContents(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            binding.partialEditContents.partialEditPhotoContainer.run {
                photoUris.clear()
                val attachView = photoContainer.getChildAt(photoContainer.childCount.minus(1))
                photoContainer.removeAllViews()
                photoContainer.addView(attachView)

                getStringArrayList(LIST_URI_STRING)?.map { uriString ->
                    photoUris.add(PhotoUriDomain(uriString))
                }
                year = getInt(SELECTED_YEAR, year)
                month = getInt(SELECTED_MONTH, month)
                dayOfMonth = getInt(SELECTED_DAY, dayOfMonth)
                hourOfDay = getInt(SELECTED_HOUR, hourOfDay)
                minute = getInt(SELECTED_MINUTE, minute)
                second = getInt(SELECTED_SECOND, second)

                photoUris.forEachIndexed { index, photoUriDto ->
                    val imageView =
                        when (isLandScape()) {
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

    protected fun initData(diary: DiaryDomain) {
        val attachedPhotos = binding.partialEditContents.partialEditPhotoContainer.photoContainer.childCount
        if (config.enableDebugOptionToastAttachedPhoto) makeToast("attachedPhotos: $attachedPhotos, ${photoUris.size}")
        if (attachedPhotos > 1) {
            for (i in attachedPhotos downTo 2) {
                binding.partialEditContents.partialEditPhotoContainer.photoContainer
                    .removeViewAt(i.minus(2))
            }
        }
        photoUris.clear()

        if (diary.isAllDay) {
            binding.partialEditContents.allDay.isChecked = true
            toggleTimePickerTool()
        }

        val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
        when (encryptionPass == null) {
            true -> {
                binding.partialEditContents.diaryTitle.setText(diary.title)
                binding.partialEditContents.diaryContents.setText(diary.contents)
            }

            false -> {
                binding.partialEditContents.diaryTitle.setText(JasyptUtils.decrypt(diary.title ?: "", encryptionPass))
                binding.partialEditContents.diaryContents.setText(JasyptUtils.decrypt(diary.contents ?: "", encryptionPass))
            }
        }

        currentTimeMillis = diary.currentTimeMillis
        if (config.holdPositionEnterEditScreen) {
            Handler(Looper.getMainLooper()).post {
                binding.partialEditContents.contentsContainer.scrollY =
                    intent.getIntExtra(DIARY_CONTENTS_SCROLL_Y, 0) -
                    (binding.partialEditContents.feelingSymbolButton.parent.parent as ViewGroup).measuredHeight
            }
        } else {
            binding.partialEditContents.diaryContents.requestFocus()
        }

        diary.photoUris.let {
            photoUris.addAll(it)
        }

        photoUris.let {
            val thumbnailSize = config.settingThumbnailSize
            it.forEachIndexed { index, photoUriDto ->
                val imageView =
                    when (isLandScape()) {
                        true -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 0F, 3F)
                        false -> EasyDiaryUtils.createAttachedPhotoView(this, photoUriDto, 0F, 0F, 3F, 0F)
                    }

                imageView.setOnClickListener(PhotoClickListener(index))
                binding.partialEditContents.partialEditPhotoContainer.run {
                    photoContainer.addView(imageView, photoContainer.childCount - 1)
                }
            }
        }

        selectFeelingSymbol(diary.symbolSequence)
        if (config.enableLocationInfo) {
            diary.location?.let {
                binding.partialEditContents.locationContainer.visibility = View.VISIBLE
                binding.partialEditContents.locationLabel.text = it.address
                location = it
            } ?: run {
                setLocationInfo()
                location?.let {
                    binding.partialEditContents.locationContainer.visibility = View.VISIBLE
                    binding.partialEditContents.locationLabel.text = it.address
                }
            }
        }
    }

    protected suspend fun isExistEasterEggDiary(allowStoredCnt: Int) =
        symbolSequence == SYMBOL_EASTER_EGG && diaryViewModel
            .findDiary(
                null,
                false,
                0,
                0,
                SYMBOL_EASTER_EGG,
            ).filter { diary -> diary.originDiaryId == DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT }
            .size > allowStoredCnt

    protected fun duplicatedEasterEggWarning() {
        showAlertDialog(
            "A diary set to the easter egg symbol already exists.",
            null,
            null,
            DialogMode.INFO,
        )
    }

    /***************************************************************************************************
     *   Abstract functions
     ***************************************************************************************************/
    abstract fun setVisiblePhotoProgress(isVisible: Boolean)

    /***************************************************************************************************
     *   Inner classes
     ***************************************************************************************************/
    inner class PhotoClickListener(
        private val photoIndex: Int,
    ) : View.OnClickListener {
        override fun onClick(v: View) {
            if (symbolSequence != SYMBOL_EASTER_EGG) {
                showAlertDialog(
                    getString(R.string.delete_photo_confirm_message),
                    { _, _ ->
                        removeIndexes.add(photoIndex)
                        binding.partialEditContents.partialEditPhotoContainer.photoContainer
                            .removeView(v)
                        initBottomToolbar()
                    },
                    { _, _ -> },
                    DialogMode.INFO,
                )
            }
        }
    }
}
