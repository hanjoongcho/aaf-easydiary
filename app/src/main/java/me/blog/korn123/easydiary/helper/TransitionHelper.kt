package me.blog.korn123.easydiary.helper

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import me.blog.korn123.easydiary.R

/**
 * Created by CHO HANJOONG on 2018-01-02.
 */

class TransitionHelper {

    companion object {
        const val DEFAULT = 0
        const val BOTTOM_TO_TOP = 1
        const val TOP_TO_BOTTOM = 2

        fun startActivityWithTransition(activity: Activity?, intent: Intent, type: Int = DEFAULT) {
            activity?.run {
                startActivity(intent)
                when (type) {
                    DEFAULT -> overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    BOTTOM_TO_TOP -> overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
            }
        }

        fun finishActivityWithTransition(activity: Activity?, type: Int = DEFAULT) {
            activity?.run {
                finish()
                when (type) {
                    DEFAULT -> overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    TOP_TO_BOTTOM -> overridePendingTransition(R.anim.stay, R.anim.slide_in_down)
                }
            }
        }
    }
}