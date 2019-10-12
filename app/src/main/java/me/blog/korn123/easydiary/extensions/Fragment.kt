package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.app.Activity
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import me.blog.korn123.commons.utils.FontUtils

fun Fragment.updateFragmentUI(rootView: ViewGroup) {
    rootView.let {
        context?.run {
            initTextSize(it, this)
            updateTextColors(it,0,0)
            updateAppViews(it)
            updateCardViewPolicy(it)
            FontUtils.setFontsTypeface(this, assets, null, it, true)
        }
    }
}

fun Fragment.confirmPermission(permissions: Array<String>, requestCode: Int) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            || ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        AlertDialog.Builder(context!!)
                .setMessage("Easy Diary 사용을 위해서는 권한승인이 필요합니다.")
                .setTitle("권한승인 요청")
                .setPositiveButton("확인") { _, _ -> requestPermissions(permissions, requestCode) }
                .show()
    } else {
        requestPermissions(permissions, requestCode)
    }
}