package me.blog.korn123.commons.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.preference.PreferenceManager
import android.util.TypedValue
import android.provider.OpenableColumns
import android.util.Log
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*
import android.provider.ContactsContract.CommonDataKinds.StructuredName.SUFFIX
import id.zelory.compressor.Compressor
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FilenameUtils
import java.security.spec.ECField


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object CommonUtils {
    val buildVersion: Int
        get() = android.os.Build.VERSION.SDK_INT

    fun preferencesContains(context: Context, key: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.contains(key)
    }

    fun loadStringPreference(context: Context, key: String, defaultValue: String): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, defaultValue)
    }

    fun saveStringPreference(context: Context, key: String, value: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putString(key, value)
        edit.commit()
    }

    fun loadLongPreference(context: Context, key: String, defaultValue: Int): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(key, defaultValue.toLong())
    }

    fun saveLongPreference(context: Context, key: String, value: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putLong(key, value)
        edit.commit()
    }

    fun loadFloatPreference(context: Context, key: String, defaultValue: Float): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getFloat(key, defaultValue)
    }

    fun saveFloatPreference(context: Context, key: String, value: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putFloat(key, value)
        edit.commit()
    }

    fun loadIntPreference(context: Context, key: String, defaultValue: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, defaultValue)
    }

    fun saveIntPreference(context: Context, key: String, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putInt(key, value)
        edit.commit()
    }

    fun loadBooleanPreference(context: Context, key: String): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(key, false)
    }

    fun saveBooleanPreference(context: Context, key: String, isEnable: Boolean) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putBoolean(key, isEnable)
        edit.commit()
    }

    fun dpToPixelFloatValue(context: Context, dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)
    }

    @JvmOverloads
    fun dpToPixel(context: Context, dp: Int, policy: Int = 0): Int {
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)
        var pixel = 0
        when (policy) {
            0 -> pixel = px.toInt()
            1 -> pixel = Math.round(px)
        }
        return pixel
    }

    fun uriToPath(contentResolver: ContentResolver, uri: Uri): String? {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToNext()
        val columnIndex = cursor.getColumnIndex("_data")
        if (columnIndex > 0) {
            path = cursor.getString(columnIndex)
        } else {
            path = uri.toString()
        }
        cursor.close()
        return path
    }

    @Throws(IOException::class)
    fun uriToFile(context: Context, uri: Uri, photoPath: String): Boolean {
        var result = false
        try {
            val tempFile = File.createTempFile("TEMP_PHOTO", "AAF").apply { deleteOnExit() }
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            IOUtils.copy(inputStream, outputStream)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)

            val compressedFile = Compressor(context).setQuality(70).compressToFile(tempFile)
            FileUtils.copyFile(compressedFile, File(photoPath))
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
    
    fun photoUriToDownSamplingBitmap(context: Context, photoUriDto: PhotoUriDto): Bitmap {
        val bitmap: Bitmap = try {
            when (photoUriDto.isContentUri()) {
                true -> {
                    BitmapUtils.decodeUri(context, Uri.parse(photoUriDto.photoUri), CommonUtils.dpToPixel(context, 70, 1), CommonUtils.dpToPixel(context, 65, 1), CommonUtils.dpToPixel(context, 45, 1))
                }
                false -> {
                    BitmapUtils.decodeFile(context, photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, 70, 1), CommonUtils.dpToPixel(context, 65, 1), CommonUtils.dpToPixel(context, 45, 1))
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
        } catch (se: SecurityException) {
            se.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
        }
        return bitmap
    }

    fun photoUriToBitmap(context: Context, photoUriDto: PhotoUriDto): Bitmap? {
        val bitmap: Bitmap? = try {
            when (photoUriDto.isContentUri()) {
                true -> {
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(photoUriDto.photoUri)))
                }
                false -> {
                    BitmapFactory.decodeFile(photoUriDto.getFilePath())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
           null
        }
        return bitmap
    }
}
