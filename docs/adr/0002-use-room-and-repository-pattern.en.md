# [ADR-0002] Move local storage to Room with a Repository layer

## Status

Accepted — supersedes [ADR-0001](0001-use-realm-for-local-storage.en.md) (Realm for local storage).

## Context

ADR-0001 chose Realm as the local database. Operating the app left these needs open.

* **A schema that can be reviewed as a file**: as the diary, alarm and D-Day models kept growing, each version's schema and its migration had to be checkable in code review. Realm left only a schema version number and a migration block, with no file to compare the real table shape against.
* **Passing data across threads**: a drawback ADR-0001 already recorded is that Realm objects are bound to the thread that created them. With more screens, coroutines and background work, the app needs value objects (data classes) that move freely.
* **File-based backup and restore**: the writer keeps a ZIP backup and restores it on another device. With SQLite underneath, serializing the database to a single JSON file inside that ZIP is straightforward.
* **Queries with several conditions**: the entry list combines a date range, a symbol, a keyword and a tag filter. That should be one SQL statement, observed as a Flow.

The app moved its data to Room in `1.4.351` and `1.4.352`, and removed the Realm dependency completely in `1.4.353` (2026-11-06).

## Decision

The local store is **Room (SQLite) with a Repository layer**.

### 1. Database

| Item | Implementation |
| --- | --- |
| Database | `AppDatabase` (`data/local/AppDatabase.kt`), `@Database`, version 8, `exportSchema = true` |
| Database file | `easy_diary.db` (`RoomConstants.DIARY_DB_NAME`) |
| Schema snapshots | `app/schemas/me.blog.korn123.easydiary.data.local.AppDatabase/1.json` … `8.json` (`ksp { arg("room.schemaLocation", "$projectDir/schemas") }`) |
| Migrations | `MIGRATION_1_2` … `MIGRATION_7_8` in `data/local/Migrations.kt`, registered with `addMigrations(...)` in `DatabaseModule.provideAppDatabase()` |
| Type conversion | `Converters` — stores `List<Long>` (`DiaryEntity.linkedDiaries`) as a Gson JSON string |
| DI | Hilt `SingletonComponent` — `data/local/di/DatabaseModule.kt`, `data/remote/di/RemoteModule.kt`, `di/RepositoryModule.kt` |

The seven entities and their tables:

| Entity | Table | Key and constraints |
| --- | --- | --- |
| `DiaryEntity` | `diaries` | PK `diaryId` (autoGenerate), index on `currentTimeMillis`, embeds `LocationEntity` with `@Embedded(prefix = "loc_")` |
| `PhotoUriEntity` | `photo_uris` | PK `id`, FK `diaryId` → `diaries.diaryId` (CASCADE) |
| `AlarmEntity` | `alarms` | PK `alarmId` |
| `ActionLogEntity` | `action_logs` | PK `id` |
| `DDayEntity` | `d_days` | PK `id` |
| `TagEntity` | `tags` | PK `tagId`, unique index on `normalizedName` |
| `DiaryTagCrossRefEntity` | `diary_tag_cross_ref` | Composite PK (`diaryId`, `tagId`), both FKs CASCADE |

`LocationEntity` is not a table of its own: it is flattened into the `loc_address`, `loc_latitude` and `loc_longitude` columns of `diaries` (a 1:1 embed). Columns and relations per entity are in the [model ERD](../diagrams/models/erd.md).

### 2. Layers

```
Compose screens · Activities · Fragments
        ↓ (Hilt injection)
@HiltViewModel  — e.g. DiaryViewModel, tag picker screen state
        ↓
domain/repository  — DiaryRepository, TagRepository, AlarmRepository, DDayRepository, ActionLogRepository (interfaces)
        ↓ (@Binds in RepositoryModule)
data/repository/*RepositoryImpl  — domain ↔ entity mapping (RoomMapper), transaction boundary
        ↓
data/datasource  — DiaryDataSource and friends, qualified @LocalDataSource / @RemoteDataSource
        ↓
data/local/datasource/*LocalDataSourceImpl  →  data/local/dao/*Dao  →  AppDatabase (Room)
```

* Domain models (`domain/model`: `Diary`, `Tag`, `Alarm`, `DDay`, `ActionLog`, …) are kept apart from the Room entities, and `data/local/mapper/RoomMapper.kt` is the only place that converts between them.
* The repository owns the transaction boundary. Writes that touch an entry together with its photos and tags — `insertDiary`, `insertTemporaryDiary`, `updateDiaryWithPhotos`, `updateDiaryTags` — run inside `database.withTransaction { }`. DAO methods that group several statements (`insertDiaryWithPhotos`, `replaceDiaryTagCrossRefs`, `TagDao.ensureTags`, …) use `@Transaction`.
* Data sources come in `@LocalDataSource` (the Room implementation) and `@RemoteDataSource` (a stub left as scaffolding from the migration). Every repository currently uses the **local** source only (`DiaryRepositoryImpl.dataSource` carries `FIXME: Remove temporary code when migrate to Jetpack Room`); the remote stubs return empty lists or null.

### 3. Tags (many-to-many)

