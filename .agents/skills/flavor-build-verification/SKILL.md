---
name: flavor-build-verification
description: Verifies all product flavors in the Android project by running Gradle build tasks (e.g. assembleDebug across all flavors). Use this skill when requested to perform full flavor build verification or compile all flavors.
---

# Flavor Build Verification Skill

## Overview
This skill provides instructions on how to verify all product flavors in the current Android project using Gradle.

## Execution Instructions
1. Execute the Gradle assemble task for debug (or release) across all flavors to ensure that all source sets (such as `foss`, `gmsDev`, `gmsProd`, `lab`, etc.) compile without errors.
2. Use the `gradle_build` tool with command line arguments like `assembleDebug` or `build` to perform the compilation.
3. Check the build output for any syntax errors, unresolved references, or resource conflicts across different product flavors.
