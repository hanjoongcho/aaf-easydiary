package me.blog.korn123.easydiary.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
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
                `weather` INTEGER NOT NULL, 
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
            """.trimIndent()
        )

        // 2. 기존 데이터 복사 (photoUris 제외)
        db.execSQL(
            """
            INSERT INTO `diaries_new` (
                `diaryId`, `originSequence`, `currentTimeMillis`, `title`, `contents`, 
                `dateString`, `weather`, `linkedDiaries`, `fontName`, 
                `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude`
            )
            SELECT 
                `sequence`, `originSequence`, `currentTimeMillis`, `title`, `contents`, 
                `dateString`, `weather`, `linkedDiaries`, `fontName`, 
                `fontSize`, `isAllDay`, `isEncrypt`, `encryptKeyHash`, `isSelected`, 
                `isHoliday`, `loc_address`, `loc_latitude`, `loc_longitude` 
            FROM `diaries`
            """.trimIndent()
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
            """.trimIndent()
        )

        // 5. 성능 향상을 위한 diaryId 인덱스 생성
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_photo_uris_diaryId` ON `photo_uris` (`diaryId`)")
    }
}