package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import br.tiagohm.markdownview.css.styles.Github
import kotlinx.android.synthetic.main.activity_markdown_view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.confirmPermission
import me.blog.korn123.easydiary.extensions.pauseLock
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.MARKDOWN_DIRECTORY
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN
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

        savedFilePath = "${EasyDiaryUtils.getApplicationDataDirectory(this) + MARKDOWN_DIRECTORY + pageTitle}.md"
        markdownUrl = intent.getStringExtra(OPEN_URL_INFO)
        markdownView.run {
            addStyleSheet(Github()/*InternalStyleSheet()*/.apply {
                removeRule(".scrollup")
                addRule("body", "padding: 0px");
            })
            webViewClient =  object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    progressBar.visibility = View.GONE
                }
            }
        }

        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            openMarkdownFile()
        } else {
            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN)
        }
    }

    private fun openMarkdownFile() {
        when (File(savedFilePath).exists()) {
            true -> {
                markdownView.loadMarkdownFromFile(File(savedFilePath))

            }
            false -> {
                Thread(Runnable { openMarkdownFileAfterDownload(markdownUrl, savedFilePath) }).start()
            }
        }
    }


    private fun openMarkdownFileAfterDownload(fileURL: String, saveFilePath: String) {
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

        runOnUiThread {
            markdownView.loadMarkdownFromFile(File(savedFilePath))
        }
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

                if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                    File(savedFilePath).delete()
                    openMarkdownFile()
                } else {
                    confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pauseLock()
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                openMarkdownFile()
            } else {
                markdownView.loadMarkdownFromUrl(markdownUrl)
            }
        }
    }

    companion object {
        const val OPEN_URL_INFO = "open_url_info"
        const val OPEN_URL_DESCRIPTION = "open_url_description"
    }
}
