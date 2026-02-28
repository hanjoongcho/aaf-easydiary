import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("realm-android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

val appCompileSdk = 36

android {
    compileSdk = appCompileSdk

    val properties =
        Properties().apply {
            load(rootProject.file("local.properties").inputStream())
        }

    signingConfigs {
        create("config") {
            keyAlias = "android"
            keyPassword = properties["storePassword"] as String
            storeFile = file(properties["storeFile"] as String)
            storePassword = properties["storePassword"] as String
        }
    }

    defaultConfig {
        applicationId = "me.blog.korn123.easydiary"
        minSdk = 23
        targetSdk = appCompileSdk
        versionCode = 343
        versionName = "1.4.343.202602xx0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
        renderscriptTargetApi = 18
        renderscriptSupportModeEnabled = true
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
            java.srcDirs("src/main/java", "src/gmsProd/java", "src/gms/java", "src/dummy/java")
        }
        getByName("gmsDev") {
            manifest.srcFile("src/gms/AndroidManifest.xml")
            java.srcDirs("src/main/java", "src/gmsDev/java", "src/gms/java", "src/dummy/java")
        }
        getByName("foss") {
            manifest.srcFile("src/foss/AndroidManifest.xml")
            java.srcDirs("src/main/java", "src/foss/java", "src/dummy/java")
        }
        getByName("lab") {
            manifest.srcFile("src/gms/AndroidManifest.xml")
            java.srcDirs("src/main/java", "src/gmsProd/java", "src/gms/java", "src/lab/java")
            res.srcDirs("src/gmsProd/res")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_18)
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

configurations.matching { it.name == "fossImplementation" }.all {
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
    configurations.all {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // androidx
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.vectordrawable:vectordrawable:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-ktx:1.12.3")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0") {
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("androidx.browser:browser:1.9.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    // compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.runtime:runtime-livedata:1.10.1")
    implementation("androidx.compose.foundation:foundation-layout:1.10.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.1")
    implementation("androidx.compose.ui:ui-graphics:1.10.1")
    implementation("androidx.compose.ui:ui-tooling:1.10.1")
    implementation("androidx.credentials:credentials:1.3.0")

    // gms
    implementation("com.google.android.gms:play-services-auth:21.5.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("com.google.api-client:google-api-client-android:2.8.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0")
    implementation("com.google.http-client:google-http-client-gson:2.1.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // google components common
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.guava:guava:33.5.0-android")

    // apache commons
    // From version 2.7, it calls the java.nio.file API internally.
    // NIO is available from Android 8.0 (API Level 26)
    // Therefore, we must use version 2.6 before the minimum supported Android version becomes API Level 26 or higher.
    implementation("commons-io:commons-io:2.21.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("org.apache.poi:poi:3.13")

    // AAFactory
    implementation("io.github.aafactory:commons:1.2.0") {
        exclude(group = "com.werb.eventbuskotlin", module = "eventbuskotlin")
        exclude(group = "com.werb.moretype", module = "moretype")
        exclude(group = "id.zelory", module = "compressor")
    }
//    implementation project(":aafactory-commons")

    // etc.
    implementation("com.github.woxingxiao:BubbleSeekBar:3.20")
    implementation("com.tbuonomo:dotsindicator:5.1.0")
    implementation("id.zelory:compressor:2.1.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
    implementation("com.github.PhilJay:MPAndroidChart:v3.0.3")
    implementation("com.github.chrisbanes:PhotoView:2.1.3")
    implementation("com.github.QuadFlask:colorpicker:0.0.13") // Version Change Prohibited: This is the last version available for download from JitPack.
    implementation("com.github.amlcurran.showcaseview:library:5.4.3")
    implementation("com.github.zhpanvip:bannerviewpager:3.5.5")
//    implementation ("com.github.bumptech.glide:glide:4.16.0") //  Landscapist-Glide includes version 4.16.0
    implementation("com.github.skydoves:landscapist-glide:2.8.3")
    implementation("jp.wasabeef:glide-transformations:4.3.0") {
        exclude(group = "com.github.bumptech.glide", module = "glide")
    }
    implementation("com.github.ksoichiro:android-observablescrollview:1.6.0")
    implementation("com.roomorama:caldroid:3.0.1")
    implementation("com.nineoldandroids:library:2.4.0")
    implementation("com.simplecityapps:recyclerview-fastscroll:2.0.1")
    implementation("org.jasypt:jasypt:1.9.3")

    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:image:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:linkify:4.6.2")
    implementation("io.noties:prism4j:2.0.0")
    implementation("com.squareup:seismic:1.0.3")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
    kapt("io.noties:prism4j-bundler:2.0.0")

//    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.7")

    // android test
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0-alpha02") {
        exclude(group = "com.android.support", module = "support-annotations")
    }

    // test
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    testImplementation("androidx.test.ext:truth:1.6.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.11.1")
}
