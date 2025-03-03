package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.databinding.FragmentSettingsLockBinding
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.openOverDueNotification
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.ui.components.CardContainer
import me.blog.korn123.easydiary.ui.components.SimpleCard
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
        bindEvent()
        updateFragmentUI(mBinding.root)
        initPreference()

        mBinding.composeView.setContent {
            AppTheme {
                Column {
                    SimpleCard(
                        "Notification-01",
                        "Basic",
                        Modifier
                            .fillMaxWidth()
                    ) {
                    }
                    var aafPinLockEnable by remember { mutableStateOf(requireContext().config.aafPinLockEnable) }
                    SwitchCard(
                        getString(R.string.pin_lock_title),
                        getString(R.string.pin_lock_summary),
                        Modifier
                            .fillMaxWidth(),
                        aafPinLockEnable
                    ) {
                        aafPinLockEnable = aafPinLockEnable.not()
                        config.aafPinLockEnable = aafPinLockEnable
                    }
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
    private val mOnClickListener = View.OnClickListener { view ->
        mActivity.run {
            when (view.id) {
                R.id.appLockSetting -> {
                    when (config.aafPinLockEnable) {
                        true -> {
                            if (config.fingerprintLockEnable) {
                                showAlertDialog(getString(R.string.pin_release_need_fingerprint_disable), null)
                            } else {
                                mBinding.appLockSettingSwitcher.isChecked = false
                                config.aafPinLockEnable = false
                                showAlertDialog(getString(R.string.pin_setting_release), null)
                                applyPolicyForRecentApps()
                            }
                        }
                        false -> {
                            startActivity(Intent(this, PinLockActivity::class.java).apply {
                                putExtra(FingerprintLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_SETTING)
                            })
                        }
                    }
                }
                R.id.fingerprint -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        when (config.fingerprintLockEnable) {
                            true -> {
                                mBinding.fingerprintSwitcher.isChecked = false
                                config.fingerprintLockEnable = false
                                showAlertDialog(getString(R.string.fingerprint_setting_release), null)
                                applyPolicyForRecentApps()
                            }
                            false -> {
                                when (config.aafPinLockEnable) {
                                    true -> {
                                        startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
                                            putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_SETTING)
                                        })
                                    }
                                    false -> {
                                        mActivity.showAlertDialog(getString(R.string.fingerprint_lock_need_pin_setting), null)
                                    }
                                }
                            }
                        }
                    } else {
                        mActivity.showAlertDialog(getString(R.string.fingerprint_not_available), null)
                    }
                }
            }
        }
    }

    private fun bindEvent() {
        mBinding.run {
            appLockSetting.setOnClickListener(mOnClickListener)
            fingerprint.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        mBinding.run {
            appLockSettingSwitcher.isChecked = mActivity.config.aafPinLockEnable
            fingerprintSwitcher.isChecked = mActivity.config.fingerprintLockEnable
        }
    }
}