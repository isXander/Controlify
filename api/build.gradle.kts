plugins {
    `java-library`
    id("com.gradleup.nmcp")
    `maven-publish`
}

group = "dev.isxander"
val apiVersion = property("apiVersion") as String
version = apiVersion

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.joml:joml:1.10.8")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

base {
    archivesName = "controlify-api"
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand("version" to inputs.properties["version"]!!)
    }
}

publishing {
    publications {
        create<MavenPublication>("api") {
            from(components["java"])

            artifactId = "controlify-api"
            groupId = "dev.isxander"
        }
    }
}
