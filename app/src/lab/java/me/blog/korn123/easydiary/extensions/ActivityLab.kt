package me.blog.korn123.easydiary.extensions

import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
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
import me.blog.korn123.easydiary.helper.DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.toDomain
import me.blog.korn123.easydiary.helper.toRealm
import me.blog.korn123.easydiary.models.Diary
import org.apache.commons.codec.binary.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

suspend fun ComponentActivity.pushMarkDown(
    path: String,
    contents: String,
) {
    getToken()
        ?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val baseUrl = "https://api.github.com"
                val retrofitApi: Retrofit =
                    Retrofit
                        .Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                val retrofitApiService = retrofitApi.create(GitHubRepos::class.java)

                // 1. 파일의 sha 값 조회
                var sha: String? = null
                val findCall =
                    retrofitApiService.getContentsDetail(
                        it,
                        "hanjoongcho",
                        "self-development",
                        path,
                    )
                val findResponse = findCall.execute()
                if (findResponse.isSuccessful) {
                    val contentsList = findResponse.body()
                    if (contentsList != null) {
                        sha = contentsList.sha // 파일이 존재하면 sha 값 세팅
                    }
                }

                // 2. CommitRequest 생성 및 푸시
                val commitRequest =
                    CommitRequest(
                        "AUTOMATIC COMMIT: Easy Diary",
                        Base64.encodeBase64String(contents.toByteArray(Charsets.UTF_8)),
                        "main",
                        sha,
                    )
                val call =
                    retrofitApiService.pushFile(
                        it,
                        "hanjoongcho",
                        "self-development",
                        path,
                        commitRequest,
                    )
                val response = call.execute()

                runOnUiThread {
                    if (response.isSuccessful) {
                        val commitResponse = response.body()
                        if (commitResponse != null) {
                            makeToast(
                                "Commit successful: ${commitResponse.commit?.message}",
                                Toast.LENGTH_LONG,
                            )
                        } else {
                            makeToast("Commit response is null", Toast.LENGTH_LONG)
                        }
                    } else {
                        showAlertDialog("Commit failed[$sha]: ${response.errorBody()?.string()}")
                    }
                }
            }
        } ?: run {
        runOnUiThread { makeToast("Token is null") }
    }
}

suspend fun ComponentActivity.getToken(): String? {
    val result = diaryRepository.getDiariesWithPhotos("GitHub Personal Access Token").first()
    return if (result.size == 1) result[0].contents else null
}

