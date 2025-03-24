plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version "2.0.21" // embedded kotlin version of gradle 8.12
}

gradlePlugin {
    plugins.create("stonecutter-configurator") {
        id = "dev.isxander.stonecutter-configurator"
        implementationClass = "dev.isxander.stonecutterconfigurator.StonecutterConfiguratorPlugin"
    }
}

repositories {
    mavenCentral()
    maven("https://maven.kikugie.dev/releases")
    maven("https://maven.kikugie.dev/snapshots")
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    implementation("dev.kikugie.stonecutter:dev.kikugie.stonecutter.gradle.plugin:0.6-alpha.13")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}
