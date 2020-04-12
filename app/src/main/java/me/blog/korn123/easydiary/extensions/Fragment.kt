package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import me.blog.korn123.commons.utils.FontUtils

fun Fragment.updateFragmentUI(rootView: ViewGroup) {
    rootView.let {
        context?.run {
            initTextSize(it)
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

fun Fragment.scaledDrawable(id: Int, width: Int, height: Int): Drawable? {
    var drawable = ContextCompat.getDrawable(context!!, id)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = (DrawableCompat.wrap(drawable!!)).mutate()
    }

    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
}