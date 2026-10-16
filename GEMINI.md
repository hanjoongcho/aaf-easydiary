# Android Project Rules & Guidelines for Gemini Agent

This document outlines the coding standards, architecture rules, and guidelines that Gemini Agent must follow when developing new features, modifying code, or performing refactoring in this project.

## 1. UI & Layout Guidelines (Compose First)

* **UI Implementation Principle:**
  * **All new UI screens and components must be written 100% in Jetpack Compose.**
  * Existing XML-based layouts should be **maintained as-is by default**, but can be migrated to Compose when explicitly requested or during partial component refactoring.
  * When adding new UI elements to an existing XML layout, use `ComposeView`.
  * If a legacy Custom View or XML-based view is required inside Compose, wrap it using `AndroidView`.

* **Compose Coding Conventions:**
  * Use the combination of `StateFlow` and `collectAsStateWithLifecycle()` as the default pattern for state management.
  * Always provide `@Preview` annotations for UI components to ensure layout verification in IDE preview.

## 2. Architecture Guidelines (Clean Architecture Oriented)

* **Clean Architecture Principle:**
  * Strictly follow **Layered and Clean Architecture principles** across the entire codebase.
  * Clear separation of concerns (SoC) must be maintained across **Presentation, Domain, and Data** layers.

* **Layer Responsibilities & Rules:**
  * **Presentation Layer (UI/ViewModel):**
    * Houses Compose UI and ViewModel components.
    * Do NOT execute business logic directly in ViewModels; delegate to **UseCases** from the Domain Layer.
  * **Domain Layer (Business Logic):**
    * Must consist of **pure Kotlin code** without any Android Framework dependencies.
    * Defines Domain Models, **UseCases**, and **Repository Interfaces**.
  * **Data Layer (Data Source/Repository):**
    * Handles communication with actual data sources like local databases (Room) or remote APIs (Retrofit).
    * Implements Repository Interfaces from the Domain Layer and provides **Mappers** to convert Data Entities into Domain Models.

## 3. Dependency Injection (Hilt) Guidelines

* **DI Principles:**
  * Use **Dagger Hilt** as the standard DI framework for managing dependencies across all layers.
  * Explicitly define interface bindings (`@Binds`) and external library object providers (`@Provides`) in Hilt Modules.

* **Hilt Integration Patterns:**
  * **Application:** Apply `@HiltAndroidApp` annotation to the custom Application class.
  * **Activity / Fragment (Legacy XML):** Annotate with `@AndroidEntryPoint` for dependency injection.
  * **ViewModel:**
    * Annotate with `@HiltViewModel` and inject required UseCases via `@Inject constructor(...)`.
    * Inject ViewModels in Compose screens using `hiltViewModel()` (from `androidx.hilt:hilt-navigation-compose`).
  * **UseCase / Repository:**
    * UseCases inject Repository Interfaces via `@Inject constructor(...)`.
    * Repository implementations are bound using `@Binds` inside Hilt Modules installed in appropriate components (e.g., `SingletonComponent`).

## 4. Database (Room Database) Guidelines

* **Room Design & Rules:**
  * **Entity & DAO:** Use **Jetpack Room** as the standard local persistence solution, placed inside the Data Layer.
  * **Hilt Integration:** Provide `RoomDatabase` and `DAO` instances via Hilt Modules (`@Provides`, `@Singleton`) to inject them into Data Sources or Repositories.
  * **Asynchronous Data Handling:**
    * For reactive queries requiring real-time observation, return **`Flow<T>`** or `PagingSource` in DAOs.
    * CUD (Insert, Update, Delete) and single-shot Query methods must use the **`suspend` keyword**.
  * **Mandatory Model Mapping:**
    * Room `@Entity` classes are strictly Data Layer models and must not be exposed to Domain or UI layers.
    * Always implement Mapper functions to map between Data Entities and Domain Models ($\text{Data Entity} \leftrightarrow \text{Domain Model}$).

