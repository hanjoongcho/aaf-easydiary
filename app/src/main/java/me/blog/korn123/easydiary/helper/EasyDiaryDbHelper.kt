package me.blog.korn123.easydiary.helper

import io.realm.*
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
                .schemaVersion(7)
                .migration(EasyDiaryMigration())
                .modules(Realm.getDefaultModule())
                .build()
    } 
    
    fun getInstance(): Realm = Realm.getInstance(mDiaryConfig) 
            
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

    fun readDiary(query: String?, isSensitive: Boolean = false): ArrayList<DiaryDto> {
        val mRealmInstance = Realm.getInstance(mDiaryConfig)
        val results: RealmResults<DiaryDto> = when (StringUtils.isEmpty(query)) {
            true -> mRealmInstance.where(DiaryDto::class.java).findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
            false -> {
                if (isSensitive) {
                    mRealmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query).or().contains("title", query).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                } else {
                    mRealmInstance.where(DiaryDto::class.java).beginGroup().contains("contents", query, Case.INSENSITIVE).or().contains("title", query, Case.INSENSITIVE).endGroup().findAll().sort(arrayOf("currentTimeMillis", "sequence"), arrayOf(Sort.DESCENDING, Sort.DESCENDING))
                }    
            }
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
}
