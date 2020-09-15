package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_settings_lock.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.FingerprintLockActivity
import me.blog.korn123.easydiary.activities.PinLockActivity
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateFragmentUI

class SettingsLockFragment : androidx.fragment.app.Fragment() {


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
        mRootView = inflater.inflate(R.layout.layout_settings_lock, container, false) as ViewGroup
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
                                appLockSettingSwitcher.isChecked = false
                                config.aafPinLockEnable = false
                                showAlertDialog(getString(R.string.pin_setting_release), null)
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
                                fingerprintSwitcher.isChecked = false
                                config.fingerprintLockEnable = false
                                showAlertDialog(getString(R.string.fingerprint_setting_release), null)
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
        appLockSetting.setOnClickListener(mOnClickListener)
        fingerprint.setOnClickListener(mOnClickListener)
    }

    private fun initPreference() {
        appLockSettingSwitcher.isChecked = mActivity.config.aafPinLockEnable
        fingerprintSwitcher.isChecked = mActivity.config.fingerprintLockEnable
    }
}