## 5. Coding Conventions & Naming Rules (Modern Android Standard)

* **Naming Conventions:**
  * **Composables:** Composable functions that emit UI (return `Unit`) must be named using **PascalCase nouns** (e.g., `UserProfileScreen()`, `PrimaryButton()`).
  * **Classes & Interfaces:** Use PascalCase with clear intent (e.g., `GetUserInfoUseCase`, `UserRepositoryImpl`).
  * **Functions & Variables:** Use camelCase (e.g., `fetchUserData()`, `isUserLoggedIn`).
  * **StateFlow / LiveData Backing Properties:** Use a leading underscore (`_`) prefix for private mutable backing properties:
    ```kotlin
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    ```
  * **Constants:** Use `UPPER_SNAKE_CASE` for `const val` or constants inside a `companion object`.

* **Visibility Modifiers & Access Control:**
  * **Principle of Least Privilege:** Always restrict visibility using `private` or `protected` for functions, variables, and helpers used only within a class scope.
  * **Minimize Default Public Visibility:** Use the `internal` modifier for classes or methods that shouldn't be exposed outside the module.
  * **Encapsulation:** Expose ViewModel states strictly as read-only interfaces (`StateFlow`, `SharedFlow`).

* **Kotlin Keywords & Language Features:**
  * **`sealed class` / `sealed interface`:** Prefer sealed hierarchies for UI States, Navigation Events, and API Responses to enable exhaustive `when` expressions.
  * **`value class` (Inline Class):** Use value classes to wrap domain primitives (e.g., IDs, currency amounts) for type safety with zero runtime overhead (`@JvmInline value class UserId(val value: String)`).
  * **`data class` / `data object`:** Use `data class` for immutable data containers (Domain Models, UI States). Use `data object` for singleton state representations.
  * **Scope Functions (`let`, `run`, `apply`, `also`, `with`):** Use scope functions with clear intent (e.g., `apply` for object initialization, `let` for null-safe operations).

## 6. Dependency & Library Usage Rules

* **Kotlin Standard Library (Stdlib) First:**
  * **DO NOT add external utility libraries (e.g., Apache Commons, Guava)** for operations that can be accomplished via Kotlin Stdlib or AndroidX KTX.
  * Utilize built-in Kotlin extension functions (`isNullOrEmpty()`, `use { }`, `readText()`, etc.) for string manipulation, I/O processing, and collection handling.
  * Refactor existing code that relies on legacy utility libraries (`StringUtils`, `IOUtils`, etc.) to equivalent Kotlin Stdlib methods.

## 7. Asynchronous & Data Stream Processing

* **Coroutines & Flow:**
  * Use **Kotlin Coroutines and Flow** for all asynchronous and stream-based operations.
  * Always execute I/O, network, database, or file processing on `Dispatchers.IO`.
  * Ensure resource safety for stream processing using `.use { }` blocks.

## 8. Code Refactoring & Pre-Commit Checklist

Before completing any task or code modification, Gemini Agent must verify the following items:

1. **Clean Architecture layer boundaries are respected:**
   * Does Presentation/UI code depend on UseCases/Domain Models instead of Data Repositories or Room Entities directly?
2. **Hilt Dependency Injection is properly configured:**
   * Are interfaces and implementations correctly bound in Hilt Modules?
3. **Naming and visibility modifiers comply with Modern Android standards:**
   * Are Composable functions named in PascalCase? Are backing properties (`_uiState`) and access modifiers (`private`) properly applied?
4. **Modern Kotlin idioms are properly utilized (`sealed class`, `value class`, `data object`, etc.)?**
5. **Entity-to-Domain Mappers are implemented and used for Room data?**
6. **Legacy external dependencies (`org.apache.commons`, etc.) are replaced with Kotlin Stdlib functions?**
7. **New UI components are implemented in Jetpack Compose instead of XML?**
8. **Room DAO methods use appropriate `suspend` functions or `Flow` return types?**
