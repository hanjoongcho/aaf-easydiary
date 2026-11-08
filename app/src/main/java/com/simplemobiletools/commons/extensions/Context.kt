package com.simplemobiletools.commons.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.helpers.MyContentProvider.Companion.COL_BACKGROUND_COLOR
import com.simplemobiletools.commons.helpers.MyContentProvider.Companion.COL_LAST_UPDATED_TS
import com.simplemobiletools.commons.helpers.MyContentProvider.Companion.COL_PRIMARY_COLOR
import com.simplemobiletools.commons.helpers.MyContentProvider.Companion.COL_TEXT_COLOR
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.*
import me.blog.korn123.easydiary.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

val Context.isRTLLayout: Boolean get() = if (isJellyBean1Plus()) resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL else false

fun Context.updateTextColors(
    viewGroup: ViewGroup,
    tmpTextColor: Int = 0,
    tmpAccentColor: Int = 0,
) {
    val textColor = if (tmpTextColor == 0) baseConfig.textColor else tmpTextColor
    val backgroundColor = baseConfig.backgroundColor
    val accentColor =
        if (tmpAccentColor == 0) {
            if (isBlackAndWhiteTheme()) {
                Color.WHITE
            } else {
                baseConfig.primaryColor
            }
        } else {
            tmpAccentColor
        }

    val cnt = viewGroup.childCount
    (0 until cnt)
        .map { viewGroup.getChildAt(it) }
        .forEach {
            when (it) {
                is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)

                is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)

                is MySwitchCompat -> it.setColors(textColor, accentColor, backgroundColor)

                //                    is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
//                    is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
                is MyEditText -> it.setColors(textColor, accentColor, backgroundColor)

                is MyFloatingActionButton -> it.setColors(textColor, accentColor, backgroundColor)

                is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)

                is MyButton -> it.setColors(textColor, accentColor, backgroundColor)

                is ViewGroup -> updateTextColors(it, textColor, accentColor)
            }
        }
}

fun Context.getLinkTextColor(): Int =
    if (baseConfig.primaryColor == resources.getColor(R.color.color_primary)) {
        baseConfig.primaryColor
    } else {
        baseConfig.textColor
    }

fun Context.isBlackAndWhiteTheme() = baseConfig.textColor == Color.WHITE && baseConfig.primaryColor == Color.BLACK && baseConfig.backgroundColor == Color.BLACK

fun Context.getAdjustedPrimaryColor() = if (isBlackAndWhiteTheme()) Color.WHITE else baseConfig.primaryColor

fun Context.toast(
    id: Int,
    length: Int = Toast.LENGTH_SHORT,
) {
    Toast.makeText(this, id, length).show()
}

fun Context.toast(
    msg: String,
    length: Int = Toast.LENGTH_SHORT,
) {
    Toast.makeText(this, msg, length).show()
}

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

fun Context.getLatestMediaId(uri: Uri = MediaStore.Files.getContentUri("external")): Long {
    val MAX_VALUE = "max_value"
    val projection = arrayOf("MAX(${BaseColumns._ID}) AS $MAX_VALUE")
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor?.moveToFirst() == true) {
            return cursor.getLongValue(MAX_VALUE)
        }
    } finally {
        cursor?.close()
    }
    return 0
}

fun Context.getLatestMediaByDateId(uri: Uri = MediaStore.Files.getContentUri("external")): Long {
    val projection = arrayOf(BaseColumns._ID)
    val sortOrder = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, null, null, sortOrder)
        if (cursor?.moveToFirst() == true) {
            return cursor.getLongValue(BaseColumns._ID)
        }
    } finally {
        cursor?.close()
    }
    return 0
}

// some helper functions were taken from https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
@SuppressLint("NewApi")
fun Context.getRealPathFromURI(uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }

    if (isKitkatPlus()) {
        if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri)
            if (id.areDigitsOnly()) {
                val newUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
                val path = getDataColumn(newUri)
                if (path != null) {
                    return path
                }
            }
        } else if (isExternalStorageDocument(uri)) {
            val documentId = DocumentsContract.getDocumentId(uri)
            val parts = documentId.split(":")
            if (parts[0].equals("primary", true)) {
                return "${Environment.getExternalStorageDirectory().absolutePath}/${parts[1]}"
            }
        } else if (isMediaDocument(uri)) {
            val documentId = DocumentsContract.getDocumentId(uri)
            val split = documentId.split(":").dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            val contentUri =
                when (type) {
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            val path = getDataColumn(contentUri, selection, selectionArgs)
            if (path != null) {
                return path
            }
        }
    }

    return getDataColumn(uri)
}

