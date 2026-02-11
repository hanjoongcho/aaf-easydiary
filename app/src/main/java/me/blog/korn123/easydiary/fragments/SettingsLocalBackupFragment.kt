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
import android.widget.TextView
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.adapters.SimpleCheckboxAdapter
import me.blog.korn123.easydiary.databinding.FragmentSettingsBackupLocalBinding
import me.blog.korn123.easydiary.databinding.PopupLocationSelectorBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.confirmExternalStoragePermission
import me.blog.korn123.easydiary.extensions.exportRealmFile
import me.blog.korn123.easydiary.extensions.getUriForFile
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
import me.blog.korn123.easydiary.helper.BACKUP_EXCEL_DIRECTORY
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.MIME_TYPE_REALM
import me.blog.korn123.easydiary.helper.MIME_TYPE_XLS
import me.blog.korn123.easydiary.helper.MIME_TYPE_ZIP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_READ_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_READ_ZIP
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_REALM
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_XLS
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SAF_WRITE_ZIP
import me.blog.korn123.easydiary.helper.RealmConstants
import me.blog.korn123.easydiary.helper.SettingLocalConstants
import me.blog.korn123.easydiary.helper.WorkerConstants
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel
import me.blog.korn123.easydiary.workers.BackupOperations
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date

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
    private val mSettingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRequestReadFileWithSAF =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requireActivity().run {
                    pauseLock()
                    if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                        when (mTaskFlag) {
                            REQUEST_CODE_SAF_READ_ZIP -> importFullBackupFile(it.data!!.data)
                            REQUEST_CODE_SAF_READ_REALM -> importRealmFileWithSAF(it.data!!.data)
                        }
                    }
                }
            }

        mRequestWriteFileWithSAF =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requireActivity().run {
                    pauseLock()
                    if (it.resultCode == Activity.RESULT_OK && it.data != null && checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                        when (mTaskFlag) {
                            REQUEST_CODE_SAF_WRITE_ZIP -> exportFullBackupFile(it.data!!.data)
                            REQUEST_CODE_SAF_WRITE_XLS -> exportExcel(it.data!!.data)
                            REQUEST_CODE_SAF_WRITE_REALM -> exportRealmFileWithSAF(it.data!!.data)
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
                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL -> {
                                createExportExcelUri()
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM -> {
                                showLocationSelectionPopup(SettingLocalConstants.MODE_BACKUP, getString(R.string.backup_internal_title), getString(R.string.backup_internal_description), getString(R.string.backup_external_title), getString(R.string.backup_external_description))
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM -> {
                                showLocationSelectionPopup(SettingLocalConstants.MODE_RECOVERY, getString(R.string.recovery_internal_title), getString(R.string.recovery_internal_description), getString(R.string.recovery_external_title), getString(R.string.recovery_external_description))
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM -> {
                                deleteRealmFile()
                            }

                            REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP -> {
                                setupLauncher(REQUEST_CODE_SAF_WRITE_ZIP) {
                                    EasyDiaryUtils.writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH) + ".zip", MIME_TYPE_ZIP, mRequestWriteFileWithSAF)
                                }
                            }
                        }
                    } else {
                        makeSnackBar(requireActivity().findViewById(android.R.id.content), getString(R.string.guide_message_3))
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentSettingsBackupLocalBinding.inflate(layoutInflater)
        return mBinding.root
    }

    @OptIn(ExperimentalLayoutApi::class)
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

                    SimpleCard(
                        title = getString(R.string.export_realm_title),
                        description = getString(R.string.export_realm_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                showLocationSelectionPopup(SettingLocalConstants.MODE_BACKUP, getString(R.string.backup_internal_title), getString(R.string.backup_internal_description), getString(R.string.backup_external_title), getString(R.string.backup_external_description))
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM) {
                                    requireActivity().confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, mRequestExternalStoragePermissionLauncher)
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
                                showLocationSelectionPopup(SettingLocalConstants.MODE_RECOVERY, getString(R.string.recovery_internal_title), getString(R.string.recovery_internal_description), getString(R.string.recovery_external_title), getString(R.string.recovery_external_description))
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM) {
                                    requireActivity().confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, mRequestExternalStoragePermissionLauncher)
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
                                    requireActivity().confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, mRequestExternalStoragePermissionLauncher)
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.export_excel_title),
                        description = getString(R.string.export_excel_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                createExportExcelUri()
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL) {
                                    requireActivity().confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, mRequestExternalStoragePermissionLauncher)
                                }
                            }
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.send_email_attach_excel_title),
                        description = getString(R.string.send_email_attach_excel_description),
                        modifier = settingCardModifier,
                    ) {
                        sendEmailWithExcel()
                    }

                    SimpleCard(
                        title = getString(R.string.export_full_backup_title),
                        description = getString(R.string.export_full_backup_description),
                        modifier = settingCardModifier,
                    ) {
                        when (requireActivity().checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                            true -> {
                                setupLauncher(REQUEST_CODE_SAF_WRITE_ZIP) {
                                    EasyDiaryUtils.writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH) + ".zip", MIME_TYPE_ZIP, mRequestWriteFileWithSAF)
                                }
                            }

                            false -> {
                                setupLauncher(REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP) {
                                    requireActivity().confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, mRequestExternalStoragePermissionLauncher)
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
    private fun exportRealmFile(showDialog: Boolean = true) {
        requireActivity().exportRealmFile()
        requireActivity().makeSnackBar("Operation completed.")
    }

    private fun exportRealmFileWithSAF(uri: Uri?) {
        uri?.let {
            val os = requireActivity().contentResolver.openOutputStream(it)
            val `is` = FileInputStream(EasyDiaryDbHelper.getRealmPath())
            IOUtils.copy(`is`, os)
            os?.close()
            `is`.close()
            requireActivity().makeSnackBar("Operation completed.")
        }
    }

    private fun importRealmFile() {
        val files = File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    var alertDialog: AlertDialog? = null
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setNegativeButton(getString(android.R.string.cancel), null)
//                    builder.setMessage(getString(R.string.open_realm_file_message))

                    val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                    it.sortDescending()
                    it.map { file ->
                        val itemInfo = hashMapOf<String, String>("name" to file.name, "createdTime" to Date(file.lastModified()).toString())
                        realmFiles.add(itemInfo)
                    }

                    val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_realm_files, null)
                    val listView = rootView.findViewById<ListView>(R.id.files)
                    val adapter = RealmFileItemAdapter(requireActivity(), R.layout.item_realm_file, realmFiles)
                    listView.adapter = adapter
                    listView.onItemClickListener =
                        AdapterView.OnItemClickListener { parent, view, position, id ->
                            val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                            val srcFile = File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_DB_DIRECTORY + itemInfo["name"])
                            val destFile = File(EasyDiaryDbHelper.getRealmPath())
                            EasyDiaryDbHelper.closeInstance()
                            FileUtils.copyFile(srcFile, destFile)
                            requireActivity().refreshApp()
                            alertDialog?.cancel()
                        }

                    alertDialog =
                        builder.create().apply {
                            requireActivity().updateAlertDialogWithIcon(DialogMode.SETTING, this, null, rootView, "${getString(R.string.open_realm_file_title)} (Total: ${it.size})")
                        }
                }

                false -> {}
            }
        }
    }

    private fun importRealmFileWithSAF(uri: Uri?) {
        uri?.let {
            val inputStream = requireActivity().contentResolver.openInputStream(it)
            val outputStream = FileOutputStream(File(EasyDiaryDbHelper.getRealmPath()))
            EasyDiaryDbHelper.closeInstance()
            IOUtils.copy(inputStream, outputStream)
            inputStream?.close()
            outputStream.close()
            requireActivity().refreshApp()
        }
    }

    private fun deleteRealmFile() {
        val files = File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val realmInfoList: ArrayList<SimpleCheckboxAdapter.SimpleCheckbox> = arrayListOf()
                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setCancelable(false)
                    builder.setPositiveButton(getString(R.string.delete)) { _, _ -> }
                    builder.setNegativeButton(getString(android.R.string.cancel), null)

                    it.sortDescending()
                    it.map { file ->
                        realmInfoList.add(SimpleCheckboxAdapter.SimpleCheckbox(file.name, Date(file.lastModified()).toString()))
                    }

                    val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_delete_realm_files, null)
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.files)

