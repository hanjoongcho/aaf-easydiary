package me.blog.korn123.easydiary.presentation.markdown

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.isConnectedOrConnecting
import me.blog.korn123.easydiary.helper.MARKDOWN_DIRECTORY
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class MarkDownUiState(
    val pageTitle: String = "",
    val markdownUrl: String = "",
    val forceAppendCodeBlock: Boolean = true,
    val savedFilePath: String = "",
    val markdownText: String = "",
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
)

@HiltViewModel
class MarkDownViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MarkDownUiState())
    val uiState: StateFlow<MarkDownUiState> = _uiState.asStateFlow()

    fun initData(pageTitle: String?, markdownUrl: String?, forceAppendCodeBlock: Boolean) {
        val title = pageTitle ?: String.EMPTY
        val url = markdownUrl ?: String.EMPTY
        val path = "${EasyDiaryUtils.getApplicationDataDirectory(getApplication()) + MARKDOWN_DIRECTORY + title}.md"
        _uiState.update {
            it.copy(
                pageTitle = title,
                markdownUrl = url,
                forceAppendCodeBlock = forceAppendCodeBlock,
                savedFilePath = path,
            )
        }
    }

    fun loadMarkdownFile() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val file = File(currentState.savedFilePath)
            if (file.exists()) {
                _uiState.update { it.copy(isLoading = false) }
                readMarkdownFile()
            } else {
                downloadAndSaveMarkdownFile()
            }
        }
    }

    fun refreshMarkdownFile() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val path = _uiState.value.savedFilePath
            if (path.isNotEmpty()) {
                File(path).delete()
            }
            loadMarkdownFile()
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun readMarkdownFile() {
        val path = _uiState.value.savedFilePath
        val content = try {
            val lines = File(path).readLines(Charsets.UTF_8)
            buildString {
                for (line in lines) {
                    append(line)
                    append(System.lineSeparator())
                }
            }.also {
                Log.i("aaf-t", it)
            }
        } catch (e: FileNotFoundException) {
            e.message ?: "File not found"
        }
        _uiState.update {
            it.copy(markdownText = content, isLoading = false)
        }
    }

    private fun downloadAndSaveMarkdownFile() {
        _uiState.update { it.copy(isLoading = true) }
        val currentState = _uiState.value
        val fileURL = currentState.markdownUrl
        val saveFilePath = currentState.savedFilePath
        val forceAppendCodeBlock = currentState.forceAppendCodeBlock

        if (getApplication<Application>().isConnectedOrConnecting()) {
            try {
                val url = URL(fileURL)
                val httpConn = url.openConnection() as HttpURLConnection
                val responseCode = httpConn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val lines = httpConn.inputStream
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readLines() }
                        .toMutableList()
                    if (forceAppendCodeBlock) {
                        lines.add(0, "```java")
                        lines.add("```")
                    }
                    File(saveFilePath).printWriter(Charsets.UTF_8).use { writer ->
                        lines.forEach { writer.println(it) }
                    }
                }
                httpConn.disconnect()
                readMarkdownFile()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, toastMessage = e.message ?: "Error downloading markdown")
                }
            }
        } else {
            _uiState.update {
                it.copy(isLoading = false, toastMessage = "Network is not available.")
            }
        }
    }
}
