package me.blog.korn123.easydiary.extensions

import android.app.Activity
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity
import me.blog.korn123.easydiary.api.models.CommitRequest
import me.blog.korn123.easydiary.api.models.Contents
import me.blog.korn123.easydiary.api.services.GitHubRepos
import me.blog.korn123.easydiary.databinding.ActivityBaseDevBinding
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_ALL
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_DEV
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_ETC
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_LIFE
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_ETF
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_FICS
import me.blog.korn123.easydiary.helper.DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

fun Activity.pushMarkDown(path: String, contents: String) {
    val token = EasyDiaryDbHelper.getToken()

    CoroutineScope(Dispatchers.IO).launch {
        val baseUrl = "https://api.github.com"
        val retrofitApi: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitApiService = retrofitApi.create(GitHubRepos::class.java)

        // 1. 파일의 sha 값 조회
        var sha: String? = null
        val findCall =
            retrofitApiService.getContentsDetail(token!!, "hanjoongcho", "self-development", path)
        val findResponse = findCall.execute()
        if (findResponse.isSuccessful) {
            val contentsList = findResponse.body()
            if (contentsList != null) {
                sha = contentsList.sha // 파일이 존재하면 sha 값 세팅
            }
        }

        // 2. CommitRequest 생성 및 푸시
        val commitRequest = CommitRequest(
            "AUTOMATIC COMMIT: Easy Diary",
            Base64.encodeBase64String(contents.toByteArray(Charsets.UTF_8)),
            "main",
            sha
        )
        val call = retrofitApiService.pushFile(
            token,
            "hanjoongcho",
            "self-development",
            path,
            commitRequest
        )
        val response = call.execute()

        runOnUiThread {
            if (response.isSuccessful) {
                val commitResponse = response.body()
                if (commitResponse != null) {
                    makeToast(
                        "Commit successful: ${commitResponse.commit?.message}",
                        Toast.LENGTH_LONG
                    )
                } else {
                    makeToast("Commit response is null", Toast.LENGTH_LONG)
                }
            } else {
                showAlertDialog("Commit failed[${sha}]: ${response.errorBody()?.string()}")
            }
        }
    }
}

fun Activity.syncMarkDown(mBinding: ActivityBaseDevBinding? = null, syncMode: String = DEV_SYNC_MARKDOWN_ALL, onComplete: () -> Unit = {}) {
    mBinding?.partialSettingsProgress?.progressContainer?.visibility = View.VISIBLE
    CoroutineScope(Dispatchers.IO).launch {
        val baseUrl = "https://api.github.com"
        var token: String? = null
        var tokenInfo: List<Diary>?
        var size = 0
        EasyDiaryDbHelper.getTemporaryInstance().run {
            tokenInfo = EasyDiaryDbHelper.findDiary("GitHub Personal Access Token", false, 0, 0, 0, this)
            tokenInfo?.let {
                size = it.size
                if (size > 0) token = it[0].contents
            }
            close()
        }

        if (size != 1) {
            runOnUiThread { makeToast("No Data") }
        } else {
            val retrofitApi: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val downloadApi: Retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
            val retrofitApiService = retrofitApi.create(GitHubRepos::class.java)
            val downloadApiService = downloadApi.create(GitHubRepos::class.java)
            fun fetchContents(path: String, usingPathTitle: Boolean, symbolSequence: Int = DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS) {
                val call = retrofitApiService.findContents(token!!, "hanjoongcho", "self-development", path)
                val response = call.execute()
                val contentsItems: List<Contents>? = response.body()
                contentsItems?.forEach { content ->
                    if (content.download_url == null) {
                        fetchContents(content.path, usingPathTitle, symbolSequence)
                    } else {
                        EasyDiaryDbHelper.getTemporaryInstance().run {
                            val title = when (usingPathTitle) {
                                true -> content.path
                                false -> if (usingPathTitle) content.name else content.name.split(".")[0]
                            }

                            val items = EasyDiaryDbHelper.findMarkdownSyncTargetDiary(title, this)
                            fun getUpdateDate(body: String): String {
                                val regex = Regex("""UPDATE:\s(\d{4}-\d{2}-\d{2})""")
                                val matchResult = regex.find(body)
                                if (matchResult != null) {
                                    val dateString = matchResult.groupValues[1]
                                    return dateString
                                } else {
                                    return "";
                                }
                            }

                            val checkedSymbolSequence = when {
                                title.startsWith("stock/ETF") -> DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
                                else -> symbolSequence
                            }

                            if (items.size == 1) {
                                runOnUiThread {
                                    mBinding?.partialSettingsProgress?.message?.text = "Sync ${title}…"
                                }
                                val re = downloadApiService.downloadContents(token!!, content.download_url).execute()
                                val diary = items[0]
                                this.beginTransaction()
                                diary.contents = re.body()
                                diary.weather = checkedSymbolSequence
                                val updateDateString = getUpdateDate(diary.contents!!)
                                if (updateDateString.isNotEmpty()) {
                                    diary.currentTimeMillis = DateUtils.dateStringToTimeStamp(updateDateString)
                                    diary.updateDateString()
                                }
                                this.commitTransaction()
                            } else if (items.isEmpty()) {
                                runOnUiThread {
                                    mBinding?.partialSettingsProgress?.message?.text = "Download ${title}…"
                                }
                                val re = downloadApiService.downloadContents(token!!, content.download_url).execute()
                                EasyDiaryDbHelper.insertDiary(Diary(
                                    BaseDiaryEditingActivity.DIARY_SEQUENCE_INIT,
                                    System.currentTimeMillis()
                                    , title
                                    , re.body()!!
                                    , checkedSymbolSequence
                                    ,true
                                ), this)
                            }
                            this.close()
                        }
                    }
                }
            }
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_DEV) fetchContents("dev", true)
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_ETC) fetchContents("etc", true)
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_LIFE) fetchContents("life", true)
//                fetchContents("stock/KOSPI", true, 10031)
//                fetchContents("stock/KOSDAQ", true, 10032)
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_FICS) fetchContents("stock/FICS", true, 10030)
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_ETF) fetchContents("stock/ETF", true, 10033)
            if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE) fetchContents("stock/knowledge", true)

            if (!listOf(
                    DEV_SYNC_MARKDOWN_ALL,
                    DEV_SYNC_MARKDOWN_DEV,
                    DEV_SYNC_MARKDOWN_ETC,
                    DEV_SYNC_MARKDOWN_LIFE,
                    DEV_SYNC_MARKDOWN_STOCK_FICS,
                    DEV_SYNC_MARKDOWN_STOCK_ETF,
                    DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE
                ).contains(syncMode)
            ) {
                fetchContents(syncMode, true)
            }

            withContext(Dispatchers.Main) {
                mBinding?.partialSettingsProgress?.progressContainer?.visibility = View.GONE
                onComplete()
            }
        }
    }
}