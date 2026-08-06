package me.blog.korn123.easydiary.helper

import io.realm.RealmList
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.LocationEntity
import me.blog.korn123.easydiary.data.local.mapper.toDomain
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri

fun me.blog.korn123.easydiary.models.DDay.toDomain(): DDay =
    DDay(
        sequence = this.sequence,
        targetTimeStamp = this.targetTimeStamp,
        title = this.title,
    )

fun me.blog.korn123.easydiary.models.ActionLog.toDomain(): ActionLog =
    ActionLog(
        sequence = this.sequence,
        className = this.className,
        signature = this.signature,
        key = this.key,
        value = this.value,
    )

fun me.blog.korn123.easydiary.models.PhotoUri.toDomain(): PhotoUri =
    PhotoUri(
        mimeType = this.mimeType,
        photoUri = this.photoUri,
    )

fun me.blog.korn123.easydiary.models.Alarm.toDomain(): Alarm =
    Alarm(
        alarmId = this.sequence,
        timeInMinutes = this.timeInMinutes,
        days = this.days,
        isEnabled = this.isEnabled,
        vibrate = this.vibrate,
        soundTitle = this.soundTitle,
        soundUri = this.soundUri,
        label = this.label,
        workMode = this.workMode,
        retryCount = this.retryCount,
    )

fun me.blog.korn123.easydiary.models.Diary.toDomain(): Diary =
    DiaryEntity(
        diaryId = this.sequence,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        symbolSequence = this.weather,
        linkedDiaries = this.linkedDiaries.toList(),
        fontName = this.fontName,
        fontSize = this.fontSize,
        isAllDay = this.isAllDay,
        isEncrypt = this.isEncrypt,
        encryptKeyHash = this.encryptKeyHash,
        isSelected = this.isSelected,
        location =
            this.location?.let { location ->
                LocationEntity(
                    address = location.address,
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            },
        isHoliday = this.isHoliday,
    ).apply {
        dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtilConstants.DATE_PATTERN_DASH)
    }.toDomain(this.photoUris?.map { it.toDomain() } ?: emptyList())

fun PhotoUri.toRealm(): me.blog.korn123.easydiary.models.PhotoUri =
    me.blog.korn123.easydiary.models.PhotoUri(
        photoUri = this.photoUri ?: "",
        mimeType = this.mimeType ?: ""
    )

fun Location.toRealm(): me.blog.korn123.easydiary.models.Location =
    me.blog.korn123.easydiary.models.Location(
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude
    )

fun Diary.toRealm(): me.blog.korn123.easydiary.models.Diary =
    me.blog.korn123.easydiary.models.Diary().apply {
        val domainDiary = this@toRealm
        sequence = domainDiary.diaryId
        originSequence = domainDiary.originDiaryId
        currentTimeMillis = domainDiary.currentTimeMillis
        title = domainDiary.title
        contents = domainDiary.contents
        dateString = domainDiary.dateString
        weather = domainDiary.symbolSequence
        photoUris = RealmList<me.blog.korn123.easydiary.models.PhotoUri>().apply {
            addAll(domainDiary.photoUris.map { it.toRealm() })
        }
        linkedDiaries = RealmList<Int>().apply {
            addAll(domainDiary.linkedDiaries)
        }
        fontName = domainDiary.fontName
        fontSize = domainDiary.fontSize
        isAllDay = domainDiary.isAllDay
        isEncrypt = domainDiary.isEncrypt
        encryptKeyHash = domainDiary.encryptKeyHash
        isSelected = domainDiary.isSelected
        location = domainDiary.location?.toRealm()
        isHoliday = domainDiary.isHoliday
    }
