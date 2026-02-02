package me.blog.korn123.easydiary.activities

import android.app.ActivityManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getPermissionString
import me.blog.korn123.easydiary.extensions.getThemeId
import me.blog.korn123.easydiary.extensions.hasPermission
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.updateStatusBarColor
import me.blog.korn123.easydiary.helper.SettingConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import androidx.core.graphics.drawable.toDrawable

/**
 * Created by CHO HANJOONG on 2017-11-25.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

open class BaseSimpleActivity : AppCompatActivity() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var useDynamicTheme = true

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId())
//            setTheme(R.style.AppTheme_AAF)
        }

        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (useDynamicTheme) {
            setTheme(getThemeId())
            updateBackgroundColor(config.screenBackgroundColor)
        }
        updateActionbarColor()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                pauseLock()
                TransitionHelper.finishActivityWithTransition(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == SettingConstants.GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    open fun updateBackgroundColor(color: Int = config.screenBackgroundColor) {
        val mainView: ViewGroup? = getMainViewGroup()
        mainView?.run {
//            setBackgroundColor(ColorUtils.setAlphaComponent(color, 255))
            setBackgroundColor(color)
        }
    }

    open fun getMainViewGroup(): ViewGroup? = null
    //    open fun getBackgroundAlpha(): Int = 255

    fun updateActionbarColor(color: Int = config.primaryColor) {
        supportActionBar?.setBackgroundDrawable(color.toDrawable())
//        supportActionBar?.title = Html.fromHtml("<font color='${color.getContrastColor().toHex()}'>${supportActionBar?.title}</font>")
        updateStatusBarColor(color)

        setTaskDescription(ActivityManager.TaskDescription(null, null, color))
    }

    fun handlePermission(
        permissionId: Int,
        callback: (granted: Boolean) -> Unit,
    ) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this,
                arrayOf(getPermissionString(permissionId)),
                SettingConstants.GENERIC_PERM_HANDLER,
            )
        }
    }
}
