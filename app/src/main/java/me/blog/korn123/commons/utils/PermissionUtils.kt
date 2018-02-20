package me.blog.korn123.commons.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

/**
 * Created by CHO HANJOONG on 2016-10-09.
 */

object PermissionUtils {
    fun confirmPermission(context: Context, activity: Activity, permissions: Array<String>, requestCode: Int) {

        // 처음 권한을 요청하는경우에 이 함수는 항상 false
        // 사용자가 '다시 묻지 않기'를 체크하지 않고, 1번이상 권한요청에 대해 거부한 경우에만 true
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(context)
                    .setMessage("Easy Diary 사용을 위해서는 권한승인이 필요합니다.")
                    .setTitle("권한승인 요청")
                    .setPositiveButton("확인") { dialog, whichButton -> ActivityCompat.requestPermissions(activity, permissions, requestCode) }
                    //                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    //                            public void onClick(DialogInterface dialog, int whichButton) {
                    //                            }
                    //                        })
                    .show()
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun checkPermission(context: Context, permissions: Array<String>): Boolean {
        var isValid = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                isValid = false
                break
            }
        }
        return isValid
    }
}
