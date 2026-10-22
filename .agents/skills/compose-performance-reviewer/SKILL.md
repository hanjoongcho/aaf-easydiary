---
name: compose-performance-reviewer
description: Validates Jetpack Compose UI code for recomposition performance optimization, state hoisting, and Modifier conventions.
---
# Compose Performance & Style Reviewer

Enforces the following best practices when writing or reviewing Jetpack Compose code:

1. **Recomposition Optimization & Stability**
   - Recommend using `ImmutableList` or marking classes with `@Immutable` / `@Stable` to prevent unnecessary recompositions when passing collections.
   - Maintain lambda parameter passing using `remember { { ... } }` or method references (`viewModel::onEvent`).
   - Use `derivedStateOf { ... }` when complex calculations feed into UI state.

2. **State Hoisting & UDF Pattern**
   - Keep Composable functions stateless by hoisting state using the `(state: UiState, onEvent: (UiEvent) -> Unit)` pattern.

3. **Modifier Chaining Order**
   - Follow standard layout order: Layout -> Padding -> Visual -> Interaction (observe how `padding()` location affects touch target and margins).
   - Expose `modifier: Modifier = Modifier` as the first optional parameter of Composables.