fun Context.getDataColumn(
    uri: Uri,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
): String? {
    var cursor: Cursor? = null
    try {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor?.moveToFirst() == true) {
            return cursor.getStringValue(MediaStore.Images.Media.DATA)
        }
    } catch (e: Exception) {
    } finally {
        cursor?.close()
    }
    return null
}

private fun isMediaDocument(uri: Uri) = uri.authority == "com.android.providers.media.documents"

private fun isDownloadsDocument(uri: Uri) = uri.authority == "com.android.providers.downloads.documents"

private fun isExternalStorageDocument(uri: Uri) = uri.authority == "com.android.externalstorage.documents"

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PackageManager.PERMISSION_GRANTED

fun Context.getPermissionString(id: Int) =
    when (id) {
        PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
        PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        PERMISSION_CAMERA -> Manifest.permission.CAMERA
        PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
        PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
        PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
        PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
        PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
        PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
        else -> ""
    }

fun Context.getFilePublicUri(
    file: File,
    applicationId: String,
): Uri {
    // try getting a media content uri first, like content://media/external/images/media/438
    // if media content uri is null, get our custom uri like content://com.simplemobiletools.gallery.provider/external_files/emulated/0/DCIM/IMG_20171104_233915.jpg
    return getMediaContentUri(file.absolutePath) ?: FileProvider.getUriForFile(this, "$applicationId.provider", file)
}

fun Context.getMediaContentUri(path: String): Uri? {
    val uri =
        when {
            path.isImageFast() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

    return getMediaContent(path, uri) ?: getMediaContent(path, MediaStore.Files.getContentUri("external")) ?: null
}

fun Context.getMediaContent(
    path: String,
    uri: Uri,
): Uri? {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = MediaStore.Images.Media.DATA + "= ?"
    val selectionArgs = arrayOf(path)
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor?.moveToFirst() == true) {
            val id = cursor.getIntValue(MediaStore.Images.Media._ID).toString()
            return Uri.withAppendedPath(uri, id)
        }
    } catch (e: Exception) {
    } finally {
        cursor?.close()
    }
    return null
}

fun Context.getFilenameFromUri(uri: Uri): String =
    if (uri.scheme == "file") {
        File(uri.toString()).name
    } else {
        var name = getFilenameFromContentUri(uri) ?: ""
        if (name.isEmpty()) {
            name = uri.lastPathSegment ?: ""
        }
        name
    }

fun Context.getMimeTypeFromUri(uri: Uri): String {
    var mimetype = uri.path!!.getMimeType()
    if (mimetype.isEmpty()) {
        try {
            mimetype = contentResolver.getType(uri)!!
        } catch (e: IllegalStateException) {
        }
    }
    return mimetype
}

fun Context.getFilenameFromContentUri(uri: Uri): String? {
    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor?.moveToFirst() == true) {
            return cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
        }
    } catch (e: Exception) {
    } finally {
        cursor?.close()
    }
    return ""
}

fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    var wasSharedThemeReturned = false
    val cursorLoader = CursorLoader(this, MyContentProvider.CONTENT_URI, null, null, null, null)
    Thread {
        val cursor = cursorLoader.loadInBackground()

        cursor?.use {
            if (cursor.moveToFirst()) {
                val textColor = cursor.getIntValue(COL_TEXT_COLOR)
                val backgroundColor = cursor.getIntValue(COL_BACKGROUND_COLOR)
                val primaryColor = cursor.getIntValue(COL_PRIMARY_COLOR)
                val lastUpdatedTS = cursor.getIntValue(COL_LAST_UPDATED_TS)
                val sharedTheme = SharedTheme(textColor, backgroundColor, primaryColor, lastUpdatedTS)
                callback(sharedTheme)
                wasSharedThemeReturned = true
            }
        }

        if (!wasSharedThemeReturned) {
            callback(null)
        }
    }.start()
}

fun Context.getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

fun Context.getUriMimeType(
    path: String,
    newUri: Uri,
): String {
    var mimeType = path.getMimeType()
    if (mimeType.isEmpty()) {
        mimeType = getMimeTypeFromUri(newUri)
    }
    return mimeType
}

