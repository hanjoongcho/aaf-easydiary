package me.blog.korn123.easydiary.helper

import android.content.Context
import io.realm.Case
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.Sort
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.DDay
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.models.PhotoUri
import org.apache.commons.lang3.StringUtils
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object EasyDiaryDbHelper {
    private val mDiaryConfig: RealmConfiguration by lazy {
        RealmConfiguration
            .Builder()
            .name(RealmConstants.DIARY_DB_NAME)
            .schemaVersion(RealmConstants.SCHEMA_VERSION)
            .allowWritesOnUiThread(true)
            .migration(EasyDiaryMigration())
            .build()
    }

    private var mRealmInstance: Realm? = null

    private fun getInstance(): Realm {
        val currentInstance: Realm? = mRealmInstance
        return if (currentInstance == null || currentInstance.isClosed) {
            Realm.getInstance(mDiaryConfig).also { mRealmInstance = it }
        } else {
            currentInstance
        }
    }

    fun getTemporaryInstance(): Realm = Realm.getInstance(mDiaryConfig)

    fun closeInstance() {
        mRealmInstance?.close()
    }

    fun getRealmPath(): String =
        getTemporaryInstance().use {
            it.path
        }

    fun beginTransaction() {
        getInstance().beginTransaction()
    }

    fun commitTransaction() {
        getInstance().commitTransaction()
    }

    fun clearSelectedStatus() {
        getInstance().executeTransaction { realm ->
            realm.where(Diary::class.java).equalTo("isSelected", true).findAll().forEach { diaryDto ->
                diaryDto.isSelected = false
            }
        }
    }

    fun <E : RealmModel?> copyFromRealm(realmObjects: Iterable<E>?): MutableList<E> = getInstance().copyFromRealm(realmObjects)

    @Deprecated(message = "Use DiaryViewModel.getMaxDiarySequence() instead")
    fun getMaxDiarySequence(realmInstance: Realm = getInstance()): Int = realmInstance.where(Diary::class.java).max("sequence")?.toInt() ?: 1

    @Deprecated(message = "Use DiaryViewModel.findOldestDiary() instead")
    fun findOldestDiary(): DiaryDomain? =
        getInstance()
            .where(Diary::class.java)
            .sort("currentTimeMillis", Sort.ASCENDING)
            .findFirst()
            ?.toDomain()

    @Deprecated(message = "Use DiaryViewModel.findDiary() instead")
    fun findDiary(
        query: String?,
        isSensitive: Boolean = false,
        startTimeMillis: Long = 0,
        endTimeMillis: Long = 0,
        symbolSequence: Int = 0,
        checkFutureDiaryOption: Boolean = false,
        realmInstance: Realm = getInstance(),
    ): List<DiaryDomain> {
        var results: RealmResults<Diary> =
            when (StringUtils.isEmpty(query)) {
                true -> {
                    realmInstance.where(Diary::class.java).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }

                false -> {
                    if (isSensitive) {
                        realmInstance
                            .where(Diary::class.java)
                            .beginGroup()
                            .contains("contents", query)
                            .or()
                            .contains("title", query)
                            .endGroup()
                            .findAll()
                            .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                    } else {
                        realmInstance
                            .where(Diary::class.java)
                            .beginGroup()
                            .contains("contents", query, Case.INSENSITIVE)
                            .or()
                            .contains("title", query, Case.INSENSITIVE)
                            .endGroup()
                            .findAll()
                            .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                    }
                }
            }

        // apply date filter
        results =
            when {
                startTimeMillis > 0 && endTimeMillis > 0 -> {
                    results
                        .where()
                        .between("currentTimeMillis", startTimeMillis, endTimeMillis)
                        .findAll()
                        .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }

                startTimeMillis > 0 -> {
                    results
                        .where()
                        .greaterThanOrEqualTo("currentTimeMillis", startTimeMillis)
                        .findAll()
                        .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }

                endTimeMillis > 0 -> {
                    results
                        .where()
                        .lessThanOrEqualTo("currentTimeMillis", endTimeMillis)
                        .findAll()
                        .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }

                else -> {
                    results
                }
            }

        if (checkFutureDiaryOption && EasyDiaryApplication.context?.config?.disableFutureDiary == true) {
            results =
                results
                    .where()
                    .lessThanOrEqualTo("currentTimeMillis", System.currentTimeMillis())
                    .findAll()
                    .sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
        }

        // apply feeling symbol
//        if (symbolSequence in 1..9998 || symbolSequence > 9999) {
        if (symbolSequence != 0 && symbolSequence != SYMBOL_SELECT_ALL) {
            results = results.where().equalTo("weather", symbolSequence).findAll()
        }

        // Exclude -1 or greater than 0
        if (EasyDiaryApplication.context?.config?.enableDebugOptionVisibleTemporaryDiary == false) {
            results = results.where().equalTo("originSequence", DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT).findAll()
        }

        val resultsList: List<Diary> =
            when (EasyDiaryApplication.context?.config?.enableTaskSymbolTopOrder ?: false) {
                true -> {
                    val mergedList = arrayListOf<Diary>()
                    val valueArray = arrayOf(80L, 81L)
                    mergedList.addAll(results.where().`in`("weather", valueArray).findAll())
                    mergedList.addAll(
                        results
                            .where()
                            .not()
                            .`in`("weather", valueArray)
                            .findAll(),
                    )
                    mergedList
                }

                else -> {
                    results
                }
            }
        return resultsList.map { it.toDomain() }
    }

    @Deprecated(message = "Use DiaryViewModel.findTemporaryDiaryBy() instead")
    fun findTemporaryDiaryBy(
        originSequence: Int,
        realmInstance: Realm = getInstance(),
    ): DiaryDomain? =
        realmInstance
            .where(Diary::class.java)
            .equalTo("originSequence", originSequence)
            .findFirst()
            ?.toDomain()

    @Deprecated(message = "Use DiaryViewModel.findFirstDiary() instead")
    fun findFirstDiary(): DiaryDomain? {
        val realm = getInstance()
        val firstItemTimeMillis =
            (
                realm
                    .where(Diary::class.java)
                    .equalTo("originSequence", DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT)
                    .min("currentTimeMillis") ?: 0L
            ).toLong()
        return realm
            .where(Diary::class.java)
            .equalTo("currentTimeMillis", firstItemTimeMillis)
            .findFirst()
            ?.toDomain()
    }

    @Deprecated(message = "Use DiaryViewModel.findParentDiariesOf() instead")
    fun findParentDiariesOf(
        sequence: Int,
        realmInstance: Realm = getInstance(),
    ): List<DiaryDomain> =
        realmInstance
            .where(Diary::class.java)
            .equalTo("linkedDiaries", sequence)
            .findAll()
            .sort("currentTimeMillis", Sort.ASCENDING)
            .toList()
            .map { it.toDomain() }

    @Deprecated(message = "Use DiaryViewModel.findDiaryBy() instead")
    fun findDiaryBy(
        sequence: Int,
        realmInstance: Realm = getInstance(),
    ): DiaryDomain? =
        realmInstance
            .where(Diary::class.java)
            .equalTo("sequence", sequence)
            .findFirst()
            ?.toDomain()

    @Deprecated(message = "Use DiaryViewModel.findDiaryBy() instead")
    fun findDiaryBy(
        photoUri: String,
        realmInstance: Realm = getInstance(),
    ): DiaryDomain? {
        val result =
            realmInstance
                .where(PhotoUri::class.java)
                .contains("photoUri", photoUri)
                .isNotEmpty("diary")
                .findFirst()
                ?.diary
        var diary: Diary? = null
        result?.let {
            if (it.isValid && it.isNotEmpty()) {
                diary = it.first()
            }
        }

        return diary?.toDomain()
    }

    @Deprecated(message = "Use DiaryViewModel.findDiaryByDateString() instead")
    fun findDiaryByDateString(
        dateString: String?,
        sort: Sort = Sort.DESCENDING,
        realmInstance: Realm = getInstance(),
    ): List<DiaryDomain> =
        realmInstance
            .where(Diary::class.java)
            .equalTo("originSequence", DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT)
            .equalTo("dateString", dateString)
            .findAll()
            .sort("currentTimeMillis", sort)
            .toList()
            .map { it.toDomain() }

    fun findPhotoUriAll(realmInstance: Realm = getInstance()): List<PhotoUri> =
        realmInstance
            .where(PhotoUri::class.java)
            .findAll()
            .sort("photoUri", Sort.ASCENDING)
            .toList()

    fun countDiaryAll(): Long =
        getInstance()
            .where(Diary::class.java)
            .equalTo("originSequence", DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT)
            .count()

    @Deprecated(message = "Use DiaryViewModel.insertDiary() instead")
    fun insertDiary(
        diary: DiaryDomain,
        realmInstance: Realm = getInstance(),
    ) {
        realmInstance.executeTransaction { realm ->
            var sequence = 1
            if (realm.where(Diary::class.java).count() > 0L) {
                val number = realm.where(Diary::class.java).max("sequence")
                number?.let {
                    sequence = it.toInt().plus(1)
                }
            }

            realm.insert(if (diary.diaryId == DiaryEditingConstants.DIARY_SEQUENCE_INIT) diary.copy(diaryId = sequence).toRealm() else diary.toRealm())
        }
    }

    @Deprecated(message = "Use DiaryViewModel.insertTemporaryDiary() instead")
    fun insertTemporaryDiary(diaryTemp: DiaryDomain) {
        deleteTemporaryDiaryBy(diaryTemp.originDiaryId)
        getInstance().executeTransaction { realm ->
            if (diaryTemp.diaryId == DiaryEditingConstants.DIARY_SEQUENCE_INIT) {
                realm.where(Diary::class.java).max("sequence")?.let {
                    realm.insert(diaryTemp.copy(diaryId = it.toInt().plus(1)).toRealm())
                }
            } else {
                realm.insert(diaryTemp.toRealm())
            }
        }
    }

    fun duplicateDiaryBy(diary: Diary) {
        diary.run {
            currentTimeMillis = System.currentTimeMillis()
            updateDateString()
            originSequence = DiaryEditingConstants.DIARY_ORIGIN_SEQUENCE_INIT
            insertDiary(this.toDomain())
        }
    }

    @Deprecated(message = "Use DiaryViewModel.updateDiary() instead")
    fun updateDiaryBy(diary: DiaryDomain) {
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(diary.toRealm()) }
    }

    @Deprecated(message = "Use DiaryViewModel.deleteDiaryBy() instead")
    fun deleteDiaryBy(
        sequence: Int,
        realmInstance: Realm = getInstance(),
    ) {
        realmInstance.run {
            where(Diary::class.java).equalTo("sequence", sequence).findFirst()?.let {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    @Deprecated(message = "Use DiaryViewModel.deleteTemporaryDiaryBy() instead")
    fun deleteTemporaryDiaryBy(
        originSequence: Int,
        realmInstance: Realm = getInstance(),
    ) {
        realmInstance.run {
            where(Diary::class.java).equalTo("originSequence", originSequence).findFirst()?.let {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    /***************************************************************************************************
     *   Manage Alarm model
     *
     ***************************************************************************************************/
    fun makeTemporaryAlarm(workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING): Alarm {
        val alarm = Alarm().apply { this.workMode = workMode }
        val sequence = getInstance().where(Alarm::class.java).max("sequence") ?: 0
        when (sequence.toInt() == countAlarmAll().toInt()) {
            true -> {
                alarm.sequence = sequence.toInt().plus(1)
            }

            false -> {
                run loop@{
                    findAlarmAll().forEachIndexed { index, item ->
                        val validSequence = index.plus(1)
                        if (item.sequence != validSequence) {
                            alarm.sequence = validSequence
                            return@loop
                        }
                    }
                }
            }
        }
        return alarm
    }

    fun duplicateAlarmBy(
        alarm: Alarm,
        realmInstance: Realm = getInstance(),
    ): Alarm = realmInstance.copyFromRealm(alarm)

    private fun findAlarmBy(
        realmInstance: Realm,
        sequence: Int,
    ): Alarm? = realmInstance.where(Alarm::class.java).equalTo("sequence", sequence).findFirst()

    fun findAlarmBy(sequence: Int): Alarm? = findAlarmBy(getInstance(), sequence)

    fun Alarm.copy(): Alarm = duplicateAlarmBy(this)

    fun findAlarmAll(): List<Alarm> = getInstance().where(Alarm::class.java).findAll().sort("sequence", Sort.ASCENDING)

    fun findSnoozeAlarms(): List<Alarm> =
        getInstance()
            .where(Alarm::class.java)
            .greaterThan("retryCount", 0)
            .findAll()
            .toList()

    fun updateAlarmBy(alarm: Alarm) {
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(alarm) }
    }

    fun deleteAlarmBy(sequence: Int) {
        findAlarmBy(sequence)?.let {
            getInstance().run {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    fun countAlarmAll(): Long = getInstance().where(Alarm::class.java).count()

    /***************************************************************************************************
     *   Manage ActionLog model
     *
     ***************************************************************************************************/
    private fun insertActionLog(
        actionLog: ActionLog,
        realmInstance: Realm = getInstance(),
    ) {
        realmInstance.executeTransaction { realm ->
            val sequence = realm.where(ActionLog::class.java).max("sequence") ?: 0
            actionLog.sequence = sequence.toInt().plus(1)
            realm.insert(actionLog)
        }
    }

    /**
     * ```
     * Realm의 아래와 같은 특징이 있음
     * Realm 인스턴스는 동일한 Theread ID로부터 호출되어야 함
     * getInstance(RealmConfiguration configuration) signature로 리턴받은 Realm 인스턴스는 싱글톤으로 관리하던가 사용이 끝나면 close해야 함
     * 명시적으로 close하지 않는경우 realm db파일 변경 시 app crash 사유가 됨
     * Activity(UI Thread), Service, Receiver등은 동일한 Thread ID에서 동작함
     * Activity(Background Thread), AppWidgetProvider등은 별도의 Thread ID에서 동작함
     *
     * 임시 인스턴스 생성 후 use block안에서 realm 데이터를 저장하면 thread 제약없이 컨트롤이 가능함
     * ```
     */
    fun insertActionLog(
        actionLog: ActionLog,
        context: Context,
    ) {
        getTemporaryInstance().use { realm ->
            if (context.config.enableDebugMode) {
                insertActionLog(actionLog, realm)
            }
        }
    }

    fun findActionLogAll(): List<ActionLog> = getInstance().where(ActionLog::class.java).findAll().sort("sequence", Sort.DESCENDING)

    fun deleteActionLogAll() {
        getInstance().executeTransaction { realm ->
            realm.where(ActionLog::class.java).findAll().deleteAllFromRealm()
        }
    }

    /***************************************************************************************************
     *   Manage DDay model
     *
     ***************************************************************************************************/
    fun duplicateDDayBy(
        dDay: DDay,
        realmInstance: Realm = getInstance(),
    ): DDay = realmInstance.copyFromRealm(dDay)

    private fun findDDayBy(
        realmInstance: Realm,
        sequence: Int,
    ): DDay? = realmInstance.where(DDay::class.java).equalTo("sequence", sequence).findFirst()

    fun findDDayBy(sequence: Int): DDay? = findDDayBy(getInstance(), sequence)

    fun findDDayAll(sortOrder: Sort = Sort.ASCENDING): List<DDay> = getInstance().where(DDay::class.java).findAll().sort("targetTimeStamp", sortOrder)

    fun updateDDayBy(dDay: DDay) {
        if (dDay.sequence == -1) {
            val sequence = getInstance().where(DDay::class.java).max("sequence") ?: 0
            dDay.sequence = sequence.toInt().plus(1)
        }
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(dDay) }
    }

    fun deleteDDayBy(sequence: Int) {
        findDDayBy(sequence)?.let {
            getInstance().run {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    fun countDDayAll(): Long = getInstance().where(DDay::class.java).count()

    /***************************************************************************************************
     *   Manage ETC.
     *
     ***************************************************************************************************/
}
