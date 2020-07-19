package me.blog.korn123.easydiary.helper

import io.realm.*
import me.blog.korn123.easydiary.models.ActionLog
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.models.DiaryDto
import me.blog.korn123.easydiary.models.PhotoUriDto
import org.apache.commons.lang3.StringUtils
import java.util.*
/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object EasyDiaryDbHelper {
    private val mDiaryConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("diary.realm")
                .schemaVersion(17)
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

    fun duplicateDiary(diaryDto: DiaryDto, realmInstance: Realm = getInstance()) {
        val copyItem = realmInstance.copyFromRealm(diaryDto)
        copyItem.currentTimeMillis = System.currentTimeMillis()
        copyItem.updateDateString()
        insertDiary(copyItem)
    }

    fun clearSelectedStatus() {
        beginTransaction()
        readDiary(null).map {
            if (it.isSelected) it.isSelected = false
        }
        commitTransaction()
    }

    /***************************************************************************************************
     *   Manage DiaryDto model
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

    fun selectFirstDiary(): DiaryDto? {
        val realm = getInstance()
        val firstItemTimeMillis = (realm.where(DiaryDto::class.java).min("currentTimeMillis") ?: 0L).toLong()
        return realm.where(DiaryDto::class.java).equalTo("currentTimeMillis", firstItemTimeMillis).findFirst()
    }

    fun countDiaryAll(): Long {
        return getInstance().where(DiaryDto::class.java).count()
    }

    fun readDiary(query: String?, isSensitive: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0, symbolSequence: Int = 0, realmInstance: Realm = getInstance()): ArrayList<DiaryDto> {
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

        val list = ArrayList<DiaryDto>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun readDiaryBy(sequence: Int, realmInstance: Realm = getInstance()): DiaryDto {
        return realmInstance.where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()!!
    }

    fun readDiaryByDateString(dateString: String?, sort: Sort = Sort.DESCENDING): List<DiaryDto> {
        val results: RealmResults<DiaryDto> = getInstance().where(DiaryDto::class.java).equalTo("dateString", dateString).findAll().sort("sequence", sort)
        val list = ArrayList<DiaryDto>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun selectPhotoUriAll(realmInstance: Realm = getInstance()): List<PhotoUriDto> {
        val results = realmInstance.where(PhotoUriDto::class.java).findAll().sort("photoUri", Sort.ASCENDING)
        val list = ArrayList<PhotoUriDto>()
        list.addAll(results.subList(0, results.size))
        return list
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
        val diaryDto = realmInstance.where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()
        if (diaryDto != null) {
            realmInstance.beginTransaction()
            diaryDto.deleteFromRealm()
            realmInstance.commitTransaction()
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

    fun readAlarmAll(): List<Alarm> {
        val results = getInstance().where(Alarm::class.java).findAll().sort("sequence", Sort.ASCENDING)
        val list = mutableListOf<Alarm>()
        list.addAll(results.subList(0, results.size))
        return list
    }

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
        alarm.sequence = sequence.toInt().plus(1)
        return alarm
    }


    /***************************************************************************************************
     *   Manage ActionLog model
     *
     ***************************************************************************************************/
    fun insertActionLog(actionLog: ActionLog) {
        getInstance().executeTransaction { realm ->
            val sequence = realm.where(ActionLog::class.java).max("sequence") ?: 0                                       
            actionLog.sequence = sequence.toInt().plus(1)
            realm.insert(actionLog)
        }
    }

    fun readActionLogAll(): List<ActionLog> {
        val results = getInstance().where(ActionLog::class.java).findAll().sort("sequence", Sort.DESCENDING)
        val list = mutableListOf<ActionLog>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun deleteActionLogAll() {
        getInstance().executeTransaction { realm ->
            realm.where(ActionLog::class.java).findAll().deleteAllFromRealm()
        }
    }
}

