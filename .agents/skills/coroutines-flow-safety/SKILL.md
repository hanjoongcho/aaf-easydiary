---
name: coroutines-flow-safety
description: Verifies Coroutines and Flow implementations for Lifecycle safety, Structured Concurrency, and exception handling.
---
# Coroutines & Flow Safety Checker

Checks asynchronous and reactive stream code against the following concurrency safety rules:

1. **Lifecycle-Aware Collection**
   - Mandatory use of `collectAsStateWithLifecycle()` when collecting Flows in the UI layer (Compose) to prevent background resource leaks.

2. **Exception Handling**
   - Handle Flow stream errors using the `catch { ... }` operator, and wrap single-shot coroutine operations using `runCatching` and the `Result<T>` pattern.
   - Do not swallow `CancellationException`; ensure it is rethrown to support structured concurrency cancellation.

3. **Scope & Dispatchers**
   - Bind UI-related work to `viewModelScope`, and explicitly switch I/O-bound tasks to `Dispatchers.IO` via `withContext`.
