package me.blog.korn123.commons.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Created by hanjoong on 2017-06-08.
 */

object BitmapUtils {

    @Throws(FileNotFoundException::class, SecurityException::class)
    fun decodeUri(c: Context, uri: Uri, requiredSize: Int, fixedWidth: Int, fixedHeight: Int): Bitmap {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri), null, o)

        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        o2.outWidth = fixedWidth
        o2.outHeight = fixedHeight
        val tempBitmap = BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri), null, o2)
        return Bitmap.createScaledBitmap(tempBitmap, fixedWidth, fixedHeight, false)
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
                out!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (view is SurfaceView) {
            view.setZOrderOnTop(true)
            view.draw(canvas)
            view.setZOrderOnTop(false)
            return bitmap
        } else {
            //For ViewGroup & View
            view.draw(canvas)
            return bitmap
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
