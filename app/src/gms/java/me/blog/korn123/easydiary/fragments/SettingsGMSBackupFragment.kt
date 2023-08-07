package me.blog.korn123.easydiary.fragments

import android.accounts.Account
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.convDateToTimeMillis
import me.blog.korn123.commons.utils.EasyDiaryUtils.getCalendarInstance
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity
import me.blog.korn123.easydiary.activities.BaseSettingsActivity
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.databinding.DialogSyncGoogleCalendarBinding
import me.blog.korn123.easydiary.databinding.FragmentSettingsBackupGmsBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.clearHoldOrientation
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.refreshApp
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.DIARY_DB_NAME
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.callAccountCallback
import me.blog.korn123.easydiary.helper.GoogleOAuthHelper.Companion.initGoogleSignAccount
import me.blog.korn123.easydiary.helper.SETTING_FLAG_EXPORT_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_IMPORT_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SYMBOL_GOOGLE_CALENDAR
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SettingsGMSBackupFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsBackupGmsBinding
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mContext: Context
    private lateinit var mRequestExternalStoragePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mRequestGoogleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var mRequestGoogleDrivePermissions: ActivityResultLauncher<Intent>
    private lateinit var mRequestGoogleCalendarPermissions: ActivityResultLauncher<Intent>
    private var mTaskFlag = 0


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestExternalStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            requireActivity().run {
                pauseLock()
                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    when (mTaskFlag) {
                        SETTING_FLAG_EXPORT_GOOGLE_DRIVE -> backupDiaryRealm()
                        SETTING_FLAG_IMPORT_GOOGLE_DRIVE -> recoverDiaryRealm()
                        SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE -> backupDiaryPhoto()
                        SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE -> recoverDiaryPhoto()
                    }
                } else {
                    makeSnackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.guide_message_3))
                }
            }
        }

        mRequestGoogleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireActivity().run {
                pauseLock()
                when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    true -> {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                        val googleSignAccount = task.getResult(ApiException::class.java)
                        googleSignAccount?.account?.let {
                            callAccountCallback(it)
                        }
                    }
                    false -> {
                        makeSnackBar("Google account verification failed.")
                        progressContainer.visibility = View. GONE
                    }
                }
            }
        }

        mRequestGoogleDrivePermissions = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireActivity().run {
                pauseLock()
                when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    true -> {
                        mPermissionCallback.invoke()
                    }
                    false -> {
                        makeSnackBar("Google account verification failed.")
                        progressContainer.visibility = View. GONE
                    }
                }
            }
        }

        mRequestGoogleCalendarPermissions = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requireActivity().run {
                pauseLock()
                when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                    true -> {
                        mPermissionCallback.invoke()
                    }
                    false -> {
                        makeSnackBar("Google account verification failed.")
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentSettingsBackupGmsBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer = (requireActivity() as BaseSettingsActivity).getProgressContainer()

        // Clear google OAuth token generated prior to version 1.4.80
        if (!requireActivity().config.clearLegacyToken) GoogleOAuthHelper.signOutGoogleOAuth(requireActivity(), false)

        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
        initPreference()
    }


    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
    // FIXME: workaround
    private lateinit var mPermissionCallback: () -> Unit
    private fun requestDrivePermissions(account: Account, permissionCallback: () -> Unit) {
        mPermissionCallback = permissionCallback
        val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(mContext, arrayListOf(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = account
        val googleDriveService: Drive = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                .setApplicationName(getString(R.string.app_name))
                .build()

        val executor = Executors.newSingleThreadExecutor()
        Tasks.call(executor, Callable<FileList> {
            try {
                var r = googleDriveService.files().list().setQ("'root' in parents and name = '${DriveServiceHelper.AAF_ROOT_FOLDER_NAME}' and trashed = false").setSpaces("drive").execute()
                mPermissionCallback.invoke()
                r
            } catch (e: UserRecoverableAuthIOException) {
                mRequestGoogleDrivePermissions.launch(e.intent)
                null
            }
        })
    }



    private fun backupDiaryRealm() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                DriveServiceHelper(mContext, account).run {
                    initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_REALM_FOLDER_NAME) {
                        createFile(
                                it!!, realmPath,
                                DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                                EasyDiaryUtils.easyDiaryMimeType
                        ).addOnSuccessListener {
                            progressContainer.visibility = View. GONE
                            requireActivity().makeSnackBar(getString(R.string.backup_completed_message))
                            requireActivity().config.diaryBackupGoogle = System.currentTimeMillis()
                            requireActivity().clearHoldOrientation()
                        }.addOnFailureListener { e ->
                            requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                            progressContainer.visibility = View.GONE
                            requireActivity().clearHoldOrientation()
                        }
                    }
                }
            }
        }
    }

    private fun recoverDiaryRealm() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }

    private fun openRealmFilePickerDialog() {
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                val driveServiceHelper = DriveServiceHelper(mContext, account)
//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)
                driveServiceHelper.queryFiles("(mimeType = '${EasyDiaryUtils.easyDiaryMimeTypeAll.joinToString("' or mimeType = '")}') and trashed = false", 1000)
                        .addOnSuccessListener {
                            var alertDialog: AlertDialog? = null
                            val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                            it.files.map { file ->
                                val itemInfo = hashMapOf<String, String>("name" to file.name, "id" to file.id, "createdTime" to file.createdTime.toString())
                                realmFiles.add(itemInfo)
                            }
                            val builder = AlertDialog.Builder(requireActivity())
                            builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> requireActivity().clearHoldOrientation() }
//                        builder.setMessage(getString(R.string.open_realm_file_message))
                            val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                            val listView = fontView.findViewById<ListView>(R.id.files)
                            val adapter = RealmFileItemAdapter(requireActivity(), R.layout.item_realm_file, realmFiles)
                            listView.adapter = adapter
                            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                                val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                                itemInfo["id"]?.let { realmFileId ->
                                    progressContainer.visibility = View.VISIBLE
                                    val realmPath = EasyDiaryDbHelper.getRealmPath()
                                    EasyDiaryDbHelper.closeInstance()
                                    driveServiceHelper.downloadFile(realmFileId, realmPath).run {
                                        addOnSuccessListener {
                                            requireActivity().refreshApp()
                                        }
                                        addOnFailureListener {  }
                                    }

                                }
                                alertDialog?.cancel()
                            }

                            alertDialog = builder.create().apply { requireActivity().updateAlertDialog(this, null, fontView, "${getString(R.string.open_realm_file_title)} (Total: ${it.files.size})") }
                            progressContainer.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                            progressContainer.visibility = View.GONE
                        }
            }
        }
    }

    private fun recoverDiaryPhoto() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                requireActivity().runOnUiThread {
                    progressContainer.visibility = View.GONE
                    requireActivity().run {
                        showAlertDialog(
                            getString(R.string.recover_confirm_attached_photo),
                            { _, _ ->
                                val recoverPhotoService =
                                    Intent(this, RecoverPhotoService::class.java)
                                startService(recoverPhotoService)
                                finish()
                            },
                            { _, _ -> clearHoldOrientation() },
                            DialogMode.INFO,
                            false
                        )
                    }
                }
            }
        }
    }

    private fun backupDiaryPhoto() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestDrivePermissions(account) {
                DriveServiceHelper(mContext, account).run {
                    initDriveWorkingDirectory(DriveServiceHelper.AAF_EASY_DIARY_PHOTO_FOLDER_NAME) { photoFolderId ->
                        progressContainer.visibility = View.GONE
                        requireActivity().run {
                            showAlertDialog(
                                getString(R.string.backup_confirm_message),
                                { _, _ ->
                                    val backupPhotoService =
                                        Intent(this, BackupPhotoService::class.java)
                                    backupPhotoService.putExtra(
                                        DriveServiceHelper.WORKING_FOLDER_ID,
                                        photoFolderId
                                    )
                                    ContextCompat.startForegroundService(
                                        context,
                                        backupPhotoService
                                    )
                                    finish()
                                },
                                { _, _ -> clearHoldOrientation() },
                                DialogMode.INFO,
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestCalendarPermissions(account: Account, permissionCallback: () -> Unit) {
        mPermissionCallback = permissionCallback
        val credential: GoogleAccountCredential =
            GoogleAccountCredential.usingOAuth2(requireActivity(), arrayListOf(CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR_EVENTS_READONLY))
                .apply { selectedAccount = account }
        val calendarService: Calendar = Calendar.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
            .setApplicationName(getString(R.string.app_name))
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                calendarService.calendarList().list().execute()
                mPermissionCallback.invoke()
            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    mRequestGoogleCalendarPermissions.launch(e.intent)
                }
            }
        }
    }

    private fun getCurrentYearLastDatetime(): DateTime {
        val calendar = java.util.Calendar.getInstance(Locale.getDefault()).apply {
            set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
            set(java.util.Calendar.DATE, this.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, this.getActualMaximum(java.util.Calendar.HOUR_OF_DAY))
            set(java.util.Calendar.MINUTE, this.getActualMaximum(java.util.Calendar.MINUTE))
            set(java.util.Calendar.SECOND, this.getActualMaximum(java.util.Calendar.SECOND))
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return DateTime(calendar.timeInMillis)
    }

    private lateinit var mSDatePickerDialog: DatePickerDialog
    private lateinit var mEDatePickerDialog: DatePickerDialog
    private lateinit var mTimeMax: DateTime
    private lateinit var mTimeMin: DateTime
    private fun syncGoogleCalendar() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
            requestCalendarPermissions(account) {
                val credential: GoogleAccountCredential =
                    GoogleAccountCredential.usingOAuth2(
                        requireActivity(),
                        arrayListOf(CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR_EVENTS_READONLY)
                    ).apply {
                        selectedAccount = account
                    }
                val calendarService = Calendar.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential
                )
                    .setApplicationName(getString(R.string.app_name))
                    .build()

                fun fetchData(calendarId: String, nextPageToken: String?, total: Int = 0) {
                    var insertCount = 0
                    progressContainer.visibility = View.VISIBLE
                    mBinding.syncGoogleCalendarProgress.setProgressCompat(0, false)
                    mBinding.syncGoogleCalendarProgress.visibility = View.VISIBLE

                    CoroutineScope(Dispatchers.IO).launch {
                        val result = if (nextPageToken == null) {
                            calendarService
                                .events()
                                .list(calendarId)
                                .setMaxResults(2000)
                                .setTimeMin(mTimeMin)
                                .setTimeMax(mTimeMax)
                                .setSingleEvents(true)
                                .execute()
                        } else {
                            calendarService
                                .events()
                                .list(calendarId)
                                .setPageToken(nextPageToken)
                                .setMaxResults(2000)
                                .setTimeMin(mTimeMin)
                                .setTimeMax(mTimeMax)
                                .setSingleEvents(true)
                                .execute()
                        }
                        withContext(Dispatchers.Main) { progressContainer.visibility = View.GONE }
//                        val descriptions = arrayListOf<String>()
                            result.items.forEachIndexed { index, item ->
                                Log.i(AAF_TEST, "$index ${item.start?.date} ${item.summary} ${item.start?.dateTime}")
//                                descriptions.add(item.summary)
                                val timeMillis = if (item.start?.dateTime != null) item.start.dateTime.value else item.start?.date?.value ?: 0
                                withContext(Dispatchers.Main) {
                                    if (EasyDiaryDbHelper.findDiary(item.summary)
                                            .none { diary -> diary.currentTimeMillis == timeMillis }
                                        && !(item.description == null && item.summary == null)) {
                                        EasyDiaryDbHelper.insertDiary(
                                            Diary(
                                                BaseDiaryEditingActivity.DIARY_SEQUENCE_INIT,
                                                timeMillis,
                                                if (item.description != null) item.summary else "",
                                                item.description ?: item.summary,
                                                SYMBOL_GOOGLE_CALENDAR,
                                                item?.start?.dateTime == null
                                            )
                                        )
                                        insertCount++
                                    }
                                    mBinding.syncGoogleCalendarProgress.setProgressCompat(index.div(result.items.size.toFloat()).times(100).toInt(), true)
                                }
                            }
                            if (result.nextPageToken != null) {
                                fetchData(calendarId, result.nextPageToken, total.plus(insertCount))
                            } else {
                                withContext(Dispatchers.Main) {
                                    mBinding.syncGoogleCalendarProgress.visibility = View.GONE
                                    requireActivity().showAlertDialog("${DateUtils.getDateTimeStringFromTimeMillis(mTimeMin.value)} ~ ${DateUtils.getDateTimeStringFromTimeMillis(mTimeMax.value)} 기간에 등록된 ${total.plus(insertCount)}건의 이벤트가 등록되었습니다.", null, null)
                                }
                            }
                    }
                }
                fun fetchCalendarList() {
                    var alertDialog: AlertDialog? = null
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = calendarService.calendarList().list().execute()
                        withContext(Dispatchers.Main) {
                            val builder = AlertDialog.Builder(requireActivity())
                            builder.setNegativeButton(getString(android.R.string.cancel), null)
                            val dialogSyncGoogleCalendarBinding = DialogSyncGoogleCalendarBinding.inflate(layoutInflater)
                            val calendarInfo = ArrayList<Map<String, String>>()
                            result.items.forEach { calendar ->
                                calendarInfo.add(mapOf(
                                    "optionTitle" to calendar.summary,
                                    "optionValue" to calendar.id
                                ))
                            }
                            val optionItemAdapter = OptionItemAdapter(requireActivity(), R.layout.item_check_label, calendarInfo, null, null, false)
                            dialogSyncGoogleCalendarBinding.run {
                                listView.adapter = optionItemAdapter
                                listView.setOnItemClickListener { parent, view, position, id ->
                                    calendarInfo[position]["optionValue"]?.let {
                                        fetchData(it, null)
                                        alertDialog?.dismiss()
                                    }
                                }

                                val fromCalendar = getCalendarInstance(false, -1)
                                val toCalendar = getCalendarInstance(true, 1)
                                mTimeMin = DateTime(fromCalendar.timeInMillis)
                                mTimeMax = DateTime(toCalendar.timeInMillis)

                                mSDatePickerDialog = DatePickerDialog(
                                    requireContext()
                                    , { _, year, month, dayOfMonth ->
                                        val startMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year)
                                        mTimeMin = DateTime(startMillis)
                                        textSyncFromDate.text = DateUtils.getDateTimeStringFromTimeMillis(startMillis)
                                    }
                                    , fromCalendar.get(java.util.Calendar.YEAR)
                                    , fromCalendar.get(java.util.Calendar.MONTH)
                                    , fromCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                                )

                                mEDatePickerDialog = DatePickerDialog(
                                    requireContext()
                                    , { _, year, month, dayOfMonth ->
                                        val endMillis = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, true)
                                        mTimeMax = DateTime(endMillis)
                                        textSyncToDate.text = DateUtils.getDateTimeStringFromTimeMillis(endMillis)
                                    }
                                    , toCalendar.get(java.util.Calendar.YEAR)
                                    , toCalendar.get(java.util.Calendar.MONTH)
                                    , toCalendar.get(java.util.Calendar.DAY_OF_MONTH)
                                )

                                textSyncFromDate.run {
                                    setOnClickListener { mSDatePickerDialog.show() }
                                    text = DateUtils.getDateTimeStringFromTimeMillis(convDateToTimeMillis(false, -1))
                                }
                                textSyncToDate.run {
                                    setOnClickListener { mEDatePickerDialog.show() }
                                    text = DateUtils.getDateTimeStringFromTimeMillis(convDateToTimeMillis(true))
                                }

                                requireActivity().run {
                                    FontUtils.setFontsTypeface(requireContext(), null, cardSyncOptions)
                                    initTextSize(cardSyncOptions)
                                    updateTextColors(cardSyncOptions)
                                    updateAppViews(dialogSyncGoogleCalendarBinding.root)
                                    updateCardViewPolicy(dialogSyncGoogleCalendarBinding.root)
                                }
                            }
                            requireActivity().clearHoldOrientation()
                            progressContainer.visibility = View.GONE
                            alertDialog = builder.create().apply {
                                requireActivity().updateAlertDialogWithIcon(DialogMode.INFO, this, null, dialogSyncGoogleCalendarBinding.root, "Sync Google Calendar")
                            }
                        }
                    }
                }
                fetchCalendarList()
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.restoreSetting -> {
                mTaskFlag = SETTING_FLAG_IMPORT_GOOGLE_DRIVE
                if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    recoverDiaryRealm()
                } else { // Permission has already been granted
                    mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.backupSetting -> {
                mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    backupDiaryRealm()
                } else { // Permission has already been granted
                    mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.backupAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
                when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> backupDiaryPhoto()
                    false -> mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.recoverAttachPhoto -> {
                mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
                when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> recoverDiaryPhoto()
                    false -> mRequestExternalStoragePermissionLauncher.launch(EXTERNAL_STORAGE_PERMISSIONS)
                }
            }
            R.id.signInGoogleOAuth -> {
                when (GoogleOAuthHelper.isValidGoogleSignAccount(requireActivity())) {
                    false -> {
                        initGoogleSignAccount(requireActivity(), mRequestGoogleSignInLauncher) { account ->
                            requestDrivePermissions(account) {
                                determineAccountInfo()
                            }
                        }
                    }
                    true -> {}
                }
            }
            R.id.signOutGoogleOAuth -> {
                GoogleOAuthHelper.signOutGoogleOAuth(requireActivity())
                determineAccountInfo()
            }
            R.id.syncGoogleCalendar -> {
                syncGoogleCalendar()
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
            restoreSetting.setOnClickListener(mOnClickListener)
            backupSetting.setOnClickListener(mOnClickListener)
            backupAttachPhoto.setOnClickListener(mOnClickListener)
            recoverAttachPhoto.setOnClickListener(mOnClickListener)
            signInGoogleOAuth.setOnClickListener(mOnClickListener)
            signOutGoogleOAuth.setOnClickListener(mOnClickListener)
            syncGoogleCalendar.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        determineAccountInfo()
    }

    private fun determineAccountInfo() {
        mBinding.run {
            when (GoogleOAuthHelper.isValidGoogleSignAccount(requireActivity())) {
                true -> {
                    profilePhoto.visibility = View.VISIBLE
                    signInGoogleOAuthTitle.text = getString(R.string.google_drive_account_information_title)
                    GoogleOAuthHelper.getGoogleSignAccount(requireActivity())?.run {
                        val sb = StringBuilder()
                        sb.append(this.displayName +  System.getProperty("line.separator"))
                        sb.append(this.email)
                        requireActivity().runOnUiThread { accountInfo.text = sb.toString() }
                        Glide.with(requireActivity())
                                .load(this.photoUrl)
                                .apply(RequestOptions().circleCrop())
                                .into(profilePhoto)
                    }
                }
                false -> {
                    profilePhoto.visibility = View.GONE
                    signInGoogleOAuthTitle.text = getString(R.string.google_drive_account_sign_in_title)
                    accountInfo.text = getString(R.string.google_drive_account_sign_in_description)
                }
            }
        }
    }
}
