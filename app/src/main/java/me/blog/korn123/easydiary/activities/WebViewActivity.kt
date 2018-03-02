package me.blog.korn123.easydiary.activities

import android.webkit.WebView
import io.github.hanjoongcho.commons.activities.BaseWebViewActivity
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.extensions.resumeLock

/**
 * Created by CHO HANJOONG on 2017-02-11.
 */

class WebViewActivity : BaseWebViewActivity() {
    override fun onPause() {
        super.onPause()
        pauseLock()
    }

    override fun onResume() {
        super.onResume()
        resumeLock()
        findViewById<WebView>(R.id.webView).settings.javaScriptEnabled = true
    }
}
