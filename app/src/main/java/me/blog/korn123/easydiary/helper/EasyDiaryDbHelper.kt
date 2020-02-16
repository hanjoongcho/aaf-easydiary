package me.blog.korn123.easydiary.helper

import io.realm.*
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
                .schemaVersion(8)
                .migration(EasyDiaryMigration())
                .modules(Realm.getDefaultModule())
                .build()
    } 
    
    fun getInstance(): Realm = Realm.getInstance(mDiaryConfig)


    /***************************************************************************************************
     *   Manage DiaryDto model
     *
     ***************************************************************************************************/
    fun insertDiary(currentTimeMillis: Long, title: String, contents: String) {
        Realm.getInstance(mDiaryConfig).executeTransaction { realm ->
            var sequence = 1
            if (realm.where(DiaryDto::class.java).count() > 0L) {
                val number = realm.where(DiaryDto::class.java).max("sequence")
                number?.let {
                    sequence = it.toInt().plus(1)
                }
            }
            val diaryDto = DiaryDto(sequence, currentTimeMillis, title, contents)
            realm.insert(diaryDto)
        }
    }

    fun insertDiary(diaryDto: DiaryDto) {
        Realm.getInstance(mDiaryConfig).executeTransaction { realm ->
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
        val realm = Realm.getInstance(mDiaryConfig)
        val firstItemTimeMillis = (realm.where(DiaryDto::class.java).min("currentTimeMillis") ?: 0L).toLong()
        return realm.where(DiaryDto::class.java).equalTo("currentTimeMillis", firstItemTimeMillis).findFirst()
    }

    fun countDiaryAll(): Long {
        return Realm.getInstance(mDiaryConfig).where(DiaryDto::class.java).count()
    }

    fun readDiary(query: String?, isSensitive: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0, symbolSequence: Int = 0): ArrayList<DiaryDto> {
        val mRealmInstance = Realm.getInstance(mDiaryConfig)
        var results: RealmResults<DiaryDto> = when (StringUtils.isEmpty(query)) {
            true -> {
                mRealmInstance.where(DiaryDto::class.java).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                
            }
            false -> {
                if (isSensitive) {
                    mRealmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query).or().contains("title", query).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                } else {
                    mRealmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query, Case.INSENSITIVE).or().contains("title", query, Case.INSENSITIVE).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
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
        if (symbolSequence > 0) {
            results = results.where().equalTo("weather", symbolSequence).findAll()
        }

        val list = ArrayList<DiaryDto>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun readDiaryBy(sequence: Int): DiaryDto {
        return readDiaryBy(Realm.getInstance(mDiaryConfig), sequence)
    }

    fun readDiaryBy(realmInstance: Realm, sequence: Int): DiaryDto {
        return realmInstance.where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()!!
    }

    fun readDiaryByDateString(dateString: String?): List<DiaryDto> {
        val results: RealmResults<DiaryDto> = Realm.getInstance(mDiaryConfig).where(DiaryDto::class.java).equalTo("dateString", dateString).findAll().sort("sequence", Sort.DESCENDING)
        val list = ArrayList<DiaryDto>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun selectPhotoUriAll(): List<PhotoUriDto> {
        val results = Realm.getInstance(mDiaryConfig).where(PhotoUriDto::class.java).findAll().sort("photoUri", Sort.ASCENDING)
        val list = ArrayList<PhotoUriDto>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun countPhotoUriBy(uriString: String): Int {
        val count = Realm.getInstance(mDiaryConfig).where(PhotoUriDto::class.java).equalTo("photoUri", uriString).count()
        return count.toInt()
    }

    fun countDiaryBy(dateString: String): Int = Realm.getInstance(mDiaryConfig).where(DiaryDto::class.java).equalTo("dateString", dateString).count().toInt()

    fun updateDiary(diaryDto: DiaryDto) {
        Realm.getInstance(mDiaryConfig).executeTransaction { realm -> realm.insertOrUpdate(diaryDto) }
    }

    fun deleteDiary(sequence: Int) {
        val mRealmInstance = Realm.getInstance(mDiaryConfig)
        val diaryDto = mRealmInstance.where(DiaryDto::class.java).equalTo("sequence", sequence).findFirst()
        if (diaryDto != null) {
            mRealmInstance.beginTransaction()
            diaryDto.deleteFromRealm()
            mRealmInstance.commitTransaction()
        }
    }


    /***************************************************************************************************
     *   Manage Alarm model
     *
     ***************************************************************************************************/
    fun insertAlarm(alarm: Alarm) {
        Realm.getInstance(mDiaryConfig).executeTransaction { realm ->
            var sequence = 1
            if (realm.where(Alarm::class.java).count() > 0L) {
                val number = realm.where(Alarm::class.java).max("sequence")
                number?.let {
                    sequence = it.toInt().plus(1)
                }
            }
            alarm.sequence = sequence
            realm.insert(alarm)
        }
    }

    fun countAlarmAll(): Long {
        return Realm.getInstance(mDiaryConfig).where(Alarm::class.java).count()
    }

    fun readAlarmAll(): List<Alarm> {
        val results = Realm.getInstance(mDiaryConfig).where(Alarm::class.java).findAll().sort("sequence", Sort.ASCENDING)
        val list = mutableListOf<Alarm>()
        list.addAll(results.subList(0, results.size))
        return list
    }

    fun readAlarmBy(sequence: Int): Alarm? {
        return readAlarmBy(Realm.getInstance(mDiaryConfig), sequence)
    }

    private fun readAlarmBy(realmInstance: Realm, sequence: Int): Alarm? {
        return realmInstance.where(Alarm::class.java).equalTo("sequence", sequence).findFirst()
    }
}
