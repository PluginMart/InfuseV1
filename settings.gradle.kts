pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }

    plugins {
        id("net.fabricmc.fabric-loom") version providers.gradleProperty("loom_version")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

// Should match your modid
rootProject.name = "infusev1"

include("api", "impl")