import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt.android)
}

val appCompileSdk = 37

// AGP 9.0+ / 10.0+ 대응을 위한 ApplicationExtension 적용
configure<ApplicationExtension> {
    compileSdk = appCompileSdk

    val properties =
        Properties().apply {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                load(localPropertiesFile.inputStream())
            }
        }

    signingConfigs {
        create("config") {
            keyAlias = "android"
            keyPassword = properties.getProperty("storePassword", "")
            storeFile = file(properties.getProperty("storeFile", "dummy.jks"))
            storePassword = properties.getProperty("storePassword", "")
        }
    }

    defaultConfig {
        applicationId = "me.blog.korn123.easydiary"
        minSdk = 26
        targetSdk = appCompileSdk
        versionCode = 353
        versionName = "1.4.353.202610xx0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        signingConfig = signingConfigs.getByName("config")
    }

    flavorDimensions += "buildType"
    productFlavors {
        create("gmsProd") {
            dimension = "buildType"
            signingConfig = signingConfigs.getByName("config")
        }
        create("gmsDev") {
            dimension = "buildType"
            signingConfig = signingConfigs.getByName("config")
        }
        create("foss") {
            dimension = "buildType"
            signingConfig = signingConfigs.getByName("config")
        }
        create("lab") {
            dimension = "buildType"
            signingConfig = signingConfigs.getByName("config")
        }
    }

    sourceSets {
        getByName("gmsProd") {
            manifest.srcFile("src/gms/AndroidManifest.xml")
            kotlin.directories.addAll(listOf("src/main/java", "src/gmsProd/java", "src/gms/java", "src/dummy/java"))
        }
        getByName("gmsDev") {
            manifest.srcFile("src/gms/AndroidManifest.xml")
            kotlin.directories.addAll(listOf("src/main/java", "src/gmsDev/java", "src/gms/java", "src/dummy/java"))
        }
        getByName("foss") {
            manifest.srcFile("src/foss/AndroidManifest.xml")
            kotlin.directories.addAll(listOf("src/main/java", "src/foss/java", "src/dummy/java"))
        }
        getByName("lab") {
            manifest.srcFile("src/gms/AndroidManifest.xml")
            kotlin.directories.addAll(listOf("src/main/java", "src/gmsProd/java", "src/gms/java", "src/lab/java"))
            res.directories.add("src/gmsProd/res")
        }
        getByName("androidTest") {
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        compose = true
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("config")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("config")
        }
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "META-INF/commons_release.kotlin_module",
                    "META-INF/rxjava.properties",
                    "META-INF/DEPENDENCIES",
                )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    namespace = "me.blog.korn123.easydiary"

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_18)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

configurations.all {
    exclude(group = "com.android.support")
}

configurations.matching { it.name == "fossImplementation" }.configureEach {
    exclude(group = "com.google.android.gms", module = "play-services-auth")
    exclude(group = "com.google.android.play", module = "review")
    exclude(group = "com.google.android.play", module = "review-ktx")
    exclude(group = "com.google.api-client", module = "google-api-client-android")
    exclude(group = "com.google.apis", module = "google-api-services-drive")
    exclude(group = "com.google.apis", module = "google-api-services-calendar")
    exclude(group = "com.google.http-client", module = "google-http-client-gson")
    exclude(group = "androidx.credentials", module = "credentials-play-services-auth")
    exclude(group = "com.google.android.libraries.identity.googleid", module = "googleid")
}

afterEvaluate {
    configurations.configureEach {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // androidx
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.vectordrawable)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.splashscreen)

    // compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.compose.material3.windowsizeclass)

    // room
    val roomVersion = libs.versions.room.get()
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // gms
    implementation(libs.google.android.gms.play.services.auth)
    implementation(libs.google.android.play.review)
    implementation(libs.google.android.play.review.ktx)
    implementation(libs.google.api.client.android)
    implementation(libs.google.android.gms.play.services.drive)
    implementation(libs.google.apis.google.api.services.drive)
    implementation(libs.google.apis.google.api.services.calendar)
    implementation(libs.google.http.client.gson)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.android.libraries.identity.googleid)

    // google components common
    implementation(libs.google.android.flexbox)
    implementation(libs.google.android.material)
    implementation(libs.google.code.gson)
    //noinspection NewerVersionAvailable
    implementation(libs.google.guava)

    // Apache Commons
    implementation(libs.commons.io)
    implementation(libs.org.apache.commons.lang3)
    //noinspection NewerVersionAvailable
    implementation(libs.org.apache.poi) // Version Change Prohibited

    // AAFactory
    implementation(libs.io.github.aafactory.commons) {
        exclude(group = "com.werb.eventbuskotlin", module = "eventbuskotlin")
        exclude(group = "com.werb.moretype", module = "moretype")
        exclude(group = "id.zelory", module = "compressor")
    }

    // etc.
    implementation(libs.com.kizitonwose.calendar.compose)
    implementation(libs.com.tbuonomo.dotsindicator)
    //noinspection NewerVersionAvailable
    implementation(libs.id.zelory.compressor) // Version Change Prohibited
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk7)
    implementation(libs.com.github.philjay.mpandroidchart)
    implementation(libs.com.github.chrisbanes.photoview)
    implementation(libs.com.github.zhpanvip.bannerviewpager)
    implementation(libs.com.github.skydoves.landscapist.glide)
    implementation(libs.jp.wasabeef.glide.transformations) {
        exclude(group = "com.github.bumptech.glide", module = "glide")
    }
    implementation(libs.com.github.ksoichiro.android.observablescrollview)
    implementation(libs.com.nineoldandroids.library)
    implementation(libs.com.simplecityapps.recyclerview.fastscroll)
    implementation(libs.org.jasypt.jasypt)

    // io.noties (Markwon & Prism4j)
    implementation(libs.io.noties.markwon.core)
    implementation(libs.io.noties.markwon.syntax.highlight)
    implementation(libs.io.noties.markwon.ext.tables)
    implementation(libs.io.noties.markwon.image)
    implementation(libs.io.noties.markwon.html)
    implementation(libs.io.noties.markwon.ext.strikethrough)
    implementation(libs.io.noties.markwon.linkify)
    implementation(libs.io.noties.prism4j)

    implementation(libs.com.squareup.seismic)
    implementation(libs.com.squareup.retrofit2.retrofit)
    implementation(libs.com.squareup.retrofit2.converter.gson)
    implementation(libs.com.squareup.retrofit2.converter.scalars)

    // test
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core) {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    testImplementation(libs.junit)
}
