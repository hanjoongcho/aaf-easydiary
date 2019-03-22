package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import br.tiagohm.markdownview.css.styles.Github
import kotlinx.android.synthetic.main.activity_markdown_view.*
import me.blog.korn123.easydiary.R

class MarkDownViewActivity : EasyDiaryActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_view)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = intent.getStringExtra(OPEN_URL_DESCRIPTION)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        markdownView.loadMarkdownFromUrl(intent.getStringExtra(OPEN_URL_INFO))
        markdownView.addStyleSheet(Github()/*InternalStyleSheet()*/.apply {
            removeRule(".scrollup")
            addRule("body", "padding: 0px");
        })

        markdownView.webViewClient =  object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }
        }
    }

    companion object {
        const val OPEN_URL_INFO = "open_url_info"
        const val OPEN_URL_DESCRIPTION = "open_url_description"
    }
}
