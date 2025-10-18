package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.models.Release
import id.zelory.compressor.Compressor
import io.realm.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.BitmapUtils
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FlavorUtils.getDiarySymbolMap
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.databinding.ActivityDiaryMainBinding
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.BACKUP_DB_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_EXECUTION_MODE
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_ACCESS_FROM_OUTSIDE
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.FILE_URI_PREFIX
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_COARSE_LOCATION
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_FINE_LOCATION
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.USER_CUSTOM_FONTS_DIRECTORY
import me.blog.korn123.easydiary.helper.WORKING_DIRECTORY
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.models.PhotoUri
import me.blog.korn123.easydiary.views.SlidingTabLayout
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


/***************************************************************************************************
 *   Confirm Permissions
 *
 ***************************************************************************************************/
fun Activity.confirmPermission(permissions: Array<String>, requestCode: Int) {
    if (permissions.any { permission ->  ActivityCompat.shouldShowRequestPermissionRationale(this, permission) }) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_confirmation_dialog_message))
            .setTitle(getString(R.string.permission_confirmation_dialog_title))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> ActivityCompat.requestPermissions(this, permissions, requestCode) }
            .show()
    } else {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}

fun Activity.confirmExternalStoragePermission(permissions: Array<String>, activityResultLauncher: ActivityResultLauncher<Array<String>>) {
    if (permissions.any { permission ->  ActivityCompat.shouldShowRequestPermissionRationale(this, permission) }) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_confirmation_dialog_message))
            .setTitle(getString(R.string.permission_confirmation_dialog_title))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> activityResultLauncher.launch(permissions) }
            .show()
    } else {
        activityResultLauncher.launch(permissions)
    }
}


/***************************************************************************************************
 *   Messages
 *
 ***************************************************************************************************/
fun Activity.makeSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
        .make(findViewById(android.R.id.content), message, duration)
        .setBackgroundTint(config.primaryColor)
        .setTextColor(Color.WHITE)
        .setAction("Action", null).show()
}

fun Activity.showBetaFeatureMessage() {
    makeSnackBar("\uD83D\uDEA7 This feature is currently in beta testing.", Snackbar.LENGTH_LONG)
}



/***************************************************************************************************
 *   Screen Dimension
 *
 ***************************************************************************************************/
/**
 * 0 → 3버튼 네비게이션 (기본 소프트키)
 * 1 → 2버튼 네비게이션 (홈/뒤로 버튼)
 * 2 → 제스처 네비게이션
 * @return
 */
@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
)
fun Activity.getNavigationMode(): Int {
    try {
        return Settings.Secure.getInt(contentResolver, "navigation_mode");
    } catch (e: SettingNotFoundException) {
        e.printStackTrace()
        return -1 // 설정 값이 없을 경우
    }
}

@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
)
fun Activity.hideStatusBars() {
    if (isRedVelvetCakePlus()) {
        window.insetsController?.hide(WindowInsets.Type.statusBars())
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
)
fun Activity.hideNavigationBars() {
    if (isVanillaIceCreamPlus()) {
        window.insetsController?.hide(WindowInsets.Type.navigationBars())
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
)
@RequiresApi(Build.VERSION_CODES.R)
fun Activity.isNavigationBarVisible(): Boolean {

    // 전체 화면 크기 가져오기
    val metrics = windowManager.currentWindowMetrics
    val fullHeight = metrics.bounds.height()

    // 현재 화면에서 사용 가능한 높이 가져오기
    val visibleBounds: Rect = Rect()
    window.decorView.getWindowVisibleDisplayFrame(visibleBounds)
    val visibleHeight: Int = visibleBounds.height()

    // 네비게이션 바 높이가 존재하면 보이는 상태
    return (fullHeight - visibleHeight) > 0
}

@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
)
fun Activity.printDisplayMetrics() {
    val metrics = DisplayMetrics()
    val realMetrics = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.getRealMetrics(realMetrics)
        display?.getMetrics(metrics)
    } else {
        val display = windowManager.defaultDisplay
        display.getRealMetrics(realMetrics)
        display.getMetrics(metrics)
    }
    Log.i(AAF_TEST, "metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
    Log.i(AAF_TEST, "realMetrics: ${realMetrics.widthPixels} x ${realMetrics.heightPixels}")
}

@Deprecated(
    message = "검증되지 않아서 사용하지 않는 기능임",
    replaceWith =
        ReplaceWith(""),
)
fun Activity.getRootViewHeight(): Int {
    return getDefaultDisplay().y - actionBarHeight() - statusBarHeight()
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

fun Activity.topBarHeight(): Int {
    return actionBarHeight().plus(statusBarHeight())
}

fun Activity.navigationBarHeight(): Int {
    var navigationBarHeight = 0
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        navigationBarHeight = resources.getDimensionPixelSize(resourceId)
    }
    return navigationBarHeight
}

fun Activity.getDefaultDisplay(): Point {
    val display = windowManager.defaultDisplay
    val size = Point()
    display.getSize(size)
    return size
}

fun Activity.getDashboardCardWidth(ratio: Float): Int {
    val scaleFactor = if (isLandScape()) 0.5F else 1F
    return getDefaultDisplay().x.times(ratio).times(scaleFactor).toInt()
}

fun Activity.getDisplayMetrics(): DisplayMetrics {
    val outMetrics = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val display = display
        display?.getMetrics(outMetrics)
//        display?.getRealMetrics(outMetrics)
    } else {
        val display = windowManager.defaultDisplay
        display.getMetrics(outMetrics)
//        display.getRealMetrics(outMetrics)
    }
    return outMetrics
}

