package me.blog.korn123.easydiary.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.models.Release
import me.blog.korn123.commons.utils.FlavorUtils.getDiarySymbolMap
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryMainActivity
import me.blog.korn123.easydiary.activities.EasyDiaryActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.adapters.SymbolPagerAdapter
import me.blog.korn123.easydiary.dialogs.WhatsNewDialog
import me.blog.korn123.easydiary.enums.GridSpanMode
import me.blog.korn123.easydiary.helper.DIARY_EXECUTION_MODE
import me.blog.korn123.easydiary.helper.EXECUTION_MODE_ACCESS_FROM_OUTSIDE
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_COARSE_LOCATION
import me.blog.korn123.easydiary.helper.PERMISSION_ACCESS_FINE_LOCATION
import me.blog.korn123.easydiary.helper.PinLockConstants
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.presentation.fingerprintlock.FingerprintLockActivity
import me.blog.korn123.easydiary.presentation.pinlock.PinLockActivity
import me.blog.korn123.easydiary.views.SlidingTabLayout

/***************************************************************************************************
 *   Hilt EntryPoint for Repository access in extensions
 ***************************************************************************************************/

/***************************************************************************************************
 *   Confirm Permissions
 *
 ***************************************************************************************************/
fun Activity.confirmPermission(
    permissions: Array<String>,
    requestCode: Int,
) {
    if (permissions.any { permission -> ActivityCompat.shouldShowRequestPermissionRationale(this, permission) }) {
        AlertDialog
            .Builder(this)
            .setMessage(getString(R.string.permission_confirmation_dialog_message))
            .setTitle(getString(R.string.permission_confirmation_dialog_title))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> ActivityCompat.requestPermissions(this, permissions, requestCode) }
            .show()
    } else {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}

fun Activity.confirmExternalStoragePermission(
    permissions: Array<String>,
    activityResultLauncher: ActivityResultLauncher<Array<String>>,
) {
    if (permissions.any { permission -> ActivityCompat.shouldShowRequestPermissionRationale(this, permission) }) {
        AlertDialog
            .Builder(this)
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
fun Activity.makeSnackBar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
) {
    Snackbar
        .make(findViewById(android.R.id.content), message, duration)
        .setBackgroundTint(config.primaryColor)
        .setTextColor(Color.WHITE)
        .setAction("Action", null)
        .show()
}

/***************************************************************************************************
 *   Screen Dimension
 *
 ***************************************************************************************************/

fun Activity.isLandScape(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Activity.actionBarHeight(): Int {
    val typedValue = TypedValue()
    var actionBarHeight = 0
    if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
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
    return (getDefaultDisplay().x * ratio * scaleFactor).toInt()
}

/**
 * Handles horizontal system window insets.
 * For Material ActionBars using XML layouts, the ActionBar UI might be obscured by system bars in landscape mode,
 * which cannot be handled at the XML level, so it must be handled programmatically.
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
 * Adds bottom margin equal to the navigation bar height to the given view
 * when the application screen extends into the bottom navigation area.
 * Version check is omitted since Edge-to-Edge mode is not forced below SDK 35.
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
 * Applies IME inset for SDK 35+.
 * The IME area appears at the bottom of the screen when the keyboard is raised,
 * and this adjusts the bottom margin of the given view by the height of that area.
 * This feature only applies in portrait mode and is not applied in landscape mode.
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
 * Forcefully hides system bars in landscape mode for SDK 35+.
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
 * Changes the system status bar background color or text (icon) color.
 * - SDK 35+: Changes text (icon) color.
 * - Below SDK 35: Makes background color translucent.
 * - Typically called when the screen extends fullscreen without a top action bar.
 */
fun Activity.applyFullScreenStatusBarTheme(checkColor: Int = config.screenBackgroundColor) {
    if (isVanillaIceCreamPlus()) {
        // true: Light background -> Dark text (light status bar icons)
        // false: Dark background -> White text
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = isColorLight(checkColor)
    } else {
        @Suppress("DEPRECATION")
        window.statusBarColor = ColorUtils.setAlphaComponent(config.primaryColor, 150)

        val color =
            if (isColorLight(config.primaryColor)) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
        @Suppress("DEPRECATION")
        window.navigationBarColor = ColorUtils.setAlphaComponent(color.toArgb(), 150)
    }
}

fun Activity.updateStatusBarAppearance(checkColor: Int = config.primaryColor) {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = isColorLight(checkColor)
}

fun Activity.updateNavigationBarAppearance(checkColor: Int = config.primaryColor) {
    val color =
        if (isColorLight(checkColor)) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black

    window.navigationBarColor = color.toArgb()

    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightNavigationBars = isColorLight(checkColor)
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
                startActivity(
                    Intent(this, FingerprintLockActivity::class.java).apply {
                        putExtra(FingerprintLockConstants.LAUNCHING_MODE, FingerprintLockConstants.ACTIVITY_UNLOCK)
                    },
                )
            }

            config.aafPinLockEnable -> {
                startActivity(
                    Intent(this, PinLockActivity::class.java).apply {
                        putExtra(PinLockConstants.LAUNCHING_MODE, PinLockConstants.ACTIVITY_UNLOCK)
                    },
                )
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
    val uri = Uri.parse("market://details?id=$targetAppId")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
    )
    try {
        startActivity(goToMarket)
    } catch (_: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$targetAppId"),
            ),
        )
    }
}

