package me.blog.korn123.easydiary.extensions

import android.app.Activity
import android.content.Intent
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.easydiary.activities.DiaryLockActivity

/**
 * Created by CHO HANJOONG on 2018-02-10.
 */

fun Activity.pauseLock() {
    val enableLock = CommonUtils.loadBooleanPreference(this, Constants.APP_LOCK_ENABLE)
    if (enableLock) {
        val currentMillis = System.currentTimeMillis()
        CommonUtils.saveLongPreference(this, Constants.SETTING_PAUSE_MILLIS, currentMillis)
    }
}

fun Activity.resumeLock() {
    val enableLock = CommonUtils.loadBooleanPreference(this, Constants.APP_LOCK_ENABLE)
    val pauseMillis = CommonUtils.loadLongPreference(this, Constants.SETTING_PAUSE_MILLIS, 0)
    if (enableLock && pauseMillis != 0L) {
        if (System.currentTimeMillis() - pauseMillis > 1000) {
            val lockDiaryIntent = Intent(this, DiaryLockActivity::class.java)
            startActivity(lockDiaryIntent)
        }
    }
}
