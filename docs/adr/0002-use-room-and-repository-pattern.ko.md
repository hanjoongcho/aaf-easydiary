# [ADR-0002] 로컬 저장소를 Room + Repository 계층으로 전환

## 상태 (Status)

승인됨 (Accepted) — [ADR-0001](0001-use-realm-for-local-storage.ko.md)(Realm 채택)을 대체합니다.

## 컨텍스트 (Context)

ADR-0001에서 로컬 저장소로 Realm을 채택했지만, 운영 과정에서 다음 요구가 남았습니다.

* **스키마 변경을 리뷰 가능한 형태로 관리**: 일기·알람·디데이 모델이 계속 늘어나면서 버전별 스키마와 마이그레이션을 코드 리뷰에서 확인할 수 있어야 합니다. Realm은 스키마 버전 숫자와 마이그레이션 블록만 남아 실제 테이블 형태를 파일로 대조할 수 없었습니다.
* **스레드 경계 없는 데이터 전달**: ADR-0001의 단점으로 적은 대로 Realm 객체는 생성된 스레드에 묶입니다. 화면·코루틴·백그라운드 작업이 늘면서 값 객체(data class)를 자유롭게 주고받을 수 있는 모델이 필요했습니다.
* **파일 단위 백업·복구**: 사용자는 백업 파일을 ZIP으로 내려받아 보관하고 다른 기기에서 복구합니다. SQLite 파일 기반이면 DB를 JSON으로 직렬화해 한 파일에 담는 방식이 단순해집니다.
* **조건이 많은 조회**: 기간, 기분 심볼, 키워드, 태그 필터를 함께 적용하는 목록 조회가 늘었습니다. 이를 SQL 한 문장으로 표현하고 Flow로 관찰할 수 있어야 합니다.

앱은 `1.4.351`·`1.4.352`에서 Room으로 데이터를 옮겼고, `1.4.353`(2026-11-06)에서 Realm 의존성을 완전히 제거했습니다.

## 결정 (Decision)

로컬 저장소를 **Room(SQLite) + Repository 계층**으로 구성합니다.

### 1. 데이터베이스

| 항목 | 구현 |
| --- | --- |
| 데이터베이스 | `AppDatabase` (`data/local/AppDatabase.kt`), `@Database`, 버전 8, `exportSchema = true` |
| DB 파일 | `easy_diary.db` (`RoomConstants.DIARY_DB_NAME`) |
| 스키마 스냅샷 | `app/schemas/me.blog.korn123.easydiary.data.local.AppDatabase/1.json` ~ `8.json` (`ksp { arg("room.schemaLocation", "$projectDir/schemas") }`) |
| 마이그레이션 | `data/local/Migrations.kt`의 `MIGRATION_1_2` ~ `MIGRATION_7_8`, `DatabaseModule.provideAppDatabase()`에서 `addMigrations(...)`로 등록 |
| 타입 변환 | `Converters` — `List<Long>`(`DiaryEntity.linkedDiaries`)을 Gson JSON 문자열로 저장 |
| DI | Hilt `SingletonComponent` — `data/local/di/DatabaseModule.kt`, `data/remote/di/RemoteModule.kt`, `di/RepositoryModule.kt` |

엔티티 7종과 테이블 이름은 다음과 같습니다.

| 엔티티 | 테이블 | 주요 키·제약 |
| --- | --- | --- |
| `DiaryEntity` | `diaries` | PK `diaryId`(autoGenerate), `currentTimeMillis` 인덱스, `LocationEntity`를 `@Embedded(prefix = "loc_")`로 포함 |
| `PhotoUriEntity` | `photo_uris` | PK `id`, FK `diaryId` → `diaries.diaryId` (CASCADE) |
| `AlarmEntity` | `alarms` | PK `alarmId` |
| `ActionLogEntity` | `action_logs` | PK `id` |
| `DDayEntity` | `d_days` | PK `id` |
| `TagEntity` | `tags` | PK `tagId`, `normalizedName` 유니크 인덱스 |
| `DiaryTagCrossRefEntity` | `diary_tag_cross_ref` | 복합 PK (`diaryId`, `tagId`), 양쪽 FK CASCADE |

`LocationEntity`는 별도 테이블이 아니라 `diaries`의 `loc_address`·`loc_latitude`·`loc_longitude` 컬럼으로 펼쳐집니다(1:1 포함). 엔티티별 컬럼과 관계는 [모델 ERD](../diagrams/models/erd.md)에 정리했습니다.

### 2. 계층 구조

