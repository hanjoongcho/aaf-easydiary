package me.blog.korn123.easydiary.data.local.mapper

import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import me.blog.korn123.easydiary.data.local.models.LocationEntity
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri
import me.blog.korn123.easydiary.helper.DateUtilConstants

fun Diary.toEntity(): DiaryEntity =
    DiaryEntity(
        diaryId = this.diaryId,
        originSequence = this.originDiaryId,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        weather = this.weather,
        linkedDiaries = this.linkedDiaries,
        fontName = this.fontName,
        fontSize = this.fontSize,
        isAllDay = this.isAllDay,
        isEncrypt = this.isEncrypt,
        encryptKeyHash = this.encryptKeyHash,
        isSelected = this.isSelected,
        location =
            this.location?.let {
                LocationEntity(
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            },
        isHoliday = this.isHoliday,
    ).apply {
        if (dateString == null) {
            dateString = DateUtils.timeMillisToDateTime(currentTimeMillis, DateUtilConstants.DATE_PATTERN_DASH)
        }
    }

fun DiaryEntity.toDomain(): Diary =
    Diary(
        diaryId = this.diaryId,
        originDiaryId = this.originSequence,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        weather = this.weather,
        linkedDiaries = this.linkedDiaries,
        fontName = this.fontName,
        fontSize = this.fontSize,
        isAllDay = this.isAllDay,
        isEncrypt = this.isEncrypt,
        encryptKeyHash = this.encryptKeyHash,
        isSelected = this.isSelected,
        location =
            this.location?.let {
                Location(
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            },
        isHoliday = this.isHoliday,
    )

fun me.blog.korn123.easydiary.models.Diary.toDomain(): Diary =
    DiaryEntity(
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        weather = this.weather,
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
    }.toDomain()
