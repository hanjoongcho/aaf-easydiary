
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Gradle Plugin
    id("com.android.application") version "8.11.2" apply false
    id("com.android.library") version "8.11.2" apply false

    // Kotlin - Synchronized to 2.1.10 for stability with Hilt 2.60.1
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false

    // KSP - Must match Kotlin version (2.1.10)
    id("com.google.devtools.ksp") version "2.1.10-1.0.31" apply false

    // Hilt - Updated to 2.60.1 to fix "Unexpected annotation value" errors
    id("com.google.dagger.hilt.android") version "2.55" apply false

    id("io.realm.kotlin") version "1.4.0" apply true
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
