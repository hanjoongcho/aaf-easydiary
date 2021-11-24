package me.blog.korn123.easydiary.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.markwon.utils.ColorUtils
import io.noties.markwon.utils.Dip
import io.noties.prism4j.Prism4j
import io.noties.prism4j.annotations.PrismBundle
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityMarkdownViewerBinding
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

@PrismBundle(include = ["java", "kotlin", "javascript"], grammarLocatorClassName = ".GrammarLocatorSourceCode")
class MarkDownViewerActivity : EasyDiaryActivity() {
    private lateinit var mBinding: ActivityMarkdownViewerBinding
    private lateinit var savedFilePath: String
    private lateinit var markdownUrl: String
    private val mPrism4j = Prism4j(GrammarLocatorSourceCode())
    private var mForceAppendCodeBlock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMarkdownViewerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        val pageTitle = intent.getStringExtra(OPEN_URL_DESCRIPTION)
        mForceAppendCodeBlock = intent.getBooleanExtra(FORCE_APPEND_CODE_BLOCK, true)

        supportActionBar?.run {
            title = pageTitle
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        savedFilePath = "${EasyDiaryUtils.getApplicationDataDirectory(this) + MARKDOWN_DIRECTORY + pageTitle}.md"
        markdownUrl = intent.getStringExtra(OPEN_URL_INFO)!!
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
                runOnUiThread { mBinding.progressBar.visibility = View.GONE }
                initMarkdown()
            }
            false -> {
                Thread(Runnable { openMarkdownFileAfterDownload(markdownUrl, savedFilePath) }).start()
            }
        }
    }

    private fun initMarkdown() {
        Markwon.builder(this)
                .usePlugin(TablePlugin.create { builder: TableTheme.Builder ->
                    val dip: Dip = Dip.create(this)
                    builder
                            .tableBorderWidth(dip.toPx(2))
                            .tableBorderColor(Color.BLACK)
                            .tableCellPadding(dip.toPx(4))
                            .tableHeaderRowBackgroundColor(ColorUtils.applyAlpha(Color.BLUE, 80))
//                            .tableEvenRowBackgroundColor(ColorUtils.applyAlpha(Color.GREEN, 80))
//                            .tableOddRowBackgroundColor(ColorUtils.applyAlpha(Color.BLUE, 80))
                })
                .usePlugin(SyntaxHighlightPlugin.create(mPrism4j, Prism4jThemeDefault.create(0)))
                .build().apply { setMarkdown(mBinding.markdownView, readSavedFile()) }
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
                val lines = IOUtils.readLines(inputStream, "UTF-8")
                if (mForceAppendCodeBlock) {
                    lines.add(0, "```java")
                    lines.add("```")
                }
                FileUtils.writeLines(File(saveFilePath), "UTF-8", lines)
                inputStream.close()
            }
            httpConn.disconnect()

            runOnUiThread {
                mBinding.progressBar.visibility = View.GONE
                initMarkdown()
            }
        } else {
            runOnUiThread {
                mBinding.progressBar.visibility = View.GONE
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
                mBinding.progressBar.visibility = View.VISIBLE

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
        const val FORCE_APPEND_CODE_BLOCK = "force_append_code_block"
    }
}
