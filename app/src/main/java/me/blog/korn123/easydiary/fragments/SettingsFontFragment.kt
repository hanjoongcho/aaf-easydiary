package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseSettingsActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.adapters.FontItemAdapter
import me.blog.korn123.easydiary.adapters.OptionItemAdapter
import me.blog.korn123.easydiary.databinding.FragmentSettingsFontBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.confirmExternalStoragePermission
import me.blog.korn123.easydiary.extensions.getBaseName
import me.blog.korn123.easydiary.extensions.getExtension
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.DEFAULT_CALENDAR_FONT_SCALE
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.USER_CUSTOM_FONTS_DIRECTORY
import me.blog.korn123.easydiary.presentation.settings.SettingsViewModel
import me.blog.korn123.easydiary.presentation.settings.components.FontSize
import me.blog.korn123.easydiary.presentation.settings.components.LineSpacing
import me.blog.korn123.easydiary.presentation.settings.components.SimpleCard
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.io.File

class SettingsFontFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentSettingsFontBinding
    private lateinit var progressContainer: ConstraintLayout
    private lateinit var requestFontPick: ActivityResultLauncher<Intent>
    private lateinit var requestExternalStoragePermissionLauncher: ActivityResultLauncher<Array<String>>
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestFontPick =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                requireActivity().run {
                    pauseLock()
                    if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                        activityResult.data?.data?.let { uri ->
                            val fileName = EasyDiaryUtils.queryName(contentResolver, uri)
                            if (fileName.getExtension().equals("ttf", true)) {
                                Thread {
                                    val fontDestDir = File(EasyDiaryUtils.getApplicationDataDirectory(this) + USER_CUSTOM_FONTS_DIRECTORY)
                                    contentResolver.openInputStream(uri)?.use { inputStream ->
                                        File(fontDestDir, fileName).outputStream().use { fos ->
                                            inputStream.copyTo(fos)
                                        }
                                    }
                                    runOnUiThread {
                                        progressContainer.visibility = View.GONE
                                        showAlertDialog("${fileName.getBaseName()} font file is registered.", null)
                                    }
                                }.start()
                                progressContainer.visibility = View.VISIBLE
                            } else {
                                showAlertDialog(
                                    "$fileName is not ttf file.",
                                    null,
                                    null,
                                    DialogMode.INFO,
                                    false,
                                    getString(R.string.add_ttf_fonts_title),
                                )
                            }
                        }
                    }
                }
            }

        requestExternalStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                requireActivity().run {
                    pauseLock()
                    when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                        true -> openFontSettingDialog()
                        false -> makeSnackBar(binding.root, getString(R.string.guide_message_3))
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsFontBinding.inflate(layoutInflater)
        return binding.root
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        progressContainer = (requireActivity() as BaseSettingsActivity).getProgressContainer()
        updateFragmentUI(binding.root)

        binding.composeView.setContent {
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

                    val enableCardViewPolicy: Boolean by settingsViewModel.enableCardViewPolicy.collectAsState()
                    val fontSize: Float by settingsViewModel.fontSize.collectAsState()
                    val lineSpacingScaleFactor: Float by settingsViewModel.lineSpacingScaleFactor.collectAsState()
                    val fontFamily: FontFamily? by settingsViewModel.fontFamily.collectAsState()
                    val fontSettingDescription: String by settingsViewModel.fontSettingDescription.collectAsState()
                    SimpleCard(
                        title = getString(R.string.font_setting),
                        description = fontSettingDescription,
                        fontSize = fontSize,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                    ) {
                        requireActivity().run {
                            if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                                openFontSettingDialog()
                            } else {
                                confirmExternalStoragePermission(EXTERNAL_STORAGE_PERMISSIONS, requestExternalStoragePermissionLauncher)
                            }
                        }
                    }

                    LineSpacing(
                        title = "${getString(R.string.font_line_spacing)} $lineSpacingScaleFactor",
                        description = getString(R.string.font_line_spacing_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    ) { progressFloat ->
                        setFontsStyle()
                        settingsViewModel.setLineSpacingScaleFactor(progressFloat)
                    }

                    FontSize(
                        title = getString(R.string.font_size_title),
                        description = getString(R.string.font_size_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                        callbackMinus = {
                            requireActivity().run {
                                config.settingFontSize = config.settingFontSize.minus(5)
                                settingsViewModel.setFontSize(config.settingFontSize)
                            }
                        },
                        callbackPlus = {
                            requireActivity().run {
                                config.settingFontSize = config.settingFontSize.plus(5)
                                settingsViewModel.setFontSize(config.settingFontSize)
                            }
                        },
                    )

                    val calendarFontScaleDescription: String by settingsViewModel.calendarFontScaleDescription.collectAsState(
                        when (requireActivity().config.settingCalendarFontScale) {
                            DEFAULT_CALENDAR_FONT_SCALE -> {
                                getString(R.string.calendar_font_scale_disable)
                            }

                            else -> {
                                getString(
                                    R.string.calendar_font_scale_factor,
                                    requireActivity().config.settingCalendarFontScale,
                                )
                            }
                        },
                    )
                    SimpleCard(
                        title = getString(R.string.calendar_font_scale_title),
                        description = getString(R.string.calendar_font_scale_description),
                        subDescription = calendarFontScaleDescription,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    ) {
                        openCalendarFontScaleDialog()
                    }

                    SimpleCard(
                        title = getString(R.string.add_ttf_fonts_title),
                        description = getString(R.string.add_ttf_fonts_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    ) {
                        performFileSearch()
                    }

                    var boldStyleEnable by remember { mutableStateOf(requireContext().config.boldStyleEnable) }
                    SwitchCard(
                        title = getString(R.string.bold_style_option_title),
                        description = getString(R.string.bold_style_option_summary),
                        modifier = settingCardModifier,
                        isOn = boldStyleEnable,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    ) {
                        requireActivity().run {
                            boldStyleEnable = boldStyleEnable.not()
                            config.boldStyleEnable = boldStyleEnable
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(binding.root)
        initPreference()
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    private fun performFileSearch() {
        val intent =
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }

        requestFontPick.launch(intent)
    }

    private fun initPreference() {
        binding.run {
            settingsViewModel.setLineSpacingScaleFactor(requireActivity().config.lineSpacingScaleFactor)
            settingsViewModel.setFontFamily(FontUtils.getComposeFontFamily(requireContext()))
        }
    }

    /**
     *  Float Formatting depends on Locale
     *  https://stackoverflow.com/questions/44541638/java-lang-numberformatexception-while-executing-in-france-machine
     */
    private fun openCalendarFontScaleDialog() {
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(requireActivity())
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val containerView = inflater.inflate(R.layout.dialog_option_item, null)
        val listView = containerView.findViewById<ListView>(R.id.listView)

        var selectedIndex = 0
        val listFontScale = ArrayList<Map<String, String>>()
        listFontScale.add(mapOf("optionTitle" to getString(R.string.calendar_font_scale_disable), "optionValue" to "-1"))
        for (i in 1..20 step 1) {
            listFontScale.add(mapOf("optionTitle" to getString(R.string.calendar_font_scale_factor, i * 0.1), "optionValue" to "${i * 0.1F}"))
        }

        listFontScale.mapIndexed { index, map ->
            val fontScale = map["optionValue"] ?: "0"
            if (requireActivity().config.settingCalendarFontScale == fontScale.toFloat()) selectedIndex = index
        }

        val arrayAdapter = OptionItemAdapter(requireActivity(), R.layout.item_check_label, listFontScale, requireActivity().config.settingCalendarFontScale)
        listView.adapter = arrayAdapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val fontInfo = parent.adapter.getItem(position) as? Map<*, *>
                (fontInfo?.get("optionValue") as? String)?.let {
                    requireActivity().config.settingCalendarFontScale = it.toFloat()
                }
                alertDialog?.cancel()
                initPreference()
            }

        alertDialog = builder.create().apply { requireActivity().updateAlertDialog(this, null, containerView, getString(R.string.calendar_font_scale_title)) }
        listView.setSelection(selectedIndex)
    }

    private fun openFontSettingDialog() {
        var alertDialog: AlertDialog? = null
        EasyDiaryUtils.initWorkingDirectory(requireActivity())
        val builder = AlertDialog.Builder(requireActivity())
        builder.setNegativeButton(getString(android.R.string.cancel), null)
        val inflater = requireActivity().getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val fontView = inflater.inflate(R.layout.dialog_fonts, null)
        val listView = fontView.findViewById<ListView>(R.id.listFont)

        val fontNameArray = resources.getStringArray(R.array.pref_list_fonts_title)
        val fontPathArray = resources.getStringArray(R.array.pref_list_fonts_values)
        val listFont = ArrayList<Map<String, String>>()
        var selectedIndex = 0
        for (i in fontNameArray.indices) {
            val map = HashMap<String, String>()
            map["disPlayFontName"] = fontNameArray[i]
            map["fontName"] = fontPathArray[i]
            listFont.add(map)
        }

        val fontDir = File(EasyDiaryUtils.getApplicationDataDirectory(requireActivity()) + USER_CUSTOM_FONTS_DIRECTORY)
        fontDir.list()?.let {
            for (fontName in it) {
                if (fontName.getExtension().equals("ttf", ignoreCase = true)) {
                    val map = HashMap<String, String>()
                    map["disPlayFontName"] = fontName.getBaseName()
                    map["fontName"] = fontName
                    listFont.add(map)
                }
            }
        }

        listFont.mapIndexed { index, map ->
            if (requireActivity().config.settingFontName == map["fontName"]) selectedIndex = index
        }

        val arrayAdapter = FontItemAdapter(requireActivity(), R.layout.item_check_label, listFont)
        listView.adapter = arrayAdapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val fontInfo = parent.adapter.getItem(position) as? Map<*, *>
                (fontInfo?.get("fontName") as? String)?.let {
                    requireActivity().config.settingFontName = it
                    FontUtils.setCommonTypeface(requireActivity())
                    initPreference()
                    setFontsStyle()

                    (requireActivity() as SettingsActivity).run {
                        pauseLock()
                        updateUI()
                    }
                }
                alertDialog?.cancel()
            }

        alertDialog = builder.create().apply { requireActivity().updateAlertDialog(this, null, fontView, getString(R.string.font_setting)) }
        listView.setSelection(selectedIndex)
    }

    private fun setFontsStyle() {
        FontUtils.setFontsTypeface(requireActivity(), null, binding.root)
    }
}
