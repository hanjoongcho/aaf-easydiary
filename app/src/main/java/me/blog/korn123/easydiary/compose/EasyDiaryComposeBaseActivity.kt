package me.blog.korn123.easydiary.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.viewmodel.compose.viewModel
import me.blog.korn123.easydiary.extensions.applyPolicyForRecentApps
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getStatusBarColor
import me.blog.korn123.easydiary.extensions.isBelowVanillaIceCream
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.resumeLock
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.viewmodels.SettingsViewModel

open class EasyDiaryComposeBaseActivity : ComponentActivity() {
    //    val mSettingsViewModel: SettingsViewModel by viewModels()
    lateinit var mSettingsViewModel: SettingsViewModel

    @Composable
    fun initSettingsViewModel(): SettingsViewModel =
        if (LocalInspectionMode.current) {
            SettingsViewModel()
        } else {
            viewModel()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isBelowVanillaIceCream()) {
            @Suppress("DEPRECATION")
            window.statusBarColor = getStatusBarColor(config.primaryColor)
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finishActivityWithTransition()
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        resumeLock()
        applyPolicyForRecentApps()
    }

    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
//        hideSystemBars()
    }

    fun finishActivityWithTransition() {
        TransitionHelper.finishActivityWithTransition(this@EasyDiaryComposeBaseActivity)
    }
}