/**
 * 화면 좌우 시스템영역 인셋 처리를 함
 *
 * 머티리얼 앱바의 경우 XML레이아웃 사용 시 가로화면에서 앱바 UI가 시스템영역에 가려질 수 있으나 XML레벨에서 처리할 방법이 없어서
 * 코드에서 직접 처리해야 함
 */
fun Activity.applyHorizontalInsets() {
    if (isVanillaIceCreamPlus() && isLandScape()) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_holder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.rightMargin = systemBars.right
                layoutParams.leftMargin = systemBars.left
                v.layoutParams = layoutParams
            }
            insets
        }
        ViewCompat.requestApplyInsets(findViewById(R.id.main_holder))

        val dashboardContainer = findViewById<View>(R.id.dashboard_container)
        if (dashboardContainer == null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val layoutParams = v.layoutParams
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.rightMargin = systemBars.right
                    layoutParams.leftMargin = systemBars.left
                    v.layoutParams = layoutParams
                }
                insets
            }
            ViewCompat.requestApplyInsets(findViewById(R.id.app_bar))
        }
    }
}

/**
 * - 애플리케이션 화면이 하단 내비게이션 영역까지 확장되어 사용되는 경우 파라미터로 넘겨받은
 * 뷰의 하단에 내비게이션영역 높이값만큼 마진을 추가함
 * - Version SDK 35 미만인 경우 Edege to Edge 모드가 강제 적용되지 않고 앱이 시스템 UI 뒤로 확장되지 않기 때문에
 * 버전 체크를 하지 않음
 */
fun Activity.applyBottomNavigationInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val layoutParams = v.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.bottomMargin = systemBars.bottom
            v.layoutParams = layoutParams
        }
        insets
    }
}

/**
 * Version SDK 35 이상의 경우 IME 영역의 인셋을 적용함
 * IME 영역은 키보드가 올라올 때 화면 하단에 나타나는 영역으로, 이 영역의 높이만큼
 * 파라미터로 넘겨받은 뷰의 하단 마진을 조정함
 * 이 기능은 세로화면 모드에서만 적용되며, 가로화면 모드에서는 적용되지 않음
 */
fun Activity.applyBottomImeInsets(view: View) {
    if (isVanillaIceCreamPlus() && !isLandScape()) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val layoutParams = v.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.bottomMargin = ime.bottom
                v.layoutParams = layoutParams
            }
            insets
        }
    }
}

/**
 * Version SDK 35 이상의 가로화면 모드인 경우 강제로 시스템바를 숨김처리 함
 */
fun Activity.hideSystemBarsVanillaIceCreamPlusIsLandScape() {
    if (isVanillaIceCreamPlus() && isLandScape()) {
        // From version 15, the system bar area is forcibly extended
        // In landscape mode, the position of the navigation bar varies depending on system settings
        // Buttons: Right side of the screen
        // Gesture navigation: Bottom of the screen
        // When the navigation bar is transparently (forcibly) placed on the right side,
        // there is no way to properly handle the Material ActionBar area
        hideSystemBars()
    }
}

