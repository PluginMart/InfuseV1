plugins {
    id("my-conventions")
    `maven-publish`
}

dependencies {
    implementation(libs.configurate)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "turbo-maven"
            url = uri("https://maven.turbojax.org/releases/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
