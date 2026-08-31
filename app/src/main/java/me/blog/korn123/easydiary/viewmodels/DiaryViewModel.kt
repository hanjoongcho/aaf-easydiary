package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.Realm
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
import me.blog.korn123.easydiary.helper.DIARY_PHOTO_DIRECTORY
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.DiaryEditingConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL
import me.blog.korn123.easydiary.helper.toRealm
import java.io.File
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
        val allDiaries: StateFlow<List<Diary>> =
            diaryRepository
                .getAllDiaries()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        val diaryCount: StateFlow<Int> =
            allDiaries
                .map { it.size }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0,
                )

        fun addDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.insertDiary(diary)
            }
        }

        suspend fun insertDiary(diary: Diary) {
            diaryRepository.insertDiary(diary)
        }

        suspend fun addAllDiaries(diaries: List<Diary>) {
            diaryRepository.addAllDiaries(diaries)
        }

        fun deleteDiary(diary: Diary) {
            viewModelScope.launch {
                diaryRepository.deleteDiary(diary)
            }
        }

        fun deleteDiaryById(seq: Int) {
            viewModelScope.launch {
                diaryRepository.deleteDiaryById(seq)
            }
        }

        fun deleteAllDiaries() {
            viewModelScope.launch {
                diaryRepository.deleteAllDiaries()
            }
        }

        suspend fun getDiaryCount(): Int = findDiary(query = null).size

        suspend fun getLatestDiary(): Diary? =
            diaryRepository
                .getAllDiaries()
                .first()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

        suspend fun getLatestDiaryWithPhotos(): Diary? =
            diaryRepository
                .getDiariesWithPhotos()
                .first()
                .firstOrNull { diary -> diary.photoUris.isNotEmpty() }

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
                        .getDiariesWithPhotos(
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
                        ).first()

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

        suspend fun findDiaryBy(sequence: Int): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                diaryRepository.getDiaryWithPhotosById(sequence).first()
            } else {
                EasyDiaryDbHelper.getTemporaryInstance().use { realm ->
                    EasyDiaryDbHelper.findDiaryBy(sequence, realm)
                }
            }

        suspend fun findDiaryBy(
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
                    ).first()
                    .minByOrNull { it.currentTimeMillis }
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

        suspend fun findTemporaryDiaryBy(
            originSequence: Int,
        ): Diary? =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).firstOrNull { it.originDiaryId == originSequence }
            } else {
                EasyDiaryDbHelper.findTemporaryDiaryBy(originSequence)
            }

        suspend fun getMaxDiarySequence(): Int =
            if (application.config.enableJetpackRoomDatabase) {
                findDiary(null).maxByOrNull { it.diaryId }?.diaryId ?: 1
            } else {
                EasyDiaryDbHelper.getMaxDiarySequence()
            }

        // FIXME: Remove legacy realm functions
        suspend fun updateDiary(diary: Diary) {
            diaryRepository.updateDiaryWithPhotos(diary)
            EasyDiaryDbHelper.updateDiaryBy(diary)
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
                val photoDirectory = File(EasyDiaryUtils.getApplicationDataDirectory(context) + DIARY_PHOTO_DIRECTORY)
                val files = photoDirectory.listFiles() ?: return@withContext null

                val diaryMap =
                    if (application.config.enableJetpackRoomDatabase) {
                        val allDiariesWithPhotos = diaryRepository.getDiariesWithPhotos().first()
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
            var results =
                when {
                    startTimeMillis > 0 && endTimeMillis > 0 -> {
                        diaries
                            .filter { it.currentTimeMillis in startTimeMillis..endTimeMillis }
                            .sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
                    }

                    startTimeMillis > 0 -> {
                        diaries
                            .filter { it.currentTimeMillis >= startTimeMillis }
                            .sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
                    }

                    endTimeMillis > 0 -> {
                        diaries
                            .filter { it.currentTimeMillis <= endTimeMillis }
                            .sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
                    }

                    else -> {
                        diaries.sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
                    }
                }

            // apply future diary filter
            if (checkFutureDiaryOption && getApplication<Application>().config.disableFutureDiary) {
                results =
                    results
                        .filter { it.currentTimeMillis <= System.currentTimeMillis() }
                        .sortedWith(compareByDescending<Diary> { it.currentTimeMillis }.thenByDescending { it.diaryId })
            }

            // apply feeling symbol filter
            if (symbolSequence != 0 && symbolSequence != SYMBOL_SELECT_ALL) {
                results = results.filter { it.symbolSequence == symbolSequence }
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
