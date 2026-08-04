package me.blog.korn123.easydiary.data.local.mapper

import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.data.local.models.DiaryEntity
import me.blog.korn123.easydiary.data.local.models.LocationEntity
import me.blog.korn123.easydiary.data.local.models.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri
import me.blog.korn123.easydiary.helper.DateUtilConstants

fun Diary.toEntity(): DiaryEntity =
    DiaryEntity(
        diaryId = this.diaryId,
        originDiaryId = this.originDiaryId,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        symbolSequence = this.symbolSequence,
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

fun DiaryEntity.toDomain(photoUris: List<PhotoUri> = emptyList()): Diary =
    Diary(
        diaryId = this.diaryId,
        originDiaryId = this.originDiaryId,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = this.dateString,
        symbolSequence = this.symbolSequence,
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
        photoUris = photoUris,
    )

fun DiaryWithPhotos.toDomain(): Diary =
    Diary(
        diaryId = this.diary.diaryId,
        originDiaryId = this.diary.originDiaryId,
        currentTimeMillis = this.diary.currentTimeMillis,
        title = this.diary.title,
        contents = this.diary.contents,
        dateString = this.diary.dateString,
        symbolSequence = this.diary.symbolSequence,
        linkedDiaries = this.diary.linkedDiaries,
        fontName = this.diary.fontName,
        fontSize = this.diary.fontSize,
        isAllDay = this.diary.isAllDay,
        isEncrypt = this.diary.isEncrypt,
        encryptKeyHash = this.diary.encryptKeyHash,
        isSelected = this.diary.isSelected,
        location =
            this.diary.location?.let {
                Location(
                    address = it.address,
                    latitude = it.latitude,
                    longitude = it.longitude,
                )
            },
        isHoliday = this.diary.isHoliday,
        photoUris = this.photoUris.map { it.toDomain() },
    )

fun PhotoUriEntity.toDomain(): PhotoUri =
    PhotoUri(
        mimeType = this.mimeType,
        photoUri = this.photoUri,
    )

fun PhotoUri.toEntity(diaryId: Int): PhotoUriEntity =
    PhotoUriEntity(
        diaryId = diaryId,
        photoUri = this.photoUri,
        mimeType = this.mimeType,
    )

fun me.blog.korn123.easydiary.models.PhotoUri.toDomain(): PhotoUri =
    PhotoUri(
        mimeType = this.mimeType,
        photoUri = this.photoUri,
    )

fun me.blog.korn123.easydiary.models.Diary.toDomain(): Diary =
    DiaryEntity(
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
