package me.blog.korn123.commons.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import id.zelory.compressor.Compressor
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SecondItemAdapter
import me.blog.korn123.easydiary.enums.Calculation
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.fragments.DiaryFragment
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.models.PhotoUri
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

/**
 * Created by hanjoong on 2017-04-30.
 */
object EasyDiaryUtils {
    /***************************************************************************************************
     *   Constants
     *
     ***************************************************************************************************/
    private const val HIGHLIGHT_COLOR: Int = 0x9FFFFF00.toInt()

    val easyDiaryMimeType: String
        get() {
            val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
            val currentVersion = realmInstance.version.toInt()
            realmInstance.close()
            return "text/aaf_v$currentVersion"
        }

    val easyDiaryMimeTypeAll: Array<String?>
        get() {
            val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
            val currentVersion = realmInstance.version.toInt()
            realmInstance.close()
            val easyDiaryMimeType = arrayOfNulls<String>(currentVersion)
            for (i in 0 until currentVersion) {
                easyDiaryMimeType[i] = "text/aaf_v" + (i + 1)
            }
            return easyDiaryMimeType
        }


    /***************************************************************************************************
     *   String Utils
     *
     ***************************************************************************************************/
    fun summaryDiaryLabel(diary: Diary): String {
//        return if (!diary.title.isNullOrEmpty()) diary.title!! else StringUtils.abbreviate(diary.contents, 10)
        return if (diary.title.isNullOrEmpty()) diary.contents!!.lines()[0] else diary.title!!
    }

    fun searchWordIndexes(contents: String, searchWord: String): List<Int> {
        val indexes = arrayListOf<Int>()
        if (searchWord.isNotEmpty()) {
            var index = contents.indexOf(searchWord, 0, true)
            while (index >= 0) {
                indexes.add(index)
                index = contents.indexOf(searchWord, index.plus(1), true)
            }
        }
        return indexes
    }


    /***************************************************************************************************
     *   Number Utils
     *
     ***************************************************************************************************/
    fun isNumberString(string: String?): Boolean = string?.toFloatOrNull() != null

    fun isContainNumber(string: String?): Boolean {
        return string?.contains("\\d+\\.?\\d+".toRegex()) ?: false
    }

    fun isStockNumber(string: String?): Boolean {
        return "$string,".matches("^(\\d+,)+$".toRegex())
    }

    fun findNumber(string: String?): Float {
        var number = 0f
        string?.let {
            val intRange = "\\d+\\.?\\d+".toRegex().find(it)?.range ?: IntRange(0, 0)
            number = string.substring(intRange).toFloat()
        }
        return number
    }


    /***************************************************************************************************
     *   Date Utils
     *
     ***************************************************************************************************/
    fun datePickerToTimeMillis(dayOfMonth: Int, month: Int, year: Int, isFullHour: Boolean = false, hour: Int = 0, minute: Int = 0, second: Int = 0): Long {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, if (isFullHour) 23 else hour)
        cal.set(Calendar.MINUTE, if (isFullHour) 59 else minute)
        cal.set(Calendar.SECOND, if (isFullHour) 59 else second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun convDateToTimeMillis(field: Int, amount: Int, isZeroHour: Boolean = true, isZeroMinute: Boolean = true, isZeroSecond: Boolean = true, isZeroMilliSecond: Boolean = true): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (isZeroHour) calendar.set(Calendar.HOUR_OF_DAY, 0)
        if (isZeroMinute) calendar.set(Calendar.MINUTE, 0)
        if (isZeroSecond) calendar.set(Calendar.SECOND, 0)
        if (isZeroMilliSecond) calendar.set(Calendar.MILLISECOND, 0)
        if (amount != 0) {
            calendar.add(field, amount)
        }
        return calendar.timeInMillis
    }

