package me.blog.korn123.easydiary.gms.drive

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import com.google.android.gms.drive.BaseDriveActivity
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.OpenFileActivityOptions
import com.google.android.gms.drive.events.OpenFileCallback
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.pauseLock
import org.apache.commons.io.IOUtils
import java.io.FileOutputStream

class RecoverDiaryActivity : BaseDriveActivity() {
    
    override fun onDriveClientReady() {
        val openOptions = OpenFileActivityOptions.Builder()
                .setActivityTitle(getString(io.github.aafactory.commons.R.string.select_file))
                .setMimeType(EasyDiaryUtils.easyDiaryMimeTypeAll.asList())
                .build()
        pickItem(openOptions)
    }

    override fun addListener() {
        mTask?.let {
            it.addOnSuccessListener(this) { driveId ->
                retrieveContents(driveId.asDriveFile())
            }.addOnFailureListener(this) { e ->
                Log.e(TAG, "No folder selected", e)
                showAlertDialog(getString(R.string.folder_not_selected), DialogInterface.OnClickListener { _, _ ->
                    pauseLock()
                    finish()
                }, false)
            }
        }
    }

    private fun retrieveContents(file: DriveFile) {
        // [START drive_android_read_with_progress_listener]
        val openCallback = object : OpenFileCallback() {
            override fun onProgress(bytesDownloaded: Long, bytesExpected: Long) {}

            override fun onContents(driveContents: DriveContents) {
                try {
                    val driveContents = driveContents.inputStream
                    val outputStream = FileOutputStream(EasyDiaryDbHelper.getInstance().path)
                    IOUtils.copy(driveContents, outputStream)
                    IOUtils.closeQuietly(outputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                config.aafPinLockPauseMillis = System.currentTimeMillis()
                val context = this@RecoverDiaryActivity
                val readDiaryIntent = Intent(context, DiaryMainActivity::class.java)
                readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                val mPendingIntentId = 123456
                val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, readDiaryIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                System.exit(0)
            }

            override fun onError(e: Exception) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e)
                showMessage(getString(io.github.aafactory.commons.R.string.read_failed))
                finish()
                // [END_EXCLUDE]
            }
        }

        driveResourceClient?.openFile(file, DriveFile.MODE_READ_ONLY, openCallback)
        // [END drive_android_read_with_progress_listener]
    }

    companion object {
        private const val TAG = "GoogleDriveDownloader"
    }
}