```
Compose 화면 · Activity · Fragment
        ↓ (Hilt 주입)
@HiltViewModel  ── 예: DiaryViewModel, TagPicker 계열 화면 상태
        ↓
domain/repository  ── DiaryRepository, TagRepository, AlarmRepository, DDayRepository, ActionLogRepository (인터페이스)
        ↓ (RepositoryModule @Binds)
data/repository/*RepositoryImpl  ── 도메인 모델 ↔ 엔티티 매핑(RoomMapper), 트랜잭션 경계
        ↓
data/datasource  ── DiaryDataSource 등 인터페이스, @LocalDataSource / @RemoteDataSource 한정자
        ↓
data/local/datasource/*LocalDataSourceImpl  →  data/local/dao/*Dao  →  AppDatabase(Room)
```

* 도메인 모델(`domain/model`: `Diary`, `Tag`, `Alarm`, `DDay`, `ActionLog` 등)과 Room 엔티티를 분리하고, 변환은 `data/local/mapper/RoomMapper.kt`에서만 합니다.
* Repository가 트랜잭션 경계입니다. 일기·사진·태그를 함께 쓰는 `insertDiary`, `insertTemporaryDiary`, `updateDiaryWithPhotos`, `updateDiaryTags`는 `database.withTransaction { }` 안에서 실행합니다. DAO 안에서도 여러 쿼리를 묶어야 하는 곳(`insertDiaryWithPhotos`, `replaceDiaryTagCrossRefs`, `TagDao.ensureTags` 등)은 `@Transaction`을 씁니다.
* 데이터소스는 `@LocalDataSource`(Room 구현)와 `@RemoteDataSource`(마이그레이션 초기 골격으로 남아 있는 스텁)로 나뉘고, 현재 Repository는 두 구현 중 **항상 로컬 데이터소스**를 사용합니다(`DiaryRepositoryImpl.dataSource`에 `FIXME: Remove temporary code when migrate to Jetpack Room` 주석). 원격 스텁은 빈 목록·null을 돌려줍니다.

### 3. 태그(다대다) 모델

* `TagEntity`는 표시용 `name`과 비교용 `normalizedName`을 함께 저장하고 `normalizedName`에 유니크 인덱스를 겁니다. 이름 정리 규칙(공백 정리·소문자 비교·최대 24자)은 `domain/model/Tag.kt`의 `TagRules` 한 곳에서만 정합니다.
* 일기와 태그의 연결은 `DiaryTagCrossRefEntity`가 담당하고, `DiaryWithPhotos`가 `@Relation` + `Junction`으로 일기·사진·태그를 한 번에 읽습니다.
* 태그 이름만 다루는 작업(생성·이름 변경·병합·삭제)은 `TagDao`가 트랜잭션 하나로 처리하고, 일기에 태그를 붙이는 쓰기는 일기 저장과 같은 트랜잭션에서 `DiaryDao.replaceDiaryTagCrossRefs()`로 처리합니다.

### 4. 백업·복구 (JSON)

* 백업: `Context.exportRoomData()`가 `generateRoomDataMap()`으로 각 Repository에서 읽은 데이터를 모아 ZIP 한 개를 만듭니다.
  * 파일 이름: `easy_diary.db_<yyyyMMdd_HHmmss>.zip` (`BACKUP_DB_DIRECTORY` 아래)
  * ZIP 엔트리: `easy_diary_all.json` (`RoomConstants.EXPORT_JSON_FILE_ENTRY_NAME`, pretty JSON)
  * 키: `PREFERENCE`, `META`, `ACTION_LOG`, `ALARM`, `D_DAY`, `DIARY`, `TAGS`, `DIARY_TAG_CROSS_REF`
* 복구: `Context.importRoomData()`가 같은 키를 읽어 섹션별로 기존 데이터를 지우고 다시 넣습니다. 일기 → 태그 → 태그 연결 순서로 넣어 외래 키를 만족시키고, 백업에 태그 키가 없는(태그 도입 이전) 파일도 그대로 복구됩니다. 연결 중 존재하지 않는 일기·태그를 가리키는 행은 건너뜁니다.
* SAF 경로(`exportRoomDataWithSAF`·`importRoomDataWithSAF`)도 같은 JSON을 사용하고, 복구 파일 분류는 `classifyUriByExtension()`이 `.zip` + 파일명에 `db_` 포함 여부로 판단합니다.

## 선택 근거 (Rationale)

