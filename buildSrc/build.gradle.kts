plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.neoforged.net/releases")
    exclusiveContent {
        forRepositories(
            maven("https://maven.kikugie.dev/releases"),
            maven("https://maven.kikugie.dev/snapshots")
        )
        filter {
            includeGroupAndSubgroups("dev.kikugie")
        }
    }
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

    implementation(plugin("dev.isxander.modstitch.base", "0.6.2-unstable"))
    implementation(plugin("dev.kikugie.stonecutter", "0.7.1"))
    implementation(plugin("fabric-loom", "1.10.5"))
    implementation(plugin("net.neoforged.moddev", "2.0.80"))
}
