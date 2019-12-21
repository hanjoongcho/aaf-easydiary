package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.werb.pickphotoview.PickPhotoView
import com.werb.pickphotoview.util.PickConfig
import com.werb.pickphotoview.util.PickUtils
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import io.realm.RealmList
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.layout_edit_contents.*
import kotlinx.android.synthetic.main.layout_edit_photo_container.*
import kotlinx.android.synthetic.main.layout_edit_toolbar_sub.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryWeatherItemAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiarySymbol
import me.blog.korn123.easydiary.models.PhotoUriDto
import me.blog.korn123.easydiary.views.SlidingTabLayout
import java.io.File
import java.text.ParseException
import java.util.*
import kotlin.collections.ArrayList

abstract class EditActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    protected lateinit var mRecognizerIntent: Intent
    protected lateinit var mPhotoUris: RealmList<PhotoUriDto>
    protected lateinit var mDatePickerDialog: DatePickerDialog
    protected lateinit var mTimePickerDialog: TimePickerDialog
    protected lateinit var mSecondsPickerDialog: AlertDialog
    protected val mRemoveIndexes = ArrayList<Int>()
    protected var mCurrentTimeMillis: Long = 0
    private val mCalendar = Calendar.getInstance(Locale.getDefault())
    protected var mYear = mCalendar.get(Calendar.YEAR)

    /**
     * mMonth is not Calendar.MONTH
     * mMonth range is 1 ~ 12
     */
    protected var mMonth = mCalendar.get(Calendar.MONTH).plus(1)
    protected var mDayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH)
    protected var mHourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY)
    protected var mMinute = mCalendar.get(Calendar.MINUTE)
    protected var mSecond = mCalendar.get(Calendar.SECOND)
    protected var mSelectedItemPosition = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EasyDiaryUtils.changeDrawableIconColor(this, Color.WHITE, R.drawable.calendar_4_w)
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

    
    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    fun addTextWatcher() {
        if (config.enableCountCharacters) {
            contentsLength.visibility = View.VISIBLE
            contentsLength.text = getString(R.string.diary_contents_length, 0)
            diaryContents.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    contentsLength.text = getString(R.string.diary_contents_length, p0?.length ?: 0)
                }
            })
        }
    }

    fun toggleSimpleLayout() {
        when (photoContainerScrollView.visibility) {
            View.VISIBLE -> {
                photoContainerScrollView.visibility = View.GONE
                togglePhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.expand))
                supportActionBar?.hide()
            }
            View.GONE -> {
                photoContainerScrollView.visibility = View.VISIBLE
                togglePhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.collapse))
                supportActionBar?.show()
            }
            else -> {}
        }
    }

    internal val mEditListener = View.OnClickListener { view ->
        hideSoftInputFromWindow()

        when (view.id) {
            R.id.photoView -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                callImagePicker()
            } else {
                confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
            }
            R.id.captureCamera -> {
                val captureFile = File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY, CAPTURE_CAMERA_FILE_NAME)
                captureFile.createNewFile()
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

//    protected fun setupSpinner() {
//        val weatherArr = resources.getStringArray(R.array.weather_item_array)
//        val arrayAdapter = DiaryWeatherItemAdapter(this, R.layout.item_weather, Arrays.asList(*weatherArr))
//        weatherSpinner.adapter = arrayAdapter
//    }

    private fun addCategory(itemList: ArrayList<Array<String>>, categoryList: ArrayList<String>, resourceName: String, categoryName: String) {
        val resourceId = resources.getIdentifier(resourceName, "array", packageName)
        if (resourceId != 0) {
            itemList.add(resources.getStringArray(resourceId))
            categoryList.add(categoryName)
        }
    }

    var mDialog: AppCompatDialog? = null
    protected fun openFeelingSymbolDialog() {
        if (mDialog == null) {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val symbolDialog = inflater.inflate(R.layout.dialog_feeling_pager, null)

            val itemList = arrayListOf<Array<String>>()
            val categoryList = arrayListOf<String>()
            addCategory(itemList, categoryList, "weather_item_array", getString(R.string.category_weather))
            addCategory(itemList, categoryList, "emotion_item_array", getString(R.string.category_emotion))
            addCategory(itemList, categoryList, "daily_item_array", getString(R.string.category_daily))
            addCategory(itemList, categoryList, "food_item_array", getString(R.string.category_food))
            addCategory(itemList, categoryList, "leisure_item_array", getString(R.string.category_leisure))
            addCategory(itemList, categoryList, "landscape_item_array", getString(R.string.category_landscape))
            addCategory(itemList, categoryList, "symbol_item_array", getString(R.string.category_symbol))
            addCategory(itemList, categoryList, "flag_item_array", getString(R.string.category_flag))

            val viewPager = symbolDialog.findViewById(R.id.viewpager) as androidx.viewpager.widget.ViewPager
            val symbolPagerAdapter = SymbolPagerAdapter(this, itemList, categoryList)
            viewPager.adapter = symbolPagerAdapter

            val slidingTabLayout = symbolDialog.findViewById(R.id.sliding_tabs) as SlidingTabLayout
            slidingTabLayout.setViewPager(viewPager)

            val dismissButton = symbolDialog.findViewById(R.id.closeBottomSheet) as ImageView
            dismissButton.setOnClickListener { mDialog?.dismiss() }

            if (isLandScape()) {
                val builder = AlertDialog.Builder(this)
                builder.setView(symbolDialog)
                mDialog = builder.create()
            } else {
                mDialog = BottomSheetDialog(this)
                mDialog?.run {
                    setContentView(symbolDialog)
                    setCancelable(false)
                    setCanceledOnTouchOutside(true)
                }
            }
        }
        
        mDialog?.show()
    }
    
    protected fun selectFeelingSymbol(index: Int) {
        mSelectedItemPosition = index
        when (mSelectedItemPosition == 0) {
            true -> symbolText.visibility = View.VISIBLE
            false -> symbolText.visibility = View.GONE
        }
        FlavorUtils.initWeatherView(this, symbol, mSelectedItemPosition, false)
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
    
    protected fun callImagePicker() {
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
    
    fun attachPhotos(selectPaths: ArrayList<String>) {
        setVisiblePhotoProgress(true)
        Thread(Runnable {
            selectPaths.map { item ->
                val photoPath = EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
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
                photoContainer.postDelayed({ photoContainerScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
                setVisiblePhotoProgress(false)
            }
        }).start()
    }

    protected fun initBottomToolbar() {
        bottomTitle.text = String.format(getString(R.string.attached_photo_count), photoContainer.childCount -1)
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
    
    /**
     * The [android.support.v4.view.PagerAdapter] used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the [.getPageTitle] method which controls what is displayed in the
     * [SlidingTabLayout].
     */
    inner class SymbolPagerAdapter(val activity: Activity, private val items: ArrayList<Array<String>>, private val categories: List<String>) : androidx.viewpager.widget.PagerAdapter() {

        /**
         * @return the number of pages to display
         */
        override fun getCount(): Int {
            return items.size
        }

        /**
         * @return true if the value returned from [.instantiateItem] is the
         * same object as the [View] added to the [ViewPager].
         */
        override fun isViewFromObject(view: View, o: Any): Boolean {
            return o === view
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at `position`. This is important as what this method
         * returns is what is displayed in the [SlidingTabLayout].
         *
         *
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        override fun getPageTitle(position: Int): CharSequence? {
            return categories[position]
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the [View] which should be displayed at `position`. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            // Inflate a new layout from our resources
            val view = activity.layoutInflater.inflate(R.layout.dialog_feeling, container, false)
            // Add the newly created View to the ViewPager
            container.addView(view)

            val symbolList = arrayListOf<DiarySymbol>()
            items[position].map { item -> symbolList.add(DiarySymbol(item))}

            val arrayAdapter = DiaryWeatherItemAdapter(activity, R.layout.item_weather, symbolList)
            val gridView = view.findViewById<GridView>(R.id.feelingSymbols)
            gridView.adapter = arrayAdapter
            gridView.setOnItemClickListener { parent, view, position, id ->
                val diarySymbol = parent.adapter.getItem(position) as DiarySymbol
                selectFeelingSymbol(diarySymbol.sequence)
                mDialog?.dismiss()
            }


            // Return the View
            return view
        }

        /**
         * Destroy the item from the [ViewPager]. In our case this is simply removing the
         * [View].
         */
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}