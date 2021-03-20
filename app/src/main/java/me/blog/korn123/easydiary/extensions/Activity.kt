package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.models.Release
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_COARSE_LOCATION
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_FINE_LOCATION
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.views.SlidingTabLayout
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import kotlin.system.exitProcess


/**
 * Created by CHO HANJOONG on 2018-02-10.
 */
fun Activity.resumeLock() {
    if (config.aafPinLockPauseMillis > 0L && System.currentTimeMillis() - config.aafPinLockPauseMillis > 1000) {
        
        // FIXME remove test code
//        Toast.makeText(this, "${(System.currentTimeMillis() - config.aafPinLockPauseMillis) / 1000}", Toast.LENGTH_LONG).show()
        when {
            config.fingerprintLockEnable -> {
                startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
                    putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_UNLOCK)
                })
            }
            config.aafPinLockEnable -> {
                startActivity(Intent(this, PinLockActivity::class.java).apply {
                    putExtra(PinLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_UNLOCK)
                })
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
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.permission_confirmation_dialog_message))
                .setTitle(getString(R.string.permission_confirmation_dialog_title))
                .setPositiveButton(getString(R.string.ok)) { _, _ -> ActivityCompat.requestPermissions(this, permissions, requestCode) }
                .show()
    } else {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}

fun Activity.checkWhatsNew(releases: List<Release>, currVersion: Int, applyFilter: Boolean = true) {
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

fun Activity.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.makeSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
            .make(findViewById(android.R.id.content), message, duration)
            .setAction("Action", null).show()
}

fun Activity.setScreenOrientationSensor(enableSensor: Boolean) {
    requestedOrientation = when (enableSensor) {
        true -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
        false -> ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
    }
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.holdCurrentOrientation() {
    when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Configuration.ORIENTATION_LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun Activity.clearHoldOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

fun Activity.isLandScape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Activity.actionBarHeight(): Int {
    val typedValue = TypedValue()
    var actionBarHeight = 0
    if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
        actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
    }
    return actionBarHeight
}

fun Activity.statusBarHeight(): Int {
    var statusBarHeight = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        statusBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return statusBarHeight
}

fun Activity.getDefaultDisplay(): Point {
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun Activity.getRootViewHeight(): Int {
    return getDefaultDisplay().y - actionBarHeight() - statusBarHeight()
}

fun Activity.startActivityWithTransition(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

fun Activity.restartApp() {
    val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
    readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    val mPendingIntentId = 123456
    val mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, readDiaryIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
    ActivityCompat.finishAffinity(this)
    //System.runFinalizersOnExit(true)
    exitProcess(0)
}

fun Activity.refreshApp() {
    val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
    readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    TransitionHelper.startActivityWithTransition(this, readDiaryIntent)
}

fun Activity.makeSnackBar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
}

fun Activity.showAlertDialog(title: String?, message: String, positiveListener: DialogInterface.OnClickListener?, negativeListener: DialogInterface.OnClickListener?, cancelable: Boolean = true) {
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(cancelable)
    builder.setNegativeButton(getString(R.string.cancel), negativeListener)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    builder.create().apply {
        updateAlertDialog(this, message, null, title)
    }
}

fun Activity.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener, negativeListener: DialogInterface.OnClickListener?) {
    showAlertDialog(null, message, positiveListener, negativeListener)
}

fun Activity.showAlertDialog(title: String?, message: String, positiveListener: DialogInterface.OnClickListener?, cancelable: Boolean = true) {
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(cancelable)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    builder.create().apply {
        updateAlertDialog(this, message, null, title)
    }
}

fun Activity.showAlertDialog(message: String, positiveListener: DialogInterface.OnClickListener?, cancelable: Boolean = true) {
    showAlertDialog(null, message, positiveListener, cancelable)
}

fun Activity.startMainActivityWithClearTask() {
    Intent(this, DiaryMainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
    }
    this.overridePendingTransition(0, 0)
}

fun Activity.isAccessFromOutside(): Boolean = intent.getStringExtra(DIARY_EXECUTION_MODE) == EXECUTION_MODE_ACCESS_FROM_OUTSIDE

fun Activity.openFeelingSymbolDialog(guideMessage: String, callback: (Int) -> Unit) {
    var dialog: Dialog? = null
    val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val symbolDialog = inflater.inflate(R.layout.dialog_feeling_pager, null)

    val itemList = arrayListOf<Array<String>>()
    val categoryList = arrayListOf<String>()
    addCategory(itemList, categoryList, "weather_item_array", getString(R.string.category_weather))
    addCategory(itemList, categoryList, "emotion_item_array", getString(R.string.category_emotion))
    addCategory(itemList, categoryList, "daily_item_array", getString(R.string.category_daily))
    addCategory(itemList, categoryList, "tasks_item_array", getString(R.string.category_tasks))
    addCategory(itemList, categoryList, "food_item_array", getString(R.string.category_food))
    addCategory(itemList, categoryList, "leisure_item_array", getString(R.string.category_leisure))
    addCategory(itemList, categoryList, "landscape_item_array", getString(R.string.category_landscape))
    addCategory(itemList, categoryList, "symbol_item_array", getString(R.string.category_symbol))
    addCategory(itemList, categoryList, "flag_item_array", getString(R.string.category_flag))

    val viewPager = symbolDialog.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewpager).apply { setBackgroundColor(config.backgroundColor) }
    symbolDialog.findViewById<TextView>(R.id.diarySymbolGuide)?.let {
        it.text = guideMessage
    }
    val symbolPagerAdapter = SymbolPagerAdapter(this, itemList, categoryList) { symbolSequence ->
        callback.invoke(symbolSequence)
        dialog?.dismiss()
    }
    viewPager.adapter = symbolPagerAdapter

    val slidingTabLayout = symbolDialog.findViewById<SlidingTabLayout>(R.id.sliding_tabs).apply { setBackgroundColor(config.backgroundColor) }
    slidingTabLayout.setViewPager(viewPager)

    val dismissButton = symbolDialog.findViewById(R.id.closeBottomSheet) as ImageView
    dismissButton.setOnClickListener { dialog?.dismiss() }

    if (isLandScape()) {
        val builder = AlertDialog.Builder(this)
        builder.setView(symbolDialog)
        dialog = builder.create()
    } else {
        dialog = BottomSheetDialog(this)
        dialog.run {
            setContentView(symbolDialog)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
        }
    }

    dialog.show()
}

