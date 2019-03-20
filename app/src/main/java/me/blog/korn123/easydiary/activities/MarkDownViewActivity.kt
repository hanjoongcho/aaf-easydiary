package me.blog.korn123.easydiary.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_markdown_view.*
import me.blog.korn123.easydiary.R
import br.tiagohm.markdownview.css.styles.Github

class MarkDownViewActivity : EasyDiaryActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_view)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = intent.getStringExtra(OPEN_URL_DESCRIPTION)
            setDisplayHomeAsUpEnabled(true)
        }

        markdownView.loadMarkdownFromUrl(intent.getStringExtra(OPEN_URL_INFO))
        markdownView.addStyleSheet(Github().apply {
            removeRule(".scrollup")
        })
    }

    companion object {
        const val OPEN_URL_INFO = "open_url_info"
        const val OPEN_URL_DESCRIPTION = "open_url_description"
    }
}
