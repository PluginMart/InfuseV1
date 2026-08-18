plugins {
    id("net.fabricmc.fabric-loom")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("infusev1") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

val libs = project.the<VersionCatalogsExtension>().named("libs")

dependencies {
    minecraft(libs.findLibrary("minecraft").get())
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
