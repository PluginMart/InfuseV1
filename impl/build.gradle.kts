plugins {
    id("my-conventions")
}

dependencies {
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(project(":api"))
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
}
