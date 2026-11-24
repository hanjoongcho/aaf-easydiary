package me.blog.korn123.easydiary.data.local.mapper

import me.blog.korn123.easydiary.data.local.entity.ActionLogEntity
import me.blog.korn123.easydiary.data.local.entity.AlarmEntity
import me.blog.korn123.easydiary.data.local.entity.DDayEntity
import me.blog.korn123.easydiary.data.local.entity.DiaryEntity
import me.blog.korn123.easydiary.data.local.entity.DiaryTagCrossRefEntity
import me.blog.korn123.easydiary.data.local.entity.LocationEntity
import me.blog.korn123.easydiary.data.local.entity.PhotoUriEntity
import me.blog.korn123.easydiary.data.local.entity.TagEntity
import me.blog.korn123.easydiary.data.local.relations.DiaryWithPhotos
import me.blog.korn123.easydiary.data.local.relations.TagWithCountRow
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.model.Diary
import me.blog.korn123.easydiary.domain.model.DiaryTagLink
import me.blog.korn123.easydiary.domain.model.Location
import me.blog.korn123.easydiary.domain.model.PhotoUri
import me.blog.korn123.easydiary.domain.model.Tag
import me.blog.korn123.easydiary.domain.model.TagRules
import me.blog.korn123.easydiary.domain.model.TagWithCount
import me.blog.korn123.easydiary.enums.ActionLogKey
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.helper.DateUtilConstants
import me.blog.korn123.easydiary.utils.DateUtils

fun Diary.toEntity(): DiaryEntity =
    DiaryEntity(
        diaryId = this.diaryId,
        originDiaryId = this.originDiaryId,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title,
        contents = this.contents,
        dateString = storedDateString(),
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
    )

/**
 * The date string this diary is stored with.
 *
 * An 'All day' diary keeps the date it already has, because that date is the value the entry is about
 * (issue #214). [Diary.currentTimeMillis] only says which moment of that day the entry was written at, and
 * a device time zone change moves that moment to another day: a write that does not edit the date itself
 * (linking an entry to another one, encrypting it, restoring a backup) must not move the entry with it. The
 * date is only derived from the instant when the diary brings none, which is what a fresh entry, a restored
 * draft and a save from the editor do.
 */
private fun Diary.storedDateString(): String =
    when (isAllDay) {
        true -> dateString?.takeIf { DateUtils.startOfDayTimeMillis(it) != null } ?: dateFromTimeMillis()
        false -> dateFromTimeMillis()
    }

private fun Diary.dateFromTimeMillis(): String = DateUtils.timeMillisToDate(currentTimeMillis, DateUtilConstants.DATE_PATTERN_DASH)

fun DiaryEntity.toDomain(
    photoUris: List<PhotoUri> = emptyList(),
    tags: List<Tag> = emptyList(),
): Diary =
    Diary(
        diaryId = this.diaryId,
        originDiaryId = this.originDiaryId,
        currentTimeMillis = this.currentTimeMillis,
        title = this.title ?: String.EMPTY,
        contents = this.contents ?: String.EMPTY,
        dateString = this.dateString,
        symbolSequence = this.symbolSequence,
        linkedDiaries = ArrayList(this.linkedDiaries),
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
        tags = tags,
    )

fun DiaryWithPhotos.toDomain(): Diary =
    Diary(
        diaryId = this.diary.diaryId,
        originDiaryId = this.diary.originDiaryId,
        currentTimeMillis = this.diary.currentTimeMillis,
        title = this.diary.title ?: String.EMPTY,
        contents = this.diary.contents ?: String.EMPTY,
        dateString = this.diary.dateString,
        symbolSequence = this.diary.symbolSequence,
        linkedDiaries = ArrayList(this.diary.linkedDiaries),
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
        tags = this.tags.map { it.toDomain() },
    )

fun TagEntity.toDomain(): Tag =
    Tag(
        tagId = this.tagId,
        name = this.name,
    )

fun TagWithCountRow.toDomain(): TagWithCount =
    TagWithCount(
        tag = this.tag.toDomain(),
        diaryCount = this.diaryCount,
    )

fun Tag.toEntity(): TagEntity =
    TagEntity(
        tagId = this.tagId,
        name = TagRules.displayName(this.name),
        normalizedName = TagRules.normalize(this.name),
    )

fun DiaryTagLink.toEntity(): DiaryTagCrossRefEntity =
    DiaryTagCrossRefEntity(
        diaryId = this.diaryId,
        tagId = this.tagId,
    )

fun DiaryTagCrossRefEntity.toDomain(): DiaryTagLink =
    DiaryTagLink(
        diaryId = this.diaryId,
        tagId = this.tagId,
    )

fun PhotoUriEntity.toDomain(): PhotoUri =
    PhotoUri(
        mimeType = this.mimeType,
        photoUri = this.photoUri,
    )

fun PhotoUri.toEntity(diaryId: Long): PhotoUriEntity =
    PhotoUriEntity(
        diaryId = diaryId,
        photoUri = this.photoUri,
        mimeType = this.mimeType,
    )

fun Alarm.toEntity(): AlarmEntity =
    AlarmEntity(
        alarmId = this.alarmId,
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

fun AlarmEntity.toDomain(): Alarm =
    Alarm(
        alarmId = this.alarmId,
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

fun ActionLog.toEntity(): ActionLogEntity =
    ActionLogEntity(
        id = this.id,
        className = this.className,
        signature = this.signature,
        key = this.key.name,
        value = this.value,
    )

fun ActionLogEntity.toDomain(): ActionLog =
    ActionLog(
        id = this.id,
        className = this.className,
        signature = this.signature,
        key = ActionLogKey.fromString(this.key),
        value = this.value,
    )

fun DDay.toEntity(): DDayEntity =
    DDayEntity(
        id = this.id,
        targetTimeStamp = this.targetTimeStamp,
        title = this.title,
    )

fun DDayEntity.toDomain(): DDay =
    DDay(
        id = this.id,
        targetTimeStamp = this.targetTimeStamp,
        title = this.title ?: String.EMPTY,
    )