    fun convDateToTimeMillis(isFullHour: Boolean = false, addYears: Int = 0): Long {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, if (isFullHour) 23 else 0)
        cal.set(Calendar.MINUTE, if (isFullHour) 59 else 0)
        cal.set(Calendar.SECOND, if (isFullHour) 59 else 0)
        if (addYears != 0) cal.add(Calendar.YEAR, addYears)
        return cal.timeInMillis
    }

    fun getCalendarInstance(isFullHour: Boolean = false, addYears: Int = 0): Calendar {
        return getCalendarInstance(isFullHour, Calendar.YEAR, addYears)
    }

    fun getCalendarInstance(isFullHour: Boolean = false, field: Int, amount: Int): Calendar {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, if (isFullHour) 23 else 0)
        cal.set(Calendar.MINUTE, if (isFullHour) 59 else 0)
        cal.set(Calendar.SECOND, if (isFullHour) 59 else 0)
        if (amount != 0) cal.add(field, amount)
        return cal
    }


    /***************************************************************************************************
     *   Image Utils
     *
     ***************************************************************************************************/
    fun createBackgroundGradientDrawable(color: Int, alpha: Int, cornerRadius: Float): Drawable {
        val gradientDrawable = GradientDrawable().apply {
            setColor(ColorUtils.setAlphaComponent(color, alpha))
            setCornerRadius(cornerRadius)
        }
        return gradientDrawable
    }

    fun createAttachedPhotoView(context: Context, photoUri: PhotoUri, marginLeft:Float = 0F, marginTop:Float = 0F, marginRight:Float = 3F, marginBottom:Float = 0F): ImageView {
        val thumbnailSize = context.dpToPixel(context.config.settingThumbnailSize)
        val cornerRadius = thumbnailSize * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
        val imageView = ImageView(context)
        val layoutParams = LinearLayout.LayoutParams(thumbnailSize, thumbnailSize)
        layoutParams.setMargins(context.dpToPixel(marginLeft), context.dpToPixel(marginTop), context.dpToPixel(marginRight), context.dpToPixel(marginBottom))
        imageView.layoutParams = layoutParams
        imageView.background = createBackgroundGradientDrawable(context.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, cornerRadius)
        imageView.scaleType = ImageView.ScaleType.CENTER
        val padding = (context.dpToPixel(2.5F, Calculation.FLOOR))
        imageView.setPadding(padding, padding, padding, padding)
        Glide.with(context)
            .load(getApplicationDataDirectory(context) + photoUri.getFilePath())
            .apply(createThumbnailGlideOptions(cornerRadius, photoUri.isEncrypt()))
            .into(imageView)
        return imageView
    }

    fun createAttachedPhotoViewForFlexBox(activity: Activity, photoUri: PhotoUri, attachedCount:Int): ImageView {
        val spanCount = when {
            !activity.isLandScape() && attachedCount == 1 -> 1
            !activity.isLandScape() && attachedCount == 2 -> 2
            !activity.isLandScape() && attachedCount > 2 -> 3
            activity.isLandScape()  -> 5
            else -> 1
        }
        val thumbnailSize = (activity.getDefaultDisplay().x - activity.dpToPixel(ATTACH_PHOTO_CARD_PADDING_DP) - activity.dpToPixel(spanCount * ATTACH_PHOTO_MARGIN_DP * 2f)).div(spanCount)
        val cornerRadius = thumbnailSize * PHOTO_CORNER_RADIUS_SCALE_FACTOR_NORMAL
        val imageView = ImageView(activity)
        val layoutParams = LinearLayout.LayoutParams(thumbnailSize, thumbnailSize)
        layoutParams.setMargins(activity.dpToPixel(ATTACH_PHOTO_MARGIN_DP), activity.dpToPixel(ATTACH_PHOTO_MARGIN_DP), activity.dpToPixel(ATTACH_PHOTO_MARGIN_DP), activity.dpToPixel(ATTACH_PHOTO_MARGIN_DP))
        imageView.layoutParams = layoutParams
        imageView.background = createBackgroundGradientDrawable(activity.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA, cornerRadius)
        imageView.scaleType = ImageView.ScaleType.CENTER
        val padding = (activity.dpToPixel(2.5F, Calculation.FLOOR))
        imageView.setPadding(padding, padding, padding, padding)
        Glide.with(activity)
            .load(getApplicationDataDirectory(activity) + photoUri.getFilePath())
            .apply(createThumbnailGlideOptions(cornerRadius, photoUri.isEncrypt()))
            .into(imageView)
        return imageView
    }

    fun downSamplingImage(context: Context, uri: Uri, destFile: File): String {
        val mimeType = context.contentResolver.getType(uri) ?: MIME_TYPE_JPEG
        val uriStream = context.contentResolver.openInputStream(uri)
        when (mimeType) {
            "image/gif" -> {
//                Handler(Looper.getMainLooper()).post { context.toast(mimeType, Toast.LENGTH_SHORT) }
                val fos = FileOutputStream(destFile)
                IOUtils.copy(uriStream, fos)
                uriStream?.close()
                fos.close()
            }
            else -> {
                val tempFile = File.createTempFile(UUID.randomUUID().toString(), "tmp")
                val fos = FileOutputStream(tempFile)
                IOUtils.copy(uriStream, fos)
                val compressedFile = Compressor(context).setQuality(70).compressToFile(tempFile)
                compressedFile.copyTo(destFile, true)
                uriStream?.close()
                fos.close()
                tempFile.delete()
            }
        }
        return mimeType
    }

    fun downSamplingImage(context: Context, srcFile: File, destFile: File) {
        val compressedFile = Compressor(context).setQuality(70).compressToFile(srcFile)
        compressedFile.copyTo(destFile, true)
    }

    fun createThumbnailGlideOptions(radius: Float, isEncrypt: Boolean = false): RequestOptions = createThumbnailGlideOptions(radius.toInt(), isEncrypt)

    fun createThumbnailGlideOptions(radius: Int, isEncrypt: Boolean = false): RequestOptions = RequestOptions()
        /*.error(R.drawable.error_7)*/
        .placeholder(if (isEncrypt) R.drawable.ic_padlock else R.drawable.ic_error_7)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .priority(Priority.HIGH)
        .transform(MultiTransformation(CenterCrop(), RoundedCorners(radius)))


    /***************************************************************************************************
     *   File Utils
     *
     ***************************************************************************************************/
    private fun makeDirectory(path: String) {
        val workingDirectory = File(path)
        if (!workingDirectory.exists()) workingDirectory.mkdirs()
    }

    fun initWorkingDirectory(context: Context) {
//        if (context.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            makeDirectory(getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + DIARY_POSTCARD_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + MARKDOWN_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + BACKUP_EXCEL_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + BACKUP_DB_DIRECTORY)
//        }
    }

    fun getExternalStorageDirectory(): File = Environment.getExternalStorageDirectory()

    fun initLegacyWorkingDirectory(context: Context) {
        if (context.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            makeDirectory(getExternalStorageDirectory().absolutePath + BACKUP_EXCEL_DIRECTORY)
        }
    }

    fun getApplicationDataDirectory(context: Context): String {
//        return Environment.getExternalStorageDirectory().absolutePath
        return context.applicationInfo.dataDir
    }

    fun readFileWithSAF(mimeType: String, activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
        }
        activityResultLauncher.launch(intent)
    }

    fun writeFileWithSAF(fileName: String, mimeType: String, activityResultLauncher: ActivityResultLauncher<Intent>) {
        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            type = mimeType
            // Create a file with the requested MIME type.
            putExtra(Intent.EXTRA_TITLE, fileName)
        }.run {
            activityResultLauncher.launch(this)
        }
    }


    /***************************************************************************************************
     *   View Utils
     *
     ***************************************************************************************************/
    fun boldString(context: Context, textView: TextView?) {
        if (context.config.boldStyleEnable) {
            boldStringForce(textView)
        }
    }

    fun boldStringForce(textView: TextView?) {
        textView?.let { tv ->
            val spannableString = SpannableString(tv.text)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, tv.text.length, 0)
            tv.text = spannableString
        }
    }

    fun warningString(textView: TextView) {
        val spannableString = SpannableString(textView.text)
        spannableString.setSpan(UnderlineSpan(), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
    }

    fun highlightString(textView: TextView) {
        val spannableString = SpannableString(textView.text)
        spannableString.setSpan(BackgroundColorSpan(HIGHLIGHT_COLOR), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(UnderlineSpan(), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
    }

    fun highlightStringIgnoreCase(textView: TextView?, input: String?, highlightColor: Int = HIGHLIGHT_COLOR) {
        textView?.let { tv ->
            input?.let { targetString ->
                val inputLower = targetString.toLowerCase()
                val contentsLower = tv.text.toString().toLowerCase()
                val spannableString = SpannableString(tv.text)
                removeSpans(spannableString)

                var indexOfKeyword = contentsLower.indexOf(inputLower)
                while (indexOfKeyword >= 0) {
                    spannableString.setSpan(BackgroundColorSpan(highlightColor), indexOfKeyword, indexOfKeyword + inputLower.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableString.setSpan(ForegroundColorSpan(Color.BLACK), indexOfKeyword, indexOfKeyword + targetString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    indexOfKeyword = contentsLower.indexOf(inputLower, indexOfKeyword + inputLower.length)
                }
                tv.text = spannableString
            }
        }
    }

    fun highlightString(textView: TextView?, input: String?, highlightColor: Int = HIGHLIGHT_COLOR) {
        textView?.let { tv ->
            input?.let { targetString ->
                //Get the text from text view and create a spannable string
                val spannableString = SpannableString(tv.text)
                removeSpans(spannableString)

                //Search for all occurrences of the keyword in the string
                var indexOfKeyword = spannableString.toString().indexOf(targetString)
                while (indexOfKeyword >= 0) {
                    //Create a background color span on the keyword
                    spannableString.setSpan(BackgroundColorSpan(highlightColor), indexOfKeyword, indexOfKeyword + targetString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannableString.setSpan(ForegroundColorSpan(Color.BLACK), indexOfKeyword, indexOfKeyword + targetString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    //Get the next index of the keyword
                    indexOfKeyword = spannableString.toString().indexOf(targetString, indexOfKeyword + targetString.length)
                }

                //Set the final text on TextView
                tv.text = spannableString
            }
        }
    }

    fun removeSpans(spannableString: SpannableString) {
        spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)?.forEach { spannableString.removeSpan(it) }
        spannableString.getSpans(0, spannableString.length, ForegroundColorSpan::class.java)?.forEach { spannableString.removeSpan(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun disableTouchEvent(view: View) {
        view.setOnTouchListener { _, _ -> true }
    }

    fun applyMarkDownEllipsize(textContents: TextView, sequence: Int, delayMillis: Long = 0) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (textContents.tag == sequence) {
                val max = textContents.maxLines
                val layout = textContents.layout
                if ((layout?.lineCount ?: 0) > max) {
                    val end = layout.getLineEnd(max - 1)
                    textContents.setText(textContents.text.subSequence(0, end - 1), TextView.BufferType.SPANNABLE)
                    textContents.append("…")
                }
            }
        }, delayMillis)
    }

    /***************************************************************************************************
     *   Dialog Utils
     *
     ***************************************************************************************************/
    fun createSecondsPickerBuilder(context: Context, itemClickListener: AdapterView.OnItemClickListener, second: Int): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setNegativeButton(context.getString(android.R.string.cancel), null)
        builder.setTitle(context.getString(R.string.common_create_seconds))
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val secondsPicker = inflater.inflate(R.layout.dialog_seconds_picker, null)
        val listView = secondsPicker.findViewById<ListView>(R.id.seconds)
        val listSecond = ArrayList<Map<String, String>>()
        for (i in 0..59) {
            val map = hashMapOf<String, String>()
            map.put("label", i.toString() + "s")
            map.put("value", i.toString())
            listSecond.add(map)
        }
        val adapter = SecondItemAdapter(context, R.layout.item_second, listSecond, second)
        listView.adapter = adapter
        listView.onItemClickListener = itemClickListener
        builder.setView(secondsPicker)
        return builder
    }

    fun openCustomOptionMenu(content: View, parent: View): PopupWindow {
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popup: PopupWindow = PopupWindow(content, width, height, true).apply {
//            animationStyle = R.style.text_view_option_animation
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showAtLocation(parent, Gravity.TOP or Gravity.RIGHT,0, parent.context.dpToPixel(24F))
        }
        content.x = 1000f
        content.y = 0f
        val animX = ObjectAnimator.ofFloat(content, "x", 0f)
        val animY = ObjectAnimator.ofFloat(content, "y", 0f)
        AnimatorSet().apply {
            playTogether(animX, animY)
            duration = 390
            start()
        }
        return popup
    }


    /***************************************************************************************************
     *   Conversion Utils
     *
     ***************************************************************************************************/
    fun sequenceToPageIndex(diaryList: List<Diary>, sequence: Int): Int {
        var pageIndex = 0
        if (sequence > -1) {
            for (i in diaryList.indices) {
                if (diaryList[i].sequence == sequence) {
                    pageIndex = i
                    break
                }
            }
        }
        return pageIndex
    }

    fun queryName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor: Cursor? = resolver.query(uri, null, null, null, null)
        var name: String? = null
        returnCursor?.let {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            name = returnCursor.getString(nameIndex);
            returnCursor.close()
        }
        return name ?: UUID.randomUUID().toString()
    }

    fun jsonStringToHashMap(jsonString: String): HashMap<String, Any> {
        val type = object : TypeToken<HashMap<String, Any>>(){}.type
        return GsonBuilder().create().fromJson(jsonString, type)
    }

    fun jsonFileToHashMap(filename: String): HashMap<String, Any> {
        val reader = JsonReader(FileReader(filename))
        val type = object : TypeToken<HashMap<String, Any>>(){}.type
        val map: HashMap<String, Any> = GsonBuilder().create().fromJson(reader, type)
        reader.close()
        return map
    }

    fun hashMapToJsonString(map: HashMap<String, Any>): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(map)
    }

    fun fromHtml(target: String): Spanned {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Html.fromHtml(target)
        }
        return Html.fromHtml(target, Html.FROM_HTML_MODE_LEGACY);
    }


    /***************************************************************************************************
     *   Chart Utils
     *
     ***************************************************************************************************/
    fun getSymbolUsedCountMap(isReverse: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0): Map<Int, Int> {
        EasyDiaryDbHelper.getTemporaryInstance().let { realmInstance ->
            val listDiary = EasyDiaryDbHelper.findDiary(null, false, startTimeMillis, endTimeMillis, realmInstance = realmInstance)

            val map = hashMapOf<Int, Int>()
            listDiary.map { diaryDto ->
                val targetColumn = diaryDto.weather
                if (targetColumn != 0) {
                    if (map[targetColumn] == null) {
                        map[targetColumn] = 1
                    } else {
                        map[targetColumn] = (map[targetColumn] ?: 0) + 1
                    }
                }
            }
            realmInstance.close()
            return when(isReverse) {
                true -> map.toList().sortedByDescending { (_, value) -> value }.toMap()
                false -> map.toList().sortedBy { (_, value) -> value }.toMap()
            }
        }
    }


    /***************************************************************************************************
     *   Legacy Utils
     *
     ***************************************************************************************************/
    fun photoUriToDownSamplingBitmap(
        context: Context,
        photoUri: PhotoUri,
        requiredSize: Int = 50,
        fixedWidth: Int = 45,
        fixedHeight: Int = 45
    ): Bitmap = try {
        when (photoUri.isContentUri()) {
            true -> {
                BitmapUtils.decodeFile(context, Uri.parse(photoUri.photoUri), context.dpToPixel(fixedWidth.toFloat(), Calculation.FLOOR), context.dpToPixel(fixedHeight.toFloat(), Calculation.FLOOR))
            }
            false -> {
                when (fixedWidth == fixedHeight) {
                    true -> BitmapUtils.decodeFileCropCenter(getApplicationDataDirectory(context) + photoUri.getFilePath(), context.dpToPixel(fixedWidth.toFloat(), Calculation.FLOOR))
                    false -> BitmapUtils.decodeFile(getApplicationDataDirectory(context) + photoUri.getFilePath(), context.dpToPixel(fixedWidth.toFloat(), Calculation.FLOOR), context.dpToPixel(fixedHeight.toFloat(), Calculation.FLOOR))
                }

            }
        }
    } catch (fe: FileNotFoundException) {
        fe.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_error_7)
    } catch (se: SecurityException) {
        se.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_error_7)
    } catch (e: Exception) {
        e.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.ic_error_7)
    }

    fun photoUriToBitmap(context: Context, photoUri: PhotoUri): Bitmap? {
        val bitmap: Bitmap? = try {
            when (photoUri.isContentUri()) {
                true -> {
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(photoUri.photoUri)))
                }
                false -> {
                    BitmapFactory.decodeFile(getApplicationDataDirectory(context) + photoUri.getFilePath())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return bitmap
    }

    /***************************************************************************************************
     *   ETC.
     *
     ***************************************************************************************************/
    fun applyFilter(mode: String?) : List<Diary> {
        val diaryList: List<Diary> = when (mode) {
            DiaryFragment.MODE_TASK_TODO -> EasyDiaryDbHelper.findDiary(
                null,
                false,
                0,
                0,
                0
            ).filter { item -> item.weather in 80..81 }.reversed()
            DiaryFragment.MODE_TASK_DOING -> EasyDiaryDbHelper.findDiary(
                null,
                false,
                0,
                0,
                81
            )
            DiaryFragment.MODE_TASK_DONE -> EasyDiaryDbHelper.findDiary(
                null,
                false,
                0,
                0,
                0
            ).filter { item -> item.weather in 82..83 }
            DiaryFragment.MODE_TASK_CANCEL -> EasyDiaryDbHelper.findDiary(
                null,
                false,
                0,
                0,
                83
            )
            DiaryFragment.MODE_FUTURE -> EasyDiaryDbHelper.findDiary(
                null,
                false,
                0,
                0,
                0
            ).filter { item -> (item.weather < 80 || item.weather > 83) && item.currentTimeMillis > System.currentTimeMillis() }.reversed()
            else -> EasyDiaryDbHelper.findDiary(null, false, 0, 0, 0)
                .filter { item -> (item.weather < 80 || item.weather > 83) && item.currentTimeMillis <= System.currentTimeMillis() }
                .run { if (this.size > 100) this.subList(0, 100) else this }
        }

        return diaryList
    }
}
