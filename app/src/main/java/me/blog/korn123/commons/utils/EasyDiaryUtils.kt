package me.blog.korn123.commons.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import id.zelory.compressor.Compressor
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SecondItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.models.PhotoUri
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*

/**
 * Created by hanjoong on 2017-04-30.
 */

object EasyDiaryUtils {
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

    fun initLegacyWorkingDirectory(context: Context) {
        if (context.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            makeDirectory(getExternalStorageDirectory().absolutePath + BACKUP_EXCEL_DIRECTORY)
        }
    }

    private fun makeDirectory(path: String) {
        val workingDirectory = File(path)
        if (!workingDirectory.exists()) workingDirectory.mkdirs()
    }

    fun getApplicationDataDirectory(context: Context): String {
//        return Environment.getExternalStorageDirectory().absolutePath
        return context.applicationInfo.dataDir
    }

    fun getExternalStorageDirectory(): File = Environment.getExternalStorageDirectory()

    fun boldString(context: Context, textView: TextView?) {
        if (context.config.boldStyleEnable) {
            textView?.let { tv ->
                val spannableString = SpannableString(tv.text)
                spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, tv.text.length, 0)
                tv.text = spannableString
            }
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
//        spannableString.setSpan(UnderlineSpan(), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, textView.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
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

    fun removeSpans(spannableString: SpannableString) {
        spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)?.forEach { spannableString.removeSpan(it) }
        spannableString.getSpans(0, spannableString.length, ForegroundColorSpan::class.java)?.forEach { spannableString.removeSpan(it) }
    }

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

    fun photoUriToDownSamplingBitmap(
            context: Context,
            photoUri: PhotoUri,
            requiredSize: Int = 50,
            fixedWidth: Int = 45,
            fixedHeight: Int = 45
    ): Bitmap = try {
        when (photoUri.isContentUri()) {
            true -> {
                BitmapUtils.decodeFile(context, Uri.parse(photoUri.photoUri), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
            }
            false -> {
                when (fixedWidth == fixedHeight) {
                    true -> BitmapUtils.decodeFileCropCenter(getApplicationDataDirectory(context) + photoUri.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR))
                    false -> BitmapUtils.decodeFile(getApplicationDataDirectory(context) + photoUri.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
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
            else ->{
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

    fun summaryDiaryLabel(diary: Diary): String {
        return if (!diary.title.isNullOrEmpty()) diary.title!! else StringUtils.abbreviate(diary.contents, 10)
    }

    fun datePickerToTimeMillis(dayOfMonth: Int, month: Int, year: Int, isFullHour: Boolean = false, hour: Int = 0, minute: Int = 0, second: Int = 0): Long {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cal.set(Calendar.HOUR_OF_DAY, if (isFullHour) 23 else hour)
        cal.set(Calendar.MINUTE, if (isFullHour) 59 else minute)
        cal.set(Calendar.SECOND, if (isFullHour) 59 else second)
        return cal.timeInMillis
    }

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

    fun createAttachedPhotoView(context: Context, photoUri: PhotoUri, marginLeft:Float = 0F, marginTop:Float = 0F, marginRight:Float = 3F, marginBottom:Float = 0F, bgResourceId: Int = R.drawable.bg_card_thumbnail): ImageView {
        val thumbnailSize = context.config.settingThumbnailSize
//        val bitmap = photoUriToDownSamplingBitmap(context, photoUriDto, 0, thumbnailSize.toInt() - 5, thumbnailSize.toInt() - 5)
        val imageView = ImageView(context)
        val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(context, thumbnailSize), CommonUtils.dpToPixel(context, thumbnailSize))
//        val marginLeft = if (photoIndex == 0)  0 else CommonUtils.dpToPixel(context, 3F)
        layoutParams.setMargins(CommonUtils.dpToPixel(context, marginLeft), CommonUtils.dpToPixel(context, marginTop), CommonUtils.dpToPixel(context, marginRight), CommonUtils.dpToPixel(context, marginBottom))
        imageView.layoutParams = layoutParams
        val drawable = ContextCompat.getDrawable(context, bgResourceId)
        val gradient = drawable as GradientDrawable
        gradient.setColor(ColorUtils.setAlphaComponent(context.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
        imageView.background = gradient
//        imageView.setImageBitmap(bitmap)
        val padding = (CommonUtils.dpToPixel(context, 2.5F, CALCULATION.FLOOR))
        imageView.setPadding(padding, padding, padding, padding)
        imageView.scaleType = ImageView.ScaleType.CENTER
        val options = RequestOptions()
//                .error(R.drawable.error_7)
                .placeholder(R.drawable.ic_error_7)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
        Glide.with(context).load(getApplicationDataDirectory(context) + photoUri.getFilePath()).apply(options).into(imageView)
        return imageView
    }

    fun jsonFileToHashMap(filename: String): HashMap<String, Any> {
        val reader = JsonReader(FileReader(filename))
        val type = object : TypeToken<HashMap<String, Any>>(){}.type
        val map: HashMap<String, Any> = GsonBuilder().create().fromJson(reader, type)
        reader.close()
        return map
    }

    fun fromHtml(target: String): Spanned {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Html.fromHtml(target)
        }
        return Html.fromHtml(target, Html.FROM_HTML_MODE_LEGACY);
    }

    fun jsonStringToHashMap(jsonString: String): HashMap<String, Any> {
        val type = object : TypeToken<HashMap<String, Any>>(){}.type
        return GsonBuilder().create().fromJson(jsonString, type)
    }

    fun openCustomOptionMenu(content: View, parent: View): PopupWindow {
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val popup: PopupWindow = PopupWindow(content, width, height, true).apply {
//            animationStyle = R.style.text_view_option_animation
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            showAtLocation(parent, Gravity.TOP or Gravity.RIGHT,0, CommonUtils.dpToPixel(parent.context, 24F))
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

    @SuppressLint("ClickableViewAccessibility")
    fun disableTouchEvent(view: View) {
        view.setOnTouchListener { _, _ -> true }
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
}
