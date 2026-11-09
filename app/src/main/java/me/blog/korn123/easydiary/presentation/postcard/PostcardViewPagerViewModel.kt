package me.blog.korn123.easydiary.presentation.postcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import java.io.File
import javax.inject.Inject

data class PostcardViewPagerUiState(
    val postcardFiles: List<File> = emptyList(),
    val initialPosition: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class PostcardViewPagerViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PostcardViewPagerUiState())
    val uiState: StateFlow<PostcardViewPagerUiState> = _uiState.asStateFlow()

    fun initData(initialPosition: Int) {
        _uiState.update { it.copy(initialPosition = initialPosition) }
        loadPostcardFiles()
    }

    private fun loadPostcardFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val files = fetchPostcardFiles()
            _uiState.update {
                it.copy(
                    postcardFiles = files,
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun fetchPostcardFiles(): List<File> =
        withContext(Dispatchers.IO) {
            File(EasyDiaryUtils.getApplicationDataDirectory(getApplication()) + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                ?.filter { it.extension.equals("jpg", true) }
                ?.sortedDescending()
                ?: emptyList()
        }
}
