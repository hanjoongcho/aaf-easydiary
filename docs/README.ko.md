# ADR 및 다이어그램 작성 가이드

## 📢 목적
아키텍처 결정과 근거를 기록해 추후 검토·추적을 쉽게 합니다.

## 📚 문서 목록

| 문서 | 내용 |
| --- | --- |
| [ADR-0001 — 디바이스 로컬 저장소로 Realm 채택](adr/0001-use-realm-for-local-storage.ko.md) | Realm 채택 결정 (대체됨) |
| [ADR-0002 — 로컬 저장소를 Room + Repository 계층으로 전환](adr/0002-use-room-and-repository-pattern.ko.md) | 현행 저장소 구조, 계층, 태그 모델, JSON 백업 |
| [모델 ERD](diagrams/models/erd.md) | Room 엔티티·컬럼·관계 (`app/schemas/.../8.json` 기준) |
| [데이터 계층](diagrams/helper/EasyDiaryDbHelper.ko.md) | `AppDatabase` · DAO · DataSource · Repository 흐름 |
| [지문 잠금 화면](diagrams/activities/FingerprintLockActivity.ko.md) | `FingerprintLockActivity` 진입 경로와 인증 흐름 |

## 📋 ADR 가이드
* 제목 / 파일명: 예시 docs/adr/0001-short-title.ko.md
* 상태(승인, 제안, 폐기 등)
* 컨텍스트: 문제 및 배경 설명
* 결정: 선택한 해결책 요약
* 선택 근거(Rationale): 대안 비교 및 이유 설명
* 결과(또는 Tradeoffs): 영향, 장단점
* 날짜 / 작성자 / 관련 ADR / 참조
* 이전 결정을 뒤집는 ADR은 이전 ADR의 상태를 `Superseded`로 바꾸고 서로 링크합니다. (`0001` → `0002` 예시)

### ADR 템플릿
```
# [ADR-0001] 간단한 제목

## 상태

Accepted

## 컨텍스트

(문제와 배경을 기술하세요.)

## 결정

(선택한 결정을 간결히 기술하세요.)

## 선택 근거

(이 결정을 선택한 이유와 다른 옵션 비교.)

## 결과

(장점, 단점, 마이그레이션 영향 등.)

## 날짜

YYYY-MM-DD

## 작성자

- 이름 <email>

## 참조

- 링크 또는 메모
```

## 📋 다이어그램 가이드
* 제목 / 파일명: 예시 docs/diagrams/models/erd.md
* 모델의 영속 필드와 연관 관계를 명확히 표기.
* Room 기준 표기 권장: 엔티티는 `<<Entity>>`, 기본 키는 `<<PK>>`, 외래 키·유니크 인덱스는 `<<FK>>`·`<<UNIQUE>>`, `@Embedded`로 펼친 값 객체는 `<<Embedded>>`.
* 테이블 이름·컬럼 이름은 `app/schemas/`의 스키마 JSON과 엔티티 코드에서 그대로 가져옵니다.
* 관계 표기는 가독성 우선: DiaryEntity "1" --> "0..*" PhotoUriEntity : photoUris

### Mermaid 예시
```
classDiagram
class DiaryEntity {
    <<Entity>>
    +Long diaryId <<PK>>
    +Long currentTimeMillis
    +String? title
    +LocationEntity? location <<Embedded>>
}
class PhotoUriEntity {
    <<Entity>>
    +Int id <<PK>>
    +Long diaryId <<FK>>
    +String? photoUri
}
class LocationEntity {
    <<Embedded>>
    +String? address
    +Double latitude
    +Double longitude
}
DiaryEntity "1" *-- "0..1" LocationEntity : loc_
DiaryEntity "1" --> "0..*" PhotoUriEntity : photoUris
```
