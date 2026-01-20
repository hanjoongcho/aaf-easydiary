package me.blog.korn123.easydiary.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.activityViewModels
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.MarkDownViewerActivity
import me.blog.korn123.easydiary.activities.SettingsActivity
import me.blog.korn123.easydiary.databinding.FragmentSettingsAppInfoBinding
import me.blog.korn123.easydiary.enums.Launcher
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.extensions.openGooglePlayBy
import me.blog.korn123.easydiary.extensions.toggleLauncher
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.theme.AppTheme
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel

class SettingsAppInfoFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsAppInfoBinding
    private val mSettingsViewModel: SettingsViewModel by activityViewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentSettingsAppInfoBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        bindEvent()
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
                    val rateAppSettingSummary: String by mSettingsViewModel.rateAppSettingSummary.observeAsState("")
                    val inviteSummary: String by mSettingsViewModel.inviteSummary.observeAsState("")

                    SimpleCard(
                        title = getString(R.string.rate_app),
                        description = rateAppSettingSummary,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        lineSpacingScaleFactor = lineSpacingScaleFactor,
                    ) {
                        if (BuildConfig.FLAVOR == "foss") {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getStoreUrl())))
                        } else {
                            requireActivity().openGooglePlayBy("me.blog.korn123.easydiary")
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.invite_friends),
                        description = inviteSummary,
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                    ) {
                        val text = String.format(getString(R.string.share_text), getString(R.string.app_name), getStoreUrl())
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                            startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
                        }
                    }

                    SimpleCard(
                        title = getString(R.string.faq_title),
                        description = getString(R.string.faq_description),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                    ) {
                        TransitionHelper.startActivityWithTransition(
                            requireActivity(),
                            Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                                putExtra(MarkDownViewerActivity.OPEN_URL_INFO, getString(R.string.faq_url))
                                putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.faq_title))
                                putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                            },
                        )
                    }

                    SimpleCard(
                        title = getString(R.string.privacy_policy_title),
                        description = getString(R.string.privacy_policy_description),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                    ) {
                        TransitionHelper.startActivityWithTransition(
                            requireActivity(),
                            Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                                putExtra(MarkDownViewerActivity.OPEN_URL_INFO, getString(R.string.privacy_policy_url))
                                putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.privacy_policy_title))
                                putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                            },
                        )
                    }

                    SimpleCard(
                        title = getString(R.string.release_notes_title),
                        description = getString(R.string.release_notes_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                    ) {
                        (requireActivity() as SettingsActivity).checkWhatsNewDialog(false)
                    }

                    SimpleCard(
                        title = getString(R.string.preferences_information_licenses),
                        description = getString(R.string.preferences_information_licenses_summary),
                        modifier = settingCardModifier,
                        enableCardViewPolicy = enableCardViewPolicy,
                        fontFamily = fontFamily,
                        onLongClick = {
                            requireActivity().run {
                                when (config.enableDebugMode) {
                                    true -> {
                                        config.enableDebugMode = false

                                        makeToast("Debug console is disabled.")
                                        toggleLauncher(Launcher.EASY_DIARY)
                                    }

                                    false -> {
                                        config.enableDebugMode = true
                                        makeToast("Debug console is enabled.")
                                        toggleLauncher(Launcher.DEBUG)
                                    }
                                }
                                true
                            }
                        },
                    ) {
                        TransitionHelper.startActivityWithTransition(
                            requireActivity(),
                            Intent(requireActivity(), MarkDownViewerActivity::class.java).apply {
                                putExtra(
                                    MarkDownViewerActivity.OPEN_URL_INFO,
                                    "https://raw.githubusercontent.com/hanjoongcho/aaf-easydiary/master/THIRDPARTY.md",
                                )
                                putExtra(MarkDownViewerActivity.OPEN_URL_DESCRIPTION, getString(R.string.preferences_information_licenses))
                                putExtra(MarkDownViewerActivity.FORCE_APPEND_CODE_BLOCK, false)
                            },
                        )
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
     *   etc functions
     *
     ***************************************************************************************************/
    private fun bindEvent() {
        setupInvite()
    }

    private fun initPreference() {
        mSettingsViewModel.setRateAppSettingSummary(
            String.format("v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE),
        )
    }

    @SuppressLint("StringFormatInvalid")
    private fun setupInvite() {
        mSettingsViewModel.setInviteSummary(String.format(getString(R.string.invite_friends_summary), getString(R.string.app_name)))
    }

    private fun getStoreUrl(): String =
        if (BuildConfig.FLAVOR ==
            "foss"
        ) {
            "https://f-droid.org/packages/${requireActivity().packageName}/"
        } else {
            "https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
        }
}
