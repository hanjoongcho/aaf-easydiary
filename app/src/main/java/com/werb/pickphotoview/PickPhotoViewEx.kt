package com.werb.pickphotoview

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

import com.werb.pickphotoview.model.PickModel
import me.blog.korn123.easydiary.R

class PickPhotoViewEx private constructor(private val builder: Builder) {

    private fun start(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(builder.activity, PickPhotoActivity::class.java)
        activityResultLauncher.launch(intent)
        builder.activity.overridePendingTransition(R.anim.activity_anim_bottom_to_top, R.anim.activity_anim_not_change)
    }

    class Builder(val activity: Activity) {

        private val pickModel = PickModel()

        fun setPickPhotoSize(photoSize: Int): Builder {
            pickModel.pickPhotoSize = photoSize
            return this
        }

        fun setHasPhotoSize(hasSize: Int): Builder {
            pickModel.hasPhotoSize = hasSize
            return this
        }

        fun setAllPhotoSize(allSize: Int): Builder {
            pickModel.allPhotoSize = allSize
            return this
        }

        fun setSpanCount(spanCount: Int): Builder {
            pickModel.spanCount = spanCount
            return this
        }

        fun setShowCamera(showCamera: Boolean): Builder {
            pickModel.isShowCamera = showCamera
            return this
        }

        /** clickSelectable used with photoSize == 1  */
        fun setClickSelectable(clickSelectable: Boolean): Builder {
            pickModel.isClickSelectable = clickSelectable
            return this
        }

        fun setToolbarColor(toolbarColor: Int): Builder {
            pickModel.toolbarColor = toolbarColor
            return this
        }

        fun setStatusBarColor(statusBarColor: Int): Builder {
            pickModel.statusBarColor = statusBarColor
            return this
        }

        fun setToolbarTextColor(toolbarTextColor: Int): Builder {
            pickModel.toolbarTextColor = toolbarTextColor
            return this
        }

        fun setSelectIconColor(selectIconColor: Int): Builder {
            pickModel.selectIconColor = selectIconColor
            return this
        }

        fun setLightStatusBar(lightStatusBar: Boolean): Builder {
            pickModel.lightStatusBar = lightStatusBar
            return this
        }

        fun setShowGif(showGif: Boolean): Builder {
            pickModel.isShowGif = showGif
            return this
        }

//        fun setShowVideo(showVideo: Boolean): Builder {
//            pickModel.isShowVideo = showVideo
//            return this
//        }

        private fun create(): PickPhotoViewEx {
            GlobalData.model = pickModel
            return PickPhotoViewEx(this)
        }

        fun start(activityResultLauncher: ActivityResultLauncher<Intent>) {
            create().start(activityResultLauncher)
        }
    }
}