//                    val spacesItemDecoration = PostCardViewerActivity.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding))
                    val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(requireActivity(), 1)

                    recyclerView.apply {
                        adapter = SimpleCheckboxAdapter(requireActivity(), realmInfoList)
                        layoutManager = gridLayoutManager
//                        addItemDecoration(spacesItemDecoration)
                    }

                    builder.create().apply {
                        requireActivity().updateAlertDialog(this, null, rootView, "${getString(R.string.delete_realm_title)} (Total: ${it.size})")
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

    private fun sendEmailWithExcel() {
        val exportFileName = "aaf-easydiray_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}"
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.export_excel_title))
        builder.setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_excel_3))
        builder.setCancelable(false)
//        builder.setPositiveButton(getString(R.string.ok), null)
        val alert = builder.create()
        val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_export_progress_excel, null)
        val progressInfo = containerView.findViewById<TextView>(R.id.progressInfo)
        alert.setView(containerView)
        alert.show()

        lifecycleScope.launch(Dispatchers.IO) {
            val workBook = createWorkBook(progressInfo, "Create excel file...")
            val outputStream = FileOutputStream("${EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_EXCEL_DIRECTORY + exportFileName}.xls")
            workBook.write(outputStream)
            outputStream.close()
            withContext(Dispatchers.Main) { alert.cancel() }

            val destFile = File(File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + BACKUP_EXCEL_DIRECTORY), "$exportFileName.xls")
            // test code for attach file to email
            val emailIntent: Intent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "text/plain"
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("email@example.com"))
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here")
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "body text")
            emailIntent.putExtra(Intent.EXTRA_STREAM, requireActivity().getUriForFile(destFile))
            startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
        }
    }

    private fun createExportExcelUri() {
        setupLauncher(REQUEST_CODE_SAF_WRITE_XLS) {
            EasyDiaryUtils.writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH) + ".xls", MIME_TYPE_XLS, mRequestWriteFileWithSAF)
        }
    }

    private fun exportExcel(uri: Uri?) {
//        EasyDiaryUtils.initLegacyWorkingDirectory(mActivity)
        val exportFileName = "aaf-easydiray_${DateUtils.getCurrentDateTime(DateUtilConstants.DATE_TIME_PATTERN_WITHOUT_DASH)}"
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.export_excel_title))
        builder.setIcon(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_excel_3))
        builder.setCancelable(false)
