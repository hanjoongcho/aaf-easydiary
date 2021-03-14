package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64.encodeToString
import android.view.View
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.isOreoPlus
import io.github.aafactory.commons.utils.BitmapUtils
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.coroutines.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.CheatSheetAdapter
import me.blog.korn123.easydiary.databinding.ActivityDevBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.services.BaseNotificationService
import me.blog.korn123.easydiary.services.NotificationService
import me.blog.korn123.easydiary.viewmodels.BaseDevViewModel
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

open class BaseDevActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityDevBinding
    private val mViewModel: BaseDevViewModel by viewModels()
    private val mLocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private var mCheatSheetList = arrayListOf<CheatSheetAdapter.CheatSheet>()
    private val mNetworkLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            makeToast("Network location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }
    private val mGPSLocationListener = object : LocationListener {
        override fun onLocationChanged(p0: Location) {
            makeToast("GPS location has been updated")
            mLocationManager.removeUpdates(this)
        }
        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_dev)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mViewModel

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = "Easy-Diary Dev Mode"
            setDisplayHomeAsUpEnabled(true)
        }

        setupActionLog()
        setupNextAlarm()
        setupNotification()
        setupClearUnusedPhoto()
        setupLocation()
        setupCoroutine()
        setupCheatSheet()
        setupHtmlBook()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager.run {
            removeUpdates(mGPSLocationListener)
            removeUpdates(mNetworkLocationListener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        pauseLock()

        when (requestCode) {
            REQUEST_CODE_ACTION_LOCATION_SOURCE_SETTINGS -> {
                makeSnackBar(if (isLocationEnabled()) "GPS provider setting is activated." else "The request operation did not complete normally.")
            }
            REQUEST_CODE_SAF_HTML_BOOK -> {
                intent?.let {
                    exportHtmlBook(it.data)
                }
            }
        }
    }


    /***************************************************************************************************
     *   test functions
     *
     ***************************************************************************************************/
    private fun setupNextAlarm() {
        mBinding.cardNextAlarm.setOnClickListener {
            val nextAlarm = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val triggerTimeMillis = (getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime ?: 0
                when (triggerTimeMillis > 0) {
                    true -> DateUtils.getFullPatternDateWithTime(triggerTimeMillis)
                    false -> "Alarm info is not exist."
                }
            } else {
                Settings.System.getString(contentResolver, Settings.System.NEXT_ALARM_FORMATTED)
            }

            toast(nextAlarm, Toast.LENGTH_LONG)
        }
    }

    private fun setupNotification() {
        mBinding.cardNotification1.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_DEV, createNotification(NotificationInfo(R.drawable.ic_diary_writing, true)))
            }
        }
        mBinding.cardNotification2.setOnClickListener {
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                notify(NOTIFICATION_ID_DEV, createNotification(NotificationInfo(R.drawable.ic_diary_backup_local, useActionButton = true, useCustomContentView = true)))
            }
        }
    }

    private fun setupActionLog() {
        mBinding.clearLog.setOnClickListener {
            EasyDiaryDbHelper.deleteActionLogAll()
            updateActionLog()
        }
        updateActionLog()
    }

    private fun updateActionLog() {
        val actionLogs: List<ActionLog> = EasyDiaryDbHelper.readActionLogAll()
        val sb = StringBuilder()
        actionLogs.map {
            sb.append("${it.className}-${it.signature}-${it.key}: ${it.value}\n")
        }
        mBinding.actionLog.text = sb.toString()
    }

    private fun setupClearUnusedPhoto() {
        mBinding.clearUnusedPhoto.setOnClickListener {
            val localPhotoBaseNames = arrayListOf<String>()
            val unUsedPhotos = arrayListOf<String>()
            File(EasyDiaryUtils.getApplicationDataDirectory(this) + DIARY_PHOTO_DIRECTORY).listFiles().map {
                localPhotoBaseNames.add(it.name)
            }

            EasyDiaryDbHelper.selectPhotoUriAll().map { photoUriDto ->
                if (!localPhotoBaseNames.contains(FilenameUtils.getBaseName(photoUriDto.getFilePath()))) {
                    unUsedPhotos.add(FilenameUtils.getBaseName(photoUriDto.getFilePath()))
                }
            }
            showAlertDialog(unUsedPhotos.size.toString(), null, true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocation() {
        mBinding.buttonRequestLastLocation.setOnClickListener {
            updateLocation()
        }

        mBinding.buttonUpdateGpsProvider.setOnClickListener {
            when (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F, mGPSLocationListener)
                }
                false -> makeSnackBar("GPS Provider is not available.")
            }
        }

        mBinding.buttonUpdateNetworkProvider.setOnClickListener {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            when (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                true -> {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0F, mNetworkLocationListener)
                }
                false -> makeSnackBar("Network Provider is not available.")
            }
        }
    }

    private var mCoroutineJob1: Job? = null
    private fun setupCoroutine() {
        fun updateConsole(message: String, tag: String = Thread.currentThread().name) {
            mBinding.textCoroutine1Console.append("$tag: $message\n")
            mBinding.scrollCoroutine.post { mBinding.scrollCoroutine.fullScroll(View.FOCUS_DOWN) }
        }
        suspend fun doWorld() {
            delay(1000)
        }

        mBinding.buttonCoroutineBasicStart.setOnClickListener {
            if (mCoroutineJob1?.isActive == true) {
                updateConsole("Job has already started.")
            } else {
                mCoroutineJob1 = GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                    for (i in 1..50) {
                        if (isActive) {
                            val currentThreadName = Thread.currentThread().name
                            runOnUiThread { updateConsole(i.toString(), currentThreadName) }
                            runBlocking {
                                delay(100)
                            }
                        }
                    }
                }
            }
        }

        mBinding.buttonCoroutineBasicStop.setOnClickListener {
            if (mCoroutineJob1?.isActive == true) {
                runBlocking { mCoroutineJob1?.cancelAndJoin() }
            } else {
                updateConsole("The job has been canceled")
            }
        }

        mBinding.buttonCoroutineBasicStatus.setOnClickListener {
            mCoroutineJob1?.let {
                when (it.isActive) {
                    true -> updateConsole("On")
                    false -> updateConsole("Off")
                }
            } ?: run {
                updateConsole("Coroutine is not initialized.")
            }
        }

        mBinding.buttonCoroutineMultiple.setOnClickListener {
            for (k in 1..3) {
                GlobalScope.launch { // launch a new coroutine and keep a reference to its Job
                    for (i in 1..10) {
                        val currentThreadName = Thread.currentThread().name
                        runOnUiThread { updateConsole(i.toString(), currentThreadName) }
                        runBlocking {
                            delay(100)
                        }
                    }
                }
            }
        }

        mBinding.buttonCoroutineBlocking.setOnClickListener {
            runBlocking {
                updateConsole("runBlocking block")
            }
        }
    }

    private fun setupCheatSheet() {
        mCheatSheetList.run {
            add(CheatSheetAdapter.CheatSheet("Package kotlin", "Explanation of kotlin basic functions", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.md"))
            add(CheatSheetAdapter.CheatSheet("Package kotlin.collections", "Explanation of kotlin collection functions", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.collections.md"))
            add(CheatSheetAdapter.CheatSheet("Cheat Sheet", "This page is a collection of useful link information such as open source projects and development related guides.", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/README.md"))
            add(CheatSheetAdapter.CheatSheet("Spring Annotation", "Describes annotations mainly used in Spring Framework", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/annotations/spring.md"))

            add(CheatSheetAdapter.CheatSheet("데이터베이스 표준화", "국가상수도데이터베이스표준화지침(20210101 개정)", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/design/database/standardization.md"))

            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Lambda Expression",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/lambda/LambdaExpressionTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Default Methods",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/defaultmethod/DefaultMethodTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Functions",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/functions/FunctionFunctionalInterfaceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream Count",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithCountTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream with Filter",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithFilterTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream with Map",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithMapTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream with Sorted",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithSortedTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream with Match",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithMatchTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream Reduce",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamReduceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Stream Consumer",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/consumer/ConsumerFunctionalInterfaceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Predicate",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/predicate/PredicateFunctionalInterfaceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Comparator",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/comparator/ComparatorFunctionalInterfaceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "Java 8 - Suppliers",
                    "",
                    "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/suppliers/SupplierFunctionalInterfaceTest.java", true
            ))
            add(CheatSheetAdapter.CheatSheet(
                    "ES6",
                    "var, let, const",
                    "https://gist.githubusercontent.com/hanjoongcho/983fe388a669f1da9df13cf64f63c5f3/raw/d1587f1da1d7ead1ba695e50094dbf52daaf6e1e/var-let-const.md", false
            ))
        }

        mBinding.recyclerCheatSheet.apply {
            layoutManager = LinearLayoutManager(this@BaseDevActivity, LinearLayoutManager.VERTICAL, false)
//            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            adapter =  CheatSheetAdapter(
                    this@BaseDevActivity,
                    mCheatSheetList,
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        val item = mCheatSheetList[position]
                        TransitionHelper.startActivityWithTransition(this@BaseDevActivity, Intent(this@BaseDevActivity, MarkDownViewActivity::class.java).apply {
                            putExtra(MarkDownViewActivity.OPEN_URL_INFO, item.url)
                            putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, item.title)
                            putExtra(MarkDownViewActivity.FORCE_APPEND_CODE_BLOCK, item.forceAppendCodeBlock)
                        })
                    }
            )
        }
    }

    private fun photoToBase64(photoPath: String): String {
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

    private fun resourceToBase64(resourceId: Int): String {
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

    private fun createHtmlString(): String {
        val diaryDivision = StringBuilder()
        val diaryList = EasyDiaryDbHelper.readDiary(null)
        for (i in 1..30) {
            val html = StringBuilder()
            val diary = diaryList[i]
            val resourceId = FlavorUtils.sequenceToSymbolResourceId(diary.weather)
            when (resourceId > 0) {
                true -> html.append("<div class='title'><img src='data:image/png;base64, ${resourceToBase64(resourceId)}' />${diary.title}</div>")
                false -> html.append("<div class='title'>${diary.title}</div>")
            }
            html.append("<div class='datetime'>${DateUtils.getFullPatternDateWithTimeAndSeconds(diary.currentTimeMillis)}</div>")
            html.append("<pre class='contents'>")
            html.append(diary.contents)
            html.append("</pre>")
            html.append("<div class='photo-container'>")
            diary.photoUris?.forEach {
                html.append("<div class='photo'><img src='data:image/png;base64, ${photoToBase64(EasyDiaryUtils.getApplicationDataDirectory(this) + it.getFilePath())}' /></div>")
            }
            html.append("</div>")
            html.append("<hr>")
            diaryDivision.append(html.toString())
        }

        val template = StringBuilder()
        template.append("<!DOCTYPE html>")
        template.append("<html>")
        template.append("<head>")
        template.append("<meta charset='UTF-8'>")
        template.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
        template.append("<title>Insert title here</title>")
        template.append("<style type='text/css'>")
        template.append("body { margin: 1rem; font-family: 나눔고딕, monospace; }")
        template.append("hr { margin: 1.5rem 0 }")
        template.append(".title { margin-top: 1rem; font-size: 1.3rem; }")
        template.append(".title img { width: 30px; margin-right: 1rem; }")
        template.append(".datetime { font-size: 0.8rem; text-align: right; }")
        template.append(".contents { margin-top: 1rem; font-size: 0.9rem; font-family: 나눔고딕, monospace; white-space: pre-wrap; }")
        template.append(".photo-container .photo { background: rgb(31 32 33); padding: 0.2rem; border-radius: 5px; margin-bottom: 0.2rem; }")
        template.append(".photo img { width: 100%; display: block; }")
        template.append("</style>")
        template.append("<body>")
        template.append(diaryDivision.toString())
        template.append("</body>")
        template.append("</html>")

        return template.toString()
    }

    private fun exportHtmlBook(uri: Uri?) {
        uri?.let {
            val os = contentResolver.openOutputStream(it)
            IOUtils.write(createHtmlString(), os, "UTF-8")
            os?.close()
        }
    }

    @SuppressLint("NewApi")
    private fun setupHtmlBook() {
        mBinding.buttonCreateHtml.setOnClickListener {
            writeFileWithSAF("EasyDiary_HTMLBook.html", MIME_TYPE_HTML, REQUEST_CODE_SAF_HTML_BOOK)
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun updateLocation() {
        fun setLocationInfo() {
            getLastKnownLocation()?.let {
                var info = "Longitude: ${it.longitude}\nLatitude: ${it.latitude}\n"
                getFromLocation(it.latitude, it.longitude, 1)?.let { address ->
                    if (address.isNotEmpty()) {
                        info += fullAddress(address[0])
                    }
                }
                mBinding.textLocationConsole.text = info
            }
        }
        when (hasGPSPermissions()) {
            true -> setLocationInfo()
            false -> {
                acquireGPSPermissions() {
                    setLocationInfo()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createNotification(notificationInfo: NotificationInfo): Notification {
        if (isOreoPlus()) {
            // Create the NotificationChannel
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("${NOTIFICATION_CHANNEL_ID}_dev", "${NOTIFICATION_CHANNEL_NAME}_dev", importance)
            channel.description = NOTIFICATION_CHANNEL_DESCRIPTION

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val title = "Notification Title"
        val text = "알림에 대한 본문 내용이 들어가는 영역입니다. 기본 템플릿을 확장형 알림을 구현할 수 있습니다."
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "${NOTIFICATION_CHANNEL_ID}_dev")
        notificationBuilder
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_easydiary)
                .setLargeIcon(BitmapFactory.decodeResource(resources, notificationInfo.largeIconResourceId))
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(
                        PendingIntent.getActivity(this, 0, Intent(this, DiaryMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }, PendingIntent.FLAG_UPDATE_CURRENT)
                )


        if (notificationInfo.useCustomContentView) {
            notificationBuilder
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(RemoteViews(applicationContext.packageName, R.layout.partial_notification))
        } else {
            notificationBuilder
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text).setSummaryText(title))
        }

        if (notificationInfo.useActionButton) {
            notificationBuilder.addAction(
                    R.drawable.ic_easydiary,
                    getString(R.string.dismiss),
                    PendingIntent.getService(this, 0, Intent(this, NotificationService::class.java).apply {
                        action = BaseNotificationService.ACTION_DISMISS_DEV
                    }, 0)
            )
        }

        return notificationBuilder.build()
    }

    companion object {
        const val NOTIFICATION_ID_DEV = 9999
    }
}


/***************************************************************************************************
 *   classes
 *
 ***************************************************************************************************/
data class NotificationInfo(var largeIconResourceId: Int, var useActionButton: Boolean = false, var useCustomContentView: Boolean = false)


/***************************************************************************************************
 *   extensions
 *
 ***************************************************************************************************/
fun fun1(param1: String, block: (responseData: String) -> String): String {
    println(param1)
    return block("")
}

fun fun2(param1: String, block: (responseData: String) -> Boolean): String {
    println(param1)
    var blockReturn = block(param1)
    return param1
}

fun test1() {
    val result = fun1("banana") { responseData ->
        "data: $responseData"
    }
    println(result)
}






