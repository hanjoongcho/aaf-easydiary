package me.blog.korn123.easydiary.helper

import android.content.Context
import io.realm.*
import me.blog.korn123.easydiary.activities.EditActivity
import me.blog.korn123.easydiary.activities.EditActivity.Companion.DIARY_ORIGIN_SEQUENCE_INIT
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.models.*
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object EasyDiaryDbHelper {
    private val mDiaryConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("diary.realm")
                .schemaVersion(20)
                .migration(EasyDiaryMigration())
                .modules(Realm.getDefaultModule()!!)
                .build()
    }

    private var mRealmInstance: Realm? = null

    private fun getInstance(): Realm {
        if (mRealmInstance == null || mRealmInstance?.isClosed == true) {
            mRealmInstance = Realm.getInstance(mDiaryConfig)
        }
        return mRealmInstance!!
    }

    fun getTemporaryInstance() = Realm.getInstance(mDiaryConfig)!!

    fun closeInstance() {
        mRealmInstance?.close()
    }

    fun getRealmPath(): String {
        return getInstance().path
    }

    fun beginTransaction() {
        getInstance().beginTransaction()
    }

    fun commitTransaction() {
        getInstance().commitTransaction()
    }

    fun clearSelectedStatus() {
        getInstance().executeTransaction { realm ->
            realm.where(DiaryDto::class.java).equalTo("isSelected", true).findAll().forEach { diaryDto ->
                diaryDto.isSelected = false
            }
        }
    }

    /***************************************************************************************************
     *   Manage DiaryDto model
     *   Create: Insert
     *   Read: Find
     *   Update: Update
     *   Delete: Delete
     *
     ***************************************************************************************************/
    fun insertDiary(diaryDto: DiaryDto) {
        getInstance().executeTransaction { realm ->
            var sequence = 1
            if (realm.where(DiaryDto::class.java).count() > 0L) {
                val number = realm.where(DiaryDto::class.java).max("sequence")
                number?.let {
                    sequence = it.toInt().plus(1)
                }
            }
            diaryDto.sequence = sequence
            realm.insert(diaryDto)
        }
    }

    fun insertTemporaryDiary(diaryTemp: DiaryDto) {
        deleteTemporaryDiaryBy(diaryTemp.originSequence)
        getInstance().executeTransaction { realm ->
            if (diaryTemp.sequence == EditActivity.DIARY_SEQUENCE_INIT) {
                realm.where(DiaryDto::class.java).max("sequence")?.let {
                    diaryTemp.sequence = it.toInt().plus(1)
                }
            }
            realm.insert(diaryTemp)
        }
    }

    fun duplicateDiaryBy(diaryDto: DiaryDto) {
        diaryDto.currentTimeMillis = System.currentTimeMillis()
        diaryDto.updateDateString()
        diaryDto.originSequence = DIARY_ORIGIN_SEQUENCE_INIT
        insertDiary(diaryDto)
    }

    fun findTemporaryDiaryBy(originSequence: Int, realmInstance: Realm = getInstance()): DiaryDto? {
        return realmInstance.where(DiaryDto::class.java).equalTo("originSequence", originSequence).findFirst()
    }

    fun findFirstDiary(): DiaryDto? {
        val realm = getInstance()
        val firstItemTimeMillis = (realm.where(DiaryDto::class.java)
                .equalTo("originSequence", DIARY_ORIGIN_SEQUENCE_INIT)
                .min("currentTimeMillis") ?: 0L).toLong()
        return realm.where(DiaryDto::class.java).equalTo("currentTimeMillis", firstItemTimeMillis).findFirst()
    }

    /**
     * Makes an unmanaged in-memory copy of already persisted RealmObjects
     *
     * @return an in-memory detached copy of managed RealmObjects.
     */
    fun findDiary(query: String?, isSensitive: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0, symbolSequence: Int = 0, realmInstance: Realm = getInstance()): List<DiaryDto> {
        var results: RealmResults<DiaryDto> = when (StringUtils.isEmpty(query)) {
            true -> {
                realmInstance.where(DiaryDto::class.java).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))

            }
            false -> {
                if (isSensitive) {
                    realmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query).or().contains("title", query).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                } else {
                    realmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query, Case.INSENSITIVE).or().contains("title", query, Case.INSENSITIVE).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }
            }
        }

        // apply date filter
        results = when {
            startTimeMillis > 0 && endTimeMillis > 0 -> results.where().between("currentTimeMillis", startTimeMillis, endTimeMillis).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
            startTimeMillis > 0 -> results.where().greaterThanOrEqualTo("currentTimeMillis", startTimeMillis).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
            endTimeMillis > 0 -> results.where().lessThanOrEqualTo("currentTimeMillis", endTimeMillis).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
            else -> results
        }

        // apply feeling symbol
        if (symbolSequence in 1..9998) {
            results = results.where().equalTo("weather", symbolSequence).findAll()
        }

        // Exclude -1 or greater than 0
        if (EasyDiaryApplication.context?.config?.enableDebugMode == false) {
            results = results.where().equalTo("originSequence", DIARY_ORIGIN_SEQUENCE_INIT).findAll()
        }

        return when (EasyDiaryApplication.context?.config?.enableTaskSymbolTopOrder ?: false) {
            true -> {
                val sortedList = realmInstance.copyFromRealm(results)
                sortedList.sortWith(kotlin.Comparator { item1, item2 ->
//                    Log.i("EDD", "${sortedList.indexOf(item1)}(${item1.sequence}) to ${sortedList.indexOf(item2)}(${item2.sequence}) ${item1.weather}, ${item2.weather}")
                    when {
                        item1.weather in 80..81 && item2.weather in 80..81 -> 0
                        item1.weather in 80..81 -> -1
                        item2.weather in 80..81 -> 1
                        else -> 0
                    }
                })
                sortedList
            }
            else -> {
                realmInstance.copyFromRealm(results)
            }
        }
    }

    fun findDiaryBy(sequence: Int, realmInstance: Realm = getInstance()): DiaryDto? {
        return realmInstance.where(DiaryDto::class.java)
                .equalTo("sequence", sequence).findFirst()
    }

    fun findDiaryByDateString(dateString: String?, sort: Sort = Sort.DESCENDING): List<DiaryDto> {
        return getInstance().where(DiaryDto::class.java)
                .equalTo("originSequence", DIARY_ORIGIN_SEQUENCE_INIT)
                .equalTo("dateString", dateString)
                .findAll()
                .sort("sequence", sort).toList()
    }

    fun findPhotoUriAll(realmInstance: Realm = getInstance()): List<PhotoUriDto> {
        return realmInstance.where(PhotoUriDto::class.java).findAll().sort("photoUri", Sort.ASCENDING).toList()
    }

    fun updateDiaryBy(diaryDto: DiaryDto) {
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(diaryDto) }
    }

    fun deleteDiaryBy(sequence: Int, realmInstance: Realm = getInstance()) {
        realmInstance.run {
            where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()?.let {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    fun deleteTemporaryDiaryBy(originSequence: Int, realmInstance: Realm = getInstance()) {
        realmInstance.run {
            where(DiaryDto::class.java).equalTo("originSequence", originSequence).findFirst()?.let {
                beginTransaction()
                it.deleteFromRealm()
                commitTransaction()
            }
        }
    }

    fun countDiaryAll(): Long {
        return getInstance().where(DiaryDto::class.java)
                .equalTo("originSequence", DIARY_ORIGIN_SEQUENCE_INIT)
                .count()
    }

    fun countDiaryBy(dateString: String): Int = getInstance().where(DiaryDto::class.java)
            .equalTo("originSequence", DIARY_ORIGIN_SEQUENCE_INIT)
            .equalTo("dateString", dateString)
            .count().toInt()

    fun countPhotoUriBy(uriString: String): Int = getInstance().where(PhotoUriDto::class.java)
            .equalTo("photoUri", uriString)
            .count().toInt()


    /***************************************************************************************************
     *   Manage Alarm model
     *
     ***************************************************************************************************/
    fun insertTemporaryAlarm(workMode: Int = Alarm.WORK_MODE_DIARY_WRITING): Alarm {
        val alarm = Alarm().apply { this.workMode = workMode }
        val sequence = getInstance().where(Alarm::class.java).max("sequence") ?: 0
        when (sequence.toInt() == countAlarmAll().toInt()) {
            true ->  alarm.sequence = sequence.toInt().plus(1)
            false -> {
                run loop@ {
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

    fun duplicateAlarmBy(alarm: Alarm, realmInstance: Realm = getInstance()): Alarm {
        return realmInstance.copyFromRealm(alarm)
    }

    private fun findAlarmBy(realmInstance: Realm, sequence: Int): Alarm? {
        return realmInstance.where(Alarm::class.java).equalTo("sequence", sequence).findFirst()
    }

    fun findAlarmBy(sequence: Int): Alarm? {
        return findAlarmBy(getInstance(), sequence)
    }

    fun findAlarmAll(): List<Alarm> = getInstance().where(Alarm::class.java).findAll().sort("sequence", Sort.ASCENDING)

    fun findSnoozeAlarms(): List<Alarm> = getInstance().where(Alarm::class.java).greaterThan("retryCount", 0).findAll().toList()

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

    fun countAlarmAll(): Long {
        return getInstance().where(Alarm::class.java).count()
    }


    /***************************************************************************************************
     *   Manage ActionLog model
     *
     ***************************************************************************************************/
    fun insertActionLog(actionLog: ActionLog, context: Context) {
        if (context.config.enableDebugMode) {
            getInstance().executeTransaction { realm ->
                val sequence = realm.where(ActionLog::class.java).max("sequence") ?: 0
                actionLog.sequence = sequence.toInt().plus(1)
                realm.insert(actionLog)
            }
        }
    }

    fun findActionLogAll(): List<ActionLog> = getInstance().where(ActionLog::class.java).findAll().sort("sequence", Sort.DESCENDING)

    fun deleteActionLogAll() {
        getInstance().executeTransaction { realm ->
            realm.where(ActionLog::class.java).findAll().deleteAllFromRealm()
        }
    }
}

