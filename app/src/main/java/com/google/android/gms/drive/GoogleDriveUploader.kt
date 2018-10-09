package com.google.android.gms.drive

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.helper.DIARY_DB_NAME
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

/**
 * Created by Administrator on 2017-11-21.
 */

class GoogleDriveUploader : BaseDriveActivity() {

    override fun onDriveClientReady() {
        createFileWithIntent()
    }

    private fun createFileWithIntent() {
        // [START drive_android_create_file_with_intent]
        val createContentsTask = driveResourceClient?.createContents()
        createContentsTask?.let {
            it.continueWithTask { task ->
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
                driveClient?.newCreateFileActivityIntentSender(createOptions)
            }.addOnSuccessListener(this) { intentSender ->

                try {
                    startIntentSenderForResult(
                            intentSender, REQUEST_CODE_CREATE_FILE, null, 0, 0, 0)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Unable to create file", e)
                    showMessage(getString(io.github.aafactory.commons.R.string.file_create_error))
                    finish()
                }
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "Unable to create file", e)
                showMessage(getString(io.github.aafactory.commons.R.string.file_create_error))
                finish()
            }
        }
        // [END drive_android_create_file_with_intent]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CREATE_FILE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "Unable to create file")
                showMessage(getString(io.github.aafactory.commons.R.string.file_create_error))
            } else {
                data?.let {
                    val driveId = it.getParcelableExtra<DriveId>(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
//                    showMessage(getString(io.github.aafactory.commons.R.string.file_created, "File created with ID: $driveId"))
                }
            }
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val TAG = "GoogleDriveUploader"
        private const val REQUEST_CODE_CREATE_FILE = 2
    }
}