package me.blog.korn123.easydiary.extensions

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun Bitmap.cropCenter(): Bitmap =
    when (width >= height) {
        true -> {
            Bitmap.createBitmap(
                this,
                width / 2 - height / 2,
                0,
                height,
                height,
            )
        }

        false -> {
            Bitmap.createBitmap(
                this,
                0,
                height / 2 - width / 2,
                width,
                width,
            )
        }
    }

fun Bitmap.saveBitmapToFileCache(strFilePath: String) {
    val fileCacheItem = File(strFilePath)
    try {
        fileCacheItem.createNewFile()
        FileOutputStream(fileCacheItem).use { out ->
            this.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