fun Activity.checkWhatsNew(
    releases: List<Release>,
    currVersion: Int,
    applyFilter: Boolean = true,
) {
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

fun Activity.refreshApp() {
    getSystemService<NotificationManager>()?.cancelAll()

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
suspend fun Activity.syncCustomSymbolPaths() {
    val items =
        this.diaryRepository
            .getDiariesWithPhotos(
                query = null,
                symbolSequence = SYMBOL_EASTER_EGG,
            )

    val diary = items.firstOrNull()
    val result = diary?.photoUris?.map { it } ?: listOf()
    config.customSymbolPaths = result
}

fun Activity.openFeelingSymbolDialog(
    guideMessage: String,
    selectedSymbolSequence: Int = 0,
    symbolUsedCountMap: Map<Int, Int>,
    callback: (Int) -> Unit,
) {
    var dialog: Dialog? = null
    val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val symbolDialog = inflater.inflate(R.layout.dialog_feeling_pager, null)

    val itemList = arrayListOf<Array<String>>()
    val categoryList = arrayListOf<String>()
    var tabIndex = 0

    // Append recently used symbols
    if (symbolUsedCountMap.isNotEmpty()) {
        val symbolMap = getDiarySymbolMap(this)
        categoryList.add(getString(R.string.recently_used_symbol))
        itemList.add(
            arrayOf(
                getUncheckedSymbolItem(),
                *symbolUsedCountMap.entries
                    .map { entry ->
                        "${entry.key}|${symbolMap[entry.key] ?: "🎃"}"
                    }.toTypedArray(),
            ),
        )
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

    val currentItem =
        when (selectedSymbolSequence) {
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

    val viewPager =
        symbolDialog.findViewById<androidx.viewpager.widget.ViewPager>(R.id.viewpager).apply {
            setBackgroundColor(config.backgroundColor)
        }
    symbolDialog.findViewById<TextView>(R.id.diarySymbolGuide)?.let {
        it.text = guideMessage
    }
    val symbolPagerAdapter =
        SymbolPagerAdapter(this, itemList, categoryList, selectedSymbolSequence) { symbolSequence ->
            callback.invoke(symbolSequence)
            dialog?.dismiss()
        }
    viewPager.adapter = symbolPagerAdapter

    val slidingTabLayout =
        symbolDialog
            .findViewById<SlidingTabLayout>(
                R.id.sliding_tabs,
            ).apply { setBackgroundColor(config.backgroundColor) }
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

fun Activity.addUserCustomSymbols(
    categoryList: ArrayList<String>,
    itemList: ArrayList<Array<String>>,
) {
    // FIXME: WIP START
    var customSymbolSequence = SYMBOL_USER_CUSTOM_START
    if (config.enableDebugMode) {
        val customSymbols = arrayListOf<String>()
        categoryList.add("Custom")
        config.customSymbolPaths.forEach { _ ->
//            item.getFilePath()
            customSymbols.add("$customSymbolSequence|${customSymbolSequence++}")
        }
        itemList.add(arrayOf(getUncheckedSymbolItem(), "$SYMBOL_EASTER_EGG|Easter Egg", *customSymbols.toTypedArray()))
    }
    // FIXME: WIP END
}

fun Activity.addCategory(
    itemList: ArrayList<Array<String>>,
    categoryList: ArrayList<String>,
    resourceName: String,
    categoryName: String,
) {
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

fun Activity.openGridSettingDialog(
    rootView: ViewGroup,
    gridSpanMode: GridSpanMode,
    callback: (spanCount: Int) -> Unit,
) {
    var alertDialog: AlertDialog? = null
    val builder = AlertDialog.Builder(this)
    builder.setNegativeButton(getString(android.R.string.cancel), null)
    val inflater = getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val containerView = inflater.inflate(R.layout.dialog_option_item, rootView, false)
    val listView = containerView.findViewById<ListView>(R.id.listView)

    val maxSpanCount =
        when {
            isLandScape() && gridSpanMode == GridSpanMode.DIARY_MAIN -> 5
            !isLandScape() && gridSpanMode == GridSpanMode.DIARY_MAIN -> 3
            isLandScape() && (gridSpanMode == GridSpanMode.POSTCARD || gridSpanMode == GridSpanMode.GALLERY) -> 10
            !isLandScape() && (gridSpanMode == GridSpanMode.POSTCARD || gridSpanMode == GridSpanMode.GALLERY) -> 5
            else -> 2
        }
    val optionItems = mutableListOf<Map<String, String>>()
    for (i in 1..maxSpanCount) {
        optionItems.add(mapOf("optionTitle" to getString(R.string.postcard_grid_option_column_number, i), "optionValue" to i.toString()))
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
    listView.onItemClickListener =
        AdapterView.OnItemClickListener { parent, _, position, _ ->
            @Suppress("UNCHECKED_CAST")
            val optionInfo = parent.adapter.getItem(position) as HashMap<String, String>
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

    // Since status bar control cannot be done directly on SDK 35+, a dummy view is used.
    if (isVanillaIceCreamPlus()) findViewById<View>(R.id.dummyStatusBar)?.setBackgroundColor(getStatusBarColor(color))
}

fun EasyDiaryActivity.acquireGPSPermissions(
    activityResultLauncher: ActivityResultLauncher<Intent>,
    callback: () -> Unit,
) {
    when (
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ).any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission,
            )
        }
    ) {
        true -> {
            // If authorization is denied
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.holdCurrentOrientation() {
    when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        Configuration.ORIENTATION_LANDSCAPE -> {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        else -> {}
    }
}

fun Activity.clearHoldOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

fun Activity.isGmsOrLabFlavor(): Boolean = !BuildConfig.IS_FOSS

fun Activity.isFossFlavor(): Boolean = BuildConfig.IS_FOSS

fun Activity.isLabFlavor(): Boolean = BuildConfig.IS_LAB
