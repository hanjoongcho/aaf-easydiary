package me.blog.korn123.easydiary.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.services.NotificationService
import org.apache.commons.io.IOUtils
import java.io.*
import java.net.URLDecoder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipHelper(val context: Context) {
    private lateinit var mBuilder: NotificationCompat.Builder
    private val mFileNames = ArrayList<String>()
    private var mRootDirectoryName: String? = null
    var isOnProgress = true

    @SuppressLint("NewApi")
    fun showNotification(title: String, message: String, actionString: String) {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
//            val importance = NotificationManager.IMPORTANCE_HIGH
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_compress", "${NOTIFICATION_CHANNEL_NAME}_compress", importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(mChannel)
        }

        mBuilder = NotificationCompat.Builder(context, "${NOTIFICATION_CHANNEL_ID}_compress")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setOngoing(false)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(message))
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                        R.drawable.ic_launcher_round,
                        context.getString(R.string.cancel),
                        PendingIntent.getService(context, 0, Intent(context, NotificationService::class.java).apply {
                            action = actionString
                        }, 0)
                )
        notificationManager.notify(NOTIFICATION_COMPLETE_ID, mBuilder.build())
    }

    private fun updateNotification(progress: Int) {
        if (isOnProgress) {
            val message = mFileNames[progress]
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder.setProgress(mFileNames.size, progress.plus(1), false)
                    .setContentTitle("${progress.plus(1)}/${mFileNames.size}")
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(message))
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, mBuilder.build())
        }
    }

    private fun updateDecompressProgress(progress: Int, totalCount: Int, fileName: String) {
        if (isOnProgress) {
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder.setProgress(totalCount, progress.plus(1), false)
                    .setContentTitle("${progress.plus(1)}/$totalCount")
                    .setContentText(fileName)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(fileName).setSummaryText(fileName))
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, mBuilder.build())
        }
    }

    fun updateNotification(title: String, message: String) {
        if (isOnProgress) {
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder.mActions.clear()
            mBuilder/*.setProgress(0, 0, false)*/
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(message))
                    .addAction(
                            R.drawable.ic_launcher_round,
                            context.getString(R.string.dismiss),
                            PendingIntent.getService(context, 0, Intent(context, NotificationService::class.java).apply {
                                action = NotificationService.ACTION_DISMISS
                            }, 0)
                    )
            notificationManager.notify(NOTIFICATION_COMPLETE_ID, mBuilder.build())
        }
    }

    fun determineFiles(targetDirectoryName: String) {
        this.mRootDirectoryName = targetDirectoryName
        determineFiles(targetDirectoryName, null)
    }

    private fun determineFiles(targetDirectory: String, basePath: String?) {
        val sourceDir = File(targetDirectory)
        for (file in sourceDir.listFiles()) {
            if (file.isDirectory) {
                val currentBasePath = if (basePath != null) basePath + file.name + File.separator else file.name + File.separator
                determineFiles(file.absolutePath, currentBasePath)
            } else {
                if (basePath == null) {
                    mFileNames.add(file.name)
                } else {
                    mFileNames.add(basePath + file.name)
                }
            }
        }
    }

    fun compress(destFile: File) {
        showNotification("Full data backup", "Preparing to backup all data ...", NotificationService.ACTION_FULL_BACKUP_CANCEL)
        val zipOutputStream: ZipOutputStream
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(destFile))

            mFileNames.forEachIndexed { index, fileName ->
                if (!isOnProgress) return@forEachIndexed
                updateNotification(index)
                try {
                    val fileInputStream = FileInputStream(mRootDirectoryName + fileName)
                    zipOutputStream.putNextEntry(ZipEntry(fileName))
                    val bytes = IOUtils.toByteArray(fileInputStream)
                    zipOutputStream.write(bytes, 0, bytes.size)
                    zipOutputStream.closeEntry()
                    fileInputStream.close()
                } catch (ex: IOException) {
                    System.err.println(ex)
                }
            }
            zipOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun countFileEntry(uri: Uri?): Int {
        val uriStream = context.contentResolver.openInputStream(uri!!)
        var count = 0
        try {
            val zipInputStream = ZipInputStream(uriStream)
            while (zipInputStream.nextEntry != null) {
                zipInputStream.closeEntry()
                count++
            }
            zipInputStream.close()
            uriStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return count
    }

    fun decompress(uri: Uri?) {
        showNotification("Full data recovery", "Recovery of all data is in progress.", NotificationService.ACTION_FULL_RECOVERY_CANCEL)
        val fileCount = countFileEntry(uri)
        val uriStream = context.contentResolver.openInputStream(uri!!)
        val buffer = ByteArray(1024)
        try {
            val zipInputStream = ZipInputStream(uriStream)
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            val workingPath =  EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY
            var index = 0
            while (zipEntry != null) {
                if (!isOnProgress) break
                val fileName = zipEntry.name
                Log.i(AAF_TEST, fileName)
                val newFile = File(workingPath + fileName)

                File(newFile.parent).mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var len: Int
                writeLoop@ while (true) {
                    len = zipInputStream.read(buffer)
                    if (len > 0) {
                        fileOutputStream.write(buffer, 0, len)
                    } else {
                        break@writeLoop
                    }
                }
                fileOutputStream.close()

                zipInputStream.closeEntry()
                zipEntry = zipInputStream.nextEntry
                updateDecompressProgress(index++, fileCount, fileName)
            }

            zipInputStream.closeEntry()
            zipInputStream.close()
            uriStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun decompress(zipFileName: String, compressDirectoryName: String) {
        val dir = File(compressDirectoryName)
        if (!dir.exists()) dir.mkdirs()

        val fileInputStream: FileInputStream
        val buffer = ByteArray(1024)
        try {
            fileInputStream = FileInputStream(zipFileName)
            val zipInputStream = ZipInputStream(fileInputStream)
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(compressDirectoryName + File.separator + fileName)

                File(newFile.parent).mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var len: Int
                writeLoop@ while (true) {
                    len = zipInputStream.read(buffer)
                    if (len > 0) {
                        fileOutputStream.write(buffer, 0, len)
                    } else {
                        break@writeLoop
                    }
                }
                fileOutputStream.close()

                zipInputStream.closeEntry()
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
            zipInputStream.close()
            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun printFileNames() {
        for (fileName in mFileNames) {
            Log.i("aaf", fileName)
        }
    }
}