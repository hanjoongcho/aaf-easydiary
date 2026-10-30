---
name: android-code-refactor
description: Refactor Android Kotlin/Java code according to project guidelines (removing m-prefix, restricting Compose to presentation layer, deleting unused code, translating Korean comments/strings to English, and decomposing complex functions). Use when refactoring Android Studio code or reviewing Android source files.
---

# Android Code Refactor

Refactor Android Kotlin and Java code in Android Studio according to clean code principles and strict architectural boundary rules.

## When to Use

Use this skill whenever requested to refactor, clean up, review, or modernize Android code snippets or files from Android Studio.

## Refactoring Rules

1. **Remove `m` Prefix from Variable Names**
    - Strip legacy `m` or `m_` prefixes from member variables, fields, and global properties (e.g., `mUserName` -> `userName`, `mBinding` -> `binding`).
    - Ensure all callsites and references are updated accordingly.

2. **Compose Refactoring Scope Boundary**
    - **Only** apply or introduce Jetpack Compose refactoring if the file resides within the `presentation` package (e.g., `package com.example.app.presentation...`).
    - Do **not** touch or convert code to Compose if the file belongs to `domain`, `data`, `di`, or any other non-presentation package.

3. **Remove Unused Code**
    - Delete unused imports, dead code paths, unused private methods/properties, and unreferenced parameters.

4. **Translate Korean Comments and Hardcoded Strings to English**
    - Convert all Korean comments (inline, KDoc, javadoc) into clean, standard English.
    - Translate all hardcoded Korean string literals into English.

5. **Decompose Complex Functions**
    - Break down long or complex functions into smaller, single-responsibility helper functions with clear, descriptive names.

## Refactoring Checklist & Output Format

When refactoring code:
1. Identify package location (`presentation` vs others).
2. Clean up variable naming (`m` prefix removal).
3. Translate comments and string literals to English.
4. Purge dead/unused code.
5. Extract complex logic into smaller sub-functions.
6. Return the fully refactored, compilation-ready code block.