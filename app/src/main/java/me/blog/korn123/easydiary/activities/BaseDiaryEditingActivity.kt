package me.blog.korn123.easydiary.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextUtils
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
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityBaseDiaryEditingBinding
import me.blog.korn123.easydiary.domain.model.TagRules
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.applyBottomImeInsets
import me.blog.korn123.easydiary.extensions.applyTextCursorColor
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.createTemporaryPhotoFile
import me.blog.korn123.easydiary.extensions.dateAnchorTimeMillis
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
import me.blog.korn123.easydiary.extensions.storedTimeFormat
import me.blog.korn123.easydiary.extensions.updateDashboardInnerCard
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
import me.blog.korn123.easydiary.helper.KeyboardIncognitoHelper
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
import me.blog.korn123.easydiary.helper.TagPickerConstants
import me.blog.korn123.easydiary.presentation.sketchpad.SketchpadActivity
import me.blog.korn123.easydiary.presentation.tag.TagPickerActivity
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.DiaryAutoSavePolicy
import me.blog.korn123.easydiary.utils.DiaryDraftSnapshot
import me.blog.korn123.easydiary.utils.DiaryDraftTracker
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createBackgroundGradientDrawable
import me.blog.korn123.easydiary.utils.JasyptUtils
import me.blog.korn123.easydiary.views.FixedCardView
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
import me.blog.korn123.easydiary.domain.model.Tag as TagDomain

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
    private val locationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val networkLocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("Network location has been updated")
                locationManager.removeUpdates(this)
            }
        }
    private val gpsLocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (config.enableDebugOptionToastLocation) makeToast("GPS location has been updated")
                locationManager.removeUpdates(this)
            }
        }
    private val requestSpeechInput =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            pauseLock()
            if (activityResult.resultCode == RESULT_OK && activityResult.data != null) {
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
            if (result.resultCode == RESULT_OK && result.data?.data != null) {
                result.data?.data?.toString()?.let { uriString ->
                    attachPhotos(arrayListOf(uriString), isUriString = true)
                }
            }
        }
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
            pauseLock()
            if (uris.isNotEmpty()) {
                attachPhotos(ArrayList(uris.map { uri -> uri.toString() }), isUriString = true)
            } else {
                showAlertDialog("No selected photos.")
            }
        }
    val requestCaptureCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            if (result.resultCode == RESULT_OK) {
                attachPhotos(
                    arrayListOf(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + CAPTURE_CAMERA_FILE_NAME),
                    false,
                )
            }
        }

    /**
     * Takes the sketch the built-in sketchpad (issue #260) exported as a PNG content URI and feeds it into the
     * same attachment pipeline as a picked photo, so the sketch is stored with the entry and travels with
     * the diary view, the backups and the PDF export.
     */
    private val requestSketchpad =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.toString()?.let { uriString ->
                    attachPhotos(arrayListOf(uriString), isUriString = true)
                }
            }
        }

    private val requestTagPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            pauseLock()
            if (result.resultCode == RESULT_OK) {
                val selectedNames =
                    result.data
                        ?.getStringArrayListExtra(TagPickerConstants.RESULT_SELECTED_TAG_NAMES)
                        .orEmpty()
                applySelectedTagNames(selectedNames)
            }
        }

    protected lateinit var binding: ActivityBaseDiaryEditingBinding
    protected val photoUris: ArrayList<PhotoUriDomain> = ArrayList()

    /**
     * The tags of the entry being edited (issue #270). A tag picked in the picker stays in memory until the
     * entry is saved, so a name the writer creates here becomes a stored tag only together with the entry.
     */
    protected val selectedTags: MutableList<TagDomain> = mutableListOf()
    protected var currentTimeMillis: Long = 0
    protected var year = calendarInstance[Calendar.YEAR]
    protected var location: LocationDomain? = null
    protected var isDiarySaved = false

    /***************************************************************************************************
     *   Draft autosave (issue #261)
     ***************************************************************************************************/
    private val draftTracker = DiaryDraftTracker()
    private var draftOriginSequence: Long = DiaryEditingConstants.DIARY_SEQUENCE_TEMPORARY
    private var isDraftAutoSaveStarted = false
    private var isDraftFinalized = false
    private var draftGeneration = 0
    private var draftDebounceJob: Job? = null
    private var draftPeriodicJob: Job? = null

    /**
     * Serialises every write and delete of the draft of this editor. A delete queued while an insert is
     * still in flight waits for that insert and then removes what it wrote, so a draft cannot come back
     * after the entry was saved or the draft was discarded.
     */
    private val draftWriteMutex = Mutex()

    protected var linkedDiaries: List<Long> = arrayListOf()

    /**
     * month is not Calendar.MONTH
     * month range is 1 ~ 12
     */
    var month = calendarInstance[Calendar.MONTH] + 1
    var dayOfMonth = calendarInstance[Calendar.DAY_OF_MONTH]
    var hourOfDay = calendarInstance[Calendar.HOUR_OF_DAY]
    var minute = calendarInstance[Calendar.MINUTE]
    var second = calendarInstance[Calendar.SECOND]
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

                R.id.sketchpad -> {
                    requestSketchpad.launch(Intent(this, SketchpadActivity::class.java))
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
        applyKeyboardIncognitoPolicy()
        binding.partialEditContents.tagAddButton.setOnClickListener { openTagPicker() }
        renderTagChips()

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
            val listUriString = ArrayList(photoUris.mapNotNull { it.photoUri })
            putStringArrayList(LIST_URI_STRING, listUriString)
            putInt(SELECTED_YEAR, year)
            putInt(SELECTED_MONTH, month)
            putInt(SELECTED_DAY, dayOfMonth)
            putInt(SELECTED_HOUR, hourOfDay)
            putInt(SELECTED_MINUTE, minute)
            putInt(SELECTED_SECOND, second)
            putInt(SYMBOL_SEQUENCE, selectedItemPosition)
            putStringArrayList(TagPickerConstants.SELECTED_TAG_NAMES, ArrayList(selectedTags.map { it.name }))
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
                        (parent.adapter.getItem(position) as? Map<*, *>)?.get("value")?.toString()?.let {
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
        applyDiaryEditorCursorColor()
        applyKeyboardIncognitoPolicy()
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

    /**
     * Paints the caret of title and contents with the text color so that the caret stays visible
     * on every customized theme (#156, #224). The text colors of the screen are refreshed in
     * `EasyDiaryActivity.onResume()` before this runs, so the caret follows the current theme.
     */
    private fun applyDiaryEditorCursorColor() {
        val textColor = config.textColor
        binding.partialEditContents.run {
            diaryTitle.applyTextCursorColor(textColor)
            diaryContents.applyTextCursorColor(textColor)
        }
    }

    private fun applyKeyboardIncognitoPolicy() {
        val keyboardIncognitoEnabled = config.enableKeyboardIncognito
        binding.partialEditContents.run {
            KeyboardIncognitoHelper.applyPolicy(diaryTitle, keyboardIncognitoEnabled)
            KeyboardIncognitoHelper.applyPolicy(diaryContents, keyboardIncognitoEnabled)
        }
    }

    /***************************************************************************************************
     *   Tags (issue #270)
     ***************************************************************************************************/

    /** Opens the Compose tag picker with every stored tag and the ones this editor already holds. */
    private fun openTagPicker() {
        hideSoftInputFromWindow()
        lifecycleScope.launch {
            val storedNames = diaryViewModel.findAllTags().map { it.name }
            val intent =
                Intent(this@BaseDiaryEditingActivity, TagPickerActivity::class.java).apply {
                    putStringArrayListExtra(TagPickerConstants.TAG_NAMES, ArrayList(storedNames))
                    putStringArrayListExtra(
                        TagPickerConstants.SELECTED_TAG_NAMES,
                        ArrayList(selectedTags.map { it.name }),
                    )
                }
            requestTagPicker.launch(intent)
        }
    }

    /**
     * Takes the picker's answer in. The order the writer picked the tags in is kept, and a tag the editor
     * already knew keeps its stored id so saving it does not have to look the tag up again.
     */
    private fun applySelectedTagNames(names: List<String>) {
        val knownTags = selectedTags.associateBy { TagRules.normalize(it.name) }
        val cleanedNames = TagRules.sanitizeAll(names)
        selectedTags.clear()
        cleanedNames.forEach { name ->
            selectedTags.add(knownTags[TagRules.normalize(name)] ?: TagDomain(name = name))
        }
        renderTagChips()
        onEditorStateChanged()
    }

    private fun renderTagChips() {
        binding.partialEditContents.tagChipContainer.run {
            removeAllViews()
            selectedTags.forEach { tag ->
                addView(createTagChip(tag))
            }
        }
    }

    /** A chip of the tag row. Tapping it takes the tag off the entry again. */
    private fun createTagChip(tag: TagDomain): View {
        // Same inner padding as the reading view's Compose TagChip: 8dp sideways, 3dp up and down (issue #270).
        val horizontalPadding = dpToPixel(8f)
        val verticalPadding = dpToPixel(3f)
        val textView =
            TextView(this).apply {
                text = TagRules.label(tag.name).plus(TAG_CHIP_REMOVE_MARK)
                setTextColor(config.textColor)
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                textSize = 12f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        val chip =
            FixedCardView(this).apply {
                // Set before the first layout, so the very first frame already carries the padding and
                // shadow the Compose chip has instead of gaining them in onDraw() (issue #270).
                useCompatPadding = true
                radius = resources.getDimension(R.dimen.card_radius)
                cardElevation = dpToPixel(2f).toFloat()
                layoutParams =
                    ViewGroup
                        .MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            marginEnd = dpToPixel(4f)
                            // Space between wrapped rows, and room for the bottom shadow inside the row.
                            bottomMargin = dpToPixel(4f)
                        }
                addView(textView)
            }
        updateDashboardInnerCard(chip)
        chip.setOnClickListener {
            selectedTags.remove(tag)
            renderTagChips()
            onEditorStateChanged()
        }
        return chip
    }

    private fun showSpeechDialog() {
        try {
            requestSpeechInput.launch(recognizerIntent)
        } catch (_: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message))
        }
    }

    private fun callImagePicker() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            try {
                requestImagePicker.launch(pickImageIntent)
            } catch (_: ActivityNotFoundException) {
                showAlertDialog(getString(R.string.gallery_intent_not_found_message))
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

    /**
     * Starts the autosave of [originSequence] once the editor holds its initial state, so that state becomes
     * the baseline and merely opening and leaving the editor writes nothing to the database (issue #261).
     * The periodic write keeps a draft of at most [DiaryAutoSavePolicy.INTERVAL_MILLIS] of typing when the
     * process dies without ever pausing the editor.
     */
    protected fun startDraftAutoSave(originSequence: Long) {
        draftOriginSequence = originSequence
        draftTracker.markStored(captureDraftSnapshot())
        isDraftAutoSaveStarted = true
        draftPeriodicJob?.cancel()
        draftPeriodicJob =
            lifecycleScope.launch {
                while (isActive) {
                    delay(DiaryAutoSavePolicy.INTERVAL_MILLIS)
                    persistDraftIfDirty()
                }
            }
    }

    /**
     * Takes the editor state the user now sees (a loaded diary or a restored draft) as the baseline, so the
     * next autosave writes only what is edited after this point.
     */
    protected fun resetDraftBaseline() {
        draftTracker.markStored(captureDraftSnapshot())
    }

    /**
     * Restarts the idle debounce. Every editor change calls this: title, contents, feeling symbol,
     * date/time, all day, attached photos and location.
     */
    protected fun onEditorStateChanged() {
        if (isDraftAutoSaveStarted.not() || isDraftFinalized) return
        draftDebounceJob?.cancel()
        draftDebounceJob =
            lifecycleScope.launch {
                delay(DiaryAutoSavePolicy.DEBOUNCE_MILLIS)
                persistDraftIfDirty()
            }
    }

    /**
     * Last chance to keep what was typed when the editor pauses. The write is dispatched on the application
     * scope, so tearing the activity down does not cancel it, and it is skipped when the entry was saved or
     * the editor state did not change.
     */
    protected fun persistDraftOnPause() {
        if (isDiarySaved) return
        persistDraftIfDirty()
    }

    private fun persistDraftIfDirty() {
        if (isDraftAutoSaveStarted.not() || isDraftFinalized) return
        val snapshot = captureDraftSnapshot()
        if (draftTracker.isDirty(snapshot).not()) return
        val generation = draftGeneration
        val originSequence = draftOriginSequence
        applicationScope.launch(Dispatchers.Main.immediate) {
            draftWriteMutex.withLock {
                // A save or discard may have happened while this write was waiting for the lock.
                if (generation != draftGeneration) return@withLock
                try {
                    withContext(Dispatchers.IO) {
                        diaryRepository.insertTemporaryDiary(snapshot.toDiary(originSequence))
                    }
                    draftTracker.markStored(snapshot)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // A failed draft write must never interrupt the editor; the next autosave retries it.
                    e.printStackTrace()
                }
            }
        }
    }

    private fun captureDraftSnapshot(): DiaryDraftSnapshot =
        DiaryDraftSnapshot(
            title =
                binding.partialEditContents.diaryTitle.text
                    .toString(),
            contents =
                binding.partialEditContents.diaryContents.text
                    .toString(),
            symbolSequence = selectedItemPosition,
            isAllDay = binding.partialEditContents.allDay.isChecked,
            currentTimeMillis = currentTimeMillis,
            photoUris = draftPhotoUris(),
            location = location,
            tags = selectedTags.toList(),
        )

    /**
     * The attached photos without the ones the user removed, computed without touching [photoUris] or
     * [removeIndexes]: the views of the photo container index into [photoUris], and applying the removals on
     * every autosave would leave those indexes behind (the removals are applied for real on save and
     * onSaveInstanceState).
     */
    private fun draftPhotoUris(): List<PhotoUriDomain> {
        if (removeIndexes.isEmpty()) return photoUris.toList()
        val removedIndexes = removeIndexes.toSet()
        return photoUris.filterIndexed { index, _ -> index !in removedIndexes }
    }

    /**
     * Removes the draft because the user explicitly saved the entry.
     *
     * The generation is bumped first, so an autosave that is still queued writes nothing, and the write lock
     * is taken afterwards, so an insert already running completes before the delete. This editor writes no
     * further draft (issue #261).
     */
    protected suspend fun clearDraftAfterSave(originSequence: Long) {
        isDraftFinalized = true
        draftGeneration++
        draftDebounceJob?.cancel()
        draftPeriodicJob?.cancel()
        draftTracker.clear()
        draftWriteMutex.withLock {
            deleteDraftWhileLocked(originSequence)
        }
    }

    /**
     * Removes a draft the user explicitly chose not to restore, and keeps tracking the editor so that a new
     * edit still leaves a draft behind.
     */
    protected fun discardDraft(originSequence: Long) {
        draftGeneration++
        draftDebounceJob?.cancel()
        applicationScope.launch(Dispatchers.Main.immediate) {
            draftWriteMutex.withLock {
                deleteDraftWhileLocked(originSequence)
            }
            resetDraftBaseline()
        }
    }

    private suspend fun deleteDraftWhileLocked(originSequence: Long) {
        try {
            withContext(Dispatchers.IO) {
                diaryRepository.deleteTemporaryDiaryByOriginId(originSequence)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
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
                            // The restored draft stays in the database until the entry is explicitly saved or
                            // discarded, so a process that dies right after the restore still recovers it.
                            resetDraftBaseline()
                        }
                    },
                    { _, _ ->
                        // The user discarded the draft the dialog offered; remove exactly that draft so the
                        // prompt does not come back on the next visit (issue #261).
                        discardDraft(originSequence)
                    },
                    DialogMode.INFO,
                    false,
                    getString(R.string.load_auto_save_diary_title),
                )
            }
        }
    }

    private val draftTextWatcher =
        object : TextWatcher {
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
            ) {}

            override fun afterTextChanged(s: Editable?) {
                onEditorStateChanged()
            }
        }

    protected fun addTextWatcher() {
        binding.partialEditContents.run {
            diaryTitle.addTextChangedListener(draftTextWatcher)
            diaryContents.addTextChangedListener(draftTextWatcher)
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
            onEditorStateChanged()
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
        binding.partialEditContents.partialEditPhotoContainer.run {
            photoView.run {
                resizePhotoContainerIcon(imageXY)
                background =
                    createBackgroundGradientDrawable(
                        config.primaryColor,
                        THUMBNAIL_BACKGROUND_ALPHA,
                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                    )
            }
            captureCamera.run {
                resizePhotoContainerIcon(imageXY)
                background =
                    createBackgroundGradientDrawable(
                        config.primaryColor,
                        THUMBNAIL_BACKGROUND_ALPHA,
                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                    )
            }
            sketchpad.run {
                resizePhotoContainerIcon(imageXY)
                background =
                    createBackgroundGradientDrawable(
                        config.primaryColor,
                        THUMBNAIL_BACKGROUND_ALPHA,
                        imageXY * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL,
                    )
            }
        }
    }

    /**
     * Resizes an icon of the photo container in place, keeping the margins declared in
     * partial_edit_photo_container.xml (marginStart in portrait, marginTop in landscape)
     * that a newly created LinearLayout.LayoutParams would drop.
     */
    private fun View.resizePhotoContainerIcon(imageXY: Int) {
        val updatedParams =
            (layoutParams as? LinearLayout.LayoutParams)?.apply {
                width = imageXY
                height = imageXY
            } ?: LinearLayout.LayoutParams(imageXY, imageXY)
        layoutParams = updatedParams
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
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentView.windowToken, 0)
        }
    }

    protected fun setupDialog() {
        datePickerDialog =
            DatePickerDialog(this, startDateListener, year, month - 1, dayOfMonth).apply {
                datePicker.firstDayOfWeek =
                    when (config.calendarStartDay) {
                        CALENDAR_START_DAY_MONDAY -> Calendar.MONDAY
                        CALENDAR_START_DAY_SATURDAY -> Calendar.SATURDAY
                        else -> CALENDAR_START_DAY_SUNDAY
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
                    false -> DateUtils.getDateTimeStringFromTimeMillis(currentTimeMillis, SimpleDateFormat.FULL, SimpleDateFormat.MEDIUM, timeFormatMode = storedTimeFormat())
                }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        onEditorStateChanged()
    }

    protected fun attachPhotos(
        selectPaths: ArrayList<String>,
        isUriString: Boolean,
    ) {
        setVisiblePhotoProgress(true)
        Thread {
            selectPaths.forEach { item ->
                val photoPath = EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                try {
                    val mimeType: String =
                        when (isUriString) {
                            true -> {
                                EasyDiaryUtils.downSamplingImage(this, item.toUri(), File(photoPath))
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
                        onEditorStateChanged()
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
                            photoContainer.postDelayed(
                                { (photoContainerScrollView.getChildAt(0) as HorizontalScrollView).fullScroll(HorizontalScrollView.FOCUS_RIGHT) },
                                100L,
                            )
                        }
                    }
                }
                setVisiblePhotoProgress(false)
            }
        }.start()
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
        onEditorStateChanged()
    }

    protected fun initDateTime() {
        intent?.run {
            if (hasExtra(SettingConstants.INITIALIZE_TIME_MILLIS)) {
                currentTimeMillis = getLongExtra(SettingConstants.INITIALIZE_TIME_MILLIS, 0)
            }
        }

        if (currentTimeMillis > 0) {
            applyCurrentTimeMillis(currentTimeMillis)
        }
    }

    /**
     * Copies [timeMillis] into the year/month/day and hour/minute/second fields the date and time pickers
     * edit, in the current time zone.
     */
    protected fun applyCurrentTimeMillis(timeMillis: Long) {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timeMillis
        year = calendar[Calendar.YEAR]
        month = calendar[Calendar.MONTH] + 1
        dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
        hourOfDay = calendar[Calendar.HOUR_OF_DAY]
        minute = calendar[Calendar.MINUTE]
        second = calendar[Calendar.SECOND]
    }

    protected fun restoreContents(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            binding.partialEditContents.partialEditPhotoContainer.run {
                photoUris.clear()
                val attachView = photoContainer.getChildAt(photoContainer.childCount.minus(1))
                photoContainer.removeAllViews()
                photoContainer.addView(attachView)

                getStringArrayList(LIST_URI_STRING)?.forEach { uriString ->
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
                applySelectedTagNames(getStringArrayList(TagPickerConstants.SELECTED_TAG_NAMES).orEmpty())
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

        // An 'All day' diary keeps the day it was written on in any time zone: the editor re-anchors the
        // entry to 00:00 of the stored date string (issue #214), so the date picker shows that day and a
        // save in another time zone writes it back instead of the day the zone change moved the entry to.
        currentTimeMillis = diary.dateAnchorTimeMillis()
        applyCurrentTimeMillis(currentTimeMillis)

        // A loaded diary (existing diary or auto-saved temporary diary) always keeps its own all day state,
        // so the 'All day by default' setting never overwrites what the user saved.
        binding.partialEditContents.allDay.isChecked = diary.isAllDay
        toggleTimePickerTool()

        val encryptionPass = intent.getStringExtra(DIARY_ENCRYPT_PASSWORD)
        when (encryptionPass == null) {
            true -> {
                binding.partialEditContents.diaryTitle.setText(diary.title)
                binding.partialEditContents.diaryContents.setText(diary.contents)
            }

            false -> {
                binding.partialEditContents.diaryTitle.setText(JasyptUtils.decrypt(diary.title, encryptionPass))
                binding.partialEditContents.diaryContents.setText(JasyptUtils.decrypt(diary.contents, encryptionPass))
            }
        }

        if (config.holdPositionEnterEditScreen) {
            binding.partialEditContents.contentsContainer.post {
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
        applySelectedTagNames(diary.tags.map { it.name })
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
        selectedItemPosition == SYMBOL_EASTER_EGG && diaryViewModel
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
            if (selectedItemPosition != SYMBOL_EASTER_EGG) {
                showAlertDialog(
                    getString(R.string.delete_photo_confirm_message),
                    { _, _ ->
                        removeIndexes.add(photoIndex)
                        binding.partialEditContents.partialEditPhotoContainer.photoContainer
                            .removeView(v)
                        initBottomToolbar()
                        onEditorStateChanged()
                    },
                    { _, _ -> },
                    DialogMode.INFO,
                )
            }
        }
    }
}

/** The mark that tells the writer a tag chip is tapped to take the tag off the entry. */
private const val TAG_CHIP_REMOVE_MARK = "  ✕"
