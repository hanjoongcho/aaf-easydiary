package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.GalleryAdapter
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.History
import me.blog.korn123.easydiary.domain.repository.DiaryRepository
import me.blog.korn123.easydiary.extensions.actionLogRepository
import me.blog.korn123.easydiary.extensions.alarmRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.CALENDAR_SORTING_ASC
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.PhotoHighlightManager
import java.io.File
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Calendar
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class DiaryViewModel
    @Inject
    constructor(
        application: Application,
        private val diaryRepository: DiaryRepository,
        private val photoHighlightManager: PhotoHighlightManager,
    ) : AndroidViewModel(application) {
        /***************************************************************************************************
         *   compose layout functions
         *
         ***************************************************************************************************/

        var isLoading by mutableStateOf(false)
        var loadingMessage by mutableStateOf<String?>(null)

        val photoHighlightList: StateFlow<List<History>> = photoHighlightManager.historyList

        fun updatePhotoHighlightIfNeeded() {
            if (photoHighlightManager.isCalculated()) return

            val startTime = System.currentTimeMillis()
            var totalHistoryCount = 0
            Log.i(AAF_TEST, "updateHistory start from ViewModel")

            viewModelScope.launch(Dispatchers.IO) {
                val oldestDiary = findOldestDiary() ?: return@launch
                val historyItems = mutableListOf<History>()
                val oneDayMillis: Long = 1000 * 60 * 60 * 24
                val oneYearDays = 365
                val betweenMillis = System.currentTimeMillis().minus(oldestDiary.currentTimeMillis)
                val betweenDays = betweenMillis / oneDayMillis

                val allDiaries = findDiary(null)

                val deferredHistories = mutableListOf<kotlinx.coroutines.Deferred<List<History>>>()

                fun fetchHistoryForPeriod(
                    pastMillis: Long,
                    historyTag: String,
                ): List<History> {
                    val periodHistories = mutableListOf<History>()
                    val defaultDayBuffer = 1
                    val noDataDayBufferMaxLoop = 3
                    val elapsedTime =
                        measureTimeMillis {
                            val maxBufferMillis =
                                pastMillis.plus((defaultDayBuffer + noDataDayBufferMaxLoop) * oneDayMillis)

                            val diaryItems =
                                allDiaries
                                    .filter { it.currentTimeMillis in pastMillis..maxBufferMillis }
                                    .sortedBy { it.currentTimeMillis }

                            if (diaryItems.isNotEmpty()) {
                                val targetDate = diaryItems.first().currentTimeMillis
                                diaryItems
                                    .filter { it.currentTimeMillis <= targetDate + oneDayMillis }
                                    .forEach { diary ->
                                        diary.photoUrisWithEncryptionPolicy()?.forEach { photoUri ->
                                            periodHistories.add(
                                                History(
                                                    historyTag,
                                                    DateUtils.getDateStringFromTimeMillis(
                                                        diary.currentTimeMillis,
                                                        SimpleDateFormat.FULL,
                                                    ),
                                                    if (diary.isEncrypt) {
                                                        ""
                                                    } else {
                                                        EasyDiaryUtils.getApplicationDataDirectory(
                                                            application,
                                                        ) + photoUri.getFilePath()
                                                    },
                                                    diary.diaryId,
                                                ),
                                            )
                                        }
                                    }
                            }
                        }
                    Log.i(AAF_TEST, "[$totalHistoryCount] fetchHistoryForPeriod end: ${elapsedTime}ms")
                    totalHistoryCount++
                    return periodHistories
                }

                for (i in 1..11) {
                    val pastMills = EasyDiaryUtils.convDateToTimeMillis(Calendar.MONTH, i.unaryMinus())
                    if (oldestDiary.currentTimeMillis < pastMills) {
                        deferredHistories.add(
                            async {
                                fetchHistoryForPeriod(
                                    pastMills,
                                    MessageFormat.format(
                                        application.getString(R.string.monthly_highlight_tag),
                                        i,
                                    ),
                                )
                            },
                        )
                    }
                }

                if (betweenDays > oneYearDays) {
                    for (i in 1..(betweenDays / oneYearDays).toInt()) {
                        val pastMills =
                            EasyDiaryUtils.convDateToTimeMillis(Calendar.YEAR, i.unaryMinus())
                        deferredHistories.add(
                            async {
                                fetchHistoryForPeriod(
                                    pastMills,
                                    MessageFormat.format(
                                        application.getString(R.string.yearly_highlight_tag),
                                        i,
                                    ),
                                )
                            },
                        )
                    }
                }

                val results = deferredHistories.awaitAll()
                results.forEach { historyItems.addAll(it) }
                historyItems.reverse()
                Log.i(
                    AAF_TEST,
                    "[totalHistoryCount: $totalHistoryCount] updateHistory step-02 finished time in ViewModel: ${System.currentTimeMillis() - startTime}",
                )

                photoHighlightManager.updateHistory(historyItems)
            }
        }

        fun invalidatePhotoHighlightCache() {
            photoHighlightManager.invalidateCache()
        }

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
                        .observeDiariesWithPhotos(
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

        suspend fun findDiaryById(sequence: Long): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.observeDiaryWithPhotosById(sequence).first()
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiaryBy(sequence.toInt(), realm)
                }
            }

        suspend fun findOldestDiary(): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository
                    .findOldestDiary()
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
            sequence: Long,
        ): List<Diary> =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.observeParentDiariesOf(sequence).first()
            } else {
                EasyDiaryDbHelper.findParentDiariesOf(sequence.toInt())
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
            originSequence: Long,
        ): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).firstOrNull { it.originDiaryId == originSequence }
            } else {
                EasyDiaryDbHelper.findTemporaryDiaryBy(originSequence.toInt())
            }

        suspend fun getDiaryCount(): Int =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(query = null).size
            } else {
                EasyDiaryDbHelper.countDiaryAll().toInt()
            }

        suspend fun getMaxDiarySequence(): Long =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).maxByOrNull { it.diaryId }?.diaryId ?: 1L
            } else {
                EasyDiaryDbHelper.getMaxDiarySequence().toLong()
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

            val allDiariesInRange =
                if (application.config.enableJetpackRoomDatabase) {
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
            val groupedMap =
                if (sortAsc) {
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

        suspend fun migRealmToRoom() {
            if (!application.config.enableJetpackRoomDatabase) {
                val domainDiaries = mutableListOf<Diary>()
                val domainAlarms = mutableListOf<Alarm>()
                val domainActionLogs = mutableListOf<ActionLog>()
                val domainDDays = mutableListOf<DDay>()
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    domainDiaries.addAll(EasyDiaryDbHelper.findDiary(query = null, realmInstance = realm))
                    domainAlarms.addAll(EasyDiaryDbHelper.findAlarmAll())
                    domainActionLogs.addAll(EasyDiaryDbHelper.findAllActionLogs())
                    domainDDays.addAll(EasyDiaryDbHelper.findDDayAll())
                }

                loadingMessage = "migrating realm to room..."
                loadingMessage = "Diary migration..."
                diaryRepository.deleteAllDiaries()
                diaryRepository.insertAllDiaries(domainDiaries)

                loadingMessage = "Alarm migration..."
                application.alarmRepository.deleteAllAlarms()
                application.alarmRepository.insertAllAlarms(domainAlarms)

                loadingMessage = "ActionLog migration..."
                application.actionLogRepository.deleteAllActionLogs(true)
                application.actionLogRepository.insertAllActionLogs(domainActionLogs)

                loadingMessage = "D-Day migration..."
                application.dDayRepository.deleteAllDDays()
                application.dDayRepository.insertAllDDays(domainDDays)

                application.config.enableJetpackRoomDatabase = true
            }
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
