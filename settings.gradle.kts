pluginManagement {
    repositories {
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "aaf-easydiary"
include(":app")
