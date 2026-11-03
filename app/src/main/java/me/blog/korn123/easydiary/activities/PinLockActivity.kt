package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
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
import me.blog.korn123.easydiary.utils.FontUtils

class PinLockActivity : BaseSimpleActivity() {
    private lateinit var binding: ActivityPinLockBinding
    private var password = arrayOfNulls<String>(4)
    private var passwordView = arrayOfNulls<TextView>(4)
    private var cursorIndex = 0
    private var activityMode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        activityMode = intent.getStringExtra(PinLockConstants.LAUNCHING_MODE)

        binding.run {
            passwordView[0] = pass1
            passwordView[1] = pass2
            passwordView[2] = pass3
            passwordView[3] = pass4
            setupKeypadClickListeners()
            setupFingerprintLock()
        }

        applyBottomNavigationInsets(binding.rightContainer)
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
        FontUtils.setFontsTypeface(applicationContext, null, binding.container)
        binding.infoMessage.text = if (activityMode == PinLockConstants.ACTIVITY_SETTING) getString(R.string.pin_setting_guide_message) else getString(R.string.pin_unlock_guide_message)
    }

    private fun ActivityPinLockBinding.setupKeypadClickListeners() {
        val clickListener = View.OnClickListener { view -> handleKeyPadInput(view) }
        listOf(
            num0, num1, num2, num3, num4,
            num5, num6, num7, num8, num9, delete
        ).forEach { it.setOnClickListener(clickListener) }
    }

    private fun ActivityPinLockBinding.setupFingerprintLock() {
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

    private fun handleKeyPadInput(view: View?) {
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

        if (inputPass == "delete") {
            handleDeleteInput()
        } else if (inputPass.isNotEmpty()) {
            handleDigitInput(inputPass)
        }
    }

    private fun handleDeleteInput() {
        if (cursorIndex > 0) cursorIndex--
        passwordView[cursorIndex]?.text = ""
    }

    private fun handleDigitInput(inputPass: String) {
        password[cursorIndex] = inputPass
        val displayPass = if (activityMode == PinLockConstants.ACTIVITY_SETTING) inputPass else "-"
        passwordView[cursorIndex]?.text = displayPass

        if (cursorIndex == 3) {
            processCompletedPassword()
        } else {
            cursorIndex++
        }
    }

    private fun processCompletedPassword() {
        val fullPassword = password.joinToString(separator = "") { it ?: "" }

        when (activityMode) {
            PinLockConstants.ACTIVITY_SETTING -> handleSettingMode(fullPassword)
            PinLockConstants.ACTIVITY_UNLOCK -> handleUnlockMode(fullPassword)
        }
    }

    private fun handleSettingMode(fullPassword: String) {
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

    private fun handleUnlockMode(fullPassword: String) {
        if (config.aafPinLockSavedPassword == fullPassword) {
            pauseLock()
            finish()
        } else {
            holdCurrentOrientation()
            showAlertDialog(
                message = getString(R.string.pin_verification_fail),
                positiveListener = { _, _ ->
                    ActivityCompat.finishAffinity(this@PinLockActivity)
                },
                negativeListener = null,
                dialogMode = DialogMode.DEFAULT,
                cancelable = false,
            )
        }
    }
}
