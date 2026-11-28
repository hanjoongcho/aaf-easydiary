package me.blog.korn123.easydiary.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. 새로운 스키마로 임시 테이블 생성 (photoUris 삭제, sequence -> diaryId)
            db.execSQL(
                """
                CREATE TABLE `diaries_new` (
                    `diaryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `originSequence` INTEGER NOT NULL, 
                    `currentTimeMillis` INTEGER NOT NULL, 
                    `title` TEXT, 
                    `contents` TEXT, 
                    `dateString` TEXT, 
                    `symbolSequence` INTEGER NOT NULL, 
                    `linkedDiaries` TEXT NOT NULL, 
                    `fontName` TEXT, 
                    `fontSize` REAL NOT NULL, 
                    `isAllDay` INTEGER NOT NULL, 
                    `isEncrypt` INTEGER NOT NULL, 
                    `encryptKeyHash` TEXT, 
                    `isSelected` INTEGER NOT NULL, 
                    `isHoliday` INTEGER NOT NULL, 
                    `loc_address` TEXT, 
                    `loc_latitude` REAL, 
                    `loc_longitude` REAL
                )
                """.trimIndent(),
            )

            // 2. 기존 데이터 복사 (photoUris 제외)
            db.execSQL(
                """
                INSERT INTO `diaries_new` (
                    `diaryId`, `originSequence`, `currentTimeMillis`, `title`, `contents`, 
                    `dateString`, `symbolSequence`, `linkedDiaries`, `fontName`, 
                    `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                    `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude`
                )
                SELECT 
                    `sequence`, `originSequence`, `currentTimeMillis`, `title`, `contents`, 
                    `dateString`, `weather`, `linkedDiaries`, `fontName`, 
                    `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                    `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude` 
                FROM `diaries`
                """.trimIndent(),
            )

            // 3. 기존 테이블 삭제 및 이름 변경
            db.execSQL("DROP TABLE `diaries`")
            db.execSQL("ALTER TABLE `diaries_new` RENAME TO `diaries`")

            // 4. photo_uris 테이블 생성 (DiaryEntity의 diaryId를 참조하는 외래키 포함)
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `photo_uris` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `diaryId` INTEGER NOT NULL, 
                    `photoUri` TEXT, 
                    `mimeType` TEXT, 
                    FOREIGN KEY(`diaryId`) REFERENCES `diaries`(`diaryId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
                """.trimIndent(),
            )

            // 5. 성능 향상을 위한 diaryId 인덱스 생성
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_uris_diaryId` ON `photo_uris` (`diaryId`)")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // SQLite 3.25.0 미만 버전 대응을 위해 테이블 재구성 방식 사용
            db.execSQL(
                """
                CREATE TABLE `diaries_new` (
                    `diaryId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `originDiaryId` INTEGER NOT NULL, 
                    `currentTimeMillis` INTEGER NOT NULL, 
                    `title` TEXT, 
                    `contents` TEXT, 
                    `dateString` TEXT, 
                    `symbolSequence` INTEGER NOT NULL, 
                    `linkedDiaries` TEXT NOT NULL, 
                    `fontName` TEXT, 
                    `fontSize` REAL NOT NULL, 
                    `isAllDay` INTEGER NOT NULL, 
                    `isEncrypt` INTEGER NOT NULL, 
                    `encryptKeyHash` TEXT, 
                    `isSelected` INTEGER NOT NULL, 
                    `isHoliday` INTEGER NOT NULL, 
                    `loc_address` TEXT, 
                    `loc_latitude` REAL, 
                    `loc_longitude` REAL
                )
                """.trimIndent(),
            )

            db.execSQL(
                """
                INSERT INTO `diaries_new` (
                    `diaryId`, `originDiaryId`, `currentTimeMillis`, `title`, `contents`, 
                    `dateString`, `symbolSequence`, `linkedDiaries`, `fontName`, 
                    `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                    `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude`
                )
                SELECT 
                    `diaryId`, `originSequence`, `currentTimeMillis`, `title`, `contents`, 
                    `dateString`, `symbolSequence`, `linkedDiaries`, `fontName`, 
                    `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                    `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude` 
                FROM `diaries`
                """.trimIndent(),
            )

            db.execSQL("DROP TABLE `diaries`")
            db.execSQL("ALTER TABLE `diaries_new` RENAME TO `diaries`")
        }
    }

