pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

rootProject.name = "infusev1"

include("common", "fabric")