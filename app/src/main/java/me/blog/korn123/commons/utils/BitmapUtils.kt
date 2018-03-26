package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.view.ViewGroup
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*

/**
 * Created by hanjoong on 2017-06-08.
 */

object BitmapUtils {

    fun decodeFile(context: Context, uri: Uri, fixedWidth: Int, fixedHeight: Int): Bitmap {
        var inputStream: InputStream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        BitmapFactory.decodeStream(inputStream, null, options)
        IOUtils.closeQuietly(inputStream)
        val inSampleSize = calculateInSampleSize(options, fixedWidth, fixedHeight)

        inputStream = context.contentResolver.openInputStream(uri)
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        val tempBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        return Bitmap.createScaledBitmap(tempBitmap, fixedWidth, fixedHeight, false)
    }

    fun decodeFile(path: String, fixedWidth: Int, fixedHeight: Int): Bitmap {
        var inputStream: InputStream = FileUtils.openInputStream(File(path))
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(inputStream, null, options)
        IOUtils.closeQuietly(inputStream)
        val inSampleSize = calculateInSampleSize(options, fixedWidth, fixedHeight)

        inputStream = FileUtils.openInputStream(File(path))
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        val tempBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        return Bitmap.createScaledBitmap(tempBitmap, fixedWidth, fixedHeight, false)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val h = options.outHeight
        val w = options.outWidth
        var inSampleSize = 0
        if (h > reqHeight || w > reqWidth) {
            val ratioW = w.toFloat() / reqWidth
            val ratioH = h.toFloat() / reqHeight
            inSampleSize = Math.min(ratioH, ratioW).toInt()
        }
        inSampleSize = Math.max(1, inSampleSize)
        return inSampleSize
    }
    
    fun decodeFileCropCenter(path: String, fixedWidthHeight: Int): Bitmap {
        var inputStream: InputStream = FileUtils.openInputStream(File(path))
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        BitmapFactory.decodeStream(inputStream, null, options)
        IOUtils.closeQuietly(inputStream)
        val inSampleSize = calculateInSampleSize(options, fixedWidthHeight, fixedWidthHeight)
        inputStream = FileUtils.openInputStream(File(path))
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize 
        val tempBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        val sampling = when (tempBitmap.width > tempBitmap.height) {
            true -> {
                val ratio: Float = fixedWidthHeight * 1.0F / tempBitmap.height 
                Bitmap.createScaledBitmap(tempBitmap, (tempBitmap.width * ratio).toInt(), (tempBitmap.height * ratio).toInt(), false) 
            }
            false -> {
                val ratio: Float = fixedWidthHeight * 1.0F / tempBitmap.width 
                Bitmap.createScaledBitmap(tempBitmap, (tempBitmap.width * ratio).toInt(), (tempBitmap.height * ratio).toInt(), false)
            }
        }
        return cropCenter(sampling)
    }
    
    fun cropCenter(srcBmp: Bitmap): Bitmap {
        return when (srcBmp.width >= srcBmp.height) {
            true -> {
                Bitmap.createBitmap(
                        srcBmp,
                        srcBmp.width / 2 - srcBmp.height / 2,
                        0,
                        srcBmp.height,
                        srcBmp.height
                )
            }
            false -> {
                Bitmap.createBitmap(
                        srcBmp,
                        0,
                        srcBmp.height / 2 - srcBmp.width / 2,
                        srcBmp.width,
                        srcBmp.width
                )
            }
        }
    }

    fun saveBitmapToFileCache(bitmap: Bitmap, strFilePath: String) {
        val fileCacheItem = File(strFilePath)
        var out: OutputStream? = null

        try {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)

            //quality int: Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning compress for max quality. Some formats, like PNG which is lossless, will ignore the quality setting
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun diaryViewGroupToBitmap(view: ViewGroup): Bitmap {
        val scrollView = view.getChildAt(0) as ViewGroup
        val bitmap = Bitmap.createBitmap(scrollView.width, scrollView.getChildAt(0).height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        scrollView.draw(canvas)
        return bitmap
    }
}
