package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_pin_lock.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*


class PinLockActivity : BaseSimpleActivity() {
    private var mPassword = arrayOfNulls<String>(4)
    private var mPasswordView = arrayOfNulls<TextView>(4)
    private var mCursorIndex = 0
    private var activityMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lock)
        activityMode = intent.getStringExtra(LAUNCHING_MODE)
        
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
                startActivity(Intent(this, FingerprintLockActivity::class.java).apply {
                    putExtra(FingerprintLockActivity.LAUNCHING_MODE, FingerprintLockActivity.ACTIVITY_UNLOCK)
                })
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)
        infoMessage.text = if (activityMode == ACTIVITY_SETTING) getString(R.string.pin_setting_guide_message) else getString(R.string.pin_unlock_guide_message) 
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAffinity(this)
    }
    
    private val keyPadClickListener: View.OnClickListener = View.OnClickListener { view ->
        val inputPass = when (view?.id) {
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
                val displayPass = if (activityMode == ACTIVITY_SETTING) inputPass else "-"
                mPasswordView[mCursorIndex]?.text = displayPass

                if (mCursorIndex == 3) {
                    var fullPassword = ""
                    mPassword.map {
                        fullPassword += it
                    }

                    when (activityMode) {
                        ACTIVITY_SETTING -> {
//                            holdCurrentOrientation()
                            setScreenOrientationSensor(false)
                            showAlertDialog(getString(R.string.pin_setting_complete), DialogInterface.OnClickListener { _, _ ->
                                config.aafPinLockEnable = true
                                config.aafPinLockSavedPassword = fullPassword
                                pauseLock()
                                finish()
                            }, false)
                        }
                        ACTIVITY_UNLOCK -> {
                            when (config.aafPinLockSavedPassword == fullPassword) {
                                true -> {
                                    pauseLock()
                                    finish()
                                }
                                false -> {
                                    holdCurrentOrientation()
                                    showAlertDialog(getString(R.string.pin_verification_fail), DialogInterface.OnClickListener { _, _ ->
                                        onBackPressed()
                                    }, false)
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

    companion object {
        const val LAUNCHING_MODE = "launching_mode"
        const val ACTIVITY_SETTING = "activity_setting"
        const val ACTIVITY_UNLOCK = "activity_unlock"
    }
}