fun Context.isThankYouInstalled() = isPackageInstalled("com.simplemobiletools.thankyou")

fun Context.isPackageInstalled(pkgName: String): Boolean =
    try {
        packageManager.getPackageInfo(pkgName, 0)
        true
    } catch (e: Exception) {
        false
    }

// format day bits to strings like "Mon, Tue, Wed"
fun Context.getSelectedDaysString(bitMask: Int): String {
    val dayBits = arrayListOf(MONDAY_BIT, TUESDAY_BIT, WEDNESDAY_BIT, THURSDAY_BIT, FRIDAY_BIT, SATURDAY_BIT, SUNDAY_BIT)
    val weekDays = resources.getStringArray(R.array.week_days).toList() as ArrayList<String>

    if (baseConfig.isSundayFirst) {
        dayBits.moveLastItemToFront()
        weekDays.moveLastItemToFront()
    }

    var days = ""
    dayBits.forEachIndexed { index, bit ->
        if (bitMask and bit != 0) {
            days += "${weekDays[index].substringTo(3)}, "
        }
    }
    return days.trim().trimEnd(',')
}

fun Context.formatMinutesToTimeString(totalMinutes: Int) = formatSecondsToTimeString(totalMinutes * 60)

fun Context.formatSecondsToTimeString(totalSeconds: Int): String {
    val days = totalSeconds / DAY_SECONDS
    val hours = (totalSeconds % DAY_SECONDS) / HOUR_SECONDS
    val minutes = (totalSeconds % HOUR_SECONDS) / MINUTE_SECONDS
    val seconds = totalSeconds % MINUTE_SECONDS
    val timesString = StringBuilder()
    if (days > 0) {
        val daysString = String.format(resources.getQuantityString(R.plurals.days, days, days))
        timesString.append("$daysString, ")
    }

    if (hours > 0) {
        val hoursString = String.format(resources.getQuantityString(R.plurals.hours, hours, hours))
        timesString.append("$hoursString, ")
    }

    if (minutes > 0) {
        val minutesString = String.format(resources.getQuantityString(R.plurals.minutes, minutes, minutes))
        timesString.append("$minutesString, ")
    }

    if (seconds > 0) {
        val secondsString = String.format(resources.getQuantityString(R.plurals.seconds, seconds, seconds))
        timesString.append(secondsString)
    }

    var result = timesString.toString().trim().trimEnd(',')
    if (result.isEmpty()) {
        result = String.format(resources.getQuantityString(R.plurals.minutes, 0, 0))
    }
    return result
}

fun Context.getFormattedMinutes(
    minutes: Int,
    showBefore: Boolean = true,
) = getFormattedSeconds(minutes * 60, showBefore)

fun Context.getFormattedSeconds(
    seconds: Int,
    showBefore: Boolean = true,
) = when (seconds) {
    -1 -> {
        getString(R.string.no_reminder)
    }

    0 -> {
        getString(R.string.at_start)
    }

    else -> {
        if (seconds % YEAR_SECONDS == 0) {
            resources.getQuantityString(R.plurals.years, seconds / YEAR_SECONDS, seconds / YEAR_SECONDS)
        }

        when {
            seconds % MONTH_SECONDS == 0 -> {
                resources.getQuantityString(R.plurals.months, seconds / MONTH_SECONDS, seconds / MONTH_SECONDS)
            }

            seconds % WEEK_SECONDS == 0 -> {
                resources.getQuantityString(R.plurals.weeks, seconds / WEEK_SECONDS, seconds / WEEK_SECONDS)
            }

            seconds % DAY_SECONDS == 0 -> {
                resources.getQuantityString(R.plurals.days, seconds / DAY_SECONDS, seconds / DAY_SECONDS)
            }

            seconds % HOUR_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.hours_before else R.plurals.by_hours
                resources.getQuantityString(base, seconds / HOUR_SECONDS, seconds / HOUR_SECONDS)
            }

            seconds % MINUTE_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.minutes_before else R.plurals.by_minutes
                resources.getQuantityString(base, seconds / MINUTE_SECONDS, seconds / MINUTE_SECONDS)
            }

            else -> {
                val base = if (showBefore) R.plurals.seconds_before else R.plurals.by_seconds
                resources.getQuantityString(base, seconds, seconds)
            }
        }
    }
}
