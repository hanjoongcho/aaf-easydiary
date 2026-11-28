package me.blog.korn123.easydiary.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.simplemobiletools.commons.helpers.isOreoPlus
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.pendingIntentFlag
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException as Zip4jException
import net.lingala.zip4j.headers.HeaderReader
import net.lingala.zip4j.io.inputstream.ZipInputStream as Zip4jInputStream
import net.lingala.zip4j.model.LocalFileHeader
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipHelper(
    val context: Context,
) {
    /**
     * What a selected archive turned out to be, read from its first local file header before anything is extracted.
     * [INVALID] covers everything that is not a readable zip, so a recovery can be refused without touching data.
     */
    enum class ArchiveStatus {
        PLAIN,
        ENCRYPTED,
        INVALID,
    }

    /** Result of checking a password against an encrypted archive. */
    enum class PasswordStatus {
        CORRECT,
        WRONG,
        INVALID_ARCHIVE,
    }

    /** How an extraction ended, so the caller can report a wrong password apart from a broken archive. */
    enum class ExtractionResult {
        COMPLETED,
        CANCELLED,
        WRONG_PASSWORD,
        INVALID_ARCHIVE,
        FAILED,
    }

    private lateinit var mBuilder: NotificationCompat.Builder
    private val mFileNames = ArrayList<String>()
    private var mRootDirectoryName: String? = null
    private var lastUpdateTime = 0L
    private val updateInterval = 1000L
    var isOnProgress = true

    @SuppressLint("NewApi")
    fun createNotification(
        notificationId: Int,
        title: String,
        message: String,
        actionString: String,
    ): Notification {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        if (isOreoPlus()) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_zip_helper", context.getString(R.string.notification_channel_name_zip_helper), importance)
            mChannel.description = NOTIFICATION_CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(mChannel)
        }

        mBuilder =
            NotificationCompat
                .Builder(context, "${NOTIFICATION_CHANNEL_ID}_zip_helper")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_diary_backup_local))
                .setOngoing(false)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true)
                .setContentTitle(if (context.config.enableDebugOptionVisibleAlarmSequence) "[$notificationId] $title" else title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                    R.drawable.ic_easydiary,
                    context.getString(R.string.cancel),
                    PendingIntent.getService(
                        context,
                        notificationId,
                        Intent(context, NotificationService::class.java).apply {
                            action = actionString
                        },
                        context.pendingIntentFlag(),
                    ),
                )
        return mBuilder.build()
    }

    fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        actionString: String,
    ) {
        val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(notificationId, title, message, actionString)
        notificationManager.notify(notificationId, notification)
    }

    private fun updateCompressProgress(progress: Int) {
        Log.i(AAF_TEST, "updateCompressProgress ${progress.plus(1)}/${mFileNames.size}")
        val currentTime = System.currentTimeMillis()
        if ((isOnProgress && progress == mFileNames.size.minus(1)) || currentTime - lastUpdateTime >= updateInterval) {
            lastUpdateTime = currentTime
            val message = mFileNames[progress]
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder
                .setProgress(mFileNames.size, progress.plus(1), false)
                .setContentTitle(if (context.config.enableDebugOptionVisibleAlarmSequence) "[$NOTIFICATION_COMPRESS_ID] ${progress.plus(1)}/${mFileNames.size}" else "${progress.plus(1)}/${mFileNames.size}")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(context.getString(R.string.backup_full_notification_compressing)))
            notificationManager.notify(NOTIFICATION_COMPRESS_ID, mBuilder.build())
        }
    }

    private fun updateDecompressProgress(
        progress: Int,
        totalCount: Int,
        fileName: String,
    ) {
        val currentTime = System.currentTimeMillis()
        if ((isOnProgress && progress == totalCount - 1) || currentTime - lastUpdateTime >= updateInterval) {
            lastUpdateTime = currentTime
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder
                .setProgress(totalCount, progress.plus(1), false)
                .setContentTitle(if (context.config.enableDebugOptionVisibleAlarmSequence) "[$NOTIFICATION_COMPRESS_ID] ${progress.plus(1)}/$totalCount" else "${progress.plus(1)}/$totalCount")
                .setContentText(fileName)
                .setStyle(NotificationCompat.BigTextStyle().bigText(fileName).setSummaryText(context.getString(R.string.recovery_full_notification_decompressing)))
            notificationManager.notify(NOTIFICATION_DECOMPRESS_ID, mBuilder.build())
        }
    }

    @SuppressLint("RestrictedApi")
    fun updateNotification(
        notificationId: Int,
        title: String,
        message: String,
    ) {
        if (isOnProgress) {
            val actionFlag = if (notificationId == NOTIFICATION_COMPRESS_ID || notificationId == NOTIFICATION_COMPRESS_COMPLETE_ID) NotificationConstants.ACTION_DISMISS_COMPRESS else NotificationConstants.ACTION_DISMISS_DECOMPRESS
            val notificationManager = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            mBuilder.mActions.clear()
            mBuilder
                .setProgress(0, 0, false)
                .setContentTitle(if (context.config.enableDebugOptionVisibleAlarmSequence) "[$notificationId] $title" else title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .addAction(
                    R.drawable.ic_easydiary,
                    context.getString(R.string.dismiss),
                    PendingIntent.getService(
                        context,
                        notificationId,
                        Intent(context, NotificationService::class.java).apply {
                            action = actionFlag
                        },
                        context.pendingIntentFlag(),
                    ),
                )
            notificationManager.notify(notificationId, mBuilder.build())
        }
    }

    fun determineFiles(targetDirectoryName: String) {
        this.mRootDirectoryName = targetDirectoryName
        determineFiles(targetDirectoryName, null)
    }

    private fun determineFiles(
        targetDirectory: String,
        basePath: String?,
    ) {
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
        showNotification(
            NOTIFICATION_COMPRESS_ID,
            context.getString(R.string.backup_full_notification_title),
            context.getString(R.string.backup_full_notification_preparing),
            NotificationConstants.ACTION_FULL_BACKUP_CANCEL,
        )
        try {
            ZipOutputStream(FileOutputStream(destFile)).use { zipOutputStream ->
                mFileNames.forEachIndexed { index, fileName ->
                    if (!isOnProgress) return@forEachIndexed
                    updateCompressProgress(index)
                    try {
                        FileInputStream(mRootDirectoryName + fileName).use { fileInputStream ->
                            zipOutputStream.putNextEntry(ZipEntry(fileName))
                            val bytes = fileInputStream.readBytes()
                            zipOutputStream.write(bytes, 0, bytes.size)
                            zipOutputStream.closeEntry()
                        }
                    } catch (ex: IOException) {
                        System.err.println(ex)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Writes the entries collected by [determineFiles] into [destFile] as an AES-256 encrypted zip.
     * A null or blank [password] produces the plain archive of [compress], so a skipped password changes nothing
     * for a user who does not want encryption.
     */
    fun compress(
        destFile: File,
        password: String?,
    ) {
        if (password.isNullOrEmpty() || mFileNames.isEmpty()) {
            compress(destFile)
            return
        }

        showNotification(
            NOTIFICATION_COMPRESS_ID,
            context.getString(R.string.backup_full_notification_title),
            context.getString(R.string.backup_full_notification_preparing),
            NotificationConstants.ACTION_FULL_BACKUP_CANCEL,
        )
        try {
            ZipFile(destFile, password.toCharArray()).use { zipFile ->
                mFileNames.forEachIndexed { index, fileName ->
                    if (!isOnProgress) return@forEachIndexed
                    updateCompressProgress(index)
                    try {
                        FileInputStream(mRootDirectoryName + fileName).use { fileInputStream ->
                            zipFile.addStream(fileInputStream, encryptedZipParameters(fileName))
                        }
                    } catch (ex: Exception) {
                        // One unreadable file must not fail the whole backup, the same way the plain archive skips it.
                        System.err.println(ex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun encryptedZipParameters(fileName: String) =
        ZipParameters().apply {
            fileNameInZip = fileName
            isEncryptFiles = true
            encryptionMethod = EncryptionMethod.AES
            aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256
            compressionMethod = CompressionMethod.DEFLATE
        }

    /**
     * Counts the entries of the archive behind [uri], which is the total the recovery notification shows as "n/total".
     *
     * A password switches to the encrypted reader for the same reason [decompress] does: the standard [ZipInputStream]
     * cannot get past the encrypted entry header of an AES archive - it refuses the entry - so an encrypted backup used
     * to answer 0 here and the progress read "1/0".
     *
     * zip4j's ZipInputStream has no way to step over an entry without reading it: [Zip4jInputStream.getNextEntry]
     * first drains the entry that is open (reading it to its end, which is also where its AES MAC or CRC is verified)
     * and only then reads the next local file header. Counting an encrypted archive therefore reads it once in full,
     * exactly as counting a plain archive already did, and [decompress] reads it a second time to extract it.
     */
    private fun countFileEntry(
        uri: Uri?,
        password: String? = null,
    ): Int {
        if (uri == null) return 0
        val uriStream = context.contentResolver.openInputStream(uri) ?: return 0
        var count = 0
        try {
            if (password.isNullOrEmpty()) {
                val zipInputStream = ZipInputStream(uriStream)
                while (zipInputStream.nextEntry != null && isOnProgress) {
                    zipInputStream.closeEntry()
                    count++
                }
                zipInputStream.close()
            } else {
                val zipInputStream = Zip4jInputStream(uriStream, password.toCharArray())
                while (zipInputStream.getNextEntry() != null && isOnProgress) {
                    count++
                }
                zipInputStream.close()
            }
        } catch (e: IOException) {
            // A wrong password or a damaged entry stops the count here; decompress reports it from its own read.
            e.printStackTrace()
        } finally {
            try {
                uriStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return count
    }

    /**
     * Reads only the first local file header of the archive behind [uri], which is where a zip records whether its
     * entries are password protected. Nothing is written, so the caller can decide on a password prompt before the
     * extraction - and the restore that follows it - ever starts.
     */
    fun inspectArchive(uri: Uri?): ArchiveStatus {
        if (uri == null) return ArchiveStatus.INVALID
        return try {
            val localFileHeader =
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    HeaderReader().readLocalFileHeader(inputStream, null)
                }
            when {
                localFileHeader == null -> ArchiveStatus.INVALID
                localFileHeader.isEncrypted -> ArchiveStatus.ENCRYPTED
                else -> ArchiveStatus.PLAIN
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ArchiveStatus.INVALID
        }
    }

    /**
     * Checks [password] against the first entry of the archive behind [uri] without extracting anything.
     *
     * Opening an encrypted entry derives the key from the password and compares it with the verifier (AES) or the
     * check byte (ZipCrypto) stored in the archive, so a wrong password is rejected while the archive is still only
     * being read.
     */
    fun verifyPassword(
        uri: Uri?,
        password: String,
    ): PasswordStatus {
        if (uri == null) return PasswordStatus.INVALID_ARCHIVE
        if (password.isEmpty()) return PasswordStatus.WRONG
        return try {
            val localFileHeader =
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    Zip4jInputStream(inputStream, password.toCharArray()).getNextEntry()
                }
            when (localFileHeader) {
                null -> PasswordStatus.INVALID_ARCHIVE
                else -> PasswordStatus.CORRECT
            }
        } catch (e: Zip4jException) {
            e.printStackTrace()
            when (e.type) {
                Zip4jException.Type.WRONG_PASSWORD -> PasswordStatus.WRONG
                else -> PasswordStatus.INVALID_ARCHIVE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PasswordStatus.INVALID_ARCHIVE
        }
    }

    /**
     * Extracts the archive behind [uri] into the working directory.
     *
     * A null or blank [password] walks the plain archive exactly as this class always has. A password switches to the
     * encrypted reader, which is the only one that can decode an AES entry; both readers share the same guard against
     * entry names that would leave the working directory.
     */
    fun decompress(
        uri: Uri?,
        password: String? = null,
    ): ExtractionResult {
        if (uri == null) return ExtractionResult.INVALID_ARCHIVE
        showNotification(
            NOTIFICATION_DECOMPRESS_ID,
            context.getString(R.string.recovery_full_notification_title),
            context.getString(R.string.recovery_full_notification_progress),
            NotificationConstants.ACTION_FULL_RECOVERY_CANCEL,
        )
        val fileCount = countFileEntry(uri, password)
        return when {
            password.isNullOrEmpty() -> decompressPlain(uri, fileCount)
            else -> decompressEncrypted(uri, password, fileCount)
        }
    }

    private fun decompressPlain(
        uri: Uri,
        fileCount: Int,
    ): ExtractionResult {
        val uriStream = context.contentResolver.openInputStream(uri) ?: return ExtractionResult.INVALID_ARCHIVE
        val buffer = ByteArray(1024)
        try {
            val zipInputStream = ZipInputStream(uriStream)
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            var index = 0
            while (zipEntry != null) {
                if (!isOnProgress) return ExtractionResult.CANCELLED
                val fileName = zipEntry.name
                val newFile = resolveWorkingFile(fileName)

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
            return ExtractionResult.COMPLETED
        } catch (e: IOException) {
            e.printStackTrace()
            return ExtractionResult.INVALID_ARCHIVE
        } catch (e: Exception) {
            e.printStackTrace()
            return ExtractionResult.FAILED
        } finally {
            try {
                uriStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun decompressEncrypted(
        uri: Uri,
        password: String,
        fileCount: Int,
    ): ExtractionResult {
        val uriStream = context.contentResolver.openInputStream(uri) ?: return ExtractionResult.INVALID_ARCHIVE
        val buffer = ByteArray(1024)
        try {
            val zipInputStream = Zip4jInputStream(uriStream, password.toCharArray())
            var localFileHeader: LocalFileHeader? = zipInputStream.getNextEntry()
            var index = 0
            while (localFileHeader != null) {
                if (!isOnProgress) return ExtractionResult.CANCELLED
                val fileName = localFileHeader.fileName
                val newFile = resolveWorkingFile(fileName)

                if (!localFileHeader.isDirectory) {
                    File(newFile.parent).mkdirs()
                    FileOutputStream(newFile).use { fileOutputStream ->
                        var len: Int
                        while (true) {
                            len = zipInputStream.read(buffer)
                            if (len > 0) {
                                fileOutputStream.write(buffer, 0, len)
                            } else {
                                break
                            }
                        }
                    }
                }

                localFileHeader = zipInputStream.getNextEntry()
                updateDecompressProgress(index++, fileCount, fileName)
            }
            return when {
                isOnProgress -> ExtractionResult.COMPLETED
                else -> ExtractionResult.CANCELLED
            }
        } catch (e: Zip4jException) {
            e.printStackTrace()
            return when (e.type) {
                Zip4jException.Type.WRONG_PASSWORD -> ExtractionResult.WRONG_PASSWORD
                Zip4jException.Type.CHECKSUM_MISMATCH -> ExtractionResult.INVALID_ARCHIVE
                else -> ExtractionResult.FAILED
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return ExtractionResult.INVALID_ARCHIVE
        } catch (e: Exception) {
            e.printStackTrace()
            return ExtractionResult.FAILED
        } finally {
            try {
                uriStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Maps a zip entry name onto the working directory, refusing names that are absolute or that climb out of it.
     */
    private fun resolveWorkingFile(fileName: String): File {
        if (File(fileName).isAbsolute) {
            throw IOException("ZIP entry has an absolute path: $fileName")
        }

        val workingDirectory = File(EasyDiaryUtils.getApplicationDataDirectory(context) + WORKING_DIRECTORY).canonicalFile
        val workingDirectoryPrefix = if (workingDirectory.path.endsWith(File.separator)) workingDirectory.path else workingDirectory.path + File.separator
        val newFile = File(workingDirectory, fileName).canonicalFile
        if (!newFile.path.startsWith(workingDirectoryPrefix)) {
            throw IOException("ZIP entry escapes the working directory: $fileName")
        }
        return newFile
    }

    fun decompress(
        zipFileName: String,
        compressDirectoryName: String,
    ) {
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
