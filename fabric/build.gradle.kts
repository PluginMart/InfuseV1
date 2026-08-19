plugins {
    id("my-conventions")
}

dependencies {
    implementation(libs.fabric.loader)
    implementation(project(":common"))

    include(project(":common"))

    include(libs.configurate.core)
    include(libs.configurate.yaml)
    include(libs.checkerQual)
    include(libs.geantyref)
    include(libs.option)
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}
