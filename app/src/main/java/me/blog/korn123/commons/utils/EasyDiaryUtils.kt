package me.blog.korn123.commons.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import id.zelory.compressor.Compressor
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.CALCULATION
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.SecondItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.FileNotFoundException
import java.util.*

/**
 * Created by hanjoong on 2017-04-30.
 */

object EasyDiaryUtils {
    private const val HIGHLIGHT_COLOR: Int = 0x9FFFFF00.toInt()

    val easyDiaryMimeType: String
        get() = "text/aaf_v" + EasyDiaryDbHelper.getInstance().version

    val easyDiaryMimeTypeAll: Array<String?>
        get() {
            val currentVersion = EasyDiaryDbHelper.getInstance().version.toInt()
            val easyDiaryMimeType = arrayOfNulls<String>(currentVersion)
            for (i in 0 until currentVersion) {
                easyDiaryMimeType[i] = "text/aaf_v" + (i + 1)
            }
            return easyDiaryMimeType
        }

    fun initWorkingDirectory(context: Context) {
        if (context.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            makeDirectory(getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + DIARY_POSTCARD_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + USER_CUSTOM_FONTS_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + MARKDOWN_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + BACKUP_EXCEL_DIRECTORY)
            makeDirectory(getApplicationDataDirectory(context) + BACKUP_DB_DIRECTORY)
        }
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
    
    fun highlightString(textView: TextView?, input: String?) {
        textView?.let { tv ->
            input?.let { targetString ->
                //Get the text from text view and create a spannable string
                val spannableString = SpannableString(tv.text)

                //Get the previous spans and remove them
                val backgroundSpans = spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)

                for (span in backgroundSpans) {
                    spannableString.removeSpan(span)
                }

                //Search for all occurrences of the keyword in the string
                var indexOfKeyword = spannableString.toString().indexOf(targetString)

                while (indexOfKeyword >= 0) {
                    //Create a background color span on the keyword
                    spannableString.setSpan(BackgroundColorSpan(HIGHLIGHT_COLOR), indexOfKeyword, indexOfKeyword + targetString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    //Get the next index of the keyword
                    indexOfKeyword = spannableString.toString().indexOf(targetString, indexOfKeyword + targetString.length)
                }

                //Set the final text on TextView
                tv.text = spannableString    
            }
        }
    }

    fun highlightStringIgnoreCase(textView: TextView?, input: String?) {
        textView?.let { tv -> 
            input?.let { targetString ->
                val inputLower = targetString.toLowerCase()
                val contentsLower = tv.text.toString().toLowerCase()
                val spannableString = SpannableString(tv.text)

                val backgroundSpans = spannableString.getSpans(0, spannableString.length, BackgroundColorSpan::class.java)

                for (span in backgroundSpans) {
                    spannableString.removeSpan(span)
                }

                var indexOfKeyword = contentsLower.indexOf(inputLower)

                while (indexOfKeyword >= 0) {
                    spannableString.setSpan(BackgroundColorSpan(HIGHLIGHT_COLOR), indexOfKeyword, indexOfKeyword + inputLower.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    indexOfKeyword = contentsLower.indexOf(inputLower, indexOfKeyword + inputLower.length)
                }
                tv.text = spannableString
            }
        }
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
            photoUriDto: PhotoUriDto,
            requiredSize: Int = 50,
            fixedWidth: Int = 45,
            fixedHeight: Int = 45
    ): Bitmap = try {
        when (photoUriDto.isContentUri()) {
            true -> {
                BitmapUtils.decodeFile(context, Uri.parse(photoUriDto.photoUri), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
            }
            false -> {
                when (fixedWidth == fixedHeight) {
                    true -> BitmapUtils.decodeFileCropCenter(getApplicationDataDirectory(context) + photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR))
                    false -> BitmapUtils.decodeFile(getApplicationDataDirectory(context) + photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, fixedWidth.toFloat(), CALCULATION.FLOOR), CommonUtils.dpToPixel(context, fixedHeight.toFloat(), CALCULATION.FLOOR))
                }

            }
        }
    } catch (fe: FileNotFoundException) {
        fe.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    } catch (se: SecurityException) {
        se.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    } catch (e: Exception) {
        e.printStackTrace()
        BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
    }

    fun photoUriToBitmap(context: Context, photoUriDto: PhotoUriDto): Bitmap? {
        val bitmap: Bitmap? = try {
            when (photoUriDto.isContentUri()) {
                true -> {
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(photoUriDto.photoUri)))
                }
                false -> {
                    BitmapFactory.decodeFile(getApplicationDataDirectory(context) + photoUriDto.getFilePath())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return bitmap
    }
    
    fun downSamplingImage(context: Context, srcFile: File, destFile: File) {
        val compressedFile = Compressor(context).setQuality(70).compressToFile(srcFile)
        FileUtils.copyFile(compressedFile, destFile)
    }

    fun changeDrawableIconColor(context: Context, color: Int, resourceId: Int) {
        ContextCompat.getDrawable(context, resourceId)?.apply {
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    fun summaryDiaryLabel(diaryDto: DiaryDto): String {
        return if (!diaryDto.title.isNullOrEmpty()) diaryDto.title!! else StringUtils.abbreviate(diaryDto.contents, 10)
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

    fun sequenceToPageIndex(diaryList: ArrayList<DiaryDto>, sequence: Int): Int {
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
}
