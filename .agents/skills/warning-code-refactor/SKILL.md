---
name: warning-code-refactor
description: Refactors compiler warnings, replaces deprecated APIs, eliminates unchecked casts, and cleans up unused imports or variables in Kotlin and Android code.
---
# Warning Code Refactor Guide

Detects and resolves compiler warnings and code smells across the codebase, refactoring them into modern Kotlin and Android best practices without breaking existing functionality.

1. **Deprecated API Replacements (Modern Alternatives)**
    - `startActivityForResult` -> `ActivityResultLauncher` (`registerForActivityResult`)
    - `Handler(Looper.getMainLooper()).postDelayed` -> `lifecycleScope` / `viewModelScope` with `delay()`
    - System UI visibility flags (`SYSTEM_UI_FLAG_*`) -> `WindowInsetsControllerCompat`
    - Replace other deprecated AndroidX / Kotlin classes and methods with official recommendations.

2. **Type Safety & Safe Casts (Type Safety & Nullability)**
    - Avoid forced casting (`as`) and overuse of `@Suppress("UNCHECKED_CAST")`. Prefer safe casting (`as?`), safe calls (`?.`), smart casts, or sealed hierarchies.
    - Eliminate non-null assertion operators (`!!`). Replace them with Elvis operators (`?:`), `checkNotNull`, or `requireNotNull` for explicit exception handling.

3. **Exhaustive `when` Expressions & Unused Code Cleanup**
    - For `when` expressions handling Enums or Sealed Classes/Interfaces, explicitly handle all branches instead of relying on `else` blocks to enforce compile-time verification.
    - Remove unused variables, parameters, dead code, and redundant explicit type arguments (`<>`).
    - Clean up wildcard imports (`import foo.*`) and unused imports.

4. **Minimizing `@Suppress` Annotations**
    - Avoid using `@Suppress` to temporarily hide warnings.
    - If `@Suppress` is unavoidable due to legacy bindings or external library constraints, add an explanatory comment justifying its necessity.