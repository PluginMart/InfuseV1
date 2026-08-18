plugins {
    id("my-conventions")
}

dependencies {
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(project(":api"))

    include(project(":api"))

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

tasks.jar {
    inputs.property("projectName", project.name)

    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }

    archiveFileName = "${rootProject.name}-fabric-${version}.jar"
}