fun Activity.addCategory(itemList: ArrayList<Array<String>>, categoryList: ArrayList<String>, resourceName: String, categoryName: String) {
    val resourceId = resources.getIdentifier(resourceName, "array", packageName)
    if (resourceId != 0) {
        itemList.add(resources.getStringArray(resourceId))
        categoryList.add(categoryName)
    }
}

fun Activity.getLayoutLayoutInflater(): LayoutInflater{
    return getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}

fun Activity.scaledDrawable(id: Int, width: Int, height: Int): Drawable? {
    var drawable = AppCompatResources.getDrawable(this, id)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = (DrawableCompat.wrap(drawable!!)).mutate()
    }

    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
}

@TargetApi(Build.VERSION_CODES.KITKAT)
fun Activity.writeFileWithSAF(fileName: String, mimeType: String, requestCode: Int) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        addCategory(Intent.CATEGORY_OPENABLE)

        type = mimeType
        // Create a file with the requested MIME type.
        putExtra(Intent.EXTRA_TITLE, fileName)
    }
    startActivityForResult(intent, requestCode)
}

fun Activity.exportHtmlBook(uri: Uri?, diaryList: List<DiaryDto>) {
    uri?.let {
        val os = contentResolver.openOutputStream(it)
        IOUtils.write(createHtmlString(diaryList), os, "UTF-8")
        os?.close()
    }
}

