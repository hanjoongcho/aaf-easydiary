package me.blog.korn123.easydiary.fragments

import android.app.Activity
import android.content.Context
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

class SettingsLock : androidx.fragment.app.Fragment() {


    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mRootView: ViewGroup
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity


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

        mContext = context!!
        mActivity = activity!!

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
        when (view.id) {
            R.id.appLockSetting -> {
                when (mContext.config.aafPinLockEnable) {
                    true -> {
                        if (mContext.config.fingerprintLockEnable) {
                            mActivity.showAlertDialog(getString(R.string.pin_release_need_fingerprint_disable), null)
                        } else {
                            appLockSettingSwitcher.isChecked = false
                            mContext.config.aafPinLockEnable = false
                            mActivity.showAlertDialog(getString(R.string.pin_setting_release), null)
                        }
                    }
                    false -> {
                        startActivity(Intent(mContext, PinLockActivity::class.java).apply {
                            putExtra(FingerprintLockActivity.LAUNCHING_MODE, PinLockActivity.ACTIVITY_SETTING)
                        })
                    }
                }
            }
            R.id.fingerprint -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    when (mContext.config.fingerprintLockEnable) {
                        true -> {
                            fingerprintSwitcher.isChecked = false
                            mContext.config.fingerprintLockEnable = false
                            mActivity.showAlertDialog(getString(R.string.fingerprint_setting_release), null)
                        }
                        false -> {
                            when (mContext.config.aafPinLockEnable) {
                                true -> {
                                    startActivity(Intent(mContext, FingerprintLockActivity::class.java).apply {
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

    private fun bindEvent() {
        appLockSetting.setOnClickListener(mOnClickListener)
        fingerprint.setOnClickListener(mOnClickListener)
    }

    private fun initPreference() {
        appLockSettingSwitcher.isChecked = mContext.config.aafPinLockEnable
        fingerprintSwitcher.isChecked = mContext.config.fingerprintLockEnable
    }
}