# ADR and Diagram Writing Guide

## 📢 Purpose
Record architectural decisions and their rationale to make future review and tracking easier.

## 📚 Documents

| Document | Contents |
| --- | --- |
| [ADR-0001 — Adopt Realm for device local storage](adr/0001-use-realm-for-local-storage.en.md) | The Realm decision (superseded) |
| [ADR-0002 — Move local storage to Room with a Repository layer](adr/0002-use-room-and-repository-pattern.en.md) | Current storage structure, layers, tag model, JSON backup |
| [Model ERD](diagrams/models/erd.md) | Room entities, columns and relations (from `app/schemas/.../8.json`) |
| [Data layer](diagrams/helper/EasyDiaryDbHelper.ko.md) | `AppDatabase` · DAO · DataSource · Repository flow (Korean) |
| [Fingerprint lock screen](diagrams/activities/FingerprintLockActivity.ko.md) | `FingerprintLockActivity` entry points and authentication flow (Korean) |

## ADR Guide
* Title / filename: e.g. `docs/adr/0001-short-title.ko.md`
* Status (Accepted, Proposed, Deprecated, etc.)
* Context: describe the problem and background
* Decision: summarize the chosen solution
* Rationale: compare alternatives and explain reasons
* Consequences (or Tradeoffs): impacts, pros and cons
* Date / Authors / Related ADR / References
* An ADR that reverses an earlier decision marks that ADR `Superseded` and links the two. (`0001` → `0002` is the example)

### ADR Template
```
# [ADR-0001] Short Title

## Status

Accepted

## Context

(Describe the problem and background.)

## Decision

(Describe the chosen decision concisely.)

## Rationale

(Why this decision? Compare alternatives.)

## Consequences

(Benefits, drawbacks, migration notes.)

## Date

YYYY-MM-DD

## Authors

- Name <email>

## References

- Link or note
```

## 📋 Diagram Guide
* Title / filename: e.g. `docs/diagrams/models/erd.md`
* Clearly indicate persistent fields and relationships of models.
* Room notation: entities are `<<Entity>>`, primary keys `<<PK>>`, foreign keys and unique indices `<<FK>>` / `<<UNIQUE>>`, and value objects flattened with `@Embedded` are `<<Embedded>>`.
* Take table and column names from the schema JSON under `app/schemas/` and from the entity code.
* Prefer readability for relations: `DiaryEntity "1" --> "0..*" PhotoUriEntity : photoUris`

### Mermaid example
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