| 대안 | 검토 결과 |
| --- | --- |
| Realm 유지 (ADR-0001) | 객체 그래프는 편하지만 스키마를 파일로 검증할 수 없고, 객체가 스레드에 묶이며, 백업은 DB 파일 복사에 가깝습니다. 위 컨텍스트의 요구를 모두 만족하지 못합니다. |
| SQLite 직접 사용(Room 없이) | 마이그레이션·쿼리 검증·컴파일 타임 검사를 직접 만들어야 합니다. Room은 스키마 JSON 내보내기와 마이그레이션 등록을 표준 방식으로 제공합니다. |
| DataStore·단일 JSON 파일 저장 | 기간·심볼·태그 조건을 조합한 조회와 부분 갱신에 적합하지 않습니다. 목록이 커질수록 전체 파일을 읽고 다시 써야 합니다. |
| Room + Repository 계층 | 조회 조건을 SQL로 표현하고 Flow로 관찰할 수 있으며, 화면이 DAO를 직접 알지 않아도 됩니다. 백업은 테이블을 JSON으로 직렬화하는 것으로 끝납니다. |

계층을 나눈 이유는 화면 코드가 특정 저장 기술에 묶이지 않게 하기 위해서입니다. 실제로 `@RemoteDataSource` 스텁과 `Config.enableJetpackRoomDatabase` 플래그처럼 전환기 흔적이 남아 있어도, 화면은 `DiaryRepository` 인터페이스만 보면 됩니다.

## 결과 (Consequences)

**장점**

* DB 스키마가 `app/schemas/*.json`으로 남아 버전별 변경을 diff로 확인할 수 있고, 마이그레이션 테스트의 기준이 됩니다.
* 엔티티·도메인 모델이 값 객체라 코루틴·Flow·Compose 상태로 자유롭게 전달됩니다.
* 기간·키워드·심볼·태그 필터를 SQL 한 문장(`DiaryDao.getDiariesWithPhotos`)으로 처리합니다.
* 백업 파일이 ZIP + JSON 하나라 사용자가 보관·이전하기 쉽고, 태그 도입 이전 백업도 복구됩니다.

**단점·유의 사항**

* DB 파일 자체는 암호화하지 않습니다. 암호화 일기는 앱 계층에서 제목·본문을 Jasypt(`utils/JasyptUtils.kt`)로 암복호화하고 `isEncrypt`·`encryptKeyHash`로 표시합니다. ADR-0001이 기대한 "Realm의 투명한 DB 암호화"에 해당하는 기능은 없으므로, 기기 저장소 보호는 OS 수준 암호화에 의존합니다.
* 전환기 코드가 남아 있습니다. `@RemoteDataSource` 스텁 4종, Repository의 `FIXME: Remove temporary code`, `Config.enableJetpackRoomDatabase` 플래그(기본값 `false`, `DiaryViewModel`의 날짜 범위 조회 분기)는 정리가 필요합니다.
* 레거시 Realm 파일은 삭제하지 않고 내보내기 경로(`Context.exportLegacyRealmFile()`, `diary.realm_`)로만 남아 있습니다.
* JSON 백업은 테이블 전체를 메모리에 올려 직렬화하므로 일기 수가 많을수록 복구 시간과 메모리 사용이 늘어납니다.

**마이그레이션 영향**

* 설치된 기기의 기존 데이터는 `MIGRATION_1_2` ~ `MIGRATION_7_8`로 이어서 올라옵니다. `MIGRATION_7_8`은 `tags`·`diary_tag_cross_ref`만 추가하고 `diaries` 테이블은 건드리지 않습니다.
* 스키마를 바꿀 때는 엔티티 수정 → 스키마 JSON 추가 → `Migrations.kt`에 마이그레이션 작성 → `DatabaseModule`에 등록 순서를 지킵니다.

## 날짜

2026-11-24

## 작성자

- Hanjoong Cho (@hanjoongcho)

## 참조

- [ADR-0001 — 디바이스 로컬 저장소로 Realm 채택](0001-use-realm-for-local-storage.ko.md) (대체됨)
- English: [0002-use-room-and-repository-pattern.en.md](0002-use-room-and-repository-pattern.en.md)
- [모델 ERD](../diagrams/models/erd.md)
- [데이터 계층 다이어그램](../diagrams/helper/EasyDiaryDbHelper.ko.md)
- 코드: `data/local/AppDatabase.kt`, `data/local/entity/*`, `data/local/dao/*`, `data/local/Migrations.kt`, `data/repository/*`, `domain/repository/*`, `data/local/mapper/RoomMapper.kt`, `extensions/Context.kt`(`exportRoomData`·`importRoomData`), `helper/Constants.kt`(`RoomConstants`)
- 앱 릴리스 노트: `app/src/main/assets/RELEASE_ko.txt` — `1.4.352`(Room 전환), `1.4.353`(Realm 제거)
