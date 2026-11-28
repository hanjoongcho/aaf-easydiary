package me.blog.korn123.easydiary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import me.blog.korn123.easydiary.helper.AlarmConstants

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    var alarmId: Int = 0,
    var timeInMinutes: Int = 0,
    var days: Int = 0,
    var isEnabled: Boolean = false,
    var vibrate: Boolean = false,
    var soundTitle: String? = null,
    var soundUri: String? = null,
    var label: String? = null,
    var workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING,
    var retryCount: Int = 0,

    /**
     * Destination of [AlarmConstants.WORK_MODE_DIARY_FULL_BACKUP_EXTERNAL], kept as the string of the tree uri the user
     * picked, whose grant is persisted with takePersistableUriPermission. Null on every other work mode.
     */
    var backupFolderUri: String? = null,

    /** Display name of [backupFolderUri] at the time it was picked, so the edit screen can name the folder offline. */
    var backupFolderName: String? = null,

    /** Optional zip password of the scheduled full backup. Null or empty writes an unencrypted archive. */
    var backupPassword: String? = null,

    /** [AlarmConstants.BACKUP_FILE_MODE_OVERWRITE] or [AlarmConstants.BACKUP_FILE_MODE_TIMESTAMPED]. */
    @ColumnInfo(defaultValue = "0")
    var backupFileNameMode: Int = AlarmConstants.BACKUP_FILE_MODE_OVERWRITE,
)
