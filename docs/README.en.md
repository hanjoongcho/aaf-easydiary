# ADR and Diagram Writing Guide

## 📢 Purpose
* Record architectural decisions and their rationale to make future review and tracking easier.

## ADR Guide
* Title / filename: e.g. `docs/adr/0001-short-title.ko.md`
* Status (Accepted, Proposed, Deprecated, etc.)
* Context: describe the problem and background
* Decision: summarize the chosen solution
* Rationale: compare alternatives and explain reasons
* Consequences (or Tradeoffs): impacts, pros and cons
* Date / Authors / Related ADR / References

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
* When using Realm, recommend annotations like `<<RealmObject>>`, `<<PrimaryKey>>`, `@LinkingObjects`.
* Prefer readability for relations: `Diary "1" --> "0..1" Location : location`

### Mermaid example
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