val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `alarms` (
                    `alarmId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `timeInMinutes` INTEGER NOT NULL, 
                    `days` INTEGER NOT NULL, 
                    `isEnabled` INTEGER NOT NULL, 
                    `vibrate` INTEGER NOT NULL, 
                    `soundTitle` TEXT, 
                    `soundUri` TEXT, 
                    `label` TEXT, 
                    `workMode` INTEGER NOT NULL, 
                    `retryCount` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `action_logs` (
                    `sequence` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `className` TEXT, 
                    `signature` TEXT, 
                    `key` TEXT, 
                    `value` TEXT
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `d_days` (
                    `sequence` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `targetTimeStamp` INTEGER NOT NULL, 
                    `title` TEXT
                )
                """.trimIndent(),
            )
        }
    }

val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // action_logs: sequence -> id
            db.execSQL(
                """
                CREATE TABLE `action_logs_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `className` TEXT, 
                    `signature` TEXT, 
                    `key` TEXT, 
                    `value` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `action_logs_new` (`id`, `className`, `signature`, `key`, `value`)
                SELECT `sequence`, `className`, `signature`, `key`, `value` FROM `action_logs`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `action_logs`")
            db.execSQL("ALTER TABLE `action_logs_new` RENAME TO `action_logs`")

            // d_days: sequence -> id
            db.execSQL(
                """
                CREATE TABLE `d_days_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `targetTimeStamp` INTEGER NOT NULL, 
                    `title` TEXT
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `d_days_new` (`id`, `targetTimeStamp`, `title`)
                SELECT `sequence`, `targetTimeStamp`, `title` FROM `d_days`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `d_days`")
            db.execSQL("ALTER TABLE `d_days_new` RENAME TO `d_days`")
        }
    }

val MIGRATION_6_7 =
    object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_diaries_currentTimeMillis` ON `diaries` (`currentTimeMillis`)")
        }
    }

val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Issue #270: a diary carries any number of tags. The existing `diaries` table is not touched.
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tags` (
                    `tagId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `normalizedName` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_tags_normalizedName` ON `tags` (`normalizedName`)",
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `diary_tag_cross_ref` (
                    `diaryId` INTEGER NOT NULL,
                    `tagId` INTEGER NOT NULL,
                    PRIMARY KEY(`diaryId`, `tagId`),
                    FOREIGN KEY(`diaryId`) REFERENCES `diaries`(`diaryId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`tagId`) REFERENCES `tags`(`tagId`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_diary_tag_cross_ref_diaryId` ON `diary_tag_cross_ref` (`diaryId`)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_diary_tag_cross_ref_tagId` ON `diary_tag_cross_ref` (`tagId`)",
            )
        }
    }

val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Issue #302 step 3: a schedule may now run a full backup into a folder the user picked, so the alarm row
            // keeps that folder, the folder name shown on the edit screen, the optional zip password and the file
            // naming mode. Existing rows keep a null folder and the overwrite mode, which none of the other work
            // modes read.
            db.execSQL("ALTER TABLE `alarms` ADD COLUMN `backupFolderUri` TEXT")
            db.execSQL("ALTER TABLE `alarms` ADD COLUMN `backupFolderName` TEXT")
            db.execSQL("ALTER TABLE `alarms` ADD COLUMN `backupPassword` TEXT")
            db.execSQL("ALTER TABLE `alarms` ADD COLUMN `backupFileNameMode` INTEGER NOT NULL DEFAULT 0")
        }
    }
