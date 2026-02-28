package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityPinLockBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.applyBottomNavigationInsets
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.hideSystemBars
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.FingerprintLockConstants
import me.blog.korn123.easydiary.helper.PinLockConstants

class PinLockActivity : BaseSimpleActivity() {
    private lateinit var mBinding: ActivityPinLockBinding
    private var mPassword = arrayOfNulls<String>(4)
    private var mPasswordView = arrayOfNulls<TextView>(4)
    private var mCursorIndex = 0
    private var activityMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        activityMode = intent.getStringExtra(PinLockConstants.LAUNCHING_MODE)

        mBinding.run {
            mPasswordView[0] = pass1
            mPasswordView[1] = pass2
            mPasswordView[2] = pass3
            mPasswordView[3] = pass4
            num0.setOnClickListener(keyPadClickListener)
            num1.setOnClickListener(keyPadClickListener)
            num2.setOnClickListener(keyPadClickListener)
            num3.setOnClickListener(keyPadClickListener)
            num4.setOnClickListener(keyPadClickListener)
            num5.setOnClickListener(keyPadClickListener)
            num6.setOnClickListener(keyPadClickListener)
            num7.setOnClickListener(keyPadClickListener)
            num8.setOnClickListener(keyPadClickListener)
            num9.setOnClickListener(keyPadClickListener)
            delete.setOnClickListener(keyPadClickListener)
            if (config.fingerprintLockEnable) {
                fingerprint.visibility = View.VISIBLE
                changeFingerprintLock.setOnClickListener {
                    startActivity(
                        Intent(this@PinLockActivity, FingerprintLockActivity::class.java).apply {
                            putExtra(FingerprintLockConstants.LAUNCHING_MODE, FingerprintLockConstants.ACTIVITY_UNLOCK)
                        },
                    )
                    finish()
                }
            }
        }

        applyBottomNavigationInsets(mBinding.rightContainer)
        if (isLandScape()) hideSystemBars()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    ActivityCompat.finishAffinity(this@PinLockActivity)
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, null, mBinding.container)
        mBinding.infoMessage.text = if (activityMode == PinLockConstants.ACTIVITY_SETTING) getString(R.string.pin_setting_guide_message) else getString(R.string.pin_unlock_guide_message)
    }

    private val keyPadClickListener: View.OnClickListener =
        View.OnClickListener { view ->
            val inputPass =
                when (view?.id) {
                    R.id.num0 -> "0"
                    R.id.num1 -> "1"
                    R.id.num2 -> "2"
                    R.id.num3 -> "3"
                    R.id.num4 -> "4"
                    R.id.num5 -> "5"
                    R.id.num6 -> "6"
                    R.id.num7 -> "7"
                    R.id.num8 -> "8"
                    R.id.num9 -> "9"
                    R.id.delete -> "delete"
                    else -> ""
                }

            when (inputPass) {
                "delete" -> {
                    if (mCursorIndex > 0) mCursorIndex--
                    mPasswordView[mCursorIndex]?.text = ""
                    return@OnClickListener
                }

                else -> {
                    mPassword[mCursorIndex] = inputPass
                    val displayPass = if (activityMode == PinLockConstants.ACTIVITY_SETTING) inputPass else "-"
                    mPasswordView[mCursorIndex]?.text = displayPass

                    if (mCursorIndex == 3) {
                        var fullPassword = ""
                        mPassword.map {
                            fullPassword += it
                        }

                        when (activityMode) {
                            PinLockConstants.ACTIVITY_SETTING -> {
                                holdCurrentOrientation()
                                showAlertDialog(
                                    getString(R.string.pin_setting_complete, fullPassword),
                                    { _, _ ->
                                        config.aafPinLockEnable = true
                                        config.aafPinLockSavedPassword = fullPassword
                                        pauseLock()
                                        finish()
                                    },
                                    { _, _ ->
                                        finish()
                                    },
                                    DialogMode.INFO,
                                    false,
                                )
                            }

                            PinLockConstants.ACTIVITY_UNLOCK -> {
                                when (config.aafPinLockSavedPassword == fullPassword) {
                                    true -> {
                                        pauseLock()
                                        finish()
                                    }

                                    false -> {
                                        holdCurrentOrientation()
                                        showAlertDialog(
                                            message = getString(R.string.pin_verification_fail),
                                            positiveListener =
                                                { _, _ ->
                                                    ActivityCompat.finishAffinity(this@PinLockActivity)
                                                },
                                            negativeListener = null,
                                            dialogMode = DialogMode.DEFAULT,
                                            cancelable = false,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        mCursorIndex++
                    }
                }
            }
        }
}
