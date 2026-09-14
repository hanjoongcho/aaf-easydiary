package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import java.io.File
import java.time.YearMonth
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.map

@HiltViewModel
class DiaryViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
    ) : AndroidViewModel(application) {
        var isLoading by mutableStateOf(false)
        var loadingMessage by mutableStateOf<String?>(null)

        suspend fun addAllDiaries(diaries: List<Diary>) {
            diaryRepository.insertAllDiaries(diaries)
        }

        fun deleteAllDiaries() {
            viewModelScope.launch {
                diaryRepository.deleteAllDiaries()
            }
        }

        suspend fun getPhotoUriCount(): Int = diaryRepository.getPhotoUris().first().size

        /***************************************************************************************************
         *   compose layout functions
         *
         ***************************************************************************************************/
        val query = MutableStateFlow("")
        val isSensitive = MutableStateFlow(false)
        val startTimeMillis = MutableStateFlow(0L)
        val endTimeMillis = MutableStateFlow(0L)
        val symbolSequence = MutableStateFlow(0)
        val checkFutureDiaryOption = MutableStateFlow(false)

        data class FindDiaryParams(
            val query: String?,
            val isSensitive: Boolean = false,
            val startTimeMillis: Long = 0,
            val endTimeMillis: Long = 0,
            val symbolSequence: Int = 0,
            val checkFutureDiaryOption: Boolean = false,
        )

        private val findDiaryParams =
            combine(
                query,
                isSensitive,
                startTimeMillis,
                endTimeMillis,
                symbolSequence,
            ) {
                query,
                isSensitive,
                startTimeMillis,
                endTimeMillis,
                symbolSequence,
                ->
                FindDiaryParams(
                    query,
                    application.config.diarySearchQueryCaseSensitive,
                    startTimeMillis,
                    endTimeMillis,
                    symbolSequence,
                )
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        val diaries: StateFlow<List<Diary>> =
            findDiaryParams
                .flatMapLatest { params ->
                    diaryRepository
                        .getDiariesWithPhotosFlow(
                            query = params.query,
                            isSensitive = params.isSensitive,
                            startTimeMillis = params.startTimeMillis,
                            endTimeMillis = params.endTimeMillis,
                            symbolSequence = params.symbolSequence,
                        ).map {
                            resolveDiaryFilter(
                                it,
                                params.startTimeMillis,
                                params.endTimeMillis,
                                params.symbolSequence,
                                params.checkFutureDiaryOption,
                            )
                        }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        /***************************************************************************************************
         *   legacy layout functions
         *
         ***************************************************************************************************/
        suspend fun findDiary(
            query: String?,
            isSensitive: Boolean = false,
            startTimeMillis: Long = 0,
            endTimeMillis: Long = 0,
            symbolSequence: Int = 0,
            checkFutureDiaryOption: Boolean = false,
        ): List<Diary> =
            if (application.config.enableJetpackRoomDatabase) {
                val results =
                    diaryRepository
                        .getDiariesWithPhotos(
                            query = query,
                            isSensitive = isSensitive,
                            startTimeMillis = startTimeMillis,
                            endTimeMillis = endTimeMillis,
                            symbolSequence = symbolSequence,
                        )

                resolveDiaryFilter(
                    results,
                    startTimeMillis,
                    endTimeMillis,
                    symbolSequence,
                    checkFutureDiaryOption,
                )
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiary(
                        query,
                        isSensitive,
                        startTimeMillis,
                        endTimeMillis,
                        symbolSequence,
                        checkFutureDiaryOption,
                        realmInstance = realm,
                    )
                }
            }

        suspend fun findDiaryById(sequence: Int): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.getDiaryWithPhotosById(sequence).first()
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiaryBy(sequence, realm)
                }
            }

        suspend fun findDiaryByPhotoUri(
            photoUriString: String,
        ): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.getDiaryWithPhotosByPhotoUri(photoUriString).first()
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiaryBy(photoUriString, realm)
                }
            }

        suspend fun findOldestDiary(): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository
                    .getDiariesWithPhotos(
                        query = null,
                    ).minByOrNull { it.currentTimeMillis }
            } else {
                EasyDiaryDbHelper.findOldestDiary()
            }

        suspend fun findFirstDiary(): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null)
                    .filter { it.originDiaryId == DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT }
                    .minByOrNull { it.currentTimeMillis }
            } else {
                EasyDiaryDbHelper.findFirstDiary()
            }

        suspend fun findParentDiariesOf(
            sequence: Int,
        ): List<Diary> =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.findParentDiariesOf(sequence).first()
            } else {
                EasyDiaryDbHelper.findParentDiariesOf(sequence)
            }

        suspend fun findDiaryByDateString(
            dateString: String,
            sort: Sort = Sort.DESCENDING,
        ): List<Diary> =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository
                    .getDiariesByDateString(dateString, sort == Sort.ASCENDING)
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiaryByDateString(dateString, sort, realm)
                }
            }

        suspend fun findTemporaryDiaryBy(
            originSequence: Int,
        ): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).firstOrNull { it.originDiaryId == originSequence }
            } else {
                EasyDiaryDbHelper.findTemporaryDiaryBy(originSequence)
            }

        suspend fun getDiaryCount(): Int =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(query = null).size
            } else {
                EasyDiaryDbHelper.countDiaryAll().toInt()
            }

        suspend fun getMaxDiarySequence(): Int =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).maxByOrNull { it.diaryId }?.diaryId ?: 1
            } else {
                EasyDiaryDbHelper.getMaxDiarySequence()
            }

        suspend fun insertDiary(diary: Diary) {
            diaryRepository.insertDiary(diary)
        }

        suspend fun insertTemporaryDiary(diary: Diary) {
            diaryRepository.insertTemporaryDiary(diary)
        }

        suspend fun duplicateDiary(diary: Diary) {
            diaryRepository.duplicateDiary(diary)
        }

        suspend fun updateDiary(diary: Diary) {
            diaryRepository.updateDiaryWithPhotos(diary)
        }

        suspend fun clearSelectedStatus() {
            diaryRepository.clearSelectedStatus()
        }

        suspend fun deleteTemporaryDiaryByOriginId(originDiaryId: Int) {
            diaryRepository.deleteTemporaryDiaryByOriginId(originDiaryId)
        }

        suspend fun deleteDiaryById(seq: Int) {
            diaryRepository.deleteDiaryById(seq)
        }

        suspend fun getSymbolUsedCountMap(
            isReverse: Boolean = false,
            startTimeMillis: Long = 0,
            endTimeMillis: Long = 0,
        ): Map<Int, Int> {
            val listDiary =
                this.findDiary(
                    null,
                    false,
                    startTimeMillis,
                    endTimeMillis,
                )

            val map = hashMapOf<Int, Int>()
            listDiary.forEach { diaryDto ->
                val targetColumn = diaryDto.symbolSequence
                if (targetColumn != 0) {
                    if (map[targetColumn] == null) {
                        map[targetColumn] = 1
                    } else {
                        map[targetColumn] = (map[targetColumn] ?: 0) + 1
                    }
                }
            }
            return when (isReverse) {
                true -> map.toList().sortedByDescending { (_, value) -> value }.toMap()
                false -> map.toList().sortedBy { (_, value) -> value }.toMap()
            }
        }

        suspend fun applyFilter(mode: String?): List<Diary> {
            val currentTime = System.currentTimeMillis()

            return when (mode) {
                DiaryComponentConstants.MODE_TASK_TODO -> {
                    findDiary(query = null)
                        .filter { it.symbolSequence in 80..81 }
                        .reversed()
                }

                DiaryComponentConstants.MODE_TASK_DOING -> {
                    findDiary(query = null, symbolSequence = 81)
                }

                DiaryComponentConstants.MODE_TASK_DONE -> {
                    findDiary(query = null)
                        .filter { it.symbolSequence in 82..83 }
                }

                DiaryComponentConstants.MODE_TASK_CANCEL -> {
                    findDiary(query = null, symbolSequence = 83)
                }

                DiaryComponentConstants.MODE_FUTURE -> {
                    findDiary(query = null)
                        .filter { it.symbolSequence !in 80..83 && it.currentTimeMillis > currentTime }
                        .reversed()
                }

                else -> {
                    findDiary(query = null)
                        .filter { it.symbolSequence !in 80..83 && it.currentTimeMillis <= currentTime }
                        .take(100) // subList(0, 100) 보다 가독성이 좋은 take() 사용
                }
            }
        }

        suspend fun getAttachedPhotos(
            context: Context,
        ): List<GalleryAdapter.AttachedPhoto>? =
            withContext(Dispatchers.IO) {
                val photoDirectory =
                    File(EasyDiaryUtils.getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
                val files = photoDirectory.listFiles() ?: return@withContext null

                val diaryMap =
                    if (application.config.enableJetpackRoomDatabase) {
                        val allDiariesWithPhotos = diaryRepository.getDiariesWithPhotos()
                        val map = mutableMapOf<String, Diary>()
                        allDiariesWithPhotos.forEach { diary ->
                            diary.photoUris.forEach { photo ->
                                photo.photoUri?.let { uri ->
                                    val fileName = uri.substringAfterLast('/')
                                    if (!map.containsKey(fileName)) {
                                        map[fileName] = diary
                                    }
                                }
                            }
                        }
                        map
                    } else {
                        val listPostcard =
                            File(EasyDiaryUtils.getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
                                .listFiles()
                                ?.map { file ->
                                    val diary =
                                        EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                                            EasyDiaryDbHelper.findDiaryBy(file.name, realm)
                                        }
                                    GalleryAdapter.AttachedPhoto(file, false, diary)
                                }?.sortedByDescending { item ->
                                    item.diary?.currentTimeMillis ?: 0
                                }
                        return@withContext listPostcard
                    }

                files
                    .map { file ->
                        val diary = diaryMap[file.name]
                        GalleryAdapter.AttachedPhoto(file, false, diary)
                    }.sortedByDescending { item ->
                        item.diary?.currentTimeMillis ?: 0
                    }
            }

        suspend fun getDateStringMap(
            month: Int,
            year: Int,
        ): Map<String, List<Diary>> {
            // 1. 해당 연/월의 1일 날짜 생성
            val targetMonth = YearMonth.of(year, month)
            val startOfMonth = targetMonth.atDay(1)

            // 2. 기준 월의 시작일 - 7주(49일)
            val startDate = startOfMonth.minusWeeks(7)

            // 3. 기준 월의 시작일 + 7주(49일)
            val endDate = startOfMonth.plusWeeks(7)

            val sortAsc = application.config.calendarSorting == CALENDAR_SORTING_ASC

            val allDiariesInRange = if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.getDiariesByDateRange(startDate.toString(), endDate.toString())
            } else {
                // Realm legacy: fetch one by one as before to keep compatibility
                val sort = if (sortAsc) Sort.ASCENDING else Sort.DESCENDING
                val dateList = mutableListOf<String>()
                var current = startDate
                while (!current.isAfter(endDate)) {
                    dateList.add(current.toString())
                    current = current.plusDays(1)
                }
                return dateList.associateWith { findDiaryByDateString(it, sort) }
            }

            // Grouping for Room
            val groupedMap = if (sortAsc) {
                allDiariesInRange.sortedBy { it.currentTimeMillis }
            } else {
                allDiariesInRange.sortedByDescending { it.currentTimeMillis }
            }.groupBy { it.dateString ?: "" }

            val resultMap = mutableMapOf<String, List<Diary>>()
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                val dateStr = currentDate.toString()
                resultMap[dateStr] = groupedMap[dateStr] ?: emptyList()
                currentDate = currentDate.plusDays(1)
            }
            return resultMap
        }

        /***************************************************************************************************
         *   common functions
         *
         ***************************************************************************************************/
        fun resolveDiaryFilter(
            diaries: List<Diary>,
            startTimeMillis: Long = 0,
            endTimeMillis: Long = 0,
            symbolSequence: Int = 0,
            checkFutureDiaryOption: Boolean = false,
        ): List<Diary> {
            // apply date filter & sorting (sync with EasyDiaryDbHelper)
            var results = diaries

            // apply future diary filter
            if (checkFutureDiaryOption && getApplication<Application>().config.disableFutureDiary) {
                results =
                    results
                        .filter { it.currentTimeMillis <= System.currentTimeMillis() }
                        .sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
            }

            // apply temporary diary filter (originDiaryId == 0 is normal diary)
            if (!application.config.enableDebugOptionVisibleTemporaryDiary) {
                results =
                    results.filter { it.originDiaryId == DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT }
            }

            // apply task symbol top order logic
            if (application.config.enableTaskSymbolTopOrder) {
                val taskSymbols = listOf(80, 81)
                val mergedList = mutableListOf<Diary>()
                mergedList.addAll(results.filter { it.symbolSequence in taskSymbols })
                mergedList.addAll(results.filter { it.symbolSequence !in taskSymbols })
                results = mergedList
            }

            return results
        }
    }
