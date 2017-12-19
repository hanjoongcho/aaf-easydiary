package com.simplemobiletools.commons.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.MenuItem
import android.view.WindowManager
//import com.simplemobiletools.commons.asynctasks.CopyMoveTask
//import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_LICENSES
import com.simplemobiletools.commons.helpers.APP_NAME
import com.simplemobiletools.commons.helpers.APP_VERSION_NAME
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE
import java.io.File
import java.util.*

open class BaseSimpleActivity : AppCompatActivity() {
    var copyMoveCallback: (() -> Unit)? = null
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var useDynamicTheme = true
    private val GENERIC_PERM_HANDLER = 100

    companion object {
        var funAfterSAFPermission: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId())
        }
        super.onCreate(savedInstanceState)
//        if (!packageName.startsWith("com.simplemobiletools.", true)) {
//            if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
//                val label = "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"
//                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {
//                    launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
        if (useDynamicTheme) {
            setTheme(getThemeId())
            updateBackgroundColor()
        }
        updateActionbarColor()
    }

    override fun onStop() {
        super.onStop()
        actionOnPermission = null
    }

    override fun onDestroy() {
        super.onDestroy()
        funAfterSAFPermission = null
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        window.decorView.setBackgroundColor(color)
    }

    fun updateActionbarColor(color: Int = baseConfig.primaryColor) {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
        supportActionBar?.title = Html.fromHtml("<font color='${color.getContrastColor().toHex()}'>${supportActionBar?.title}</font>")
        updateStatusbarColor(color)

        if (isLollipopPlus()) {
            setTaskDescription(ActivityManager.TaskDescription(null, null, color))
        }
    }

    fun updateStatusbarColor(color: Int) {
        if (isLollipopPlus()) {
            window.statusBarColor = color.darkenColor()
        }
    }

    fun setTranslucentNavigation() {
        if (isKitkatPlus()) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
//        super.onActivityResult(requestCode, resultCode, resultData)
//        if (requestCode == OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK && resultData != null) {
//            if (isProperFolder(resultData.data)) {
//                saveTreeUri(resultData)
//                funAfterSAFPermission?.invoke()
//            } else {
//                toast(R.string.wrong_root_selected)
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
//                startActivityForResult(intent, requestCode)
//            }
//        }
//    }

//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private fun saveTreeUri(resultData: Intent) {
//        val treeUri = resultData.data
//        baseConfig.treeUri = treeUri.toString()
//
//        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        contentResolver.takePersistableUriPermission(treeUri, takeFlags)
//    }

    private fun isProperFolder(uri: Uri) = isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)

    @SuppressLint("NewApi")
    private fun isRootUri(uri: Uri) = DocumentsContract.getTreeDocumentId(uri).endsWith(":")

    @SuppressLint("NewApi")
    private fun isInternalStorage(uri: Uri) = isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri).contains("primary")

    private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority

//    fun startAboutActivity(appNameId: Int, licenseMask: Int, versionName: String) {
//        Intent(applicationContext, AboutActivity::class.java).apply {
//            putExtra(APP_NAME, getString(appNameId))
//            putExtra(APP_LICENSES, licenseMask)
//            putExtra(APP_VERSION_NAME, versionName)
//            startActivity(this)
//        }
//    }

    fun startCustomizationActivity() = startActivity(Intent(this, CustomizationActivity::class.java))

//    fun handleSAFDialog(file: File, callback: () -> Unit): Boolean {
//        return if (isShowingSAFDialog(file, baseConfig.treeUri, OPEN_DOCUMENT_TREE)) {
//            funAfterSAFPermission = callback
//            true
//        } else {
//            callback()
//            false
//        }
//    }

//    fun copyMoveFilesTo(files: ArrayList<File>, source: String, destination: String, isCopyOperation: Boolean, copyPhotoVideoOnly: Boolean, callback: () -> Unit) {
//        if (source == destination) {
//            toast(R.string.source_and_destination_same)
//            return
//        }
//
//        val destinationFolder = File(destination)
//        if (!destinationFolder.exists()) {
//            toast(R.string.invalid_destination)
//            return
//        }
//
//        if (files.size == 1) {
//            if (File(destinationFolder.absolutePath, files[0].name).exists()) {
//                toast(R.string.name_taken)
//                return
//            }
//        }
//
//        handleSAFDialog(destinationFolder) {
//            copyMoveCallback = callback
//            if (isCopyOperation) {
//                toast(R.string.copying)
//                startCopyMove(files, destinationFolder, isCopyOperation, copyPhotoVideoOnly)
//            } else {
//                if (isPathOnSD(source) || isPathOnSD(destinationFolder.absolutePath)) {
//                    handleSAFDialog(File(source)) {
//                        toast(R.string.moving)
//                        startCopyMove(files, destinationFolder, false, copyPhotoVideoOnly)
//                    }
//                } else {
//                    toast(R.string.moving)
//                    val updatedFiles = ArrayList<File>(files.size * 2)
//                    updatedFiles.addAll(files)
//                    for (oldFile in files) {
//                        val newFile = File(destinationFolder, oldFile.name)
//                        if (!newFile.exists() && oldFile.renameTo(newFile)) {
//                            if (!baseConfig.keepLastModified) {
//                                newFile.setLastModified(System.currentTimeMillis())
//                            }
//                            updateInMediaStore(oldFile, newFile)
//                            updatedFiles.add(newFile)
//                        }
//                    }
//
//                    scanFiles(updatedFiles) {
//                        runOnUiThread {
//                            copyMoveListener.copySucceeded(false, files.size * 2 == updatedFiles.size)
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private fun startCopyMove(files: ArrayList<File>, destinationFolder: File, isCopyOperation: Boolean, copyPhotoVideoOnly: Boolean) {
//        val pair = Pair(files, destinationFolder)
//        CopyMoveTask(this, isCopyOperation, copyPhotoVideoOnly, copyMoveListener).execute(pair)
//    }

//    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
//        actionOnPermission = null
//        if (hasPermission(permissionId)) {
//            callback(true)
//        } else {
//            isAskingPermissions = true
//            actionOnPermission = callback
//            ActivityCompat.requestPermissions(this, arrayOf(getPermissionString(permissionId)), GENERIC_PERM_HANDLER)
//        }
//    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

//    val copyMoveListener = object : CopyMoveTask.CopyMoveListener {
//        override fun copySucceeded(copyOnly: Boolean, copiedAll: Boolean) {
//            if (copyOnly) {
//                toast(if (copiedAll) R.string.copying_success else R.string.copying_success_partial)
//            } else {
//                toast(if (copiedAll) R.string.moving_success else R.string.moving_success_partial)
//            }
//            copyMoveCallback?.invoke()
//            copyMoveCallback = null
//        }
//
//        override fun copyFailed() {
//            toast(R.string.copy_move_failed)
//            copyMoveCallback = null
//        }
//    }
}
