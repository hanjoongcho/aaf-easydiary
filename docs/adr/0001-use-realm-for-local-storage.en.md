# [ADR-0001] Adopt Realm for device local storage

## Status

Accepted

## Context

The EasyDiary service needs to store and quickly query data on the device that has complex relationships, such as diary body text, photo paths, and location information. Traditional Android SQLite or the Room library are based on relational data models (RDBMS) and have an impedance mismatch with object-oriented languages like Kotlin/Java, which increases maintenance cost when writing complex queries.

## Decision

We adopt **Realm (NoSQL Object Database)** as the local database. This decision is based on the library comparison and technical advantages below.

| Comparison | Realm | Room (SQLite) |
| --- | --- | --- |
| **Model structure** | Object-oriented (store objects as-is) | Relational (requires table mapping) |
| **Performance** | Very fast because there is no object serialization | Relatively slower due to SQL parsing and cursor conversion |
| **Relationship representation** | `RealmList`, direct references (concise) | Foreign keys, Join, `@Relation` (complex) |
| **Reactive UI** | Live Objects (auto-updates on data changes) | Requires LiveData/Flow integration |
| **Learning curve** | Low (use like objects) | Medium (SQL knowledge required) |

### Rationales

1. **Object-centered design:** The `Diary` model contains sub-objects such as `PhotoUri` and `Location`, and links to other diaries (`linkedDiaries`). Realm can store and retrieve this object graph without separate Join queries or serialization overhead, improving developer productivity.
2. **Handling complex data relationships:** Structures like `RealmList<PhotoUri>` can be handled without intermediate tables, simplifying integrity management for diary data.
3. **Lazy loading performance:** Even with thousands of diary entries, Realm loads data on demand, keeping memory footprint low while maintaining fast scrolling performance.
4. **Encryption support:** Combined with model-level `isEncrypt` policies, Realm's transparent DB encryption features are appropriate.

## Consequences

* **Benefits:** Reduces time spent writing SQL queries, allowing more focus on business logic (markdown sync, encryption, etc.). Observing data changes in the UI layer becomes very easy.
* **Drawbacks:** Adding the Realm library slightly increases app binary size (APK). Also, Realm objects are bound to the thread where they are created, so care is needed when passing data between threads.
