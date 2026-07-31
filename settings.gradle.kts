pluginManagement {
    repositories {
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "realm-android") {
                useModule("io.realm:realm-gradle-plugin:10.17.0")
            }
        }
    }
}

include(":app")
// include ':app', ':aafactory-commons'
// project(':aafactory-commons').projectDir = new File(settingsDir, "../aafactory-commons/commons")
