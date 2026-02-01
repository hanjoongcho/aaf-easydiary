package me.blog.korn123.easydiary.helper

import android.app.Activity
import android.content.Intent
import me.blog.korn123.easydiary.R

/**
 * Created by CHO HANJOONG on 2018-01-02.
 */

class TransitionHelper {
    companion object {
        fun startActivityWithTransition(
            activity: Activity?,
            intent: Intent,
            type: Int = TransitionConstants.DEFAULT,
        ) {
            activity?.run {
                startActivity(intent)
                when (type) {
                    TransitionConstants.DEFAULT -> overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    TransitionConstants.BOTTOM_TO_TOP -> overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
                }
            }
        }

        fun finishActivityWithTransition(
            activity: Activity?,
            type: Int = TransitionConstants.DEFAULT,
        ) {
            activity?.run {
                finish()
                when (type) {
                    TransitionConstants.DEFAULT -> overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    TransitionConstants.TOP_TO_BOTTOM -> overridePendingTransition(R.anim.stay, R.anim.slide_in_down)
                }
            }
        }
    }
}
