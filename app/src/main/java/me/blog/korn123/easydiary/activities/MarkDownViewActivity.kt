package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import br.tiagohm.markdownview.css.styles.Github
import kotlinx.android.synthetic.main.activity_markdown_view.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.MARKDOWN_DIRECTORY
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class MarkDownViewActivity : EasyDiaryActivity() {
    private lateinit var savedFilePath: String
    private lateinit var markdownUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_markdown_view)
        setSupportActionBar(toolbar)
        val pageTitle = intent.getStringExtra(OPEN_URL_DESCRIPTION)
        supportActionBar?.run {
            title = pageTitle
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        savedFilePath = "${Environment.getExternalStorageDirectory().absolutePath + MARKDOWN_DIRECTORY + pageTitle}.md"
        markdownUrl = intent.getStringExtra(OPEN_URL_INFO)

        markdownView.addStyleSheet(Github()/*InternalStyleSheet()*/.apply {
            removeRule(".scrollup")
            addRule("body", "padding: 0px");
        })
        when (File(savedFilePath).exists()) {
            true -> {
                markdownView.loadMarkdownFromFile(File(savedFilePath))
                markdownView.webViewClient =  object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        progressBar.visibility = View.GONE
                    }
                }
            }
            false -> {
                markdownView.loadMarkdownFromUrl(markdownUrl)
                markdownView.webViewClient =  object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        progressBar.visibility = View.GONE
                        Thread(Runnable { downloadFile(markdownUrl, savedFilePath) }).start()
                    }
                }
            }
        }
    }

    fun downloadFile(fileURL: String, saveFilePath: String) {
        val url = URL(fileURL)
        val httpConn = url.openConnection() as HttpURLConnection
        val responseCode = httpConn.responseCode
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // opens input stream from the HTTP connection
            val inputStream = httpConn.inputStream
            FileUtils.copyInputStreamToFile(inputStream, File(saveFilePath))
            inputStream.close()
        }
        httpConn.disconnect()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.markdown_view, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.update -> {
                Thread.sleep(200) /*wait ripple animation*/
                progressBar.visibility = View.VISIBLE
                Thread(Runnable {
                    downloadFile(markdownUrl, savedFilePath)
                    runOnUiThread {
                        markdownView.loadMarkdownFromFile(File(savedFilePath))
                        markdownView.webViewClient =  object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                progressBar.visibility = View.GONE
                            }
                        }
                    }
                }).start()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val OPEN_URL_INFO = "open_url_info"
        const val OPEN_URL_DESCRIPTION = "open_url_description"
    }
}
