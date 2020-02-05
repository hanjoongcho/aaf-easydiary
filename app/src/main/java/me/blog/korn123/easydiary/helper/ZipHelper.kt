package me.blog.korn123.easydiary.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipHelper(val context: Context) {
    private lateinit var mBuilder: NotificationCompat.Builder
    private val mTitle = "Compressing all files..."
    private val mFileNames = ArrayList<String>()
    private var mRootDirectoryName: String? = null

    @SuppressLint("NewApi")
    fun showNotification() {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_HIGH
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
                .setContentTitle("Compressing all files...")
                .setContentText("...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(0, mBuilder.build())
    }

    fun updateNotification(message: String, title: String) {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        mBuilder.setContentTitle(title)
        mBuilder.setContentText(message)
        notificationManager.notify(0, mBuilder.build())
    }

    fun determineFiles(targetDirectoryName: String) {
        this.mRootDirectoryName = targetDirectoryName
        determineFiles(targetDirectoryName, null)
    }

    fun determineFiles(targetDirectory: String, basePath: String?) {
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
        showNotification()
        val zipOutputStream: ZipOutputStream
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(destFile))

            mFileNames.forEachIndexed { index, fileName ->
                updateNotification("$index/${mFileNames.size} $fileName", mTitle)
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
                System.out.println("Unzipping to " + newFile.absolutePath)

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