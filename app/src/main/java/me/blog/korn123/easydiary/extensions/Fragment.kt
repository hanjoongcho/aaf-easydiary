package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.Config

val Fragment.config: Config get() = Config.newInstance(context!!)

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
                .setMessage(getString(R.string.permission_confirmation_dialog_message))
                .setTitle(getString(R.string.permission_confirmation_dialog_title))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> requestPermissions(permissions, requestCode) }
                .show()
    } else {
        requestPermissions(permissions, requestCode)
    }
}

fun Fragment.scaledDrawable(id: Int, width: Int, height: Int): Drawable? {
    var drawable = AppCompatResources.getDrawable(context!!, id)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = (DrawableCompat.wrap(drawable!!)).mutate()
    }

    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
}