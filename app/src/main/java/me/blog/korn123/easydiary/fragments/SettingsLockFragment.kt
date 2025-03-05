package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.databinding.FragmentSettingsLockBinding
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

class SettingsLockFragment : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsLockBinding
    private val mActivity: Activity
        get() = requireActivity()


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentSettingsLockBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.composeView.setContent {
            AppTheme {
                Column {
                    var aafPinLockEnable by remember { mutableStateOf(requireContext().config.aafPinLockEnable) }
                    SwitchCard(
                        getString(R.string.pin_lock_title),
                        getString(R.string.pin_lock_summary),
                        Modifier
                            .fillMaxWidth(),
                        aafPinLockEnable
                    ) {
                        mActivity.run {
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
                                    startActivity(Intent(this, PinLockActivity::class.java).apply {
                                        putExtra(FingerprintLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_SETTING)
                                    })
                                }
                            }
                        }
                    }
                    var fingerprintLockEnable by remember { mutableStateOf(requireContext().config.fingerprintLockEnable) }
                    SwitchCard(
                        getString(R.string.fingerprint_lock_title),
                        getString(R.string.fingerprint_lock_summary),
                        Modifier
                            .fillMaxWidth(),
                        fingerprintLockEnable
                    ) {
                        mActivity.run {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                                                startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
                                                    putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_SETTING)
                                                })
                                            }
                                            false -> {
                                                mActivity.showAlertDialog(getString(R.string.fingerprint_lock_need_pin_setting))
                                            }
                                        }
                                    }
                                }
                            } else {
                                mActivity.showAlertDialog(getString(R.string.fingerprint_not_available))
                            }
                        }
                    }
                }
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
}