package me.blog.korn123.commons.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.util.TypedValue
import id.zelory.compressor.Compressor
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object CommonUtils {
    fun dpToPixelFloatValue(context: Context, dp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics)
    }

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
    
    fun photoUriToDownSamplingBitmap(
            context: Context,
            photoUriDto: PhotoUriDto,
            requiredSize: Int = 50,
            fixedWidth: Int = 45,
            fixedHeight: Int = 45
    ): Bitmap = try {
            when (photoUriDto.isContentUri()) {
                true -> {
                    BitmapUtils.decodeUri(context, Uri.parse(photoUriDto.photoUri), CommonUtils.dpToPixel(context, requiredSize, 1), CommonUtils.dpToPixel(context, fixedWidth, 1), CommonUtils.dpToPixel(context, fixedHeight, 1))
                }
                false -> {
                    BitmapUtils.decodeFile(context, photoUriDto.getFilePath(), CommonUtils.dpToPixel(context, requiredSize, 1), CommonUtils.dpToPixel(context, fixedWidth, 1), CommonUtils.dpToPixel(context, fixedHeight, 1))
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
        } catch (se: SecurityException) {
            se.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.question_shield)
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

    fun getDefaultDisplay(activity: Activity): Point {
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }
}