fun ComponentActivity.syncMarkDown(
    mBinding: ActivityBaseDevBinding? = null,
    syncMode: String = DEV_SYNC_MARKDOWN_ALL,
    onComplete: () -> Unit = {},
) {
    mBinding?.partialSettingsProgress?.progressContainer?.visibility = View.VISIBLE
    lifecycleScope.launch(Dispatchers.IO) {
        val baseUrl = "https://api.github.com"
        val token: String? = getToken()
        token
            ?.let {
                val retrofitApi: Retrofit =
                    Retrofit
                        .Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                val downloadApi: Retrofit =
                    Retrofit
                        .Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build()
                val retrofitApiService = retrofitApi.create(GitHubRepos::class.java)
                val downloadApiService = downloadApi.create(GitHubRepos::class.java)

                suspend fun fetchContents(
                    path: String,
                    usingPathTitle: Boolean,
                    symbolSequence: Int = DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_DOCS,
                ) {
                    val call =
                        retrofitApiService.findContents(
                            token = it,
                            owner = "hanjoongcho",
                            repo = "self-development",
                            path = path,
                        )
                    val response = call.execute()
                    val contentsItems: List<Contents>? = response.body()
                    contentsItems?.forEach { content ->
                        if (content.download_url == null) {
                            fetchContents(content.path, usingPathTitle, symbolSequence)
                        } else {
                            val title =
                                when (usingPathTitle) {
                                    true -> {
                                        content.path
                                    }

                                    false -> {
                                        if (usingPathTitle) {
                                            content.name
                                        } else {
                                            content.name.split(
                                                ".",
                                            )[0]
                                        }
                                    }
                                }

                            val items = diaryRepository.getDiariesWithPhotos(title).first()

                            fun getUpdateDate(body: String): String {
                                val regex = Regex("""UPDATE:\s(\d{4}-\d{2}-\d{2})""")
                                val matchResult = regex.find(body)
                                if (matchResult != null) {
                                    val dateString = matchResult.groupValues[1]
                                    return dateString
                                } else {
                                    return ""
                                }
                            }

                            val checkedSymbolSequence =
                                when {
                                    title.startsWith("stock/FICS") -> DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_FICS
                                    title.startsWith("stock/ETF") -> DEV_SYNC_SYMBOL_USER_CUSTOM_SYNC_ETF
                                    else -> symbolSequence
                                }

                            if (items.size == 1) {
                                runOnUiThread {
                                    mBinding?.partialSettingsProgress?.message?.text =
                                        "Sync $title…"
                                }
                                val re =
                                    downloadApiService
                                        .downloadContents(
                                            token,
                                            content.download_url,
                                        ).execute()
                                var diary = items[0]
                                diary =
                                    diary.copy(
                                        contents = re.body() ?: "",
                                        symbolSequence = checkedSymbolSequence,
                                    )
                                val updateDateString = getUpdateDate(diary.contents ?: "")
                                if (updateDateString.isNotEmpty()) {
                                    diary =
                                        diary.copy(
                                            currentTimeMillis =
                                                DateUtils.dateStringToTimeStamp(
                                                    updateDateString,
                                                ),
                                            dateString =
                                                DateUtils.timeMillisToDateTime(
                                                    DateUtils.dateStringToTimeStamp(
                                                        updateDateString,
                                                    ),
                                                    DateUtilConstants.DATE_PATTERN_DASH,
                                                ),
                                        )
                                    diaryRepository.updateDiaryWithPhotos(diary)
                                }
                            } else if (items.isEmpty()) {
                                runOnUiThread {
                                    mBinding?.partialSettingsProgress?.message?.text =
                                        "Download $title…"
                                }
                                val re =
                                    downloadApiService
                                        .downloadContents(
                                            token,
                                            content.download_url,
                                        ).execute()
                                diaryRepository.insertDiary(
                                    Diary(
                                        DiaryEditingConstants.DIARY_SEQUENCE_INIT,
                                        System.currentTimeMillis(),
                                        title,
                                        re.body() ?: "",
                                        checkedSymbolSequence,
                                        true,
                                    ).toDomain(),
                                )
                            }
                        }
                    }
                }
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_DEV) {
                    fetchContents(
                        "dev-vault",
                        true,
                    )
                }
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_ETC) {
                    fetchContents(
                        "etc",
                        true,
                    )
                }
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_LIFE) {
                    fetchContents(
                        "life",
                        true,
                    )
                }
//                fetchContents("stock/KOSPI", true, 10031)
//                fetchContents("stock/KOSDAQ", true, 10032)
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_FICS) {
                    fetchContents(
                        "stock/FICS",
                        true,
                        10030,
                    )
                }
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_ETF) {
                    fetchContents(
                        "stock/ETF",
                        true,
                        10033,
                    )
                }
                if (syncMode == DEV_SYNC_MARKDOWN_ALL || syncMode == DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE) {
                    fetchContents(
                        "stock/knowledge",
                        true,
                    )
                }

                if (!listOf(
                        DEV_SYNC_MARKDOWN_ALL,
                        DEV_SYNC_MARKDOWN_DEV,
                        DEV_SYNC_MARKDOWN_ETC,
                        DEV_SYNC_MARKDOWN_LIFE,
                        DEV_SYNC_MARKDOWN_STOCK_FICS,
                        DEV_SYNC_MARKDOWN_STOCK_ETF,
                        DEV_SYNC_MARKDOWN_STOCK_KNOWLEDGE,
                    ).contains(syncMode)
                ) {
                    fetchContents(syncMode, true)
                }

                withContext(Dispatchers.Main) {
                    mBinding?.partialSettingsProgress?.progressContainer?.visibility = View.GONE
                    onComplete()
                }
            } ?: run {
            runOnUiThread { makeToast("Token is null or duplicate") }
        }
    }
}