fun Activity.createHtmlString(diaryList: List<DiaryDto>): String {
    val diaryDivision = StringBuilder()
    diaryList.forEach {
        val html = StringBuilder()
        val resourceId = FlavorUtils.sequenceToSymbolResourceId(it.weather)
        when (resourceId > 0) {
            true -> html.append("<div class='title'> <div class='title-left'><img src='data:image/png;base64, ${resourceToBase64(resourceId)}' /></div> <div class='title-right'>${it.title}</div> </div>")
            false -> html.append("<div class='title'> <div class='title-right'>${it.title}</div> </div>")
        }
        html.append("<div class='datetime'>${DateUtils.getFullPatternDateWithTimeAndSeconds(it.currentTimeMillis)}</div>")
        html.append("<pre class='contents'>")
        html.append(it.contents)
        html.append("</pre>")
        html.append("<div class='photo-container'>")

        it.photoUris?.let { photoUriList ->
            val imageColumn = when {
                photoUriList.size % 4 == 0 -> 4
                photoUriList.size % 2 == 0 -> 2
                else -> 1
            }
            photoUriList.forEach { photoUriDto ->
            html.append("<div class='photo col${imageColumn}'><img src='data:image/png;base64, ${photoToBase64(EasyDiaryUtils.getApplicationDataDirectory(this) + photoUriDto.getFilePath())}' /></div>")
        }
        }
        html.append("</div>")
        html.append("<hr>")
        diaryDivision.append(html.toString())
    }

    val template = StringBuilder()
    template.append("<!DOCTYPE html>")
    template.append("<html>")
    template.append("<head>")
    template.append("   <meta charset='UTF-8'>")
    template.append("   <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
    template.append("   <title>Insert title here</title>")
    template.append("   <style type='text/css'>")
    template.append("       body { margin: 1rem; font-family: 나눔고딕, monospace; }")
    template.append("       hr { margin: 1.5rem 0 }")
    template.append("       .title { margin-top: 1rem; font-size: 1.3rem; }")
    template.append("       .title img { width: 30px; margin-right: 1rem; display: block; }")
    template.append("       .title-left { display:inline-block; }")
    template.append("       .title-right { display:inline-block; position: absolute; }")
    template.append("       .datetime { font-size: 0.8rem; text-align: right; }")
    template.append("       .contents { margin-top: 1rem; font-size: 0.9rem; font-family: 나눔고딕, monospace; white-space: pre-wrap; }")
    template.append("       .photo-container { display: flex; flex-wrap: wrap; }")
    template.append("       .photo-container .photo { background: rgb(240 239 240); padding: 0.3rem; border-radius: 5px; margin: 0.25rem; box-sizing: border-box; }")
    template.append("       .photo.col1 { width: calc(100% - 0.5rem); }")
    template.append("       .photo.col2 { width: calc(50% - 0.5rem); }")
    template.append("       .photo.col4 { width: calc(25% - 0.5rem); }")
    template.append("       .photo img { width: 100%; display: block; border-radius: 5px; }")
    template.append("   </style>")
    template.append("<body>")
    template.append(diaryDivision.toString())
    template.append("</body>")
    template.append("</html>")

    return template.toString()
}

fun Activity.photoToBase64(photoPath: String): String {
    var image64 = ""
    val bitmap = BitmapUtils.cropCenter(BitmapFactory.decodeFile(photoPath))
//        val fis = FileInputStream(photoPath)
//        IOUtils.copy(fis, bos)
    if (bitmap != null) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos)
        image64 = Base64.encodeBase64String(bos.toByteArray())
        bos.close()
    }
    return image64
}

fun Activity.resourceToBase64(resourceId: Int): String {
    var image64 = ""
    val bitmap = scaledDrawable(resourceId, 100, 100)?.toBitmap()
//        val bitmap = BitmapFactory.decodeResource(resources, resourceId)
    if (bitmap != null) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        image64 = Base64.encodeBase64String(bos.toByteArray())
        bos.close()
    }
    return image64
}

fun EasyDiaryActivity.acquireGPSPermissions(callback: () -> Unit) {
    handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) { hasCoarseLocation ->
        if (hasCoarseLocation) {
            handlePermission(PERMISSION_ACCESS_FINE_LOCATION) { hasFineLocation ->
                if (hasFineLocation) {
                    if (isLocationEnabled()) {
                        callback()
                    } else {
                        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
                    }
                }
            }
        }
    }
}

fun EasyDiaryActivity.getLocationWithGPSProvider(callback: (location: Location?) -> Unit) {
    val gpsProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val networkProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    when (checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION))) {
        true -> {
            if (isLocationEnabled()) {
                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            } else {
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
            }
        }
        false -> {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) { hasCoarseLocation ->
                if (hasCoarseLocation) {
                    handlePermission(PERMISSION_ACCESS_FINE_LOCATION) { hasFineLocation ->
                        if (hasFineLocation) {
                            if (isLocationEnabled()) {
                                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
                            } else {
                                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
                            }
                        }
                    }
                }
            }
        }
    }
}

