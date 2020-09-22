package me.blog.korn123.easydiary.helper

import android.content.Context
import io.realm.*
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object EasyDiaryDbHelper {
    private val mDiaryConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("diary.realm")
                .schemaVersion(19)
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
     *
     ***************************************************************************************************/
    fun duplicateDiary(diaryDto: DiaryDto) {
        diaryDto.currentTimeMillis = System.currentTimeMillis()
        diaryDto.updateDateString()
        insertDiary(diaryDto)
    }

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

    fun selectFirstDiary(): DiaryDto? {
        val realm = getInstance()
        val firstItemTimeMillis = (realm.where(DiaryDto::class.java).min("currentTimeMillis") ?: 0L).toLong()
        return realm.where(DiaryDto::class.java).equalTo("currentTimeMillis", firstItemTimeMillis).findFirst()
    }

    fun countDiaryAll(): Long {
        return getInstance().where(DiaryDto::class.java).count()
    }

    /**
     * Makes an unmanaged in-memory copy of already persisted RealmObjects
     *
     * @return an in-memory detached copy of managed RealmObjects.
     */
    fun readDiary(query: String?, isSensitive: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0, symbolSequence: Int = 0, realmInstance: Realm = getInstance()): List<DiaryDto> {
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

    fun readDiaryBy(sequence: Int, realmInstance: Realm = getInstance()): DiaryDto {
        return realmInstance.where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()!!
    }

    fun readDiaryByDateString(dateString: String?, sort: Sort = Sort.DESCENDING): List<DiaryDto> {
        return getInstance().where(DiaryDto::class.java).equalTo("dateString", dateString).findAll().sort("sequence", sort).toList()
    }

    fun selectPhotoUriAll(realmInstance: Realm = getInstance()): List<PhotoUriDto> {
        return realmInstance.where(PhotoUriDto::class.java).findAll().sort("photoUri", Sort.ASCENDING).toList()
    }

    fun countPhotoUriBy(uriString: String): Int {
        val count = getInstance().where(PhotoUriDto::class.java).equalTo("photoUri", uriString).count()
        return count.toInt()
    }

    fun countDiaryBy(dateString: String): Int = getInstance().where(DiaryDto::class.java).equalTo("dateString", dateString).count().toInt()

    fun updateDiary(diaryDto: DiaryDto) {
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(diaryDto) }
    }

    fun deleteDiary(sequence: Int, realmInstance: Realm = getInstance()) {
        realmInstance.run {
            where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()?.let {
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
    private fun readAlarmBy(realmInstance: Realm, sequence: Int): Alarm? {
        return realmInstance.where(Alarm::class.java).equalTo("sequence", sequence).findFirst()
    }

    fun deleteAlarm(sequence: Int) {
        readAlarmBy(sequence)?.let {
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

    fun readAlarmAll(): List<Alarm> = getInstance().where(Alarm::class.java).findAll().sort("sequence", Sort.ASCENDING)

    fun readAlarmBy(sequence: Int): Alarm? {
        return readAlarmBy(getInstance(), sequence)
    }

    fun duplicateAlarm(alarm: Alarm, realmInstance: Realm = getInstance()): Alarm {
        return realmInstance.copyFromRealm(alarm)
    }

    fun updateAlarm(alarm: Alarm) {
        getInstance().executeTransaction { realm -> realm.insertOrUpdate(alarm) }
    }

    fun createTemporaryAlarm(workMode: Int = Alarm.WORK_MODE_DIARY_WRITING): Alarm {
        val alarm = Alarm().apply { this.workMode = workMode }
        val sequence = getInstance().where(Alarm::class.java).max("sequence") ?: 0
        when (sequence.toInt() == countAlarmAll().toInt()) {
            true ->  alarm.sequence = sequence.toInt().plus(1)
            false -> {
                run loop@ {
                    readAlarmAll().forEachIndexed { index, item ->
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

    fun readSnoozeAlarms(): List<Alarm> = getInstance().where(Alarm::class.java).greaterThan("retryCount", 0).findAll().toList()


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

    fun readActionLogAll(): List<ActionLog> = getInstance().where(ActionLog::class.java).findAll().sort("sequence", Sort.DESCENDING)

    fun deleteActionLogAll() {
        getInstance().executeTransaction { realm ->
            realm.where(ActionLog::class.java).findAll().deleteAllFromRealm()
        }
    }
}

