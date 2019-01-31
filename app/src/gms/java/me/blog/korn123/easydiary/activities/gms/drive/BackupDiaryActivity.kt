package me.blog.korn123.easydiary.gms.drive

import android.content.DialogInterface
import com.google.android.gms.drive.BaseDriveActivity
import com.google.android.gms.drive.CreateFileActivityOptions
import com.google.android.gms.drive.MetadataChangeSet
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.DIARY_DB_NAME
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

/**
 * Created by Administrator on 2017-11-21.
 */

class BackupDiaryActivity : BaseDriveActivity() {
    override fun onDriveClientReady() {
        createFileWithIntent()
    }

    override fun addListener() {
        mTask?.let {
            it.addOnSuccessListener(this) { driveId ->
                holdCurrentOrientation()
                showAlertDialog(getString(io.github.aafactory.commons.R.string.file_saved), DialogInterface.OnClickListener { _, _ ->
                    pauseLock()
                    finish()
                }, false)
            }.addOnFailureListener(this) { e ->
                holdCurrentOrientation()
                showAlertDialog(getString(R.string.folder_not_selected), DialogInterface.OnClickListener { _, _ ->
                    pauseLock()
                    finish()
                }, false)
            }
        }
    }

    override fun showDialog() {}
    
    private fun createFileWithIntent() {
        // [START drive_android_create_file_with_intent]
        driveResourceClient?.createContents()?.continueWith { task ->
            val contents = task.result
            val outputStream = contents.outputStream
            val backupFile = File(EasyDiaryDbHelper.getInstance().path)
            try {
                FileUtils.copyFile(backupFile, outputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val changeSet = MetadataChangeSet.Builder()
                    .setTitle(DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"))
                    .setMimeType(EasyDiaryUtils.easyDiaryMimeType)
                    .setStarred(true)
                    .build()

            val createOptions = CreateFileActivityOptions.Builder()
                    .setInitialDriveContents(contents)
                    .setInitialMetadata(changeSet)
                    .build()
            pickUploadFolder(createOptions)
        }
        // [END drive_android_create_file_with_intent]
    }

    companion object {
        private const val TAG = "BackupDiaryActivity"
    }
}