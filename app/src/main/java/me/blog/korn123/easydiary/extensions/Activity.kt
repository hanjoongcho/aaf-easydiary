package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.annotation.SuppressLint
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
import android.graphics.Point
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.models.Release
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_COARSE_LOCATION
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_FINE_LOCATION
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog
import me.blog.korn123.easydiary.helper.DIARY_EXECUTION_MODE
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_ACCESS_FROM_OUTSIDE
import me.blog.korn123.easydiary.helper.REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.views.SlidingTabLayout
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

    val viewPager = symbolDialog.findViewById(R.id.viewpager) as androidx.viewpager.widget.ViewPager
    symbolDialog.findViewById<TextView>(R.id.diarySymbolGuide).text = guideMessage
    val symbolPagerAdapter = SymbolPagerAdapter(this, itemList, categoryList) { symbolSequence ->
        callback.invoke(symbolSequence)
        dialog?.dismiss()
    }
    viewPager.adapter = symbolPagerAdapter

    val slidingTabLayout = symbolDialog.findViewById(R.id.sliding_tabs) as SlidingTabLayout
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

