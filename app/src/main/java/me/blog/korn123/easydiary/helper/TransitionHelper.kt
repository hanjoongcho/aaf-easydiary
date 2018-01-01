package me.blog.korn123.easydiary.helper

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.view.View

/**
 * Created by CHO HANJOONG on 2018-01-02.
 */

class TransitionHelper {
    companion object {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun startActivityWithTransition(activity: Activity, intent: Intent, bundle: Bundle?) {
            ActivityCompat.startActivity(activity, intent, bundle)
        }

        @JvmStatic
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun startActivityWithTransition(activity: Activity, intent: Intent) {

            val animationBundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    *createSafeTransitionParticipants(activity,
                            false)).toBundle()

            ActivityCompat.startActivity(activity, intent, animationBundle)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun startActivityWithTransition(activity: Activity, targetClass: Class<*>) {

            val animationBundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    *createSafeTransitionParticipants(activity,
                            false)).toBundle()

            val intent = Intent(activity, targetClass)
            ActivityCompat.startActivity(activity, intent, animationBundle)
        }

        /**
         * Create the transition participants required during a activity transition while
         * avoiding glitches with the system UI.

         * @param activity The activity used as start for the transition.
         *
         * @param includeStatusBar If false, the status bar will not be added as the transition
         * participant.
         *
         * @return All transition participants.
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun createSafeTransitionParticipants(activity: Activity,
                                             includeStatusBar: Boolean,
                                             vararg others: Pair<View, String>
        ): Array<Pair<View, String>> {
            // Avoid system UI glitches as described here:
            // https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb

            return ArrayList<Pair<View, String>>(3).apply {
                if (includeStatusBar) {
                    addViewById(activity, android.R.id.statusBarBackground, this)
                }
                addViewById(activity, android.R.id.navigationBarBackground, this)
                addAll(others.toList())

            }.toTypedArray()
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun addViewById(activity: Activity,
                                @IdRes viewId: Int,
                                participants: ArrayList<Pair<View, String>>) {
            val view = activity.window.decorView.findViewById<View>(viewId)
            view?.transitionName?.let { participants.add(Pair(view, it)) }
        }
    }
}