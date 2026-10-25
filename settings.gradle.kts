pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        version("26.2", "26.2")
        version("26.3", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "seamless-loading-screen-continued"