* `TagEntity` stores the display `name` and the comparison `normalizedName`, with a unique index on the latter. The naming rule (trimmed, whitespace collapsed, case-insensitive comparison, at most 24 characters) lives only in `TagRules` (`domain/model/Tag.kt`).
* `DiaryTagCrossRefEntity` holds the attachment between an entry and a tag, and `DiaryWithPhotos` reads entry, photos and tags in one query through `@Relation` + `Junction`.
* Work on tag names alone (create, rename, merge, delete) is a single transaction in `TagDao`; attaching tags to an entry happens in the same transaction as writing the entry, through `DiaryDao.replaceDiaryTagCrossRefs()`.

### 4. Backup and restore (JSON)

* Backup: `Context.exportRoomData()` collects the data of each repository through `generateRoomDataMap()` and writes one ZIP.
  * File name: `easy_diary.db_<yyyyMMdd_HHmmss>.zip` under `BACKUP_DB_DIRECTORY`.
  * ZIP entry: `easy_diary_all.json` (`RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME`, pretty JSON).
  * Keys: `PREFERENCE`, `META`, `ACTION_LOG`, `ALARM`, `D_DAY`, `DIARY`, `TAGS`, `DIARY_TAG_CROSS_REF`.
* Restore: `Context.importRoomData()` reads the same keys, clears each section and writes it back. Entries, then tags, then tag attachments are written in that order so the foreign keys hold, and a backup written before tags existed (no tag keys) restores as it is. Attachments that point at an entry or a tag missing from this database are skipped.
* The SAF paths (`exportRoomDataWithSAF`, `importRoomDataWithSAF`) use the same JSON, and a restore file is classified by `classifyUriByExtension()` — a `.zip` whose name contains `db_`.

## Rationale

| Alternative | Outcome of the review |
| --- | --- |
| Keep Realm (ADR-0001) | The object graph is convenient, but the schema cannot be verified as a file, objects are bound to their thread, and a backup is close to copying the database file. It does not meet the needs above. |
| Use SQLite directly, without Room | Migration, query verification and compile-time checks would all have to be built. Room provides schema export and migration registration in a standard way. |
| DataStore or a single JSON file | Poor fit for combining date, symbol and tag conditions and for partial updates; the whole file has to be read and rewritten as the list grows. |
| Room with a Repository layer | Query conditions become SQL observed as Flow, screens do not need to know a DAO, and a backup is just serializing the tables. |

The layer split exists so that screen code is not tied to a storage technology. Even while transition traces remain — the `@RemoteDataSource` stubs and the `Config.enableJetpackRoomDatabase` flag — a screen only sees the `DiaryRepository` interface.

## Consequences

**Benefits**

* The database schema is kept in `app/schemas/*.json`, so each version's change is visible as a diff and usable as the baseline for migration tests.
* Entities and domain models are value objects, so they can be passed through coroutines, Flows and Compose state freely.
* Date, keyword, symbol and tag filters are handled by one SQL statement (`DiaryDao.getDiariesWithPhotos`).
* A backup is one ZIP with one JSON file, easy for the writer to keep and move, and backups from before tags still restore.

**Drawbacks and cautions**

* The database file itself is not encrypted. An encrypted entry has its title and contents encrypted and decrypted in the app layer with Jasypt (`utils/JasyptUtils.kt`) and is marked by `isEncrypt` / `encryptKeyHash`. The "transparent database encryption" ADR-0001 expected from Realm has no counterpart here, so storage protection rests on OS-level encryption.
* Transition code remains: four `@RemoteDataSource` stubs, the `FIXME: Remove temporary code` in the repositories, and the `Config.enableJetpackRoomDatabase` flag (default `false`, used by `DiaryViewModel` to choose the date-range read path).
* The legacy Realm file is not deleted; it remains only behind the export path (`Context.exportLegacyRealmFile()`, `diary.realm_`).
* A JSON backup holds whole tables in memory while serializing, so restore time and memory grow with the number of entries.

**Migration impact**

* Data already on a device is carried forward by `MIGRATION_1_2` … `MIGRATION_7_8`. `MIGRATION_7_8` adds only `tags` and `diary_tag_cross_ref` and does not touch `diaries`.
* To change the schema: edit the entity, add the schema JSON, write the migration in `Migrations.kt`, and register it in `DatabaseModule`, in that order.

## Date

2026-11-24

## Authors

- Hanjoong Cho (@hanjoongcho)

## References

- [ADR-0001 — Adopt Realm for device local storage](0001-use-realm-for-local-storage.en.md) (superseded)
- Korean: [0002-use-room-and-repository-pattern.ko.md](0002-use-room-and-repository-pattern.ko.md)
- [Model ERD](../diagrams/models/erd.md)
- [Data layer diagram](../diagrams/helper/EasyDiaryDbHelper.ko.md) (Korean)
- Code: `data/local/AppDatabase.kt`, `data/local/entity/*`, `data/local/dao/*`, `data/local/Migrations.kt`, `data/repository/*`, `domain/repository/*`, `data/local/mapper/RoomMapper.kt`, `extensions/Context.kt` (`exportRoomData`, `importRoomData`), `helper/Constants.kt` (`RoomConstants`)
- App release notes: `app/src/main/assets/RELEASE_en.txt` — `1.4.352` (migration to Room), `1.4.353` (Realm removed)