//        builder.setPositiveButton(getString(R.string.ok), null)
        val alert = builder.create()
        val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_export_progress_excel, null)
        val progressInfo = containerView.findViewById<TextView>(R.id.progressInfo)
        val confirmButton = containerView.findViewById<TextView>(R.id.confirm)
        progressInfo.text = "Preparing to export..."
        alert.setView(containerView)
        alert.show()

        lifecycleScope.launch(Dispatchers.IO) {
            val workBook = createWorkBook(progressInfo, "$exportFileName.xls")
//            val outputStream = FileOutputStream("${EasyDiaryUtils.getExternalStorageDirectory().absolutePath + BACKUP_EXCEL_DIRECTORY + exportFileName}.xls")
            val outputStream = requireActivity().contentResolver.openOutputStream(uri!!)
            workBook.write(outputStream)
            outputStream?.close()
            withContext(Dispatchers.Main) {
                confirmButton.visibility = View.VISIBLE
                confirmButton.setOnClickListener { alert.cancel() }
            }
        }
    }

    private fun createWorkBook(
        infoView: TextView? = null,
        guideMessage: String? = "",
    ): Workbook {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val diaryList = EasyDiaryDbHelper.findDiary(null, false, 0, 0, 0, realmInstance)
        val wb: Workbook = HSSFWorkbook()
        val sheet = wb.createSheet("new sheet")

        val headerFont =
            wb.createFont().apply {
                color = IndexedColors.WHITE.index
            }
        val headerStyle =
            wb.createCellStyle().apply {
                wrapText = true
                fillForegroundColor = IndexedColors.BLUE.index
                fillPattern = CellStyle.SOLID_FOREGROUND
                alignment = CellStyle.ALIGN_CENTER
                verticalAlignment = CellStyle.VERTICAL_CENTER
                setFont(headerFont)
            }
        val bodyStyle =
            wb.createCellStyle().apply {
                wrapText = true
                verticalAlignment = CellStyle.VERTICAL_TOP
            }

        val headerRow = sheet.createRow(0)
        headerRow.height = (256 * 3).toShort()
        headerRow.createCell(SettingLocalConstants.SEQ).setCellValue(getString(R.string.export_excel_header_seq))
        headerRow.createCell(SettingLocalConstants.WRITE_DATE).setCellValue(getString(R.string.export_excel_header_write_date))
        headerRow.createCell(SettingLocalConstants.TITLE).setCellValue(getString(R.string.export_excel_header_title))
        headerRow.createCell(SettingLocalConstants.CONTENTS).setCellValue(getString(R.string.export_excel_header_contents))
        headerRow.createCell(SettingLocalConstants.ATTACH_PHOTO_NAME).setCellValue(getString(R.string.export_excel_header_attach_photo_path))
        headerRow.createCell(SettingLocalConstants.ATTACH_PHOTO_SIZE).setCellValue(getString(R.string.export_excel_header_attach_photo_size))
        headerRow.createCell(SettingLocalConstants.WRITE_TIME_MILLIS).setCellValue(getString(R.string.export_excel_header_write_time_millis))
        headerRow.createCell(SettingLocalConstants.SYMBOL).setCellValue(getString(R.string.export_excel_header_symbol))
        headerRow.createCell(SettingLocalConstants.IS_ALL_DAY).setCellValue(getString(R.string.export_excel_header_is_all_day))

        headerRow.getCell(SettingLocalConstants.SEQ).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.WRITE_DATE).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.TITLE).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.CONTENTS).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.ATTACH_PHOTO_NAME).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.ATTACH_PHOTO_SIZE).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.WRITE_TIME_MILLIS).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.SYMBOL).cellStyle = headerStyle
        headerRow.getCell(SettingLocalConstants.IS_ALL_DAY).cellStyle = headerStyle

        // FIXME:
        // https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html#setColumnWidth-int-int-
        sheet.setColumnWidth(SettingLocalConstants.SEQ, 256 * 10)
        sheet.setColumnWidth(SettingLocalConstants.WRITE_DATE, 256 * 30)
        sheet.setColumnWidth(SettingLocalConstants.TITLE, 256 * 30)
        sheet.setColumnWidth(SettingLocalConstants.CONTENTS, 256 * 50)
        sheet.setColumnWidth(SettingLocalConstants.ATTACH_PHOTO_NAME, 256 * 80)
        sheet.setColumnWidth(SettingLocalConstants.ATTACH_PHOTO_SIZE, 256 * 15)
        sheet.setColumnWidth(SettingLocalConstants.WRITE_TIME_MILLIS, 256 * 60)
        sheet.setColumnWidth(SettingLocalConstants.SYMBOL, 256 * 10)
        sheet.setColumnWidth(SettingLocalConstants.IS_ALL_DAY, 256 * 30)
        val diarySymbolMap = FlavorUtils.getDiarySymbolMap(requireActivity())
        val size = diaryList.size
        diaryList.forEachIndexed { index, diaryDto ->
            val row = sheet.createRow(index + 1)
            val photoNames = StringBuffer()
            val photoSizes = StringBuffer()
            diaryDto.photoUris?.map {
                photoNames.append("$DIARY_PHOTO_DIRECTORY${FilenameUtils.getName(it.getFilePath())}\n")
                photoSizes.append("${File(it.getFilePath()).length() / 1024}\n")
            }

            val sequence = row.createCell(SettingLocalConstants.SEQ).apply { cellStyle = bodyStyle }
            val writeDate = row.createCell(SettingLocalConstants.WRITE_DATE).apply { cellStyle = bodyStyle }
            val title = row.createCell(SettingLocalConstants.TITLE).apply { cellStyle = bodyStyle }
            val contents = row.createCell(SettingLocalConstants.CONTENTS).apply { cellStyle = bodyStyle }
            val attachPhotoNames = row.createCell(SettingLocalConstants.ATTACH_PHOTO_NAME).apply { cellStyle = bodyStyle }
            val attachPhotoSizes = row.createCell(SettingLocalConstants.ATTACH_PHOTO_SIZE).apply { cellStyle = bodyStyle }
            val writeTimeMillis = row.createCell(SettingLocalConstants.WRITE_TIME_MILLIS).apply { cellStyle = bodyStyle }
            val weather = row.createCell(SettingLocalConstants.SYMBOL).apply { cellStyle = bodyStyle }
            val isAllDay = row.createCell(SettingLocalConstants.IS_ALL_DAY).apply { cellStyle = bodyStyle }

            sequence.setCellValue(diaryDto.sequence.toDouble())
            writeDate.setCellValue(DateUtils.getDateTimeStringFromTimeMillis(diaryDto.currentTimeMillis))
            title.setCellValue(diaryDto.title)
            contents.setCellValue(diaryDto.contents)
            attachPhotoNames.setCellValue(photoNames.toString())
            attachPhotoSizes.setCellValue(photoSizes.toString())
            writeTimeMillis.setCellValue(diaryDto.currentTimeMillis.toDouble())
            isAllDay.setCellValue(diaryDto.isAllDay)
            weather.setCellValue(diarySymbolMap[diaryDto.weather])

            requireActivity().runOnUiThread {
                infoView?.run {
                    text = "${index.plus(1)} / $size\n$guideMessage"
                }
            }
        }
        realmInstance.close()
        return wb
    }

