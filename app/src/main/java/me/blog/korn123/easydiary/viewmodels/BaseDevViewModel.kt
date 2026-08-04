package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.data.repository.DiaryRepository
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.makeToast
import javax.inject.Inject
import kotlin.rem

@HiltViewModel
class BaseDevViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
    ) : AndroidViewModel(application) {
        val config = application.config
        var symbol by mutableIntStateOf(1)
        var locationInfo by mutableStateOf("N/A")
        var coroutine1Console by mutableStateOf("")
        var isLoading by mutableStateOf(false)
        var profilePicUri by mutableStateOf<String?>(null)

        fun plus() {
            symbol = symbol.plus(1)
        }

        fun addDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.insertDiary(diary)
            }
        }

        suspend fun addAllDiaries(diaries: List<Diary>): Int {
            var count = 0

            // 1. UI 상태 변경 및 시작은 메인 스레드에서 (withContext를 쓰지 않거나 Main 지정)
            isLoading = true
            coroutine1Console = "Migration started..."

            try {
                // 2. 무거운 DB 작업만 IO 스레드에서 일괄 처리 또는 반복 처리
                withContext(Dispatchers.IO) {
                    diaries.forEachIndexed { index, diary ->
                        diaryRepository.insertDiary(diary)

                        // 주의: 루프 안에서 너무 자주 메인 스레드로 전환하면 성능이 떨어질 수 있습니다.
                        // 10개 단위이므로 메인 스레드로 전환해 UI를 업데이트해 줍니다.
                        if (index % 10 == 0 || index == diaries.lastIndex) {
                            withContext(Dispatchers.Main) {
                                coroutine1Console = "Migrating... ${index + 1} / ${diaries.size}"
                            }
                        }
                        count++
                    }
                }

                // 3. 완료 상태도 메인 스레드에서
                coroutine1Console = "Migration completed: ${diaries.size} diaries"
            } catch (e: Exception) {
                coroutine1Console = "Migration failed: ${e.message}"
            } finally {
                // 4. 로딩 종료도 메인 스레드에서
                isLoading = false
            }

            return count
        }

        suspend fun getDiaryCount(): Int = diaryRepository.getAllDiaries().first().size

        suspend fun getLatestDiary(): Diary? =
            diaryRepository
                .getAllDiaries()
                .first()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

        suspend fun getLatestDiaryWithPhotos(): Diary? =
            diaryRepository
                .getDiariesWithPhotos()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

        fun deleteAllDiaries() {
            viewModelScope.launch {
                diaryRepository.deleteAllDiaries()
            }
        }

        var enableDebugOptionVisibleDiarySequence by mutableStateOf(config.enableDebugOptionVisibleDiarySequence)
            private set // We control the internal state

        fun toggleDebugOptionVisibleDiarySequence() {
            val newValue = enableDebugOptionVisibleDiarySequence.not()
            config.enableDebugOptionVisibleDiarySequence = newValue
            enableDebugOptionVisibleDiarySequence = newValue
        }

        var enableDebugOptionVisibleAlarmSequence by mutableStateOf(config.enableDebugOptionVisibleAlarmSequence)
            private set // We control the internal state

        fun toggleDebugOptionVisibleAlarmSequence() {
            val newValue = enableDebugOptionVisibleAlarmSequence.not()
            config.enableDebugOptionVisibleAlarmSequence = newValue
            enableDebugOptionVisibleAlarmSequence = newValue
        }

        var enableDebugOptionVisibleTreeStatus by mutableStateOf(config.enableDebugOptionVisibleTreeStatus)
            private set // We control the internal state

        fun toggleDebugOptionVisibleTreeStatus() {
            val newValue = enableDebugOptionVisibleTreeStatus.not()
            config.enableDebugOptionVisibleTreeStatus = newValue
            enableDebugOptionVisibleTreeStatus = newValue
        }

        var enableDebugOptionVisibleChartStock by mutableStateOf(config.enableDebugOptionVisibleChartStock)
            private set // We control the internal state

        fun toggleDebugOptionVisibleChartStock() {
            val newValue = enableDebugOptionVisibleChartStock.not()
            config.enableDebugOptionVisibleChartStock = newValue
            enableDebugOptionVisibleChartStock = newValue
        }

        var enableDebugOptionVisibleChartWeight by mutableStateOf(config.enableDebugOptionVisibleChartWeight)
            private set // We control the internal state

        fun toggleDebugOptionVisibleChartWeight() {
            val newValue = enableDebugOptionVisibleChartWeight.not()
            config.enableDebugOptionVisibleChartWeight = newValue
            enableDebugOptionVisibleChartWeight = newValue
        }

        var enableDebugOptionToastLocation by mutableStateOf(config.enableDebugOptionToastLocation)
            private set // We control the internal state

        fun toggleDebugOptionToastLocation() {
            val newValue = enableDebugOptionToastLocation.not()
            config.enableDebugOptionToastLocation = newValue
            enableDebugOptionToastLocation = newValue
        }

        var enableDebugOptionVisibleTemporaryDiary by mutableStateOf(config.enableDebugOptionVisibleTemporaryDiary)
            private set

        fun toggleDebugOptionVisibleTemporaryDiary() {
            val newValue = enableDebugOptionVisibleTemporaryDiary.not()
            config.enableDebugOptionVisibleTemporaryDiary = newValue
            enableDebugOptionVisibleTemporaryDiary = newValue
        }

        var enableDebugOptionVisibleFontPreviewEmoji by mutableStateOf(config.enableDebugOptionVisibleFontPreviewEmoji)
            private set

        fun toggleDebugOptionVisibleFontPreviewEmoji() {
            val newValue = enableDebugOptionVisibleFontPreviewEmoji.not()
            config.enableDebugOptionVisibleFontPreviewEmoji = newValue
            enableDebugOptionVisibleFontPreviewEmoji = newValue
        }
    }
