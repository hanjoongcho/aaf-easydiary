package me.blog.korn123.easydiary.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.noties.markwon.Markwon
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import kotlinx.android.synthetic.main.activity_markdown_view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EXTERNAL_STORAGE_PERMISSIONS
import me.blog.korn123.easydiary.helper.MARKDOWN_DIRECTORY
import me.blog.korn123.easydiary.helper.REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle

@PrismBundle(include = ["java", "kotlin"], grammarLocatorClassName = ".GrammarLocatorSourceCode")
class MarkDownViewActivity : EasyDiaryActivity() {
    private lateinit var savedFilePath: String
    private lateinit var markdownUrl: String
    private lateinit var mMarkDown: Markwon
    private val mPrism4j = Prism4j(GrammarLocatorSourceCode())

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

        mMarkDown = Markwon.create(this)
        savedFilePath = "${EasyDiaryUtils.getApplicationDataDirectory(this) + MARKDOWN_DIRECTORY + pageTitle}.md"
        markdownUrl = intent.getStringExtra(OPEN_URL_INFO)
//        markdownView.run {
//            webViewClient =  object : WebViewClient() {
//                override fun onPageFinished(view: WebView, url: String) {
//                    progressBar.visibility = View.GONE
//                }
//            }
//        }

        if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
            openMarkdownFile()
        } else {
            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE_WITH_MARKDOWN)
        }
    }

    private fun openMarkdownFile() {
        when (File(savedFilePath).exists()) {
            true -> {
                runOnUiThread { progressBar.visibility = View.GONE }
                mMarkDown.setParsedMarkdown(markdownView, Markwon.builder(this)
                        .usePlugin(SyntaxHighlightPlugin.create(mPrism4j, Prism4jThemeDefault.create(0)))
                        .build().toMarkdown(readSavedFile())
                )
            }
            false -> {
                Thread(Runnable { openMarkdownFileAfterDownload(markdownUrl, savedFilePath) }).start()
            }
        }
    }


    private fun openMarkdownFileAfterDownload(fileURL: String, saveFilePath: String) {
        if (isConnectedOrConnecting()) {
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
                progressBar.visibility = View.GONE
                mMarkDown.setParsedMarkdown(markdownView, Markwon.builder(this)
                        .usePlugin(SyntaxHighlightPlugin.create(mPrism4j, Prism4jThemeDefault.create(0)))
                        .build().toMarkdown(readSavedFile())
                )
            }
        } else {
            runOnUiThread {
                progressBar.visibility = View.GONE
                makeToast("Network is not available.")
            }
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
                makeSnackBar("Permission denied")
            }
        }
    }

    private fun readSavedFile(): String {
        val sb = StringBuilder()
        try {
            val lines = IOUtils.readLines(FileInputStream(File(savedFilePath)), "UTF-8")
            lines.map {
                sb.append(it)
                sb.append(System.getProperty("line.separator"))
            }
            Log.i("aaf-t", sb.toString())
        } catch (e: FileNotFoundException) {
            sb.append(e.message)
        }
        return sb.toString()
    }

    companion object {
        const val OPEN_URL_INFO = "open_url_info"
        const val OPEN_URL_DESCRIPTION = "open_url_description"
    }
}