fun Activity.hideSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        window.insetsController?.hide(WindowInsets.Type.systemBars())
        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

/**
 * 💡 시스템 상태바의 배경컬러 또는 텍스트(아이콘) 컬러를 변경함
 *  - Version SDK 35 이상: 텍스트(아이콘) 컬러를 변경함
 *  - Version SDK 35 미만: 배경색을 반투명 처리함
 *  - 상단 액션바 없이 전체화면으로 화면이 확장되어 사용되는 경우에 호출하는 것이 기본임
 */
fun Activity.updateSystemStatusBarColor(checkColor: Int = config.screenBackgroundColor) {
    if (isVanillaIceCreamPlus()) {
        // true: 밝은 배경 → 검정 텍스트 (light status bar icons)
        // false: 어두운 배경 → 흰색 텍스트
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =  isColorLight(checkColor)
    } else {
        window.statusBarColor = ColorUtils.setAlphaComponent(config.primaryColor, 150)
        window.navigationBarColor = androidx.compose.ui.graphics.Color.Transparent.toArgb()
    }
}


/***************************************************************************************************
 *   etc functions
 *
 ***************************************************************************************************/
fun Activity.resumeLock() {
    if (config.aafPinLockPauseMillis > 0L && System.currentTimeMillis() - config.aafPinLockPauseMillis > 1000) {
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

fun Activity.applyPolicyForRecentApps() {
    if (config.aafPinLockEnable || config.fingerprintLockEnable) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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

fun Activity.startActivityWithTransition(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

//fun Activity.restartApp() {
//    val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
//    readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//    val mPendingIntentId = 123456
//    val mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, readDiaryIntent, PendingIntent.FLAG_CANCEL_CURRENT)
//    val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
//    ActivityCompat.finishAffinity(this)
//    //System.runFinalizersOnExit(true)
//    exitProcess(0)
//}

fun Activity.refreshApp() {
    val readDiaryIntent = Intent(this, DiaryMainActivity::class.java)
    readDiaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    TransitionHelper.startActivityWithTransition(this, readDiaryIntent)
}

fun Activity.startMainActivityWithClearTask() {
    Intent(this, DiaryMainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(this)
    }
    this.overridePendingTransition(0, 0)
}

fun Activity.isAccessFromOutside(): Boolean = intent.getStringExtra(DIARY_EXECUTION_MODE) == EXECUTION_MODE_ACCESS_FROM_OUTSIDE

// FIXME: WIP START
fun getCustomSymbolPaths(symbolSequence: Int, realmInstance: Realm? = null): List<PhotoUri> {
    // EasyDiaryUtils.getApplicationDataDirectory(this)
    val items = if (realmInstance == null) EasyDiaryDbHelper.findDiary(
        null,
        false,
        0,
        0,
        symbolSequence
    ) else EasyDiaryDbHelper.findDiary(null, false, 0, 0, symbolSequence, realmInstance)
    val diary = if (items.isNotEmpty()) items[0] else null
    return diary?.photoUris ?: listOf()
}

fun Activity.openFeelingSymbolDialog(guideMessage: String, selectedSymbolSequence: Int = 0, callback: (Int) -> Unit) {
    var dialog: Dialog? = null
    val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val symbolDialog = inflater.inflate(R.layout.dialog_feeling_pager, null)

    val itemList = arrayListOf<Array<String>>()
    val categoryList = arrayListOf<String>()
    var tabIndex = 0

    // Append recently used symbols
    val symbolUsedCountMap = EasyDiaryUtils.getSymbolUsedCountMap(true)
    if (symbolUsedCountMap.isNotEmpty()) {
        val symbolMap = getDiarySymbolMap(this)
        categoryList.add(getString(R.string.recently_used_symbol))
        itemList.add(arrayOf(getUncheckedSymbolItem(), *symbolUsedCountMap.entries.map { entry -> "${entry.key}|${symbolMap[entry.key] ?: "🎃"}" }.toTypedArray()))
        tabIndex++
    }

    addCategory(itemList, categoryList, "weather_item_array", getString(R.string.category_weather))
    addCategory(itemList, categoryList, "emotion_item_array", getString(R.string.category_emotion))
    addCategory(itemList, categoryList, "daily_item_array", getString(R.string.category_daily))
    addCategory(itemList, categoryList, "tasks_item_array", getString(R.string.category_tasks))
    addCategory(itemList, categoryList, "food_item_array", getString(R.string.category_food))
    addCategory(itemList, categoryList, "leisure_item_array", getString(R.string.category_leisure))
    addCategory(itemList, categoryList, "landscape_item_array", getString(R.string.category_landscape))
    addCategory(itemList, categoryList, "symbol_item_array", getString(R.string.category_symbol))
    addCategory(itemList, categoryList, "flag_item_array", getString(R.string.category_flag))

    // Append user customization symbols
    addUserCustomSymbols(categoryList, itemList)

    val currentItem = when (selectedSymbolSequence) {
        in 1..39 -> tabIndex
        in 100..199 -> tabIndex.plus(1)
        in 80..83 -> tabIndex.plus(3)
        in 40..99 -> tabIndex.plus(2)
        in 250..299 -> tabIndex.plus(4)
        in 300..349 -> tabIndex.plus(5)
        in 200..249 -> tabIndex.plus(6)
        in 350..449 -> tabIndex.plus(7)
        in 450..749 -> tabIndex.plus(8)
        else -> 0
    }

    val viewPager = symbolDialog.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewpager).apply { setBackgroundColor(config.backgroundColor) }
    symbolDialog.findViewById<TextView>(R.id.diarySymbolGuide)?.let {
        it.text = guideMessage
    }
    val symbolPagerAdapter = SymbolPagerAdapter(this, itemList, categoryList, selectedSymbolSequence) { symbolSequence ->
        callback.invoke(symbolSequence)
        dialog?.dismiss()
    }
    viewPager.adapter = symbolPagerAdapter

    val slidingTabLayout = symbolDialog.findViewById<SlidingTabLayout>(R.id.sliding_tabs).apply { setBackgroundColor(config.backgroundColor) }
    slidingTabLayout.setViewPager(viewPager)
    viewPager.setCurrentItem(currentItem, true)

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

fun Activity.addUserCustomSymbols(categoryList: ArrayList<String>, itemList: ArrayList<Array<String>>) {
    // FIXME: WIP START
    var customSymbolSequence = SYMBOL_USER_CUSTOM_START
    if (config.enableDebugMode) {
        val customSymbols = arrayListOf<String>()
        categoryList.add("Custom")
        getCustomSymbolPaths(SYMBOL_EASTER_EGG).forEach { item ->
//            item.getFilePath()
            customSymbols.add("$customSymbolSequence|${customSymbolSequence++}")
        }
        itemList.add(arrayOf(getUncheckedSymbolItem(), "$SYMBOL_EASTER_EGG|Easter Egg", *customSymbols.toTypedArray()))
    }
    // FIXME: WIP END
}

fun Activity.addCategory(itemList: ArrayList<Array<String>>, categoryList: ArrayList<String>, resourceName: String, categoryName: String) {
    val resourceId = resources.getIdentifier(resourceName, "array", packageName)
    if (resourceId != 0) {
        if (resourceName == "weather_item_array") {
            itemList.add(resources.getStringArray(resourceId))
        } else {
            itemList.add(arrayOf(getUncheckedSymbolItem(), *resources.getStringArray(resourceId)))
        }
        categoryList.add(categoryName)
    }
}

fun Activity.getUncheckedSymbolItem(): String {
    val resourceId = resources.getIdentifier("weather_item_array", "array", packageName)
    return if (resourceId != 0) resources.getStringArray(resourceId)[0] else "-1|N/A"
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

//}

//@TargetApi(Build.VERSION_CODES.KITKAT)
//fun Activity.writeFileWithSAF(fileName: String, mimeType: String, requestCode: Int) {
//    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//        // Filter to only show results that can be "opened", such as
//        // a file (as opposed to a list of contacts or timezones).
//        addCategory(Intent.CATEGORY_OPENABLE)
//
//        type = mimeType
//        // Create a file with the requested MIME type.
//        putExtra(Intent.EXTRA_TITLE, fileName)
//    }
//    startActivityForResult(intent, requestCode)

fun Activity.exportHtmlBook(uri: Uri?, diaryList: List<Diary>) {
    uri?.let {
        val os = contentResolver.openOutputStream(it)
        IOUtils.write(createHtmlString(diaryList), os, "UTF-8")
        os?.close()
    }
}

fun Activity.createHtmlString(diaryList: List<Diary>): String {
    val diaryDivision = StringBuilder()
    diaryList.forEach {
        val html = StringBuilder()
        val resourceId = FlavorUtils.sequenceToSymbolResourceId(it.weather)
        when (resourceId > 0) {
            true -> html.append("<div class='title'> <div class='title-left'><img src='data:image/png;base64, ${resourceToBase64(resourceId)}' /></div> <div class='title-right'>${it.title}</div> </div>")
            false -> html.append("<div class='title'> <div class='title-right'>${it.title}</div> </div>")
        }
        html.append("<div class='datetime'>${DateUtils.getDateTimeStringFromTimeMillis(it.currentTimeMillis, SimpleDateFormat.FULL, SimpleDateFormat.FULL)}</div>")
        html.append("<pre class='contents'>")
        html.append(it.contents)
        html.append("</pre>")
        html.append("<div class='photo-container'>")

        it.photoUris?.let { photoUriList ->
            val imageColumn = when (photoUriList.size) {
                1 -> 1
//                photoUriList.size % 2 == 0 -> 2
                else -> 2
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
    template.append("       .title { margin-top: 1rem; font-size: 1.3rem; display: flex; }")
    template.append("       .title img { width: 30px; margin-right: 1rem; display: block; }")
    template.append("       .title-left { display:inline-block; }")
    template.append("       .title-right { display:inline-block; white-space: pre-wrap; word-break: break-all; }")
    template.append("       .datetime { font-size: 0.8rem; text-align: right; }")
    template.append("       .contents { margin-top: 1rem; font-size: 0.9rem; font-family: 나눔고딕, monospace; white-space: pre-wrap; word-break: break-all; }")
    template.append("       .photo-container { display: flex; flex-wrap: wrap; }")
    template.append("       .photo-container .photo { background: rgb(240 239 240); padding: 0.3rem; border-radius: 5px; margin: 0.25rem; box-sizing: border-box; }")
    template.append("       .photo.col1 { width: calc(100% - 0.5rem); }")
    template.append("       .photo.col2 { width: calc(50% - 0.5rem); }")
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
    val bos = ByteArrayOutputStream()
    try {
        val bitmap = BitmapUtils.cropCenter(BitmapFactory.decodeFile(photoPath))
//        val fis = FileInputStream(photoPath)
//        IOUtils.copy(fis, bos)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos)
        image64 = Base64.encodeBase64String(bos.toByteArray())
    } catch (e: Exception) {
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

fun Activity.openGridSettingDialog(rootView: ViewGroup, gridSpanMode: GridSpanMode, callback: (spanCount: Int) -> Unit) {
    var alertDialog: AlertDialog? = null
    val builder = AlertDialog.Builder(this)
    builder.setNegativeButton(getString(android.R.string.cancel), null)
    val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val containerView = inflater.inflate(R.layout.dialog_option_item, rootView, false)
    val listView = containerView.findViewById<ListView>(R.id.listView)

    val maxSpanCount = when {
        isLandScape() && gridSpanMode == GridSpanMode.DIARY_MAIN -> 5
        !isLandScape() && gridSpanMode == GridSpanMode.DIARY_MAIN -> 3
        isLandScape() && (gridSpanMode == GridSpanMode.POSTCARD || gridSpanMode == GridSpanMode.GALLERY) -> 10
        !isLandScape() && (gridSpanMode == GridSpanMode.POSTCARD || gridSpanMode == GridSpanMode.GALLERY) -> 5
        else -> 2
    }
    val optionItems = mutableListOf<Map<String, String>>()
    for (i in 1..maxSpanCount) {
        optionItems.add(mapOf("optionTitle" to getString(R.string.postcard_grid_option_column_number, i), "optionValue" to "$i"))
    }

    var spanCount = 0
    var selectedIndex = 0
    optionItems.mapIndexed { index, map ->
        val size = map["optionValue"] ?: "0"
        when (isLandScape()) {
             true -> {
                when {
                    gridSpanMode == GridSpanMode.POSTCARD && config.postcardSpanCountLandscape == size.toInt() -> {
                        spanCount = config.postcardSpanCountLandscape
                        selectedIndex = index
                    }
                    gridSpanMode == GridSpanMode.DIARY_MAIN && config.diaryMainSpanCountLandscape == size.toInt() -> {
                        spanCount = config.diaryMainSpanCountLandscape
                        selectedIndex = index
                    }
                    gridSpanMode == GridSpanMode.GALLERY && config.gallerySpanCountLandscape == size.toInt() -> {
                        spanCount = config.gallerySpanCountLandscape
                        selectedIndex = index
                    }
                }

            }
            false -> {
                when {
                    gridSpanMode == GridSpanMode.POSTCARD && config.postcardSpanCountPortrait == size.toInt() -> {
                        spanCount = config.postcardSpanCountPortrait
                        selectedIndex = index
                    }
                    gridSpanMode == GridSpanMode.DIARY_MAIN && config.diaryMainSpanCountPortrait == size.toInt() -> {
                        spanCount = config.diaryMainSpanCountPortrait
                        selectedIndex = index
                    }
                    gridSpanMode == GridSpanMode.GALLERY && config.gallerySpanCountPortrait == size.toInt() -> {
                        spanCount = config.gallerySpanCountPortrait
                        selectedIndex = index
                    }
                }
            }
        }
    }

    val arrayAdapter = OptionItemAdapter(this, R.layout.item_check_label, optionItems, spanCount.toFloat())
    listView.adapter = arrayAdapter
    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
        @Suppress("UNCHECKED_CAST") val optionInfo = parent.adapter.getItem(position) as HashMap<String, String>
        optionInfo["optionValue"]?.let {
//                config.summaryMaxLines = it.toInt()
//                initPreference()
            when (isLandScape()) {
                true -> {
                    when (gridSpanMode) {
                        GridSpanMode.POSTCARD -> config.postcardSpanCountLandscape = it.toInt()
                        GridSpanMode.DIARY_MAIN -> config.diaryMainSpanCountLandscape = it.toInt()
                        GridSpanMode.GALLERY -> config.gallerySpanCountLandscape = it.toInt()
                    }
                }
                false -> {
                    when (gridSpanMode) {
                        GridSpanMode.POSTCARD -> config.postcardSpanCountPortrait = it.toInt()
                        GridSpanMode.DIARY_MAIN -> config.diaryMainSpanCountPortrait = it.toInt()
                        GridSpanMode.GALLERY -> config.gallerySpanCountPortrait = it.toInt()
                    }
                }
            }
            callback.invoke(it.toInt())
        }
        alertDialog?.cancel()
    }

    alertDialog = builder.create().apply { updateAlertDialog(this, null, containerView, getString(R.string.postcard_grid_option_title)) }
    listView.setSelection(selectedIndex)
}

fun Activity.diaryMainSpanCount(): Int = if (isLandScape()) config.diaryMainSpanCountLandscape else config.diaryMainSpanCountPortrait

fun Activity.postcardViewerSpanCount(): Int = if (isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait

fun Activity.getStatusBarColor(color: Int) = if (config.enableStatusBarDarkenColor) color.darkenColor() else color

fun Activity.updateStatusBarColor(color: Int) {
    window.statusBarColor = getStatusBarColor(color)

    // SDK Version 35 이상인 경우 상태바 컨드롤을 직접 할 수 없어서 dummy view를 사용함
    if (isVanillaIceCreamPlus()) findViewById<View>(R.id.dummyStatusBar)?.setBackgroundColor(getStatusBarColor(color))
}

fun EasyDiaryActivity.acquireGPSPermissions(
    activityResultLauncher: ActivityResultLauncher<Intent>,
    callback: () -> Unit
) {

    when (arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).any { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            permission
        )
    }) {
        true -> {
            // If authorization is denied
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        false -> {
            // If authorization request is possible
            handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) { hasCoarseLocation ->
                if (hasCoarseLocation) {
                    handlePermission(PERMISSION_ACCESS_FINE_LOCATION) { hasFineLocation ->
                        if (hasFineLocation) {
                            if (isLocationEnabled()) {
                                callback()
                            } else {
//                        startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
                                activityResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        }
                    }
                }
            }
        }
    }
}

//fun EasyDiaryActivity.getLocationWithGPSProvider(callback: (location: Location?) -> Unit) {
//    val gpsProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//    val networkProvider = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//    when (checkPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,  Manifest.permission.ACCESS_COARSE_LOCATION))) {
//        true -> {
//            if (isLocationEnabled()) {
//                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
//            } else {
//                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
//            }
//        }
//        false -> {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) { hasCoarseLocation ->
//                if (hasCoarseLocation) {
//                    handlePermission(PERMISSION_ACCESS_FINE_LOCATION) { hasFineLocation ->
//                        if (hasFineLocation) {
//                            if (isLocationEnabled()) {
//                                callback(gpsProvider.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: networkProvider.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
//                            } else {
//                                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

fun EasyDiaryActivity.migrateData(binging: ActivityDiaryMainBinding) {
    CoroutineScope(Dispatchers.IO).launch {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val listPhotoUri = EasyDiaryDbHelper.findPhotoUriAll(realmInstance)
        var isFontDirMigrate = false

        runOnUiThread {
            binging.progressDialog.visibility = View.VISIBLE
            binging.modalContainer.visibility = View.VISIBLE
        }

        for ((index, dto) in listPhotoUri.withIndex()) {
//                Log.i("PHOTO-URI", dto.photoUri)
            if (dto.isContentUri()) {
                val photoPath = EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                uriToFile(Uri.parse(dto.photoUri), photoPath)
                realmInstance.beginTransaction()
                dto.photoUri = FILE_URI_PREFIX + photoPath
                realmInstance.commitTransaction()
                runOnUiThread {
                    binging.progressInfo.text = "Converting... ($index/${listPhotoUri.size})"
                }
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + WORKING_DIRECTORY).listFiles()?.let {
                    it.forEach { file ->
                        if (file.extension.equals("jpg", true)) FileUtils.moveFileToDirectory(file, File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + DIARY_POSTCARD_DIRECTORY), true)
                    }
                }

                // Move attached photo from external storage to application data directory
                // From 1.4.102
                // 01. DIARY_PHOTO_DIRECTORY
                val photoSrcDir = File(EasyDiaryUtils.getExternalStorageDirectory(), DIARY_PHOTO_DIRECTORY)
                val photoDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + DIARY_PHOTO_DIRECTORY)
                photoSrcDir.listFiles()?.let {
                    it.forEachIndexed { index, file ->
                        Log.i("aaf-t", "${File(photoDestDir, file.name).exists()} ${File(photoDestDir, file.name).absolutePath}")
                        if (File(photoDestDir, file.name).exists()) {
                            Log.i("aaf-t", "${File(photoDestDir, file.name).delete()}")
                        }
                        FileUtils.copyFileToDirectory(file, photoDestDir)
                        runOnUiThread {
                            binging.migrationMessage.text = getString(R.string.storage_migration_message)
                            binging.progressInfo.text = "$index/${it.size} (Photo)"
                        }
                    }
                    photoSrcDir.renameTo(File(photoSrcDir.absolutePath + "_migration"))
                }
//                destDir.listFiles().map { file ->
//                    FileUtils.moveToDirectory(file, srcDir, true)
//                }

                // 02. DIARY_POSTCARD_DIRECTORY
                val postCardSrcDir = File(EasyDiaryUtils.getExternalStorageDirectory(), DIARY_POSTCARD_DIRECTORY)
                val postCardDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + DIARY_POSTCARD_DIRECTORY)
                postCardSrcDir.listFiles()?.let {
                    it.forEachIndexed { index, file ->
                        if (File(postCardDestDir, file.name).exists()) {
                            File(postCardDestDir, file.name).delete()
                        }
                        FileUtils.copyFileToDirectory(file, postCardDestDir)
                        runOnUiThread {
                            binging.progressInfo.text = "$index/${it.size} (Postcard)"
                        }
                    }
                    postCardSrcDir.renameTo(File(postCardSrcDir.absolutePath + "_migration"))
                }

                // 03. USER_CUSTOM_FONTS_DIRECTORY
                val fontSrcDir = File(EasyDiaryUtils.getExternalStorageDirectory(), USER_CUSTOM_FONTS_DIRECTORY)
                val fontDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + USER_CUSTOM_FONTS_DIRECTORY)
                fontSrcDir.listFiles()?.let {
                    it.forEachIndexed { index, file ->
                        if (File(fontDestDir, file.name).exists()) {
                            File(fontDestDir, file.name).delete()
                        }
                        FileUtils.copyFileToDirectory(file, fontDestDir)
                        runOnUiThread {
                            binging.progressInfo.text = "$index/${it.size} (Font)"
                        }
                    }
                    fontSrcDir.renameTo(File(fontSrcDir.absolutePath + "_migration"))
                    if (it.isNotEmpty()) isFontDirMigrate = true
                }

                // 04. BACKUP_DB_DIRECTORY
                val dbSrcDir = File(EasyDiaryUtils.getExternalStorageDirectory(), BACKUP_DB_DIRECTORY)
                val dbDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(this@migrateData) + BACKUP_DB_DIRECTORY)
                dbSrcDir.listFiles()?.let {
                    it.forEachIndexed { index, file ->
                        if (File(dbDestDir, file.name).exists()) {
                            File(dbDestDir, file.name).delete()
                        }
                        FileUtils.copyFileToDirectory(file, dbDestDir)
                        runOnUiThread {
                            binging.progressInfo.text = "$index/${it.size} (Database)"
                        }
                    }
                    dbSrcDir.renameTo(File(dbSrcDir.absolutePath + "_migration"))
                }
            }
        }

        realmInstance.close()
        runOnUiThread {
            binging.progressDialog.visibility = View.GONE
            binging.modalContainer.visibility = View.GONE
            if (isFontDirMigrate) {
                showAlertDialog("Font 리소스가 변경되어 애플리케이션을 다시 시작합니다.", DialogInterface.OnClickListener { _, _ ->
                    triggerRestart(DiaryMainActivity::class.java)
                }, false)
            }
        }
    }
}

fun Activity.appLaunched() {
    val appId = BuildConfig.APPLICATION_ID
    val defaultClassName = "${appId.removeSuffix(".debug")}.activities.IntroActivity"
    //
    packageManager.setComponentEnabledSetting(
        ComponentName(appId, defaultClassName),
        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
        PackageManager.DONT_KILL_APP
    )

    val lineClassName = "${appId.removeSuffix(".debug")}.activities.IntroActivity.Line"
    packageManager.setComponentEnabledSetting(
        ComponentName(appId, lineClassName),
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )
}

fun Activity.clearLockSettingsTemporary() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    val parsedDate: Date? = dateFormat.parse("2022-05-14 23:59:59")
    parsedDate?.let {
        val remainMinutes = it.time.minus(System.currentTimeMillis()).div(1000).div(60)
        if (remainMinutes > 0) {
            config.aafPinLockEnable = false
            config.fingerprintLockEnable = false
            showAlertDialog("Password lock setting is forcibly released. Password lock settings will be unavailable for the next $remainMinutes minutes.", null)
        }
    }
}

fun Activity.uriToFile(uri: Uri, photoPath: String): Boolean {
    var result = false
    try {
        val tempFile = File.createTempFile("TEMP_PHOTO", "AAF").apply { deleteOnExit() }
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)
        IOUtils.copy(inputStream, outputStream)
        IOUtils.closeQuietly(inputStream)
        IOUtils.closeQuietly(outputStream)

        val compressedFile = Compressor(this).setQuality(70).compressToFile(tempFile)
        FileUtils.copyFile(compressedFile, File(photoPath))
        result = true
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

fun Activity.triggerRestart(cls: Class<*>) {
    val intent = Intent(this, cls)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    this.startActivity(intent)
    finish()
    Runtime.getRuntime().exit(0)
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

fun Activity.hideSoftInputFromWindow() {
    val currentView = this.currentFocus
    if (currentView != null) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView.windowToken, 0)
    }
}
