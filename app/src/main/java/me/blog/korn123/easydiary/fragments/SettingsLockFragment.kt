package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.databinding.FragmentSettingsLockBinding
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import me.blog.korn123.easydiary.helper.PinLockConstants
import me.blog.korn123.easydiary.presentation.settings.SettingsViewModel
import me.blog.korn123.easydiary.presentation.settings.components.SwitchCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme

class SettingsLockFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentSettingsLockBinding
    private val currentActivity: Activity
        get() = requireActivity()
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsLockBinding.inflate(layoutInflater)
        return binding.root
    }

    @OptIn(ExperimentalLayoutApi::class)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

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

                    val fontSize: Float by settingsViewModel.fontSize.collectAsState()
                    val fontFamily: FontFamily? by settingsViewModel.fontFamily.collectAsState()
                    var aafPinLockEnable by remember { mutableStateOf(requireContext().config.aafPinLockEnable) }
                    SwitchCard(
                        title = getString(R.string.pin_lock_title),
                        description = getString(R.string.pin_lock_summary),
                        fontSize = fontSize,
                        modifier = settingCardModifier,
                        isOn = aafPinLockEnable,
                        fontFamily = fontFamily,
                    ) {
                        currentActivity.run {
                            when (config.aafPinLockEnable) {
                                true -> {
                                    if (config.fingerprintLockEnable) {
                                        showAlertDialog(getString(R.string.pin_release_need_fingerprint_disable))
                                    } else {
                                        aafPinLockEnable = false
                                        config.aafPinLockEnable = aafPinLockEnable
                                        showAlertDialog(getString(R.string.pin_setting_release))
                                        applyPolicyForRecentApps()
                                    }
                                }

                                false -> {
                                    aafPinLockEnable = true
                                    config.aafPinLockEnable = aafPinLockEnable
                                    startActivity(
                                        Intent(this, PinLockActivity::class.java).apply {
                                            putExtra(
                                                FingerprintLockConstants.LAUNCHING_MODE,
                                                PinLockConstants.ACTIVITY_SETTING,
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                    var fingerprintLockEnable by remember { mutableStateOf(requireContext().config.fingerprintLockEnable) }
                    SwitchCard(
                        title = getString(R.string.fingerprint_lock_title),
                        description = getString(R.string.fingerprint_lock_summary),
                        fontSize = fontSize,
                        modifier = settingCardModifier,
                        isOn = fingerprintLockEnable,
                        fontFamily = fontFamily,
                    ) {
                        currentActivity.run {
                            when (config.fingerprintLockEnable) {
                                true -> {
                                    fingerprintLockEnable = false
                                    config.fingerprintLockEnable = fingerprintLockEnable
                                    showAlertDialog(getString(R.string.fingerprint_setting_release))
                                    applyPolicyForRecentApps()
                                }

                                false -> {
                                    when (config.aafPinLockEnable) {
                                        true -> {
                                            fingerprintLockEnable = true
                                            config.fingerprintLockEnable = fingerprintLockEnable
                                            startActivity(
                                                Intent(
                                                    this,
                                                    FingerprintLockActivity::class.java,
                                                ).apply {
                                                    putExtra(
                                                        FingerprintLockConstants.LAUNCHING_MODE,
                                                        FingerprintLockConstants.ACTIVITY_SETTING,
                                                    )
                                                },
                                            )
                                        }

                                        false -> {
                                            currentActivity.showAlertDialog(getString(R.string.fingerprint_lock_need_pin_setting))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
