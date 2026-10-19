package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.adapters.SimpleCheckboxAdapter
import me.blog.korn123.easydiary.databinding.FragmentSettingsBackupLocalBinding
import me.blog.korn123.easydiary.databinding.PopupLocationSelectorBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.UriFileType
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.classifyUriByExtension
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.confirmExternalStoragePermission
import me.blog.korn123.easydiary.extensions.exportRoomData
import me.blog.korn123.easydiary.extensions.exportRoomDataWithSAF
import me.blog.korn123.easydiary.extensions.importRoomData
import me.blog.korn123.easydiary.extensions.importRoomDataWithSAF
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.refreshApp
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.BACKUP_DB_DIRECTORY
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.MIME_TYPE_XLS
import me.blog.korn123.easydiary.helper.MIME_TYPE_ZIP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_READ_ROOM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_READ_ZIP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_ROOM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_XLS
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_ZIP
import me.blog.korn123.easydiary.helper.RoomConstants
import me.blog.korn123.easydiary.helper.SettingLocalConstants
import me.blog.korn123.easydiary.helper.WorkerConstants
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.DiaryViewModel
import me.blog.korn123.easydiary.workers.BackupOperations
import java.io.File
import java.util.Date

@AndroidEntryPoint
class SettingsLocalBackupFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsBackupLocalBinding
    private lateinit var mRequestExternalStoragePermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mRequestWriteFileWithSAF: ActivityResultLauncher<Intent>
    private lateinit var mRequestReadFileWithSAF: ActivityResultLauncher<Intent>
    private var mTaskFlag = 0
    private val diaryViewModel: DiaryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRequestReadFileWithSAF =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requireActivity().run {
                    pauseLock()
                    if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                        when (mTaskFlag) {
                            REQUEST_CODE_SAF_READ_ZIP -> {
                                it.data?.data?.let { uri ->
                                    importFullBackupFile(uri)
                                }
                            }

                            REQUEST_CODE_SAF_READ_ROOM -> {
                                it.data?.data?.let { uri ->
                                    when (classifyUriByExtension(uri.toString())) {
                                        UriFileType.ROOM -> {
                                            lifecycleScope.launch {
                                                importRoomDataWithSAF(
                                                    uri = uri,
                                                    isMergeData = false,
                                                    successCallback = { message ->
                                                        makeSnackBar(message)
                                                        refreshApp()
                                                    },
                                                    failCallback = { message ->
                                                        makeSnackBar(message)
                                                    },
                                                )
                                            }
                                        }

                                        UriFileType.UNKNOWN -> {
                                            makeSnackBar("Unknown file type.")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        mRequestWriteFileWithSAF =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requireActivity().run {
                    pauseLock()
                    if (it.resultCode == Activity.RESULT_OK && it.data != null &&
                        checkPermission(
                            EXTERNAL_STORAGE_PERMISSIONS,
                        )
                    ) {
                        when (mTaskFlag) {
                            REQUEST_CODE_SAF_WRITE_ZIP -> {
                                it.data?.data?.let { uri ->
                                    exportFullBackupFile(uri)
                                }
                            }

                            REQUEST_CODE_SAF_WRITE_ROOM -> {
                                it.data?.data?.let { uri ->
                                    lifecycleScope.launch {
                                        exportRoomDataWithSAF(uri)
                                        makeSnackBar("Operation completed.")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        mRequestExternalStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                requireActivity().run {
                    pauseLock()
                    if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                        when (mTaskFlag) {
                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM -> {
                                showLocationSelectionPopup(
                                    SettingLocalConstants.MODE_BACKUP,
                                    getString(R.string.backup_internal_title),
                                    getString(R.string.backup_internal_description),
                                    getString(R.string.backup_external_title),
                                    getString(R.string.backup_external_description),
                                )
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM -> {
                                showLocationSelectionPopup(
                                    SettingLocalConstants.MODE_RECOVERY,
                                    getString(R.string.recovery_internal_title),
                                    getString(R.string.recovery_internal_description),
                                    getString(R.string.recovery_external_title),
                                    getString(R.string.recovery_external_description),
                                )
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM -> {
                                deleteRealmFile()
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP -> {
                                setupLauncher(REQUEST_CODE_SAF_WRITE_ZIP) {
                                    EasyDiaryUtils.writeFileWithSAF(
                                        DateUtils.getCurrentDateTime(
                                            DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH,
                                        ) + ".zip",
                                        MIME_TYPE_ZIP,
                                        mRequestWriteFileWithSAF,
                                    )
                                }
                            }
                        }
                    } else {
                        makeSnackBar(
                            requireActivity().findViewById(android.R.id.content),
                            getString(R.string.guide_message_3),
                        )
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentSettingsBackupLocalBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        updateFragmentUI(mBinding.root)

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

                    SimpleCard(
                        title = getString(R.string.export_realm_title),
                        description = getString(R.string.export_realm_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                showLocationSelectionPopup(
                                    SettingLocalConstants.MODE_BACKUP,
                                    getString(R.string.backup_internal_title),
                                    getString(R.string.backup_internal_description),
                                    getString(R.string.backup_external_title),
                                    getString(R.string.backup_external_description),
                                )
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM) {
                                    requireActivity().confirmExternalStoragePermission(
                                        EXTERNAL_STORAGE_PERMISSIONS,
                                        mRequestExternalStoragePermissionLauncher,
                                    )
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.import_realm_title),
                        description = getString(R.string.import_realm_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                showLocationSelectionPopup(
                                    SettingLocalConstants.MODE_RECOVERY,
                                    getString(R.string.recovery_internal_title),
                                    getString(R.string.recovery_internal_description),
                                    getString(R.string.recovery_external_title),
                                    getString(R.string.recovery_external_description),
                                )
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM) {
                                    requireActivity().confirmExternalStoragePermission(
                                        EXTERNAL_STORAGE_PERMISSIONS,
                                        mRequestExternalStoragePermissionLauncher,
                                    )
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.delete_realm_title),
                        description = getString(R.string.delete_realm_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                deleteRealmFile()
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM) {
                                    requireActivity().confirmExternalStoragePermission(
                                        EXTERNAL_STORAGE_PERMISSIONS,
                                        mRequestExternalStoragePermissionLauncher,
                                    )
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.export_full_backup_title),
                        description = getString(R.string.export_full_backup_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                setupLauncher(REQUEST_CODE_SAF_WRITE_ZIP) {
                                    EasyDiaryUtils.writeFileWithSAF(
                                        DateUtils.getCurrentDateTime(
                                            DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH,
                                        ) + ".zip",
                                        MIME_TYPE_ZIP,
                                        mRequestWriteFileWithSAF,
                                    )
                                }
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP) {
                                    requireActivity().confirmExternalStoragePermission(
                                        EXTERNAL_STORAGE_PERMISSIONS,
                                        mRequestExternalStoragePermissionLauncher,
                                    )
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.import_full_backup_title),
                        description = getString(R.string.import_full_backup_description),
                        modifier = settingCardModifier,
                    ) {
                        setupLauncher(REQUEST_CODE_SAF_READ_ZIP) {
                            EasyDiaryUtils.readFileWithSAF(MIME_TYPE_ZIP, mRequestReadFileWithSAF)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
    }

    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
    private suspend fun exportRoomData() {
        requireActivity().run {
            exportRoomData(updateLocalBackupTime = true)
            makeSnackBar("Operation completed.")
        }
    }

    private fun importRoomFile() {
        val files =
            File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    var alertDialog: AlertDialog? = null
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setNegativeButton(getString(android.R.string.cancel), null)
//                    builder.setMessage(getString(R.string.open_realm_file_message))

                    val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                    it.sortDescending()
                    it.forEach { file ->
                        val itemInfo =
                            hashMapOf<String, String>(
                                "name" to file.name,
                                "createdTime" to Date(file.lastModified()).toString(),
                            )
                        realmFiles.add(itemInfo)
                    }

                    val inflater =
                        requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_realm_files, null)
                    val listView = rootView.findViewById<ListView>(R.id.files)
                    val adapter =
                        RealmFileItemAdapter(
                            requireActivity(),
                            R.layout.item_realm_file,
                            realmFiles,
                        )
                    listView.adapter = adapter
                    listView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, _, position, _ ->
                            val itemInfo =
                                parent.adapter.getItem(position) as HashMap<*, *>

                            // check file name
                            when (classifyUriByExtension((itemInfo["name"] ?: "") as String)) {
                                UriFileType.ROOM -> {
                                    lifecycleScope.launch {
                                        val srcFile =
                                            File(
                                                EasyDiaryUtils.getApplicationDataDirectory(
                                                    requireActivity(),
                                                ) + BACKUP_DB_DIRECTORY + (itemInfo["name"] ?: ""),
                                            )
                                        requireActivity().run {
                                            lifecycleScope.launch {
                                                importRoomData(
                                                    srcFile,
                                                    false,
                                                    isMergeData = false,
                                                    successCallback = { message ->
                                                        makeSnackBar(message)
                                                        requireActivity().refreshApp()
                                                    },
                                                    failCallback = { message ->
                                                        makeSnackBar(message)
                                                    },
                                                )
                                            }
                                        }
                                        alertDialog?.cancel()
                                    }
                                }

                                UriFileType.UNKNOWN -> {
                                    alertDialog?.cancel()
                                    requireActivity().makeSnackBar("Unknown file type.")
                                }
                            }
                        }

                    alertDialog =
                        builder.create().apply {
                            requireActivity().updateAlertDialogWithIcon(
                                DialogMode.SETTING,
                                this,
                                null,
                                rootView,
                                "${getString(R.string.open_realm_file_title)} (Total: ${it.size})",
                            )
                        }
                }

                false -> {}
            }
        }
    }

    private fun deleteRealmFile() {
        val files =
            File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val realmInfoList: ArrayList<SimpleCheckboxAdapter.SimpleCheckbox> =
                        arrayListOf()
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setCancelable(false)
                    builder.setPositiveButton(getString(R.string.delete)) { _, _ -> }
                    builder.setNegativeButton(getString(android.R.string.cancel), null)

                    it.sortDescending()
                    it.forEach { file ->
                        realmInfoList.add(
                            SimpleCheckboxAdapter.SimpleCheckbox(
                                file.name,
                                Date(file.lastModified()).toString(),
                            ),
                        )
                    }

                    val inflater =
                        requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_delete_realm_files, null)
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.files)

//                    val spacesItemDecoration = PostCardViewerActivity.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding))
                    val gridLayoutManager =
                        androidx.recyclerview.widget.GridLayoutManager(requireActivity(), 1)

                    recyclerView.apply {
                        adapter = SimpleCheckboxAdapter(requireActivity(), realmInfoList)
                        layoutManager = gridLayoutManager
//                        addItemDecoration(spacesItemDecoration)
                    }

                    builder.create().apply {
                        requireActivity().updateAlertDialog(
                            this,
                            null,
                            rootView,
                            "${getString(R.string.delete_realm_title)} (Total: ${it.size})",
                        )
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val checkedList = mutableListOf<String>()
                            realmInfoList.forEach { item ->
                                if (item.isChecked) checkedList.add(item.title)
                            }
                            requireActivity().showAlertDialog(
                                getString(R.string.delete_confirm),
                                { _, _ ->
                                    checkedList.map { filename ->
                                        File(
                                            EasyDiaryUtils.getApplicationDataDirectory(
                                                requireActivity(),
                                            ) + BACKUP_DB_DIRECTORY + filename,
                                        ).delete()
                                    }
                                    this.dismiss()
                                },
                                { _, _ -> },
                                DialogMode.WARNING,
                                true,
                                getString(R.string.delete),
                                getString(R.string.delete),
                            )
                        }
                    }
                }

                false -> {}
            }
        }
    }

    private fun createExportExcelUri() {
        setupLauncher(REQUEST_CODE_SAF_WRITE_XLS) {
            EasyDiaryUtils.writeFileWithSAF(
                DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH) + ".xls",
                MIME_TYPE_XLS,
                mRequestWriteFileWithSAF,
            )
        }
    }

    private fun exportFullBackupFile(uri: Uri?) {
        lifecycleScope.launch {
            exportRoomData()
            BackupOperations
                .Builder(
                    requireActivity(),
                    uri.toString(),
                    WorkerConstants.WORK_MODE_BACKUP,
                ).build()
                .apply {
                    continuation.enqueue()
                }
        }
    }

    private fun importFullBackupFile(uri: Uri?) {
        BackupOperations
            .Builder(
                requireActivity(),
                uri.toString(),
                WorkerConstants.WORK_MODE_RECOVERY,
            ).build()
            .apply {
                continuation.enqueue()
            }
    }

    private fun setupLauncher(
        taskFlag: Int,
        callback: () -> Unit,
    ) {
        mTaskFlag = taskFlag
        callback()
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun showLocationSelectionPopup(
        popupMode: Int,
        internalTitle: String,
        internalDescription: String,
        externalTitle: String,
        externalDescription: String,
    ) {
        var dialog: AlertDialog? = null
        val builder =
            AlertDialog.Builder(requireActivity()).apply {
                setNegativeButton(getString(android.R.string.cancel), null)
            }

        val popupView =
            PopupLocationSelectorBinding.inflate(layoutInflater).apply {
                modeInternalTitle.text = internalTitle
                modeInternalDescription.text = internalDescription
                modeExternalTitle.text = externalTitle
                modeExternalDescription.text = externalDescription

                root.setBackgroundColor(requireActivity().config.backgroundColor)
                modeInternal.setOnClickListener {
                    lifecycleScope.launch {
                        when (popupMode) {
                            SettingLocalConstants.MODE_BACKUP -> {
                                exportRoomData()
                            }

                            SettingLocalConstants.MODE_RECOVERY -> {
                                importRoomFile()
                            }
                        }
                        dialog?.dismiss()
                    }
                }
                modeExternal.setOnClickListener {
                    when (popupMode) {
                        SettingLocalConstants.MODE_BACKUP -> {
                            setupLauncher(REQUEST_CODE_SAF_WRITE_ROOM) {
                                EasyDiaryUtils.writeFileWithSAF(
                                    RoomConstants.DIARY_DB_NAME + "_" +
                                        DateUtils.getCurrentDateTime(
                                            "yyyyMMdd_HHmmss",
                                        ),
                                    MIME_TYPE_ZIP,
                                    mRequestWriteFileWithSAF,
                                )
                            }
                        }

                        SettingLocalConstants.MODE_RECOVERY -> {
                            setupLauncher(REQUEST_CODE_SAF_READ_ROOM) {
                                EasyDiaryUtils.readFileWithSAF(
                                    arrayOf(MIME_TYPE_ZIP),
                                    mRequestReadFileWithSAF,
                                )
                            }
                        }
                    }
                    dialog?.dismiss()
                }
            }
        requireActivity().run {
            updateDrawableColorInnerCardView(R.drawable.ic_delete)
            updateTextColors(popupView.root)
            initTextSize(popupView.root)
        }

        FontUtils.setFontsTypeface(requireActivity(), null, popupView.root, true)
        builder.setView(popupView.root)
        dialog =
            builder.create().apply {
                requireActivity().updateAlertDialogWithIcon(
                    DialogMode.SETTING,
                    this,
                    null,
                    popupView.root,
                )
            }
    }
}
