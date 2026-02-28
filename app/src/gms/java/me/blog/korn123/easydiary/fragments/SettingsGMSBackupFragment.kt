package me.blog.korn123.easydiary.fragments

import GoogleAuthManager
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.getCalendarInstance
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
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
import me.blog.korn123.easydiary.extensions.makeToast
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
import me.blog.korn123.easydiary.helper.DriveServiceHelper
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.GDriveConstants
import me.blog.korn123.easydiary.helper.RealmConstants
import me.blog.korn123.easydiary.helper.SETTING_FLAG_EXPORT_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_IMPORT_GOOGLE_DRIVE
import me.blog.korn123.easydiary.helper.SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
import me.blog.korn123.easydiary.services.BackupPhotoService
import me.blog.korn123.easydiary.services.RecoverPhotoService
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SimpleCardWithImage
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel
import java.util.Locale
import kotlin.coroutines.resume

class SettingsGMSBackupFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsBackupGmsBinding
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var mRequestExternalStoragePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var googleAuthLauncher: ActivityResultLauncher<Intent>
    private var mTaskFlag = 0
    private val mSettingsViewModel: SettingsViewModel by activityViewModels()
    private val authManager by lazy { GoogleAuthManager(requireContext()) }

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRequestExternalStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
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
                        makeSnackBar(
                            requireActivity().findViewById(android.R.id.content),
                            getString(R.string.guide_message_3),
                        )
                    }
                }
            }

        googleAuthLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requireActivity().run {
                    pauseLock()
                    when (it.resultCode == Activity.RESULT_OK && it.data != null) {
                        true -> {
                            permissionContinuation?.resume(true)
                        }

                        false -> {
                            permissionContinuation?.resume(false)
                            progressContainer.visibility = View.GONE
                        }
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentSettingsBackupGmsBinding.inflate(layoutInflater)
        return mBinding.root
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer = (requireActivity() as BaseSettingsActivity).getProgressContainer()
        updateFragmentUI(mBinding.root)
        initPreference()

        mBinding.composeView.setContent {
            AppTheme {
                val configuration = LocalConfiguration.current
                FlowRow(
                    maxItemsInEachRow = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2,
                    modifier = Modifier,
                ) {
                    val settingCardModifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)

                    val enableCardViewPolicy: Boolean by mSettingsViewModel.enableCardViewPolicy.observeAsState(
                        true,
                    )
                    val fontSize: Float by mSettingsViewModel.fontSize.observeAsState(config.settingFontSize)
                    val lineSpacingScaleFactor: Float by mSettingsViewModel.lineSpacingScaleFactor.observeAsState(
                        config.lineSpacingScaleFactor,
                    )
                    val fontFamily: FontFamily? by mSettingsViewModel.fontFamily.observeAsState(
                        FontUtils.getComposeFontFamily(requireContext()),
                    )

                    val informationTitle: String by mSettingsViewModel.informationTitle.observeAsState(
                        getString(R.string.google_drive_account_sign_in_title),
                    )
                    val profileImageUrl: Uri? by mSettingsViewModel.profileImageUrl.observeAsState(
                        null,
                    )
                    val accountInfo: String by mSettingsViewModel.accountInfo.observeAsState(
                        getString(R.string.google_drive_account_sign_in_description),
                    )

                    determineAccountInfo()
                    SimpleCardWithImage(
                        title = informationTitle,
                        description = accountInfo,
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_oauth2,
                        imageUrl = profileImageUrl,
                    ) {
                        when (authManager.isLoggedInLocal()) {
                            false -> {
                                lifecycleScope.launch {
                                    authManager.getGoogleAccount()
                                    requestDrivePermissions()
                                    determineAccountInfo()
                                }
                            }

                            true -> {}
                        }
                    }
                    SimpleCard(
                        title = getString(R.string.google_drive_account_sign_out_title),
                        description = getString(R.string.google_drive_account_sign_out_description),
                        modifier = settingCardModifier,
                    ) {
                        lifecycleScope.launch {
                            progressContainer.visibility = View.VISIBLE
                            authManager.signOut()
                            determineAccountInfo()
                            progressContainer.visibility = View.GONE
                        }
                    }
                    SimpleCardWithImage(
                        title = getString(R.string.backup_diary),
                        description = getString(R.string.backup_diary_summary),
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_drive,
                    ) {
                        mTaskFlag = SETTING_FLAG_EXPORT_GOOGLE_DRIVE
                        if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            backupDiaryRealm()
                        } else { // Permission has already been granted
                            mRequestExternalStoragePermissionLauncher.launch(
                                EXTERNAL_STORAGE_PERMISSIONS,
                            )
                        }
                    }
                    SimpleCardWithImage(
                        title = getString(R.string.restore_diary),
                        description = getString(R.string.restore_diary_summary),
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_drive,
                    ) {
                        mTaskFlag = SETTING_FLAG_IMPORT_GOOGLE_DRIVE
                        if (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            recoverDiaryRealm()
                        } else { // Permission has already been granted
                            mRequestExternalStoragePermissionLauncher.launch(
                                EXTERNAL_STORAGE_PERMISSIONS,
                            )
                        }
                    }
                    SimpleCardWithImage(
                        title = getString(R.string.backup_attach_photo_title),
                        description = getString(R.string.backup_attach_photo_summary),
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_drive,
                    ) {
                        mTaskFlag = SETTING_FLAG_EXPORT_PHOTO_GOOGLE_DRIVE
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                backupDiaryPhoto()
                            }

                            false -> {
                                mRequestExternalStoragePermissionLauncher.launch(
                                    EXTERNAL_STORAGE_PERMISSIONS,
                                )
                            }
                        }
                    }
                    SimpleCardWithImage(
                        title = getString(R.string.recover_attach_photo_title),
                        description = getString(R.string.recover_attach_photo_summary),
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_drive,
                    ) {
                        mTaskFlag = SETTING_FLAG_IMPORT_PHOTO_GOOGLE_DRIVE
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                recoverDiaryPhoto()
                            }

                            false -> {
                                mRequestExternalStoragePermissionLauncher.launch(
                                    EXTERNAL_STORAGE_PERMISSIONS,
                                )
                            }
                        }
                    }
                    SimpleCardWithImage(
                        title = getString(R.string.sync_google_calendar_event_title),
                        description = getString(R.string.sync_google_calendar_event_summary),
                        modifier = settingCardModifier,
                        imageResourceId = R.drawable.logo_google_calendar,
                    ) {
                        syncGoogleCalendar()
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
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
    private lateinit var mPermissionCallback: () -> Unit
    private var permissionContinuation: CancellableContinuation<Boolean>? = null

    private suspend fun launchPermissionGate(intent: Intent): Boolean =
        suspendCancellableCoroutine { continuation ->
            permissionContinuation = continuation

            // Launch intent
            googleAuthLauncher.launch(intent)

            // Handle cancellation
            continuation.invokeOnCancellation {
                permissionContinuation = null
            }
        }

    /**
     * Executes Google API tasks; if a permission error occurs, 
     * automatically displays a popup and retries upon approval.
     */
    suspend fun <T> runWithGmsAuth(
        block: () -> T,
    ): T? =
        withContext(Dispatchers.IO) {
            try {
                // 1. Attempt the original task
                block()
            } catch (e: UserRecoverableAuthIOException) {
                // 2. Permission error occurred! 
                // Go to the UI thread, show the permission popup, and wait for the result (suspends here)
                val isGranted =
                    withContext(Dispatchers.Main) {
                        launchPermissionGate(e.intent)
                    }

                // 3. If granted, retry the original task (not a recursive call, just executing the block)
                if (isGranted) {
                    block()
                } else {
                    requireContext().makeToast("Permission Denied")
                    null
                }
            }
        }

    private suspend fun requestDrivePermissions() {
        runWithGmsAuth {
            authManager.getEmail()?.let {
                val credential =
                    authManager.createGoogleAccountCredential(
                        it,
                        arrayListOf(DriveScopes.DRIVE_FILE),
                    )
                val googleDriveService: Drive =
                    Drive
                        .Builder(NetHttpTransport(), GsonFactory(), credential)
                        .setApplicationName(getString(R.string.app_name))
                        .build()
                // call test code for checking permissions
                googleDriveService
                    .files()
                    .list()
                    .setQ("'root' in parents and name = '${GDriveConstants.AAF_ROOT_FOLDER_NAME}' and trashed = false")
                    .setSpaces("drive")
                    .execute()
            }
        }
    }

    private fun backupDiaryRealm() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        val realmPath = EasyDiaryDbHelper.getRealmPath()
        lifecycleScope.launch {
            val finalResult =
                runCatching {
                    val googleAccount = authManager.getGoogleAccount()
                    requestDrivePermissions()
                    val driveServiceHelper = DriveServiceHelper(requireContext(), googleAccount)
                    val rootDriveId = driveServiceHelper.initDriveWorkingDirectory(GDriveConstants.AAF_EASY_DIARY_REALM_FOLDER_NAME)
                    driveServiceHelper
                        .createFile(
                            rootDriveId,
                            realmPath,
                            RealmConstants.DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"),
                            EasyDiaryUtils.easyDiaryMimeType,
                        )
                }

            finalResult
                .onSuccess {
                    progressContainer.visibility = View.GONE
                    requireActivity().makeSnackBar(getString(R.string.backup_completed_message))
                    requireActivity().config.diaryBackupGoogle = System.currentTimeMillis()
                    requireActivity().clearHoldOrientation()
                }.onFailure { e ->
                    requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                    progressContainer.visibility = View.GONE
                    requireActivity().clearHoldOrientation()
                }
        }
    }

    private fun recoverDiaryRealm() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        openRealmFilePickerDialog()
    }

    private fun openRealmFilePickerDialog() {
        lifecycleScope.launch {
            val googleAccount = authManager.getGoogleAccount()
            requestDrivePermissions()
            val driveServiceHelper = DriveServiceHelper(requireContext(), googleAccount)
//            driveServiceHelper.queryFiles("mimeType contains 'text/aaf_v' and name contains '$DIARY_DB_NAME'", 1000)
            driveServiceHelper
                .queryFiles(
                    "(mimeType = '${
                        EasyDiaryUtils.easyDiaryMimeTypeAll.joinToString(
                            "' or mimeType = '",
                        )
                    }') and trashed = false",
                    1000,
                ).addOnSuccessListener {
                    var alertDialog: AlertDialog? = null
                    val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                    it.files.map { file ->
                        val itemInfo =
                            hashMapOf<String, String>(
                                "name" to file.name,
                                "id" to file.id,
                                "createdTime" to file.createdTime.toString(),
                            )
                        realmFiles.add(itemInfo)
                    }
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setNegativeButton(getString(android.R.string.cancel)) { _, _ -> requireActivity().clearHoldOrientation() }
//                        builder.setMessage(getString(R.string.open_realm_file_message))
                    val inflater =
                        requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val fontView = inflater.inflate(R.layout.dialog_realm_files, null)
                    val listView = fontView.findViewById<ListView>(R.id.files)
                    val adapter =
                        RealmFileItemAdapter(
                            requireActivity(),
                            R.layout.item_realm_file,
                            realmFiles,
                        )
                    listView.adapter = adapter
                    listView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val itemInfo =
                                parent.adapter.getItem(position) as HashMap<String, String>
                            itemInfo["id"]?.let { realmFileId ->
                                progressContainer.visibility = View.VISIBLE
                                val realmPath = EasyDiaryDbHelper.getRealmPath()
                                EasyDiaryDbHelper.closeInstance()
                                driveServiceHelper.downloadFile(realmFileId, realmPath).run {
                                    addOnSuccessListener {
                                        requireActivity().refreshApp()
                                    }
                                    addOnFailureListener { }
                                }
                            }
                            alertDialog?.cancel()
                        }

                    alertDialog =
                        builder.create().apply {
                            requireActivity().updateAlertDialog(
                                this,
                                null,
                                fontView,
                                "${getString(R.string.open_realm_file_title)} (Total: ${it.files.size})",
                            )
                        }
                    progressContainer.visibility = View.GONE
                }.addOnFailureListener { e ->
                    e.printStackTrace()
                    requireActivity().makeSnackBar(e.message ?: "Please try again later.")
                    progressContainer.visibility = View.GONE
                }
        }
    }

    private fun recoverDiaryPhoto() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            val finalResult =
                runCatching {
                    authManager.getGoogleAccount()
                    requestDrivePermissions()
                    progressContainer.visibility = View.GONE
                    requireActivity().run {
                        showAlertDialog(
                            getString(R.string.recover_confirm_attached_photo),
                            { _, _ ->
                                val recoverPhotoService =
                                    Intent(this, RecoverPhotoService::class.java)
                                ContextCompat.startForegroundService(requireContext(), recoverPhotoService)
                                finish()
                            },
                            { _, _ -> clearHoldOrientation() },
                            DialogMode.INFO,
                            false,
                        )
                    }
                }

            finalResult.onFailure { e ->
                progressContainer.visibility = View.GONE
                requireActivity().clearHoldOrientation()
                requireActivity().makeSnackBar(e.message ?: "Please try again later.")
            }
        }
    }

    private fun backupDiaryPhoto() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            val finalResult =
                runCatching {
                    val googleAccount = authManager.getGoogleAccount()
                    requestDrivePermissions()
                    val driveServiceHelper = DriveServiceHelper(requireContext(), googleAccount)
                    val photoFolderId = driveServiceHelper.initDriveWorkingDirectory(GDriveConstants.AAF_EASY_DIARY_PHOTO_FOLDER_NAME)
                    progressContainer.visibility = View.GONE

                    // this call is not coroutine scope
                    // If photoFolderId is successfully verified, it can be executed regardless of the runCatching return
                    requireActivity().run {
                        showAlertDialog(
                            getString(R.string.backup_confirm_message),
                            { _, _ ->
                                val backupPhotoService =
                                    Intent(this, BackupPhotoService::class.java)
                                backupPhotoService.putExtra(
                                    GDriveConstants.WORKING_FOLDER_ID,
                                    photoFolderId,
                                )
                                ContextCompat.startForegroundService(
                                    requireContext(),
                                    backupPhotoService,
                                )
                                finish()
                            },
                            { _, _ -> clearHoldOrientation() },
                            DialogMode.INFO,
                            false,
                        )
                    }
                }

            finalResult.onFailure { e ->
                progressContainer.visibility = View.GONE
                requireActivity().clearHoldOrientation()
                requireActivity().makeSnackBar(e.message ?: "Please try again later.")
            }
        }
    }

    private suspend fun requestCalendarPermissionEventReadOnly() {
        runWithGmsAuth {
            authManager.getEmail()?.let {
                val credential: GoogleAccountCredential =
                    authManager.createGoogleAccountCredential(
                        it,
                        arrayListOf(CalendarScopes.CALENDAR_EVENTS_READONLY),
                    )
                val calendarService: Calendar =
                    Calendar
                        .Builder(NetHttpTransport(), GsonFactory(), credential)
                        .setApplicationName(getString(R.string.app_name))
                        .build()
                calendarService.calendarList().list().execute()
            }
        }
    }

    private suspend fun requestCalendarPermissionCalendarReadOnly() {
        runWithGmsAuth {
            authManager.getEmail()?.let {
                val credential: GoogleAccountCredential =
                    authManager.createGoogleAccountCredential(
                        it,
                        arrayListOf(CalendarScopes.CALENDAR_READONLY),
                    )
                val calendarService: Calendar =
                    Calendar
                        .Builder(NetHttpTransport(), GsonFactory(), credential)
                        .setApplicationName(getString(R.string.app_name))
                        .build()
                calendarService.calendarList().list().execute()
            }
        }
    }

    private fun getCurrentYearLastDatetime(): DateTime {
        val calendar =
            java.util.Calendar.getInstance(Locale.getDefault()).apply {
                set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
                set(java.util.Calendar.DATE, this.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    this.getActualMaximum(java.util.Calendar.HOUR_OF_DAY),
                )
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

    // TODO: fixme elegance
    private fun syncGoogleCalendar() {
        requireActivity().holdCurrentOrientation()
        progressContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            authManager.getGoogleAccount()
            requestCalendarPermissionCalendarReadOnly()
//            requestCalendarPermissionEventReadOnly()
            authManager.getCalendarCredential()?.let {
                val calendarService = authManager.getCalendarService(requireContext(), it)

                fun fetchData(
                    calendarId: String,
                    nextPageToken: String?,
                    total: Int = 0,
                ) {
                    var insertCount = 0
                    progressContainer.visibility = View.VISIBLE
                    mBinding.syncGoogleCalendarProgress.setProgressCompat(0, false)
                    mBinding.syncGoogleCalendar.visibility = View.VISIBLE

                    lifecycleScope.launch(Dispatchers.IO) {
                        val result =
                            if (nextPageToken == null) {
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
                        withContext(Dispatchers.Main) {
                            progressContainer.visibility = View.GONE
                        }
//                        val descriptions = arrayListOf<String>()
                        result.items.forEachIndexed { index, item ->
                            Log.i(
                                AAF_TEST,
                                "$index ${item.start?.date} ${item.summary} ${item.start?.dateTime}",
                            )
//                                descriptions.add(item.summary)
                            withContext(Dispatchers.Main) {
                                insertCount += authManager.calendarEventToDiary(item, calendarId)
                                mBinding.syncGoogleCalendarProgress.setProgressCompat(
                                    index
                                        .div(
                                            result.items.size.toFloat(),
                                        ).times(100)
                                        .toInt(),
                                    true,
                                )
                            }
                        }
                        if (result.nextPageToken != null) {
                            fetchData(calendarId, result.nextPageToken, total.plus(insertCount))
                        } else {
                            withContext(Dispatchers.Main) {
                                mBinding.syncGoogleCalendar.visibility = View.GONE
                                requireActivity().showAlertDialog(
                                    "${
                                        DateUtils.getDateTimeStringFromTimeMillis(
                                            mTimeMin.value,
                                        )
                                    } ~ ${DateUtils.getDateTimeStringFromTimeMillis(mTimeMax.value)} 기간에 등록된 ${
                                        total.plus(
                                            insertCount,
                                        )
                                    }건의 이벤트가 등록되었습니다.",
                                    null,
                                    null,
                                )
                            }
                        }
                    }
                }

                fun fetchCalendarList() {
                    var alertDialog: AlertDialog? = null
                    lifecycleScope.launch(Dispatchers.IO) {
                        val result = calendarService.calendarList().list().execute()
                        withContext(Dispatchers.Main) {
                            val builder = AlertDialog.Builder(requireActivity())
                            builder.setNegativeButton(getString(android.R.string.cancel), null)
                            val dialogSyncGoogleCalendarBinding =
                                DialogSyncGoogleCalendarBinding.inflate(layoutInflater)
                            val calendarInfo = ArrayList<Map<String, String>>()
                            result.items.forEach { calendar ->
                                calendarInfo.add(
                                    mapOf(
                                        "optionTitle" to calendar.summary,
                                        "optionValue" to calendar.id,
                                    ),
                                )
                            }
                            val optionItemAdapter =
                                OptionItemAdapter(
                                    requireActivity(),
                                    R.layout.item_check_label,
                                    calendarInfo,
                                    null,
                                    null,
                                    false,
                                )
                            dialogSyncGoogleCalendarBinding.run {
                                listView.adapter = optionItemAdapter
                                listView.setOnItemClickListener { parent, view, position, id ->
                                    calendarInfo[position]["optionValue"]?.let {
                                        fetchData(it, null)
                                        alertDialog?.dismiss()
                                    }
                                }

                                val fromCalendar =
                                    getCalendarInstance(false, java.util.Calendar.MONTH, -1)
                                val toCalendar =
                                    getCalendarInstance(true, java.util.Calendar.MONTH, 1)
                                mTimeMin = DateTime(fromCalendar.timeInMillis)
                                mTimeMax = DateTime(toCalendar.timeInMillis)

                                mSDatePickerDialog =
                                    DatePickerDialog(
                                        requireContext(),
                                        { _, year, month, dayOfMonth ->
                                            val startMillis =
                                                EasyDiaryUtils.datePickerToTimeMillis(
                                                    dayOfMonth,
                                                    month,
                                                    year,
                                                )
                                            mTimeMin = DateTime(startMillis)
                                            textSyncFromDate.text =
                                                DateUtils.getDateTimeStringFromTimeMillis(
                                                    startMillis,
                                                )
                                        },
                                        fromCalendar.get(java.util.Calendar.YEAR),
                                        fromCalendar.get(java.util.Calendar.MONTH),
                                        fromCalendar.get(java.util.Calendar.DAY_OF_MONTH),
                                    )

                                mEDatePickerDialog =
                                    DatePickerDialog(
                                        requireContext(),
                                        { _, year, month, dayOfMonth ->
                                            val endMillis =
                                                EasyDiaryUtils.datePickerToTimeMillis(
                                                    dayOfMonth,
                                                    month,
                                                    year,
                                                    true,
                                                )
                                            mTimeMax = DateTime(endMillis)
                                            textSyncToDate.text =
                                                DateUtils.getDateTimeStringFromTimeMillis(endMillis)
                                        },
                                        toCalendar.get(java.util.Calendar.YEAR),
                                        toCalendar.get(java.util.Calendar.MONTH),
                                        toCalendar.get(java.util.Calendar.DAY_OF_MONTH),
                                    )

                                textSyncFromDate.run {
                                    setOnClickListener { mSDatePickerDialog.show() }
                                    text =
                                        DateUtils.getDateTimeStringFromTimeMillis(fromCalendar.timeInMillis)
                                }
                                textSyncToDate.run {
                                    setOnClickListener { mEDatePickerDialog.show() }
                                    text =
                                        DateUtils.getDateTimeStringFromTimeMillis(toCalendar.timeInMillis)
                                }

                                requireActivity().run {
                                    FontUtils.setFontsTypeface(
                                        requireContext(),
                                        null,
                                        cardSyncOptions,
                                    )
                                    initTextSize(cardSyncOptions)
                                    updateTextColors(cardSyncOptions)
                                    updateAppViews(dialogSyncGoogleCalendarBinding.root)
                                    updateCardViewPolicy(dialogSyncGoogleCalendarBinding.root)
                                }
                            }
                            requireActivity().clearHoldOrientation()
                            progressContainer.visibility = View.GONE
                            alertDialog =
                                builder.create().apply {
                                    requireActivity().updateAlertDialogWithIcon(
                                        DialogMode.INFO,
                                        this,
                                        null,
                                        dialogSyncGoogleCalendarBinding.root,
                                        "Sync Google Calendar",
                                    )
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

    private fun initPreference() {
        determineAccountInfo()
    }

    fun determineAccountInfo() {
        when (authManager.isLoggedInLocal()) {
            true -> {
                mSettingsViewModel.run {
                    setInformationTitle(getString(R.string.google_drive_account_information_title))
                    val sb = StringBuilder()
                    sb.append(authManager.getDisplayName() + System.lineSeparator())
                    sb.append(authManager.getEmail())
                    authManager.getProfileUri()?.let { setProfileImageUrl(it.toUri()) }
                    setAccountInfo(sb.toString())
                }
            }

            false -> {
                mSettingsViewModel.run {
                    setInformationTitle(getString(R.string.google_drive_account_sign_in_title))
                    setProfileImageUrl(null)
                    setAccountInfo(getString(R.string.google_drive_account_sign_in_description))
                }
            }
        }
    }
}
