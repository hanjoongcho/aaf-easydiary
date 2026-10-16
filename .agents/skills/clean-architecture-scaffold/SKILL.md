---
name: clean-architecture-scaffold
description: Generates Clean Architecture package structures and boilerplate code aligned with Modern Android Development (MAD) standards.
---
# Clean Architecture Scaffold Generator

Guides or generates boilerplate code for feature modules while adhering to Modern Android Development architecture rules:

1. **Layering Architecture**
   - **UI Layer**: Composable + ViewModel (define `UiState` and `UiEvent` using `StateFlow`).
   - **Domain Layer**: `UseCase` (adhere to Single Responsibility Principle with `operator fun invoke()`).
   - **Data Layer**: `Repository` (interface + implementation) and `DataSource` (Ktor/Retrofit, Room).

2. **UI State Representation**
   - Structure UI states explicitly using Sealed Classes / Interfaces (`Success`, `Error`, `Loading`).

3. **Dependency Injection (Hilt)**
   - Apply standard Hilt setups with `@HiltViewModel`, `@Inject constructor()`, `@Module`, and `@InstallIn(SingletonComponent::class)`.
