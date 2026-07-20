plugins {
    `java-library`
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
}

val javaVersion = (project.property("javaVersion") as String).toInt()
val minecraftVersion: String by project

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("${minecraftVersion}+")
}

tasks.runServer {
    minecraftVersion(minecraftVersion)
    jvmArgs("-Dlog4j.configurationFile=log4j2.xml")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.processResources {
    val props = mapOf("version" to version,
        "mcVersion" to minecraftVersion)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.register("resetAndRun") {
    delete("run/plugins/$rootProject.name")
    finalizedBy("runServer")
}