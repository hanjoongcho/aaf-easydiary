# ADR 및 다이어그램 작성 가이드

## 📢 목적
* 아키텍처 결정과 근거를 기록해 추후 검토·추적을 쉽게 합니다.
 
## 📋 ADR 가이드
* 제목 / 파일명: 예시 docs/adr/0001-short-title.ko.md
* 상태(승인, 제안, 폐기 등)
* 컨텍스트: 문제 및 배경 설명
* 결정: 선택한 해결책 요약
* 선택 근거(Rationale): 대안 비교 및 이유 설명
* 결과(또는 Tradeoffs): 영향, 장단점
* 날짜 / 작성자 / 관련 ADR / 참조

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
* Realm 사용 시 <<RealmObject>>, <<PrimaryKey>>, @LinkingObjects 같은 주석 표기 권장.
* 관계 표기는 가독성 우선: Diary "1" --> "0..1" Location : location
 
### Mermaid 예시
```
classDiagram
class Diary {
    <<RealmObject>>
    +Int sequence <<PrimaryKey>>
    +String? title
    +Location? location
}
class Location {
    <<RealmObject>>
    +String? address
    +Double latitude
    +Double longitude
}
Diary "1" --> "0..1" Location : location
```
