package me.blog.korn123.easydiary.activities

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.aafactory.commons.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.activity_lock_setting.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.showAlertDialog


class PinLockActivity : BaseSimpleActivity() {
    private var mPassword = arrayOfNulls<String>(4)
    private var mPasswordView = arrayOfNulls<TextView>(4)
    private var mCursorIndex = 0
    private var activityMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_setting)
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
    }

    override fun onResume() {
        isBackgroundColorFromPrimaryColor = true
        super.onResume()
        FontUtils.setFontsTypeface(applicationContext, assets, null, container)
    }

    override fun getMainViewGroup(): ViewGroup? = container

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
            else -> ""
        }
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
                    showAlertDialog(getString(R.string.pin_number_setting_complete), DialogInterface.OnClickListener { _, _ ->
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
                            mCursorIndex = 0
                            mPasswordView.map { 
                                it?.text = null
                            }
                        }
                    }
                }
            }
        } else {
            mCursorIndex++
        }
    }

    companion object {
        const val LAUNCHING_MODE = "launching_mode"
        const val ACTIVITY_SETTING = "activity_setting"
        const val ACTIVITY_UNLOCK = "activity_unlock"
    }
}
