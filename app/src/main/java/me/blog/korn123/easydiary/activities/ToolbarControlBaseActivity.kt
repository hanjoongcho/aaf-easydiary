/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks
import com.github.ksoichiro.android.observablescrollview.ScrollState
import com.github.ksoichiro.android.observablescrollview.Scrollable
import com.nineoldandroids.animation.ValueAnimator
import com.nineoldandroids.view.ViewHelper
import io.github.aafactory.commons.utils.CommonUtils
import me.blog.korn123.easydiary.databinding.ActivityDiaryMainBinding
import me.blog.korn123.easydiary.extensions.config


abstract class ToolbarControlBaseActivity<S : Scrollable> : EasyDiaryActivity(), ObservableScrollViewCallbacks {
    protected lateinit var binding: ActivityDiaryMainBinding
    private var mScrollable: S? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        mScrollable = createScrollable()
        mScrollable?.setScrollViewCallbacks(this)
    }

    protected abstract fun createScrollable(): S

    override fun onScrollChanged(scrollY: Int, firstScroll: Boolean, dragging: Boolean) {}

    override fun onDownMotionEvent() {}

    override fun onUpOrCancelMotionEvent(scrollState: ScrollState?) {
        scrollState?.let {
            Log.e("DEBUG", "onUpOrCancelMotionEvent: $scrollState ${toolbarIsHidden()}")
            if (!keypadIsShown()) {
                if (scrollState == ScrollState.UP) {
                    if (toolbarIsShown()) {
                        hideToolbar()
                    }
                } else if (scrollState == ScrollState.DOWN) {
                    if (toolbarIsHidden()) {
                        showToolbar()
                    }
                } else if (scrollState == ScrollState.STOP) {
                    if (toolbarIsHidden()) {
                        showToolbar()
                    }
                }
            }
        }
    }

    private fun toolbarIsShown(): Boolean {
        return ViewHelper.getTranslationY(binding.appBar).toInt() == 0
    }

    private fun toolbarIsHidden(): Boolean {
        return ViewHelper.getTranslationY(binding.appBar).toInt() == -binding.appBar.height
    }

    private fun showToolbar() {
        moveToolbar(0F)
        if (config.enableCardViewPolicy) binding.searchCard.useCompatPadding = true
    }

    private fun hideToolbar() {
        moveToolbar(-binding.appBar.height.toFloat())
        binding.searchCard.useCompatPadding = false
    }

    private fun moveToolbar(toTranslationY: Float) {
        if (ViewHelper.getTranslationY(binding.appBar) == toTranslationY) {
            return
        }
        val animator = ValueAnimator.ofFloat(ViewHelper.getTranslationY(binding.appBar), toTranslationY).setDuration(500)
        animator.addUpdateListener { animation ->
            val translationY = animation.animatedValue as Float
            ViewHelper.setTranslationY(binding.appBar, translationY)
            ViewHelper.setTranslationY(binding.mainHolder as View?, translationY)
            val lp = (binding.mainHolder as View).layoutParams as FrameLayout.LayoutParams
            lp.height = (-translationY).toInt() + getScreenHeight() - lp.topMargin
            (binding.mainHolder as View).requestLayout()
        }
        animator.start()
    }

    private fun getScreenHeight(): Int {
        return findViewById<View>(android.R.id.content).height
    }

    private fun keypadIsShown(): Boolean {
        var isShow = false
        val rootView = findViewById<View>(android.R.id.content)
        val heightDiff = rootView.rootView.height - rootView.height
        if (heightDiff > CommonUtils.dpToPixel(this, 200F)) {
            isShow = true
        }
        Log.i("keypadIsShown", "$heightDiff, ${CommonUtils.dpToPixel(this, 200F)}")

        return isShow
    }
}
