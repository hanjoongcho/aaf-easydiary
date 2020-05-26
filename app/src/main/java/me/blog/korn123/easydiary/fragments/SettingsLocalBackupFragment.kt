package me.blog.korn123.easydiary.fragments

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.aafactory.commons.extensions.baseConfig
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.layout_settings_backup_local.*
import kotlinx.android.synthetic.main.popup_location_selector.view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.RealmFileItemAdapter
import me.blog.korn123.easydiary.adapters.SimpleCheckbox
import me.blog.korn123.easydiary.adapters.SimpleCheckboxAdapter
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.viewmodels.BackupOperations
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
import java.util.*

class SettingsLocalBackupFragment() : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRootView: ViewGroup
    private val mActivity: Activity
        get() = activity!!


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.layout_settings_backup_local, container, false) as ViewGroup
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindEvent()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mRootView)
        initPreference()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mActivity.pauseLock()
        if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            when (requestCode) {
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    createExportExcelUri()
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    showLocationSelectionPopup(MODE_BACKUP, getString(R.string.backup_internal_title), getString(R.string.backup_internal_description), getString(R.string.backup_external_title), getString(R.string.backup_external_description))
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    showLocationSelectionPopup(MODE_RECOVERY, getString(R.string.recovery_internal_title), getString(R.string.recovery_internal_description), getString(R.string.recovery_external_title), getString(R.string.recovery_external_description))
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    deleteRealmFile()
                }
                REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip", MIME_TYPE_ZIP, REQUEST_CODE_SAF_WRITE_ZIP)
                }
            }
        } else {
            mActivity.makeSnackBar(mActivity.findViewById(android.R.id.content), getString(R.string.guide_message_3))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && intent != null) {
            when (requestCode) {
                REQUEST_CODE_SAF_WRITE_ZIP -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportFullBackupFile(intent.data)
                }
                REQUEST_CODE_SAF_WRITE_XLS -> if (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    exportExcel(intent.data)
                }
                REQUEST_CODE_SAF_READ_ZIP -> {
                    importFullBackupFile(intent.data)
                }
                REQUEST_CODE_SAF_WRITE_REALM -> {
                    exportRealmFileWithSAF(intent.data)
                }
                REQUEST_CODE_SAF_READ_REALM -> {
                    importRealmFileWithSAF(intent.data)
                }
            }
        }
    }


    /***************************************************************************************************
     *   backup and recovery
     *
     ***************************************************************************************************/
    private fun exportRealmFile(showDialog: Boolean = true) {
        mActivity.exportRealmFile()
        mActivity.makeSnackBar("Operation completed.")
    }

    private fun exportRealmFileWithSAF(uri: Uri?) {
        uri?.let {
            val os = mActivity.contentResolver.openOutputStream(it)
            val `is` = FileInputStream(EasyDiaryDbHelper.getRealmPath())
            IOUtils.copy(`is`, os)
            os?.close()
            `is`.close()
            mActivity.makeSnackBar("Operation completed.")
        }
    }

    private fun importRealmFile() {
        val files = File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    var alertDialog: AlertDialog? = null
                    val builder = AlertDialog.Builder(mActivity)
                    builder.setNegativeButton(getString(android.R.string.cancel), null)
//                    builder.setMessage(getString(R.string.open_realm_file_message))

                    val realmFiles: ArrayList<HashMap<String, String>> = arrayListOf()
                    it.sortDescending()
                    it.map { file ->
                        val itemInfo = hashMapOf<String, String>("name" to file.name, "createdTime" to Date(file.lastModified()).toString())
                        realmFiles.add(itemInfo)
                    }

                    val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_realm_files, null)
                    val listView = rootView.findViewById<ListView>(R.id.files)
                    val adapter = RealmFileItemAdapter(mActivity, R.layout.item_realm_file, realmFiles)
                    listView.adapter = adapter
                    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val itemInfo = parent.adapter.getItem(position) as HashMap<String, String>
                        val srcFile = File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_DB_DIRECTORY + itemInfo["name"])
                        val destFile = File(EasyDiaryDbHelper.getRealmPath())
                        EasyDiaryDbHelper.closeInstance()
                        FileUtils.copyFile(srcFile, destFile)
                        mActivity.refreshApp()
                        alertDialog?.cancel()
                    }

                    alertDialog = builder.create().apply { mActivity.updateAlertDialog(this, null, rootView, "${getString(R.string.open_realm_file_title)} (Total: ${it.size})") }
                }
                false -> {}
            }
        }
    }

    private fun importRealmFileWithSAF(uri: Uri?) {
        uri?.let {
            val inputStream = mActivity.contentResolver.openInputStream(it)
            val outputStream = FileOutputStream(File(EasyDiaryDbHelper.getRealmPath()))
            EasyDiaryDbHelper.closeInstance()
            IOUtils.copy(inputStream, outputStream)
            inputStream?.close()
            outputStream.close()
            mActivity.refreshApp()
        }
    }

    private fun deleteRealmFile() {
        val files = File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_DB_DIRECTORY).listFiles()
        files?.let {
            when (it.isNotEmpty()) {
                true -> {
                    val realmInfoList: ArrayList<SimpleCheckbox> = arrayListOf()
                    val builder = AlertDialog.Builder(mActivity)
                    builder.setCancelable(false)
                    builder.setPositiveButton(getString(R.string.delete)) { _, _ -> }
                    builder.setNegativeButton(getString(android.R.string.cancel), null)

                    it.sortDescending()
                    it.map { file ->
                        realmInfoList.add(SimpleCheckbox(file.name, Date(file.lastModified()).toString()))
                    }

                    val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val rootView = inflater.inflate(R.layout.dialog_delete_realm_files, null)
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.files)

//                    val spacesItemDecoration = PostCardViewerActivity.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding))
                    val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(mActivity, 1)

                    recyclerView.apply {
                        adapter = SimpleCheckboxAdapter(realmInfoList, AdapterView.OnItemClickListener { parent, view, position, id ->
                            val realmInfo = parent.adapter.getItem(position) as SimpleCheckbox
                            mActivity.makeSnackBar("${realmInfo.isChecked}")
                        })
                        layoutManager = gridLayoutManager
//                        addItemDecoration(spacesItemDecoration)
                    }

                    builder.create().apply {
                        mActivity.updateAlertDialog(this, null, rootView, "${getString(R.string.delete_realm_title)} (Total: ${it.size})")
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val checkedList = mutableListOf<String>()
                            realmInfoList.forEach { item ->
                                if (item.isChecked) checkedList.add(item.title)
                            }
                            mActivity.showAlertDialog(getString(R.string.delete_confirm), DialogInterface.OnClickListener { _, _ ->
                                checkedList.map { filename ->
                                    File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_DB_DIRECTORY + filename).delete()
                                }
                                this.dismiss()
                            } , null)
                        }
                    }
                }
                false -> {}
            }
        }
    }
    
    private fun sendEmailWithExcel() {
        val exportFileName = "aaf-easydiray_${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER)}"
        val builder = AlertDialog.Builder(mActivity)
        builder.setTitle(getString(R.string.export_excel_title))
        builder.setIcon(ContextCompat.getDrawable(mActivity, R.drawable.excel_3))
        builder.setCancelable(false)
//        builder.setPositiveButton(getString(R.string.ok), null)
        val alert = builder.create()
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_export_progress_excel, null)
        val progressInfo = containerView.findViewById<TextView>(R.id.progressInfo)
        alert.setView(containerView)
        alert.show()

        Thread(Runnable {
            val workBook = createWorkBook(progressInfo, "Create excel file...")
            val outputStream = FileOutputStream("${EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_EXCEL_DIRECTORY + exportFileName}.xls")
            workBook.write(outputStream)
            outputStream.close()
            mActivity.runOnUiThread {
                alert.cancel()
            }

            val destFile = File(File(EasyDiaryUtils.getApplicationDataDirectory(mActivity) + BACKUP_EXCEL_DIRECTORY), "$exportFileName.xls")
            // test code for attach file to email
            val emailIntent: Intent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "text/plain"
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("email@example.com"))
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here")
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "body text")
            emailIntent.putExtra(Intent.EXTRA_STREAM, mActivity.getUriForFile(destFile))
            startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
        }).start()
    }

    private fun createExportExcelUri() {
        writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".xls", MIME_TYPE_XLS, REQUEST_CODE_SAF_WRITE_XLS)
    }

    private fun exportExcel(uri: Uri?) {
//        EasyDiaryUtils.initLegacyWorkingDirectory(mActivity)
        val exportFileName = "aaf-easydiray_${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER)}"
        val builder = AlertDialog.Builder(mActivity)
        builder.setTitle(getString(R.string.export_excel_title))
        builder.setIcon(ContextCompat.getDrawable(mActivity, R.drawable.excel_3))
        builder.setCancelable(false)
//        builder.setPositiveButton(getString(R.string.ok), null)
        val alert = builder.create()
        val inflater = mActivity.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_export_progress_excel, null)
        val progressInfo = containerView.findViewById<TextView>(R.id.progressInfo)
        val confirmButton = containerView.findViewById<TextView>(R.id.confirm)
        progressInfo.text = "Preparing to export..."
        alert.setView(containerView)
        alert.show()

        Thread(Runnable {
            val workBook = createWorkBook(progressInfo, "$exportFileName.xls")
//            val outputStream = FileOutputStream("${EasyDiaryUtils.getExternalStorageDirectory().absolutePath + BACKUP_EXCEL_DIRECTORY + exportFileName}.xls")
            val outputStream = mActivity.contentResolver.openOutputStream(uri!!)
            workBook.write(outputStream)
            outputStream?.close()
            mActivity.runOnUiThread {
                confirmButton.visibility = View.VISIBLE
                confirmButton.setOnClickListener { alert.cancel() }
            }
        }).start()
    }

    private fun createWorkBook(infoView: TextView? = null, guideMessage: String? = ""): Workbook {
        val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
        val diaryList = EasyDiaryDbHelper.readDiary(null, false, 0, 0, 0, realmInstance)
        val wb: Workbook = HSSFWorkbook()
        val sheet = wb.createSheet("new sheet")

        val headerFont = wb.createFont().apply {
            color = IndexedColors.WHITE.index
        }
        val headerStyle = wb.createCellStyle().apply {
            wrapText = true
            fillForegroundColor = IndexedColors.BLUE.index
            fillPattern = CellStyle.SOLID_FOREGROUND
            alignment = CellStyle.ALIGN_CENTER
            verticalAlignment = CellStyle.VERTICAL_CENTER
            setFont(headerFont)
        }
        val bodyStyle = wb.createCellStyle().apply {
            wrapText = true
            verticalAlignment = CellStyle.VERTICAL_TOP
        }

        val headerRow = sheet.createRow(0)
        headerRow.height = 256 * 3
        headerRow.createCell(SEQ).setCellValue(getString(R.string.export_excel_header_seq))
        headerRow.createCell(WRITE_DATE).setCellValue(getString(R.string.export_excel_header_write_date))
        headerRow.createCell(TITLE).setCellValue(getString(R.string.export_excel_header_title))
        headerRow.createCell(CONTENTS).setCellValue(getString(R.string.export_excel_header_contents))
        headerRow.createCell(ATTACH_PHOTO_NAME).setCellValue(getString(R.string.export_excel_header_attach_photo_path))
        headerRow.createCell(ATTACH_PHOTO_SIZE).setCellValue(getString(R.string.export_excel_header_attach_photo_size))
        headerRow.createCell(WRITE_TIME_MILLIS).setCellValue(getString(R.string.export_excel_header_write_time_millis))
        headerRow.createCell(SYMBOL).setCellValue(getString(R.string.export_excel_header_symbol))
        headerRow.createCell(IS_ALL_DAY).setCellValue(getString(R.string.export_excel_header_is_all_day))

        headerRow.getCell(SEQ).cellStyle = headerStyle
        headerRow.getCell(WRITE_DATE).cellStyle = headerStyle
        headerRow.getCell(TITLE).cellStyle = headerStyle
        headerRow.getCell(CONTENTS).cellStyle = headerStyle
        headerRow.getCell(ATTACH_PHOTO_NAME).cellStyle = headerStyle
        headerRow.getCell(ATTACH_PHOTO_SIZE).cellStyle = headerStyle
        headerRow.getCell(WRITE_TIME_MILLIS).cellStyle = headerStyle
        headerRow.getCell(SYMBOL).cellStyle = headerStyle
        headerRow.getCell(IS_ALL_DAY).cellStyle = headerStyle

        // FIXME:
        // https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/Sheet.html#setColumnWidth-int-int-
        sheet.setColumnWidth(SEQ, 256 * 10)
        sheet.setColumnWidth(WRITE_DATE, 256 * 30)
        sheet.setColumnWidth(TITLE, 256 * 30)
        sheet.setColumnWidth(CONTENTS, 256 * 50)
        sheet.setColumnWidth(ATTACH_PHOTO_NAME, 256 * 80)
        sheet.setColumnWidth(ATTACH_PHOTO_SIZE, 256 * 15)
        sheet.setColumnWidth(WRITE_TIME_MILLIS, 256 * 60)
        sheet.setColumnWidth(SYMBOL, 256 * 10)
        sheet.setColumnWidth(IS_ALL_DAY, 256 * 30)
        val diarySymbolMap = FlavorUtils.getDiarySymbolMap(mActivity)
        diaryList.forEachIndexed { index, diaryDto ->
            val row = sheet.createRow(index + 1)
            val photoNames = StringBuffer()
            val photoSizes = StringBuffer()
            diaryDto.photoUris?.map {
                photoNames.append("$DIARY_PHOTO_DIRECTORY${FilenameUtils.getName(it.getFilePath())}\n")
                photoSizes.append("${File(it.getFilePath()).length() / 1024}\n")
            }

            val sequence = row.createCell(SEQ).apply {cellStyle = bodyStyle}
            val writeDate = row.createCell(WRITE_DATE).apply {cellStyle = bodyStyle}
            val title = row.createCell(TITLE).apply {cellStyle = bodyStyle}
            val contents = row.createCell(CONTENTS).apply {cellStyle = bodyStyle}
            val attachPhotoNames = row.createCell(ATTACH_PHOTO_NAME).apply {cellStyle = bodyStyle}
            val attachPhotoSizes = row.createCell(ATTACH_PHOTO_SIZE).apply {cellStyle = bodyStyle}
            val writeTimeMillis = row.createCell(WRITE_TIME_MILLIS).apply {cellStyle = bodyStyle}
            val weather = row.createCell(SYMBOL).apply {cellStyle = bodyStyle}
            val isAllDay = row.createCell(IS_ALL_DAY).apply {cellStyle = bodyStyle}

            sequence.setCellValue(diaryDto.sequence.toDouble())
            writeDate.setCellValue(DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis))
            title.setCellValue(diaryDto.title)
            contents.setCellValue(diaryDto.contents)
            attachPhotoNames.setCellValue(photoNames.toString())
            attachPhotoSizes.setCellValue(photoSizes.toString())
            writeTimeMillis.setCellValue(diaryDto.currentTimeMillis.toDouble())
            isAllDay.setCellValue(diaryDto.isAllDay)
            weather.setCellValue(diarySymbolMap[diaryDto.weather])

            mActivity.runOnUiThread {
                infoView?.run {
                    text = "${index.plus(1)} / ${diaryList.size}\n$guideMessage"
                }
            }
        }
        realmInstance.close()
        return wb
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun writeFileWithSAF(fileName: String, mimeType: String, requestCode: Int) {
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

    private fun readFileWithSAF(mimeType: String, requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = mimeType
        }
        startActivityForResult(intent, requestCode)
    }

    private fun exportFullBackupFile(uri: Uri?) {
        exportRealmFile(false)
        BackupOperations.Builder(mActivity, uri.toString(), BackupOperations.WORK_MODE_BACKUP).build().apply {
            continuation.enqueue()
        }
    }

    private fun importFullBackupFile(uri: Uri?) {
        BackupOperations.Builder(mActivity, uri.toString(), BackupOperations.WORK_MODE_RECOVERY).build().apply {
            continuation.enqueue()
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.exportExcel -> {
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> createExportExcelUri()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_EXCEL)
                }
            }
            R.id.exportFullBackupFile -> {
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> writeFileWithSAF(DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER) + ".zip", MIME_TYPE_ZIP, REQUEST_CODE_SAF_WRITE_ZIP)
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_FULL_BACKUP)
                }
            }
            R.id.importFullBackupFile -> {
                readFileWithSAF(MIME_TYPE_ZIP, REQUEST_CODE_SAF_READ_ZIP)
            }
            R.id.sendEmailWithExcel -> {
                sendEmailWithExcel()
            }
            R.id.exportRealmFile -> {
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> showLocationSelectionPopup(MODE_BACKUP, getString(R.string.backup_internal_title), getString(R.string.backup_internal_description), getString(R.string.backup_external_title), getString(R.string.backup_external_description))
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_EXPORT_REALM)
                }
            }
            R.id.importRealmFile -> {
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> showLocationSelectionPopup(MODE_RECOVERY, getString(R.string.recovery_internal_title), getString(R.string.recovery_internal_description), getString(R.string.recovery_external_title), getString(R.string.recovery_external_description))
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_IMPORT_REALM)
                }
            }
            R.id.deleteRealmFile -> {
                when (mActivity.checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    true -> deleteRealmFile()
                    false -> confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_DELETE_REALM)
                }
            }
        }
    }

    private fun bindEvent() {
        exportExcel.setOnClickListener(mOnClickListener)
        sendEmailWithExcel.setOnClickListener(mOnClickListener)
        exportRealmFile.setOnClickListener(mOnClickListener)
        importRealmFile.setOnClickListener(mOnClickListener)
        deleteRealmFile.setOnClickListener(mOnClickListener)
        exportFullBackupFile.setOnClickListener(mOnClickListener)
        importFullBackupFile.setOnClickListener(mOnClickListener)
    }

    private fun initPreference() {}

    private fun showLocationSelectionPopup(popupMode: Int, internalTitle: String, internalDescription: String, externalTitle: String, externalDescription: String) {
        var dialog: AlertDialog? = null
        val builder = AlertDialog.Builder(mActivity).apply {
            setNegativeButton(getString(android.R.string.cancel), null)
        }
        val inflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = (inflater.inflate(R.layout.popup_location_selector, null) as ViewGroup).apply {
            modeInternalTitle.text = internalTitle
            modeInternalDescription.text = internalDescription
            modeExternalTitle.text = externalTitle
            modeExternalDescription.text = externalDescription

            setBackgroundColor(mActivity.baseConfig.backgroundColor)
            closePopup.setOnClickListener { dialog?.dismiss() }
            modeInternal.setOnClickListener {
                when (popupMode) {
                    MODE_BACKUP -> exportRealmFile()
                    MODE_RECOVERY -> importRealmFile()
                }
                dialog?.dismiss()
            }
            modeExternal.setOnClickListener {
                when (popupMode) {
                    MODE_BACKUP -> writeFileWithSAF(DIARY_DB_NAME + "_" + DateUtils.getCurrentDateTime("yyyyMMdd_HHmmss"), MIME_TYPE_REALM, REQUEST_CODE_SAF_WRITE_REALM)
                    MODE_RECOVERY -> readFileWithSAF(MIME_TYPE_REALM, REQUEST_CODE_SAF_READ_REALM)
                }
                dialog?.dismiss()
            }
        }
        mActivity.run {
            updateTextColors(popupView)
            initTextSize(popupView)
        }

        FontUtils.setFontsTypeface(mActivity, mActivity.assets, null, popupView, true)
        builder.setView(popupView)
        dialog = builder.create().apply {
            mActivity.updateAlertDialog(this, null, popupView)
        }
    }

    companion object {
        const val SEQ = 0
        const val WRITE_DATE = 1
        const val TITLE = 2
        const val CONTENTS = 3
        const val ATTACH_PHOTO_NAME = 4
        const val ATTACH_PHOTO_SIZE = 5
        const val WEATHER = 6  /* no longer used since version 1.4.79 */
        const val SYMBOL = 6
        const val IS_ALL_DAY = 7
        const val WRITE_TIME_MILLIS = 8
        const val MODE_BACKUP = 0
        const val MODE_RECOVERY = 1
    }
}