//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    private fun writeFileWithSAF(fileName: String, mimeType: String, activityResultLauncher: ActivityResultLauncher<Intent>) {
//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//            // Filter to only show results that can be "opened", such as
//            // a file (as opposed to a list of contacts or timezones).
//            addCategory(Intent.CATEGORY_OPENABLE)
//
//            type = mimeType
//            // Create a file with the requested MIME type.
//            putExtra(Intent.EXTRA_TITLE, fileName)
//        }
//        activityResultLauncher.launch(intent)
//    }

//    private fun readFileWithSAF(mimeType: String, activityResultLauncher: ActivityResultLauncher<Intent>) {
//        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//            type = mimeType
//        }
//        activityResultLauncher.launch(intent)
//    }

    private fun exportFullBackupFile(uri: Uri?) {
        exportRealmFile(false)
        BackupOperations.Builder(requireActivity(), uri.toString(), WorkerConstants.WORK_MODE_BACKUP).build().apply {
            continuation.enqueue()
        }
    }

    private fun importFullBackupFile(uri: Uri?) {
        BackupOperations.Builder(requireActivity(), uri.toString(), WorkerConstants.WORK_MODE_RECOVERY).build().apply {
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
//            closePopup.setOnClickListener { dialog?.dismiss() }
                modeInternal.setOnClickListener {
                    when (popupMode) {
                        SettingLocalConstants.MODE_BACKUP -> exportRealmFile()
                        SettingLocalConstants.MODE_RECOVERY -> importRealmFile()
                    }
                    dialog?.dismiss()
                }
                modeExternal.setOnClickListener {
                    when (popupMode) {
                        SettingLocalConstants.MODE_BACKUP -> {
                            setupLauncher(REQUEST_CODE_SAF_WRITE_REALM) {
                                EasyDiaryUtils.writeFileWithSAF(RealmConstants.DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"), MIME_TYPE_REALM, mRequestWriteFileWithSAF)
                            }
                        }

                        SettingLocalConstants.MODE_RECOVERY -> {
                            setupLauncher(REQUEST_CODE_SAF_READ_REALM) {
                                EasyDiaryUtils.readFileWithSAF(MIME_TYPE_REALM, mRequestReadFileWithSAF)
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
//            requireActivity().updateAlertDialog(this, null, popupView.root)
                requireActivity().updateAlertDialogWithIcon(DialogMode.SETTING, this, null, popupView.root)
            }
    }
}
