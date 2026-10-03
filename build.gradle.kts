// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Gradle Plugin
    id("com.android.application") version "9.4.1" apply false
    id("com.android.library") version "9.4.1" apply false

    // Kotlin - Synchronized to 2.1.10 for stability with Hilt 2.60.1
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false

    // KSP - Must match Kotlin version (2.1.10)
    id("com.google.devtools.ksp") version "2.3.6" apply false

    // Hilt - Updated to 2.60.1 to fix "Unexpected annotation value" errors
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
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
