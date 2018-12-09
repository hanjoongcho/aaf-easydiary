package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.models.Release
import io.github.aafactory.commons.activities.BaseSimpleActivity
import me.blog.korn123.easydiary.activities.DiaryLockActivity
import me.blog.korn123.easydiary.activities.FingerprintActivity
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog

/**
 * Created by CHO HANJOONG on 2018-02-10.
 */

fun Activity.pauseLock() {
    if (config.aafPinLockEnable || config.fingerprintLockEnable) {
        config.aafPinLockPauseMillis = System.currentTimeMillis()
    }
}

fun Activity.resumeLock() {
    if (config.aafPinLockPauseMillis > 0L && System.currentTimeMillis() - config.aafPinLockPauseMillis > 1000) {
        when {
            config.fingerprintLockEnable -> {
                startActivity(Intent(this, FingerprintActivity::class.java).apply {
                    putExtra(FingerprintActivity.LAUNCHING_MODE, FingerprintActivity.ACTIVITY_UNLOCK)
                })
            }
            config.aafPinLockEnable -> {
                startActivity(Intent(this, DiaryLockActivity::class.java))
            }
        }
    }
}

fun Activity.openGooglePlayBy(targetAppId: String) {
    val uri = Uri.parse("market://details?id=" + targetAppId)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + targetAppId)))
    }
}

fun Activity.confirmPermission(permissions: Array<String>, requestCode: Int) {
    // 처음 권한을 요청하는경우에 이 함수는 항상 false
    // 사용자가 '다시 묻지 않기'를 체크하지 않고, 1번이상 권한요청에 대해 거부한 경우에만 true
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        AlertDialog.Builder(this)
                .setMessage("Easy Diary 사용을 위해서는 권한승인이 필요합니다.")
                .setTitle("권한승인 요청")
                .setPositiveButton("확인") { dialog, whichButton -> ActivityCompat.requestPermissions(this, permissions, requestCode) }
                //                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                //                            public void onClick(DialogInterface dialog, int whichButton) {
                //                            }
                //                        })
                .show()
    } else {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}

fun BaseSimpleActivity.checkWhatsNew(releases: List<Release>, currVersion: Int, applyFilter: Boolean = true) {
    when (applyFilter) {
        true -> {
            if (baseConfig.lastVersion == 0) {
                baseConfig.lastVersion = currVersion
                return
            }

            val newReleases = arrayListOf<Release>()
            releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

            if (newReleases.isNotEmpty() && !baseConfig.avoidWhatsNew) {
                WhatsNewDialog(this, newReleases)
            }

            baseConfig.lastVersion = currVersion
        }
        false -> {
            WhatsNewDialog(this, releases)
        }
    }
}

fun Activity.makeSnackBar(message: String) {
    Snackbar
            .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setAction("Action", null).show()
}